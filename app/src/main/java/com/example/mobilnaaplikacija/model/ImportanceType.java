package com.example.mobilnaaplikacija.model;

public enum ImportanceType {
    NORMALAN("Normalan"),
    VAŽAN("Važan"),
    EKSTREMNO_VAŽAN("Ekstremno važan"),
    SPECIJALAN("Specijalan");

    private final String displayName;

    ImportanceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {

        return displayName;
    }

    public static ImportanceType fromDisplayName(String text) {
        for (ImportanceType i : values()) {
            if (i.displayName.equalsIgnoreCase(text.trim())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Nepoznata bitnost: " + text);
    }

    @Override
    public String toString() {
        return displayName;
    }
}
