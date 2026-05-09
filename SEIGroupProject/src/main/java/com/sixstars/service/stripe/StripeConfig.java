package com.sixstars.service.stripe;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Sandbox/test Stripe wiring via environment variables or .env (loaded into System properties).
 * <p>
 * Register these exact URLs in Stripe Dashboard (test mode):
 * Connect redirect: {@value #OAUTH_FULL_URL} (or override with {@code STRIPE_OAUTH_REDIRECT_URI})
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

    private record ResolvedOAuth(String listenHost, int listenPort, String callbackPath, String redirectUriForStripe) {
    }

    private static volatile ResolvedOAuth cachedOAuthBinding;

    /**
     * Optional override when Stripe Dashboard only allows {@code localhost} or you need an exact string match.
     * Example: {@code STRIPE_OAUTH_REDIRECT_URI=http://localhost:45263/stripe/oauth/callback}
     * <p>Must be {@code http}, host {@code 127.0.0.1} or {@code localhost}, and path must match what you register in Stripe.</p>
     */
    public static String oauthRedirectUri() {
        return resolvedOAuth().redirectUriForStripe();
    }

    /** Host the embedded HTTP server binds for Connect OAuth callback. */
    public static String oauthListenHost() {
        return resolvedOAuth().listenHost();
    }

    public static int oauthListenPort() {
        return resolvedOAuth().listenPort();
    }

    public static String oauthCallbackPath() {
        return resolvedOAuth().callbackPath();
    }

    /**
     * Human-readable OAuth setup reference (printed to stdout when starting Connect from Account Center).
     */
    public static String formatStripeConnectOAuthSetupText() {
        ResolvedOAuth r = resolvedOAuth();
        String cid = getConnectClientId();
        String cidLine = cid == null ? "(missing — set STRIPE_CONNECT_CLIENT_ID in .env)" : cid;
        String secretLine = hasSecretKey()
                ? "configured (value not shown)"
                : "(missing — set STRIPE_SECRET_KEY in .env)";
        String override = trimOrNull(prop("STRIPE_OAUTH_REDIRECT_URI"));
        String overrideLine = override == null
                ? "(not set — default 127.0.0.1 URI below is used)"
                : override;
        String listener = "http://" + r.listenHost() + ":" + r.listenPort() + r.callbackPath();
        return """
                SixStars Hotel — Stripe Connect (sandbox / test mode)

                1) Stripe Dashboard: turn Test mode ON → Connect → Settings → OAuth.

                2) Add this redirect URI EXACTLY (no spaces before/after):
                """ + "    " + r.redirectUriForStripe() + """

                3) Optional .env override (restart app after changing):
                    STRIPE_OAUTH_REDIRECT_URI=
                """ + "    " + overrideLine + """

                4) This app listens here while you approve in the browser (keep the app open):
                """ + "    " + listener + """

                5) STRIPE_CONNECT_CLIENT_ID (Test ca_… from the SAME Stripe account as step 1):
                """ + "    " + cidLine + """

                6) STRIPE_SECRET_KEY:
                """ + "    " + secretLine + """
                """;
    }

    /** Prints {@link #formatStripeConnectOAuthSetupText()} (console reference while OAuth runs). */
    public static void logOAuthRedirectForStripeConnect() {
        System.out.println(formatStripeConnectOAuthSetupText());
    }

    private static ResolvedOAuth resolvedOAuth() {
        ResolvedOAuth cached = cachedOAuthBinding;
        if (cached != null) {
            return cached;
        }
        synchronized (StripeConfig.class) {
            if (cachedOAuthBinding == null) {
                cachedOAuthBinding = computeResolvedOAuth();
            }
            return cachedOAuthBinding;
        }
    }

    private static ResolvedOAuth computeResolvedOAuth() {
        String override = trimOrNull(prop("STRIPE_OAUTH_REDIRECT_URI"));
        if (override != null) {
            try {
                URI u = new URI(override);
                if (!"http".equalsIgnoreCase(u.getScheme())) {
                    System.err.println("[Stripe] STRIPE_OAUTH_REDIRECT_URI must use http for local OAuth; ignoring: " + override);
                } else {
                    String host = u.getHost();
                    if (host == null) {
                        System.err.println("[Stripe] STRIPE_OAUTH_REDIRECT_URI has no host; ignoring override.");
                    } else if (!host.equalsIgnoreCase("localhost") && !host.equals("127.0.0.1")) {
                        System.err.println("[Stripe] STRIPE_OAUTH_REDIRECT_URI host must be localhost or 127.0.0.1; ignoring: " + host);
                    } else {
                        int port = u.getPort();
                        if (port <= 0) {
                            port = 80;
                        }
                        String path = u.getPath();
                        if (path == null || path.isEmpty()) {
                            path = "/";
                        }
                        while (path.length() > 1 && path.endsWith("/")) {
                            path = path.substring(0, path.length() - 1);
                        }
                        String listenHost = host.equalsIgnoreCase("localhost") ? "localhost" : "127.0.0.1";
                        URI canonical = new URI("http", null, listenHost, port, path, null, null);
                        String redirect = canonical.toASCIIString();
                        return new ResolvedOAuth(listenHost, port, path, redirect);
                    }
                }
            } catch (URISyntaxException e) {
                System.err.println("[Stripe] Invalid STRIPE_OAUTH_REDIRECT_URI: " + e.getMessage());
            }
        }
        try {
            URI canonical = new URI("http", null, "127.0.0.1", OAUTH_HTTP_PORT, OAUTH_CALLBACK_PATH, null, null);
            return new ResolvedOAuth("127.0.0.1", OAUTH_HTTP_PORT, OAUTH_CALLBACK_PATH, canonical.toASCIIString());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
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
