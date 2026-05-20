package com.example.recipetds.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipetds.R;
import com.example.recipetds.models.Ingredient;
import com.example.recipetds.models.Recipe;
import com.example.recipetds.repository.RecipeRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    private Recipe currentRecipe;
    private EditText editTextServings;
    private List<Ingredient> userIngredients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        editTextServings = findViewById(R.id.editTextServings);

        String json = getIntent().getStringExtra("USER_INGREDIENTS_JSON");
        if (json != null) {
            Type listType = new TypeToken<List<Ingredient>>() {}.getType();
            userIngredients = new Gson().fromJson(json, listType);
        }

        int recipeId = getIntent().getIntExtra("RECIPE_ID", -1);
        if (recipeId != -1) {
            currentRecipe = RecipeRepository.getInstance(this).getRecipeById(recipeId);
            if (currentRecipe != null) {
                int baseDoses = currentRecipe.getBase_doses() > 0 ? currentRecipe.getBase_doses() : 4;
                editTextServings.setText(String.valueOf(baseDoses));
                
                displayRecipe(currentRecipe, baseDoses);
                setupServingsInput();
            }
        }

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void setupServingsInput() {
        editTextServings.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (!input.isEmpty()) {
                    try {
                        int selectedServings = Integer.parseInt(input);
                        if (selectedServings > 0) {
                            displayRecipe(currentRecipe, selectedServings);
                        }
                    } catch (NumberFormatException e) {
                        // Ignore
                    }
                }
            }
        });
    }

    private void displayRecipe(Recipe recipe, int selectedServings) {
        TextView textViewName = findViewById(R.id.textViewRecipeName);
        TextView textViewIngredients = findViewById(R.id.textViewIngredients);
        TextView textViewSteps = findViewById(R.id.textViewSteps);

        if (textViewName != null) textViewName.setText(recipe.getName());

        if (textViewIngredients != null) {
            SpannableStringBuilder ingredientsSpannable = new SpannableStringBuilder();
            List<Ingredient> ingredients = recipe.getIngredients();

            int baseDoses = recipe.getBase_doses() > 0 ? recipe.getBase_doses() : 4;
            double scaleFactor = (double) selectedServings / baseDoses;

            if (ingredients != null) {
                for (Ingredient reqIng : ingredients) {
                    boolean hasIngredient = false;
                    String reqName = reqIng.getItem().toLowerCase();

                    for (Ingredient userIng : userIngredients) {
                        if (reqName.contains(userIng.getItem().toLowerCase()) ||
                            userIng.getItem().toLowerCase().contains(reqName)) {
                            // Check if quantity is enough if units match
                            if (reqIng.getUnit().equalsIgnoreCase(userIng.getUnit())) {
                                if (userIng.getQuantity() >= (reqIng.getQuantity() * scaleFactor)) {
                                    hasIngredient = true;
                                    break;
                                }
                            } else {
                                // If units don't match, we assume we have it but don't know quantity
                                // unless we implement a conversion table.
                                // To follow the user's request of "less than quantity needed", 
                                // we'll be strict if the unit is the same.
                                // If unit is different, we currently fallback to name matching.
                                hasIngredient = true; 
                                break;
                            }
                        }
                    }

                    int start = ingredientsSpannable.length();
                    if (hasIngredient) {
                        ingredientsSpannable.append("✅ ");
                    } else {
                        ingredientsSpannable.append("❌ ");
                    }

                    double scaledQuantity = reqIng.getQuantity() * scaleFactor;
                    String quantityStr = (scaledQuantity == (long) scaledQuantity)
                            ? String.format(Locale.getDefault(), "%d", (long) scaledQuantity)
                            : String.format(Locale.getDefault(), "%.2f", scaledQuantity);

                    ingredientsSpannable.append(quantityStr)
                            .append(" ")
                            .append(reqIng.getUnit())
                            .append(" ")
                            .append(reqIng.getItem())
                            .append("\n");

                    int end = ingredientsSpannable.length();
                    int color = hasIngredient ? Color.parseColor("#2E7D32") : Color.RED;
                    ingredientsSpannable.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            textViewIngredients.setText(ingredientsSpannable);
        }

        if (textViewSteps != null) {
            StringBuilder stepsText = new StringBuilder();
            List<String> steps = recipe.getSteps();
            if (steps != null) {
                int stepNum = 1;
                for (String step : steps) {
                    stepsText.append(stepNum++).append(". ").append(step).append("\n\n");
                }
            }
            textViewSteps.setText(stepsText.toString());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        getOnBackPressedDispatcher().onBackPressed();
        return true;
    }
}
