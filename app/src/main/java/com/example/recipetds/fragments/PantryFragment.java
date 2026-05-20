package com.example.recipetds.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipetds.R;
import com.example.recipetds.adapters.IngredientListAdapter;
import com.example.recipetds.models.Ingredient;

import java.util.ArrayList;
import java.util.List;

public class PantryFragment extends Fragment {

    private RecyclerView recyclerViewUserIngredients;
    private IngredientListAdapter ingredientAdapter;
    private final List<Ingredient> userIngredients = new ArrayList<>();
    
    private OnPantryChangedListener listener;

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
        View view = inflater.inflate(R.layout.fragment_pantry, container, false);

        recyclerViewUserIngredients = view.findViewById(R.id.recyclerViewUserIngredients);
        setupRecyclerView();

        return view;
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
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_ingredient, null);
        EditText editName = dialogView.findViewById(R.id.editTextName);
        EditText editQuantity = dialogView.findViewById(R.id.editTextQuantity);
        AutoCompleteTextView unitAutoComplete = dialogView.findViewById(R.id.spinnerUnit);

        String[] units = getResources().getStringArray(R.array.units_array);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, units);
        unitAutoComplete.setAdapter(unitAdapter);
        
        // Set a default unit
        if (units.length > 0) {
            unitAutoComplete.setText(units[0], false);
        }

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.add_ingredient_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_add, (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String quantityStr = editQuantity.getText().toString().trim();
                    String unit = unitAutoComplete.getText().toString();

                    if (!name.isEmpty() && !quantityStr.isEmpty()) {
                        try {
                            double quantity = Double.parseDouble(quantityStr);
                            userIngredients.add(new Ingredient(name, quantity, unit));
                            ingredientAdapter.updateIngredients(userIngredients);
                            if (listener != null) {
                                listener.onPantryChanged(userIngredients);
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
