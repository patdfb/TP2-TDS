package com.example.firstcontactapp;

/**
 * Represents a country with its flag emoji and English name.
 */
public class Recipe {

    private final String flag;
    private final String name;

    public Country(String flag, String name) {
        this.flag = flag;
        this.name = name;
    }

    public String getFlag() {
        return flag;
    }

    public String getName() {
        return name;
    }
}
