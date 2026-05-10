package com.sixstars.model;

/**
 * Notification channels for email / in-app delivery. Grouped by {@link #getCategory()} for the Account Center UI.
 */
public enum NotificationType {

    // —— Reservations & stay ——
    RESERVATION_CONFIRMED("Confirmation & itinerary", "Reservations & stay"),
    RESERVATION_UPDATES("Booking changes & cancellations", "Reservations & stay"),
    RESERVATION_REMINDERS("Check-in & check-out reminders", "Reservations & stay"),
    PRE_ARRIVAL_PREFERENCES("Pre-arrival & room preferences", "Reservations & stay"),
    WAITLIST_AND_AVAILABILITY("Waitlist & last-minute availability", "Reservations & stay"),
    EARLY_LATE_STAY("Early check-in / late checkout", "Reservations & stay"),

    // —— Charges & folio ——
    FOLIO_AND_CHARGES("Folio, charges & adjustments", "Charges & folio"),
    INVOICES_AND_RECEIPTS("Invoices & tax receipts", "Charges & folio"),
    PAYMENTS_AND_CARDS("Payments & saved cards", "Charges & folio"),
    INCIDENTALS_AND_FEES("Incidentals, holds & fees", "Charges & folio"),

    // —— Shop & dining ——
    SHOP_PURCHASES("Shop purchase confirmations", "Shop & dining"),
    ORDER_STATUS("Order ready / pickup / delivery", "Shop & dining"),
    IN_ROOM_DINING("In-room dining & menus", "Shop & dining"),
    SPECIAL_AMENITIES("Special requests & celebrations", "Shop & dining"),

    // —— Offers & loyalty ——
    SHOP_PROMOTIONS("Promotions & limited-time offers", "Offers & loyalty"),
    LOYALTY_AND_POINTS("Points, tier & rewards", "Offers & loyalty"),
    PERSONALIZED_OFFERS("Personalized deals for you", "Offers & loyalty"),
    SURVEYS_AND_FEEDBACK("Surveys & post-stay feedback", "Offers & loyalty"),

    // —— Messaging ——
    CONCIERGE_AND_STAFF("Concierge & staff messages", "Messaging"),
    IN_APP_CHAT_REPLIES("In-app chat replies", "Messaging"),

    // —— Security & account ——
    ACCOUNT_ACTIVITY("Account & profile updates", "Security & account"),
    SECURITY_SIGN_IN("Sign-in & new device alerts", "Security & account"),
    PASSWORD_AND_VERIFICATION("Password & verification codes", "Security & account"),
    PRIVACY_AND_POLICY("Privacy & policy updates", "Security & account"),

    // —— On-property ——
    HOUSEKEEPING_AND_SERVICE("Housekeeping & service timing", "On-property"),
    MAINTENANCE_AND_QUIET_HOURS("Maintenance, noise & closures", "On-property"),

    // —— Experiences & travel ——
    SPA_WELLNESS_AND_POOL("Spa, pool & fitness", "Experiences & travel"),
    LOCAL_EVENTS("Local events & hotel happenings", "Experiences & travel"),
    TRANSPORT_AND_PARKING("Shuttle, parking & directions", "Experiences & travel"),

    // —— System ——
    SYSTEM_ALERTS("Operational alerts & issues", "System"),
    APP_UPDATES_AND_LEGAL("App updates & terms of use", "System");

    private final String displayName;
    private final String category;

    NotificationType(String displayName, String category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Used to group rows on the Notifications settings page. */
    public String getCategory() {
        return category;
    }
}
