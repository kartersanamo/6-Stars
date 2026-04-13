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

    private JLabel selectedRoomLabel;
    private JLabel selectedStartLabel;
    private JLabel selectedEndLabel;

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

        bookButton = new JButton("Reserve Selected Room");
        bookButton.setEnabled(false);
        logoutButton = new JButton("Logout");
        backButton = new JButton("Back");

        listModel = new DefaultListModel<>();
        resultsList = new JList<>(listModel);
        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        selectedRoomLabel = new JLabel("Selected Room: None");
        selectedStartLabel = new JLabel("Check-in: Not entered");
        selectedEndLabel = new JLabel("Check-out: Not entered");

        JPanel topPanel = new JPanel(new GridLayout(3, 4, 10, 10));
        topPanel.add(new JLabel("Check-in (YYYY-MM-DD):"));
        topPanel.add(startField);
        topPanel.add(new JLabel("Check-out (YYYY-MM-DD):"));
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

        JScrollPane scrollPane = new JScrollPane(resultsList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Available Rooms"));
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JPanel summaryPanel = new JPanel(new GridLayout(4, 1));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Reservation Summary"));
        summaryPanel.add(selectedRoomLabel);
        summaryPanel.add(selectedStartLabel);
        summaryPanel.add(selectedEndLabel);
        summaryPanel.add(new JLabel("You can only reserve when both dates are valid."));

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(bookButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(backButton);

        bottomPanel.add(summaryPanel);
        bottomPanel.add(buttonPanel);

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
                updateSummaryLabels();
                updateRoomList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSummaryLabels();
                updateRoomList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSummaryLabels();
                updateRoomList();
            }
        });

        endField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateSummaryLabels();
                updateRoomList();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateSummaryLabels();
                updateRoomList();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateSummaryLabels();
                updateRoomList();
            }
        });

        resultsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSummaryLabels();
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
                updateRoomList();
                return;
            }

            resService.makeReservation(start, end, List.of(selectedRoom));
            JOptionPane.showMessageDialog(this, "Reservation Successful!");

            resultsList.clearSelection();
            updateRoomList();
            updateSummaryLabels();
            bookButton.setEnabled(false);
        });

        backButton.addActionListener(e -> {
            if (AccountController.currentAccount != null) {
                cardLayout.show(pages, "menu page");
            } else {
                cardLayout.show(pages, "home");
            }
        });

        logoutButton.addActionListener(e -> cardLayout.show(pages, "welcome"));

        updateRoomList();
        updateSummaryLabels();
    }

    private void updateRoomList() {
        listModel.clear();

        Object selectedType = bedTypeBox.getSelectedItem();
        Object selectedTheme = themeBox.getSelectedItem();
        Object selectedQuality = qualityBox.getSelectedItem();
        String roomNumberText = roomNumberField.getText().trim();

        List<Room> allRooms = roomService.getAllRooms();

        boolean validDates = datesAreValid();
        LocalDate start = null;
        LocalDate end = null;

        if (validDates) {
            start = LocalDate.parse(startField.getText().trim());
            end = LocalDate.parse(endField.getText().trim());
        }

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

            if (matches && validDates && !resService.isRoomAvailable(room, start, end)) {
                matches = false;
            }

            if (matches) {
                listModel.addElement(room);
            }
        }

        if (!listModel.contains(resultsList.getSelectedValue())) {
            resultsList.clearSelection();
        }

        updateSummaryLabels();
        updateBookButtonState();
    }

    private void updateSummaryLabels() {
        Room selectedRoom = resultsList.getSelectedValue();

        if (selectedRoom == null) {
            selectedRoomLabel.setText("Selected Room: None");
        } else {
            selectedRoomLabel.setText("Selected Room: Room " + selectedRoom.getRoomNumber());
        }

        String startText = startField.getText().trim();
        String endText = endField.getText().trim();

        if (startText.isEmpty()) {
            selectedStartLabel.setText("Check-in: Not entered");
        } else {
            selectedStartLabel.setText("Check-in: " + startText);
        }

        if (endText.isEmpty()) {
            selectedEndLabel.setText("Check-out: Not entered");
        } else {
            selectedEndLabel.setText("Check-out: " + endText);
        }
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