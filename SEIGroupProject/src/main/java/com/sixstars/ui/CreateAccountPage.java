package com.sixstars.ui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.BorderLayout;
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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;
import com.sixstars.service.security.PasswordStrengthEvaluator;
import com.sixstars.ui.components.PasswordStrengthHeaderPanel;

public class CreateAccountPage extends JPanel {
    private final AccountController accountController;
    private JPanel formPanel;
    private JLabel roleLabel;
    private JComboBox<Role> roleComboBox;
    private JTextField emailField;
    private boolean isAdmin;
    JPasswordField passwordField;
    private final PasswordStrengthHeaderPanel passwordStrengthHeader = new PasswordStrengthHeaderPanel(true);
    private Runnable refreshAccountValidationUi;

    public CreateAccountPage(JPanel pages, CardLayout cardLayout) {
        accountController = new AccountController();

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(35, 45, 35, 45)
        ));
        card.setPreferredSize(new Dimension(460, 1050));

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
        emailField = new JTextField();
        styleTextField(emailField);

        JLabel emailRequirementLabel = createRequirementLabel("Valid email format (example@domain.com)");

        JLabel passwordLabel = createCenteredLabel("Password");
        passwordField = new JPasswordField();
        styleTextField(passwordField);
        passwordField.setEchoChar('*');

        JLabel confirmPasswordLabel = createCenteredLabel("Confirm Password");
        JPasswordField confirmPasswordField = new JPasswordField();
        styleTextField(confirmPasswordField);
        confirmPasswordField.setEchoChar('*');

        JLabel passwordLengthRequirementLabel = createRequirementLabel("At least 8 characters");
        JLabel passwordUpperRequirementLabel = createRequirementLabel("At least 1 uppercase letter");
        JLabel passwordLowerRequirementLabel = createRequirementLabel("At least 1 lowercase letter");
        JLabel passwordDigitRequirementLabel = createRequirementLabel("At least 1 number");
        JLabel passwordSpecialRequirementLabel = createRequirementLabel("At least 1 special character");
        JLabel passwordMatchRequirementLabel = createRequirementLabel("Passwords match");

        JCheckBox showPassword = new JCheckBox("Show Password");
        showPassword.setBackground(UITheme.CARD_BACKGROUND);

        showPassword.addActionListener(e -> {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar((char) 0); // show text
                confirmPasswordField.setEchoChar((char) 0); // show text
            } else {
                passwordField.setEchoChar('*'); // hide text again
                confirmPasswordField.setEchoChar('*'); // hide text again
            }
        });

        roleLabel = createCenteredLabel("Role");
        Role[] roles = {Role.CLERK};
        roleComboBox = new JComboBox<>(roles);
        styleComboBox(roleComboBox);

        JButton createButton = new JButton("Create Account");
        stylePrimaryButton(createButton);

        JLabel loginPromptLabel = new JLabel("Already have an account?");
        loginPromptLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        loginPromptLabel.setForeground(UITheme.TEXT_MEDIUM);
        loginPromptLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton loginButton = new JButton("Log In");
        styleLinkButton(loginButton);

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
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(emailRequirementLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(passwordLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(passwordField, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 10, 0);
        formPanel.add(confirmPasswordLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(confirmPasswordField, gbc);

        JLabel strengthTitle = createCenteredLabel("Password strength");
        strengthTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        strengthTitle.setForeground(UITheme.TEXT_DARK);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 6, 0);
        formPanel.add(strengthTitle, gbc);

        JPanel strengthWrap = new JPanel();
        strengthWrap.setLayout(new BoxLayout(strengthWrap, BoxLayout.Y_AXIS));
        strengthWrap.setOpaque(false);
        strengthWrap.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordStrengthHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordStrengthHeader.setMaximumSize(new Dimension(320, 44));
        strengthWrap.add(passwordStrengthHeader);
        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(strengthWrap, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(passwordLengthRequirementLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(passwordUpperRequirementLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(passwordLowerRequirementLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 4, 0);
        formPanel.add(passwordDigitRequirementLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(passwordSpecialRequirementLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 18, 0);
        formPanel.add(passwordMatchRequirementLabel, gbc);

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

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 6, 0);
        formPanel.add(loginPromptLabel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(0, 0, 14, 0);
        formPanel.add(loginButton, gbc);

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

        JPanel cardContainer = new JPanel(new GridBagLayout());
        cardContainer.setOpaque(false);
        cardContainer.add(card);

        JScrollPane scrollPane = new JScrollPane(cardContainer);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        scrollPane.setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);

        Runnable refreshValidationUI = () -> {
            String emailText = emailField.getText().trim();
            String passwordText = new String(passwordField.getPassword());
            String confirmPasswordText = new String(confirmPasswordField.getPassword());

            boolean emailValid = isValidEmail(emailText);
            PasswordStrengthEvaluator.Result strength = PasswordStrengthEvaluator.evaluate(passwordText);
            boolean passwordsMatch = !passwordText.isEmpty()
                    && PasswordStrengthEvaluator.matches(passwordText, confirmPasswordText);

            passwordStrengthHeader.updateFromNewPassword(passwordText);

            updateRequirementLabel(emailRequirementLabel, "Valid email format (example@domain.com)", emailValid);
            updateRequirementLabel(passwordLengthRequirementLabel, "At least 8 characters", strength.lengthOk());
            updateRequirementLabel(passwordUpperRequirementLabel, "At least 1 uppercase letter", strength.upperOk());
            updateRequirementLabel(passwordLowerRequirementLabel, "At least 1 lowercase letter", strength.lowerOk());
            updateRequirementLabel(passwordDigitRequirementLabel, "At least 1 number", strength.digitOk());
            updateRequirementLabel(passwordSpecialRequirementLabel, "At least 1 special character", strength.specialOk());
            updateRequirementLabel(passwordMatchRequirementLabel, "Passwords match", passwordsMatch);

            boolean passwordOk = strength.lengthOk() && strength.upperOk() && strength.lowerOk()
                    && strength.digitOk() && strength.specialOk();
            createButton.setEnabled(emailValid && passwordOk && passwordsMatch);
            updatePrimaryButtonState(createButton);
        };
        refreshAccountValidationUi = refreshValidationUI;

        DocumentListener validationListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshValidationUI.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshValidationUI.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshValidationUI.run();
            }
        };

        emailField.getDocument().addDocumentListener(validationListener);
        passwordField.getDocument().addDocumentListener(validationListener);
        confirmPasswordField.getDocument().addDocumentListener(validationListener);
        refreshValidationUI.run();

        createButton.addActionListener(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String confirmPassword = new String(confirmPasswordField.getPassword()).trim();
            Role roleSet;

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please fill in all fields.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Passwords do not match.",
                        "Invalid Information",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (!isValidEmail(email) || !passwordRulesSatisfied(password)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please satisfy all email and password requirements.",
                        "Invalid Information",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            try {
                if (!isAdmin) {
                    roleSet = Role.GUEST;
                } else {
                    roleSet = Role.CLERK;
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
                confirmPasswordField.setText("");
                refreshValidationUI.run();
                

                if (!isAdmin) {
                    try {
                        accountController.sendVerificationCode(createdAccount.getEmail());
                    } catch (RuntimeException verificationEx) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Account created, but the verification email could not be sent yet. " + verificationEx.getMessage(),
                                "Verification Email Failed",
                                JOptionPane.WARNING_MESSAGE
                        );
                    }

                    EmailVerificationDialog dialog = new EmailVerificationDialog(
                            SwingUtilities.getWindowAncestor(this),
                            accountController,
                            createdAccount.getEmail()
                    );
                    dialog.setVisible(true);

                    if (!dialog.wasVerified()) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Please verify your email to continue.",
                                "Verification Required",
                                JOptionPane.INFORMATION_MESSAGE
                        );
                        return;
                    }

                    Account verifiedAccount = accountController.getAccountByEmail(createdAccount.getEmail());
                    if (verifiedAccount == null) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Verification succeeded, but we couldn't reload the new account.",
                                "Verification Error",
                                JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    AccountController.currentAccount = verifiedAccount;

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

        loginButton.addActionListener(e -> cardLayout.show(pages, "login"));
    }

    private JLabel createCenteredLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.TEXT_MEDIUM);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JLabel createRequirementLabel(String text) {
        JLabel label = new JLabel("✗ " + text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setHorizontalAlignment(SwingConstants.LEFT);
        label.setForeground(new Color(180, 46, 46));
        return label;
    }

    private void updateRequirementLabel(JLabel label, String text, boolean met) {
        label.setText((met ? "✓ " : "✗ ") + text);
        label.setForeground(met ? new Color(34, 139, 34) : new Color(180, 46, 46));
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");
    }

    private boolean passwordRulesSatisfied(String password) {
        PasswordStrengthEvaluator.Result r = PasswordStrengthEvaluator.evaluate(password);
        return r.lengthOk() && r.upperOk() && r.lowerOk() && r.digitOk() && r.specialOk();
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
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(126, 94, 43), 1));
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        updatePrimaryButtonState(button);
    }

    private void updatePrimaryButtonState(JButton button) {
        if (button.isEnabled()) {
            button.setBackground(UITheme.ACCENT_GOLD);
            button.setForeground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(new Color(126, 94, 43), 1));
        } else {
            button.setBackground(new Color(224, 224, 224));
            button.setForeground(new Color(120, 120, 120));
            button.setBorder(BorderFactory.createLineBorder(new Color(190, 190, 190), 1));
        }
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

    public void refreshInfo() {
        isAdmin = AccountController.currentAccount != null &&
                AccountController.currentAccount.getRole() == Role.ADMIN;

        roleLabel.setVisible(isAdmin);
        roleComboBox.setVisible(isAdmin);
        if (isAdmin) {
            roleComboBox.setSelectedItem(Role.CLERK);
            passwordField.setText(generateRandomPassword(10));
        } else {
            passwordField.setText("");
        }
        formPanel.revalidate();
        formPanel.repaint();
        if (refreshAccountValidationUi != null) {
            refreshAccountValidationUi.run();
        }
    }
}