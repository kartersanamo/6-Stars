package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Reservation;
import com.sixstars.model.Role;
import com.sixstars.model.Room;
import com.sixstars.model.ShopOrder;
import com.sixstars.model.ShopOrderItem;
import com.sixstars.service.AccountService;
import com.sixstars.service.BillingService;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;
import com.sixstars.database.ShopItemDAO;
import com.sixstars.model.Item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClerkBillingSearchPage extends JPanel {
    private final BillingService billingService;
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final AccountService accountService;
    private final ShopItemDAO shopItemDAO;
    private final JTextField emailField;
    private final JPanel resultsPanel;

    public ClerkBillingSearchPage(JPanel pages, CardLayout cardLayout) {
        this.billingService = new BillingService();
        this.reservationService = new ReservationService();
        this.roomService = new RoomService();
        this.accountService = new AccountService();
        this.shopItemDAO = new ShopItemDAO();
        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        // --- Search Header ---
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PAGE_BACKGROUND);
        header.setBorder(new EmptyBorder(20, 40, 10, 40));

        JLabel title = new JLabel("Billing Search & Hotel Summary");
        title.setFont(UITheme.TITLE_FONT); // Use constant from UITheme
        title.setForeground(UITheme.TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        // Back Button matching CheckInPage's "false" primary style
        JButton btnBack = createThemedButton("Back to Dashboard", false);
        btnBack.addActionListener(e -> {
            Account current = AccountController.currentAccount;
            if (current != null && current.getRole() == Role.ADMIN) {
                cardLayout.show(pages, "admin page");
            } else {
                cardLayout.show(pages, "clerk page");
            }
        });
        header.add(btnBack, BorderLayout.EAST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(UITheme.PAGE_BACKGROUND);
        searchPanel.setBorder(new EmptyBorder(10, 40, 10, 40));

        JLabel searchLabel = new JLabel("Guest Email: ");
        searchLabel.setFont(UITheme.LABEL_FONT);
        searchPanel.add(searchLabel);

        emailField = new JTextField(20);
        emailField.setFont(UITheme.INPUT_FONT);
        searchPanel.add(emailField);

        JButton btnSearch = createThemedButton("Generate Bill", true);
        btnSearch.addActionListener(e -> performSearch(emailField.getText().trim()));
        this.addHierarchyListener(e -> {
            emailField.requestFocusInWindow();
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                javax.swing.JRootPane root = javax.swing.SwingUtilities.getRootPane(this);
                if (root != null) {
                    root.setDefaultButton(btnSearch);
                }
            }
        });
        searchPanel.add(btnSearch);

        JButton btnSummary = createThemedButton("Hotel Summary", false);
        btnSummary.addActionListener(e -> showHotelSummary());
        searchPanel.add(btnSummary);

        // --- Results Area ---
        resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setOpaque(false);
        resultsPanel.setBorder(new EmptyBorder(10, 40, 30, 40));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);
        topContainer.add(header);
        topContainer.add(searchPanel);

        add(topContainer, BorderLayout.NORTH);
        add(resultsPanel, BorderLayout.CENTER);
    }

    private void performSearch(String email) {
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a guest email.");
            return;
        }

        resultsPanel.removeAll();
        JPanel billingContent = createExternalBillingView(email);
        resultsPanel.add(billingContent, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private JPanel createExternalBillingView(String email) {
        BillingPage customView = new BillingPage();
        customView.refreshForEmail(email);

        return customView;
    }

    private void showHotelSummary() {
        resultsPanel.removeAll();
        JPanel summaryCard = createHotelSummaryCard();
        JScrollPane summaryScrollPane = new JScrollPane(summaryCard);
        summaryScrollPane.setBorder(BorderFactory.createEmptyBorder());
        summaryScrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        summaryScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        resultsPanel.add(summaryScrollPane, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private JPanel createHotelSummaryCard() {
        HotelMetrics metrics = collectHotelMetrics();

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(22, 22, 22, 22)
        ));

        JLabel title = new JLabel("Hotel Financial Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Snapshot of reservations, payments, rooms, guests, and operations.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.TEXT_MEDIUM);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel overall = new JLabel(String.format("Combined Revenue: $%.2f", metrics.grandTotal));
        overall.setFont(new Font("SansSerif", Font.BOLD, 22));
        overall.setForeground(new Color(176, 132, 38));
        overall.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        card.add(overall);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel financialRow = createMetricsRow(
                createStatCard("Reservation Revenue", String.format("$%d.00", metrics.reservationRevenue), "Room bookings and penalties"),
                createStatCard("Shop Revenue", String.format("$%.2f", metrics.shopRevenue), "All in-hotel product sales"),
                createStatCard("Avg Revenue / Reservation", String.format("$%.2f", metrics.avgReservationValue), "Across all reservation records")
        );
        financialRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(financialRow);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel operationsRow = createMetricsRow(
                createStatCard("Total Rooms", String.valueOf(metrics.totalRooms), "Hotel inventory"),
                createStatCard("Occupied Today", String.format("%d (%.1f%%)", metrics.occupiedRooms, metrics.occupancyRate), "Checked-in and in-stay now"),
                createStatCard("Vacant Today", String.valueOf(metrics.vacantRooms), "Ready or unbooked rooms")
        );
        operationsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(operationsRow);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel reservationsRow = createMetricsRow(
                createStatCard("Reservations Tracked", String.valueOf(metrics.totalReservations), "All statuses"),
                createStatCard("Checked In", String.valueOf(metrics.checkedInCount), "Currently marked CHECKED_IN"),
                createStatCard("Check-ins Today", String.valueOf(metrics.checkInsToday), "Start date is today")
        );
        reservationsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(reservationsRow);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel shopRow = createMetricsRow(
                createStatCard("Shop Orders", String.valueOf(metrics.totalShopOrders), "Payments recorded"),
                createStatCard("Units Sold", String.valueOf(metrics.totalShopUnitsSold), "Total quantities from orders"),
                createStatCard("Inventory Units", String.valueOf(metrics.totalInventoryUnits), "Current stock on hand")
        );
        shopRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(shopRow);
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel detailsRow = new JPanel(new GridLayout(1, 2, 12, 12));
        detailsRow.setOpaque(false);
        detailsRow.add(createBreakdownPanel("Account Mix", metrics.accountBreakdownLines));
        detailsRow.add(createBreakdownPanel("Reservation Status Mix", metrics.reservationStatusLines));
        detailsRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(detailsRow);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel detailsRow2 = new JPanel(new GridLayout(1, 2, 12, 12));
        detailsRow2.setOpaque(false);
        detailsRow2.add(createBreakdownPanel("Room Quality Mix", metrics.roomQualityLines));
        detailsRow2.add(createBreakdownPanel("Top Selling Shop Items", metrics.topSellingItemsLines));
        detailsRow2.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(detailsRow2);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel detailsRow3 = new JPanel(new GridLayout(1, 2, 12, 12));
        detailsRow3.setOpaque(false);
        detailsRow3.add(createBreakdownPanel("Top Guests by Spend", metrics.topGuestLines));
        detailsRow3.add(createBreakdownPanel("Operational Highlights", metrics.highlightsLines));
        detailsRow3.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(detailsRow3);

        return card;
    }

    private HotelMetrics collectHotelMetrics() {
        HotelMetrics metrics = new HotelMetrics();
        LocalDate today = LocalDate.now();

        List<Reservation> reservations = billingService.getAllReservationCharges();
        List<ShopOrder> allOrders = billingService.getAllShopPurchases();
        List<Room> rooms = roomService.getAllRooms();
        List<Account> accounts = accountService.getAllAccounts();
        List<Item> items = shopItemDAO.getAllItems();

        metrics.totalReservations = reservations.size();
        metrics.reservationRevenue = billingService.getHotelReservationTotal();
        metrics.shopRevenue = billingService.getHotelShopTotal();
        metrics.grandTotal = billingService.getHotelGrandTotal();
        metrics.avgReservationValue = metrics.totalReservations == 0
                ? 0
                : (double) metrics.reservationRevenue / metrics.totalReservations;

        metrics.totalShopOrders = allOrders.size();
        metrics.totalRooms = rooms.size();
        metrics.totalInventoryUnits = items.stream().mapToInt(Item::getStock).sum();
        metrics.vacantRooms = metrics.totalRooms;

        EnumMap<Role, Integer> roleCount = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            roleCount.put(role, 0);
        }
        for (Account account : accounts) {
            roleCount.put(account.getRole(), roleCount.get(account.getRole()) + 1);
        }
        metrics.accountBreakdownLines.add(String.format("Guests: %d", roleCount.get(Role.GUEST)));
        metrics.accountBreakdownLines.add(String.format("Clerks: %d", roleCount.get(Role.CLERK)));
        metrics.accountBreakdownLines.add(String.format("Admins: %d", roleCount.get(Role.ADMIN)));
        metrics.accountBreakdownLines.add(String.format("Total Accounts: %d", accounts.size()));

        Map<String, Integer> statusCount = new HashMap<>();
        int checkedInRooms = 0;
        for (Reservation reservation : reservations) {
            String status = reservation.getStatus() == null ? "UNKNOWN" : reservation.getStatus().toUpperCase();
            statusCount.put(status, statusCount.getOrDefault(status, 0) + 1);

            boolean isInStayWindow = !today.isBefore(reservation.getStartDate()) && today.isBefore(reservation.getEndDate());
            if ("CHECKED_IN".equals(status)) {
                metrics.checkedInCount++;
                if (isInStayWindow) {
                    checkedInRooms += reservation.getRooms().size();
                }
            }
            if (today.equals(reservation.getStartDate())) {
                metrics.checkInsToday++;
            }
            if (today.equals(reservation.getEndDate())) {
                metrics.checkOutsToday++;
            }
        }

        metrics.occupiedRooms = Math.min(checkedInRooms, metrics.totalRooms);
        metrics.vacantRooms = Math.max(0, metrics.totalRooms - metrics.occupiedRooms);
        metrics.occupancyRate = metrics.totalRooms == 0 ? 0 : (metrics.occupiedRooms * 100.0 / metrics.totalRooms);

        statusCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> metrics.reservationStatusLines.add(e.getKey() + ": " + e.getValue()));

        Map<String, Integer> qualityCount = new HashMap<>();
        int smokingRooms = 0;
        int totalRoomRate = 0;
        for (Room room : rooms) {
            String quality = room.getQualityLevel().name();
            qualityCount.put(quality, qualityCount.getOrDefault(quality, 0) + 1);
            if (room.isSmoking()) {
                smokingRooms++;
            }
            totalRoomRate += room.getPricePerNight();
        }
        qualityCount.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> metrics.roomQualityLines.add(e.getKey() + ": " + e.getValue()));
        double avgRoomRate = rooms.isEmpty() ? 0 : (double) totalRoomRate / rooms.size();
        metrics.highlightsLines.add(String.format("Average room nightly rate: $%.2f", avgRoomRate));
        metrics.highlightsLines.add("Smoking rooms: " + smokingRooms);
        metrics.highlightsLines.add("Non-smoking rooms: " + (metrics.totalRooms - smokingRooms));
        metrics.highlightsLines.add("Check-outs today: " + metrics.checkOutsToday);

        Map<String, Integer> itemUnits = new HashMap<>();
        Map<String, Double> itemRevenue = new HashMap<>();
        metrics.totalShopUnitsSold = 0;
        for (ShopOrder order : allOrders) {
            for (ShopOrderItem orderItem : order.getItems()) {
                metrics.totalShopUnitsSold += orderItem.getQuantity();
                itemUnits.put(orderItem.getItemName(), itemUnits.getOrDefault(orderItem.getItemName(), 0) + orderItem.getQuantity());
                itemRevenue.put(orderItem.getItemName(), itemRevenue.getOrDefault(orderItem.getItemName(), 0.0) + orderItem.getLineTotal());
            }
        }

        itemUnits.entrySet().stream()
                .sorted((a, b) -> Integer.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(e -> {
                    double revenue = itemRevenue.getOrDefault(e.getKey(), 0.0);
                    metrics.topSellingItemsLines.add(String.format("%s - %d sold ($%.2f)", e.getKey(), e.getValue(), revenue));
                });
        if (metrics.topSellingItemsLines.isEmpty()) {
            metrics.topSellingItemsLines.add("No shop sales recorded yet.");
        }

        int lowStockItems = 0;
        double inventoryValue = 0;
        for (Item item : items) {
            if (item.getStock() <= 5) {
                lowStockItems++;
            }
            inventoryValue += item.getStock() * item.getPrice();
        }
        metrics.highlightsLines.add(String.format("Current shop inventory value: $%.2f", inventoryValue));
        metrics.highlightsLines.add("Low-stock shop items (<= 5): " + lowStockItems);

        Map<String, Double> guestSpend = new HashMap<>();
        for (Reservation reservation : reservations) {
            guestSpend.put(
                    reservation.getGuestEmail(),
                    guestSpend.getOrDefault(reservation.getGuestEmail(), 0.0) + reservation.getTotalCost()
            );
        }
        for (ShopOrder order : allOrders) {
            guestSpend.put(
                    order.getGuestEmail(),
                    guestSpend.getOrDefault(order.getGuestEmail(), 0.0) + order.getTotalCost()
            );
        }
        guestSpend.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .forEach(e -> metrics.topGuestLines.add(String.format("%s - $%.2f", e.getKey(), e.getValue())));
        if (metrics.topGuestLines.isEmpty()) {
            metrics.topGuestLines.add("No guest spend data available.");
        }

        return metrics;
    }

    private JPanel createMetricsRow(JPanel left, JPanel middle, JPanel right) {
        JPanel row = new JPanel(new GridLayout(1, 3, 12, 12));
        row.setOpaque(false);
        row.add(left);
        row.add(middle);
        row.add(right);
        return row;
    }

    private JPanel createStatCard(String label, String value, String context) {
        JPanel stat = new JPanel();
        stat.setLayout(new BoxLayout(stat, BoxLayout.Y_AXIS));
        stat.setBackground(new Color(250, 250, 250));
        stat.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel labelText = new JLabel(label);
        labelText.setFont(new Font("SansSerif", Font.BOLD, 13));
        labelText.setForeground(UITheme.TEXT_MEDIUM);
        labelText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel valueText = new JLabel(value);
        valueText.setFont(new Font("SansSerif", Font.BOLD, 21));
        valueText.setForeground(UITheme.TEXT_DARK);
        valueText.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel contextText = new JLabel(context);
        contextText.setFont(new Font("SansSerif", Font.PLAIN, 12));
        contextText.setForeground(UITheme.TEXT_MEDIUM);
        contextText.setAlignmentX(Component.LEFT_ALIGNMENT);

        stat.add(labelText);
        stat.add(Box.createRigidArea(new Dimension(0, 5)));
        stat.add(valueText);
        stat.add(Box.createRigidArea(new Dimension(0, 3)));
        stat.add(contextText);
        return stat;
    }

    private JPanel createBreakdownPanel(String title, List<String> lines) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(250, 250, 250));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel heading = new JLabel(title);
        heading.setFont(new Font("SansSerif", Font.BOLD, 15));
        heading.setForeground(UITheme.TEXT_DARK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(heading);
        panel.add(Box.createRigidArea(new Dimension(0, 8)));

        if (lines.isEmpty()) {
            JLabel empty = new JLabel("No data available.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
            empty.setForeground(UITheme.TEXT_MEDIUM);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(empty);
            return panel;
        }

        for (String line : lines) {
            JLabel row = new JLabel(line);
            row.setFont(new Font("SansSerif", Font.PLAIN, 13));
            row.setForeground(UITheme.TEXT_MEDIUM);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(row);
            panel.add(Box.createRigidArea(new Dimension(0, 4)));
        }
        return panel;
    }

    private static class HotelMetrics {
        int totalReservations;
        int reservationRevenue;
        double shopRevenue;
        double grandTotal;
        double avgReservationValue;
        int totalShopOrders;
        int totalShopUnitsSold;
        int totalInventoryUnits;
        int totalRooms;
        int occupiedRooms;
        int vacantRooms;
        double occupancyRate;
        int checkedInCount;
        int checkInsToday;
        int checkOutsToday;
        List<String> accountBreakdownLines = new ArrayList<>();
        List<String> reservationStatusLines = new ArrayList<>();
        List<String> roomQualityLines = new ArrayList<>();
        List<String> topSellingItemsLines = new ArrayList<>();
        List<String> topGuestLines = new ArrayList<>();
        List<String> highlightsLines = new ArrayList<>();
    }

    private JButton createThemedButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(UITheme.BUTTON_FONT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 40));

        if (isPrimary) {
            button.setBackground(UITheme.ACCENT_GOLD);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(UITheme.SECONDARY_BUTTON);
            button.setForeground(UITheme.TEXT_DARK);
        }
        button.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        button.setOpaque(true);
        return button;
    }
}