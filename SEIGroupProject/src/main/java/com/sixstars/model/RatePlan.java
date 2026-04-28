package com.sixstars.model;

public enum RatePlan {
    STANDARD("Standard Rate", 0.0),
    PROMOTION("Promotion Rate", 0.15),
    GROUP("Group Rate", 0.12),
    NON_REFUNDABLE("Non-Refundable Rate", 0.20);

    private final String displayName;
    private final double discountPercent;

    RatePlan(String displayName, double discountPercent) {
        this.displayName = displayName;
        this.discountPercent = discountPercent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDiscountPercent() {
        return discountPercent;
    }

    public int applyDiscount(int baseRate) {
        double discounted = baseRate * (1.0 - discountPercent);
        return (int) Math.round(discounted);
    }

    @Override
    public String toString() {
        if (discountPercent <= 0) {
            return displayName;
        }
        int percent = (int) Math.round(discountPercent * 100);
        return displayName + " (-" + percent + "%)";
    }
}
