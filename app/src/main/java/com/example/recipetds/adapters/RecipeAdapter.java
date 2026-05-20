package com.example.recipetds.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipetds.R;
import com.example.recipetds.models.Ingredient;
import com.example.recipetds.models.Recipe;
import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes = new ArrayList<>();
    private List<Ingredient> userPantry = new ArrayList<>();
    private final OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.listener = listener;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        final List<Recipe> oldList = this.recipes;
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
                       oldRecipe.getAvailableIngredientsCount(userPantry) == newRecipe.getAvailableIngredientsCount(userPantry);
            }
        });

        this.recipes = newList;
        diffResult.dispatchUpdatesTo(this);
    }

    public void updatePantry(List<Ingredient> pantry) {
        this.userPantry = pantry != null ? pantry : new ArrayList<>();
        // Since pantry affects all items' available count, we need to refresh the list
        // DiffUtil can still help if we want, but since it affects the "contents" of every recipe view
        // relative to the pantry, a simple notifyDataSetChanged is often okay here, 
        // but let's try to be consistent with DiffUtil.
        updateRecipes(this.recipes);
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
        holder.bind(recipe, userPantry);
        holder.cardView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textViewRecipeName;
        TextView textViewRecipeCategory;
        TextView textViewIngredientsCount;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewRecipe);
            textViewRecipeName = itemView.findViewById(R.id.textViewRecipeName);
            textViewRecipeCategory = itemView.findViewById(R.id.textViewRecipeCategory);
            textViewIngredientsCount = itemView.findViewById(R.id.textViewIngredientsCount);
        }

        void bind(Recipe recipe, List<Ingredient> pantry) {
            textViewRecipeName.setText(recipe.getName());
            textViewRecipeCategory.setText(recipe.getCategory());

            int total = recipe.getIngredients() != null ? recipe.getIngredients().size() : 0;
            int available = recipe.getAvailableIngredientsCount(pantry);

            String status = available + "/" + total + " ingredients available";
            textViewIngredientsCount.setText(status);

            if (available == total && total > 0) {
                textViewIngredientsCount.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), android.R.color.holo_green_dark));
            } else {
                textViewIngredientsCount.setTextColor(androidx.core.content.ContextCompat.getColor(itemView.getContext(), android.R.color.darker_gray));
            }
        }
    }
}
