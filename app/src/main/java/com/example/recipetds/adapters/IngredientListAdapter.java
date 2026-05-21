package com.example.recipetds.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipetds.R;
import com.example.recipetds.models.Ingredient;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IngredientListAdapter extends RecyclerView.Adapter<IngredientListAdapter.ViewHolder> {

    private List<Ingredient> ingredients;
    private final OnIngredientRemovedListener listener;

    public interface OnIngredientRemovedListener {
        void onIngredientRemoved(Ingredient ingredient);
    }

    public IngredientListAdapter(OnIngredientRemovedListener listener) {
        this.ingredients = new ArrayList<>();
        this.listener = listener;
    }

    public void updateIngredients(List<Ingredient> newIngredients) {
        final List<Ingredient> oldList = this.ingredients;
        final List<Ingredient> newList = new ArrayList<>(newIngredients);
        
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                // If they have unique IDs, use them. Otherwise, compare name and unit.
                Ingredient oldItem = oldList.get(oldItemPosition);
                Ingredient newItem = newList.get(newItemPosition);
                return oldItem.getItem().equals(newItem.getItem()) && 
                       oldItem.getUnit().equals(newItem.getUnit());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Ingredient oldItem = oldList.get(oldItemPosition);
                Ingredient newItem = newList.get(newItemPosition);
                return oldItem.getItem().equals(newItem.getItem()) &&
                       oldItem.getQuantity() == newItem.getQuantity() &&
                       oldItem.getUnit().equals(newItem.getUnit());
            }
        });

        this.ingredients = newList;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_ingredient, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.textViewName.setText(ingredient.getItem());
        
        String quantityStr = (ingredient.getQuantity() == (long) ingredient.getQuantity())
                ? String.format(Locale.getDefault(), "%d", (long) ingredient.getQuantity())
                : String.format(Locale.getDefault(), "%.2f", ingredient.getQuantity());
        
        holder.textViewQuantity.setText(String.format("%s %s", quantityStr, ingredient.getUnit()));
        
        holder.buttonRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIngredientRemoved(ingredient);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textViewName;
        final TextView textViewQuantity;
        final ImageButton buttonRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewIngredientName);
            textViewQuantity = itemView.findViewById(R.id.textViewIngredientQuantity);
            buttonRemove = itemView.findViewById(R.id.buttonRemoveIngredient);
        }
    }
}
