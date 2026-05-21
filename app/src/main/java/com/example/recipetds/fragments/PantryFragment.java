package com.example.recipetds.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private IngredientListAdapter ingredientAdapter;
    private final List<Ingredient> userIngredients = new ArrayList<>();
    
    private OnPantryChangedListener listener;

    @Override
    public void onIngredientAdded(Ingredient ingredient) {
        userIngredients.add(ingredient);
        if (ingredientAdapter != null) {
            ingredientAdapter.updateIngredients(userIngredients);
        }
        if (listener != null) {
            listener.onPantryChanged(userIngredients);
        }
    }

    public void setInitialPantry(List<Ingredient> pantry) {
        userIngredients.clear();
        userIngredients.addAll(pantry);
        if (ingredientAdapter != null) {
            ingredientAdapter.updateIngredients(userIngredients);
        }
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
        setupRecyclerView();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("user_ingredients", new Gson().toJson(userIngredients));
    }

    private void setupRecyclerView() {
        ingredientAdapter = new IngredientListAdapter(ingredient -> {
            userIngredients.remove(ingredient);
            ingredientAdapter.updateIngredients(userIngredients);
            if (listener != null) {
                listener.onPantryChanged(userIngredients);
            }
        });
        recyclerViewUserIngredients.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewUserIngredients.setAdapter(ingredientAdapter);
        ingredientAdapter.updateIngredients(userIngredients);
    }

    public void showAddIngredientDialog() {
        AddIngredientDialogFragment dialog = new AddIngredientDialogFragment();
        dialog.show(getChildFragmentManager(), "AddIngredientDialog");
    }
}
