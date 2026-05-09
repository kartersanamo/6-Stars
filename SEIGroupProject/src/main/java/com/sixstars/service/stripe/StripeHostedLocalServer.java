package com.sixstars.service.stripe;

import java.awt.Desktop;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Local redirect receivers for Stripe sandbox flows ({@code 127.0.0.1} ports in {@link StripeConfig}).
 * Register the same URIs under Stripe Dashboard (Checkout success/cancel, Connect redirects).
 */
public final class StripeHostedLocalServer {

    private StripeHostedLocalServer() {
    }

    /** Begin listening for Stripe Checkout redirects; invoke {@link Runnable#run()} when finished to shut down quickly. */
    public static BindHandle bindCheckout(Consumer<String> onSessionReturned, Runnable onCancelled) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", StripeConfig.CHECKOUT_HTTP_PORT), 0);
        ExecutorService pool = Executors.newCachedThreadPool();
        AtomicBoolean stopped = new AtomicBoolean();

        Runnable shutdown = () -> {
            if (stopped.compareAndSet(false, true)) {
                server.stop(0);
                pool.shutdownNow();
            }
        };

        server.createContext(StripeConfig.CHECKOUT_SUCCESS_PATH, exchange -> handleCheckoutSuccess(pool, shutdown, exchange, onSessionReturned, onCancelled));
        server.createContext(StripeConfig.CHECKOUT_CANCEL_PATH, exchange -> {
            respondHtml(exchange, true,
                    "Checkout canceled",
                    "No charge was finalized. You can switch back to 6 Stars Hotel.");
            deferShutdown(pool, shutdown);
            onCancelled.run();
        });

        server.setExecutor(pool);
        server.start();

        return new BindHandle(server, shutdown);
    }

    private static void handleCheckoutSuccess(ExecutorService pool, Runnable shutdown,
            HttpExchange exchange, Consumer<String> onSessionReturned, Runnable onCancelled)
            throws IOException {
        Map<String, String> q = parseQuery(exchange.getRequestURI().getRawQuery());
        String sessionId = q.get("session_id");
        if (sessionId == null || sessionId.isBlank()) {
            respondHtml(exchange, false,
                    "Missing session reference",
                    "Stripe did not return a checkout session identifier.");
            deferShutdown(pool, shutdown);
            onCancelled.run();
            return;
        }
        respondHtml(exchange, true,
                "Stripe complete",
                "You can switch back to 6 Stars Hotel.");
        deferShutdown(pool, shutdown);
        onSessionReturned.accept(sessionId);
    }

    private static void deferShutdown(ExecutorService pool, Runnable shutdown) {
        pool.submit(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            shutdown.run();
        });
    }

    /** OAuth listener; exposes the browser URL ({@link OAuthListening#authorizeUrl()}). */
    public static OAuthListening bindOAuth(Consumer<String> onAuthorizationCodeOrNull) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress(StripeConfig.oauthListenHost(), StripeConfig.oauthListenPort()), 0);
        ExecutorService pool = Executors.newCachedThreadPool();
        AtomicBoolean stopped = new AtomicBoolean();

        Runnable shutdown = () -> {
            if (stopped.compareAndSet(false, true)) {
                server.stop(0);
                pool.shutdownNow();
            }
        };

        final String oauthState = UUID.randomUUID().toString();
        String authorizeUri = buildAuthorizeUri(oauthState);

        server.createContext(StripeConfig.oauthCallbackPath(), exchange -> {
            Map<String, String> q = parseQuery(exchange.getRequestURI().getRawQuery());
            String returned = q.get("state");
            String code = q.get("code");

            boolean stateMatches = oauthState.equals(returned);

            try {
                if (!stateMatches) {
                    respondHtml(exchange, false, "OAuth state mismatch",
                            "Try connecting again from the hotel app.");
                    deferShutdown(pool, shutdown);
                    onAuthorizationCodeOrNull.accept(null);
                    return;
                }
                if (code == null || code.isBlank()) {
                    respondHtml(exchange, false, "Stripe Connect canceled",
                            q.getOrDefault("error_description", "No authorization code."));
                    deferShutdown(pool, shutdown);
                    onAuthorizationCodeOrNull.accept(null);
                    return;
                }

                respondHtml(exchange, true,
                        "Stripe account linked (sandbox)",
                        "Return to 6 Stars Hotel to finish configuring payments.");
                deferShutdown(pool, shutdown);
                onAuthorizationCodeOrNull.accept(code);
            } catch (Exception exCaught) {
                respondHtml(exchange, false,
                        "Connect error",
                        exCaught.getMessage() != null ? exCaught.getMessage() : "");
                deferShutdown(pool, shutdown);
                onAuthorizationCodeOrNull.accept(null);
            }
        });
        server.setExecutor(pool);
        server.start();
        return new OAuthListening(authorizeUri, shutdown);
    }

    public record OAuthListening(String authorizeUrl, Runnable shutdown) {
        public void stopQuietly() {
            shutdown.run();
        }
    }

    public record BindHandle(HttpServer server, Runnable shutdown) {
        public void stopQuietly() {
            shutdown.run();
        }
    }

    private static String buildAuthorizeUri(String state) {
        String rawCid = StripeConfig.getConnectClientId();
        String cid = rawCid == null ? "" : rawCid.trim();
        String redirect = StripeConfig.oauthRedirectUri();
        return "https://connect.stripe.com/oauth/authorize?"
                + "response_type=code"
                + "&client_id=" + urlEncode(cid)
                + "&scope=" + urlEncode("read_write")
                + "&redirect_uri=" + urlEncode(redirect)
                + "&state=" + urlEncode(state);
    }

    private static Map<String, String> parseQuery(String raw) {
        Map<String, String> map = new HashMap<>();
        if (raw == null || raw.isBlank()) {
            return map;
        }
        for (String chunk : raw.split("&")) {
            int eq = chunk.indexOf('=');
            if (eq <= 0) {
                continue;
            }
            String k = decode(chunk.substring(0, eq));
            String v = decode(chunk.substring(eq + 1));
            map.put(k, v);
        }
        return map;
    }

    private static String decode(String s) {
        return java.net.URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static String urlEncode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static void respondHtml(HttpExchange exchange, boolean ok,
            String title, String paragraph) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        String body = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>"
                + escape(title) + "</title></head><body style=\"font-family:system-ui,sans-serif;padding:28px;background:#fcf9ef;\"><h2>"
                + escape(title) + "</h2><p>" + escape(paragraph)
                + "</p></body></html>";
        byte[] raw = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(ok ? 200 : 400, raw.length);
        exchange.getResponseBody().write(raw);
        exchange.close();
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public static boolean browse(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(java.net.URI.create(url));
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
