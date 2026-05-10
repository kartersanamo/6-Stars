package com.sixstars.model;

public enum PaymentKind {
    STRIPE_CHECKOUT("Stripe Checkout"),
    SAVED_CARD_SIMULATED("Saved card (simulated)");

    private final String display;

    PaymentKind(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }

    public static PaymentKind fromStored(String name) {
        if (name == null || name.isBlank()) {
            return SAVED_CARD_SIMULATED;
        }
        try {
            return PaymentKind.valueOf(name.trim());
        } catch (IllegalArgumentException ex) {
            return SAVED_CARD_SIMULATED;
        }
    }
}
