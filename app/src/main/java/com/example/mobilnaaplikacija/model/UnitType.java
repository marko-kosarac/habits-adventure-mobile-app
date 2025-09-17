package com.example.mobilnaaplikacija.model;

public enum UnitType {
    DAN("Dan"),
    SEDMICA("Sedmica"),
    MJESEC("Mjesec"),
    GODINA("Godina");

    private final String displayName;

    UnitType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UnitType fromDisplayName(String text) {
        for (UnitType u : values()) {
            if (u.displayName.equalsIgnoreCase(text.trim())) {
                return u;
            }
        }
        throw new IllegalArgumentException("Nepoznata jedinica ponavljanja: " + text);
    }

    @Override
    public String toString() {
        return displayName;
    }
}