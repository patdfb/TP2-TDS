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

import java.util.Objects;

public class AddIngredientDialogFragment extends DialogFragment {

    public interface AddIngredientListener {
        void onIngredientAdded(Ingredient ingredient);
    }

    private AddIngredientListener listener;

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
        
        if (unitAdapter.getCount() > 0) {
            spinnerUnit.setText(Objects.requireNonNull(unitAdapter.getItem(0)).toString(), false);
        }

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_ingredient_title)
                .setView(dialogView)
                .setPositiveButton(R.string.action_add, (dialog, which) -> {
                    String name = editName.getText().toString().trim();
                    String quantityStr = editQuantity.getText().toString().trim();
                    String unit = spinnerUnit.getText().toString();

                    if (!name.isEmpty() && !quantityStr.isEmpty()) {
                        try {
                            double quantity = Double.parseDouble(quantityStr);
                            if (listener != null) {
                                listener.onIngredientAdded(new Ingredient(name, quantity, unit));
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(requireContext(), "Invalid quantity", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .create();
    }
}
