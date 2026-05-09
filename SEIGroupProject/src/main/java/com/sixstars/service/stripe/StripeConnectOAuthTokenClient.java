package com.sixstars.service.stripe;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Exchanges a Connect OAuth authorization code for a Stripe user (connected account id). */
public final class StripeConnectOAuthTokenClient {

    private static final Pattern STRIPE_USER_PATTERN = Pattern.compile("\"stripe_user_id\"\\s*:\\s*\"(acct_[^\"]+)\"");

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private StripeConnectOAuthTokenClient() {
    }

    public static String exchangeCodeForStripeUserId(String authorizationCode)
            throws IOException, InterruptedException, StripeOAuthException {
        String secret = StripeConfig.getSecretKey();
        if (secret == null || secret.isBlank()) {
            throw new StripeOAuthException("Missing STRIPE_SECRET_KEY");
        }

        String form = "grant_type=" + URLEncoder.encode("authorization_code", StandardCharsets.UTF_8)
                + "&code=" + URLEncoder.encode(authorizationCode.trim(), StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(secret.trim(), StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://connect.stripe.com/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> resp = HTTP.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new StripeOAuthException("OAuth token exchange failed: HTTP "
                    + resp.statusCode() + " — " + resp.body());
        }
        Matcher m = STRIPE_USER_PATTERN.matcher(resp.body());
        if (!m.find()) {
            throw new StripeOAuthException("Stripe did not return a connected account id. Response: "
                    + resp.body());
        }
        return m.group(1);
    }

    /** Unchecked-ish checked exception boundary for callers. */
    public static final class StripeOAuthException extends Exception {
        public StripeOAuthException(String msg) {
            super(msg);
        }
    }
}
