package com.example.recipetds.fragments;

import static android.widget.Toast.*;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetds.R;
import com.example.recipetds.activities.RecipeDetailActivity;
import com.example.recipetds.adapters.RecipeAdapter;
import com.example.recipetds.models.Ingredient;
import com.example.recipetds.models.Recipe;
import com.example.recipetds.repository.RecipeRepository;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeListFragment extends Fragment {

    private RecyclerView recyclerViewRecipes;
    private SearchView searchView;
    private Spinner spinnerCategory;
    private RecipeAdapter adapter;
    private RecipeRepository repository;

    private String currentSearchQuery = "";
    private String currentCategory = "All";
    private List<Ingredient> userIngredients = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        repository = RecipeRepository.getInstance(requireContext());

        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes);
        searchView = view.findViewById(R.id.searchView);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);

        setupRecyclerView();
        setupSearchView();
        setupCategorySpinner();

        loadRecipes();

        return view;
    }

    public void updatePantry(List<Ingredient> pantry) {
        this.userIngredients = pantry;
        if (adapter != null) {
            adapter.updatePantry(pantry);
        }
    }

    private void setupRecyclerView() {
        adapter = new RecipeAdapter(recipe -> {
            Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            intent.putExtra("USER_INGREDIENTS_JSON", new Gson().toJson(userIngredients));
            startActivity(intent);
        });

        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewRecipes.setAdapter(adapter);
        adapter.updatePantry(userIngredients);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                loadRecipes();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                loadRecipes();
                return true;
            }
        });
    }

    private void setupCategorySpinner() {
        List<Recipe> allRecipes = repository.getAllRecipes();
        Set<String> categorySet = new HashSet<>();
        categorySet.add("All");
        for (Recipe recipe : allRecipes) {
            categorySet.add(recipe.getCategory());
        }

        List<String> categories = new ArrayList<>(categorySet);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                currentCategory = parent.getItemAtPosition(position).toString();
                loadRecipes();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
                currentCategory = "All";
                loadRecipes();
            }
        });
    }

    private void loadRecipes() {
        List<Recipe> recipes;

        if (TextUtils.isEmpty(currentSearchQuery)) {
            recipes = repository.getAllRecipes();
        } else {
            List<String> searchIngredientsNames = new ArrayList<>();
            String[] queryParts = currentSearchQuery.toLowerCase().trim().split(",");
            for (String part : queryParts) {
                if (!part.trim().isEmpty()) {
                    searchIngredientsNames.add(part.trim());
                }
            }
            recipes = repository.searchRecipesByIngredients(searchIngredientsNames);
        }

        if (!currentCategory.equals("All")) {
            List<Recipe> filteredByCategory = new ArrayList<>();
            for (Recipe recipe : recipes) {
                if (recipe.getCategory().equals(currentCategory)) {
                    filteredByCategory.add(recipe);
                }
            }
            recipes = filteredByCategory;
        }

        adapter.updateRecipes(recipes);

        if (recipes.isEmpty()) {
            makeText(requireContext(), "No recipes found with those ingredients", LENGTH_SHORT).show();
        }

    }
}
