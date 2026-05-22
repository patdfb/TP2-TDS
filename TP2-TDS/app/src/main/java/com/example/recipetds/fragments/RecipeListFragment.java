package com.example.recipetds.fragments;

import static android.widget.Toast.*;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetds.R;
import com.example.recipetds.activities.FavoritesActivity;
import com.example.recipetds.activities.RecipeDetailActivity;
import com.example.recipetds.adapters.RecipeAdapter;
import com.example.recipetds.models.Ingredient;
import com.example.recipetds.models.Recipe;
import com.example.recipetds.repository.RecipeRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.speech.RecognizerIntent;
import android.widget.ImageButton;
import java.util.Locale;

public class RecipeListFragment extends Fragment {

    private RecyclerView recyclerViewRecipes;
    private SearchView searchView;
    private Spinner spinnerCategory;
    private RecipeAdapter adapter;
    private RecipeRepository repository;

    private String currentSearchQuery = "";
    private String currentCategory = "All";
    private List<Ingredient> userIngredients = new ArrayList<>();
    private boolean isFavoritesOnly = false;

    private static final int VOICE_SEARCH_REQUEST_CODE = 101;

    private ImageButton buttonVoiceSearch;

    public static RecipeListFragment newInstance(boolean favoritesOnly) {
        RecipeListFragment fragment = new RecipeListFragment();
        Bundle args = new Bundle();
        args.putBoolean("favorites_only", favoritesOnly);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            isFavoritesOnly = getArguments().getBoolean("favorites_only", false);
        }

        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        if (savedInstanceState != null) {
            currentSearchQuery = savedInstanceState.getString("search_query", "");
            currentCategory = savedInstanceState.getString("category", "All");
            String json = savedInstanceState.getString("user_ingredients", null);
            if (json != null) {
                Type type = new TypeToken<List<Ingredient>>() {}.getType();
                userIngredients = new Gson().fromJson(json, type);
            }
        }

        repository = RecipeRepository.getInstance(requireContext());

        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes);
        searchView = view.findViewById(R.id.searchView);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Button buttonViewFavorites = view.findViewById(R.id.buttonViewFavorites);

        buttonVoiceSearch = view.findViewById(R.id.buttonVoiceSearch);
        if (buttonVoiceSearch != null) {
            buttonVoiceSearch.setOnClickListener(v -> startVoiceSearch());
        }

        if (isFavoritesOnly) {
            if (buttonViewFavorites != null) {
                buttonViewFavorites.setVisibility(View.GONE);
            }
        } else {
            if (buttonViewFavorites != null) {
                buttonViewFavorites.setOnClickListener(v -> {
                    Intent intent = new Intent(requireContext(), FavoritesActivity.class);
                    intent.putExtra("USER_INGREDIENTS_JSON", new Gson().toJson(userIngredients));
                    startActivity(intent);
                });
            }
        }

        setupRecyclerView();
        setupSearchView();
        setupCategorySpinner();

        loadRecipes();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload in case favorites were changed in another tab or detail view
        loadRecipes();
    }

    public void updatePantry(List<Ingredient> pantry) {
        this.userIngredients = pantry;
        if (adapter != null) {
            adapter.updatePantry(pantry);
            loadRecipes();
        }
    }

    private void setupRecyclerView() {
        adapter = new RecipeAdapter(repository, new RecipeAdapter.OnRecipeInteractionListener() {
            @Override
            public void onRecipeClick(Recipe recipe) {
                Intent intent = new Intent(requireContext(), RecipeDetailActivity.class);
                intent.putExtra("RECIPE_ID", recipe.getId());
                intent.putExtra("USER_INGREDIENTS_JSON", new Gson().toJson(userIngredients));
                startActivity(intent);
            }

            @Override
            public void onFavoriteToggle(Recipe recipe, boolean isFavorite) {
                repository.setFavorite(recipe.getId(), isFavorite);
                if (isFavoritesOnly && !isFavorite) {
                    loadRecipes(); // Remove from list if we are in favorites view
                }
            }
        });

        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewRecipes.setAdapter(adapter);
        adapter.updatePantry(userIngredients);
    }

    private void setupSearchView() {
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            searchView.setQuery(currentSearchQuery, false);
            searchView.setIconified(false);
            searchView.clearFocus();
        }
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
        java.util.Collections.sort(categories);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        if (!currentCategory.equals("All")) {
            int position = categories.indexOf(currentCategory);
            if (position >= 0) {
                spinnerCategory.setSelection(position);
            }
        }

        spinnerCategory.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = parent.getItemAtPosition(position).toString();
                if (!currentCategory.equals(selectedCategory)) {
                    currentCategory = selectedCategory;
                    loadRecipes();
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
    }

    private void startVoiceSearch() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pt-PT");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say an ingredient name...");

        PackageManager pm = requireContext().getPackageManager();
        if (intent.resolveActivity(pm) != null) {
            try {
                startActivityForResult(intent, VOICE_SEARCH_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                makeText(requireContext(),
                        "Voice recognition not available on this device",
                        LENGTH_SHORT).show();
            }
        } else {
            makeText(requireContext(),
                    "Voice recognition not supported",
                    LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VOICE_SEARCH_REQUEST_CODE
                && resultCode == Activity.RESULT_OK
                && data != null) {

            ArrayList<String> results =
                    data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);

                currentSearchQuery = spokenText;
                searchView.setQuery(spokenText, false);
                searchView.setIconified(false);
                loadRecipes();
            }
        }
    }

    private void loadRecipes() {
        if (adapter == null) return;
        
        List<Recipe> recipes;
        if (isFavoritesOnly) {
            recipes = repository.getFavoriteRecipes();
        } else {
            recipes = repository.getAllRecipes();
        }

        // Apply search filter if present
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            List<Recipe> searchResults;
            if (currentSearchQuery.contains(",")) {
                // Search by multiple ingredients
                List<String> searchIngredientsNames = new ArrayList<>();
                String[] queryParts = currentSearchQuery.toLowerCase().trim().split(",");
                for (String part : queryParts) {
                    if (!part.trim().isEmpty()) {
                        searchIngredientsNames.add(part.trim());
                    }
                }
                searchResults = repository.searchRecipesByIngredients(searchIngredientsNames);
            } else {
                // Search by single ingredient name or recipe name (more flexible)
                searchResults = repository.searchRecipesByIngredientString(currentSearchQuery);
            }

            // Filter current list (either all or favorites) by search results
            List<Recipe> filteredList = new ArrayList<>();
            for (Recipe recipe : recipes) {
                for (Recipe searchResult : searchResults) {
                    if (recipe.getId() == searchResult.getId()) {
                        filteredList.add(recipe);
                        break;
                    }
                }
            }
            recipes = filteredList;
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

        if (recipes.isEmpty() && isResumed()) {
            makeText(requireContext(), "No recipes found with those ingredients", LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("search_query", currentSearchQuery);
        outState.putString("category", currentCategory);
        outState.putString("user_ingredients", new Gson().toJson(userIngredients));
    }
}
