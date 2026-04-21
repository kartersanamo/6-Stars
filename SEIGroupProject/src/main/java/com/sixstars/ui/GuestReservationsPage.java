package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Reservation;
import com.sixstars.model.Room;
import com.sixstars.service.ReservationService;
import com.sixstars.app.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class GuestReservationsPage extends JPanel {
    private DefaultListModel<Reservation> listModel;
    private JList<Reservation> resList;
    private ReservationService resService;

    public GuestReservationsPage(JPanel pages, CardLayout cardLayout, ReservationService resService) {
        this.resService = resService;

        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.PAGE_BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // 1. Top Bar
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("My Reservations");
        title.setFont(UITheme.TITLE_FONT);

        JButton btnBack = createButton("Back", 100);
        btnBack.addActionListener(e -> cardLayout.show(pages, "home"));

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(btnBack, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // 2. The List
        listModel = new DefaultListModel<>();
        resList = new JList<>(listModel);
        resList.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resList.setFixedCellHeight(35);
        resList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resList.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // --- ADDED: Selection Listener ---
        // This detects when a user clicks a row in your list
        resList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Reservation selected = resList.getSelectedValue();
                if (selected != null) {
                    System.out.println("Guest selected reservation ID: " + selected.getId());
                }
            }
        });

        add(new JScrollPane(resList), BorderLayout.CENTER);

        // 3. Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomPanel.setOpaque(false);

        JButton btnModify = createButton("Modify Dates", 160);
        JButton btnCancel = createButton("Cancel Booking", 160);

        btnModify.addActionListener(e -> handleModify());
        btnCancel.addActionListener(e -> handleCancel());

        bottomPanel.add(btnCancel);
        bottomPanel.add(btnModify);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void refresh() {
        listModel.clear();
        if (AccountController.currentAccount != null) {
            List<Reservation> userBookings = resService.getGuestReservations(AccountController.currentAccount.getEmail());
            for (Reservation r : userBookings) {
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
        if (selected != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to cancel reservation #" + selected.getId() + "?",
                    "Confirm Cancellation", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                resService.cancelBooking(selected.getId());
                refresh();
                JOptionPane.showMessageDialog(this, "Reservation cancelled.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a reservation to cancel.");
        }
    }

    private JButton createButton(String text, int width) {
        JButton b = new JButton(text);
        b.setPreferredSize(new Dimension(width, 40));
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}