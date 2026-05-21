package com.example.recipetds.models;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
public class Ingredient {
    @SerializedName("item")
    private String item;
    @SerializedName("quantidade")
    private double quantity;
    @SerializedName("unidade")
    private String unit;

    public Ingredient() {}

    public Ingredient (String item, double quantity, String unit) {
        this.item = item;
        this.quantity = quantity;
        this.unit = unit;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @NonNull
    @Override
    public String toString() {
        return quantity + " " + unit + " " + item;
    }
}