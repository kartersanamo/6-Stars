package com.sixstars.model;

public enum Theme {
    NATURE_RETREAT("Nature Retreat"),
    URBAN_ELEGANCE("Urban Elegance"),
    VINTAGE_CHARM("Vintage Charm");

    private final String displayName;

    Theme(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName; // This is what the JComboBox will show!
    }
}