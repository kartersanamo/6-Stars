package com.sixstars.ui;

import com.sixstars.model.*;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RoomManagementPage extends JPanel {
    /**
     * Warm palette aligned with {@link AccountCenterPage} (sidebar + billing sections),
     * not flat white / gray chrome.
     */
    private static final Color RIM_PAGE = UITheme.PAGE_BACKGROUND;
    private static final Color RIM_HEADER_FOOTER = new Color(245, 240, 228);
    private static final Color RIM_PANEL = new Color(252, 249, 241);
    private static final Color RIM_SECTION = new Color(252, 250, 245);
    private static final Color RIM_WARM_BORDER = new Color(212, 200, 182);
    private static final Color RIM_INPUT_FILL = new Color(255, 252, 246);
    private static final Color RIM_BUTTON_IDLE = new Color(246, 241, 230);
    private static final Color RIM_SUBHEAD = new Color(112, 103, 90);
    private static final Color RIM_STAT_TOTAL = new Color(176, 132, 38);
    private static final Color RIM_STAT_AVAILABLE = new Color(44, 122, 72);
    private static final Color RIM_STAT_OCCUPIED = new Color(130, 86, 76);
    private static final Color RIM_STAT_MAINT = new Color(108, 98, 88);

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
    private JPanel notificationPanel;
    private JLabel notificationMessageLabel;

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
        setBackground(RIM_PAGE);

        // Build notification panel first (will be added to page)
        notificationPanel = createNotificationPanel();
        notificationPanel.setVisible(false);

        // Build the page sections
        add(buildHeaderSection(), BorderLayout.NORTH);
        add(buildNotificationAndContent(), BorderLayout.CENTER);
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

    private JPanel buildNotificationAndContent() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(notificationPanel, BorderLayout.NORTH);
        container.add(buildMainContent(), BorderLayout.CENTER);
        return container;
    }

    private JPanel buildHeaderSection() {
        JPanel header = new JPanel(new BorderLayout(24, 0));
        header.setBackground(RIM_HEADER_FOOTER);
        header.setBorder(new CompoundBorder(
                new LineBorder(RIM_WARM_BORDER, 1),
                new EmptyBorder(22, 28, 22, 28)
        ));

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Room Inventory Manager");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(UITheme.TEXT_DARK);

        JLabel subtitleLabel = new JLabel("Monitor and manage all hotel rooms");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(RIM_SUBHEAD);

        titlePanel.add(titleLabel);
        titlePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        titlePanel.add(subtitleLabel);

        // Statistics — neutral cards with a slim accent stripe (values stay typographic, not traffic-light hues)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statsPanel.setOpaque(false);

        statsPanel.add(createStatCardPanel("Total Rooms", totalRoomsValueLabel = new JLabel("0"), RIM_STAT_TOTAL));
        statsPanel.add(createStatCardPanel("Available", availableRoomsValueLabel = new JLabel("0"), RIM_STAT_AVAILABLE));
        statsPanel.add(createStatCardPanel("Occupied", occupiedRoomsValueLabel = new JLabel("0"), RIM_STAT_OCCUPIED));
        statsPanel.add(createStatCardPanel("Maintenance", maintenanceRoomsValueLabel = new JLabel("0"), RIM_STAT_MAINT));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(statsPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createStatCardPanel(String label, JLabel valueLabel, Color accentStripe) {
        JPanel card = new JPanel();
        card.setOpaque(true);
        card.setBackground(RIM_PANEL);
        card.setBorder(new CompoundBorder(
                new MatteBorder(0, 3, 0, 0, accentStripe),
                new CompoundBorder(
                        new LineBorder(RIM_WARM_BORDER, 1),
                        new EmptyBorder(12, 14, 12, 14)
                )
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("SansSerif", Font.PLAIN, 11));
        labelComp.setForeground(RIM_SUBHEAD);
        labelComp.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        valueLabel.setForeground(UITheme.TEXT_DARK);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(labelComp);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(valueLabel);

        return card;
    }

    private JPanel buildMainContent() {
        JPanel main = new JPanel(new BorderLayout(0, 20));
        main.setBackground(RIM_PAGE);
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
        leftScrollPane.setBackground(RIM_PAGE);
        leftScrollPane.getViewport().setBackground(RIM_PAGE);
        leftScrollPane.setBorder(BorderFactory.createEmptyBorder());
        leftScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        leftScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        leftScrollPane.setPreferredSize(new Dimension(320, 400));

        // Right Panel: Room Grid
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBackground(RIM_PAGE);
        scrollPane.getViewport().setBackground(RIM_PAGE);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        roomGridPanel = new JPanel();
        roomGridPanel.setLayout(new GridLayout(0, 3, 12, 12));
        roomGridPanel.setBackground(RIM_PAGE);
        roomGridPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        scrollPane.setViewportView(roomGridPanel);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightPanel);
        splitPane.setDividerLocation(320);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(5);
        splitPane.setBorder(BorderFactory.createEmptyBorder());

        main.add(splitPane, BorderLayout.CENTER);
        return main;
    }

    private JPanel buildFiltersSection() {
        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
        filtersPanel.setOpaque(true);
        filtersPanel.setBackground(RIM_SECTION);
        filtersPanel.setBorder(new CompoundBorder(
                new LineBorder(RIM_WARM_BORDER, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JLabel filterTitle = new JLabel("Filters & search");
        filterTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        filterTitle.setForeground(UITheme.ACCENT_GOLD);
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

    private void showModifyRoomDialog(Room room) {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Modify Room " + room.getRoomNumber(),
                true
        );

        dialog.setSize(420, 420);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(RIM_PANEL);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Modify Room " + room.getRoomNumber());
        title.setFont(UITheme.LABEL_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JComboBox<BedType> editBedTypeBox = new JComboBox<>(BedType.values());
        editBedTypeBox.setSelectedItem(room.getBedType());

        JComboBox<Theme> editThemeBox = new JComboBox<>(Theme.values());
        editThemeBox.setSelectedItem(room.getTheme());

        JComboBox<QualityLevel> editQualityBox = new JComboBox<>(QualityLevel.values());
        editQualityBox.setSelectedItem(room.getQualityLevel());

        JSpinner editPriceSpinner = new JSpinner(
                new SpinnerNumberModel(room.getPricePerNight(), 50, 500, 10)
        );
        styleSpinnerEditor(editPriceSpinner);

        JCheckBox editSmokingCheckBox = new JCheckBox("Smoking Allowed");
        editSmokingCheckBox.setSelected(room.isSmoking());
        editSmokingCheckBox.setBackground(RIM_PANEL);

        panel.add(title);
        panel.add(Box.createRigidArea(new Dimension(0, 16)));

        panel.add(createFilterLabel("Bed Type"));
        panel.add(editBedTypeBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFilterLabel("Theme"));
        panel.add(editThemeBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFilterLabel("Quality Level"));
        panel.add(editQualityBox);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(createFilterLabel("Price Per Night ($)"));
        panel.add(editPriceSpinner);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        panel.add(editSmokingCheckBox);
        panel.add(Box.createRigidArea(new Dimension(0, 18)));

        JButton saveButton = createPrimaryButton("Save Changes");
        saveButton.addActionListener(e -> {
            Room updatedRoom = new Room(
                    room.getRoomNumber(),
                    (BedType) editBedTypeBox.getSelectedItem(),
                    (Theme) editThemeBox.getSelectedItem(),
                    (QualityLevel) editQualityBox.getSelectedItem(),
                    editSmokingCheckBox.isSelected(),
                    (Integer) editPriceSpinner.getValue()
            );

            roomService.updateRoom(updatedRoom);

            refreshData();
            updateRoomDisplay();

            showNotification("Room " + room.getRoomNumber() + " updated successfully!");
            dialog.dispose();
        });

        panel.add(saveButton);

        dialog.add(panel);
        dialog.setVisible(true);
    }

    private JLabel createFilterLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(RIM_SUBHEAD);
        return label;
    }

    private JTextField createFilterTextField(String placeholder) {
        JTextField field = new JTextField();
        field.setFont(UITheme.INPUT_FONT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setPreferredSize(new Dimension(300, 36));
        field.setBackground(RIM_INPUT_FILL);
        field.setForeground(UITheme.TEXT_DARK);
        field.setToolTipText(placeholder);
        field.setBorder(new CompoundBorder(
                new LineBorder(RIM_WARM_BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    private void styleSpinnerEditor(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor defaultEditor) {
            JTextField tf = defaultEditor.getTextField();
            tf.setBackground(RIM_INPUT_FILL);
            tf.setForeground(UITheme.TEXT_DARK);
            tf.setBorder(new CompoundBorder(
                    new LineBorder(RIM_WARM_BORDER, 1),
                    new EmptyBorder(4, 8, 4, 8)
            ));
        }
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
        box.setBackground(RIM_INPUT_FILL);
        box.setForeground(UITheme.TEXT_DARK);
        box.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        box.setPreferredSize(new Dimension(300, 36));
    }

    private JPanel buildAddRoomSection() {
        JPanel addPanel = new JPanel();
        addPanel.setLayout(new BoxLayout(addPanel, BoxLayout.Y_AXIS));
        addPanel.setOpaque(true);
        addPanel.setBackground(RIM_PANEL);
        addPanel.setBorder(new CompoundBorder(
                new MatteBorder(0, 3, 0, 0, UITheme.ACCENT_GOLD),
                new CompoundBorder(
                        new LineBorder(RIM_WARM_BORDER, 1),
                        new EmptyBorder(18, 15, 18, 18)
                )
        ));

        JLabel addTitle = new JLabel("Add new room");
        addTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
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
        styleSpinnerEditor(pricePerNightSpinner);
        addPanel.add(pricePerNightSpinner);
        addPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // Smoking
        smokingCheckBox = new JCheckBox("Smoking Allowed");
        smokingCheckBox.setFont(UITheme.INPUT_FONT);
        smokingCheckBox.setBackground(RIM_PANEL);
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
        footer.setBackground(RIM_HEADER_FOOTER);
        footer.setBorder(new CompoundBorder(
                new LineBorder(RIM_WARM_BORDER, 1),
                new EmptyBorder(14, 28, 14, 28)
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
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(new Color(255, 252, 246));
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
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(RIM_BUTTON_IDLE);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setOpaque(true);
        button.setBorder(new CompoundBorder(
                new LineBorder(RIM_WARM_BORDER, 1),
                new EmptyBorder(9, 14, 9, 14)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        button.setPreferredSize(new Dimension(300, 40));
        return button;
    }

    private void addNewRoom() {
        try {
            String roomNumText = roomNumField.getText().trim();
            if (roomNumText.isEmpty()) {
                showNotification("Please enter a room number.");
                return;
            }

            int roomNum = Integer.parseInt(roomNumText);
            if (roomNum < 100 || roomNum > 9999) {
                showNotification("Room number should be between 100 and 9999.");
                return;
            }

            // Check for duplicates
            if (allRooms.stream().anyMatch(r -> r.getRoomNumber() == roomNum)) {
                showNotification("Room " + roomNum + " already exists.");
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

            showNotification("Room " + roomNum + " added successfully!");
        } catch (NumberFormatException ex) {
            showNotification("Please enter a valid room number.");
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
            roomGridPanel.setLayout(new GridLayout(0, 3, 12, 12));
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
        card.setBackground(RIM_PANEL);

        Color stripe = getStatusStripeColor(room.getStatus());
        card.setBorder(new CompoundBorder(
                new MatteBorder(0, 2, 0, 0, stripe),
                new CompoundBorder(
                        new LineBorder(RIM_WARM_BORDER, 1),
                        new EmptyBorder(14, 14, 14, 14)
                )
        ));

        JLabel roomNumLabel = new JLabel("Room " + room.getRoomNumber());
        roomNumLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        roomNumLabel.setForeground(UITheme.TEXT_DARK);
        roomNumLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(roomNumLabel);

        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel statusLabel = new JLabel(room.getStatus());
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusLabel.setForeground(getStatusBadgeForeground(room.getStatus()));
        statusLabel.setBackground(getStatusBadgeBackground(room.getStatus()));
        statusLabel.setOpaque(true);
        statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setMaximumSize(new Dimension(160, 24));
        card.add(statusLabel);

        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JLabel bedLabel = new JLabel("Bed · " + room.getBedType());
        bedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bedLabel.setForeground(RIM_SUBHEAD);
        bedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(bedLabel);

        JLabel themeLabel = new JLabel("Theme · " + room.getTheme());
        themeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        themeLabel.setForeground(RIM_SUBHEAD);
        themeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(themeLabel);

        JLabel qualityLabel = new JLabel("Quality · " + room.getQualityLevel());
        qualityLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        qualityLabel.setForeground(RIM_SUBHEAD);
        qualityLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(qualityLabel);

        if (room.isSmoking()) {
            JLabel smokingLabel = new JLabel("Smoking allowed");
            smokingLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            smokingLabel.setForeground(new Color(130, 95, 80));
            smokingLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(smokingLabel);
        }

        card.add(Box.createRigidArea(new Dimension(0, 10)));

        Box priceRow = Box.createHorizontalBox();
        priceRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel priceMain = new JLabel("$" + room.getPricePerNight());
        priceMain.setFont(new Font("SansSerif", Font.BOLD, 14));
        priceMain.setForeground(UITheme.ACCENT_GOLD);
        JLabel priceUnit = new JLabel(" / night");
        priceUnit.setFont(new Font("SansSerif", Font.PLAIN, 12));
        priceUnit.setForeground(RIM_SUBHEAD);
        priceRow.add(priceMain);
        priceRow.add(priceUnit);
        priceRow.add(Box.createHorizontalGlue());
        card.add(priceRow);

        card.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton detailsButton = createCardOutlineButton("View details");
        detailsButton.addActionListener(e -> showRoomDetails(room));
        card.add(detailsButton);

        card.add(Box.createVerticalGlue());
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        JButton modifyButton = createCardOutlineButton("Modify room");
        modifyButton.addActionListener(e -> showModifyRoomDialog(room));
        card.add(modifyButton);

        return card;
    }

    private JButton createCardOutlineButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(RIM_BUTTON_IDLE);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setOpaque(true);
        button.setBorder(new CompoundBorder(
                new LineBorder(RIM_WARM_BORDER, 1),
                new EmptyBorder(6, 10, 6, 10)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        return button;
    }

    private Color getStatusStripeColor(String status) {
        String s = status == null ? "Vacant" : status.trim();
        if (s.isEmpty()) {
            s = "Vacant";
        }
        return switch (s) {
            case "Occupied" -> new Color(208, 192, 188);
            case "Booked", "Reserved" -> new Color(198, 202, 214);
            case "Checked Out", "Maintenance" -> new Color(210, 204, 196);
            default -> new Color(196, 206, 190);
        };
    }

    private Color getStatusBadgeBackground(String status) {
        String s = status == null ? "Vacant" : status.trim();
        if (s.isEmpty()) {
            s = "Vacant";
        }
        return switch (s) {
            case "Occupied" -> new Color(247, 238, 235);
            case "Booked", "Reserved" -> new Color(238, 240, 246);
            case "Checked Out", "Maintenance" -> new Color(244, 241, 234);
            default -> new Color(236, 242, 232);
        };
    }

    private Color getStatusBadgeForeground(String status) {
        String s = status == null ? "Vacant" : status.trim();
        if (s.isEmpty()) {
            s = "Vacant";
        }
        return switch (s) {
            case "Occupied" -> new Color(92, 52, 52);
            case "Booked", "Reserved" -> new Color(52, 58, 76);
            case "Checked Out", "Maintenance" -> new Color(72, 66, 58);
            default -> new Color(48, 72, 52);
        };
    }

    private void showRoomDetails(Room room) {
        JDialog detailsDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Room " + room.getRoomNumber() + " Details", true);
        detailsDialog.setSize(620, 620);
        detailsDialog.setLocationRelativeTo(this);
        detailsDialog.setResizable(false);

        List<Reservation> roomReservations = getReservationsForRoom(room);
        Reservation activeReservation = findActiveReservation(roomReservations);

        JPanel dialogContent = new JPanel();
        dialogContent.setLayout(new BoxLayout(dialogContent, BoxLayout.Y_AXIS));
        dialogContent.setBackground(RIM_PAGE);
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
        dialogContent.add(Box.createRigidArea(new Dimension(0, 10)));
        dialogContent.add(createDetailRow("Current Reservation:", activeReservation == null
                ? "None active right now"
                : "#" + activeReservation.getId() + " (" + activeReservation.getGuestEmail() + ")"));

        dialogContent.add(Box.createRigidArea(new Dimension(0, 18)));

        JLabel reservationSectionTitle = new JLabel("Reservation Activity");
        reservationSectionTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        reservationSectionTitle.setForeground(UITheme.ACCENT_GOLD);
        dialogContent.add(reservationSectionTitle);

        JLabel reservationSectionSubtitle = new JLabel("Verbose timeline for this room (newest first)");
        reservationSectionSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        reservationSectionSubtitle.setForeground(RIM_SUBHEAD);
        dialogContent.add(Box.createRigidArea(new Dimension(0, 2)));
        dialogContent.add(reservationSectionSubtitle);
        dialogContent.add(Box.createRigidArea(new Dimension(0, 10)));

        if (roomReservations.isEmpty()) {
            JPanel emptyReservationPanel = new JPanel(new BorderLayout());
            emptyReservationPanel.setOpaque(true);
            emptyReservationPanel.setBackground(RIM_PANEL);
            emptyReservationPanel.setBorder(new CompoundBorder(
                    new LineBorder(RIM_WARM_BORDER, 1),
                    new EmptyBorder(12, 12, 12, 12)
            ));

            JLabel emptyReservationLabel = new JLabel("No reservations found for this room yet.");
            emptyReservationLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            emptyReservationLabel.setForeground(RIM_SUBHEAD);
            emptyReservationPanel.add(emptyReservationLabel, BorderLayout.CENTER);
            dialogContent.add(emptyReservationPanel);
        } else {
            for (Reservation reservation : roomReservations) {
                dialogContent.add(createReservationDetailCard(reservation));
                dialogContent.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        dialogContent.add(Box.createRigidArea(new Dimension(0, 20)));

        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);

        JButton closeButton = createSecondaryButton("Close");
        closeButton.setPreferredSize(new Dimension(120, 36));
        closeButton.addActionListener(e -> detailsDialog.dispose());
        buttonPanel.add(closeButton);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(RIM_PAGE);

        JScrollPane scrollPane = new JScrollPane(dialogContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        outer.add(scrollPane, BorderLayout.CENTER);
        outer.add(buttonPanel, BorderLayout.SOUTH);

        detailsDialog.add(outer);
        detailsDialog.setVisible(true);
    }

    private List<Reservation> getReservationsForRoom(Room room) {
        return allReservations.stream()
                .filter(reservation -> reservation.getRooms() != null)
                .filter(reservation -> reservation.getRooms().stream()
                        .anyMatch(bookedRoom -> bookedRoom.getRoomNumber() == room.getRoomNumber()))
                .sorted(Comparator
                        .comparing(Reservation::getStartDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .collect(Collectors.toList());
    }

    private Reservation findActiveReservation(List<Reservation> roomReservations) {
        LocalDate today = LocalDate.now();

        return roomReservations.stream()
                .filter(reservation -> reservation.getStartDate() != null && reservation.getEndDate() != null)
                .filter(reservation -> !today.isBefore(reservation.getStartDate()) && today.isBefore(reservation.getEndDate()))
                .findFirst()
                .orElse(null);
    }

    private JPanel createReservationDetailCard(Reservation reservation) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(RIM_SECTION);
        card.setBorder(new CompoundBorder(
                new LineBorder(RIM_WARM_BORDER, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel("Reservation #" + reservation.getId());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        titleLabel.setForeground(UITheme.TEXT_DARK);
        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        card.add(createDetailRow("Guest:", reservation.getGuestEmail()));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(createDetailRow("Dates:", reservation.getStartDate() + " to " + reservation.getEndDate()));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(createDetailRow("Status:", reservation.getStatus()));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(createDetailRow("Created:", String.valueOf(reservation.getCreatedDate())));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(createDetailRow("Rate/Nights:", "$" + reservation.getNightlyRate() + " x " + reservation.getNights()));
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(createDetailRow("Total:", "$" + reservation.getTotalCost()));

        return card;
    }

    private JPanel createDetailRow(String label, String value) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        row.setOpaque(false);

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("SansSerif", Font.BOLD, 13));
        labelComp.setForeground(RIM_SUBHEAD);

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

    private JPanel createNotificationPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 0));
        panel.setBackground(RIM_SECTION);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));

        panel.setBorder(new CompoundBorder(
                new LineBorder(RIM_WARM_BORDER, 1),
                new EmptyBorder(10, 28, 10, 28)
        ));

        JLabel iconLabel = new JLabel("●");
        iconLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        iconLabel.setForeground(UITheme.ACCENT_GOLD);

        notificationMessageLabel = new JLabel("Notification message");
        notificationMessageLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        notificationMessageLabel.setForeground(UITheme.TEXT_DARK);

        JButton dismissButton = new JButton("Close");
        dismissButton.setFont(new Font("SansSerif", Font.PLAIN, 12));
        dismissButton.setForeground(RIM_SUBHEAD);
        dismissButton.setBackground(RIM_SECTION);
        dismissButton.setFocusPainted(false);
        dismissButton.setBorderPainted(false);
        dismissButton.setOpaque(false);
        dismissButton.setContentAreaFilled(false);
        dismissButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dismissButton.addActionListener(_ -> panel.setVisible(false));

        panel.add(iconLabel, BorderLayout.WEST);
        panel.add(notificationMessageLabel, BorderLayout.CENTER);
        panel.add(dismissButton, BorderLayout.EAST);

        return panel;
    }

    private void showNotification(String message) {
        notificationMessageLabel.setText(message);
        notificationPanel.setVisible(true);
        revalidate();
        repaint();
    }
}