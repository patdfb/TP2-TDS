package com.example.recipetds.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Recipe {
    private int id;
    @SerializedName("nome")
    private String name;
    @SerializedName("categoria")
    private String category;
    @SerializedName("doses_base")
    private int base_doses;
    @SerializedName("ingredientes")
    private List<Ingredient> ingredients;
    @SerializedName("passos")
    private List<String> steps;

    public Recipe() {}

    public Recipe(int id, String name, String category, int base_doses,
                  List<Ingredient> ingredients, List<String> steps) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.base_doses = base_doses;
        this.ingredients = ingredients;
        this.steps = steps;
    }

    public int getId() { return id; }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getBase_doses() {
        return base_doses;
    }

    public void setBase_doses(int base_doses) {
        this.base_doses = base_doses;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public boolean canBeMadeWith(List<String> availableIngredients) {
        if (availableIngredients == null || availableIngredients.isEmpty()) {
            return false;
        }

        for (String required : availableIngredients) {
            boolean found = false;

            for (Ingredient ingredient : ingredients) {
                if (ingredient.getItem().toLowerCase().contains(required.toLowerCase().trim())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return false;
            }
        }

        return true;
    }
}