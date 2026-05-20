package com.example.recipetds.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetds.R;
import com.example.recipetds.adapters.IngredientListAdapter;
import com.example.recipetds.adapters.RecipeAdapter;
import com.example.recipetds.models.Ingredient;
import com.example.recipetds.models.Recipe;
import com.example.recipetds.repository.RecipeRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecipes;
    private RecyclerView recyclerViewUserIngredients;
    private SearchView searchView;
    private Spinner spinnerCategory;
    private RecipeAdapter adapter;
    private IngredientListAdapter ingredientAdapter;
    private RecipeRepository repository;

    private TabLayout tabLayout;
    private View layoutRecipes;
    private View layoutIngredients;
    private FloatingActionButton fabAddIngredient;

    private String currentSearchQuery = "";
    private String currentCategory = "All";
    private List<Recipe> currentRecipes = new ArrayList<>();
    private final List<Ingredient> userIngredients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize repository
        repository = RecipeRepository.getInstance(this);

        // Setup views
        setupViews();
        setupRecyclerViews();
        setupSearchView();
        setupCategorySpinner();
        setupTabs();
        setupFab();

        // Load all recipes initially
        loadRecipes();
    }

    private void setupViews() {
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        recyclerViewUserIngredients = findViewById(R.id.recyclerViewUserIngredients);
        searchView = findViewById(R.id.searchView);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        tabLayout = findViewById(R.id.tabLayout);
        layoutRecipes = findViewById(R.id.layoutRecipes);
        layoutIngredients = findViewById(R.id.layoutIngredients);
        fabAddIngredient = findViewById(R.id.fabAddIngredient);
    }

    private void setupRecyclerViews() {
        adapter = new RecipeAdapter(recipe -> {
            android.content.Intent intent = new android.content.Intent(this, RecipeDetailActivity.class);
            intent.putExtra("RECIPE_ID", recipe.getId());
            intent.putExtra("USER_INGREDIENTS_JSON", new Gson().toJson(userIngredients));
            startActivity(intent);
        });

        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewRecipes.setAdapter(adapter);

        ingredientAdapter = new IngredientListAdapter(ingredient -> {
            userIngredients.remove(ingredient);
            ingredientAdapter.updateIngredients(userIngredients);
            adapter.updatePantry(userIngredients);
            loadRecipes();
        });
        recyclerViewUserIngredients.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewUserIngredients.setAdapter(ingredientAdapter);
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

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    layoutRecipes.setVisibility(View.VISIBLE);
                    layoutIngredients.setVisibility(View.GONE);
                    fabAddIngredient.setVisibility(View.GONE);
                } else {
                    layoutRecipes.setVisibility(View.GONE);
                    layoutIngredients.setVisibility(View.VISIBLE);
                    fabAddIngredient.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupFab() {
        fabAddIngredient.setOnClickListener(v -> showAddIngredientDialog());
    }

    private void showAddIngredientDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_ingredient, null);
        EditText editName = dialogView.findViewById(R.id.editTextName);
        EditText editQuantity = dialogView.findViewById(R.id.editTextQuantity);
        Spinner spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(this,
                R.array.units_array, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUnit.setAdapter(unitAdapter);

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_ingredient_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_add, (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String quantityStr = editQuantity.getText().toString().trim();
                    String unit = spinnerUnit.getSelectedItem().toString();

                    if (!name.isEmpty() && !quantityStr.isEmpty()) {
                        try {
                            double quantity = Double.parseDouble(quantityStr);
                            userIngredients.add(new Ingredient(name, quantity, unit));
                            ingredientAdapter.updateIngredients(userIngredients);
                            adapter.updatePantry(userIngredients);
                            loadRecipes();
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void loadRecipes() {
        List<Recipe> recipes;

        // First, search by ingredients
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
