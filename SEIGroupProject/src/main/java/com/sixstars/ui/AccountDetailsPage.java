package com.sixstars.ui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class AccountDetailsPage extends JPanel {
    private final JPanel pages;
    private final CardLayout cardLayout;

    // Labels (View Mode)
    private JLabel firstNameVal = new JLabel("Unknown");
    private JLabel lastNameVal = new JLabel("Unknown");
    private JLabel emailVal = new JLabel("Unknown");
    private JLabel roleVal = new JLabel("Unknown");
    private JLabel roleBadgeLabel = new JLabel("Guest");
    private JLabel accountTypeVal = new JLabel("Standard Account");
    private JLabel quickHintLabel = new JLabel("Use quick actions below to navigate your account workflow.");

    // Fields (Edit Mode)
    private JTextField firstNameField = new JTextField();
    private JTextField lastNameField = new JTextField();
    private JPasswordField passField = new JPasswordField();

    // Labels for Password (Hidden by default)
    private JLabel passLabel = new JLabel("New Password:");
    private JLabel securityHintLabel = new JLabel("Leave blank to keep your current password.");

    private JButton editBtn;
    private JButton backBtn;
    private JButton reservationsBtn;
    private JButton shopBtn;
    private JButton billingBtn;
    private JButton dashboardBtn;
    private boolean isEditMode = false;
    private final AccountController accountController;

    public AccountDetailsPage(JPanel pages, CardLayout cardLayout, AccountController accountController) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.accountController = accountController;
        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(UITheme.CARD_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(32, 40, 32, 40)
        ));
        cardPanel.setPreferredSize(new Dimension(560, 700));

        JLabel title = new JLabel("Account Details");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Manage your profile, security, and account actions");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel roleBadgePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        roleBadgePanel.setOpaque(false);
        roleBadgeLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
        roleBadgeLabel.setBorder(new EmptyBorder(6, 14, 6, 14));
        roleBadgeLabel.setOpaque(true);
        roleBadgePanel.add(roleBadgeLabel);

        JPanel overviewPanel = new JPanel(new BorderLayout(12, 0));
        overviewPanel.setBackground(new Color(246, 239, 224));
        overviewPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel overviewTitle = new JLabel("Account Overview");
        overviewTitle.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        overviewTitle.setForeground(UITheme.TEXT_DARK);
        overviewPanel.add(overviewTitle, BorderLayout.WEST);

        accountTypeVal.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 13));
        accountTypeVal.setForeground(UITheme.TEXT_MEDIUM);
        overviewPanel.add(accountTypeVal, BorderLayout.EAST);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BACKGROUND);
        formPanel.setBorder(new EmptyBorder(22, 0, 20, 0));

        JLabel profileSectionTitle = new JLabel("Profile Information");
        profileSectionTitle.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
        profileSectionTitle.setForeground(UITheme.TEXT_DARK);

        styleValue(firstNameVal); styleValue(lastNameVal);
        styleValue(emailVal); styleValue(roleVal);
        styleField(firstNameField); styleField(lastNameField); styleField(passField);

        addSectionHeader(formPanel, 0, profileSectionTitle);
        addRow(formPanel, 0, "First Name:", firstNameVal, firstNameField);
        addRow(formPanel, 1, "Last Name:", lastNameVal, lastNameField);
        addRow(formPanel, 2, "Email:", emailVal, null);
        addRow(formPanel, 3, "Role:", roleVal, null);

        JLabel securitySectionTitle = new JLabel("Security");
        securitySectionTitle.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 16));
        securitySectionTitle.setForeground(UITheme.TEXT_DARK);
        addSectionHeader(formPanel, 4, securitySectionTitle);

        addRow(formPanel, 4, passLabel, null, passField);
        addHintRow(formPanel, 5, securityHintLabel);

        JPanel quickActionsPanel = new JPanel(new GridBagLayout());
        quickActionsPanel.setBackground(UITheme.CARD_BACKGROUND);
        quickActionsPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel quickActionsTitle = new JLabel("Quick Actions");
        quickActionsTitle.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 15));
        quickActionsTitle.setForeground(UITheme.TEXT_DARK);
        quickHintLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        quickHintLabel.setForeground(UITheme.TEXT_MEDIUM);

        reservationsBtn = new JButton("View Reservations");
        shopBtn = new JButton("Open Shop");
        billingBtn = new JButton("View Billing");
        dashboardBtn = new JButton("Go to Dashboard");

        styleSecondaryActionButton(reservationsBtn);
        styleSecondaryActionButton(shopBtn);
        styleSecondaryActionButton(billingBtn);
        styleSecondaryActionButton(dashboardBtn);

        reservationsBtn.addActionListener(e -> cardLayout.show(pages, "reservations"));
        shopBtn.addActionListener(e -> cardLayout.show(pages, "shop"));
        billingBtn.addActionListener(e -> cardLayout.show(pages, "billing page"));
        dashboardBtn.addActionListener(e -> navigateBackByRole());

        GridBagConstraints actionsGbc = new GridBagConstraints();
        actionsGbc.gridx = 0;
        actionsGbc.gridy = 0;
        actionsGbc.anchor = GridBagConstraints.WEST;
        actionsGbc.insets = new Insets(0, 0, 6, 0);
        quickActionsPanel.add(quickActionsTitle, actionsGbc);

        actionsGbc.gridy = 1;
        actionsGbc.insets = new Insets(0, 0, 12, 0);
        quickActionsPanel.add(quickHintLabel, actionsGbc);

        JPanel actionsButtonsRow = new JPanel(new GridLayout(1, 3, 10, 0));
        actionsButtonsRow.setOpaque(false);
        actionsButtonsRow.add(reservationsBtn);
        actionsButtonsRow.add(shopBtn);
        actionsButtonsRow.add(billingBtn);

        actionsGbc.gridy = 2;
        actionsGbc.insets = new Insets(0, 0, 10, 0);
        quickActionsPanel.add(actionsButtonsRow, actionsGbc);

        actionsGbc.gridy = 3;
        actionsGbc.insets = new Insets(0, 0, 0, 0);
        quickActionsPanel.add(dashboardBtn, actionsGbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 6));
        buttonPanel.setBackground(UITheme.CARD_BACKGROUND);

        editBtn = new JButton("Edit Profile");
        styleGoldButton(editBtn);
        editBtn.addActionListener(e -> handleEditAction());

        backBtn = new JButton("Back");
        styleGoldButton(backBtn);
        backBtn.addActionListener(e -> navigateBackByRole());

        buttonPanel.add(editBtn);
        buttonPanel.add(backBtn);

        cardPanel.add(title);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        cardPanel.add(subtitle);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        cardPanel.add(roleBadgePanel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        cardPanel.add(overviewPanel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        cardPanel.add(formPanel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        cardPanel.add(quickActionsPanel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 22)));
        cardPanel.add(buttonPanel);

        JPanel centeredContainer = new JPanel(new GridBagLayout());
        centeredContainer.setOpaque(false);
        centeredContainer.add(cardPanel);

        JScrollPane scrollPane = new JScrollPane(centeredContainer);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        scrollPane.setOpaque(false);

        add(scrollPane, BorderLayout.CENTER);

        toggleFields(false);
    }

    private void handleEditAction() {
        if (!isEditMode) {
            isEditMode = true;
            editBtn.setText("Save Changes");
            toggleFields(true);
        } else {
            saveChanges();
        }
    }

    private void saveChanges() {
        try {
            accountController.updateProfile(
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    new String(passField.getPassword())
            );

            JOptionPane.showMessageDialog(this, "Profile Updated Successfully!");
            isEditMode = false;
            editBtn.setText("Edit Profile");
            toggleFields(false);
            refreshInfo();
            Main.headerBar.refreshInfo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void toggleFields(boolean editing) {
        firstNameVal.setVisible(!editing);
        lastNameVal.setVisible(!editing);

        firstNameField.setVisible(editing);
        lastNameField.setVisible(editing);

        passLabel.setVisible(editing);
        passField.setVisible(editing);
        securityHintLabel.setVisible(editing);

        editBtn.setText(editing ? "Save Changes" : "Edit Profile");

        revalidate();
        repaint();
    }

    public void refreshInfo() {
        Account acc = AccountController.currentAccount;
        if (acc != null) {
            firstNameVal.setText(acc.getFirstName());
            lastNameVal.setText(acc.getLastName());
            emailVal.setText(acc.getEmail());
            roleVal.setText(formatRole(acc.getRole().toString()));

            firstNameField.setText(acc.getFirstName());
            lastNameField.setText(acc.getLastName());
            passField.setText("");

            boolean isClerk = acc.getRole() == Role.CLERK;
            boolean isAdmin = acc.getRole() == Role.ADMIN;

            editBtn.setVisible(isClerk);
            passLabel.setText(isClerk ? "New Password:" : "Password:");

            accountTypeVal.setText(isAdmin ? "Administrator Account" : isClerk ? "Clerk Account" : "Guest Account");
            roleBadgeLabel.setText(formatRole(acc.getRole().name()));

            if (isAdmin) {
                roleBadgeLabel.setBackground(new Color(220, 198, 117));
                roleBadgeLabel.setForeground(UITheme.TEXT_DARK);
                quickHintLabel.setText("Admin actions are available from your dashboard.");
            } else if (isClerk) {
                roleBadgeLabel.setBackground(new Color(197, 217, 236));
                roleBadgeLabel.setForeground(UITheme.TEXT_DARK);
                quickHintLabel.setText("Use quick actions for reservations, shop, and billing tasks.");
            } else {
                roleBadgeLabel.setBackground(new Color(211, 233, 204));
                roleBadgeLabel.setForeground(UITheme.TEXT_DARK);
                quickHintLabel.setText("Use quick actions for your stay and shopping experience.");
            }

            reservationsBtn.setVisible(!isAdmin);
            shopBtn.setVisible(!isAdmin);
            billingBtn.setVisible(isClerk || acc.getRole() == Role.GUEST);
            dashboardBtn.setText(isAdmin ? "Go to Admin Page" : isClerk ? "Go to Clerk Dashboard" : "Go to Home");
        }
    }

    private void navigateBackByRole() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            cardLayout.show(pages, "home");
            return;
        }

        if (account.getRole() == Role.CLERK) {
            cardLayout.show(pages, "clerk page");
        } else if (account.getRole() == Role.ADMIN) {
            cardLayout.show(pages, "admin page");
        } else {
            cardLayout.show(pages, "home");
        }
    }

    private void addSectionHeader(JPanel panel, int row, JLabel sectionLabel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(row == 0 ? 0 : 20, 8, 10, 8);
        panel.add(sectionLabel, gbc);
    }

    private void addHintRow(JPanel panel, int row, JLabel hintLabel) {
        hintLabel.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12));
        hintLabel.setForeground(UITheme.TEXT_MEDIUM);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = row * 2 + 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(2, 8, 8, 8);
        panel.add(hintLabel, gbc);
    }

    private void addRow(JPanel panel, int row, Object labelObj, JLabel valueLabel, JComponent editField) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = row * 2 + 1;

        JLabel l;
        if (labelObj instanceof String) {
            l = new JLabel((String) labelObj);
            l.setFont(UITheme.LABEL_FONT);
            l.setForeground(UITheme.TEXT_MEDIUM);
        } else {
            l = (JLabel) labelObj;
            l.setFont(UITheme.LABEL_FONT);
            l.setForeground(UITheme.TEXT_MEDIUM);
        }

        gbc.gridx = 0;
        panel.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        if (valueLabel != null) panel.add(valueLabel, gbc);
        if (editField != null) panel.add(editField, gbc);
    }

    private void styleValue(JLabel label) {
        label.setFont(UITheme.INPUT_FONT);
        label.setForeground(UITheme.TEXT_DARK);
    }

    private void styleField(JTextField field) {
        field.setFont(UITheme.INPUT_FONT);
        field.setPreferredSize(new Dimension(230, 34));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
    }

    private void styleGoldButton(JButton button) {
        button.setPreferredSize(new Dimension(220, 44));
        button.setMaximumSize(new Dimension(220, 44));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private void styleSecondaryActionButton(JButton button) {
        button.setPreferredSize(new Dimension(210, 38));
        button.setMinimumSize(new Dimension(210, 38));
        button.setMaximumSize(new Dimension(210, 38));
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(243, 243, 243));
        button.setForeground(UITheme.TEXT_DARK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(7, 12, 7, 12)
        ));
        button.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private String formatRole(String text) {
        if (text == null || text.isBlank()) return "Unknown";
        text = text.toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}