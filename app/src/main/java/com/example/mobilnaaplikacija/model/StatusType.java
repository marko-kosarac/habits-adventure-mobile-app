package com.example.mobilnaaplikacija.model;

public enum StatusType {
    AKTIVAN("Aktivan"),
    URAĐEN("Urađen"),
    NEURAĐEN("Neurađen"),
    PAUZIRAN("Pauziran"),
    OTKAZAN("Otkazan");

    private final String displayName;

    StatusType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static StatusType fromDisplayName(String text) {
        for (StatusType s : values()) {
            if (s.displayName.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        throw new IllegalArgumentException("Nepoznat status: " + text);
    }

    @Override
    public String toString() {
        return displayName;
    }
}