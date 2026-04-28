package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Role;
import com.sixstars.service.BillingService;
import com.sixstars.model.Reservation;
import com.sixstars.model.ShopOrder;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.List;

public class ClerkBillingSearchPage extends JPanel {
    private final BillingService billingService;
    private final JTextField emailField;
    private final JPanel resultsPanel;

    public ClerkBillingSearchPage(JPanel pages, CardLayout cardLayout) {
        this.billingService = new BillingService();
        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        // --- Search Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PAGE_BACKGROUND);
        header.setBorder(new EmptyBorder(20, 40, 10, 40));

        JLabel title = new JLabel("Guest Billing Search");
        title.setFont(UITheme.TITLE_FONT); // Use constant from UITheme
        title.setForeground(UITheme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        // Back Button matching CheckInPage's "false" primary style
        JButton btnBack = createThemedButton("Back to Dashboard", false);
        btnBack.addActionListener(e -> {
            if (AccountController.currentAccount != null
                    && AccountController.currentAccount.getRole() == Role.ADMIN) {
                cardLayout.show(pages, "admin page");
            } else {
                cardLayout.show(pages, "clerk page");
            }
        });
        header.add(btnBack, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(UITheme.PAGE_BACKGROUND);
        searchPanel.setBorder(new EmptyBorder(10, 40, 10, 40));

        JLabel searchLabel = new JLabel("Guest Email: ");
        searchLabel.setFont(UITheme.LABEL_FONT);
        searchPanel.add(searchLabel);

        emailField = new JTextField(20);
        emailField.setFont(UITheme.INPUT_FONT);
        searchPanel.add(emailField);

        JButton btnSearch = createThemedButton("Generate Bill", true);
        btnSearch.addActionListener(e -> performSearch(emailField.getText().trim()));
        this.addHierarchyListener(e -> {
            emailField.requestFocusInWindow();
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                javax.swing.JRootPane root = javax.swing.SwingUtilities.getRootPane(this);
                if (root != null) {
                    root.setDefaultButton(btnSearch);
                }
            }
        });
        searchPanel.add(btnSearch);

        // --- Results Area ---
        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setOpaque(false);
        resultsPanel.setBorder(new EmptyBorder(10, 40, 30, 40));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(header);
        topContainer.add(searchPanel);

        add(topContainer, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
    }

    private void performSearch(String email) {
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a guest email.");
            return;
        }

        resultsPanel.removeAll();
        JPanel billingContent = createExternalBillingView(email);
        resultsPanel.add(billingContent, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createExternalBillingView(String email) {
        BillingPage customView = new BillingPage();
        customView.refreshForEmail(email);

        return customView;
    }

    private JButton createThemedButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(UITheme.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 40));

        if (isPrimary) {
            button.setBackground(UITheme.ACCENT_GOLD);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(UITheme.SECONDARY_BUTTON);
            button.setForeground(UITheme.TEXT_DARK);
        }
        button.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        button.setOpaque(true);
        return button;
    }
}