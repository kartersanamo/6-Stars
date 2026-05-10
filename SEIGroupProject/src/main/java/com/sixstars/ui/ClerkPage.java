package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Item;
import com.sixstars.model.Reservation;
import com.sixstars.model.Role;
import com.sixstars.model.Room;
import com.sixstars.service.AccountService;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;
import com.sixstars.service.ShopService;

/**
 * Front-office console for clerks: live property metrics, operational shortcuts, arrivals / in-house lists,
 * inventory watch, night-audit helpers, and quick guest folio access.
 */
public class ClerkPage extends JPanel {

    private static final Color HEADER_BG = new Color(52, 42, 32);
    private static final Color HEADER_FG = new Color(255, 252, 245);
    private static final Color STAT_CARD_BG = new Color(255, 253, 248);
    private static final Color TILE_BG = new Color(252, 249, 242);
    private static final Color TILE_HOVER = new Color(242, 234, 218);
    private static final Color TILE_BORDER = new Color(210, 198, 178);
    private static final Color ACCENT_LINE = new Color(176, 132, 38);
    private static final DateTimeFormatter CLOCK_FMT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy · h:mm a", Locale.US);

    private final JPanel pages;
    private final CardLayout cardLayout;
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final ShopService shopService = new ShopService();
    private final AccountService accountService = new AccountService();

    private final JLabel statReservations = new JLabel("—");
    private final JLabel statInHouse = new JLabel("—");
    private final JLabel statArrivals = new JLabel("—");
    private final JLabel statOccupiedRooms = new JLabel("—");
    private final JLabel statLowStock = new JLabel("—");
    private final JLabel statGuestAccounts = new JLabel("—");
    private final JLabel clockLabel = new JLabel(" ");
    private final JLabel lastRefreshLabel = new JLabel(" ");
    private final JPanel arrivalsInner = new JPanel();
    private final JPanel inHouseInner = new JPanel();
    private final JPanel inventoryInner = new JPanel();
    private final JTextField quickFolioField = new JTextField(28);

    public ClerkPage(JPanel pages, CardLayout cardLayout,
            ReservationService reservationService, RoomService roomService) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.reservationService = reservationService;
        this.roomService = roomService;

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildScrollBody(), BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent e) {
                refreshDashboard();
            }
        });

        Timer clock = new Timer(30_000, _ -> clockLabel.setText(LocalDateTime.now().format(CLOCK_FMT)));
        clock.setInitialDelay(0);
        clock.start();
    }

    private JPanel buildHeader() {
        JPanel bar = new JPanel(new BorderLayout(24, 0));
        bar.setOpaque(true);
        bar.setBackground(HEADER_BG);
        bar.setBorder(new EmptyBorder(22, 32, 22, 32));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel kicker = new JLabel("SIX STARS · FRONT OFFICE");
        kicker.setFont(new Font("SansSerif", Font.BOLD, 11));
        kicker.setForeground(new Color(210, 190, 155));
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Operations console");
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(HEADER_FG);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Reservations, arrivals, folios, rooms, and retail — one place for the shift.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(new Color(230, 220, 205));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(kicker);
        left.add(Box.createRigidArea(new Dimension(0, 6)));
        left.add(title);
        left.add(Box.createRigidArea(new Dimension(0, 6)));
        left.add(sub);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        clockLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        clockLabel.setForeground(new Color(240, 232, 218));
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        clockLabel.setText(LocalDateTime.now().format(CLOCK_FMT));
        JLabel role = new JLabel(roleLine());
        role.setFont(new Font("SansSerif", Font.PLAIN, 13));
        role.setForeground(new Color(210, 200, 185));
        role.setHorizontalAlignment(SwingConstants.RIGHT);
        role.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(clockLabel);
        right.add(Box.createRigidArea(new Dimension(0, 8)));
        right.add(role);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private String roleLine() {
        Account a = AccountController.currentAccount;
        if (a == null) {
            return "Not signed in";
        }
        return a.getFirstName() + " " + a.getLastName() + " · " + formatRole(a.getRole().name());
    }

    private static String formatRole(String raw) {
        if (raw == null) {
            return "";
        }
        String lower = raw.toLowerCase(Locale.ROOT).replace('_', ' ');
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private JScrollPane buildScrollBody() {
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(true);
        body.setBackground(UITheme.PAGE_BACKGROUND);
        // Match header horizontal inset so hero and body align; avoid extra-wide min width.
        body.setBorder(new EmptyBorder(24, 32, 40, 32));

        body.add(sectionTitle("Live property pulse", "Refreshes when you return to this screen or tap Refresh."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(buildStatsStrip());
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        body.add(lastRefreshLabel);
        lastRefreshLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lastRefreshLabel.setForeground(UITheme.TEXT_MEDIUM);
        lastRefreshLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createRigidArea(new Dimension(0, 22)));

        body.add(sectionTitle("Quick guest folio", "Jump straight to billing search with a guest email."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(buildQuickFolioRow());
        body.add(Box.createRigidArea(new Dimension(0, 28)));

        body.add(sectionTitle("Shift tools", "High-impact workflows your team uses every day."));
        body.add(Box.createRigidArea(new Dimension(0, 12)));
        body.add(buildActionGrid());
        body.add(Box.createRigidArea(new Dimension(0, 28)));

        body.add(sectionTitle("Today's arrivals", "Booked guests with check-in today."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(wrapScroll(arrivalsInner, 200));
        body.add(Box.createRigidArea(new Dimension(0, 24)));

        body.add(sectionTitle("In-house register", "Guests currently checked in."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(wrapScroll(inHouseInner, 200));
        body.add(Box.createRigidArea(new Dimension(0, 24)));

        body.add(sectionTitle("Retail inventory watch", "SKUs at or below eight units — restock before the shop runs dry."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(wrapScroll(inventoryInner, 180));
        body.add(Box.createRigidArea(new Dimension(0, 24)));

        body.add(sectionTitle("Briefing & audit", "Reference material and end-of-shift hygiene."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(buildBriefingRow());
        body.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(body);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        return scroll;
    }

    private JPanel sectionTitle(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel t = new JLabel(title);
        t.setFont(new Font("Serif", Font.BOLD, 22));
        t.setForeground(UITheme.TEXT_DARK);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Plain label avoids fixed HTML widths (e.g. 880px) that forced horizontal scroll.
        JLabel s = new JLabel(subtitle);
        s.setFont(UITheme.SUBTITLE_FONT);
        s.setForeground(UITheme.TEXT_MEDIUM);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createRigidArea(new Dimension(0, 4)));
        p.add(s);
        return p;
    }

    private JPanel buildStatsStrip() {
        // Two columns so KPI cards stack into extra rows instead of forcing horizontal scroll.
        JPanel row = new JPanel(new GridLayout(0, 2, 14, 14));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        row.add(statCard("Active reservations", statReservations, new Color(56, 113, 182)));
        row.add(statCard("Guests in-house", statInHouse, new Color(43, 128, 78)));
        row.add(statCard("Arrivals today", statArrivals, new Color(176, 132, 38)));
        row.add(statCard("Rooms occupied", statOccupiedRooms, new Color(120, 72, 156)));
        row.add(statCard("Low-stock SKUs", statLowStock, new Color(184, 92, 56)));
        row.add(statCard("Guest profiles", statGuestAccounts, new Color(72, 110, 120)));
        return row;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(STAT_CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TILE_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)));
        JLabel t = new JLabel(title.toUpperCase(Locale.US));
        t.setFont(new Font("SansSerif", Font.BOLD, 10));
        t.setForeground(UITheme.TEXT_MEDIUM);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        valueLabel.setForeground(UITheme.TEXT_DARK);
        JPanel topRule = new JPanel();
        topRule.setPreferredSize(new Dimension(10, 3));
        topRule.setBackground(accent);
        topRule.setOpaque(true);
        card.add(topRule, BorderLayout.NORTH);
        card.add(t, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildQuickFolioRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        quickFolioField.setFont(UITheme.INPUT_FONT);
        quickFolioField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.FIELD_BORDER, 1, true),
                new EmptyBorder(8, 12, 8, 12)));
        JButton go = new JButton("Open folio");
        stylePrimary(go);
        go.addActionListener(_ -> openQuickFolio());
        JButton clear = new JButton("Clear");
        styleGhost(clear);
        clear.addActionListener(_ -> quickFolioField.setText(""));
        row.add(quickFolioField);
        row.add(go);
        row.add(clear);
        return row;
    }

    private void openQuickFolio() {
        String email = quickFolioField.getText().trim();
        if (email.isBlank()) {
            JOptionPane.showMessageDialog(this, "Enter a guest email to open their folio.", "Quick folio",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (Main.clerkBillingSearchPage != null) {
            Main.clerkBillingSearchPage.openGuestFolio(email);
        }
        cardLayout.show(pages, "clerk billing");
    }

    private JPanel buildActionGrid() {
        // Two columns × five rows (nine tiles): avoids a three-column strip wider than the viewport.
        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 16));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 720));

        grid.add(actionTile("✦", "New guest reservation", "Book on behalf of a guest with clerk tools.",
                () -> {
                    Main.makeReservationPage.refreshPage();
                    cardLayout.show(pages, "make reservation");
                }));
        grid.add(actionTile("≡", "Reservation desk", "Search, modify, or cancel upcoming stays.",
                () -> {
                    Main.reservationsPage.refresh();
                    cardLayout.show(pages, "reservations");
                }));
        grid.add(actionTile("✓", "Check-in & check-out", "Front desk arrivals, departures, and status.",
                () -> cardLayout.show(pages, "check in")));
        grid.add(actionTile("⌂", "Room inventory", "Themes, quality tiers, smoking flags, and availability.",
                () -> cardLayout.show(pages, "room management")));
        grid.add(actionTile("⌁", "Billing & folio lookup", "Per-guest charges, shop orders, and hotel summary.",
                () -> cardLayout.show(pages, "clerk billing")));
        grid.add(actionTile("☕", "Hotel shop (view)", "Review catalog and pricing — purchases remain guest-only.",
                () -> {
                    Main.shopPage.refreshInventory();
                    cardLayout.show(pages, "shop");
                }));
        grid.add(actionTile("●", "My staff account", "Profile, password, billing, and preferences.",
                () -> {
                    Main.accountCenterPage.refreshInfo();
                    cardLayout.show(pages, "account center");
                }));
        grid.add(actionTile("◷", "Night audit snapshot", "One-click counts for handoff to the next shift.",
                this::showNightAuditSnapshot));
        grid.add(actionTile("↻", "Run auto check-outs", "Close folios for departures past end date (system rule).",
                this::runAutoCheckOuts));
        grid.add(actionTile("⚑", "Hotel directory", "Extensions, hours, and emergency quick reference.",
                this::showHotelDirectory));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.add(grid, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel actionTile(String glyph, String title, String blurb, Runnable action) {
        JPanel tile = new JPanel(new BorderLayout(0, 8));
        tile.setOpaque(true);
        tile.setBackground(TILE_BG);
        tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TILE_BORDER, 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        tile.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel icon = new JLabel(glyph, SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 28));
        icon.setForeground(ACCENT_LINE);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 15));
        t.setForeground(UITheme.TEXT_DARK);
        JLabel b = new JLabel("<html><body style=\"margin:0\">" + blurb + "</body></html>");
        b.setFont(new Font("SansSerif", Font.PLAIN, 12));
        b.setForeground(UITheme.TEXT_MEDIUM);
        text.add(t);
        text.add(Box.createRigidArea(new Dimension(0, 6)));
        text.add(b);

        tile.add(icon, BorderLayout.WEST);
        tile.add(text, BorderLayout.CENTER);

        Color normal = TILE_BG;
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tile.setBackground(TILE_HOVER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tile.setBackground(normal);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        };
        tile.addMouseListener(ma);
        for (Component c : tile.getComponents()) {
            c.addMouseListener(ma);
        }
        if (text.getComponentCount() > 0) {
            for (Component c : text.getComponents()) {
                c.addMouseListener(ma);
            }
        }
        return tile;
    }

    private JPanel buildBriefingRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton refresh = new JButton("Refresh dashboard");
        stylePrimary(refresh);
        refresh.addActionListener(_ -> refreshDashboard());
        JButton copy = new JButton("Copy shift briefing");
        styleGhost(copy);
        copy.addActionListener(_ -> copyBriefingToClipboard());
        JButton home = new JButton("Guest home (preview)");
        styleGhost(home);
        home.addActionListener(_ -> cardLayout.show(pages, "home"));
        JButton logout = new JButton("Sign out");
        styleDanger(logout);
        logout.addActionListener(_ -> {
            AccountController.currentAccount = null;
            Main.headerBar.refreshInfo();
            cardLayout.show(pages, "home");
        });
        row.add(refresh);
        row.add(copy);
        row.add(home);
        row.add(logout);
        return row;
    }

    private JScrollPane wrapScroll(JPanel inner, int maxHeight) {
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(true);
        inner.setBackground(Color.WHITE);
        inner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TILE_BORDER, 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        JScrollPane sp = new JScrollPane(inner);
        sp.setBorder(BorderFactory.createLineBorder(TILE_BORDER, 1, true));
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setMaximumSize(new Dimension(Integer.MAX_VALUE, maxHeight));
        sp.getViewport().setBackground(Color.WHITE);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    public void refreshDashboard() {
        LocalDate today = LocalDate.now();
        List<Reservation> all = reservationService.getAllReservations();
        List<Room> rooms = roomService.getAllRooms();

        long activeRes = all.stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()))
                .count();
        statReservations.setText(String.valueOf(activeRes));

        long inHouse = all.stream()
                .filter(r -> "CHECKED_IN".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !today.isBefore(r.getStartDate()) && today.isBefore(r.getEndDate()))
                .count();
        statInHouse.setText(String.valueOf(inHouse));

        long arrivals = all.stream()
                .filter(r -> "BOOKED".equalsIgnoreCase(r.getStatus()) && today.equals(r.getStartDate()))
                .count();
        statArrivals.setText(String.valueOf(arrivals));

        Set<Integer> occRooms = new HashSet<>();
        for (Reservation r : all) {
            if (!"CHECKED_IN".equalsIgnoreCase(r.getStatus())) {
                continue;
            }
            if (!today.isBefore(r.getStartDate()) && today.isBefore(r.getEndDate()) && r.getRooms() != null) {
                for (Room room : r.getRooms()) {
                    occRooms.add(room.getRoomNumber());
                }
            }
        }
        statOccupiedRooms.setText(occRooms.size() + " / " + rooms.size());

        List<Item> inv = shopService.getInventory();
        long low = inv.stream().filter(i -> i.getStock() <= 8).count();
        statLowStock.setText(String.valueOf(low));

        long guests = accountService.getAllAccounts().stream().filter(a -> a.getRole() == Role.GUEST).count();
        statGuestAccounts.setText(String.valueOf(guests));

        fillArrivals(all, today);
        fillInHouse(all, today);
        fillInventory(inv);

        lastRefreshLabel.setText("Last refreshed: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("h:mm a", Locale.US)));
        revalidate();
        repaint();
    }

    private void fillArrivals(List<Reservation> all, LocalDate today) {
        arrivalsInner.removeAll();
        List<Reservation> list = all.stream()
                .filter(r -> "BOOKED".equalsIgnoreCase(r.getStatus()) && today.equals(r.getStartDate()))
                .sorted(Comparator.comparing(Reservation::getGuestEmail))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            arrivalsInner.add(hintRow("No booked arrivals dated today — great moment to prep rooms early."));
        } else {
            for (Reservation r : list) {
                arrivalsInner.add(reservationRow(r, "Arrival"));
                arrivalsInner.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        arrivalsInner.revalidate();
        arrivalsInner.repaint();
    }

    private void fillInHouse(List<Reservation> all, LocalDate today) {
        inHouseInner.removeAll();
        List<Reservation> list = all.stream()
                .filter(r -> "CHECKED_IN".equalsIgnoreCase(r.getStatus()))
                .filter(r -> !today.isBefore(r.getStartDate()) && today.isBefore(r.getEndDate()))
                .sorted(Comparator.comparing(Reservation::getGuestEmail))
                .collect(Collectors.toList());
        if (list.isEmpty()) {
            inHouseInner.add(hintRow("No in-house guests right now — check-ins will appear here after you confirm."));
        } else {
            for (Reservation r : list) {
                inHouseInner.add(reservationRow(r, "In-house"));
                inHouseInner.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        inHouseInner.revalidate();
        inHouseInner.repaint();
    }

    private void fillInventory(List<Item> inv) {
        inventoryInner.removeAll();
        List<Item> low = inv.stream()
                .filter(i -> i.getStock() <= 8)
                .sorted(Comparator.comparingInt(Item::getStock))
                .collect(Collectors.toList());
        if (low.isEmpty()) {
            inventoryInner.add(hintRow("All SKUs above eight units — inventory looks healthy."));
        } else {
            int n = Math.min(12, low.size());
            for (int i = 0; i < n; i++) {
                inventoryInner.add(itemRow(low.get(i)));
                inventoryInner.add(Box.createRigidArea(new Dimension(0, 6)));
            }
        }
        inventoryInner.revalidate();
        inventoryInner.repaint();
    }

    private JPanel hintRow(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel("<html><body style=\"margin:0\">" + text + "</body></html>");
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(UITheme.TEXT_MEDIUM);
        p.add(l, BorderLayout.WEST);
        return p;
    }

    private JPanel reservationRow(Reservation r, String badge) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(true);
        row.setBackground(new Color(250, 248, 244));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 218, 200), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        String rooms = r.getRooms() == null ? "—" : r.getRooms().stream()
                .map(x -> "Rm " + x.getRoomNumber())
                .collect(Collectors.joining(", "));
        JLabel left = new JLabel("<html><div style=\"font-size:13px\"><b>" + r.getGuestEmail() + "</b><br/>"
                + "<span style=\"color:#666;\">" + rooms + " · " + r.getStartDate() + " → " + r.getEndDate()
                + " · $" + r.getTotalCost() + "</span></div></html>");
        JLabel tag = new JLabel(badge, SwingConstants.CENTER);
        tag.setOpaque(true);
        tag.setFont(new Font("SansSerif", Font.BOLD, 11));
        tag.setBorder(new EmptyBorder(4, 10, 4, 10));
        tag.setBackground("Arrival".equals(badge)
                ? new Color(234, 240, 255)
                : new Color(231, 246, 236));
        tag.setForeground(UITheme.TEXT_DARK);
        JButton open = new JButton("Folio");
        styleGhost(open);
        open.addActionListener(_ -> {
            quickFolioField.setText(r.getGuestEmail());
            openQuickFolio();
        });
        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        east.setOpaque(false);
        east.add(tag);
        east.add(open);
        row.add(left, BorderLayout.CENTER);
        row.add(east, BorderLayout.EAST);
        return row;
    }

    private JPanel itemRow(Item it) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel left = new JLabel("<html><b>" + it.getName() + "</b><br/><span style=\"color:#666;\">$"
                + String.format(Locale.US, "%.2f", it.getPrice()) + " each</span></html>");
        JLabel qty = new JLabel("Stock: " + it.getStock(), SwingConstants.RIGHT);
        qty.setFont(new Font("SansSerif", Font.BOLD, 13));
        qty.setForeground(it.getStock() <= 3 ? new Color(170, 50, 40) : UITheme.TEXT_DARK);
        row.add(left, BorderLayout.CENTER);
        row.add(qty, BorderLayout.EAST);
        return row;
    }

    private void showNightAuditSnapshot() {
        LocalDate today = LocalDate.now();
        List<Reservation> all = reservationService.getAllReservations();
        long booked = all.stream().filter(r -> "BOOKED".equalsIgnoreCase(r.getStatus())).count();
        long checkedIn = all.stream().filter(r -> "CHECKED_IN".equalsIgnoreCase(r.getStatus())).count();
        long checkedOut = all.stream().filter(r -> "CHECKED_OUT".equalsIgnoreCase(r.getStatus())).count();
        long cancelled = all.stream().filter(r -> "CANCELLED".equalsIgnoreCase(r.getStatus())).count();
        int rooms = roomService.getAllRooms().size();
        String msg = "Date: " + today + "\n\n"
                + "Reservation statuses\n"
                + "  Booked: " + booked + "\n"
                + "  Checked in: " + checkedIn + "\n"
                + "  Checked out: " + checkedOut + "\n"
                + "  Cancelled: " + cancelled + "\n\n"
                + "Room inventory: " + rooms + " keys in system\n"
                + "Guest profiles: " + accountService.getAllAccounts().stream().filter(a -> a.getRole() == Role.GUEST).count() + "\n\n"
                + "Use this snapshot when handing off to the next shift or reconciling with night audit.";
        JOptionPane.showMessageDialog(this, msg, "Night audit snapshot", JOptionPane.INFORMATION_MESSAGE);
    }

    private void runAutoCheckOuts() {
        reservationService.processAutomaticCheckOuts();
        refreshDashboard();
        JOptionPane.showMessageDialog(this,
                "Automatic check-out rules were applied for stays past their departure date.\n"
                        + "Guests with updated folios may receive confirmation notices.",
                "Auto check-outs",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showHotelDirectory() {
        String msg = """
                SIX STARS HOTEL — FRONT DESK DIRECTORY

                Front desk: 7000 (internal) · +1 (254) 555-0142
                Concierge: 7010
                Housekeeping dispatch: 7020
                Engineering / maintenance: 7030
                Security: 7099

                Breakfast lounge · 6:30 a.m. – 10:30 a.m.
                Pool & fitness · 6:00 a.m. – 10:00 p.m.
                Business center · Keycard access at mezzanine

                Emergency: 9-1-1 from house phones · Hotel muster: lobby fountain

                This reference is for training and demonstration.""";
        JOptionPane.showMessageDialog(this, msg, "Hotel directory", JOptionPane.INFORMATION_MESSAGE);
    }

    private void copyBriefingToClipboard() {
        LocalDate today = LocalDate.now();
        List<Reservation> all = reservationService.getAllReservations();
        StringBuilder sb = new StringBuilder();
        sb.append("Six Stars Hotel — shift briefing\n");
        sb.append("Generated ").append(LocalDateTime.now()).append("\n\n");
        sb.append("Active reservations: ").append(all.stream().filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus())).count()).append("\n");
        sb.append("In-house (status): ").append(all.stream().filter(r -> "CHECKED_IN".equalsIgnoreCase(r.getStatus())).count()).append("\n");
        sb.append("Arrivals today (booked): ").append(all.stream().filter(r -> "BOOKED".equalsIgnoreCase(r.getStatus()) && today.equals(r.getStartDate())).count()).append("\n");
        sb.append("Rooms in inventory: ").append(roomService.getAllRooms().size()).append("\n");
        sb.append("Low-stock SKUs (≤8): ").append(shopService.getInventory().stream().filter(i -> i.getStock() <= 8).count()).append("\n");
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
            JOptionPane.showMessageDialog(this, "Briefing copied to the clipboard.", "Clipboard",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Clipboard unavailable on this system.", "Clipboard",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void stylePrimary(JButton b) {
        b.setFont(UITheme.BUTTON_FONT);
        b.setBackground(UITheme.ACCENT_GOLD);
        b.setForeground(Color.BLACK);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
    }

    private void styleGhost(JButton b) {
        b.setFont(new Font("SansSerif", Font.PLAIN, 13));
        b.setBackground(new Color(238, 232, 220));
        b.setForeground(UITheme.TEXT_DARK);
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TILE_BORDER, 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleDanger(JButton b) {
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBackground(new Color(188, 64, 64));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
    }
}
