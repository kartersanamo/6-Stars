package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;
import com.sixstars.service.AccountService;

public class LoginPage extends JPanel {

    public LoginPage(JPanel pages, CardLayout cardLayout, AccountService accountService) {
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

        // Create inline error notification panel
        JPanel errorNotificationPanel = createErrorNotificationPanel();
        errorNotificationPanel.setVisible(false);

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

        JLabel createPromptLabel = new JLabel("Don't have an account yet?");
        createPromptLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        createPromptLabel.setForeground(UITheme.TEXT_MEDIUM);
        createPromptLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton createAccountButton = new JButton("Create Account");
        styleLinkButton(createAccountButton);

        JButton backButton = new JButton("Back");
        styleSecondaryButton(backButton);

        // Add error notification at top of form
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(errorNotificationPanel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 22, 0);
        formPanel.add(emailField, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 28, 0);
        formPanel.add(passwordField, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(loginButton, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 6, 0);
        formPanel.add(createPromptLabel, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(createAccountButton, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(backButton, gbc);

        this.addHierarchyListener(e -> {
            emailField.requestFocusInWindow();
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                javax.swing.JRootPane root = javax.swing.SwingUtilities.getRootPane(this);
                if (root != null) {
                    root.setDefaultButton(loginButton);
                }
            }
        });

        // Store error message label for later updates
        JLabel errorMessageLabel = (JLabel) errorNotificationPanel.getClientProperty("errorMessageLabel");

        loginButton.addActionListener(_ -> {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());

            Account a = accountService.authenticate(email, password);
            if (a != null) {
                AccountController.currentAccount = a;
                errorNotificationPanel.setVisible(false);
                Main.headerBar.refreshInfo();

                if (a.getRole() == Role.ADMIN) {
                    cardLayout.show(pages, "admin page");
                } else if (a.getRole() == Role.CLERK){
                    cardLayout.show(pages, "clerk page");
                } else {
                    cardLayout.show(pages, "home");
                }
            } else {
                Account existing = accountService.getAccountByEmail(email);
                if (existing != null && !existing.isEmailVerified() && existing.getPasswordHash().equals(accountService.hashPassword(password))) {
                    try {
                        accountService.sendVerificationCode(existing.getEmail());
                    } catch (RuntimeException verificationEx) {
                        errorMessageLabel.setText("We couldn't send a fresh code yet: " + verificationEx.getMessage());
                        errorNotificationPanel.setVisible(true);
                        formPanel.revalidate();
                        formPanel.repaint();
                    }

                    EmailVerificationDialog dialog = new EmailVerificationDialog(
                            SwingUtilities.getWindowAncestor(this),
                            new AccountController(),
                            existing.getEmail()
                    );
                    dialog.setVisible(true);

                    if (dialog.wasVerified()) {
                        Account verified = accountService.getAccountByEmail(existing.getEmail());
                        if (verified != null) {
                            AccountController.currentAccount = verified;
                            errorNotificationPanel.setVisible(false);
                            Main.headerBar.refreshInfo();

                            if (verified.getRole() == Role.ADMIN) {
                                cardLayout.show(pages, "admin page");
                            } else if (verified.getRole() == Role.CLERK) {
                                cardLayout.show(pages, "clerk page");
                            } else {
                                cardLayout.show(pages, "home");
                            }
                        }
                    } else {
                        errorMessageLabel.setText("Please verify your email to continue.");
                        errorNotificationPanel.setVisible(true);
                        formPanel.revalidate();
                        formPanel.repaint();
                    }
                } else {
                    errorMessageLabel.setText("Invalid Credentials. Try again.");
                    errorNotificationPanel.setVisible(true);
                    formPanel.revalidate();
                    formPanel.repaint();
                }
            }

            emailField.setText("");
            passwordField.setText("");
        });

        backButton.addActionListener(_ -> {
            Main.headerBar.refreshInfo();          
            cardLayout.show(pages, "home");
        });

        createAccountButton.addActionListener(_ -> {
            emailField.setText("");
            passwordField.setText("");
            Main.createAccountPage.refreshInfo();
            cardLayout.show(pages, "create account");
        });

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

    private void styleLinkButton(JButton button) {
        button.setPreferredSize(new Dimension(320, 34));
        button.setMaximumSize(new Dimension(320, 34));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setForeground(new Color(140, 104, 47));
        button.setBackground(UITheme.CARD_BACKGROUND);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private JPanel createErrorNotificationPanel() {
        JPanel notificationPanel = new JPanel();
        notificationPanel.setLayout(new BorderLayout(8, 0));
        notificationPanel.setBackground(new Color(255, 240, 245)); // Light red/pink background
        notificationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 53, 69), 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        notificationPanel.setMaximumSize(new Dimension(320, 50));
        notificationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Error icon/bullet
        JLabel iconLabel = new JLabel("⚠");
        iconLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        iconLabel.setForeground(new Color(220, 53, 69));

        // Error message
        JLabel errorMessageLabel = new JLabel("Invalid email or password.");
        errorMessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        errorMessageLabel.setForeground(new Color(220, 53, 69));
        notificationPanel.putClientProperty("errorMessageLabel", errorMessageLabel);

        // Dismiss button (X)
        JButton dismissButton = new JButton("✕");
        dismissButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        dismissButton.setForeground(new Color(220, 53, 69));
        dismissButton.setBackground(new Color(255, 240, 245));
        dismissButton.setFocusPainted(false);
        dismissButton.setBorderPainted(false);
        dismissButton.setOpaque(false);
        dismissButton.setContentAreaFilled(false);
        dismissButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dismissButton.setPreferredSize(new Dimension(24, 24));
        dismissButton.addActionListener(_ -> notificationPanel.setVisible(false));

        notificationPanel.add(iconLabel, BorderLayout.WEST);
        notificationPanel.add(errorMessageLabel, BorderLayout.CENTER);
        notificationPanel.add(dismissButton, BorderLayout.EAST);

        return notificationPanel;
    }
}