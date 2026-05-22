package com.example.recipetds.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.recipetds.R;
import com.example.recipetds.fragments.RecipeListFragment;
import com.example.recipetds.models.Ingredient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        MaterialToolbar toolbar = findViewById(R.id.toolbarFavorites);
        toolbar.setNavigationOnClickListener(v -> finish());

        if (savedInstanceState == null) {
            RecipeListFragment fragment = RecipeListFragment.newInstance(true);
            
            // Pass user ingredients if available
            String json = getIntent().getStringExtra("USER_INGREDIENTS_JSON");
            if (json != null) {
                Type type = new TypeToken<List<Ingredient>>() {}.getType();
                List<Ingredient> ingredients = new Gson().fromJson(json, type);
                fragment.updatePantry(ingredients);
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container_favorites, fragment, "FAVORITES_LIST")
                    .commit();
        }
    }
}
