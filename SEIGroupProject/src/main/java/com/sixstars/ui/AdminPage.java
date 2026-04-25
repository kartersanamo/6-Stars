package com.sixstars.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;

public class AdminPage extends JPanel {
    public AdminPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new GridBagLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(35, 45, 35, 45)
        ));
        card.setPreferredSize(new Dimension(500, 450));

        JLabel title = new JLabel("Administrator Dashboard");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("System Management & Staff Oversight");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Action Buttons (All now using the lighter Secondary style) ---
        JButton btnCreateClerk = createThemedButton("Create Clerk Account");
        btnCreateClerk.addActionListener(_ -> {
            Main.createAccountPage.refreshInfo();
            cardLayout.show(pages, "create account");
        });

        JButton btnResetPass = createThemedButton("Reset User Password");
        btnResetPass.addActionListener(e -> {
            cardLayout.show(pages, "reset password");
        });

        JButton btnLogout = createThemedButton("Logout");
        styleLogoutButton(btnLogout);
        btnLogout.addActionListener(e -> {
            AccountController.currentAccount = null;
            Main.headerBar.refreshInfo();
            cardLayout.show(pages, "home");
        });

        card.add(Box.createVerticalGlue());
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 40)));

        card.add(btnCreateClerk);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnResetPass);
        card.add(Box.createRigidArea(new Dimension(0, 25)));
        card.add(btnLogout);
        card.add(Box.createVerticalGlue());

        add(card);
    }

    // Unified button style using the lighter color
    private JButton createThemedButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(320, 44));
        button.setMaximumSize(new Dimension(320, 44));
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setBackground(UITheme.SECONDARY_BUTTON); // Lighter color
        button.setForeground(UITheme.TEXT_DARK);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleLogoutButton(JButton button) {
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(java.awt.Color.WHITE);
    }
}