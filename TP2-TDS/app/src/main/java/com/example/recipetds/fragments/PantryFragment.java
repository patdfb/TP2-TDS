package com.example.recipetds.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetds.R;
import com.example.recipetds.adapters.IngredientListAdapter;
import com.example.recipetds.models.Ingredient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PantryFragment extends Fragment implements AddIngredientDialogFragment.AddIngredientListener {

    private RecyclerView recyclerViewUserIngredients;
    private SearchView searchViewPantry;
    private IngredientListAdapter ingredientAdapter;
    private final List<Ingredient> userIngredients = new ArrayList<>();
    private String currentSearchQuery = "";

    private OnPantryChangedListener listener;

    @Override
    public void onIngredientAdded(Ingredient ingredient) {
        userIngredients.add(ingredient);
        updateUI();
    }

    @Override
    public void onIngredientEdited(Ingredient ingredient, int position) {
        if (position >= 0 && position < userIngredients.size()) {
            userIngredients.set(position, ingredient);
            updateUI();
        }
    }

    private void updateUI() {
        filterIngredients();
        if (listener != null) {
            listener.onPantryChanged(userIngredients);
        }
    }

    public void setInitialPantry(List<Ingredient> pantry) {
        userIngredients.clear();
        userIngredients.addAll(pantry);
        filterIngredients();
    }

    public interface OnPantryChangedListener {
        void onPantryChanged(List<Ingredient> pantry);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnPantryChangedListener) {
            listener = (OnPantryChangedListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            currentSearchQuery = savedInstanceState.getString("search_query", "");
            String json = savedInstanceState.getString("user_ingredients", null);
            if (json != null) {
                Type type = new TypeToken<List<Ingredient>>() {}.getType();
                List<Ingredient> saved = new Gson().fromJson(json, type);
                userIngredients.clear();
                userIngredients.addAll(saved);
            }
        }
        View view = inflater.inflate(R.layout.fragment_pantry, container, false);

        recyclerViewUserIngredients = view.findViewById(R.id.recyclerViewUserIngredients);
        searchViewPantry = view.findViewById(R.id.searchViewPantry);

        setupRecyclerView();
        setupSearchView();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("search_query", currentSearchQuery);
        outState.putString("user_ingredients", new Gson().toJson(userIngredients));
    }

    private void setupRecyclerView() {
        ingredientAdapter = new IngredientListAdapter(new IngredientListAdapter.OnIngredientInteractionListener() {
            @Override
            public void onIngredientRemoved(Ingredient ingredient) {
                userIngredients.remove(ingredient);
                updateUI();
            }

            @Override
            public void onIngredientClicked(Ingredient ingredient, int position) {
                // Find the actual position in the full list
                int actualPosition = userIngredients.indexOf(ingredient);
                AddIngredientDialogFragment dialog = AddIngredientDialogFragment.newInstance(ingredient, actualPosition);
                dialog.show(getChildFragmentManager(), "EditIngredientDialog");
            }
        });
        recyclerViewUserIngredients.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewUserIngredients.setAdapter(ingredientAdapter);
        filterIngredients();
    }

    private void setupSearchView() {
        if (!TextUtils.isEmpty(currentSearchQuery)) {
            searchViewPantry.setQuery(currentSearchQuery, false);
            searchViewPantry.clearFocus();
        }
        searchViewPantry.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                currentSearchQuery = query;
                filterIngredients();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentSearchQuery = newText;
                filterIngredients();
                return true;
            }
        });
    }

    private void filterIngredients() {
        if (ingredientAdapter == null) return;

        if (TextUtils.isEmpty(currentSearchQuery)) {
            ingredientAdapter.updateIngredients(userIngredients);
        } else {
            List<Ingredient> filteredList = new ArrayList<>();
            String query = currentSearchQuery.toLowerCase().trim();
            for (Ingredient ingredient : userIngredients) {
                if (ingredient.getItem().toLowerCase().contains(query)) {
                    filteredList.add(ingredient);
                }
            }
            ingredientAdapter.updateIngredients(filteredList);
        }
    }

    public void showAddIngredientDialog() {
        AddIngredientDialogFragment dialog = AddIngredientDialogFragment.newInstance(null, -1);
        dialog.show(getChildFragmentManager(), "AddIngredientDialog");
    }
}
