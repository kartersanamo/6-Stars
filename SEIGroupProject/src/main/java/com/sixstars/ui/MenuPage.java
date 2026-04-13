package com.sixstars.ui;

import java.awt.CardLayout;
import java.awt.Color;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class MenuPage extends JPanel {
    private JLabel welcomeLabel;
    private JLabel subtitleLabel;
    private JButton btnManageRooms;

    public MenuPage(JPanel pages, CardLayout cardLayout) {
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

        welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(UITheme.TITLE_FONT);
        welcomeLabel.setForeground(UITheme.TEXT_DARK);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        subtitleLabel = new JLabel("Choose what you'd like to do today");
        subtitleLabel.setFont(UITheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnReserve = createPrimaryButton("Make a Reservation");
        JButton btnShop = createSecondaryButton("Visit the Store");
        JButton btnAccount = createSecondaryButton("My Account");
        btnManageRooms = createSecondaryButton("Room Management");
        JButton btnLogout = createSecondaryButton("Logout");

        btnManageRooms.setVisible(false);

        btnReserve.addActionListener(e -> cardLayout.show(pages, "make reservation"));
        btnShop.addActionListener(e -> JOptionPane.showMessageDialog(this, "Store coming soon"));
        btnAccount.addActionListener(e -> {
            Main.accountDetailsPage.refreshInfo();
            cardLayout.show(pages, "account details");
        });
        btnManageRooms.addActionListener(e -> cardLayout.show(pages, "room management"));
        btnLogout.addActionListener(e -> {
            AccountController.currentAccount = null;
            cardLayout.show(pages, "welcome");
        });

        card.add(Box.createVerticalGlue());
        card.add(welcomeLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(subtitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 35)));
        card.add(btnReserve);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnShop);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnAccount);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(btnManageRooms);
        card.add(Box.createRigidArea(new Dimension(0, 18)));
        card.add(btnLogout);
        card.add(Box.createVerticalGlue());

        add(card);
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(320, 46));
        button.setMaximumSize(new Dimension(320, 46));
        button.setFont(UITheme.BUTTON_FONT);
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
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

    public void updateWelcomeMessage() {
        Account current = AccountController.currentAccount;

        if (current != null) {
            welcomeLabel.setText("Welcome, " + current.getFirstName() + "!");

            if (current.getRole() == Role.CLERK) {
                btnManageRooms.setVisible(true);
            } else {
                btnManageRooms.setVisible(false);
            }
        } else {
            welcomeLabel.setText("Welcome!");
            btnManageRooms.setVisible(false);
        }

        revalidate();
        repaint();
    }
}