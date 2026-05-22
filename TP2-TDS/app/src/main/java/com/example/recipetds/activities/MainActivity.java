package com.example.recipetds.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.recipetds.R;
import com.example.recipetds.fragments.PantryFragment;
import com.example.recipetds.fragments.RecipeListFragment;
import com.example.recipetds.models.Ingredient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.appbar.MaterialToolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements PantryFragment.OnPantryChangedListener {

    private TabLayout tabLayout;
    private FloatingActionButton fabAddIngredient;
    
    private RecipeListFragment recipeListFragment;
    private PantryFragment pantryFragment;
    private FragmentManager fragmentManager;
    private final List<Ingredient> userIngredients = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs =
                getSharedPreferences("settings", MODE_PRIVATE);

        boolean darkMode =
                prefs.getBoolean("dark_mode", false);

        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
        setupTabs();
        setupFab();
        loadSavedIngredients();

        fragmentManager = getSupportFragmentManager();
        
        if (savedInstanceState != null) {
            String json = savedInstanceState.getString("user_ingredients", null);
            if (json != null) {
                Type type = new TypeToken<List<Ingredient>>() {}.getType();
                List<Ingredient> restored = new Gson().fromJson(json, type);
                userIngredients.clear();
                userIngredients.addAll(restored);
            }

            recipeListFragment = (RecipeListFragment) fragmentManager.findFragmentByTag("RECIPE_LIST");
            pantryFragment = (PantryFragment) fragmentManager.findFragmentByTag("PANTRY");
            
            int selectedTab = savedInstanceState.getInt("selected_tab", 0);
            TabLayout.Tab tab = tabLayout.getTabAt(selectedTab);
            if (tab != null) {
                tab.select();
            }
        }

        if (recipeListFragment == null) {
            recipeListFragment = RecipeListFragment.newInstance(false);
        }
        recipeListFragment.updatePantry(userIngredients);
        
        if (pantryFragment == null) {
            pantryFragment = new PantryFragment();
        }
        pantryFragment.setInitialPantry(userIngredients);

        updateFragmentVisibility(tabLayout.getSelectedTabPosition());
    }

    private void setupViews() {
        tabLayout = findViewById(R.id.tabLayout);
        fabAddIngredient = findViewById(R.id.fabAddIngredient);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.inflateMenu(R.menu.main_menu);

        toolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.action_settings) {

                Intent intent =
                        new Intent(this, SettingsActivity.class);

                startActivity(intent);

                return true;
            }

            return false;
        });
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                updateFragmentVisibility(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void updateFragmentVisibility(int position) {
        if (position == 0) {
            showFragment(recipeListFragment, "RECIPE_LIST");
            fabAddIngredient.setVisibility(View.GONE);
        } else {
            showFragment(pantryFragment, "PANTRY");
            fabAddIngredient.setVisibility(View.VISIBLE);
        }
    }

    private void setupFab() {
        fabAddIngredient.setOnClickListener(v -> {
            if (pantryFragment != null) {
                pantryFragment.showAddIngredientDialog();
            }
        });
    }

    private void showFragment(Fragment fragment, String tag) {
        if (fragment.isAdded() && fragment.isVisible()) {
            return;
        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, tag);
        transaction.commit();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selected_tab", tabLayout.getSelectedTabPosition());
        outState.putString("user_ingredients", new Gson().toJson(userIngredients));
    }

    @Override
    public void onPantryChanged(List<Ingredient> pantry) {
        userIngredients.clear();
        userIngredients.addAll(pantry);
        saveIngredients();
        if (recipeListFragment != null) {
            recipeListFragment.updatePantry(userIngredients);
        }
    }

    private void saveIngredients() {

        SharedPreferences prefs =
                getSharedPreferences("ingredients", MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();

        String json =
                new Gson().toJson(userIngredients);

        editor.putString("user_ingredients", json);

        editor.apply();
    }

    private void loadSavedIngredients() {

        SharedPreferences prefs =
                getSharedPreferences("ingredients", MODE_PRIVATE);

        String json =
                prefs.getString("user_ingredients", null);

        if (json != null) {

            Type type =
                    new TypeToken<List<Ingredient>>() {}.getType();

            List<Ingredient> savedIngredients =
                    new Gson().fromJson(json, type);

            userIngredients.clear();

            userIngredients.addAll(savedIngredients);
        }
    }
}
