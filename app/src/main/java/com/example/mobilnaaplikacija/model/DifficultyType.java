package com.example.mobilnaaplikacija.model;

import androidx.annotation.NonNull;

public enum DifficultyType{
    VEOMA_LAK("Veoma lak"),
    LAK("Lak"),
    TEŽAK("Težak"),
    EKSTREMNO_TEŽAK("Ekstremno težak");

    private final String displayName;

    DifficultyType(String displayName) {
        this.displayName = displayName;
    }

    public static DifficultyType fromDisplayName(String text) {
        for (DifficultyType d : values()) {
            if (d.displayName.equalsIgnoreCase(text.trim())) {
                return d;
            }
        }
        throw new IllegalArgumentException("Nepoznata težina: " + text);
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    @NonNull
    public String toString() {
        return displayName;
    }
}

