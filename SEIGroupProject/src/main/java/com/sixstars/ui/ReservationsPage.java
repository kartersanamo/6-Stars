package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Reservation;
import com.sixstars.model.Role;
import com.sixstars.service.ReservationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ReservationsPage extends JPanel {
    private DefaultListModel<Reservation> listModel;
    private JList<Reservation> resList;
    private ReservationService resService;
    private JLabel titleLabel;

    public ReservationsPage(JPanel pages, CardLayout cardLayout, ReservationService resService) {
        this.resService = resService;

        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.PAGE_BACKGROUND);
        setBorder(new EmptyBorder(40, 40, 40, 40));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        titleLabel = new JLabel("Reservations");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_DARK);

        JButton btnBack = new JButton("Back");
        styleGoldButton(btnBack);
        btnBack.addActionListener(e -> {
            var acc = AccountController.currentAccount;
            if (acc != null && acc.getRole() == Role.CLERK) {
                cardLayout.show(pages, "clerk page");
            } else {
                cardLayout.show(pages, "home");
            }
        });

        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(btnBack, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        resList = new JList<>(listModel);
        resList.setFont(UITheme.INPUT_FONT);
        resList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resList.setFixedCellHeight(50);
        resList.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(resList);
        scrollPane.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomPanel.setOpaque(false);

        JButton btnModify = new JButton("Modify Dates");
        JButton btnCancel = new JButton("Cancel Booking");

        styleGoldButton(btnModify);
        styleGoldButton(btnCancel);

        btnModify.addActionListener(e -> handleModify());
        btnCancel.addActionListener(e -> handleCancel());

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnModify);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refresh() {
        listModel.clear();
        var current = AccountController.currentAccount;

        if (current != null) {
            List<Reservation> reservations;

            if (current.getRole() == Role.CLERK) {
                titleLabel.setText("All Hotel Reservations");
                reservations = resService.getAllReservations().stream()
                        .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                        .toList();
            } else {
                titleLabel.setText("My Reservations");
                reservations = resService.getGuestReservations(current.getEmail());
            }

            for (Reservation r : reservations) {
                listModel.addElement(r);
            }
        }
        revalidate();
        repaint();
    }

    private void handleModify() {
        Reservation selected = resList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to modify.");
            return;
        }

        String newStartStr = JOptionPane.showInputDialog(
                this,
                "New Start Date (YYYY-MM-DD):",
                selected.getStartDate()
        );
        if (newStartStr == null) {
            return;
        }

        String newEndStr = JOptionPane.showInputDialog(
                this,
                "New End Date (YYYY-MM-DD):",
                selected.getEndDate()
        );
        if (newEndStr == null) {
            return;
        }

        try {
            LocalDate newStart = LocalDate.parse(newStartStr);
            LocalDate newEnd = LocalDate.parse(newEndStr);

            if (!newStart.isBefore(newEnd)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Check-out date must be after check-in date.",
                        "Invalid Date Range",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            if (newStart.isBefore(LocalDate.now())) {
                JOptionPane.showMessageDialog(
                        this,
                        "Check-in date cannot be in the past.",
                        "Invalid Date",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            resService.updateReservation(selected.getId(), newStart, newEnd, selected.getRooms());

            refresh();
            JOptionPane.showMessageDialog(this, "Reservation dates updated successfully!");
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Unable to Update Reservation",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid date format. Please use YYYY-MM-DD.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void handleCancel() {
        Reservation selected = resList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a reservation to cancel.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Confirm cancellation for Reservation #" + selected.getId() + "?",
                "Cancellation Request", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String resultMessage = resService.cancelBooking(selected.getId());
                refresh();
                JOptionPane.showMessageDialog(this, resultMessage);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Cancellation Failed: " + ex.getMessage());
            }
        }
    }

    private void styleGoldButton(JButton button) {
        button.setFont(UITheme.BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
}
