package com.example.recipetds.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.recipetds.models.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class RecipeRepository {
    private static final String TAG = "RecipeRepository";
    private static RecipeRepository instance;
    private List<Recipe> allRecipes;
    private final Context context;
    private final SharedPreferences favoritePrefs;

    private RecipeRepository (Context context) {
        this.context = context.getApplicationContext();
        this.favoritePrefs = context.getSharedPreferences("favorites", Context.MODE_PRIVATE);
        loadRecipes();
    }

    public static synchronized RecipeRepository getInstance(Context context) {
        if (instance == null) {
            instance = new RecipeRepository(context);
        }
        return instance;
    }

    private void loadRecipes() {
        try {
            String jsonString = loadJsonFromAssets();

            if (jsonString != null && !jsonString.isEmpty()) {
                Gson gson = new Gson();
                Type recipeListType = new TypeToken<List<Recipe>>() {}.getType();
                allRecipes = gson.fromJson(jsonString, recipeListType);
                Log.d(TAG, "Loaded " + allRecipes.size() + " recipes");
            } else {
                allRecipes = new ArrayList<>();
                Log.e(TAG, "Failed to load recipes JSON: String is null or empty");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading recipes: " + e.getMessage());
            allRecipes = new ArrayList<>();
        }
    }

    private String loadJsonFromAssets() {
        try (InputStream is = context.getAssets().open("receitas.json");
             Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name())) {
            return scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
        } catch (IOException ex) {
            Log.e(TAG, "IOException reading assets: " + ex.getMessage());
            return null;
        }
    }

    public List<Recipe> getAllRecipes() {
        return allRecipes != null ? new ArrayList<>(allRecipes) : new ArrayList<>();
    }

    public Recipe getRecipeById(int id) {
        if (allRecipes == null) return null;
        for (Recipe recipe : allRecipes) {
            if (recipe.getId() == id) {
                return recipe;
            }
        }
        return null;
    }

    public List<Recipe> searchRecipesByIngredients(List<String> ingredients) {
        if (ingredients == null || ingredients.isEmpty()) {
            return getAllRecipes();
        }

        List<Recipe> filteredRecipes = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (recipe.canBeMadeWith(ingredients)) {
                filteredRecipes.add(recipe);
            }
        }
        return filteredRecipes;
    }
    public List<Recipe> searchRecipesByIngredientString(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return getAllRecipes();
        }

        String[] ingredients = searchText.toLowerCase().trim().split(",");
        List<String> ingredientList = new ArrayList<>();
        for (String ingredient : ingredients) {
            ingredientList.add(ingredient.trim());
        }

        return searchRecipesByIngredients(ingredientList);
    }

    public boolean isFavorite(int recipeId) {
        return favoritePrefs.getBoolean(String.valueOf(recipeId), false);
    }

    public void setFavorite(int recipeId, boolean favorite) {
        favoritePrefs.edit().putBoolean(String.valueOf(recipeId), favorite).apply();
    }

    public List<Recipe> getFavoriteRecipes() {
        List<Recipe> favorites = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (isFavorite(recipe.getId())) {
                favorites.add(recipe);
            }
        }
        return favorites;
    }
}
