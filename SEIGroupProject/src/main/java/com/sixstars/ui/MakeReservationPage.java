package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.*;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class MakeReservationPage extends JPanel {
    private JPanel pages;
    private CardLayout cardLayout;
    private ReservationService resService;
    private RoomService roomService;

    private JTextField startField, endField;
    private JComboBox<BedType> bedTypeBox;
    private JComboBox<Theme> themeBox;         // Added
    private JComboBox<QualityLevel> qualityBox; // Added
    private JButton searchButton, bookButton, logoutButton, backButton, addRoomButton; // Added logout for navigation
    private JList<Room> resultsList;
    private DefaultListModel<Room> listModel;

    public MakeReservationPage(JPanel pages, CardLayout cardLayout, ReservationService resService, RoomService roomService) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.resService = resService;
        this.roomService = roomService;

        // 1. Setup Layout for this Panel
        setLayout(new BorderLayout(10, 10));

        // 2. Initialize Components
        startField = new JTextField(10);
        endField = new JTextField(10);
        bedTypeBox = new JComboBox<>(BedType.values());
        themeBox = new JComboBox<>(Theme.values());
        qualityBox = new JComboBox<>(QualityLevel.values());

        searchButton = new JButton("Search Rooms");
        bookButton = new JButton("Book Selected Room");
        logoutButton = new JButton("Logout");
        backButton = new JButton("Back");
        addRoomButton = new JButton("Add Room");
        addRoomButton.setVisible(false); // Hidden by default

        listModel = new DefaultListModel<>();
        resultsList = new JList<>(listModel);

        // 3. Assemble UI
        JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel row1 = new JPanel();
        row1.add(new JLabel("Check-in (YYYY-MM-DD):"));
        row1.add(startField);
        row1.add(new JLabel("Check-out:"));
        row1.add(endField);

        JPanel row2 = new JPanel();
        row2.add(new JLabel("Bed Type:")); row2.add(bedTypeBox);
        row2.add(new JLabel("Theme:")); row2.add(themeBox);
        row2.add(new JLabel("Quality:")); row2.add(qualityBox);
        row2.add(searchButton);

        topPanel.add(row1);
        topPanel.add(row2);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultsList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(bookButton);
        bottomPanel.add(logoutButton);
        bottomPanel.add(backButton);
        bottomPanel.add(addRoomButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 4. Action Listeners
        searchButton.addActionListener(e -> {
            try {
                LocalDate start = LocalDate.parse(startField.getText());
                LocalDate end = LocalDate.parse(endField.getText());

                if (!start.isBefore(end)) {
                    JOptionPane.showMessageDialog(this,
                            "Check-in date must be BEFORE the check-out date.",
                            "Date Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // check if the date is in the past
                if (start.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "You cannot book a date in the past.");
                    return;
                }

                BedType selectedType = (BedType) bedTypeBox.getSelectedItem();
                Theme theme = (Theme) themeBox.getSelectedItem();
                QualityLevel quality = (QualityLevel) qualityBox.getSelectedItem();
                List<Room> allRooms = roomService.getAllRooms();
                List<Room> found = resService.filterAvailableRooms(allRooms, start, end, selectedType, theme, quality);

                listModel.clear();
                if (found.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No rooms found for these criteria.");
                } else {
                    found.forEach(listModel::addElement);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Check date format! Use 2026-03-18");
            }
        });

        bookButton.addActionListener(e -> {
            Room selectedRoom = resultsList.getSelectedValue();
            if (selectedRoom == null) {
                JOptionPane.showMessageDialog(this, "Please select a room first!");
                return;
            }

            LocalDate start = LocalDate.parse(startField.getText());
            LocalDate end = LocalDate.parse(endField.getText());

            resService.makeReservation(start, end, List.of(selectedRoom));
            JOptionPane.showMessageDialog(this, "Reservation Successful!");
            listModel.clear(); // Clear results after booking
        });

        backButton.addActionListener(e -> {
            if (AccountController.currentAccount != null) {
                // User is logged in, go to the Menu Page we just made
                cardLayout.show(pages, "menu page");
            } else {
                // User is not logged in, go back to the Welcome screen
                cardLayout.show(pages, "welcome");
            }
        });

        // Navigation back to Welcome
        logoutButton.addActionListener(e -> cardLayout.show(pages, "welcome"));

        addRoomButton.addActionListener(e -> {
            Frame parent = (Frame) SwingUtilities.getWindowAncestor(this);
            AddRoomDialog dialog = new AddRoomDialog(parent, roomService);
            dialog.setVisible(true);

            if (dialog.isSucceeded()) {
                listModel.clear();
                for (Room r : roomService.getAllRooms()) {
                    listModel.addElement(r);
                }
                JOptionPane.showMessageDialog(this, "Room added successfully and list updated!");
            }
        });
    }

    public void updatePermissions() {
        Account current = com.sixstars.controller.AccountController.currentAccount;

        // Using == with Enums is the standard and safest way in Java
        if (current != null && current.getRole() == Role.CLERK) {
            addRoomButton.setVisible(true);
        } else {
            addRoomButton.setVisible(false);
        }

        this.revalidate();
        this.repaint();
    }
}