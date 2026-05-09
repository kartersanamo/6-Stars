package com.sixstars.service.security;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.prefs.Preferences;

import com.sixstars.model.Account;
import com.sixstars.model.NotificationType;
import com.sixstars.service.AccountService;
import com.sixstars.service.NotificationService;
import com.sixstars.ui.AccountCenterPage;
import com.sixstars.ui.accountcenter.SecurityPreferenceKeys;

/**
 * Persists sign-in history on this machine and triggers optional security alerts.
 */
public final class SignInAuditService {

    private static final Preferences ROOT = Preferences.userNodeForPackage(SignInAuditService.class);
    private static final int MAX_HISTORY_LINES = 30;
    private static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a")
            .withZone(ZoneId.systemDefault());

    private SignInAuditService() {
    }

    public record SignInEntry(Instant when, String detailLine) {
    }

    public static String getOrCreateInstallDeviceId() {
        Preferences n = ROOT.node("install");
        String id = n.get("device_id", "");
        if (id.isBlank()) {
            id = UUID.randomUUID().toString();
            n.put("device_id", id);
        }
        return id;
    }

    private static Preferences userPrefs(String normalizedEmail) {
        return ROOT.node("sessions").node(sanitizeEmailForNode(normalizedEmail));
    }

    private static String sanitizeEmailForNode(String email) {
        if (email == null) {
            return "unknown";
        }
        return email.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9._@-]", "_");
    }

    private static Preferences accountCenterRoot() {
        return Preferences.userNodeForPackage(AccountCenterPage.class);
    }

    /**
     * Call after a successful interactive sign-in (same JVM). Records history, optional new-device email,
     * and optional in-app sign-in alert.
     */
    public static void recordSuccessfulSignIn(Account account, AccountService accountService) {
        if (account == null || account.getEmail() == null) {
            return;
        }
        String email = account.getEmail().trim().toLowerCase(Locale.ROOT);
        String deviceId = getOrCreateInstallDeviceId();
        String osLine = System.getProperty("os.name", "Unknown OS")
                + " · Java " + System.getProperty("java.version", "?");
        Instant now = Instant.now();
        String detail = "6 Stars desktop app on " + osLine;
        appendHistory(email, now, detail);

        Preferences acctSecurity = SecurityPreferenceKeys.nodeForAccount(accountCenterRoot(), account);
        Preferences sessionPrefs = userPrefs(email);
        String previousDevice = sessionPrefs.get("last_device_id", "");
        boolean hadPrevious = !previousDevice.isBlank();
        boolean deviceChanged = hadPrevious && !previousDevice.equals(deviceId);

        if (deviceChanged && SecurityPreferenceKeys.isNewDeviceEmailEnabled(accountCenterRoot(), account)) {
            accountService.sendSecurityNoticeEmailIfConfigured(
                    email,
                    "New sign-in to your 6 Stars Hotel account",
                    "<p>We detected a sign-in to your account from a <b>different device profile</b> than the last one "
                            + "we saw on this computer (for example after reinstalling the app or restoring settings).</p>"
                            + "<p><b>When:</b> " + DISPLAY.format(now) + "<br/>"
                            + "<b>Where:</b> " + escapeHtml(osLine) + "</p>"
                            + "<p>If this was you, you can ignore this message.</p>");
        }

        sessionPrefs.put("last_device_id", deviceId);

        if (SecurityPreferenceKeys.isLoginInAppAlertEnabled(accountCenterRoot(), account)) {
            NotificationService ns = NotificationService.getInstance();
            if (ns.isInAppEnabled(email, NotificationType.ACCOUNT_ACTIVITY)) {
                ns.publish(NotificationType.ACCOUNT_ACTIVITY, email,
                        "Signed in — " + osLine + " (" + DISPLAY.format(now) + ")");
            }
        }
    }

    public static List<SignInEntry> readRecentHistory(Account account, int maxLines) {
        List<SignInEntry> out = new ArrayList<>();
        if (account == null || account.getEmail() == null) {
            return out;
        }
        String raw = userPrefs(account.getEmail().trim().toLowerCase(Locale.ROOT)).get("signin_history", "");
        if (raw.isBlank()) {
            return out;
        }
        String[] lines = raw.split("\n");
        int limit = Math.min(maxLines, lines.length);
        for (int i = 0; i < limit; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            int tab = line.indexOf('\t');
            if (tab <= 0) {
                continue;
            }
            try {
                long epoch = Long.parseLong(line.substring(0, tab));
                String rest = line.substring(tab + 1).trim();
                out.add(new SignInEntry(Instant.ofEpochMilli(epoch), rest));
            } catch (NumberFormatException ignored) {
                // skip corrupt line
            }
        }
        return out;
    }

    public static String currentSessionSummary(Account account) {
        if (account == null) {
            return "";
        }
        List<SignInEntry> entries = readRecentHistory(account, 1);
        if (entries.isEmpty()) {
            return "This session — sign-in history will appear here after your next successful login.";
        }
        SignInEntry e = entries.get(0);
        return "Last sign-in: " + DISPLAY.format(e.when()) + " — " + e.detailLine();
    }

    private static void appendHistory(String normalizedEmail, Instant when, String detail) {
        Preferences p = userPrefs(normalizedEmail);
        String line = when.toEpochMilli() + "\t" + detail;
        String existing = p.get("signin_history", "");
        List<String> rows = new ArrayList<>();
        rows.add(line);
        if (!existing.isBlank()) {
            for (String s : existing.split("\n")) {
                if (!s.isBlank() && rows.size() < MAX_HISTORY_LINES) {
                    rows.add(s.trim());
                }
            }
        }
        p.put("signin_history", String.join("\n", rows));
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
