package com.apvereda.uDataTypes;

public enum EntityType {
    OFFER("offer"),
    ASSIST("assist");

    private final String text;

    EntityType(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public static EntityType fromText(String text) {
        for (EntityType value : values()) {
            if (value.text.equalsIgnoreCase(text)) {
                return value;
            }
        }
        return null; // valor por defecto
    }
}
