package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.BedType;
import com.sixstars.model.QualityLevel;
import com.sixstars.model.Theme;
import com.sixstars.service.ReservationService;
import com.sixstars.model.Room;
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
    private JComboBox<Theme> themeBox;
    private JComboBox<QualityLevel> qualityBox;
    private JButton searchButton, bookButton, logoutButton, backButton; // Added logout for navigation
    private JList<Room> resultsList;
    private DefaultListModel<Room> listModel;

    public MakeReservationPage(JPanel pages, CardLayout cardLayout, ReservationService resService, RoomService roomService) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.resService = resService;
        this.roomService = roomService;

        // 1. Setup Layout for this Panel
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 2. Initialize Components
        startField = new JTextField(10);
        endField = new JTextField(10);

        bedTypeBox = new JComboBox<>(BedType.values());
        themeBox = new JComboBox<>(Theme.values());
        qualityBox = new JComboBox<>(QualityLevel.values());

        searchButton = new JButton("Search Rooms");
        bookButton = new JButton("Book Selected Room");
        bookButton.setEnabled(false);
        logoutButton = new JButton("Logout");
        backButton = new JButton("Back");

        listModel = new DefaultListModel<>();
        resultsList = new JList<>(listModel);
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 3. Assemble UI
        JPanel topPanel = new JPanel(new GridLayout(3,4,10,10));
        topPanel.add(new JLabel("Check-in (YYYY-MM-DD):"));
        topPanel.add(startField);
        topPanel.add(new JLabel("Check-out:"));
        topPanel.add(endField);
        topPanel.add(new JLabel("Bed Type:"));
        topPanel.add(bedTypeBox);
        topPanel.add(new JLabel("Theme:"));
        topPanel.add(themeBox);
        topPanel.add(new JLabel("Quality:"));
        topPanel.add(qualityBox);
        topPanel.add(new JLabel(""));
        topPanel.add(searchButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultsList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(bookButton);
        bottomPanel.add(logoutButton);
        bottomPanel.add(backButton);
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
                Theme selTheme = (Theme) themeBox.getSelectedItem();
                QualityLevel selQual = (QualityLevel) qualityBox.getSelectedItem();

                List<Room> allRooms = roomService.getAllRooms();
                List<Room> found = resService.filterAvailableRooms(allRooms, start, end, selectedType, selTheme, selQual);

                listModel.clear();
                if (found.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No rooms found for these criteria.");
                } else {
                    found.forEach(listModel::addElement);
                    bookButton.setEnabled(true);
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
            bookButton.setEnabled(false);
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

        // Initial display all Rooms
        listModel.clear();
        roomService.getAllRooms().forEach(listModel::addElement);
    }
}