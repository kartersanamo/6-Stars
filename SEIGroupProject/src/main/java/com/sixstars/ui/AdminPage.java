package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Item;
import com.sixstars.model.Reservation;
import com.sixstars.model.Role;
import com.sixstars.model.ShopOrder;
import com.sixstars.service.AccountService;
import com.sixstars.service.BillingService;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;
import com.sixstars.service.ShopService;
import com.sixstars.service.stripe.StripeConfig;

/**
 * Executive administration console: live property and business metrics, staff tooling,
 * ledger visibility, and operational controls — themed alongside the clerk hub with a darker command palette.
 */
public class AdminPage extends JPanel {

    private static final Color HEADER_TOP = new Color(28, 24, 36);
    private static final Color HEADER_BOTTOM = new Color(48, 38, 58);
    private static final Color HEADER_GOLD_LINE = new Color(176, 132, 38);
    private static final Color HEADER_FG = new Color(255, 252, 248);
    private static final Color STAT_CARD_BG = new Color(255, 253, 250);
    private static final Color TILE_BG = new Color(252, 249, 244);
    private static final Color TILE_HOVER = new Color(238, 228, 210);
    private static final Color TILE_BORDER = new Color(200, 186, 168);
    private static final Color ACCENT_LINE = new Color(120, 82, 168);
    private static final DateTimeFormatter CLOCK_FMT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy · h:mm a", Locale.US);

    private final JPanel pages;
    private final CardLayout cardLayout;
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final AccountService accountService;
    private final ShopService shopService = new ShopService();
    private final BillingService billingService = new BillingService();

    private final JLabel statAccounts = new JLabel("—");
    private final JLabel statStaff = new JLabel("—");
    private final JLabel statGuests = new JLabel("—");
    private final JLabel statActiveRes = new JLabel("—");
    private final JLabel statRooms = new JLabel("—");
    private final JLabel statLedger = new JLabel("—");
    private final JLabel statShopOrders = new JLabel("—");
    private final JLabel statLowStock = new JLabel("—");
    private final JLabel clockLabel = new JLabel(" ");
    private final JLabel lastRefreshLabel = new JLabel(" ");
    private final JPanel rosterInner = new JPanel();
    private final JPanel reservationsInner = new JPanel();
    private final JPanel inventoryInner = new JPanel();

    public AdminPage(JPanel pages, CardLayout cardLayout,
            ReservationService reservationService, RoomService roomService, AccountService accountService) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.reservationService = reservationService;
        this.roomService = roomService;
        this.accountService = accountService;

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        add(buildHeroHeader(), BorderLayout.NORTH);
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

    private JPanel buildHeroHeader() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    GradientPaint gp = new GradientPaint(0, 0, HEADER_TOP, 0, getHeight(), HEADER_BOTTOM);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(HEADER_GOLD_LINE);
                    g2.fillRect(0, getHeight() - 3, getWidth(), 3);
                } finally {
                    g2.dispose();
                }
            }
        };
        bar.setLayout(new BorderLayout(24, 0));
        bar.setBorder(new EmptyBorder(26, 36, 30, 36));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel kicker = new JLabel("SIX STARS · SYSTEM CONTROL");
        kicker.setFont(new Font("SansSerif", Font.BOLD, 11));
        kicker.setForeground(new Color(200, 180, 220));
        kicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Command Center");
        title.setFont(new Font("Serif", Font.BOLD, 34));
        title.setForeground(HEADER_FG);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = new JLabel("Property, people, ledgers, and retail — unified oversight for hotel leadership.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 15));
        sub.setForeground(new Color(230, 220, 240));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(kicker);
        left.add(Box.createRigidArea(new Dimension(0, 8)));
        left.add(title);
        left.add(Box.createRigidArea(new Dimension(0, 8)));
        left.add(sub);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        clockLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        clockLabel.setForeground(new Color(240, 232, 250));
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        clockLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        clockLabel.setText(LocalDateTime.now().format(CLOCK_FMT));
        JLabel role = new JLabel(roleLine());
        role.setFont(new Font("SansSerif", Font.PLAIN, 13));
        role.setForeground(new Color(210, 198, 225));
        role.setHorizontalAlignment(SwingConstants.RIGHT);
        role.setAlignmentX(Component.RIGHT_ALIGNMENT);
        right.add(clockLabel);
        right.add(Box.createRigidArea(new Dimension(0, 8)));
        right.add(role);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        wrap.add(bar, BorderLayout.CENTER);
        return wrap;
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
        body.setBorder(new EmptyBorder(22, 32, 36, 32));

        body.add(sectionTitle("Executive pulse", "Live counts across accounts, stay inventory, and recorded revenue."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(buildStatsGrid());
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        body.add(lastRefreshLabel);
        lastRefreshLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        lastRefreshLabel.setForeground(UITheme.TEXT_MEDIUM);
        lastRefreshLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(Box.createRigidArea(new Dimension(0, 22)));

        body.add(sectionTitle("Administrative toolkit", "Jump into the modules your administrators and owners use most."));
        body.add(Box.createRigidArea(new Dimension(0, 12)));
        body.add(buildActionGrid());
        body.add(Box.createRigidArea(new Dimension(0, 26)));

        body.add(sectionTitle("Leadership roster", "Elevated roles with property-wide permissions."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(wrapScroll(rosterInner, 160));
        body.add(Box.createRigidArea(new Dimension(0, 22)));

        body.add(sectionTitle("Reservation pipeline", "Latest stays in the system — newest first."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(wrapScroll(reservationsInner, 220));
        body.add(Box.createRigidArea(new Dimension(0, 22)));

        body.add(sectionTitle("Retail & supply spotlight", "Shop throughput and SKUs that need attention."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(wrapScroll(inventoryInner, 160));
        body.add(Box.createRigidArea(new Dimension(0, 22)));

        body.add(sectionTitle("Command strip", "Clipboard, audits, and session control."));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(buildCommandStrip());
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
        JLabel s = new JLabel(subtitle);
        s.setFont(UITheme.SUBTITLE_FONT);
        s.setForeground(UITheme.TEXT_MEDIUM);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createRigidArea(new Dimension(0, 4)));
        p.add(s);
        return p;
    }

    private JPanel buildStatsGrid() {
        JPanel row = new JPanel(new GridLayout(0, 2, 14, 14));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 520));
        row.add(statCard("Total accounts", statAccounts, new Color(72, 110, 140)));
        row.add(statCard("Staff (clerks)", statStaff, new Color(120, 72, 156)));
        row.add(statCard("Guest profiles", statGuests, new Color(43, 128, 78)));
        row.add(statCard("Active reservations", statActiveRes, new Color(56, 113, 182)));
        row.add(statCard("Room keys live", statRooms, new Color(176, 132, 38)));
        row.add(statCard("Ledger total (demo)", statLedger, new Color(44, 122, 72)));
        row.add(statCard("Shop orders (all)", statShopOrders, new Color(184, 92, 56)));
        row.add(statCard("Low-stock SKUs (≤8)", statLowStock, new Color(170, 50, 50)));
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
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
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

    private JPanel buildActionGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 14));
        grid.setOpaque(false);
        grid.setAlignmentX(Component.LEFT_ALIGNMENT);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 900));

        grid.add(actionTile("✦", "Create staff account", "Provision clerks from the same flow guests use — role is selected on the form.",
                () -> {
                    Main.createAccountPage.refreshInfo();
                    cardLayout.show(pages, "create account");
                }));
        grid.add(actionTile("⚿", "Reset user password", "Search any account and set a new hash-backed password for recovery.",
                () -> cardLayout.show(pages, "reset password")));
        grid.add(actionTile("⌁", "Hotel billing workspace", "Revenue desk: folios, shop rollups, and property-wide totals.",
                () -> cardLayout.show(pages, "clerk billing")));
        grid.add(actionTile("◎", "Launch clerk dashboard", "Hand off to the front-office console with live arrivals and tools.",
                () -> {
                    Main.clerkPage.refreshDashboard();
                    cardLayout.show(pages, "clerk page");
                }));
        grid.add(actionTile("⌂", "Room inventory editor", "Themes, tiers, smoking flags, and nightly rate intelligence.",
                () -> cardLayout.show(pages, "room management")));
        grid.add(actionTile("≡", "Master reservation ledger", "Search, amend, or audit every stay in the database.",
                () -> {
                    Main.reservationsPage.refresh();
                    cardLayout.show(pages, "reservations");
                }));
        grid.add(actionTile("✓", "Property check-in desk", "Override or assist arrivals and departures from one screen.",
                () -> cardLayout.show(pages, "check in")));
        grid.add(actionTile("☕", "Shop catalog (preview)", "See exactly what guests can buy — pricing and stock sync live.",
                () -> {
                    Main.shopPage.refreshPage();
                    cardLayout.show(pages, "shop");
                }));
        grid.add(actionTile("◇", "Walk-in booking funnel", "Open the reservation composer for phone or lobby walk-ins.",
                () -> {
                    Main.makeReservationPage.refreshPage();
                    cardLayout.show(pages, "make reservation");
                }));
        grid.add(actionTile("✉", "Guest forgot-password flow", "Test the public recovery screen used from the login page.",
                () -> cardLayout.show(pages, "forgot password")));
        grid.add(actionTile("◷", "Night audit snapshot", "One-click status counts for finance handoff or owner review.",
                this::showNightAuditSnapshot));
        grid.add(actionTile("↻", "Run automatic check-outs", "Apply system rules for departures that have aged past end date.",
                this::runAutoCheckOuts));
        grid.add(actionTile("⚑", "Crisis & operations directory", "Extensions, hours, and escalation tree for leadership.",
                this::showHotelDirectory));
        grid.add(actionTile("⚙", "Integrations & runtime", "Stripe keys, Mailgun, SQLite path, and JVM snapshot.",
                this::showIntegrationsDialog));

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
        for (Component c : text.getComponents()) {
            c.addMouseListener(ma);
        }
        return tile;
    }

    private JPanel buildCommandStrip() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton refresh = new JButton("Refresh console");
        stylePrimary(refresh);
        refresh.addActionListener(_ -> refreshDashboard());

        JButton copy = new JButton("Copy executive summary");
        styleGhost(copy);
        copy.addActionListener(_ -> copyExecutiveSummary());

        JButton guestHome = new JButton("Guest home (preview)");
        styleGhost(guestHome);
        guestHome.addActionListener(_ -> cardLayout.show(pages, "home"));

        JButton logout = new JButton("Sign out");
        styleDanger(logout);
        logout.addActionListener(_ -> {
            AccountController.currentAccount = null;
            Main.headerBar.refreshInfo();
            cardLayout.show(pages, "home");
        });

        row.add(refresh);
        row.add(copy);
        row.add(guestHome);
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
        List<Account> accounts = accountService.getAllAccounts();
        statAccounts.setText(String.valueOf(accounts.size()));
        statStaff.setText(String.valueOf(accounts.stream().filter(a -> a.getRole() == Role.CLERK).count()));
        statGuests.setText(String.valueOf(accounts.stream().filter(a -> a.getRole() == Role.GUEST).count()));

        List<Reservation> allRes = reservationService.getAllReservations();
        long active = allRes.stream().filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus())).count();
        statActiveRes.setText(String.valueOf(active));
        statRooms.setText(String.valueOf(roomService.getAllRooms().size()));

        double ledger = billingService.getHotelGrandTotal();
        statLedger.setText(String.format(Locale.US, "$%.0f", ledger));

        List<ShopOrder> orders = billingService.getAllShopPurchases();
        statShopOrders.setText(String.valueOf(orders.size()));

        long low = shopService.getInventory().stream().filter(i -> i.getStock() <= 8).count();
        statLowStock.setText(String.valueOf(low));

        fillRoster(accounts);
        fillReservations(allRes);
        fillInventoryWatch();

        lastRefreshLabel.setText("Last refreshed: " + LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("h:mm a", Locale.US)));
        clockLabel.setText(LocalDateTime.now().format(CLOCK_FMT));
        revalidate();
        repaint();
    }

    private void fillRoster(List<Account> accounts) {
        rosterInner.removeAll();
        List<Account> leaders = accounts.stream()
                .filter(a -> a.getRole() == Role.CLERK || a.getRole() == Role.ADMIN)
                .sorted(Comparator.comparing(Account::getEmail))
                .collect(Collectors.toList());
        if (leaders.isEmpty()) {
            rosterInner.add(hintRow("No elevated staff accounts yet — create a clerk from Create staff account."));
        } else {
            for (Account a : leaders) {
                rosterInner.add(rosterRow(a));
                rosterInner.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        rosterInner.revalidate();
        rosterInner.repaint();
    }

    private JPanel rosterRow(Account a) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(true);
        row.setBackground(new Color(250, 248, 244));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 218, 200), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        JLabel left = new JLabel("<html><div style=\"font-size:13px\"><b>" + esc(a.getEmail()) + "</b><br/>"
                + "<span style=\"color:#666;\">" + esc(a.getFirstName() + " " + a.getLastName()) + "</span></div></html>");
        JLabel tag = new JLabel(formatRole(a.getRole().name()), SwingConstants.CENTER);
        tag.setOpaque(true);
        tag.setFont(new Font("SansSerif", Font.BOLD, 11));
        tag.setBorder(new EmptyBorder(4, 10, 4, 10));
        tag.setBackground(a.getRole() == Role.ADMIN ? new Color(240, 228, 255) : new Color(231, 246, 236));
        tag.setForeground(UITheme.TEXT_DARK);
        row.add(left, BorderLayout.CENTER);
        row.add(tag, BorderLayout.EAST);
        return row;
    }

    private void fillReservations(List<Reservation> all) {
        reservationsInner.removeAll();
        List<Reservation> recent = all.stream()
                .sorted(Comparator.comparingInt(Reservation::getId).reversed())
                .limit(12)
                .collect(Collectors.toList());
        if (recent.isEmpty()) {
            reservationsInner.add(hintRow("No reservations on file yet."));
        } else {
            for (Reservation r : recent) {
                reservationsInner.add(reservationRow(r));
                reservationsInner.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        reservationsInner.revalidate();
        reservationsInner.repaint();
    }

    private JPanel reservationRow(Reservation r) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(true);
        row.setBackground(new Color(250, 248, 244));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 218, 200), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        String rooms = r.getRooms() == null ? "—" : r.getRooms().stream()
                .map(x -> "Rm " + x.getRoomNumber())
                .collect(Collectors.joining(", "));
        JLabel left = new JLabel("<html><div style=\"font-size:13px\"><b>" + esc(r.getGuestEmail()) + "</b><br/>"
                + "<span style=\"color:#666;\">" + rooms + " · " + r.getStartDate() + " → " + r.getEndDate()
                + " · $" + r.getTotalCost() + "</span></div></html>");
        JLabel st = new JLabel(r.getStatus() == null ? "—" : r.getStatus(), SwingConstants.CENTER);
        st.setOpaque(true);
        st.setFont(new Font("SansSerif", Font.BOLD, 10));
        st.setBorder(new EmptyBorder(4, 8, 4, 8));
        st.setBackground(new Color(234, 240, 255));
        st.setForeground(UITheme.TEXT_DARK);
        row.add(left, BorderLayout.CENTER);
        row.add(st, BorderLayout.EAST);
        return row;
    }

    private void fillInventoryWatch() {
        inventoryInner.removeAll();
        List<Item> low = shopService.getInventory().stream()
                .filter(i -> i.getStock() <= 8)
                .sorted(Comparator.comparingInt(Item::getStock))
                .limit(10)
                .collect(Collectors.toList());
        double shopRev = billingService.getHotelShopTotal();
        inventoryInner.add(hintRow("Lifetime shop revenue (recorded): $" + String.format(Locale.US, "%.2f", shopRev)));
        inventoryInner.add(Box.createRigidArea(new Dimension(0, 10)));
        if (low.isEmpty()) {
            inventoryInner.add(hintRow("All SKUs above eight units — no immediate restock pressure."));
        } else {
            for (Item it : low) {
                inventoryInner.add(itemRow(it));
                inventoryInner.add(Box.createRigidArea(new Dimension(0, 6)));
            }
        }
        inventoryInner.revalidate();
        inventoryInner.repaint();
    }

    private JPanel itemRow(Item it) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel left = new JLabel("<html><b>" + esc(it.getName()) + "</b><br/><span style=\"color:#666;\">$"
                + String.format(Locale.US, "%.2f", it.getPrice()) + " each</span></html>");
        JLabel qty = new JLabel("Stock: " + it.getStock(), SwingConstants.RIGHT);
        qty.setFont(new Font("SansSerif", Font.BOLD, 13));
        qty.setForeground(it.getStock() <= 3 ? new Color(170, 50, 40) : UITheme.TEXT_DARK);
        row.add(left, BorderLayout.CENTER);
        row.add(qty, BorderLayout.EAST);
        return row;
    }

    private JPanel hintRow(String text) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l = new JLabel("<html><body style=\"margin:0\">" + esc(text) + "</body></html>");
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        l.setForeground(UITheme.TEXT_MEDIUM);
        p.add(l, BorderLayout.WEST);
        return p;
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void showNightAuditSnapshot() {
        LocalDate today = LocalDate.now();
        List<Reservation> all = reservationService.getAllReservations();
        long booked = all.stream().filter(r -> "BOOKED".equalsIgnoreCase(r.getStatus())).count();
        long checkedIn = all.stream().filter(r -> "CHECKED_IN".equalsIgnoreCase(r.getStatus())).count();
        long checkedOut = all.stream().filter(r -> "CHECKED_OUT".equalsIgnoreCase(r.getStatus())).count();
        long cancelled = all.stream().filter(r -> "CANCELLED".equalsIgnoreCase(r.getStatus())).count();
        int rooms = roomService.getAllRooms().size();
        long guests = accountService.getAllAccounts().stream().filter(a -> a.getRole() == Role.GUEST).count();
        double ledger = billingService.getHotelGrandTotal();
        String msg = "Date: " + today + "\n\n"
                + "Reservation statuses\n"
                + "  Booked: " + booked + "\n"
                + "  Checked in: " + checkedIn + "\n"
                + "  Checked out: " + checkedOut + "\n"
                + "  Cancelled: " + cancelled + "\n\n"
                + "Room inventory: " + rooms + " keys\n"
                + "Guest profiles: " + guests + "\n"
                + "Recorded ledger total: $" + String.format(Locale.US, "%.2f", ledger) + "\n\n"
                + "Use for owner reports, night audit binders, or PMS reconciliation drills.";
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
                SIX STARS HOTEL — LEADERSHIP & CRISIS DIRECTORY

                Owner hotline: +1 (254) 555-0100
                General manager suite: 7500
                Risk & security command: 7599
                Finance controller: 7605
                IT / PMS support vendor: 7610

                Brand standards: sixstars.internal/playbook
                Media escalation: press@sixstars.example

                Emergency: 9-1-1 from house phones · Hotel muster: lobby fountain

                Reference content for training and demonstration.""";
        JOptionPane.showMessageDialog(this, msg, "Operations directory", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showIntegrationsDialog() {
        File db = new File("hotel_reservation.db");
        String dbLine = db.exists()
                ? "SQLite file: " + db.getAbsolutePath() + " (" + db.length() + " bytes)"
                : "SQLite file not found next to working directory — a new file may be created on first write.";
        String mailgun = envPresent("MAILGUN_API_KEY") && envPresent("MAILGUN_DOMAIN") && envPresent("MAILGUN_FROM_EMAIL")
                ? "Mailgun: configured (API key, domain, from)"
                : "Mailgun: not fully configured — transactional email may be disabled.";
        String stripe = (StripeConfig.hasSecretKey() ? "Stripe secret: present\n" : "Stripe secret: missing\n")
                + (StripeConfig.hasConnectClientId() ? "Stripe Connect client: present" : "Stripe Connect client: missing");
        String msg = "Java " + System.getProperty("java.version") + " · " + System.getProperty("java.vendor") + "\n"
                + "User directory: " + System.getProperty("user.dir") + "\n\n"
                + dbLine + "\n\n"
                + mailgun + "\n\n"
                + stripe;
        JOptionPane.showMessageDialog(this, msg, "Integrations & runtime", JOptionPane.INFORMATION_MESSAGE);
    }

    private static boolean envPresent(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            v = System.getProperty(key);
        }
        return v != null && !v.isBlank();
    }

    private void copyExecutiveSummary() {
        LocalDate today = LocalDate.now();
        List<Reservation> all = reservationService.getAllReservations();
        List<Account> accounts = accountService.getAllAccounts();
        StringBuilder sb = new StringBuilder();
        sb.append("Six Stars Hotel — executive summary\n");
        sb.append("Generated ").append(LocalDateTime.now()).append("\n\n");
        sb.append("Accounts: ").append(accounts.size()).append(" (clerks ")
                .append(accounts.stream().filter(a -> a.getRole() == Role.CLERK).count())
                .append(", guests ").append(accounts.stream().filter(a -> a.getRole() == Role.GUEST).count()).append(")\n");
        sb.append("Active reservations: ").append(all.stream().filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus())).count()).append("\n");
        sb.append("Checked-in now: ").append(all.stream().filter(r -> "CHECKED_IN".equalsIgnoreCase(r.getStatus())).count()).append("\n");
        sb.append("Arrivals today (booked): ").append(all.stream().filter(r -> "BOOKED".equalsIgnoreCase(r.getStatus()) && today.equals(r.getStartDate())).count()).append("\n");
        sb.append("Rooms: ").append(roomService.getAllRooms().size()).append("\n");
        sb.append("Ledger total (demo): $").append(String.format(Locale.US, "%.2f", billingService.getHotelGrandTotal())).append("\n");
        sb.append("Shop orders: ").append(billingService.getAllShopPurchases().size()).append("\n");
        sb.append("Low-stock SKUs (≤8): ").append(shopService.getInventory().stream().filter(i -> i.getStock() <= 8).count()).append("\n");
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
            JOptionPane.showMessageDialog(this, "Executive summary copied to the clipboard.", "Clipboard",
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
        b.setBackground(new Color(120, 48, 72));
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
    }
}
