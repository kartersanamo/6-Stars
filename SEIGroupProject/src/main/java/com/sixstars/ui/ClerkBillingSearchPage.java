package com.sixstars.ui;

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
    private final BillingPage billingDisplayRenderer; // We reuse the rendering logic

    public ClerkBillingSearchPage() {
        this.billingService = new BillingService();
        this.billingDisplayRenderer = new BillingPage();

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        // --- Search Header ---
        JPanel searchHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        searchHeader.setBackground(UITheme.CARD_BACKGROUND);
        searchHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR));

        JLabel searchLabel = new JLabel("Enter Guest Email:");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        emailField = new JTextField(25);
        emailField.setPreferredSize(new Dimension(200, 35));

//        emailField.addHierarchyListener(new HierarchyListener() {
//            @Override
//            public void hierarchyChanged(HierarchyEvent e) {
//                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && emailField.isShowing()) {
//                    emailField.requestFocusInWindow();
//                }
//            }
//        });

        JButton btnSearch = new JButton("Generate Bill");
        styleSearchButton(btnSearch);

        btnSearch.addActionListener(e -> performSearch(emailField.getText().trim()));
//        SwingUtilities.invokeLater(() -> {
//            JRootPane rootPane = SwingUtilities.getRootPane(btnSearch);
//            if (rootPane != null) {
//                rootPane.setDefaultButton(btnSearch);
//            }
//        });
        this.addHierarchyListener(e -> {
            emailField.requestFocusInWindow();
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                javax.swing.JRootPane root = javax.swing.SwingUtilities.getRootPane(this);
                if (root != null) {
                    root.setDefaultButton(btnSearch);
                }
            }
        });
        searchHeader.add(searchLabel);
        searchHeader.add(emailField);
        searchHeader.add(btnSearch);

        // --- Results Area ---
        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setOpaque(false);

        add(searchHeader, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
    }

    private void performSearch(String email) {
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a guest email.");
            return;
        }

        resultsPanel.removeAll();

        // We temporarily "force" the billing renderer to show a specific email's data
        // Note: You may need to modify BillingPage.java slightly to accept an email parameter
        JPanel billingContent = createExternalBillingView(email);
        resultsPanel.add(billingContent, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createExternalBillingView(String email) {
        // Logic similar to BillingPage.refresh() but using the provided email
        // Instead of AccountController.currentAccount.getEmail()
        BillingPage customView = new BillingPage();

        // Since BillingPage's refresh() uses AccountController,
        // you'll want to add a method to BillingPage called 'refreshForEmail(String email)'
        // For now, let's assume we call a modified version:
         customView.refreshForEmail(email);

        return customView;
    }

    private void styleSearchButton(JButton btn) {
        btn.setBackground(UITheme.ACCENT_GOLD);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(150, 35));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}