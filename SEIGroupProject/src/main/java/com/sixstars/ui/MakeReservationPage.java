package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.BedType;
import com.sixstars.model.QualityLevel;
import com.sixstars.model.Reservation;
import com.sixstars.model.Role;
import com.sixstars.model.Room;
import com.sixstars.model.Theme;
import com.sixstars.service.PricingSettingsService;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;
import com.toedter.calendar.JDateChooser;

public class MakeReservationPage extends JPanel {
    private static final String ROOM_IMAGE_PATH = "assets/6Stars-Room.jpg";

    private final JPanel pages;
    private final CardLayout cardLayout;
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final PricingSettingsService pricingSettingsService;

    private final JDateChooser checkInChooser;
    private final JDateChooser checkOutChooser;
    private JTextField roomNumberField;
    private final JComboBox<Object> bedTypeBox;
    private final JComboBox<Object> themeBox;
    private final JComboBox<Object> qualityBox;
    private final JComboBox<Object> smokingBox;
    private final JCheckBox onlyAvailableCheck;
    private final JLabel resultsInfoLabel;
    private final JPanel roomCardsContainer;

    public MakeReservationPage(JPanel pages, CardLayout cardLayout, ReservationService reservationService, RoomService roomService) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.reservationService = reservationService;
        this.roomService = roomService;
        this.pricingSettingsService = new PricingSettingsService();

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        Date today = todayAtMidnight();
        Date tomorrow = dateFromLocalDate(LocalDate.now().plusDays(1));
        checkInChooser = createDateChooser(today);
        checkOutChooser = createDateChooser(tomorrow);
        checkInChooser.setMinSelectableDate(today);
        checkOutChooser.setMinSelectableDate(tomorrow);
        roomNumberField = createTextField("Any");
        bedTypeBox = buildEnumFilterBox(BedType.values());
        themeBox = buildEnumFilterBox(Theme.values());
        qualityBox = buildEnumFilterBox(QualityLevel.values());
        smokingBox = new JComboBox<>(new Object[]{"Any", "Smoking", "Non-smoking"});
        styleFilterCombo(smokingBox);
        onlyAvailableCheck = new JCheckBox("Only show currently available");
        resultsInfoLabel = new JLabel("0 rooms");
        roomCardsContainer = new JPanel();

        add(buildTopSection(), BorderLayout.NORTH);
        add(buildRoomListingsSection(), BorderLayout.CENTER);
        add(buildBottomActionBar(), BorderLayout.SOUTH);

        attachFilterListeners();
        refreshListings();
    }

    private JPanel buildTopSection() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.PAGE_BACKGROUND);

        Main.headerBar.refreshInfo();
        top.add(buildSearchSummaryPanel(), BorderLayout.CENTER);
        return top;
    }

    private JPanel buildSearchSummaryPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(UITheme.CARD_BACKGROUND);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JPanel fields = new JPanel(new GridLayout(1, 4, 10, 0));
        fields.setOpaque(false);
        fields.add(createFieldCard("Check In", checkInChooser));
        fields.add(createFieldCard("Check Out", checkOutChooser));
        fields.add(createFieldCard("Room Number", roomNumberField));

        JPanel filtersRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filtersRow.setOpaque(false);
        filtersRow.add(createFilterLabel("Bed"));
        filtersRow.add(bedTypeBox);
        filtersRow.add(createFilterLabel("Theme"));
        filtersRow.add(themeBox);
        filtersRow.add(createFilterLabel("Quality"));
        filtersRow.add(qualityBox);
        filtersRow.add(createFilterLabel("Smoking"));
        filtersRow.add(smokingBox);
        filtersRow.add(onlyAvailableCheck);

        JButton clearButton = createSecondaryButton("Clear Filters");
        clearButton.addActionListener(e -> {
            checkInChooser.setDate(todayAtMidnight());
            setCheckOutMinimum();
            checkOutChooser.setDate(checkOutChooser.getMinSelectableDate());
            roomNumberField.setText("");
            bedTypeBox.setSelectedIndex(0);
            themeBox.setSelectedIndex(0);
            qualityBox.setSelectedIndex(0);
            smokingBox.setSelectedIndex(0);
            onlyAvailableCheck.setSelected(false);
            refreshListings();
        });
        filtersRow.add(clearButton);

        JLabel title = new JLabel("Browse Every Room at 6 Stars");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Hotel-style room listings with live filtering and reservation actions");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(Box.createRigidArea(new Dimension(0, 4)));
        titles.add(subtitle);

        searchPanel.add(titles, BorderLayout.NORTH);
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.add(Box.createRigidArea(new Dimension(0, 14)));
        body.add(fields);
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(filtersRow);
        searchPanel.add(body, BorderLayout.SOUTH);
        return searchPanel;
    }

    private JPanel buildRoomListingsSection() {
        JPanel section = new JPanel(new BorderLayout());
        section.setBackground(UITheme.PAGE_BACKGROUND);
        section.setBorder(new EmptyBorder(26, 36, 22, 36));

        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setOpaque(false);
        JLabel heading = new JLabel("All Available Rooms");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(UITheme.TEXT_DARK);

        resultsInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultsInfoLabel.setForeground(UITheme.TEXT_MEDIUM);
        resultsInfoLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        infoBar.add(heading, BorderLayout.WEST);
        infoBar.add(resultsInfoLabel, BorderLayout.EAST);

        roomCardsContainer.setLayout(new BoxLayout(roomCardsContainer, BoxLayout.Y_AXIS));
        roomCardsContainer.setOpaque(false);
        roomCardsContainer.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(roomCardsContainer);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);

        section.add(infoBar, BorderLayout.NORTH);
        section.add(Box.createRigidArea(new Dimension(0, 12)), BorderLayout.CENTER);
        section.add(scrollPane, BorderLayout.SOUTH);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(infoBar, BorderLayout.NORTH);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildBottomActionBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.CARD_BACKGROUND);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel helperText = new JLabel("Tip: Choose dates and filters above to narrow your results.");
        helperText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        helperText.setForeground(UITheme.TEXT_MEDIUM);

        JButton backButton = createSecondaryButton("Back");
        backButton.addActionListener(e -> {
            Main.headerBar.refreshInfo();
            Account current = AccountController.currentAccount;
            if (current != null && current.getRole() == Role.CLERK) {
                cardLayout.show(pages, "clerk page");
            } else {
                cardLayout.show(pages, "home");
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(backButton);

        footer.add(helperText, BorderLayout.CENTER);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private JPanel createFieldCard(String label, JComponent field) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(248, 248, 248));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel title = new JLabel(label);
        title.setFont(new Font("SansSerif", Font.BOLD, 12));
        title.setForeground(UITheme.TEXT_MEDIUM);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(field);
        return card;
    }

    private void attachFilterListeners() {
        DocumentListener documentListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshListings();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshListings();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshListings();
            }
        };

        checkInChooser.getDateEditor().addPropertyChangeListener("date", e -> {
            setCheckOutMinimum();
            refreshListings();
        });
        checkOutChooser.getDateEditor().addPropertyChangeListener("date", e -> refreshListings());
        roomNumberField.getDocument().addDocumentListener(documentListener);

        bedTypeBox.addActionListener(e -> refreshListings());
        themeBox.addActionListener(e -> refreshListings());
        qualityBox.addActionListener(e -> refreshListings());
        smokingBox.addActionListener(e -> refreshListings());
        onlyAvailableCheck.addActionListener(e -> refreshListings());
    }

    private void refreshListings() {
        roomCardsContainer.removeAll();

        List<Room> allRooms = roomService.getAllRooms();
        LocalDate startDate = toLocalDate(checkInChooser.getDate());
        LocalDate endDate = toLocalDate(checkOutChooser.getDate());
        boolean validDateRange = startDate != null && endDate != null && startDate.isBefore(endDate);

        int shownCount = 0;
        for (Room room : allRooms) {
            if (!matchesRoomFilters(room)) {
                continue;
            }

            boolean isAvailable = !validDateRange || reservationService.isRoomAvailable(room, startDate, endDate);
            if (onlyAvailableCheck.isSelected() && !isAvailable) {
                continue;
            }

            roomCardsContainer.add(createRoomCard(room, isAvailable, validDateRange));
            roomCardsContainer.add(Box.createRigidArea(new Dimension(0, 12)));
            shownCount++;
        }

        if (shownCount == 0) {
            roomCardsContainer.add(createEmptyStateCard());
            resultsInfoLabel.setText("0 results");
        } else {
            resultsInfoLabel.setText(shownCount + " result" + (shownCount == 1 ? "" : "s"));
        }

        roomCardsContainer.revalidate();
        roomCardsContainer.repaint();
    }

    public void refreshPage() {
        refreshListings();
    }

    private JPanel createRoomCard(Room room, boolean isAvailable, boolean hasValidDateRange) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        ImagePanel imagePanel = new ImagePanel(getRoomImageForTheme(room.getTheme()));
        imagePanel.setPreferredSize(new Dimension(340, 240));
        imagePanel.setLayout(new BorderLayout());

        JLabel imageRoomLabel = new JLabel("Room " + room.getRoomNumber());
        imageRoomLabel.setFont(new Font("Serif", Font.BOLD, 30));
        imageRoomLabel.setForeground(Color.WHITE);
        imageRoomLabel.setBorder(new EmptyBorder(14, 16, 12, 16));
        imagePanel.add(imageRoomLabel, BorderLayout.SOUTH);

        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setBackground(Color.WHITE);
        details.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Room " + room.getRoomNumber() + " - " + room.getBedType());
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel(
                "Theme: " + room.getTheme() + "  |  Quality: " + room.getQualityLevel() + "  |  "
                        + (room.isSmoking() ? "Smoking" : "Non-smoking")
        );
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        JLabel priceLabel = new JLabel("$" + room.getPricePerNight() + " per night (Standard)");
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        priceLabel.setForeground(new Color(176, 132, 38));

        JLabel maxRateLabel = new JLabel("Quality Max Daily Rate: $" + room.getQualityLevel().getMaxDailyRate());
        maxRateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        maxRateLabel.setForeground(UITheme.TEXT_MEDIUM);

        String totalText = "Select dates to see total stay cost";
        if (hasValidDateRange) {
            LocalDate startDate = toLocalDate(checkInChooser.getDate());
            LocalDate endDate = toLocalDate(checkOutChooser.getDate());
            int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            double discountRate = pricingSettingsService.getGlobalDiscountRate();
            int estimatedNightly = (int) Math.round(room.getPricePerNight() * (1.0 - discountRate));
            estimatedNightly = Math.min(estimatedNightly, room.getQualityLevel().getMaxDailyRate());
            int estimatedTotal = estimatedNightly * nights;
            totalText = "Estimated total: $" + estimatedTotal + " for " + nights
                    + " night" + (nights == 1 ? "" : "s");
        }

        JLabel totalLabel = new JLabel(totalText);
        totalLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        totalLabel.setForeground(UITheme.TEXT_MEDIUM);

        JLabel availabilityLabel = new JLabel(
                hasValidDateRange
                        ? (isAvailable ? "Available for selected dates" : "Unavailable for selected dates")
                        : "Enter check-in and check-out dates to confirm availability"
        );
        availabilityLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        availabilityLabel.setForeground(isAvailable ? new Color(44, 122, 72) : new Color(161, 62, 47));

        JButton reserveButton = createReserveButton();
        styleReserveButtonState(reserveButton, hasValidDateRange, isAvailable);
        reserveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        reserveButton.addActionListener(e -> reserveRoom(room));

        details.add(title);
        details.add(Box.createRigidArea(new Dimension(0, 8)));
        details.add(subtitle);
        details.add(Box.createRigidArea(new Dimension(0, 10)));
        details.add(priceLabel);
        details.add(Box.createRigidArea(new Dimension(0, 4)));
        details.add(maxRateLabel);
        details.add(Box.createRigidArea(new Dimension(0, 6)));
        details.add(totalLabel);
        details.add(Box.createRigidArea(new Dimension(0, 14)));
        details.add(availabilityLabel);
        details.add(Box.createVerticalGlue());
        details.add(reserveButton);

        card.add(imagePanel, BorderLayout.WEST);
        card.add(details, BorderLayout.CENTER);
        return card;
    }

    private JPanel createEmptyStateCard() {
        JPanel empty = new JPanel(new BorderLayout());
        empty.setBackground(UITheme.CARD_BACKGROUND);
        empty.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(26, 20, 26, 20)
        ));
        empty.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel text = new JLabel("No rooms match the active filters.");
        text.setHorizontalAlignment(SwingConstants.CENTER);
        text.setFont(new Font("SansSerif", Font.PLAIN, 16));
        text.setForeground(UITheme.TEXT_MEDIUM);
        empty.add(text, BorderLayout.CENTER);
        return empty;
    }

    private boolean matchesRoomFilters(Room room) {
        Object selectedBedType = bedTypeBox.getSelectedItem();
        Object selectedTheme = themeBox.getSelectedItem();
        Object selectedQuality = qualityBox.getSelectedItem();
        Object selectedSmoking = smokingBox.getSelectedItem();
        String roomNumberText = roomNumberField.getText().trim();

        if (selectedBedType instanceof BedType && room.getBedType() != selectedBedType) {
            return false;
        }
        if (selectedTheme instanceof Theme && room.getTheme() != selectedTheme) {
            return false;
        }
        if (selectedQuality instanceof QualityLevel && room.getQualityLevel() != selectedQuality) {
            return false;
        }

        if ("Smoking".equals(selectedSmoking) && !room.isSmoking()) {
            return false;
        }
        if ("Non-smoking".equals(selectedSmoking) && room.isSmoking()) {
            return false;
        }

        if (!roomNumberText.isBlank()){
            return (String.valueOf(room.getRoomNumber()).startsWith(roomNumberText));
        }

        return true;
    }

    private void reserveRoom(Room room) {
        LocalDate startDate = toLocalDate(checkInChooser.getDate());
        LocalDate endDate = toLocalDate(checkOutChooser.getDate());

        if (startDate == null || endDate == null || !startDate.isBefore(endDate)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter valid check-in and check-out dates before reserving.",
                    "Invalid Date Range",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (startDate.isBefore(LocalDate.now())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Check-in date cannot be in the past.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (!reservationService.isRoomAvailable(room, startDate, endDate)) {
            JOptionPane.showMessageDialog(
                    this,
                    "This room was just booked for the selected dates. Please choose another room.",
                    "Room Unavailable",
                    JOptionPane.ERROR_MESSAGE
            );
            refreshListings();
            return;
        }

        Account currentAccount = AccountController.currentAccount;
        String targetEmail;

        if (currentAccount == null) {
            Main.setPendingReservation(room, startDate, endDate);
            JOptionPane.showMessageDialog(this, "Please create a Guest account to complete this reservation.");
            cardLayout.show(pages, "create account");
            return;
        }

        if (currentAccount.getRole() == Role.CLERK) {
            // Show a popup to ask for the email
            String input = JOptionPane.showInputDialog(
                    this,
                    "Enter Guest Email for Room " + room.getRoomNumber() + ":",
                    "Guest Information Required",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (input == null || input.trim().isEmpty()) return; // User cancelled
            targetEmail = input.trim().toLowerCase();

            // Validate the email against the database
            if (!reservationService.isValidGuest(targetEmail)) {
                JOptionPane.showMessageDialog(this, "No Guest account found for: " + targetEmail);
                return;
            }
        } else if (currentAccount.getRole() == Role.GUEST) {
            // Normal Guest uses their own account email
            targetEmail = currentAccount.getEmail();
        } else {
            // This handles any other roles (like Admin) that shouldn't be booking
            JOptionPane.showMessageDialog(this, "Your account type cannot reserve rooms.");
            return;
        }

        Reservation reservation = reservationService.makeReservation(targetEmail, startDate, endDate, List.of(room));
        int nights = reservation.getNights();
        int actualNightly = reservation.getNightlyRate();
        int total = reservation.getTotalCost();

        JOptionPane.showMessageDialog(
                this,
                "Reservation successful for Room " + room.getRoomNumber()
                        + ".\nRate Plan: " + reservation.getRatePlan().getDisplayName()
                        + ".\nActual Nightly Rate: $" + actualNightly
                        + " (Quality Max: $" + room.getQualityLevel().getMaxDailyRate() + ")"
                        + ".\nTotal: $" + total + " for " + nights
                        + " night" + (nights == 1 ? "" : "s") + ".",
                "Booking Confirmed",
                JOptionPane.INFORMATION_MESSAGE
        );
        refreshListings();
    }

    public boolean completePendingReservationIfAny() {
        Main.PendingReservation pendingReservation = Main.consumePendingReservation();
        if (pendingReservation == null) {
            return false;
        }

        Room room = pendingReservation.getRoom();
        LocalDate startDate = pendingReservation.getStartDate();
        LocalDate endDate = pendingReservation.getEndDate();

        if (!reservationService.isRoomAvailable(room, startDate, endDate)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Your selected room is no longer available. Please choose another room.",
                    "Room Unavailable",
                    JOptionPane.ERROR_MESSAGE
            );
            refreshListings();
            cardLayout.show(pages, "make reservation");
            return true;
        }

        Account currentAccount = AccountController.currentAccount;
        Reservation reservation = reservationService.makeReservation(currentAccount.getEmail(), startDate, endDate, List.of(room));
        int nights = reservation.getNights();
        int actualNightly = reservation.getNightlyRate();
        int total = reservation.getTotalCost();

        JOptionPane.showMessageDialog(
                this,
                "Welcome! Your reservation for Room " + room.getRoomNumber()
                        + " is confirmed.\nRate Plan: " + reservation.getRatePlan().getDisplayName()
                        + ".\nActual Nightly Rate: $" + actualNightly
                        + " (Quality Max: $" + room.getQualityLevel().getMaxDailyRate() + ")"
                        + ".\nTotal: $" + total + " for " + nights
                        + " night" + (nights == 1 ? "" : "s") + ".",
                "Booking Confirmed",
                JOptionPane.INFORMATION_MESSAGE
        );
        refreshListings();
        cardLayout.show(pages, "make reservation");
        return true;
    }

    private JTextField createTextField(String placeholder) {
        JTextField textField = new JTextField();
        textField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        textField.setToolTipText(placeholder);
        return textField;
    }

    private JDateChooser createDateChooser(Date initialDate) {
        JDateChooser chooser = new JDateChooser();
        chooser.setDate(initialDate);
        chooser.setDateFormatString("yyyy-MM-dd");
        chooser.setFont(new Font("SansSerif", Font.PLAIN, 13));
        chooser.setPreferredSize(new Dimension(220, 38));
        chooser.setBackground(Color.WHITE);
        chooser.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(0, 0, 0, 0)
        ));
        JTextField textField = ((JTextField) chooser.getDateEditor().getUiComponent());
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        return chooser;
    }

    private JComboBox<Object> buildEnumFilterBox(Object[] values) {
        JComboBox<Object> box = new JComboBox<>();
        box.addItem("Any");
        for (Object value : values) {
            box.addItem(value);
        }
        styleFilterCombo(box);
        return box;
    }

    private void styleFilterCombo(JComboBox<Object> box) {
        box.setFont(new Font("SansSerif", Font.PLAIN, 13));
        box.setBackground(Color.WHITE);
        box.setForeground(UITheme.TEXT_DARK);
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text + ":");
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(UITheme.TEXT_MEDIUM);
        return label;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UITheme.BUTTON_FONT);
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createReserveButton() {
        JButton button = createPrimaryButton("Reserve This Room");
        button.setFont(new Font("SansSerif", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(191, 139, 33));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 85, 20), 2),
                new EmptyBorder(10, 24, 10, 24)
        ));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        return button;
    }

    private void styleReserveButtonState(JButton button, boolean hasValidDateRange, boolean isAvailable) {
        if (!hasValidDateRange) {
            button.setText("Select Dates to Reserve");
            button.setEnabled(false);
            button.setForeground(new Color(95, 98, 104));
            button.setBackground(new Color(233, 236, 240));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(190, 195, 201), 2),
                    new EmptyBorder(10, 24, 10, 24)
            ));
            return;
        }

        if (!isAvailable) {
            button.setText("Unavailable for Dates");
            button.setEnabled(false);
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(166, 54, 46));
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(114, 35, 30), 2),
                    new EmptyBorder(10, 24, 10, 24)
            ));
            return;
        }

        button.setText("Reserve This Room");
        button.setEnabled(true);
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(191, 139, 33));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 85, 20), 2),
                new EmptyBorder(10, 24, 10, 24)
        ));
    }

    private Date todayAtMidnight() {
        return Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date dateFromLocalDate(LocalDate date) {
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void setCheckOutMinimum() {
        LocalDate checkInDate = toLocalDate(checkInChooser.getDate());
        if (checkInDate == null) {
            return;
        }
        LocalDate minCheckOutDate = checkInDate.plusDays(1);
        Date minCheckOut = dateFromLocalDate(minCheckOutDate);
        checkOutChooser.setMinSelectableDate(minCheckOut);
        Date selectedCheckOut = checkOutChooser.getDate();
        if (selectedCheckOut == null || selectedCheckOut.before(minCheckOut)) {
            checkOutChooser.setDate(minCheckOut);
        }
    }

    private Image getRoomImageForTheme(Theme theme) {
        if (theme == null) {
            return loadImage("assets/6Stars-Room.jpg");
        }

        return switch (theme) {
            case NATURE_RETREAT -> loadImage("assets/roomImages/natureRetreatRoom.png");
            case URBAN_ELEGANCE -> loadImage("assets/roomImages/urbanEleganceRoom.png");
            case VINTAGE_CHARM -> loadImage("assets/roomImages/vintageCharmRoom.png");
        };
    }

    private Image loadImage(String relativePath) {
        ImageIcon icon = new ImageIcon(relativePath);
        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
            return icon.getImage();
        }

        ImageIcon fallbackIcon = new ImageIcon("SEIGroupProject/" + relativePath);
        if (fallbackIcon.getIconWidth() > 0 && fallbackIcon.getIconHeight() > 0) {
            return fallbackIcon.getImage();
        }
        return null;
    }

    private static class ImagePanel extends JPanel {
        private final Image image;

        ImagePanel(Image image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
                g2.setColor(new Color(25, 25, 25, 90));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }
}
