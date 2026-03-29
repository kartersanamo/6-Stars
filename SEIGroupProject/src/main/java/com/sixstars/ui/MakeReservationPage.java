package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.BedType;
import com.sixstars.model.QualityLevel;
import com.sixstars.model.Theme;
import com.sixstars.service.ReservationService;
import com.sixstars.model.Room;
import com.sixstars.service.RoomService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class MakeReservationPage extends JPanel {
    private JPanel pages;
    private CardLayout cardLayout;
    private ReservationService resService;
    private RoomService roomService;

    private JTextField startField, endField, roomNumberField;
    private JComboBox<Object> bedTypeBox;
    private JComboBox<Object> themeBox;
    private JComboBox<Object> qualityBox;
    private JButton bookButton, logoutButton, backButton;
    private JList<Room> resultsList;
    private DefaultListModel<Room> listModel;

    public MakeReservationPage(JPanel pages, CardLayout cardLayout, ReservationService resService, RoomService roomService) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.resService = resService;
        this.roomService = roomService;

        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        startField = new JTextField(10);
        endField = new JTextField(10);
        roomNumberField = new JTextField(10);

        bedTypeBox = new JComboBox<>();
        bedTypeBox.addItem("Any");
        for (BedType type : BedType.values()) {
            bedTypeBox.addItem(type);
        }

        themeBox = new JComboBox<>();
        themeBox.addItem("Any");
        for (Theme theme : Theme.values()) {
            themeBox.addItem(theme);
        }

        qualityBox = new JComboBox<>();
        qualityBox.addItem("Any");
        for (QualityLevel quality : QualityLevel.values()) {
            qualityBox.addItem(quality);
        }

        bookButton = new JButton("Book Selected Room");
        bookButton.setEnabled(false);
        logoutButton = new JButton("Logout");
        backButton = new JButton("Back");

        listModel = new DefaultListModel<>();
        resultsList = new JList<>(listModel);
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel topPanel = new JPanel(new GridLayout(3, 4, 10, 10));
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
        topPanel.add(new JLabel("Room Number:"));
        topPanel.add(roomNumberField);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultsList), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(bookButton);
        bottomPanel.add(logoutButton);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        bedTypeBox.addActionListener(e -> updateRoomList());
        themeBox.addActionListener(e -> updateRoomList());
        qualityBox.addActionListener(e -> updateRoomList());

        roomNumberField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateRoomList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateRoomList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateRoomList();
            }
        });

        startField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateBookButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateBookButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateBookButtonState();
            }
        });

        endField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateBookButtonState();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateBookButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateBookButtonState();
            }
        });

        resultsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateBookButtonState();
            }
        });

        bookButton.addActionListener(e -> {
            Room selectedRoom = resultsList.getSelectedValue();
            if (selectedRoom == null) {
                JOptionPane.showMessageDialog(this, "Please select a room first!");
                return;
            }

            if (!datesAreValid()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid check-in and check-out dates.",
                        "Date Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            LocalDate start = LocalDate.parse(startField.getText().trim());
            LocalDate end = LocalDate.parse(endField.getText().trim());

            if (!resService.isRoomAvailable(selectedRoom, start, end)) {
                JOptionPane.showMessageDialog(this,
                        "That room is not available for the selected dates.",
                        "Unavailable Room",
                        JOptionPane.ERROR_MESSAGE);
                updateBookButtonState();
                return;
            }

            resService.makeReservation(start, end, List.of(selectedRoom));
            JOptionPane.showMessageDialog(this, "Reservation Successful!");
            updateRoomList();
            bookButton.setEnabled(false);
        });

        backButton.addActionListener(e -> {
            if (AccountController.currentAccount != null) {
                cardLayout.show(pages, "menu page");
            } else {
                cardLayout.show(pages, "welcome");
            }
        });

        logoutButton.addActionListener(e -> cardLayout.show(pages, "welcome"));

        listModel.clear();
        roomService.getAllRooms().forEach(listModel::addElement);
    }

    private void updateRoomList() {
        listModel.clear();

        Object selectedType = bedTypeBox.getSelectedItem();
        Object selectedTheme = themeBox.getSelectedItem();
        Object selectedQuality = qualityBox.getSelectedItem();
        String roomNumberText = roomNumberField.getText().trim();

        List<Room> allRooms = roomService.getAllRooms();

        for (Room room : allRooms) {
            boolean matches = true;

            if (selectedType instanceof BedType && room.getBedType() != selectedType) {
                matches = false;
            }
            if (selectedTheme instanceof Theme && room.getTheme() != selectedTheme) {
                matches = false;
            }
            if (selectedQuality instanceof QualityLevel && room.getQualityLevel() != selectedQuality) {
                matches = false;
            }
            if (!roomNumberText.isEmpty()) {
                try {
                    int roomNumber = Integer.parseInt(roomNumberText);
                    if (room.getRoomNumber() != roomNumber) {
                        matches = false;
                    }
                } catch (NumberFormatException e) {
                    matches = false;
                }
            }

            if (matches) {
                listModel.addElement(room);
            }
        }

        updateBookButtonState();
    }

    private boolean datesAreValid() {
        try {
            String startText = startField.getText().trim();
            String endText = endField.getText().trim();

            if (startText.isEmpty() || endText.isEmpty()) {
                return false;
            }

            LocalDate start = LocalDate.parse(startText);
            LocalDate end = LocalDate.parse(endText);

            if (!start.isBefore(end)) {
                return false;
            }

            if (start.isBefore(LocalDate.now())) {
                return false;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateBookButtonState() {
        Room selectedRoom = resultsList.getSelectedValue();

        if (selectedRoom == null || !datesAreValid()) {
            bookButton.setEnabled(false);
            return;
        }

        LocalDate start = LocalDate.parse(startField.getText().trim());
        LocalDate end = LocalDate.parse(endField.getText().trim());

        boolean available = resService.isRoomAvailable(selectedRoom, start, end);
        bookButton.setEnabled(available);
    }
}