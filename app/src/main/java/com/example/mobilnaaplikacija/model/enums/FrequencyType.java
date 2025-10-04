package com.example.mobilnaaplikacija.model.enums;

public enum FrequencyType {
        JEDNOKRATAN("Jednokratan"),
        PONAVLJAJUCI("Ponavljajući");

        private final String displayName;

        FrequencyType(String displayName) {
                this.displayName = displayName;
        }

        public String getDisplayName() {
                return displayName;
        }

        public static FrequencyType fromDisplayName(String text) {
                for (FrequencyType f : values()) {
                        if (f.displayName.equalsIgnoreCase(text.trim())) {
                                return f;
                        }
                }
                throw new IllegalArgumentException("Nepoznat tip ponavljanja: " + text);
        }

        @Override
        public String toString() {
                return displayName;
        }
}
