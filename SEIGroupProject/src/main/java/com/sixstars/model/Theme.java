package com.sixstars.model;

public enum Theme {
    NATURE_RETREAT,
    URBAN_ELEGANCE,
    VINTAGE_CHARM;

    @Override
    public String toString() {
        switch (this) {
            case NATURE_RETREAT: return "Nature Retreat";
            case URBAN_ELEGANCE: return "Urban Elegance";
            case VINTAGE_CHARM: return "Vintage Charm";
            default: return super.toString();
        }
    }
}
