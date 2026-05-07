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
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.sixstars.app.Main;
import com.sixstars.service.AccountService;

public class PasswordResetPage extends JPanel {
    private final JPanel pages;
    private final CardLayout cardLayout;
    private final AccountService accountService;

    private final JTextField emailField = new JTextField();
    private final JTextField codeField = new JTextField();
    private final JPasswordField newPasswordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();

    private final JPanel notificationPanel;
    private final JLabel notificationMessageLabel;

    public PasswordResetPage(JPanel pages, CardLayout cardLayout, AccountService accountService) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.accountService = accountService;

        setLayout(new GridBagLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(32, 44, 32, 44)
        ));
        card.setPreferredSize(new Dimension(460, 620));

        JLabel title = new JLabel("Reset Password");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Use the access code sent to your email to create a new password.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        notificationPanel = createNotificationPanel();
        notificationPanel.setVisible(false);
        notificationMessageLabel = (JLabel) notificationPanel.getClientProperty("notificationMessageLabel");

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BACKGROUND);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel emailLabel = createLabel("Email Address");
        JLabel codeLabel = createLabel("Access Code");
        JLabel newPasswordLabel = createLabel("New Password");
        JLabel confirmPasswordLabel = createLabel("Confirm New Password");

        styleTextField(emailField);
        styleTextField(codeField);
        stylePasswordField(newPasswordField);
        stylePasswordField(confirmPasswordField);

        JButton sendCodeButton = new JButton("Send Access Code");
        stylePrimaryButton(sendCodeButton);
        sendCodeButton.addActionListener(_ -> sendAccessCode());

        JButton resetButton = new JButton("Reset Password");
        stylePrimaryButton(resetButton);
        resetButton.addActionListener(_ -> resetPassword());

        JButton backButton = new JButton("Back to Login");
        styleSecondaryButton(backButton);
        backButton.addActionListener(_ -> {
            clearResetFields();
            notificationPanel.setVisible(false);
            Main.headerBar.refreshInfo();
            cardLayout.show(pages, "login");
        });

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(emailLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(emailField, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(sendCodeButton, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(14, 0, 12, 0);
        formPanel.add(codeLabel, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(codeField, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(newPasswordLabel, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(newPasswordField, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(0, 0, 20, 0);
        formPanel.add(confirmPasswordField, gbc);

        gbc.gridy = 9;
        gbc.insets = new Insets(0, 0, 12, 0);
        formPanel.add(resetButton, gbc);

        gbc.gridy = 10;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(backButton, gbc);

        card.add(Box.createVerticalGlue());
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(notificationPanel);
        card.add(Box.createRigidArea(new Dimension(0, 16)));
        card.add(formPanel);
        card.add(Box.createVerticalGlue());

        add(card);
    }

    public void openForEmail(String email) {
        emailField.setText(email == null ? "" : email.trim().toLowerCase());
        codeField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
        showNotification("Enter the access code sent to your email, then choose a new password.", true);
        emailField.requestFocusInWindow();
    }

    private void sendAccessCode() {
        try {
            String email = emailField.getText().trim();
            accountService.sendPasswordResetCode(email);
            showNotification("Access code sent to " + email + ". Check your inbox.", true);
            codeField.requestFocusInWindow();
        } catch (RuntimeException ex) {
            showNotification(ex.getMessage(), false);
        }
    }

    private void resetPassword() {
        try {
            accountService.resetPasswordWithCode(
                    emailField.getText().trim(),
                    codeField.getText().trim(),
                    new String(newPasswordField.getPassword()),
                    new String(confirmPasswordField.getPassword())
            );
            showNotification("Password reset successfully. Returning to login...", true);
            clearResetFieldsExceptEmail();
            Timer timer = new Timer(1300, ignored -> cardLayout.show(pages, "login"));
            timer.setRepeats(false);
            timer.start();
        } catch (RuntimeException ex) {
            showNotification(ex.getMessage(), false);
        }
    }

    private void clearResetFields() {
        emailField.setText("");
        clearResetFieldsExceptEmail();
    }

    private void clearResetFieldsExceptEmail() {
        codeField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void showNotification(String message, boolean success) {
        notificationMessageLabel.setText(message);
        notificationPanel.setBackground(success ? new Color(241, 248, 236) : new Color(255, 241, 241));
        notificationPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(success ? new Color(120, 170, 90) : new Color(215, 90, 90), 1),
                new EmptyBorder(10, 14, 10, 14)
        ));
        notificationMessageLabel.setForeground(success ? new Color(56, 110, 52) : new Color(180, 40, 40));
        notificationPanel.setVisible(true);
        revalidate();
        repaint();
    }

    private JPanel createNotificationPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(new Color(255, 241, 241));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 90, 90), 1),
                new EmptyBorder(10, 14, 10, 14)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        JLabel icon = new JLabel("⚠");
        icon.setFont(new Font("SansSerif", Font.BOLD, 18));
        icon.setForeground(new Color(180, 40, 40));

        JLabel message = new JLabel("Notification message");
        message.setFont(new Font("SansSerif", Font.PLAIN, 13));
        message.setForeground(new Color(180, 40, 40));
        panel.putClientProperty("notificationMessageLabel", message);

        JButton dismiss = new JButton("✕");
        dismiss.setFont(new Font("SansSerif", Font.BOLD, 16));
        dismiss.setForeground(new Color(180, 40, 40));
        dismiss.setFocusPainted(false);
        dismiss.setBorderPainted(false);
        dismiss.setContentAreaFilled(false);
        dismiss.setOpaque(false);
        dismiss.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dismiss.addActionListener(_ -> panel.setVisible(false));

        panel.add(icon, BorderLayout.WEST);
        panel.add(message, BorderLayout.CENTER);
        panel.add(dismiss, BorderLayout.EAST);
        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.TEXT_MEDIUM);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
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

    private void stylePasswordField(JPasswordField field) {
        styleTextField(field);
    }

    private void stylePrimaryButton(JButton button) {
        button.setPreferredSize(new Dimension(320, 44));
        button.setMaximumSize(new Dimension(320, 44));
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



