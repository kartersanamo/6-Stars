package com.sixstars.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import com.sixstars.app.Main;

public class WelcomePage extends JPanel {

    public WelcomePage(JPanel pages, CardLayout cardLayout) {
        setLayout(new GridBagLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(UITheme.CARD_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(40, 50, 40, 50)
        ));
        cardPanel.setPreferredSize(new Dimension(460, 380));

        JLabel titleLabel = new JLabel("6 Stars Hotel");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Luxury stays made simple");
        subtitleLabel.setFont(UITheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = createPrimaryButton("Login");
        loginButton.addActionListener(_ -> cardLayout.show(pages, "login"));

        JButton signUpButton = createPrimaryButton("Create Account");
        signUpButton.addActionListener(_ -> {
            Main.createAccountPage.refresh();
            cardLayout.show(pages, "create account");
        });

        JButton makeReservationButton = createSecondaryButton("Make Reservation");
        makeReservationButton.addActionListener(_ -> cardLayout.show(pages, "make reservation"));

        cardPanel.add(Box.createVerticalGlue());
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 36)));
        cardPanel.add(loginButton);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        cardPanel.add(signUpButton);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        cardPanel.add(makeReservationButton);
        cardPanel.add(Box.createVerticalGlue());

        add(cardPanel);
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(280, 46));
        button.setMaximumSize(new Dimension(280, 46));
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
        button.setPreferredSize(new Dimension(280, 46));
        button.setMaximumSize(new Dimension(280, 46));
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}