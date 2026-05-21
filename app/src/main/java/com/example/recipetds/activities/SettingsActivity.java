package com.example.recipetds.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.recipetds.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import com.google.android.material.appbar.MaterialToolbar;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);

        toolbar.setNavigationOnClickListener(v -> finish());

        SwitchMaterial switchDarkMode = findViewById(R.id.switchDarkMode);

        SharedPreferences prefs =
                getSharedPreferences("settings", MODE_PRIVATE);

        boolean darkMode =
                prefs.getBoolean("dark_mode", false);

        switchDarkMode.setChecked(darkMode);

        switchDarkMode.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    prefs.edit()
                            .putBoolean("dark_mode", isChecked)
                            .apply();

                    if (isChecked) {
                        AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_YES);
                    } else {
                        AppCompatDelegate.setDefaultNightMode(
                                AppCompatDelegate.MODE_NIGHT_NO);
                    }
                });
    }
}
