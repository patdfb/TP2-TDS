package com.example.recipetds.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.recipetds.R;
import com.example.recipetds.models.Ingredient;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;

import java.util.Objects;

public class AddIngredientDialogFragment extends DialogFragment {

    public interface AddIngredientListener {
        void onIngredientAdded(Ingredient ingredient);
        void onIngredientEdited(Ingredient ingredient, int position);
    }

    private AddIngredientListener listener;
    private static final String ARG_INGREDIENT_JSON = "ingredient_json";
    private static final String ARG_POSITION = "position";

    public static AddIngredientDialogFragment newInstance(@Nullable Ingredient ingredient, int position) {
        AddIngredientDialogFragment fragment = new AddIngredientDialogFragment();
        if (ingredient != null) {
            Bundle args = new Bundle();
            args.putString(ARG_INGREDIENT_JSON, new Gson().toJson(ingredient));
            args.putInt(ARG_POSITION, position);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getParentFragment() instanceof AddIngredientListener) {
            listener = (AddIngredientListener) getParentFragment();
        } else if (context instanceof AddIngredientListener) {
            listener = (AddIngredientListener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_ingredient, null);
        EditText editName = dialogView.findViewById(R.id.editTextName);
        EditText editQuantity = dialogView.findViewById(R.id.editTextQuantity);
        AutoCompleteTextView spinnerUnit = dialogView.findViewById(R.id.spinnerUnit);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.units_array, android.R.layout.simple_list_item_1);
        spinnerUnit.setAdapter(unitAdapter);

        final Ingredient editIngredient;
        final int position;
        if (getArguments() != null && getArguments().containsKey(ARG_INGREDIENT_JSON)) {
            editIngredient = new Gson().fromJson(getArguments().getString(ARG_INGREDIENT_JSON), Ingredient.class);
            position = getArguments().getInt(ARG_POSITION);
        } else {
            editIngredient = null;
            position = -1;
        }

        if (editIngredient != null) {
            editName.setText(editIngredient.getItem());
            editQuantity.setText(String.valueOf(editIngredient.getQuantity()));
            spinnerUnit.setText(editIngredient.getUnit(), false);
        } else {
            if (unitAdapter.getCount() > 0) {
                spinnerUnit.setText(Objects.requireNonNull(unitAdapter.getItem(0)).toString(), false);
            }
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(editIngredient == null ? R.string.add_ingredient_title : R.string.edit_ingredient_title)
                .setView(dialogView)
                .setPositiveButton(editIngredient == null ? R.string.action_add : R.string.action_save, (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String quantityStr = editQuantity.getText().toString().trim();
                    String unit = spinnerUnit.getText().toString();

                    if (!name.isEmpty() && !quantityStr.isEmpty()) {
                        try {
                            double quantity = Double.parseDouble(quantityStr);
                            Ingredient ingredient = new Ingredient(name, quantity, unit);
                            if (listener != null) {
                                if (editIngredient == null) {
                                    listener.onIngredientAdded(ingredient);
                                } else {
                                    listener.onIngredientEdited(ingredient, position);
                                }
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, null);

        return builder.create();
    }
}
