package com.sixstars.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import com.sixstars.app.Main;
import com.sixstars.controller.LoginController;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class LoginPage extends JPanel {

    public LoginPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new GridBagLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(35, 45, 35, 45)
        ));
        card.setPreferredSize(new Dimension(440, 560));

        JLabel hotelLabel = new JLabel("6 Stars Hotel");
        hotelLabel.setFont(UITheme.TITLE_FONT);
        hotelLabel.setForeground(UITheme.TEXT_DARK);
        hotelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("User Login");
        subtitleLabel.setFont(UITheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BACKGROUND);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(UITheme.LABEL_FONT);
        emailLabel.setForeground(UITheme.TEXT_MEDIUM);
        emailLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField emailField = new JTextField();
        styleTextField(emailField);

        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(UITheme.LABEL_FONT);
        passwordLabel.setForeground(UITheme.TEXT_MEDIUM);
        passwordLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPasswordField passwordField = new JPasswordField();
        styleTextField(passwordField);

        JButton loginButton = new JButton("Log In");
        stylePrimaryButton(loginButton);

        JButton backButton = new JButton("Back");
        styleSecondaryButton(backButton);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 22, 0);
        formPanel.add(emailField, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 28, 0);
        formPanel.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(loginButton, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(backButton, gbc);

        loginButton.addActionListener(_ -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            Account a = LoginController.checkLogin(email, password);
            if (a != null) {
                AccountController.currentAccount = a;

                if (a.getRole() == Role.ADMIN) {
                    JOptionPane.showMessageDialog(this, "Login successful! (Admin)");
                    cardLayout.show(pages, "admin");
                } else {
                    Main.menuPage.updateWelcomeMessage();
                    cardLayout.show(pages, "menu page");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.");
            }

            emailField.setText("");
            passwordField.setText("");
        });

        backButton.addActionListener(_ -> cardLayout.show(pages, "welcome"));

        card.add(Box.createVerticalGlue());
        card.add(hotelLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(subtitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 38)));
        card.add(formPanel);
        card.add(Box.createVerticalGlue());

        add(card);
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(320, 42));
        field.setMaximumSize(new Dimension(320, 42));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(UITheme.TEXT_DARK);
        field.setCaretColor(UITheme.TEXT_DARK);
        field.setHorizontalAlignment(SwingConstants.LEFT);
    }

    private void stylePrimaryButton(JButton button) {
        button.setPreferredSize(new Dimension(320, 46));
        button.setMaximumSize(new Dimension(320, 46));
        button.setFont(UITheme.BUTTON_FONT);
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void styleSecondaryButton(JButton button) {
        button.setPreferredSize(new Dimension(320, 44));
        button.setMaximumSize(new Dimension(320, 44));
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
}