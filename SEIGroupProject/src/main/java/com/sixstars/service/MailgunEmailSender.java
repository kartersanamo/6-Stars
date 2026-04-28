package com.sixstars.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class MailgunEmailSender {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String apiKey;
    private final String domain;
    private final String fromEmail;

    public MailgunEmailSender(String apiKey, String domain, String fromEmail) {
        // Try System.getenv() first (for shell-set variables), then fall back to System.getProperty() (for .env-loaded variables)
        this.apiKey = Objects.requireNonNull(
            apiKey != null ? apiKey : getProperty("MAILGUN_API_KEY"),
            "apiKey"
        );
        this.domain = Objects.requireNonNull(
            domain != null ? domain : getProperty("MAILGUN_DOMAIN"),
            "domain"
        );
        this.fromEmail = Objects.requireNonNull(
            fromEmail != null ? fromEmail : getProperty("MAILGUN_FROM_EMAIL"),
            "fromEmail"
        );
    }

    // Helper to try System.getenv() first, then System.getProperty()
    private static String getProperty(String key) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return value;
    }

    public void sendVerificationCode(String toEmail, String code) throws IOException, InterruptedException {
        String form = formEncode(
                "from", fromEmail,
                "to", toEmail,
                "subject", "Verify your 6 Stars Hotel account",
                "html", buildVerificationHtml(code)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mailgun.net/v3/" + domain + "/messages"))
                .header("Authorization", basicAuthHeader())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Mailgun send failed: HTTP " + response.statusCode() + " - " + response.body());
        }
    }

    private String buildVerificationHtml(String code) {
        return "<p>Thanks for creating a 6 Stars Hotel account.</p>"
                + "<p>Your verification code is <b>" + escapeHtml(code) + "</b>.</p>"
                + "<p>This code expires in 15 minutes.</p>"
                + "<p>If you did not request this, you can ignore this email.</p>";
    }

    private String basicAuthHeader() {
        String token = "api:" + apiKey;
        String encoded = java.util.Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private String formEncode(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new IllegalArgumentException("keyValuePairs must contain an even number of entries");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            if (!sb.isEmpty()) {
                sb.append('&');
            }
            sb.append(URLEncoder.encode(keyValuePairs[i], StandardCharsets.UTF_8));
            sb.append('=');
            sb.append(URLEncoder.encode(keyValuePairs[i + 1], StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

