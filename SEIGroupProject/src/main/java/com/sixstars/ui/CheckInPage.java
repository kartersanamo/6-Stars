package com.sixstars.ui;

import java.awt.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.sixstars.model.Reservation;
import com.sixstars.service.ReservationService;

public class CheckInPage extends JPanel {
    private JTextField emailField;
    private JTable reservationTable;
    private DefaultTableModel tableModel;
    private ReservationService reservationService;

    public CheckInPage(JPanel pages, CardLayout cardLayout, ReservationService reservationService) {
        this.reservationService = reservationService;
        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        // --- Top Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PAGE_BACKGROUND);
        header.setBorder(new EmptyBorder(20, 40, 10, 40));

        JLabel title = new JLabel("Guest Check-In");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        JButton btnBack = createThemedButton("Back to Dashboard", false);
        btnBack.addActionListener(e -> cardLayout.show(pages, "clerk page"));
        header.add(btnBack, BorderLayout.EAST);

        // --- Search Bar ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(UITheme.PAGE_BACKGROUND);
        searchPanel.setBorder(new EmptyBorder(10, 40, 10, 40));

        searchPanel.add(new JLabel("Guest Email: "));
        emailField = new JTextField(20);
        emailField.setFont(UITheme.INPUT_FONT);
        searchPanel.add(emailField);

        JButton btnSearch = createThemedButton("Search", true);
        btnSearch.addActionListener(e -> refreshTable());
        searchPanel.add(btnSearch);

        JButton btnClear = createThemedButton("Clear", false);
        btnClear.addActionListener(e -> {
            emailField.setText("");
            refreshTable();
        });
        searchPanel.add(btnClear);

        // --- Table Setup ---
        String[] columns = {"ID", "Email", "Start Date", "End Date", "Total Cost", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        reservationTable = new JTable(tableModel);
        reservationTable.setFont(UITheme.INPUT_FONT);
        reservationTable.setRowHeight(30);
        reservationTable.setSelectionBackground(new Color(230, 220, 200));

        // Style Table Header
        JTableHeader tableHeader = reservationTable.getTableHeader();
        tableHeader.setFont(UITheme.LABEL_FONT);
        tableHeader.setBackground(UITheme.CARD_BACKGROUND);
        tableHeader.setForeground(UITheme.TEXT_DARK);
        JScrollPane scrollPane = new JScrollPane(reservationTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setBackground(UITheme.PAGE_BACKGROUND);
        tableWrapper.setBorder(new EmptyBorder(10, 40, 10, 40));
        tableWrapper.add(scrollPane, BorderLayout.CENTER);

        // --- Bottom Action Bar ---
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setBackground(UITheme.PAGE_BACKGROUND);
        footer.setBorder(new EmptyBorder(10, 40, 30, 40));

        JButton btnCheckIn = createThemedButton("Confirm Check-In", true);

        btnCheckIn.addActionListener(e -> {
            try {
                int selectedRow = reservationTable.getSelectedRow();
                if (selectedRow != -1) {
                    int resId = (int) tableModel.getValueAt(selectedRow, 0);

                    // Call the service logic we just wrote
                    reservationService.updateStatus(resId, "CHECKED_IN");

                    JOptionPane.showMessageDialog(this, "Guest successfully checked in!");
                    refreshTable();
                }
            } catch (IllegalStateException ex) {
                // This catches our "not today" error
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Check-In Denied", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnCheckOut = createThemedButton("Confirm Check-Out", false);

        btnCheckOut.addActionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            if (selectedRow != -1) {
                int resId = (int) tableModel.getValueAt(selectedRow, 0);
                reservationService.updateStatus(resId, "CHECKED_OUT");
                JOptionPane.showMessageDialog(this, "Guest Checked Out!");
                refreshTable();
            }
        });

        footer.add(btnCheckIn);
        footer.add(btnCheckOut);

        // --- Add everything to main panel ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(tableWrapper, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String email = emailField.getText().trim();
        LocalDate today = LocalDate.now();

        List<Reservation> allReservations;

        // --- NEW LOGIC: Fetch all if empty, otherwise search by email ---
        if (email.isEmpty()) {
            allReservations = reservationService.getAllReservations();
        } else {
            allReservations = reservationService.getGuestReservations(email);
        }

        // Apply the active status filters
        List<Reservation> visibleReservations = allReservations.stream()
                .filter(res -> {
                    // Rule: Eligible for Check-In (Within dates and status is BOOKED)
                    boolean canCheckIn = !today.isBefore(res.getStartDate()) &&
                            today.isBefore(res.getEndDate()) &&
                            "BOOKED".equalsIgnoreCase(res.getStatus());

                    // Rule: Eligible for Check-Out (Status is CHECKED_IN)
                    boolean canCheckOut = "CHECKED_IN".equalsIgnoreCase(res.getStatus());

                    return canCheckIn || canCheckOut;
                })
                .collect(Collectors.toList());

        // Populate the table
        if (visibleReservations.isEmpty()) {
            // Only show a message if a specific search yielded no results
            if (!email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No active reservations found for this email.");
            }
        } else {
            for (Reservation res : visibleReservations) {
                tableModel.addRow(new Object[]{
                        res.getId(),
                        res.getGuestEmail(),
                        res.getStartDate(),
                        res.getEndDate(),
                        res.getTotalCost(),
                        res.getStatus()
                });
            }
        }
    }

    // Helper to match ClerkPage button styles
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