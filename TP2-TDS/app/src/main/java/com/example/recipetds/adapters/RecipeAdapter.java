package com.example.recipetds.adapters;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipetds.R;
import com.example.recipetds.models.Ingredient;
import com.example.recipetds.models.Recipe;
import com.example.recipetds.repository.RecipeRepository;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes = new ArrayList<>();
    private List<Ingredient> userPantry = new ArrayList<>();
    private final OnRecipeInteractionListener listener;
    private final RecipeRepository repository;

    public interface OnRecipeInteractionListener {
        void onRecipeClick(Recipe recipe);
        void onFavoriteToggle(Recipe recipe, boolean isFavorite);
    }

    public RecipeAdapter(RecipeRepository repository, OnRecipeInteractionListener listener) {
        this.repository = repository;
        this.listener = listener;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        final List<Recipe> oldList = new ArrayList<>(this.recipes);
        final List<Recipe> newList = newRecipes != null ? newRecipes : new ArrayList<>();

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).getId() == newList.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Recipe oldRecipe = oldList.get(oldItemPosition);
                Recipe newRecipe = newList.get(newItemPosition);
                return oldRecipe.getName().equals(newRecipe.getName()) &&
                       oldRecipe.getCategory().equals(newRecipe.getCategory()) &&
                       oldRecipe.getAvailableIngredientsCount(userPantry) == newRecipe.getAvailableIngredientsCount(userPantry) &&
                       repository.isFavorite(oldRecipe.getId()) == repository.isFavorite(newRecipe.getId());
            }
        });

        this.recipes = newList;
        diffResult.dispatchUpdatesTo(this);
    }

    public void updatePantry(List<Ingredient> pantry) {
        this.userPantry = pantry != null ? new ArrayList<>(pantry) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        boolean isFavorite = repository.isFavorite(recipe.getId());
        holder.bind(recipe, userPantry, isFavorite);
        
        holder.cardView.setOnClickListener(v -> listener.onRecipeClick(recipe));
        
        holder.buttonFavorite.setOnClickListener(v -> {
            boolean currentStatus = repository.isFavorite(recipe.getId());
            listener.onFavoriteToggle(recipe, !currentStatus);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final TextView textViewRecipeName;
        final TextView textViewRecipeCategory;
        final TextView textViewIngredientsCount;
        final ImageButton buttonFavorite;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewRecipe);
            textViewRecipeName = itemView.findViewById(R.id.textViewRecipeName);
            textViewRecipeCategory = itemView.findViewById(R.id.textViewRecipeCategory);
            textViewIngredientsCount = itemView.findViewById(R.id.textViewIngredientsCount);
            buttonFavorite = itemView.findViewById(R.id.buttonFavorite);
        }

        void bind(Recipe recipe, List<Ingredient> pantry, boolean isFavorite) {
            textViewRecipeName.setText(recipe.getName());
            textViewRecipeCategory.setText(recipe.getCategory());

            int total = recipe.getIngredients() != null ? recipe.getIngredients().size() : 0;
            int available = recipe.getAvailableIngredientsCount(pantry);

            String status = available + "/" + total + " ingredients available";
            textViewIngredientsCount.setText(status);

            if (available == total && total > 0) {
                textViewIngredientsCount.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
            } else {
                textViewIngredientsCount.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
            }

            if (isFavorite) {
                buttonFavorite.setImageResource(R.drawable.ic_star_filled);
                buttonFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), R.color.orange_primary)));
            } else {
                buttonFavorite.setImageResource(R.drawable.ic_star_outline);
                buttonFavorite.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray)));
            }
        }
    }
}
