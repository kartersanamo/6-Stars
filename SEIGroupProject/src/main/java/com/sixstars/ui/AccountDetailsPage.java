package com.sixstars.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sixstars.app.Main;

public class AccountDetailsPage extends JPanel {
    static JLabel firstName = new JLabel("Unknown");
    static JLabel lastName = new JLabel("Unknown");
    static JLabel email = new JLabel("Unknown");
    static JLabel role = new JLabel("Unknown");

    public AccountDetailsPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new GridBagLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(UITheme.CARD_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));
        cardPanel.setPreferredSize(new Dimension(500, 330));

        // Title
        JLabel title = new JLabel("Account Details");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(UITheme.CARD_BACKGROUND);
        formPanel.setBorder(new EmptyBorder(25, 0, 25, 0));

        styleValue(firstName);
        styleValue(lastName);
        styleValue(email);
        styleValue(role);

        addRow(formPanel, 0, "First Name:", firstName);
        addRow(formPanel, 1, "Last Name:", lastName);
        addRow(formPanel, 2, "Email:", email);
        addRow(formPanel, 3, "Role:", role);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        buttonPanel.setBackground(UITheme.CARD_BACKGROUND);

        JButton back = new JButton("Back");
        back.setFont(UITheme.BUTTON_FONT);
        back.setForeground(Color.WHITE);
        back.setBackground(UITheme.ACCENT_GOLD);
        back.setFocusPainted(false);
        back.setBorder(BorderFactory.createEmptyBorder(10, 24, 10, 24));
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));

        back.addActionListener(e -> {
            Main.headerBar.refreshInfo();
            var account = com.sixstars.controller.AccountController.currentAccount;

            if (account != null && (account.getRole() == com.sixstars.model.Role.CLERK)) {
                cardLayout.show(pages, "clerk page");
            } else {
                cardLayout.show(pages, "home");
            }
        });

        buttonPanel.add(back);

        cardPanel.add(title);
        cardPanel.add(formPanel);
        cardPanel.add(buttonPanel);

        add(cardPanel);
    }

    void refreshInfo() {
        try {
            var account = com.sixstars.controller.AccountController.currentAccount;

            if (account != null) {
                firstName.setText(account.getFirstName() != null ? account.getFirstName() : "Unknown");
                lastName.setText(account.getLastName() != null ? account.getLastName() : "Unknown");
                email.setText(account.getEmail() != null ? account.getEmail() : "Unknown");

                String roleText = account.getRole() != null ? account.getRole().toString() : "Unknown";
                role.setText(formatRole(roleText));
            } else {
                firstName.setText("Unknown");
                lastName.setText("Unknown");
                email.setText("Unknown");
                role.setText("Unknown");
            }

            revalidate();
            repaint();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Failed to Refresh Account Details",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void addRow(JPanel panel, int row, String labelText, JLabel valueLabel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel label = new JLabel(labelText);
        label.setFont(UITheme.LABEL_FONT);
        label.setForeground(UITheme.TEXT_MEDIUM);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(valueLabel, gbc);
    }

    private void styleValue(JLabel label) {
        label.setFont(UITheme.INPUT_FONT);
        label.setForeground(UITheme.TEXT_DARK);
    }

    private String formatRole(String text) {
        if (text == null || text.isBlank()) {
            return "Unknown";
        }

        text = text.toLowerCase();
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}