package com.example.recipetds.repository;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.example.recipetds.models.Recipe;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RecipeRepository {
    private static final String TAG = "RecipeRepository";
    private static RecipeRepository instance;
    private List<Recipe> allRecipes;
    private Context context;

    private RecipeRepository (Context context) {
        this.context = context.getApplicationContext();
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
            // Read JSON file from assets folder
            String jsonString = loadJsonFromAssets("receitas.json");

            if (jsonString != null) {
                Gson gson = new Gson();
                Type recipeListType = new TypeToken<List<Recipe>>() {}.getType();
                allRecipes = gson.fromJson(jsonString, recipeListType);
                Log.d(TAG, "Loaded " + allRecipes.size() + " recipes");
            } else {
                allRecipes = new ArrayList<>();
                Log.e(TAG, "Failed to load recipes JSON");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading recipes: " + e.getMessage());
            allRecipes = new ArrayList<>();
        }
    }

    private String loadJsonFromAssets(String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public List<Recipe> getAllRecipes() {
        return new ArrayList<>(allRecipes);
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
}