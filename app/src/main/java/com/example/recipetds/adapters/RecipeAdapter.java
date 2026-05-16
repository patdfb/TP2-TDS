package com.example.recipetds.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetds.R;
import com.example.recipetds.models.Recipe;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes;
    private OnRecipeClickListener listener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public RecipeAdapter(OnRecipeClickListener listener) {
        this.recipes = new ArrayList<>();
        this.listener = listener;
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = (newRecipes != null)
                ? newRecipes
                : new ArrayList<>();

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
        holder.bind(recipe);

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("RECIPE_DEBUG", "Recipes: " + recipes.size());
        return recipes.size();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
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

        void bind(Recipe recipe) {
            textViewRecipeName.setText(recipe.getName());
            textViewRecipeCategory.setText(recipe.getCategory());

            if (recipe.getIngredients() != null) {
                textViewIngredientsCount.setText(
                        recipe.getIngredients().size() + " ingredients"
                );
            } else {
                textViewIngredientsCount.setText("0 ingredients");
            }
        }
    }
}