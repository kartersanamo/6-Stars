package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Reservation;
import com.sixstars.model.Room;
import com.sixstars.model.Role;
import com.sixstars.service.ReservationService;

import javax.swing.*;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ReservationsPage extends JPanel {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);

    private final DefaultListModel<Reservation> listModel;
    private final JList<Reservation> resList;
    private final ReservationService resService;
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private final JLabel totalReservationsValue;
    private final JLabel activeReservationsValue;
    private final JLabel nextCheckInValue;
    private final JLabel reservationIdValue;
    private final JLabel statusValue;
    private final JLabel guestEmailValue;
    private final JLabel stayDatesValue;
    private final JLabel pricingValue;
    private final JLabel createdValue;
    private final JTextArea roomsValue;
    private final JButton btnModify;
    private final JButton btnCancel;

    public ReservationsPage(JPanel pages, CardLayout cardLayout, ReservationService resService) {
        this.resService = resService;
        this.listModel = new DefaultListModel<>();
        this.resList = new JList<>(listModel);
        this.titleLabel = new JLabel("My Reservations");
        this.subtitleLabel = new JLabel("Review and manage all reservation details in one place.");
        this.totalReservationsValue = createMetricValueLabel();
        this.activeReservationsValue = createMetricValueLabel();
        this.nextCheckInValue = createMetricValueLabel();
        this.reservationIdValue = createDetailValueLabel();
        this.statusValue = createDetailValueLabel();
        this.guestEmailValue = createDetailValueLabel();
        this.stayDatesValue = createDetailValueLabel();
        this.pricingValue = createDetailValueLabel();
        this.createdValue = createDetailValueLabel();
        this.roomsValue = new JTextArea();
        this.btnModify = new JButton("Modify Dates");
        this.btnCancel = new JButton("Cancel Booking");

        setLayout(new BorderLayout(20, 20));
        setBackground(UITheme.PAGE_BACKGROUND);
        setBorder(new EmptyBorder(28, 32, 28, 32));

        add(buildHeader(pages, cardLayout), BorderLayout.NORTH);
        add(buildContentArea(), BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);

        configureReservationList();
        styleGoldButton(btnModify);
        styleGoldButton(btnCancel);

        btnModify.addActionListener(_ -> handleModify());
        btnCancel.addActionListener(_ -> handleCancel());
    }

    private JPanel buildHeader(JPanel pages, CardLayout cardLayout) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));

        JPanel titleCard = new JPanel(new BorderLayout(16, 0));
        titleCard.setBackground(UITheme.CARD_BACKGROUND);
        titleCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel titleText = new JPanel();
        titleText.setOpaque(false);
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));

        titleLabel.setFont(UITheme.TITLE_FONT);
        titleLabel.setForeground(UITheme.TEXT_DARK);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);

        titleText.add(titleLabel);
        titleText.add(Box.createRigidArea(new Dimension(0, 4)));
        titleText.add(subtitleLabel);

        JButton btnBack = new JButton("Back");
        styleGoldButton(btnBack);
        btnBack.addActionListener(_ -> {
            var acc = AccountController.currentAccount;
            if (acc != null && acc.getRole() == Role.CLERK) {
                cardLayout.show(pages, "clerk page");
            } else {
                cardLayout.show(pages, "home");
            }
        });

        titleCard.add(titleText, BorderLayout.CENTER);
        titleCard.add(btnBack, BorderLayout.EAST);

        JPanel metricsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        metricsRow.setOpaque(false);
        metricsRow.setBorder(new EmptyBorder(12, 0, 0, 0));
        metricsRow.add(createMetricCard("Reservations", totalReservationsValue));
        metricsRow.add(createMetricCard("Active", activeReservationsValue));
        metricsRow.add(createMetricCard("Next Check-In", nextCheckInValue));

        wrapper.add(titleCard);
        wrapper.add(metricsRow);
        return wrapper;
    }

    private JPanel buildContentArea() {
        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);

        JPanel listCard = new JPanel(new BorderLayout());
        listCard.setBackground(UITheme.CARD_BACKGROUND);
        listCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(14, 14, 14, 14)
        ));

        JLabel listHeading = new JLabel("Reservation Timeline");
        listHeading.setFont(new Font("SansSerif", Font.BOLD, 18));
        listHeading.setForeground(UITheme.TEXT_DARK);

        JScrollPane scrollPane = new JScrollPane(resList);
        scrollPane.setBorder(new EmptyBorder(10, 0, 0, 0));
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        listCard.add(listHeading, BorderLayout.NORTH);
        listCard.add(scrollPane, BorderLayout.CENTER);

        JPanel detailsCard = buildDetailsCard();
        JScrollPane detailsScrollPane = new JScrollPane(detailsCard);
        detailsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        detailsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        detailsScrollPane.getViewport().setBackground(UITheme.CARD_BACKGROUND);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listCard, detailsScrollPane);
        splitPane.setResizeWeight(0.62);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);
        splitPane.setDividerSize(8);

        content.add(splitPane, BorderLayout.CENTER);
        return content;
    }

    private JPanel buildDetailsCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));

        JLabel heading = new JLabel("Reservation Details");
        heading.setFont(new Font("SansSerif", Font.BOLD, 18));
        heading.setForeground(UITheme.TEXT_DARK);

        card.add(heading);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(createDetailRow("Reservation ID", reservationIdValue));
        card.add(createDetailRow("Status", statusValue));
        card.add(createDetailRow("Guest Email", guestEmailValue));
        card.add(createDetailRow("Stay Dates", stayDatesValue));
        card.add(createDetailRow("Pricing", pricingValue));
        card.add(createDetailRow("Booked On", createdValue));
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel roomsLabel = new JLabel("Room Details");
        roomsLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        roomsLabel.setForeground(UITheme.TEXT_MEDIUM);
        roomsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        roomsValue.setEditable(false);
        roomsValue.setFont(UITheme.INPUT_FONT);
        roomsValue.setForeground(UITheme.TEXT_DARK);
        roomsValue.setBackground(new Color(250, 250, 250));
        roomsValue.setLineWrap(true);
        roomsValue.setWrapStyleWord(true);
        roomsValue.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane roomsScroll = new JScrollPane(roomsValue);
        roomsScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        roomsScroll.setBorder(new LineBorder(UITheme.BORDER_COLOR, 1));
        roomsScroll.setPreferredSize(new Dimension(340, 150));
        roomsScroll.setMinimumSize(new Dimension(220, 120));

        card.add(roomsLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(roomsScroll);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel buildActionBar() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(UITheme.CARD_BACKGROUND);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel helper = new JLabel("Tip: Select a reservation on the left to review details or manage dates.");
        helper.setFont(new Font("SansSerif", Font.PLAIN, 13));
        helper.setForeground(UITheme.TEXT_MEDIUM);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.add(btnCancel);
        actions.add(btnModify);

        bottomPanel.add(helper, BorderLayout.WEST);
        bottomPanel.add(actions, BorderLayout.EAST);
        return bottomPanel;
    }

    private void configureReservationList() {
        resList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resList.setFont(UITheme.INPUT_FONT);
        resList.setBorder(new EmptyBorder(0, 0, 0, 0));
        resList.setBackground(UITheme.PAGE_BACKGROUND);
        resList.setCellRenderer(new ReservationCardRenderer());
        resList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailsPanel();
            }
        });
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(10, 12, 10, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titleLabel.setForeground(UITheme.TEXT_MEDIUM);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(valueLabel);
        return card;
    }

    private JPanel createDetailRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(4, 0, 4, 0));

        JLabel key = new JLabel(label);
        key.setFont(new Font("SansSerif", Font.BOLD, 13));
        key.setForeground(UITheme.TEXT_MEDIUM);

        row.add(key, BorderLayout.NORTH);
        row.add(valueLabel, BorderLayout.CENTER);
        return row;
    }

    private JLabel createMetricValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(UITheme.TEXT_DARK);
        return label;
    }

    private JLabel createDetailValueLabel() {
        JLabel label = new JLabel("-");
        label.setFont(UITheme.INPUT_FONT);
        label.setForeground(UITheme.TEXT_DARK);
        return label;
    }

    public void refresh() {
        listModel.clear();
        var current = AccountController.currentAccount;
        List<Reservation> reservations = List.of();

        if (current != null) {
            if (current.getRole() == Role.CLERK) {
                titleLabel.setText("All Active Reservations");
                subtitleLabel.setText("Track currently open stays and upcoming arrivals.");
                reservations = resService.getAllReservations().stream()
                        .filter(r -> (!"CANCELLED".equalsIgnoreCase(r.getStatus())
                                && !"CHECKED_OUT".equalsIgnoreCase(r.getStatus())))
                        .sorted(Comparator.comparing(Reservation::getStartDate))
                        .toList();
            } else {
                titleLabel.setText("My Reservations");
                subtitleLabel.setText("Review every booking detail and quickly manage upcoming stays.");
                reservations = resService.getGuestReservations(current.getEmail()).stream()
                        .sorted(Comparator.comparing(Reservation::getStartDate))
                        .toList();
            }
        }

        for (Reservation r : reservations) {
            listModel.addElement(r);
        }

        updateMetrics(reservations);

        if (!listModel.isEmpty()) {
            resList.setSelectedIndex(0);
        } else {
            updateDetailsPanel();
        }

        revalidate();
        repaint();
    }

    private void updateMetrics(List<Reservation> reservations) {
        totalReservationsValue.setText(String.valueOf(reservations.size()));

        long activeCount = reservations.stream()
                .filter(this::isActiveReservation)
                .count();
        activeReservationsValue.setText(String.valueOf(activeCount));

        LocalDate today = LocalDate.now();
        reservations.stream()
                .filter(this::isUpcomingReservation)
                .filter(r -> !r.getStartDate().isBefore(today))
                .min(Comparator.comparing(Reservation::getStartDate))
                .ifPresentOrElse(
                        r -> nextCheckInValue.setText(formatDate(r.getStartDate())),
                        () -> nextCheckInValue.setText("--")
                );
    }

    private boolean isActiveReservation(Reservation reservation) {
        return !"CANCELLED".equalsIgnoreCase(reservation.getStatus())
                && !"CHECKED_OUT".equalsIgnoreCase(reservation.getStatus());
    }

    private boolean isUpcomingReservation(Reservation reservation) {
        return isActiveReservation(reservation)
                && !"CHECKED_IN".equalsIgnoreCase(reservation.getStatus());
    }

    private void updateDetailsPanel() {
        Reservation selected = resList.getSelectedValue();
        if (selected == null) {
            reservationIdValue.setText("-");
            statusValue.setText("Select a reservation");
            guestEmailValue.setText("-");
            stayDatesValue.setText("-");
            pricingValue.setText("-");
            createdValue.setText("-");
            roomsValue.setText("Select a reservation from the list to view full room details.");
            btnModify.setEnabled(false);
            btnCancel.setEnabled(false);
            return;
        }

        reservationIdValue.setText("#" + selected.getId());
        statusValue.setText(formatStatus(selected.getStatus()));
        guestEmailValue.setText(selected.getGuestEmail());
        stayDatesValue.setText(formatDate(selected.getStartDate()) + " to " + formatDate(selected.getEndDate()));
        pricingValue.setText("$" + selected.getNightlyRate() + "/night | " + selected.getNights() + " night"
                + (selected.getNights() == 1 ? "" : "s") + " | Total: $" + selected.getTotalCost());
        createdValue.setText(formatDate(selected.getCreatedDate()));
        roomsValue.setText(buildRoomDetails(selected));
        roomsValue.setCaretPosition(0);

        btnModify.setEnabled(canModify(selected));
        btnCancel.setEnabled(canCancel(selected));
    }

    private boolean canModify(Reservation reservation) {
        return !"CANCELLED".equalsIgnoreCase(reservation.getStatus())
                && !"CHECKED_OUT".equalsIgnoreCase(reservation.getStatus());
    }

    private boolean canCancel(Reservation reservation) {
        return !"CANCELLED".equalsIgnoreCase(reservation.getStatus())
                && !"CHECKED_OUT".equalsIgnoreCase(reservation.getStatus())
                && !"CHECKED_IN".equalsIgnoreCase(reservation.getStatus());
    }

    private String buildRoomDetails(Reservation reservation) {
        if (reservation.getRooms() == null || reservation.getRooms().isEmpty()) {
            return "No room details are attached to this reservation.";
        }

        return reservation.getRooms().stream()
                .map(this::describeRoom)
                .collect(Collectors.joining("\n\n"));
    }

    private String describeRoom(Room room) {
        return "Room " + room.getRoomNumber()
                + "\nBed: " + room.getBedType()
                + "\nTheme: " + room.getTheme()
                + "\nQuality: " + room.getQualityLevel()
                + "\nType: " + (room.isSmoking() ? "Smoking" : "Non-smoking")
                + "\nRate: $" + room.getPricePerNight() + " per night";
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
        button.setUI(new GoldActionButtonUI());
        button.setFont(UITheme.BUTTON_FONT);
        button.setForeground(Color.WHITE);
        button.setBackground(UITheme.ACCENT_GOLD);

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);

        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private String formatDate(LocalDate date) {
        return date == null ? "-" : DATE_FORMAT.format(date);
    }

    private String formatStatus(String status) {
        return status == null ? "Unknown" : status.replace('_', ' ');
    }

    private Color statusColor(String status) {
        if (status == null) {
            return new Color(97, 97, 97);
        }

        String normalized = status.toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "BOOKED" -> new Color(56, 113, 182);
            case "CHECKED_IN" -> new Color(43, 128, 78);
            case "CHECKED_OUT" -> new Color(110, 110, 110);
            case "CANCELLED" -> new Color(161, 62, 47);
            default -> new Color(97, 97, 97);
        };
    }

    private class ReservationCardRenderer extends JPanel implements ListCellRenderer<Reservation> {
        private final JLabel idLabel;
        private final JLabel statusBadge;
        private final JLabel datesLabel;
        private final JLabel pricingLabel;
        private final JLabel roomsLabel;

        ReservationCardRenderer() {
            setLayout(new BorderLayout(8, 6));
            setOpaque(true);
            setBorder(new EmptyBorder(10, 12, 10, 12));

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);

            idLabel = new JLabel();
            idLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
            idLabel.setForeground(UITheme.TEXT_DARK);

            statusBadge = new JLabel();
            statusBadge.setOpaque(true);
            statusBadge.setForeground(Color.WHITE);
            statusBadge.setFont(new Font("SansSerif", Font.BOLD, 12));
            statusBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

            topRow.add(idLabel, BorderLayout.WEST);
            topRow.add(statusBadge, BorderLayout.EAST);

            datesLabel = new JLabel();
            datesLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            datesLabel.setForeground(UITheme.TEXT_DARK);

            pricingLabel = new JLabel();
            pricingLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            pricingLabel.setForeground(UITheme.TEXT_MEDIUM);

            roomsLabel = new JLabel();
            roomsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            roomsLabel.setForeground(UITheme.TEXT_MEDIUM);

            JPanel body = new JPanel();
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setOpaque(false);
            body.add(datesLabel);
            body.add(Box.createRigidArea(new Dimension(0, 4)));
            body.add(pricingLabel);
            body.add(Box.createRigidArea(new Dimension(0, 4)));
            body.add(roomsLabel);

            add(topRow, BorderLayout.NORTH);
            add(body, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Reservation> list,
                                                      Reservation value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            if (value == null) {
                return this;
            }

            idLabel.setText("Reservation #" + value.getId());
            statusBadge.setText(formatStatus(value.getStatus()));
            statusBadge.setBackground(statusColor(value.getStatus()));
            datesLabel.setText("Stay: " + formatDate(value.getStartDate()) + " to " + formatDate(value.getEndDate()));
            pricingLabel.setText("Rate: $" + value.getNightlyRate() + "/night | "
                    + value.getNights() + " night" + (value.getNights() == 1 ? "" : "s")
                    + " | Total: $" + value.getTotalCost());
            roomsLabel.setText("Rooms: " + compactRooms(value.getRooms()));

            if (isSelected) {
                setBackground(new Color(243, 235, 218));
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UITheme.ACCENT_GOLD, 2),
                        new EmptyBorder(9, 11, 9, 11)
                ));
            } else {
                setBackground(Color.WHITE);
                setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(UITheme.BORDER_COLOR, 1),
                        new EmptyBorder(10, 12, 10, 12)
                ));
            }

            return this;
        }
    }

    private String compactRooms(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return "No rooms assigned";
        }

        return rooms.stream()
                .map(room -> "#" + room.getRoomNumber() + " (" + room.getTheme() + ")")
                .collect(Collectors.joining(", "));
    }

    private static class GoldActionButtonUI extends BasicButtonUI {
        @Override
        protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            int mnemonicIndex = b.getDisplayedMnemonicIndex();
            BasicGraphicsUtils.drawStringUnderlineCharAt(g, text, mnemonicIndex, textRect.x,
                    textRect.y + fm.getAscent());
        }
    }
}
