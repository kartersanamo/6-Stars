package com.sixstars.ui;

import com.sixstars.service.BillingService;
import com.sixstars.service.AccountService;
import com.sixstars.model.Reservation;
import com.sixstars.model.ShopOrder;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.List;
import java.util.stream.Collectors;

public class ClerkBillingSearchPage extends JPanel {
    private final BillingService billingService;
    private final AccountService accountService;
    private final JTextField emailField;
    private final JComboBox<String> guestComboBox;
    private final JPanel resultsPanel;

    public ClerkBillingSearchPage(JPanel pages, CardLayout cardLayout) {
        this.billingService = new BillingService();
        this.accountService = new AccountService();
        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        // --- Search Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PAGE_BACKGROUND);
        header.setBorder(new EmptyBorder(20, 40, 10, 40));

        JLabel title = new JLabel("Guest Billing Search");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        // Back Button
        JButton btnBack = createThemedButton("Back to Dashboard", false);
        btnBack.addActionListener(e -> cardLayout.show(pages, "clerk page"));
        header.add(btnBack, BorderLayout.EAST);

        // --- Guest Dropdown ---
        JPanel guestSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        guestSelectionPanel.setBackground(UITheme.PAGE_BACKGROUND);
        guestSelectionPanel.setBorder(new EmptyBorder(10, 40, 0, 40));

        JLabel guestLabel = new JLabel("Select Guest: ");
        guestLabel.setFont(UITheme.LABEL_FONT);
        guestSelectionPanel.add(guestLabel);

        guestComboBox = new JComboBox<>();
        guestComboBox.setFont(UITheme.INPUT_FONT);
        guestComboBox.setPreferredSize(new Dimension(250, 32));
        guestComboBox.addItem("-- Select a guest --");
        guestComboBox.addActionListener(e -> onGuestSelected());
        guestSelectionPanel.add(guestComboBox);

        // --- Search Panel ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(UITheme.PAGE_BACKGROUND);
        searchPanel.setBorder(new EmptyBorder(10, 40, 10, 40));

        JLabel searchLabel = new JLabel("Or enter email: ");
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
                refreshGuestList();
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
        topContainer.add(guestSelectionPanel);
        topContainer.add(searchPanel);

        add(topContainer, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
    }

    private void refreshGuestList() {
        guestComboBox.removeAllItems();
        guestComboBox.addItem("-- Select a guest --");

        try {
            List<Account> allAccounts = accountService.getAllAccounts();
            List<String> guestEmails = allAccounts.stream()
                    .filter(a -> a.getRole() == Role.GUEST)
                    .map(Account::getEmail)
                    .sorted()
                    .collect(Collectors.toList());

            for (String email : guestEmails) {
                guestComboBox.addItem(email);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading guests: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onGuestSelected() {
        Object selected = guestComboBox.getSelectedItem();
        if (selected != null && !selected.equals("-- Select a guest --")) {
            String email = (String) selected;
            emailField.setText(email);
            performSearch(email);
        }
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