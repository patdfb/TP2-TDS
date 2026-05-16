package com.example.recipetds.activities;

import android.os.Bundle;
import android.view.View;
import android.text.TextUtils;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;  // Adiciona este import no topo

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetds.R;
import com.example.recipetds.adapters.RecipeAdapter;
import com.example.recipetds.models.Recipe;
import com.example.recipetds.repository.RecipeRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecipes;
    private SearchView searchView;
    private Spinner spinnerCategory;
    private RecipeAdapter adapter;
    private RecipeRepository repository;

    private List<Recipe> currentRecipes;
    private String currentSearchQuery = "";
    private String currentCategory = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize repository
        repository = RecipeRepository.getInstance(this);

        // Setup views
        setupViews();
        setupRecyclerView();
        setupSearchView();
        setupCategorySpinner();

        // Load all recipes initially
        loadRecipes();
    }

    private void setupViews() {
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        searchView = (androidx.appcompat.widget.SearchView) findViewById(R.id.searchView);
        spinnerCategory = findViewById(R.id.spinnerCategory);
    }

    private void setupRecyclerView() {
        adapter = new RecipeAdapter(recipe -> {
            // Handle recipe click - will implement later
            Toast.makeText(this, "Selected: " + recipe.getName(), Toast.LENGTH_SHORT).show();
        });

        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecipes.setAdapter(adapter);
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
        // Get unique categories from recipes
        List<Recipe> allRecipes = repository.getAllRecipes();
        Set<String> categorySet = new HashSet<>();
        categorySet.add("All");
        for (Recipe recipe : allRecipes) {
            categorySet.add(recipe.getCategory());
        }

        List<String> categories = new ArrayList<>(categorySet);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categories);
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

        // First, search by ingredients
        if (TextUtils.isEmpty(currentSearchQuery)) {
            recipes = repository.getAllRecipes();
        } else {
            recipes = repository.searchRecipesByIngredientString(currentSearchQuery);
        }

        // Then filter by category
        if (!currentCategory.equals("All")) {
            List<Recipe> filteredByCategory = new ArrayList<>();
            for (Recipe recipe : recipes) {
                if (recipe.getCategory().equals(currentCategory)) {
                    filteredByCategory.add(recipe);
                }
            }
            recipes = filteredByCategory;
        }

        currentRecipes = recipes;
        adapter.updateRecipes(recipes);

        // Show message if no results
        if (recipes.isEmpty()) {
            Toast.makeText(this, "No recipes found with those ingredients", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("search_query", currentSearchQuery);
        outState.putString("category", currentCategory);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            currentSearchQuery = savedInstanceState.getString("search_query", "");
            currentCategory = savedInstanceState.getString("category", "All");

            // Restore search view text
            if (!TextUtils.isEmpty(currentSearchQuery)) {
                searchView.setQuery(currentSearchQuery, false);
            }

            // Restore spinner selection
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategory.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(currentCategory);
                if (position >= 0) {
                    spinnerCategory.setSelection(position);
                }
            }
        }
    }
}
