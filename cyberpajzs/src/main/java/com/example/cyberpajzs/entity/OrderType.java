package com.example.cyberpajzs.entity;

public enum OrderType {
    PERSONAL("Személyes"),
    BUSINESS("Céges");

    private final String displayName;

    OrderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
