package com.sixstars.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class ClerkPage extends JPanel {
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JButton btnManageRooms;

    public ClerkPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new GridBagLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(35, 45, 35, 45)
        ));
        card.setPreferredSize(new Dimension(500, 520));

        titleLabel = new JLabel("Clerk Dashboard");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtitleLabel = new JLabel("Internal Management System");
        subtitleLabel.setFont(UITheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- THE FOUR BUTTONS (All now using the same theme) ---
        JButton btnReserve = createThemedButton("Make Guest Reservation");
        btnManageRooms = createThemedButton("Room Management");
        JButton btnManageReservations = createThemedButton("Manage Reservations");
        JButton btnAccount = createThemedButton("My Account");
        JButton btnLogout = createThemedButton("Logout");
        JButton btnCheckIn = createThemedButton("Guest Check-In");
        JButton btnBillingSearch = createThemedButton("Guest Billing Search");
        styleLogoutButton(btnLogout);

        // --- Listeners ---
        btnReserve.addActionListener(e -> {
            Main.makeReservationPage.refreshPage();
            cardLayout.show(pages, "make reservation");
        });

        btnAccount.addActionListener(e -> {
            Main.accountDetailsPage.refreshInfo();
            cardLayout.show(pages, "account details");
        });

        btnManageRooms.addActionListener(e -> cardLayout.show(pages, "room management"));
        btnManageReservations.addActionListener(e -> {
            Main.reservationsPage.refresh();
            cardLayout.show(pages, "reservations");
        });

        btnLogout.addActionListener(e -> {
            AccountController.currentAccount = null;
            Main.headerBar.refreshInfo();
            cardLayout.show(pages, "home");
        });

        btnCheckIn.addActionListener(e-> cardLayout.show(pages, "check in"));

        btnBillingSearch.addActionListener(e -> cardLayout.show(pages, "clerk billing"));

        // --- Build UI ---
        card.add(Box.createVerticalGlue());
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(subtitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 35)));

        card.add(btnReserve);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnManageReservations);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnManageRooms);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnAccount);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnCheckIn);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnBillingSearch);
        card.add(Box.createRigidArea(new Dimension(0, 25)));
        card.add(btnLogout);
        card.add(Box.createVerticalGlue());

        add(card);
    }

    // Unified helper method for all buttons
    private JButton createThemedButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(320, 44));
        button.setMaximumSize(new Dimension(320, 44));
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleLogoutButton(JButton button) {
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(Color.WHITE);
    }
}