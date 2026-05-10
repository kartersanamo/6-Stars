package com.sixstars.ui.billing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import com.sixstars.ui.UITheme;

/**
 * Shared Swing styling and small layout helpers for {@link com.sixstars.ui.BillingPage}.
 */
public final class BillingUiUtils {

    private BillingUiUtils() {
    }

    public static JPanel softInsetPanel(Color bg) {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 230), 1, true),
                new EmptyBorder(14, 16, 16, 16)));
        return p;
    }

    public static void styleSecondaryButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setPreferredSize(new Dimension(160, 40));
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(190, 184, 172), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public static void stylePrimaryGoldButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setPreferredSize(new Dimension(220, 44));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(88, 68, 36), 1, true),
                new EmptyBorder(10, 16, 10, 16)));
        button.setBorderPainted(true);
        applyPrimaryGoldButtonColors(button, button.isEnabled());
        button.addPropertyChangeListener("enabled", e ->
                applyPrimaryGoldButtonColors(button, (Boolean) e.getNewValue()));
    }

    public static void applyPrimaryGoldButtonColors(JButton button, boolean enabled) {
        if (enabled) {
            button.setBackground(UITheme.ACCENT_GOLD);
            button.setForeground(Color.BLACK);
        } else {
            button.setBackground(new Color(218, 212, 200));
            button.setForeground(new Color(105, 98, 88));
        }
    }

    /** Prominent link-style control under billing totals (navigates to Account Center Payment). */
    public static void styleTotalsPaymentLinkButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(new Color(44, 108, 72));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(28, 78, 52), 1, true),
                new EmptyBorder(10, 18, 10, 18)));
        button.setBorderPainted(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public static String esc(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
