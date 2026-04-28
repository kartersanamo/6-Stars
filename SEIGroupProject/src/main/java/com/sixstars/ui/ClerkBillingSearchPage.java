package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;
import com.sixstars.service.BillingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

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

        JLabel title = new JLabel("Billing Search & Hotel Summary");
        title.setFont(UITheme.TITLE_FONT); // Use constant from UITheme
        title.setForeground(UITheme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        // Back Button matching CheckInPage's "false" primary style
        JButton btnBack = createThemedButton("Back to Dashboard", false);
        btnBack.addActionListener(e -> {
            Account current = AccountController.currentAccount;
            if (current != null && current.getRole() == Role.ADMIN) {
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

        JButton btnSummary = createThemedButton("Hotel Summary", false);
        btnSummary.addActionListener(e -> showHotelSummary());
        searchPanel.add(btnSummary);

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

    private void showHotelSummary() {
        resultsPanel.removeAll();
        resultsPanel.add(createHotelSummaryCard(), BorderLayout.NORTH);
        revalidate();
        repaint();
    }

    private JPanel createHotelSummaryCard() {
        int reservationCount = billingService.getAllReservationCharges().size();
        int reservationTotal = billingService.getHotelReservationTotal();
        int shopOrderCount = billingService.getAllShopPurchases().size();
        double shopTotal = billingService.getHotelShopTotal();
        double grandTotal = billingService.getHotelGrandTotal();

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel title = new JLabel("Hotel Financial Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel reservations = new JLabel(String.format("Reservations tracked: %d", reservationCount));
        reservations.setFont(new Font("SansSerif", Font.PLAIN, 15));
        reservations.setForeground(UITheme.TEXT_MEDIUM);
        reservations.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel reservationRevenue = new JLabel(String.format("Reservation charges total: $%d.00", reservationTotal));
        reservationRevenue.setFont(new Font("SansSerif", Font.BOLD, 16));
        reservationRevenue.setForeground(UITheme.TEXT_DARK);
        reservationRevenue.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel orders = new JLabel(String.format("Shop payments tracked: %d", shopOrderCount));
        orders.setFont(new Font("SansSerif", Font.PLAIN, 15));
        orders.setForeground(UITheme.TEXT_MEDIUM);
        orders.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel shopRevenue = new JLabel(String.format("Shop payments total: $%.2f", shopTotal));
        shopRevenue.setFont(new Font("SansSerif", Font.BOLD, 16));
        shopRevenue.setForeground(UITheme.TEXT_DARK);
        shopRevenue.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel overall = new JLabel(String.format("Combined total: $%.2f", grandTotal));
        overall.setFont(new Font("SansSerif", Font.BOLD, 20));
        overall.setForeground(new Color(176, 132, 38));
        overall.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(reservations);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(reservationRevenue);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(orders);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(shopRevenue);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(overall);

        return card;
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