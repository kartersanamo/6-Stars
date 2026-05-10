package com.sixstars.service.stripe;

import java.util.Locale;
import java.util.prefs.Preferences;

/** Persists Stripe IDs and billing profile snippets per logged-in guest. */
public final class StripeGuestPreferences {

    private static final String CONNECTED_ACCOUNT_ID = "stripe_connected_account_id";
    private static final String CUSTOMER_ID = "stripe_customer_id";

    private static final String B_NAME = "billing_name";
    private static final String B_LINE1 = "billing_line1";
    private static final String B_LINE2 = "billing_line2";
    private static final String B_CITY = "billing_city";
    private static final String B_STATE = "billing_state";
    private static final String B_ZIP = "billing_zip";
    private static final String B_PHONE = "billing_phone";

    private StripeGuestPreferences() {
    }

    static String emailSegment(String email) {
        String e = email == null ? "_" : email.trim().toLowerCase(Locale.ROOT);
        return e.replaceAll("[^a-z0-9]", "_").replace("__", "_");
    }

    private static Preferences prefsNode(String guestEmail) {
        return Preferences.userRoot()
                .node("com")
                .node("sixstars")
                .node("stripe_guest")
                .node(emailSegment(guestEmail));
    }

    public static boolean isStripeAccountConnected(String guestEmail) {
        String acct = getConnectedAccountId(guestEmail);
        return acct != null && !acct.isBlank();
    }

    public static String getConnectedAccountId(String guestEmail) {
        return prefsNode(guestEmail).get(CONNECTED_ACCOUNT_ID, "").trim();
    }

    public static void setConnectedAccountId(String guestEmail, String stripeUserId) {
        prefsNode(guestEmail).put(CONNECTED_ACCOUNT_ID, stripeUserId == null ? "" : stripeUserId.trim());
    }

    public static void clearStripeLink(String guestEmail) {
        Preferences p = prefsNode(guestEmail);
        p.remove(CONNECTED_ACCOUNT_ID);
        p.remove(CUSTOMER_ID);
    }

    public static String getStripeCustomerId(String guestEmail) {
        return prefsNode(guestEmail).get(CUSTOMER_ID, "").trim();
    }

    public static void setStripeCustomerId(String guestEmail, String customerId) {
        prefsNode(guestEmail).put(CUSTOMER_ID, customerId == null ? "" : customerId.trim());
    }

    public static void saveBillingProfile(String guestEmail, String name, String line1, String line2,
            String city, String state, String zip, String phone) {
        Preferences p = prefsNode(guestEmail);
        putIfPresent(p, B_NAME, name);
        putIfPresent(p, B_LINE1, line1);
        putIfPresent(p, B_LINE2, line2);
        putIfPresent(p, B_CITY, city);
        putIfPresent(p, B_STATE, state);
        putIfPresent(p, B_ZIP, zip);
        putIfPresent(p, B_PHONE, phone);
    }

    private static void putIfPresent(Preferences p, String key, String value) {
        p.put(key, value == null ? "" : value.trim());
    }

    public static BillingProfileSnapshot loadBillingProfile(String guestEmail) {
        Preferences p = prefsNode(guestEmail);
        return new BillingProfileSnapshot(
                p.get(B_NAME, ""),
                p.get(B_LINE1, ""),
                p.get(B_LINE2, ""),
                p.get(B_CITY, ""),
                p.get(B_STATE, ""),
                p.get(B_ZIP, ""),
                p.get(B_PHONE, "")
        );
    }

    public record BillingProfileSnapshot(String nameOnCard, String line1, String line2,
            String city, String state, String zip, String phone) {
    }
}
