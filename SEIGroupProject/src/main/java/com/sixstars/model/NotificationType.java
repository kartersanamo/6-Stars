package com.sixstars.model;

public enum NotificationType {
    RESERVATION_UPDATES("Reservation Updates"),
    RESERVATION_REMINDERS("Reservation Reminders"),
    SHOP_PURCHASES("Shop Purchases"),
    SHOP_PROMOTIONS("Shop Promotions"),
    ACCOUNT_ACTIVITY("Account Activity"),
    SYSTEM_ALERTS("System Alerts");

    private final String displayName;

    NotificationType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
