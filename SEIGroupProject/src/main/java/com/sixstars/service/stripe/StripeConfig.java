package com.sixstars.service.stripe;

/**
 * Sandbox/test Stripe wiring via environment variables or .env (loaded into System properties).
 * <p>
 * Register these exact URLs in Stripe Dashboard (test mode):
 * Connect redirect: {@value #OAUTH_FULL_URL}
 * Checkout success/cancel hosts: ports {@link #CHECKOUT_HTTP_PORT}.
 */
public final class StripeConfig {

    /** Port for Stripe Connect OAuth callback (must match Stripe Connect settings). */
    public static final int OAUTH_HTTP_PORT = 45263;
    /** Port for Stripe Checkout redirect after pay / saved-card flows. */
    public static final int CHECKOUT_HTTP_PORT = 45264;

    public static final String OAUTH_CALLBACK_PATH = "/stripe/oauth/callback";
    public static final String CHECKOUT_SUCCESS_PATH = "/stripe/checkout/done";
    public static final String CHECKOUT_CANCEL_PATH = "/stripe/checkout/cancel";

    public static final String OAUTH_FULL_URL = "http://127.0.0.1:" + OAUTH_HTTP_PORT + OAUTH_CALLBACK_PATH;

    public static String oauthRedirectUri() {
        return OAUTH_FULL_URL;
    }

    public static String checkoutSuccessUrlTemplateWithSessionMacro() {
        return "http://127.0.0.1:" + CHECKOUT_HTTP_PORT + CHECKOUT_SUCCESS_PATH + "?session_id={CHECKOUT_SESSION_ID}";
    }

    public static String checkoutCancelUrl() {
        return "http://127.0.0.1:" + CHECKOUT_HTTP_PORT + CHECKOUT_CANCEL_PATH;
    }

    private StripeConfig() {
    }

    private static String prop(String key) {
        String v = System.getenv(key);
        return v != null ? v : System.getProperty(key);
    }

    /** Secret API key ({@code sk_test_...}). */
    public static String getSecretKey() {
        return trimOrNull(prop("STRIPE_SECRET_KEY"));
    }

    /** Connect OAuth application client ID ({@code ca_...}). */
    public static String getConnectClientId() {
        return trimOrNull(prop("STRIPE_CONNECT_CLIENT_ID"));
    }

    /** Optional publishable key for display/logging only ({@code pk_test_...}). */
    public static String getPublishableKey() {
        return trimOrNull(prop("STRIPE_PUBLISHABLE_KEY"));
    }

    private static String trimOrNull(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    public static boolean hasSecretKey() {
        return getSecretKey() != null;
    }

    public static boolean hasConnectClientId() {
        return getConnectClientId() != null;
    }
}
