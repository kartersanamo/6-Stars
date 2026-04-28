package com.sixstars.ui;

import com.sixstars.app.Main;
import com.sixstars.model.*;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RoomManagementPage extends JPanel {
    private final JPanel pages;
    private final CardLayout cardLayout;
    private final RoomService roomService;
    private final ReservationService reservationService;

    // Filter components
    private JTextField roomNumberFilterField;
    private JComboBox<String> bedTypeFilterBox;
    private JComboBox<String> themeFilterBox;
    private JComboBox<String> qualityFilterBox;
    private JComboBox<String> smokingFilterBox;
    private JComboBox<String> statusFilterBox;

    // Display components
    private JPanel roomGridPanel;
    private JLabel totalRoomsValueLabel;
    private JLabel availableRoomsValueLabel;
    private JLabel occupiedRoomsValueLabel;
    private JLabel maintenanceRoomsValueLabel;

    // Form components
    private JTextField roomNumField;
    private JComboBox<BedType> bedTypeBox;
    private JComboBox<Theme> themeBox;
    private JComboBox<QualityLevel> qualityBox;
    private JCheckBox smokingCheckBox;
    private JSpinner pricePerNightSpinner;

    // Data
    private List<Room> allRooms = new ArrayList<>();
    private List<Reservation> allReservations = new ArrayList<>();

    public RoomManagementPage(JPanel pages, CardLayout cardLayout, RoomService roomService, ReservationService reservationService) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.roomService = roomService;
        this.reservationService = reservationService;

        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.PAGE_BACKGROUND);

        // Build the page sections
        add(buildHeaderSection(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildFooterSection(), BorderLayout.SOUTH);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refreshData();
                updateRoomDisplay();
            }
        });

        refreshData();
        updateRoomDisplay();
    }

    private JPanel buildHeaderSection() {
        JPanel header = new JPanel(new BorderLayout(20, 0));
        header.setBackground(UITheme.CARD_BACKGROUND);
        header.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(20, 28, 20, 28)
        ));

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Room Inventory Manager");
        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_DARK);

        JLabel subtitleLabel = new JLabel("Monitor and manage all hotel rooms");
        subtitleLabel.setFont(UITheme.SUBTITLE_FONT);
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        titlePanel.add(subtitleLabel);

        // Statistics Cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);

        statsPanel.add(createStatCardPanel("Total Rooms", totalRoomsValueLabel = new JLabel("0"), new Color(100, 150, 200)));
        statsPanel.add(createStatCardPanel("Available", availableRoomsValueLabel = new JLabel("0"), new Color(76, 175, 80)));
        statsPanel.add(createStatCardPanel("Occupied", occupiedRoomsValueLabel = new JLabel("0"), new Color(244, 67, 54)));
        statsPanel.add(createStatCardPanel("Maintenance", maintenanceRoomsValueLabel = new JLabel("0"), new Color(255, 193, 7)));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        return header;
    }

    private JLabel createStatCard(String label, String value, Color accentColor) {
        // We'll return just the value label for easy updating later
        // The JLabel itself acts as a container
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueComp.setForeground(accentColor);
        valueComp.setAlignmentX(Component.CENTER_ALIGNMENT);
        return valueComp;
    }

    private JPanel createStatCardPanel(String label, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(new CompoundBorder(
                new LineBorder(accentColor, 2),
                new EmptyBorder(12, 16, 12, 16)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("SansSerif", Font.PLAIN, 12));
        labelComp.setForeground(UITheme.TEXT_MEDIUM);
        labelComp.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(labelComp);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(valueLabel);

        return card;
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(UITheme.PAGE_BACKGROUND);
        main.setBorder(new EmptyBorder(20, 28, 20, 28));

        // Left Panel: Filters & Add Form (Scrollable)
        JPanel leftPanelContent = new JPanel() {
            @Override
            public Dimension getPreferredSize() {
                Dimension preferred = super.getPreferredSize();
                java.awt.Container parent = getParent();
                if (parent instanceof JViewport) {
                    int viewportWidth = ((JViewport) parent).getWidth();
                    if (viewportWidth > 0) {
                        preferred.width = viewportWidth;
                    }
                }
                return preferred;
            }
        };
        leftPanelContent.setLayout(new BoxLayout(leftPanelContent, BoxLayout.Y_AXIS));
        leftPanelContent.setOpaque(false);

        leftPanelContent.add(buildFiltersSection());
        leftPanelContent.add(Box.createRigidArea(new Dimension(0, 20)));
        leftPanelContent.add(buildAddRoomSection());
        leftPanelContent.add(Box.createVerticalGlue());

        // Wrap left panel in scroll pane
        JScrollPane leftScrollPane = new JScrollPane(leftPanelContent);
        leftScrollPane.setBackground(UITheme.PAGE_BACKGROUND);
        leftScrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        leftScrollPane.setBorder(BorderFactory.createEmptyBorder());
        leftScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        leftScrollPane.setPreferredSize(new Dimension(320, 400));

        // Right Panel: Room Grid
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBackground(UITheme.PAGE_BACKGROUND);
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        roomGridPanel = new JPanel();
        roomGridPanel.setLayout(new GridLayout(0, 3, 16, 16));
        roomGridPanel.setBackground(UITheme.PAGE_BACKGROUND);
        roomGridPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollPane.setViewportView(roomGridPanel);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightPanel);
        splitPane.setDividerLocation(320);
        splitPane.setOpaque(false);

        main.add(splitPane, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildFiltersSection() {
        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
        filtersPanel.setOpaque(true);
        filtersPanel.setBackground(UITheme.CARD_BACKGROUND);
        filtersPanel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel filterTitle = new JLabel("Filters & Search");
        filterTitle.setFont(UITheme.LABEL_FONT);
        filterTitle.setForeground(UITheme.TEXT_DARK);
        filtersPanel.add(filterTitle);
        filtersPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Room Number Filter
        filtersPanel.add(createFilterLabel("Room Number"));
        roomNumberFilterField = createFilterTextField("e.g., 101, 201");
        filtersPanel.add(roomNumberFilterField);
        filtersPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Bed Type Filter
        filtersPanel.add(createFilterLabel("Bed Type"));
        bedTypeFilterBox = createFilterComboBox("All Bed Types", BedType.class);
        filtersPanel.add(bedTypeFilterBox);
        filtersPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Theme Filter
        filtersPanel.add(createFilterLabel("Theme"));
        themeFilterBox = createFilterComboBox("All Themes", Theme.class);
        filtersPanel.add(themeFilterBox);
        filtersPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Quality Filter
        filtersPanel.add(createFilterLabel("Quality Level"));
        qualityFilterBox = createFilterComboBox("All Qualities", QualityLevel.class);
        filtersPanel.add(qualityFilterBox);
        filtersPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Smoking Filter
        filtersPanel.add(createFilterLabel("Smoking"));
        smokingFilterBox = new JComboBox<>(new String[]{"All", "Smoking Allowed", "Non-Smoking"});
        styleComboBox(smokingFilterBox);
        filtersPanel.add(smokingFilterBox);
        filtersPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Status Filter
        filtersPanel.add(createFilterLabel("Status"));
        statusFilterBox = new JComboBox<>(new String[]{"All", "Vacant", "Occupied", "Booked", "Checked Out"});
        styleComboBox(statusFilterBox);
        filtersPanel.add(statusFilterBox);
        filtersPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Apply Filters Button
        JButton applyButton = createPrimaryButton("Apply Filters");
        applyButton.addActionListener(e -> updateRoomDisplay());
        filtersPanel.add(applyButton);

        // Reset Button
        JButton resetButton = createSecondaryButton("Reset All");
        resetButton.addActionListener(e -> resetFilters());
        filtersPanel.add(resetButton);

        return filtersPanel;
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(UITheme.TEXT_MEDIUM);
        return label;
    }

    private JTextField createFilterTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(UITheme.INPUT_FONT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setPreferredSize(new Dimension(300, 36));
        field.setBackground(Color.WHITE);
        field.setForeground(UITheme.TEXT_DARK);
        field.setToolTipText(placeholder);
        field.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return field;
    }

    private JComboBox<String> createFilterComboBox(String allOption, Class<?> enumClass) {
        java.util.List<String> items = new ArrayList<>();
        items.add(allOption);

        Object[] constants = enumClass.getEnumConstants();
        if (constants != null) {
            for (Object constant : constants) {
                items.add(constant.toString());
            }
        }

        JComboBox<String> box = new JComboBox<>(items.toArray(new String[0]));
        styleComboBox(box);
        return box;
    }

    private void styleComboBox(JComboBox<String> box) {
        box.setFont(UITheme.INPUT_FONT);
        box.setBackground(Color.WHITE);
        box.setForeground(UITheme.TEXT_DARK);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        box.setPreferredSize(new Dimension(300, 36));
    }

    private JPanel buildAddRoomSection() {
        JPanel addPanel = new JPanel();
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
        addPanel.setOpaque(true);
        addPanel.setBackground(UITheme.CARD_BACKGROUND);
        addPanel.setBorder(new CompoundBorder(
                new LineBorder(UITheme.ACCENT_GOLD, 2),
                new EmptyBorder(16, 16, 16, 16)
        ));

        JLabel addTitle = new JLabel("Add New Room");
        addTitle.setFont(UITheme.LABEL_FONT);
        addTitle.setForeground(UITheme.ACCENT_GOLD);
        addPanel.add(addTitle);
        addPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        // Room Number
        addPanel.add(createFilterLabel("Room Number"));
        roomNumField = createFilterTextField("e.g., 401");
        addPanel.add(roomNumField);
        addPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Bed Type
        addPanel.add(createFilterLabel("Bed Type"));
        bedTypeBox = new JComboBox<>(BedType.values());
        styleComboBox((JComboBox<String>) (JComboBox<?>) bedTypeBox);
        addPanel.add(bedTypeBox);
        addPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Theme
        addPanel.add(createFilterLabel("Theme"));
        themeBox = new JComboBox<>(Theme.values());
        styleComboBox((JComboBox<String>) (JComboBox<?>) themeBox);
        addPanel.add(themeBox);
        addPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Quality
        addPanel.add(createFilterLabel("Quality Level"));
        qualityBox = new JComboBox<>(QualityLevel.values());
        styleComboBox((JComboBox<String>) (JComboBox<?>) qualityBox);
        addPanel.add(qualityBox);
        addPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Price Per Night
        addPanel.add(createFilterLabel("Price Per Night ($)"));
        pricePerNightSpinner = new JSpinner(new SpinnerNumberModel(149, 50, 500, 10));
        pricePerNightSpinner.setFont(UITheme.INPUT_FONT);
        pricePerNightSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        addPanel.add(pricePerNightSpinner);
        addPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Smoking
        smokingCheckBox = new JCheckBox("Smoking Allowed");
        smokingCheckBox.setFont(UITheme.INPUT_FONT);
        smokingCheckBox.setBackground(UITheme.CARD_BACKGROUND);
        smokingCheckBox.setForeground(UITheme.TEXT_DARK);
        addPanel.add(smokingCheckBox);
        addPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Add Button
        JButton addButton = createPrimaryButton("Add Room");
        addButton.addActionListener(e -> addNewRoom());
        addPanel.add(addButton);

        return addPanel;
    }

    private JPanel buildFooterSection() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        footer.setBackground(UITheme.CARD_BACKGROUND);
        footer.setBorder(new CompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(12, 28, 12, 28)
        ));

        JButton backButton = createSecondaryButton("Back to Clerk Dashboard");
        backButton.addActionListener(e -> cardLayout.show(pages, "clerk page"));
        footer.add(backButton);

        JButton refreshButton = createPrimaryButton("Refresh Data");
        refreshButton.addActionListener(e -> {
            refreshData();
            updateRoomDisplay();
        });
        footer.add(refreshButton);

        return footer;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UITheme.BUTTON_FONT);
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(300, 40));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UITheme.BUTTON_FONT);
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(300, 40));
        return button;
    }

    private void addNewRoom() {
        try {
            String roomNumText = roomNumField.getText().trim();
            if (roomNumText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a room number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int roomNum = Integer.parseInt(roomNumText);
            if (roomNum < 100 || roomNum > 9999) {
                JOptionPane.showMessageDialog(this, "Room number should be between 100 and 9999.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check for duplicates
            if (allRooms.stream().anyMatch(r -> r.getRoomNumber() == roomNum)) {
                JOptionPane.showMessageDialog(this, "Room " + roomNum + " already exists.", "Duplicate Room", JOptionPane.WARNING_MESSAGE);
                return;
            }

            BedType bedType = (BedType) bedTypeBox.getSelectedItem();
            Theme theme = (Theme) themeBox.getSelectedItem();
            QualityLevel quality = (QualityLevel) qualityBox.getSelectedItem();
            boolean smoking = smokingCheckBox.isSelected();
            int price = (Integer) pricePerNightSpinner.getValue();

            Room newRoom = new Room(roomNum, bedType, theme, quality, smoking, price);
            roomService.addRoom(newRoom);

            // Reset form
            roomNumField.setText("");
            smokingCheckBox.setSelected(false);
            pricePerNightSpinner.setValue(149);

            // Refresh display
            refreshData();
            updateRoomDisplay();

            JOptionPane.showMessageDialog(this, "Room " + roomNum + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid room number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshData() {
        allRooms = roomService.getAllRooms();
        allReservations = reservationService.getAllReservations();

        // Update status for each room
        for (Room room : allRooms) {
            String status = reservationService.getRoomStatus(room, allReservations);
            room.setStatus(status);
        }

        updateStatistics();
    }

    private void updateStatistics() {
        int total = allRooms.size();
        long available = allRooms.stream().filter(r -> "Vacant".equals(r.getStatus())).count();
        long occupied = allRooms.stream().filter(r -> "Occupied".equals(r.getStatus())).count();
        long maintenance = allRooms.stream().filter(r -> "Checked Out".equals(r.getStatus())).count();

        totalRoomsValueLabel.setText(String.valueOf(total));
        availableRoomsValueLabel.setText(String.valueOf(available));
        occupiedRoomsValueLabel.setText(String.valueOf(occupied));
        maintenanceRoomsValueLabel.setText(String.valueOf(maintenance));
    }

    private void updateRoomDisplay() {
        roomGridPanel.removeAll();

        List<Room> filtered = filterRooms();

        if (filtered.isEmpty()) {
            JLabel emptyLabel = new JLabel("No rooms match your filters.");
            emptyLabel.setFont(UITheme.SUBTITLE_FONT);
            emptyLabel.setForeground(UITheme.TEXT_MEDIUM);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            roomGridPanel.setLayout(new BorderLayout());
            roomGridPanel.add(emptyLabel, BorderLayout.CENTER);
        } else {
            roomGridPanel.setLayout(new GridLayout(0, 3, 16, 16));
            for (Room room : filtered) {
                roomGridPanel.add(createRoomCard(room));
            }
        }

        roomGridPanel.revalidate();
        roomGridPanel.repaint();
    }

    private List<Room> filterRooms() {
        String roomNumInput = sanitizeFilterText(roomNumberFilterField.getText());
        String bedTypeFilter = sanitizeFilterText((String) bedTypeFilterBox.getSelectedItem());
        String themeFilter = sanitizeFilterText((String) themeFilterBox.getSelectedItem());
        String qualityFilter = sanitizeFilterText((String) qualityFilterBox.getSelectedItem());
        String smokingFilter = sanitizeFilterText((String) smokingFilterBox.getSelectedItem());
        String statusFilter = sanitizeFilterText((String) statusFilterBox.getSelectedItem());

        return allRooms.stream()
                .filter(r -> roomNumInput.isEmpty() || String.valueOf(r.getRoomNumber()).contains(roomNumInput))
                .filter(r -> isAllOption(bedTypeFilter) || r.getBedType().toString().equalsIgnoreCase(bedTypeFilter))
                .filter(r -> isAllOption(themeFilter) || r.getTheme().toString().equalsIgnoreCase(themeFilter))
                .filter(r -> isAllOption(qualityFilter) || r.getQualityLevel().toString().equalsIgnoreCase(qualityFilter))
                .filter(r -> {
                    if (isAllOption(smokingFilter)) return true;
                    if (smokingFilter.contains("Smoking Allowed")) return r.isSmoking();
                    return !r.isSmoking();
                })
                .filter(r -> isAllOption(statusFilter) || normalizeStatus(r.getStatus()).equalsIgnoreCase(statusFilter))
                .collect(Collectors.toList());
    }

    private String sanitizeFilterText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim();
        if (normalized.equalsIgnoreCase("e.g., 101, 201") || normalized.equalsIgnoreCase("room number")) {
            return "";
        }
        return normalized;
    }

    private boolean isAllOption(String filterValue) {
        return filterValue == null || filterValue.isBlank() || filterValue.toLowerCase().startsWith("all");
    }

    private String normalizeStatus(String status) {
        return status == null ? "Vacant" : status.trim();
    }

    private JPanel createRoomCard(Room room) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(UITheme.CARD_BACKGROUND);

        // Colored border based on status
        Color borderColor = getStatusColor(room.getStatus());
        card.setBorder(new CompoundBorder(
                new LineBorder(borderColor, 3),
                new EmptyBorder(12, 12, 12, 12)
        ));

        // Room Number
        JLabel roomNumLabel = new JLabel("Room " + room.getRoomNumber());
        roomNumLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        roomNumLabel.setForeground(UITheme.TEXT_DARK);
        card.add(roomNumLabel);

        card.add(Box.createRigidArea(new Dimension(0, 8)));

        // Status Badge
        JLabel statusLabel = new JLabel(room.getStatus().toUpperCase());
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusLabel.setForeground(Color.WHITE);
        statusLabel.setBackground(borderColor);
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        statusLabel.setMaximumSize(new Dimension(150, 20));
        card.add(statusLabel);

        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // Bed Type
        JLabel bedLabel = new JLabel("🛏 " + room.getBedType());
        bedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bedLabel.setForeground(UITheme.TEXT_MEDIUM);
        card.add(bedLabel);

        // Theme
        JLabel themeLabel = new JLabel("🎨 " + room.getTheme());
        themeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        themeLabel.setForeground(UITheme.TEXT_MEDIUM);
        card.add(themeLabel);

        // Quality
        JLabel qualityLabel = new JLabel("⭐ " + room.getQualityLevel());
        qualityLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        qualityLabel.setForeground(UITheme.TEXT_MEDIUM);
        card.add(qualityLabel);

        // Smoking
        if (room.isSmoking()) {
            JLabel smokingLabel = new JLabel("🚬 Smoking Allowed");
            smokingLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            smokingLabel.setForeground(new Color(200, 100, 100));
            card.add(smokingLabel);
        }

        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // Price
        JLabel priceLabel = new JLabel("$" + room.getPricePerNight() + "/night");
        priceLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        priceLabel.setForeground(UITheme.ACCENT_GOLD);
        card.add(priceLabel);

        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // Action Button
        JButton detailsButton = new JButton("View Details");
        detailsButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        detailsButton.setBackground(UITheme.ACCENT_GOLD);
        detailsButton.setForeground(Color.WHITE);
        detailsButton.setFocusPainted(false);
        detailsButton.setBorderPainted(false);
        detailsButton.setOpaque(true);
        detailsButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailsButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        detailsButton.addActionListener(e -> showRoomDetails(room));
        card.add(detailsButton);

        card.add(Box.createVerticalGlue());

        return card;
    }

    private Color getStatusColor(String status) {
        if (status == null) status = "Available";

        return switch (status) {
            case "Occupied" -> new Color(244, 67, 54);
            case "Maintenance" -> new Color(255, 193, 7);
            case "Reserved" -> new Color(63, 81, 181);
            default -> new Color(76, 175, 80);
        };
    }

    private void showRoomDetails(Room room) {
        JDialog detailsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Room " + room.getRoomNumber() + " Details", true);
        detailsDialog.setSize(500, 450);
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setResizable(false);

        JPanel dialogContent = new JPanel();
        dialogContent.setLayout(new BoxLayout(dialogContent, BoxLayout.Y_AXIS));
        dialogContent.setBackground(UITheme.PAGE_BACKGROUND);
        dialogContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Details
        dialogContent.add(createDetailRow("Room Number:", String.valueOf(room.getRoomNumber())));
        dialogContent.add(Box.createRigidArea(new Dimension(0, 10)));
        dialogContent.add(createDetailRow("Bed Type:", room.getBedType().toString()));
        dialogContent.add(Box.createRigidArea(new Dimension(0, 10)));
        dialogContent.add(createDetailRow("Theme:", room.getTheme().toString()));
        dialogContent.add(Box.createRigidArea(new Dimension(0, 10)));
        dialogContent.add(createDetailRow("Quality Level:", room.getQualityLevel().toString()));
        dialogContent.add(Box.createRigidArea(new Dimension(0, 10)));
        dialogContent.add(createDetailRow("Price Per Night:", "$" + room.getPricePerNight()));
        dialogContent.add(Box.createRigidArea(new Dimension(0, 10)));
        dialogContent.add(createDetailRow("Smoking:", room.isSmoking() ? "Yes" : "No"));
        dialogContent.add(Box.createRigidArea(new Dimension(0, 10)));
        dialogContent.add(createDetailRow("Status:", room.getStatus()));

        dialogContent.add(Box.createRigidArea(new Dimension(0, 20)));

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton closeButton = createSecondaryButton("Close");
        closeButton.setPreferredSize(new Dimension(120, 36));
        closeButton.addActionListener(e -> detailsDialog.dispose());
        buttonPanel.add(closeButton);

        dialogContent.add(buttonPanel);
        dialogContent.add(Box.createVerticalGlue());

        detailsDialog.add(dialogContent);
        detailsDialog.setVisible(true);
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("SansSerif", Font.BOLD, 13));
        labelComp.setForeground(UITheme.TEXT_MEDIUM);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("SansSerif", Font.PLAIN, 13));
        valueComp.setForeground(UITheme.TEXT_DARK);

        row.add(labelComp);
        row.add(Box.createRigidArea(new Dimension(10, 0)));
        row.add(valueComp);

        return row;
    }

    private void resetFilters() {
        roomNumberFilterField.setText("");
        bedTypeFilterBox.setSelectedIndex(0);
        themeFilterBox.setSelectedIndex(0);
        qualityFilterBox.setSelectedIndex(0);
        smokingFilterBox.setSelectedIndex(0);
        statusFilterBox.setSelectedIndex(0);
        updateRoomDisplay();
    }
}