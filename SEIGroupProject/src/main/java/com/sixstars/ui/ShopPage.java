package com.sixstars.ui;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.database.ShoppingCartDAO;
import com.sixstars.model.Account;
import com.sixstars.model.CartItem;
import com.sixstars.model.Item;
import com.sixstars.model.Role;
import com.sixstars.model.ShopOrder;
import com.sixstars.model.ShoppingCart;
import com.sixstars.service.ShopService;
import com.sixstars.ui.shop.ShopImageLoader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * In-stay marketplace: hero merchandising, adaptive catalog grid, and a drawer-style bag.
 */
public class ShopPage extends JPanel {

    private static final Color HERO_TOP = new Color(38, 32, 26);
    private static final Color HERO_BOTTOM = new Color(72, 58, 44);
    private static final Color PAGE_TINT = new Color(250, 248, 244);
    private static final Color CARD = Color.WHITE;
    private static final Color CARD_BORDER = new Color(228, 220, 206);
    private static final Color IMAGE_WELL = new Color(243, 240, 234);
    private static final Color MUTED = new Color(115, 105, 92);
    private static final Color PRICE_ACCENT = new Color(124, 82, 38);
    private static final Color DANGER = new Color(178, 58, 58);

    private final JPanel pages;
    private final CardLayout cardLayout;

    private final ShoppingCart cart;
    private final ShoppingCartDAO cartDAO;
    private final ShopService shopService;

    private final JPanel inventoryPanel;
    private final JPanel cartItemsPanel;
    private final CardLayout cartCardLayout;
    private final JPanel cartContainer;
    private final JLabel totalLabel;
    private final JLabel subtotalDetailLabel;
    private final JLabel resultsInfoLabel;
    private final JTextField searchField;
    private final JComboBox<String> sortCombo;
    private final JSplitPane splitPane;

    private int catalogColumns = 3;
    private boolean purchaseEnabled;

    public ShopPage(JPanel pages, CardLayout cardLayout) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.cart = new ShoppingCart();
        this.cartDAO = new ShoppingCartDAO();
        this.shopService = new ShopService();

        setLayout(new BorderLayout());
        setBackground(PAGE_TINT);

        inventoryPanel = new JPanel();
        totalLabel = new JLabel("$0.00");
        subtotalDetailLabel = new JLabel(" ");
        resultsInfoLabel = new JLabel(" ");
        searchField = styledSearchField();
        sortCombo = new JComboBox<>(new String[] {
                "Sort: featured",
                "Price: low to high",
                "Price: high to low",
                "Name: A–Z"
        });
        sortCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
        sortCombo.setMaximumRowCount(6);

        cartCardLayout = new CardLayout();
        cartContainer = new JPanel(cartCardLayout);
        cartContainer.setOpaque(false);
        cartContainer.setMinimumSize(new Dimension(300, 200));

        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setOpaque(false);

        JPanel catalogShell = buildCatalogShell();
        JPanel guestCart = buildGuestCartDrawer();
        JPanel clerkPanel = buildClerkViewDrawer();

        cartContainer.add(guestCart, "cart");
        cartContainer.add(clerkPanel, "viewOnly");

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, catalogShell, wrapCartChrome(cartContainer));
        splitPane.setResizeWeight(1.0);
        splitPane.setContinuousLayout(true);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerSize(8);
        splitPane.setOpaque(false);

        add(buildHeroHeader(), BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(buildTrustFooter(), BorderLayout.SOUTH);

        attachSearchListener();
        sortCombo.addActionListener(_ -> refreshInventory());

        splitPane.getLeftComponent().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int next = computeCatalogColumns();
                if (next != catalogColumns) {
                    catalogColumns = next;
                    refreshInventory();
                }
            }
        });

        refreshPage();
    }

    private JPanel wrapCartChrome(JPanel inner) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 16, 20));
        wrap.add(inner, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildHeroHeader() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);

        JPanel hero = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    GradientPaint gp = new GradientPaint(0, 0, HERO_TOP, 0, getHeight(), HERO_BOTTOM);
                    g2.setPaint(gp);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } finally {
                    g2.dispose();
                }
            }
        };
        hero.setLayout(new BorderLayout());
        hero.setPreferredSize(new Dimension(0, 118));
        hero.setBorder(new EmptyBorder(20, 28, 20, 28));

        JLabel kicker = new JLabel("IN-STAY MARKETPLACE");
        kicker.setFont(new Font("SansSerif", Font.BOLD, 11));
        kicker.setForeground(new Color(210, 190, 160));

        JLabel title = new JLabel("Six Stars Market");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(new Color(255, 252, 245));

        JLabel sub = new JLabel("Curated essentials, treats, and keepsakes — delivered to your room.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(new Color(235, 225, 210));

        JPanel words = new JPanel();
        words.setLayout(new BoxLayout(words, BoxLayout.Y_AXIS));
        words.setOpaque(false);
        words.add(kicker);
        words.add(Box.createRigidArea(new Dimension(0, 6)));
        words.add(title);
        words.add(Box.createRigidArea(new Dimension(0, 6)));
        words.add(sub);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton back = pillButton("← Back to hotel home", false);
        back.setForeground(new Color(255, 252, 245));
        back.setBackground(new Color(70, 58, 48));
        back.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 150), 1, true),
                new EmptyBorder(10, 16, 10, 16)));
        back.addActionListener(e -> cardLayout.show(pages, "home"));
        actions.add(back);

        hero.add(words, BorderLayout.WEST);
        hero.add(actions, BorderLayout.EAST);

        JPanel toolbar = new JPanel(new BorderLayout(16, 0));
        toolbar.setOpaque(true);
        toolbar.setBackground(CARD);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                new EmptyBorder(14, 28, 14, 28)));

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        JLabel searchGlyph = new JLabel("  ⌕  ");
        searchGlyph.setFont(new Font("SansSerif", Font.PLAIN, 18));
        searchGlyph.setForeground(MUTED);
        searchWrap.add(searchGlyph, BorderLayout.WEST);
        searchWrap.add(searchField, BorderLayout.CENTER);

        JPanel east = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        east.setOpaque(false);
        east.add(sortCombo);
        east.add(resultsInfoLabel);
        resultsInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        resultsInfoLabel.setForeground(MUTED);

        toolbar.add(searchWrap, BorderLayout.CENTER);
        toolbar.add(east, BorderLayout.EAST);

        shell.add(hero, BorderLayout.NORTH);
        shell.add(toolbar, BorderLayout.CENTER);
        return shell;
    }

    private JPanel buildCatalogShell() {
        JPanel shell = new JPanel(new BorderLayout());
        shell.setOpaque(false);
        shell.setBorder(new EmptyBorder(8, 20, 0, 4));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        heading.setBorder(new EmptyBorder(4, 8, 12, 8));
        JLabel h = new JLabel("Shop the collection");
        h.setFont(new Font("Serif", Font.BOLD, 22));
        h.setForeground(UITheme.TEXT_DARK);
        JLabel hint = new JLabel("Tap add to bag — checkout posts to your guest folio when you are checked in.");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        hint.setForeground(MUTED);
        heading.add(h, BorderLayout.NORTH);
        heading.add(hint, BorderLayout.SOUTH);

        JScrollPane scroll = new JScrollPane(inventoryPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(PAGE_TINT);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        shell.add(heading, BorderLayout.NORTH);
        shell.add(scroll, BorderLayout.CENTER);
        return shell;
    }

    private JPanel buildGuestCartDrawer() {
        JPanel drawer = new JPanel(new BorderLayout());
        drawer.setBackground(CARD);
        drawer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)));
        drawer.setPreferredSize(new Dimension(360, 0));
        drawer.setMinimumSize(new Dimension(300, 120));

        JPanel head = new JPanel();
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        head.setOpaque(false);

        JLabel bag = new JLabel("Your bag");
        bag.setFont(new Font("Serif", Font.BOLD, 22));
        bag.setForeground(UITheme.TEXT_DARK);
        JLabel sub = new JLabel("Review items before secure checkout.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12));
        sub.setForeground(MUTED);
        bag.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(bag);
        head.add(Box.createRigidArea(new Dimension(0, 4)));
        head.add(sub);
        head.add(Box.createRigidArea(new Dimension(0, 16)));

        JScrollPane cartScroll = new JScrollPane(cartItemsPanel);
        cartScroll.setBorder(BorderFactory.createLineBorder(new Color(236, 232, 224), 1, true));
        cartScroll.getViewport().setBackground(new Color(252, 251, 248));
        cartScroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel totals = new JPanel();
        totals.setLayout(new BoxLayout(totals, BoxLayout.Y_AXIS));
        totals.setOpaque(false);
        totals.setBorder(new EmptyBorder(16, 0, 0, 0));

        subtotalDetailLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtotalDetailLabel.setForeground(MUTED);
        subtotalDetailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        totalLabel.setForeground(PRICE_ACCENT);
        totalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton checkout = pillButton("Checkout", true);
        checkout.setBackground(UITheme.ACCENT_GOLD);
        checkout.setForeground(Color.BLACK);
        checkout.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        checkout.addActionListener(e -> handleCheckout());

        JButton clear = pillButton("Empty bag", false);
        clear.setForeground(DANGER);
        clear.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 200, 200), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        clear.setAlignmentX(Component.LEFT_ALIGNMENT);
        clear.addActionListener(e -> {
            cart.clear();
            persistCurrentCart();
            updateCartDisplay();
        });

        totals.add(subtotalDetailLabel);
        totals.add(Box.createRigidArea(new Dimension(0, 6)));
        totals.add(totalLabel);
        totals.add(Box.createRigidArea(new Dimension(0, 14)));
        totals.add(checkout);
        totals.add(Box.createRigidArea(new Dimension(0, 10)));
        totals.add(clear);

        drawer.add(head, BorderLayout.NORTH);
        drawer.add(cartScroll, BorderLayout.CENTER);
        drawer.add(totals, BorderLayout.SOUTH);
        return drawer;
    }

    private JPanel buildClerkViewDrawer() {
        JPanel drawer = new JPanel(new BorderLayout());
        drawer.setBackground(CARD);
        drawer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(24, 22, 24, 22)));
        drawer.setPreferredSize(new Dimension(360, 0));

        JLabel t = new JLabel("Staff catalog view");
        t.setFont(new Font("Serif", Font.BOLD, 22));
        t.setForeground(UITheme.TEXT_DARK);

        JLabel body = new JLabel("<html><body style='width:280px;color:#6A5F52;font-family:sans-serif;font-size:13px;'>"
                + "You are signed in as <b>clerk</b>. Browse live inventory and pricing. "
                + "Cart, checkout, and folio charges are available only to checked-in guests."
                + "</body></html>");

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(t);
        top.add(Box.createRigidArea(new Dimension(0, 14)));
        top.add(body);

        drawer.add(top, BorderLayout.NORTH);
        return drawer;
    }

    private JPanel buildTrustFooter() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.CENTER, 28, 10));
        bar.setOpaque(true);
        bar.setBackground(new Color(252, 250, 246));
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CARD_BORDER),
                new EmptyBorder(6, 16, 10, 16)));
        String[] bits = new String[] {
                "Room delivery routing",
                "Inventory synced live",
                "Guest folio aware"
        };
        for (String b : bits) {
            JLabel l = new JLabel("✦  " + b);
            l.setFont(new Font("SansSerif", Font.PLAIN, 12));
            l.setForeground(MUTED);
            bar.add(l);
        }
        return bar;
    }

    private JTextField styledSearchField() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 192, 178), 1, true),
                new EmptyBorder(10, 8, 10, 12)));
        f.setToolTipText("Search by product name");
        return f;
    }

    private JButton pillButton(String text, boolean fill) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", fill ? Font.BOLD : Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setOpaque(true);
        b.setContentAreaFilled(true);
        b.setBorderPainted(true);
        if (fill) {
            b.setBorder(new EmptyBorder(12, 20, 12, 20));
        } else {
            b.setBorder(new EmptyBorder(10, 16, 10, 16));
        }
        return b;
    }

    private int computeCatalogColumns() {
        Component left = splitPane.getLeftComponent();
        int w = left != null ? left.getWidth() : 800;
        if (w < 480) {
            return 2;
        }
        if (w < 720) {
            return 2;
        }
        if (w < 1020) {
            return 3;
        }
        return 4;
    }

    private void attachSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshInventory();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshInventory();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshInventory();
            }
        });
    }

    public void refreshInventory() {
        catalogColumns = computeCatalogColumns();

        List<Item> items = new ArrayList<>(shopService.getInventory());
        applySort(items);

        String q = searchField.getText().trim().toLowerCase(Locale.ROOT);
        List<Item> filtered = new ArrayList<>();
        for (Item it : items) {
            if (q.isEmpty() || it.getName().toLowerCase(Locale.ROOT).contains(q)) {
                filtered.add(it);
            }
        }

        inventoryPanel.removeAll();
        inventoryPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 0;

        int cols = catalogColumns;
        int shown = filtered.size();

        if (shown == 0) {
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = cols;
            inventoryPanel.add(createEmptyCatalogState(), gbc);
            resultsInfoLabel.setText("0 products");
        } else {
            int idx = 0;
            for (Item item : filtered) {
                gbc.gridwidth = 1;
                gbc.gridx = idx % cols;
                gbc.gridy = idx / cols;
                inventoryPanel.add(createProductCard(item), gbc);
                idx++;
            }
            int rows = (shown + cols - 1) / cols;
            gbc.gridx = 0;
            gbc.gridy = rows;
            gbc.gridwidth = cols;
            gbc.weighty = 1;
            JPanel glue = new JPanel();
            glue.setOpaque(false);
            inventoryPanel.add(glue, gbc);
            resultsInfoLabel.setText(shown + " product" + (shown == 1 ? "" : "s"));
        }

        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }

    private void applySort(List<Item> items) {
        int sel = sortCombo.getSelectedIndex();
        switch (sel) {
            case 1 -> items.sort(Comparator.comparingDouble(Item::getPrice));
            case 2 -> items.sort(Comparator.comparingDouble(Item::getPrice).reversed());
            case 3 -> items.sort(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER));
            default -> { /* featured = DAO order */ }
        }
    }

    private JPanel createEmptyCatalogState() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(48, 24, 48, 24));
        JLabel icon = new JLabel("◇", SwingConstants.CENTER);
        icon.setFont(new Font("Serif", Font.PLAIN, 42));
        icon.setForeground(new Color(200, 188, 170));
        JLabel t = new JLabel("<html><center style='color:#4A4036;font-size:15px;'>No products match that search.<br/>"
                + "<span style='color:#8A7E6E;font-size:13px;'>Try another keyword or clear the search box.</span></center></html>",
                SwingConstants.CENTER);
        p.add(icon, BorderLayout.NORTH);
        p.add(t, BorderLayout.CENTER);
        return p;
    }

    private JPanel createProductCard(Item item) {
        JPanel imgHolder = new JPanel(new BorderLayout());
        imgHolder.setBackground(IMAGE_WELL);
        imgHolder.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 224, 214)));
        imgHolder.setPreferredSize(new Dimension(200, 160));
        JLabel img = buildProductImage(item, 200, 150);
        imgHolder.add(img, BorderLayout.CENTER);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        badgeRow.setOpaque(false);
        badgeRow.setBorder(new EmptyBorder(10, 14, 0, 14));
        if (item.getStock() <= 0) {
            badgeRow.add(pillLabel("Sold out", new Color(120, 50, 50), new Color(255, 240, 240)));
        } else if (item.getStock() <= 5) {
            badgeRow.add(pillLabel("Only " + item.getStock() + " left", new Color(124, 82, 38), new Color(255, 246, 230)));
        } else if (item.getStock() <= 12) {
            badgeRow.add(pillLabel("Popular", new Color(56, 90, 120), new Color(236, 244, 252)));
        }

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(6, 14, 0, 14));

        JLabel name = new JLabel("<html><body style='width:200px;color:#3A3228;font-size:15px;font-weight:600;'>"
                + escHtml(item.getName()) + "</body></html>");
        name.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel price = new JLabel(String.format(Locale.US, "$%.2f", item.getPrice()));
        price.setFont(new Font("SansSerif", Font.BOLD, 20));
        price.setForeground(PRICE_ACCENT);
        price.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel ship = new JLabel("Room delivery · adds to folio");
        ship.setFont(new Font("SansSerif", Font.PLAIN, 11));
        ship.setForeground(MUTED);
        ship.setAlignmentX(Component.LEFT_ALIGNMENT);

        body.add(name);
        body.add(Box.createRigidArea(new Dimension(0, 8)));
        body.add(price);
        body.add(Box.createRigidArea(new Dimension(0, 4)));
        body.add(ship);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(14, 14, 0, 14));

        if (purchaseEnabled) {
            boolean inStock = item.getStock() > 0;
            JButton add = new JButton(inStock ? "Add to bag" : "Unavailable");
            add.setFont(new Font("SansSerif", Font.BOLD, 13));
            add.setFocusPainted(false);
            add.setCursor(new Cursor(Cursor.HAND_CURSOR));
            add.setEnabled(inStock);
            add.setBackground(inStock ? UITheme.ACCENT_GOLD : new Color(220, 216, 208));
            add.setForeground(inStock ? Color.BLACK : MUTED);
            add.setBorder(new EmptyBorder(12, 16, 12, 16));
            add.setOpaque(true);
            add.addActionListener(e -> addToCartFromCard(item));
            foot.add(add, BorderLayout.CENTER);
        } else {
            JLabel ro = new JLabel("View only (clerk)");
            ro.setFont(new Font("SansSerif", Font.ITALIC, 12));
            ro.setForeground(MUTED);
            foot.add(ro, BorderLayout.WEST);
        }

        return assembleProductCard(imgHolder, badgeRow, body, foot);
    }

    private JPanel assembleProductCard(JPanel imgHolder, JPanel badgeRow, JPanel body, JPanel foot) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(0, 0, 12, 0)));

        imgHolder.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(imgHolder);
        if (badgeRow.getComponentCount() > 0) {
            badgeRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(badgeRow);
        }
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(body);
        foot.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(foot);

        Color normalBorder = CARD_BORDER;
        Color hoverBorder = new Color(176, 132, 38);
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(hoverBorder, 1, true),
                        new EmptyBorder(0, 0, 12, 0)));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(normalBorder, 1, true),
                        new EmptyBorder(0, 0, 12, 0)));
            }
        });
        return card;
    }

    private JLabel pillLabel(String text, Color fg, Color bg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 10));
        l.setForeground(fg);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setBorder(new EmptyBorder(4, 10, 4, 10));
        return l;
    }

    private static String escHtml(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void addToCartFromCard(Item item) {
        Account acc = AccountController.currentAccount;
        if (acc == null) {
            JOptionPane.showMessageDialog(this,
                    "Sign in as a guest to add items to your bag.",
                    "Account required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (item.getStock() <= 0) {
            return;
        }
        int inCart = cartQuantityFor(item);
        if (inCart >= item.getStock()) {
            JOptionPane.showMessageDialog(this,
                    "You already have the maximum available quantity for this item.",
                    "Stock limit",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        cart.addItem(item, 1);
        persistCurrentCart();
        updateCartDisplay();
    }

    private int cartQuantityFor(Item item) {
        for (CartItem ci : cart.getItems()) {
            if (ci.getItem().getId() == item.getId()) {
                return ci.getQuantity();
            }
        }
        return 0;
    }

    private JLabel buildProductImage(Item item, int maxW, int maxH) {
        BufferedImage cover = ShopImageLoader.loadCover(item.getImagePath(), maxW, maxH);
        JLabel label;
        if (cover != null) {
            label = new JLabel(new ImageIcon(cover));
        } else {
            label = new JLabel("<html><center><span style='color:#A89884;font-size:12px;'>No photo<br/>"
                    + escHtml(shortName(item.getName())) + "</span></center></html>", SwingConstants.CENTER);
            label.setIcon(null);
        }
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(maxW, maxH));
        label.setMinimumSize(new Dimension(maxW, maxH));
        label.setMaximumSize(new Dimension(maxW, maxH));
        label.setOpaque(true);
        label.setBackground(IMAGE_WELL);
        return label;
    }

    private static String shortName(String n) {
        if (n == null) {
            return "";
        }
        return n.length() > 28 ? n.substring(0, 27) + "…" : n;
    }

    private JPanel createCartLine(CartItem cartItem) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(true);
        row.setBackground(new Color(255, 255, 255));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(236, 232, 224), 1, true),
                new EmptyBorder(10, 10, 10, 10)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 86));

        JLabel thumb = new JLabel();
        thumb.setPreferredSize(new Dimension(52, 52));
        thumb.setMinimumSize(new Dimension(52, 52));
        Item it = cartItem.getItem();
        BufferedImage timg = ShopImageLoader.loadCover(it.getImagePath(), 52, 52);
        if (timg != null) {
            thumb.setIcon(new ImageIcon(timg));
        } else {
            thumb.setText("◆");
            thumb.setHorizontalAlignment(SwingConstants.CENTER);
            thumb.setFont(new Font("Serif", Font.PLAIN, 22));
            thumb.setForeground(new Color(190, 180, 165));
            thumb.setOpaque(true);
            thumb.setBackground(IMAGE_WELL);
        }

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);
        JLabel title = new JLabel("<html><div style='width:160px;font-weight:600;color:#3A3228;font-size:13px;'>"
                + escHtml(it.getName()) + "</div></html>");
        JLabel meta = new JLabel(String.format(Locale.US, "$%.2f each", it.getPrice()));
        meta.setFont(new Font("SansSerif", Font.PLAIN, 11));
        meta.setForeground(MUTED);
        text.add(title);
        text.add(Box.createRigidArea(new Dimension(0, 4)));
        text.add(meta);

        JPanel qty = new JPanel();
        qty.setLayout(new BoxLayout(qty, BoxLayout.Y_AXIS));
        qty.setOpaque(false);

        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        stepper.setOpaque(false);
        JButton minus = tinyBtn("−");
        JLabel qLab = new JLabel(String.valueOf(cartItem.getQuantity()), SwingConstants.CENTER);
        qLab.setFont(new Font("SansSerif", Font.BOLD, 13));
        qLab.setPreferredSize(new Dimension(24, 28));
        JButton plus = tinyBtn("+");
        minus.addActionListener(e -> changeQty(cartItem, -1));
        plus.addActionListener(e -> changeQty(cartItem, +1));
        stepper.add(minus);
        stepper.add(qLab);
        stepper.add(plus);

        JLabel lineTotal = new JLabel(String.format(Locale.US, "$%.2f", cartItem.getTotalPrice()));
        lineTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
        lineTotal.setForeground(PRICE_ACCENT);
        lineTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        JButton remove = tinyBtn("Remove");
        remove.setForeground(DANGER);
        remove.addActionListener(e -> {
            cart.removeItem(it);
            persistCurrentCart();
            updateCartDisplay();
        });

        JPanel east = new JPanel();
        east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
        east.setOpaque(false);
        east.add(stepper);
        east.add(Box.createRigidArea(new Dimension(0, 6)));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(lineTotal, BorderLayout.EAST);
        bottom.add(remove, BorderLayout.WEST);
        east.add(bottom);

        row.add(thumb, BorderLayout.WEST);
        row.add(text, BorderLayout.CENTER);
        row.add(east, BorderLayout.EAST);
        return row;
    }

    private JButton tinyBtn(String t) {
        JButton b = new JButton(t);
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setMargin(new Insets(2, 8, 2, 8));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(248, 246, 242));
        b.setForeground(UITheme.TEXT_DARK);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        return b;
    }

    private void changeQty(CartItem cartItem, int delta) {
        Item live = findLiveItem(cartItem.getItem().getId());
        if (live == null) {
            return;
        }
        if (delta > 0) {
            int q = cartQuantityFor(live);
            if (q >= live.getStock()) {
                JOptionPane.showMessageDialog(this,
                        "No additional stock for this item.",
                        "Stock limit",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            cart.addItem(live, 1);
        } else {
            cart.decrementItem(live);
        }
        persistCurrentCart();
        updateCartDisplay();
    }

    private Item findLiveItem(int id) {
        for (Item x : shopService.getInventory()) {
            if (x.getId() == id) {
                return x;
            }
        }
        return null;
    }

    public void refreshPage() {
        updateShopAccessState();
        refreshInventory();
        loadPersistedCartForCurrentAccount();
        SwingUtilities.invokeLater(() -> splitPane.setDividerLocation(0.72));
    }

    public void persistCurrentCart() {
        Account currentAccount = AccountController.currentAccount;
        if (currentAccount == null || !purchaseEnabled) {
            return;
        }
        cartDAO.saveCart(currentAccount.getEmail(), cart);
    }

    public void clearTransientCart() {
        cart.clear();
        updateCartDisplay();
    }

    private void loadPersistedCartForCurrentAccount() {
        Account currentAccount = AccountController.currentAccount;
        if (currentAccount == null || !purchaseEnabled) {
            cart.clear();
            updateCartDisplay();
            return;
        }
        cart.setItems(cartDAO.loadCart(currentAccount.getEmail()));
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        cartItemsPanel.removeAll();

        if (cart.isEmpty()) {
            JPanel empty = new JPanel(new BorderLayout());
            empty.setOpaque(false);
            empty.setBorder(new EmptyBorder(32, 12, 32, 12));
            JLabel msg = new JLabel("<html><center style='color:#8A7E6E;font-size:13px;'>Your bag is empty.<br/>"
                    + "Add something lovely from the collection.</center></html>", SwingConstants.CENTER);
            empty.add(msg, BorderLayout.CENTER);
            cartItemsPanel.add(empty);
            subtotalDetailLabel.setText(" ");
        } else {
            int n = 0;
            double sub = 0;
            for (CartItem ci : cart.getItems()) {
                cartItemsPanel.add(createCartLine(ci));
                cartItemsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
                n += ci.getQuantity();
                sub += ci.getTotalPrice();
            }
            subtotalDetailLabel.setText(n + " item" + (n == 1 ? "" : "s") + String.format(Locale.US, " · merchandise $%.2f", sub));
        }

        totalLabel.setText(String.format(Locale.US, "$%.2f", cart.getTotal()));
        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    private void handleCheckout() {
        if (!purchaseEnabled) {
            JOptionPane.showMessageDialog(this,
                    "Shop purchases are available for guest accounts.",
                    "Guests only",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Add something to your bag first.", "Bag is empty",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Account currentAccount = AccountController.currentAccount;
        if (currentAccount == null) {
            JOptionPane.showMessageDialog(this, "Please sign in as a guest.", "Sign in required",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            ShopOrder order = shopService.checkout(currentAccount.getEmail(), cart);
            double total = order.getTotalCost();
            JOptionPane.showMessageDialog(this,
                    String.format(Locale.US, "Thank you — order #%d is confirmed.%nTotal: $%.2f", order.getId(), total),
                    "Checkout complete",
                    JOptionPane.INFORMATION_MESSAGE);
            updateCartDisplay();
            refreshInventory();
            if (Main.billingPage != null) {
                Main.billingPage.refresh();
            }
        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Checkout", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateShopAccessState() {
        Account currentAccount = AccountController.currentAccount;
        boolean isClerk = currentAccount != null && currentAccount.getRole() == Role.CLERK;
        purchaseEnabled = !isClerk;
        cartCardLayout.show(cartContainer, isClerk ? "viewOnly" : "cart");
    }
}
