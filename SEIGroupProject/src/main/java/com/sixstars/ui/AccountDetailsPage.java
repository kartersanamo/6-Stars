package com.sixstars.ui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class AccountDetailsPage extends JPanel {
    // Labels (View Mode)
    private JLabel firstNameVal = new JLabel("Unknown");
    private JLabel lastNameVal = new JLabel("Unknown");
    private JLabel emailVal = new JLabel("Unknown");
    private JLabel roleVal = new JLabel("Unknown");

    // Fields (Edit Mode)
    private JTextField firstNameField = new JTextField();
    private JTextField lastNameField = new JTextField();
    private JPasswordField passField = new JPasswordField();

    // Labels for Password (Hidden by default)
    private JLabel passLabel = new JLabel("New Password:");

    private JButton editBtn;
    private JButton backBtn;
    private boolean isEditMode = false;
    private final AccountController accountController;

    public AccountDetailsPage(JPanel pages, CardLayout cardLayout, AccountController accountController) {
        this.accountController = accountController;
        setLayout(new GridBagLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        // Main Card Panel
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(UITheme.CARD_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));
        cardPanel.setPreferredSize(new Dimension(500, 600)); // Slightly taller for password row

        // Title
        JLabel title = new JLabel("Account Details");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BACKGROUND);
        formPanel.setBorder(new EmptyBorder(25, 0, 25, 0));

        // Style the values
        styleValue(firstNameVal); styleValue(lastNameVal);
        styleValue(emailVal); styleValue(roleVal);
        styleField(firstNameField); styleField(lastNameField); styleField(passField);

        // Add Rows
        addRow(formPanel, 0, "First Name:", firstNameVal, firstNameField);
        addRow(formPanel, 1, "Last Name:", lastNameVal, lastNameField);
        addRow(formPanel, 2, "Email:", emailVal, null);
        addRow(formPanel, 3, "Role:", roleVal, null);

        // Password row - specifically using passLabel so we can hide/show the text too
        addRow(formPanel, 4, passLabel, null, passField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(UITheme.CARD_BACKGROUND);

        editBtn = new JButton("Edit Profile");
        styleGoldButton(editBtn);
        editBtn.addActionListener(e -> handleEditAction());

        backBtn = new JButton("Back");
        styleGoldButton(backBtn);
        backBtn.addActionListener(e -> {
            var account = AccountController.currentAccount;
            if (account != null && (account.getRole() == Role.CLERK)) {
                cardLayout.show(pages, "clerk page");
            } else {
                cardLayout.show(pages, "home");
            }
        });

        buttonPanel.add(editBtn);
        buttonPanel.add(backBtn);

        cardPanel.add(title);
        cardPanel.add(formPanel);
        cardPanel.add(buttonPanel);

        add(cardPanel);

        // Initial state
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
        // Values show when NOT editing
        firstNameVal.setVisible(!editing);
        lastNameVal.setVisible(!editing);

        // Fields show ONLY when editing
        firstNameField.setVisible(editing);
        lastNameField.setVisible(editing);

        // Password row visibility
        passLabel.setVisible(editing);
        passField.setVisible(editing);

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

            // Only clerks get the edit button
            editBtn.setVisible(acc.getRole() == Role.CLERK);
        }
    }

    private void addRow(JPanel panel, int row, Object labelObj, JLabel valueLabel, JComponent editField) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = row;

        // Label handling (can be String or existing JLabel)
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
        field.setPreferredSize(new Dimension(200, 30));
    }

  
    private void styleGoldButton(JButton button) {
        button.setPreferredSize(new Dimension(320, 44));
        button.setMaximumSize(new Dimension(320, 44));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    private String formatRole(String text) {
        if (text == null || text.isBlank()) return "Unknown";
        text = text.toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}