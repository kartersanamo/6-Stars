package com.sixstars.ui;

import com.sixstars.logicClasses.BedType;
import com.sixstars.logicClasses.ReservationService;
import com.sixstars.logicClasses.Room;
import com.sixstars.logicClasses.RoomService;

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
    private JButton searchButton, bookButton, logoutButton; // Added logout for navigation
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
        searchButton = new JButton("Search Rooms");
        bookButton = new JButton("Book Selected Room");
        logoutButton = new JButton("Logout");

        listModel = new DefaultListModel<>();
        resultsList = new JList<>(listModel);

        // 3. Assemble UI
        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Check-in (YYYY-MM-DD):"));
        topPanel.add(startField);
        topPanel.add(new JLabel("Check-out:"));
        topPanel.add(endField);
        topPanel.add(bedTypeBox);
        topPanel.add(searchButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultsList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(bookButton);
        bottomPanel.add(logoutButton);
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

                List<Room> allRooms = roomService.getAllRooms();
                List<Room> found = resService.filterAvailableRooms(allRooms, start, end, selectedType);

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

        // Navigation back to Welcome
        logoutButton.addActionListener(e -> cardLayout.show(pages, "welcome"));
    }
}