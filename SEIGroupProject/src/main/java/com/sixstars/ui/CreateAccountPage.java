package com.sixstars.ui;

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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class CreateAccountPage extends JPanel {
    private final AccountController accountController;
    private JPanel formPanel;
    private JLabel roleLabel;
    private JComboBox<Role> roleComboBox;
    private boolean isAdmin;
    JPasswordField passwordField;

    public CreateAccountPage(JPanel pages, CardLayout cardLayout) {
        accountController = new AccountController();

        setLayout(new GridBagLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(35, 45, 35, 45)
        ));
        card.setPreferredSize(new Dimension(460, 800));

        JLabel titleLabel = new JLabel("Create Account");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Register a new hotel guest or staff account");
        subtitleLabel.setFont(UITheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BACKGROUND);
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel firstNameLabel = createCenteredLabel("First Name");
        JTextField firstNameField = new JTextField();
        styleTextField(firstNameField);

        JLabel lastNameLabel = createCenteredLabel("Last Name");
        JTextField lastNameField = new JTextField();
        styleTextField(lastNameField);

        JLabel emailLabel = createCenteredLabel("Email Address");
        JTextField emailField = new JTextField();
        styleTextField(emailField);

        JLabel passwordLabel = createCenteredLabel("Password");
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setEchoChar('*');

        JCheckBox showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(UITheme.CARD_BACKGROUND);

        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar((char) 0); // show text
            } else {
                passwordField.setEchoChar('*'); // hide text again
            }
        });

        roleLabel = createCenteredLabel("Role");
        Role[] roles = {Role.GUEST, Role.CLERK, Role.ADMIN};
        roleComboBox = new JComboBox<>(roles);
        styleComboBox(roleComboBox);

        JButton createButton = new JButton("Create Account");
        stylePrimaryButton(createButton);

        JButton backButton = new JButton("Back");
        styleSecondaryButton(backButton);

        int row = 0;

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(firstNameLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(firstNameField, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(lastNameLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(lastNameField, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(emailLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(emailField, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(passwordField, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(showPassword, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(roleLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 24, 0);
        formPanel.add(roleComboBox, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(createButton, gbc);

        gbc.gridy = row;
        gbc.insets = new Insets(0, 0, 0, 0);
        formPanel.add(backButton, gbc);

        card.add(Box.createVerticalGlue());
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(subtitleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 30)));
        card.add(formPanel);
        card.add(Box.createVerticalGlue());

        add(card);

        createButton.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            Role roleSet;

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please fill in all fields.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                if (!isAdmin) {
                    roleSet = Role.GUEST;
                } else {
                    roleSet = (Role) roleComboBox.getSelectedItem();
                }

                Account createdAccount = accountController.createAccount(firstName, lastName, email, password, roleSet);

                JOptionPane.showMessageDialog(
                        this,
                        roleSet + " account created for " + firstName + " " + lastName,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                firstNameField.setText("");
                lastNameField.setText("");
                emailField.setText("");
                passwordField.setText("");
                

                if (!isAdmin) {
                    AccountController.currentAccount = createdAccount;
                    Main.headerBar.refreshInfo();

                    if (Main.makeReservationPage.completePendingReservationIfAny()) {
                        return;
                    }
                }

                cardLayout.show(pages, "home");

            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Account Creation Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        backButton.addActionListener(e -> {
            if (isAdmin) { // This variable is set in refreshInfo()
                cardLayout.show(pages, "admin page");
            } else {
                cardLayout.show(pages, "home");
            }
        });
    }

    private JLabel createCenteredLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.TEXT_MEDIUM);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(320, 42));
        field.setMaximumSize(new Dimension(320, 42));
        field.setFont(UITheme.INPUT_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.FIELD_BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(UITheme.TEXT_DARK);
        field.setCaretColor(UITheme.TEXT_DARK);
        field.setHorizontalAlignment(SwingConstants.LEFT);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();
        java.util.Random random = new java.util.Random();
    
        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }
    
        return password.toString();
    }

    private void styleComboBox(JComboBox<Role> comboBox) {
        comboBox.setPreferredSize(new Dimension(320, 42));
        comboBox.setMaximumSize(new Dimension(320, 42));
        comboBox.setFont(UITheme.INPUT_FONT);
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(UITheme.TEXT_DARK);
        //comboBox.setBorder(BorderFactory.createLineBorder(UITheme.FIELD_BORDER, 1));
        comboBox.setBorder(BorderFactory.createEmptyBorder());
        comboBox.setAlignmentX(Component.CENTER_ALIGNMENT);
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

    public void refreshInfo() {
        isAdmin = AccountController.currentAccount != null &&
                AccountController.currentAccount.getRole() == Role.ADMIN;

        roleLabel.setVisible(isAdmin);
        roleComboBox.setVisible(isAdmin);
        if (isAdmin) {
            passwordField.setText(generateRandomPassword(10));
        } else {
            passwordField.setText("");
        }
        formPanel.revalidate();
        formPanel.repaint();
    }
}