package com.sixstars.ui.components;

/** HTML snippets for password checklist rows in Swing labels. */
public final class PasswordStrengthUiFormatter {

    private PasswordStrengthUiFormatter() {
    }

    public static String escapeHtmlLite(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    public static String formatRuleLine(boolean ok, String text) {
        String color = ok ? "#2d7a4a" : "#a0806a";
        String mark = ok ? "✓" : "○";
        return String.format("<html><span style='color:%s;font-weight:bold;'>%s</span>&nbsp;&nbsp;<span style='color:#3a3a3a;'>%s</span></html>",
                color, mark, escapeHtmlLite(text));
    }

    public static String formatRuleLineNeutral(String text) {
        return String.format("<html><span style='color:#b0a090;'>○</span>&nbsp;&nbsp;<span style='color:#888888;'>%s</span></html>",
                escapeHtmlLite(text));
    }
}
