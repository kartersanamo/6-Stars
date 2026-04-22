package com.sixstars.ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
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

        JButton btnBack = new JButton("Back to Dashboard");
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

        JButton btnSearch = new JButton("Search");
        btnSearch.addActionListener(e -> refreshTable());
        searchPanel.add(btnSearch);

        // --- Table Setup ---
        String[] columns = {"ID", "Email", "Start Date", "End Date", "Total Cost"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        reservationTable = new JTable(tableModel);
        reservationTable.setRowHeight(30);
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

        JButton btnCheckIn = new JButton("Confirm Check-In");
        btnCheckIn.setPreferredSize(new Dimension(200, 40));
        btnCheckIn.setBackground(UITheme.ACCENT_GOLD);
        btnCheckIn.setForeground(Color.WHITE);
        btnCheckIn.setOpaque(true);
        btnCheckIn.setBorderPainted(false);

        btnCheckIn.addActionListener(e -> {
            int selectedRow = reservationTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a reservation to check in.");
                return;
            }

            int resId = (int) tableModel.getValueAt(selectedRow, 0);
            // Here you would typically update a status in the database
            JOptionPane.showMessageDialog(this, "Guest for Reservation #" + resId + " checked in successfully!");
        });

        footer.add(btnCheckIn);

        // --- Add everything to main panel ---
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(tableWrapper, BorderLayout.CENTER);

        add(header, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String email = emailField.getText().trim();

        List<Reservation> list = reservationService.getGuestReservations(email);

        if (list.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No reservations found for this email.");
            return;
        }

        for (Reservation res : list) {
            tableModel.addRow(new Object[]{
                    res.getId(),
                    res.getGuestEmail(),
                    res.getStartDate(),
                    res.getEndDate(),
                    "$" + res.getTotalCost()
            });
        }
    }
}