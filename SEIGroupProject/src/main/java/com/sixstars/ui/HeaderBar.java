package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sixstars.app.AppSession;
import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.AppNotification;
import com.sixstars.model.NotificationType;
import com.sixstars.model.Role;
import com.sixstars.service.NotificationService;

public class HeaderBar extends JPanel implements NotificationService.NotificationListener {

    private final JButton loginButton;
    private final JButton createAccountButton;
    private final JButton profileButton;
    private final JLabel profileBadgeLabel;
    private final JLayeredPane profileMenuContainer;
    private final JPopupMenu accountPopupMenu = new JPopupMenu();
    private final JPanel navPanel;
    private final JButton btnMyReservations;
    private final JButton btnShop;
    private JDialog activeToast;
    private final NotificationService notificationService = NotificationService.getInstance();
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");
    private final Map<String, String> lastToastByAccount = new HashMap<>();

    private final JPanel pages;
    private final CardLayout cardLayout;

    public HeaderBar(JPanel pages, CardLayout cardLayout) {
        this.pages = pages;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(UITheme.CARD_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 28, 16, 28)
        ));

        JButton brandButton = new JButton("6 Stars Hotel");
        brandButton.setFont(new Font("Serif", Font.BOLD, 30));
        brandButton.setForeground(UITheme.TEXT_DARK);

        brandButton.setBorderPainted(false);
        brandButton.setContentAreaFilled(false);
        brandButton.setFocusPainted(false);
        brandButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        brandButton.addActionListener(_ -> cardLayout.show(pages, "home"));

        navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);

        JButton bookNowButton = createButton("Book Now");
        bookNowButton.addActionListener(_ -> {
            Main.makeReservationPage.refreshPage();
            cardLayout.show(pages, "make reservation");
            Main.headerBar.refreshInfo();
        });

        btnShop = createButton("View Shop");
        btnShop.addActionListener(_ -> {
            if (Main.shopPage != null) {
                Main.shopPage.refreshPage();
            }
            cardLayout.show(pages, "shop");
        });

        btnMyReservations = createButton("My Reservations");
        btnMyReservations.addActionListener(_ -> {
            Main.reservationsPage.refresh();
            cardLayout.show(pages, "reservations");
        });

        loginButton = createButton("Login");
        loginButton.addActionListener(_ -> cardLayout.show(pages, "login"));

        createAccountButton = createButton("Create Account");
        createAccountButton.addActionListener(_ -> {
            Main.createAccountPage.refreshInfo();
            cardLayout.show(pages, "create account");
        });

        profileButton = new JButton();
        profileButton.setPreferredSize(new Dimension(40, 40));
        profileButton.setFocusPainted(false);
        profileButton.setBorderPainted(false);
        profileButton.setContentAreaFilled(false);
        profileButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        profileButton.addActionListener(_ -> showAccountPopupMenu());

        profileBadgeLabel = new JLabel("", SwingConstants.CENTER);
        profileBadgeLabel.setOpaque(true);
        profileBadgeLabel.setBackground(new Color(208, 55, 55));
        profileBadgeLabel.setForeground(Color.WHITE);
        profileBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        profileBadgeLabel.setBorder(new LineBorder(Color.WHITE, 1, true));
        profileBadgeLabel.setPreferredSize(new Dimension(16, 16));
        profileBadgeLabel.setVisible(false);

        profileMenuContainer = new JLayeredPane() {
            @Override
            public void doLayout() {
                int size = 40;
                profileButton.setBounds(0, 0, size, size);
                profileBadgeLabel.setBounds(size - 14, 0, 16, 16);
            }
        };
        profileMenuContainer.setPreferredSize(new Dimension(44, 40));
        profileMenuContainer.setOpaque(false);
        profileMenuContainer.add(profileButton, JLayeredPane.DEFAULT_LAYER);
        profileMenuContainer.add(profileBadgeLabel, JLayeredPane.PALETTE_LAYER);

        navPanel.add(bookNowButton);
        navPanel.add(btnShop);
        navPanel.add(btnMyReservations);
        navPanel.add(loginButton);
        navPanel.add(createAccountButton);
        navPanel.add(profileMenuContainer);

        add(brandButton, BorderLayout.WEST);
        add(navPanel, BorderLayout.EAST);

        notificationService.registerListener(this);
        refreshInfo();
    }

    public void refreshInfo() {
        Account current = AccountController.currentAccount;
        boolean loggedIn = (current != null);

        loginButton.setVisible(!loggedIn);
        createAccountButton.setVisible(!loggedIn);
        profileMenuContainer.setVisible(loggedIn);

        if (loggedIn) {
            btnMyReservations.setVisible(current.getRole() == Role.GUEST);
            profileButton.setIcon(buildProfileIcon(current));
            updateBadge(current.getEmail());
        } else {
            btnMyReservations.setVisible(false);
            profileButton.setIcon(null);
            profileBadgeLabel.setVisible(false);
        }

        revalidate();
        repaint();
    }

    @Override
    public void onNotificationsChanged(String email) {
        Account current = AccountController.currentAccount;
        if (current == null || !current.getEmail().equalsIgnoreCase(email)) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            updateBadge(current.getEmail());
            List<AppNotification> notifications = notificationService.getNotifications(current.getEmail());
            if (!notifications.isEmpty()) {
                AppNotification latest = notifications.get(0);
                String lastToastId = lastToastByAccount.get(current.getEmail());
                boolean isNew = !latest.getId().equals(lastToastId);
                if (isNew && notificationService.isInAppEnabled(current.getEmail(), latest.getType())) {
                    showToast(latest.getType().getDisplayName(), latest.getMessage());
                    lastToastByAccount.put(current.getEmail(), latest.getId());
                }
            }
        });
    }

    private static final int PROFILE_POPUP_WIDTH = 300;

    private void showAccountPopupMenu() {
        accountPopupMenu.removeAll();
        Color popupBg = new Color(255, 255, 255);
        accountPopupMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 210, 198), 1),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(popupBg);
        content.setBorder(new EmptyBorder(12, 14, 12, 14));

        Account current = AccountController.currentAccount;

        JLabel menuTitle = new JLabel("Account");
        menuTitle.setFont(new Font("SansSerif", Font.BOLD, 11));
        menuTitle.setForeground(UITheme.TEXT_MEDIUM);
        menuTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(menuTitle);
        content.add(Box.createRigidArea(new Dimension(0, 6)));

        JButton viewAccountButton = profileMenuRowButton("View account");
        viewAccountButton.addActionListener(_ -> {
            if (Main.accountCenterPage != null) {
                Main.accountCenterPage.refreshInfo();
            }
            cardLayout.show(pages, "account center");
            accountPopupMenu.setVisible(false);
        });
        content.add(viewAccountButton);

        if (current != null && (current.getRole() == Role.CLERK || current.getRole() == Role.ADMIN)) {
            content.add(Box.createRigidArea(new Dimension(0, 4)));
            JButton dashboardButton = profileMenuRowButton(
                    current.getRole() == Role.ADMIN ? "Admin dashboard" : "Staff dashboard");
            dashboardButton.addActionListener(_ -> {
                if (current.getRole() == Role.CLERK) {
                    cardLayout.show(pages, "clerk page");
                } else {
                    cardLayout.show(pages, "admin page");
                }
                accountPopupMenu.setVisible(false);
            });
            content.add(dashboardButton);
        }

        content.add(Box.createRigidArea(new Dimension(0, 10)));
        content.add(fullWidthSeparator());
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel notifHeader = new JPanel(new BorderLayout(8, 0));
        notifHeader.setOpaque(false);
        notifHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        notifHeader.setMaximumSize(new Dimension(PROFILE_POPUP_WIDTH, 24));
        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(UITheme.TEXT_DARK);
        JButton clearAll = new JButton("Clear all");
        clearAll.setFont(new Font("SansSerif", Font.PLAIN, 12));
        clearAll.setBorderPainted(false);
        clearAll.setContentAreaFilled(false);
        clearAll.setForeground(new Color(140, 104, 47));
        clearAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearAll.setFocusPainted(false);
        clearAll.addActionListener(_ -> {
            if (current != null) {
                notificationService.clearAll(current.getEmail());
            }
            accountPopupMenu.setVisible(false);
        });
        notifHeader.add(title, BorderLayout.WEST);
        notifHeader.add(clearAll, BorderLayout.EAST);
        content.add(notifHeader);
        content.add(Box.createRigidArea(new Dimension(0, 8)));

        List<AppNotification> notifications = current == null ? List.of() : notificationService.getNotifications(current.getEmail());
        if (notifications.isEmpty()) {
            JLabel empty = new JLabel("No notifications yet.");
            empty.setFont(new Font("SansSerif", Font.PLAIN, 13));
            empty.setForeground(UITheme.TEXT_MEDIUM);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(empty);
        } else {
            JPanel listPanel = new JPanel();
            listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
            listPanel.setOpaque(false);
            int showCount = Math.min(8, notifications.size());
            for (int i = 0; i < showCount; i++) {
                AppNotification notification = notifications.get(i);
                JPanel row = new JPanel(new BorderLayout(4, 0));
                row.setOpaque(true);
                row.setBackground(new Color(252, 250, 245));
                row.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(232, 226, 214), 1),
                        new EmptyBorder(5, 8, 5, 6)));
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                // Constrain width only so BoxLayout does not squash row height (fixed height was clipping HTML).
                row.setMaximumSize(new Dimension(PROFILE_POPUP_WIDTH, Integer.MAX_VALUE));

                String time = notification.getCreatedAt().atZone(ZoneId.systemDefault()).format(TIME_FORMAT);
                String msg = escapeHtmlNotif(notification.getMessage());
                if (msg.length() > 90) {
                    msg = msg.substring(0, 87) + "…";
                }
                String typeName = escapeHtmlNotif(notification.getType().getDisplayName());
                JLabel text = new JLabel("<html><div style=\"width:210px;margin:0;padding:2px 0 2px 0;font-size:11px;line-height:1.35;\">"
                        + "<b>" + typeName + "</b><br/>"
                        + "<span style=\"color:#555;\">" + msg + "</span>"
                        + " <span style=\"color:#999;font-size:10px;\">· " + time + "</span></div></html>");
                text.setFont(new Font("SansSerif", Font.PLAIN, 11));
                text.setForeground(UITheme.TEXT_DARK);
                text.setVerticalAlignment(SwingConstants.TOP);

                JButton clearOne = new JButton("×");
                clearOne.setFont(new Font("SansSerif", Font.BOLD, 12));
                clearOne.setMargin(new Insets(0, 2, 0, 2));
                clearOne.setBorderPainted(false);
                clearOne.setContentAreaFilled(false);
                clearOne.setForeground(UITheme.TEXT_MEDIUM);
                clearOne.setCursor(new Cursor(Cursor.HAND_CURSOR));
                clearOne.setFocusPainted(false);
                clearOne.setPreferredSize(new Dimension(18, 22));
                clearOne.addActionListener(_ -> {
                    if (current != null) {
                        notificationService.clearNotification(current.getEmail(), notification.getId());
                    }
                    accountPopupMenu.setVisible(false);
                });

                row.add(text, BorderLayout.CENTER);
                row.add(clearOne, BorderLayout.EAST);
                listPanel.add(row);
                if (i < showCount - 1) {
                    listPanel.add(Box.createRigidArea(new Dimension(0, 3)));
                }
            }
            JScrollPane scroll = new JScrollPane(listPanel);
            scroll.setBorder(BorderFactory.createLineBorder(new Color(232, 226, 214), 1));
            // Viewport height budget per row (multi-line HTML); scrollbar appears if list is taller.
            int approxRow = 62;
            int gapBetween = 3;
            scroll.setPreferredSize(new Dimension(PROFILE_POPUP_WIDTH,
                    Math.min(320, 12 + showCount * approxRow + Math.max(0, showCount - 1) * gapBetween)));
            scroll.getViewport().setBackground(popupBg);
            scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
            scroll.getVerticalScrollBar().setUnitIncrement(16);
            content.add(scroll);
        }

        content.add(Box.createRigidArea(new Dimension(0, 12)));
        content.add(fullWidthSeparator());
        content.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton logOutButton = new JButton("Log out");
        logOutButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        logOutButton.setForeground(Color.WHITE);
        logOutButton.setBackground(new Color(178, 48, 48));
        logOutButton.setOpaque(true);
        logOutButton.setBorderPainted(false);
        logOutButton.setFocusPainted(false);
        logOutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logOutButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        logOutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        logOutButton.setPreferredSize(new Dimension(PROFILE_POPUP_WIDTH - 28, 40));
        logOutButton.addActionListener(_ -> {
            accountPopupMenu.setVisible(false);
            AppSession.logout(pages, cardLayout);
        });
        content.add(logOutButton);

        accountPopupMenu.add(content);
        accountPopupMenu.pack();
        int x = Math.min(0, profileButton.getWidth() - accountPopupMenu.getPreferredSize().width);
        accountPopupMenu.show(profileButton, x, profileButton.getHeight() + 4);
    }

    private static JSeparator fullWidthSeparator() {
        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
        sep.setForeground(new Color(230, 224, 212));
        sep.setMaximumSize(new Dimension(PROFILE_POPUP_WIDTH, 10));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        return sep;
    }

    private static String escapeHtmlNotif(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    private static JButton profileMenuRowButton(String label) {
        JButton b = new JButton(label);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("SansSerif", Font.PLAIN, 14));
        b.setForeground(UITheme.TEXT_DARK);
        b.setBackground(new Color(248, 245, 238));
        b.setOpaque(true);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(228, 220, 206), 1),
                new EmptyBorder(8, 12, 8, 12)));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return b;
    }

    private void updateBadge(String email) {
        int unread = notificationService.getUnreadCount(email);
        if (unread <= 0) {
            profileBadgeLabel.setVisible(false);
            return;
        }
        profileBadgeLabel.setVisible(true);
        profileBadgeLabel.setText(unread > 9 ? "9+" : Integer.toString(unread));
    }

    private void showToast(String title, String message) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        if (owner == null) {
            return;
        }
        if (activeToast != null) {
            activeToast.dispose();
        }

        activeToast = new JDialog(owner);
        activeToast.setUndecorated(true);
        JPanel toast = new JPanel(new BorderLayout());
        toast.setBackground(new Color(249, 246, 238));
        toast.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        JLabel label = new JLabel("<html><b>" + title + "</b><br/>" + message + "</html>");
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(UITheme.TEXT_DARK);
        toast.add(label, BorderLayout.CENTER);
        activeToast.add(toast);
        activeToast.pack();

        int x = owner.getX() + owner.getWidth() - activeToast.getWidth() - 20;
        int y = owner.getY() + 80;
        activeToast.setLocation(x, y);
        activeToast.setAlwaysOnTop(true);
        activeToast.setVisible(true);

        Timer timer = new Timer(5000, _ -> {
            if (activeToast != null) {
                activeToast.dispose();
                activeToast = null;
            }
        });
        timer.setRepeats(false);
        timer.start();
    }

    private javax.swing.Icon buildProfileIcon(Account account) {
        int size = 36;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setClip(new java.awt.geom.Ellipse2D.Double(0, 0, size, size));

            if (account.getProfileImagePath() != null && !account.getProfileImagePath().isBlank()) {
                File file = new File(account.getProfileImagePath());
                if (file.exists()) {
                    Image raw = new javax.swing.ImageIcon(file.getAbsolutePath()).getImage();
                    g2.drawImage(raw, 0, 0, size, size, null);
                } else {
                    paintInitials(g2, account, size);
                }
            } else {
                paintInitials(g2, account, size);
            }
        } finally {
            g2.dispose();
        }
        return new javax.swing.ImageIcon(image);
    }

    private void paintInitials(Graphics2D g2, Account account, int size) {
        g2.setColor(new Color(194, 159, 92));
        g2.fillOval(0, 0, size, size);
        String initials = "?";
        if (account != null && account.getFirstName() != null && !account.getFirstName().isBlank()) {
            initials = account.getFirstName().substring(0, 1).toUpperCase();
        }
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        int textWidth = g2.getFontMetrics().stringWidth(initials);
        int textHeight = g2.getFontMetrics().getAscent();
        g2.drawString(initials, (size - textWidth) / 2, (size + textHeight) / 2 - 2);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}