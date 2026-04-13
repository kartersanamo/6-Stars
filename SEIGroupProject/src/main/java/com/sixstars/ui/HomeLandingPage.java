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
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.sixstars.app.Main;
import com.sixstars.model.Room;
import com.sixstars.service.RoomService;

public class HomeLandingPage extends JPanel {
    private static final String HERO_IMAGE_PATH = "assets/6Stars-Background.jpg";
    private static final String ROOM_IMAGE_PATH = "assets/6Stars-Room.jpg";

    private final Image heroImage;
    private final Image roomImage;
    private final RoomService roomService;

    public HomeLandingPage(JPanel pages, CardLayout cardLayout) {
        this.roomService = new RoomService();
        this.heroImage = loadImage(HERO_IMAGE_PATH);
        this.roomImage = loadImage(ROOM_IMAGE_PATH);

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        add(buildHeader(pages, cardLayout), BorderLayout.NORTH);
        add(buildContentScrollPane(pages, cardLayout), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader(JPanel pages, CardLayout cardLayout) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CARD_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(16, 28, 16, 28)
        ));

        JLabel brandLabel = new JLabel("6 Stars Hotel");
        brandLabel.setFont(new Font("Serif", Font.BOLD, 30));
        brandLabel.setForeground(UITheme.TEXT_DARK);

        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);

        JButton bookNowButton = createHeaderButton("Book Now");
        JButton loginButton = createHeaderButton("Login");
        JButton createAccountButton = createHeaderButton("Create Account");

        bookNowButton.addActionListener(e -> cardLayout.show(pages, "make reservation"));
        loginButton.addActionListener(e -> cardLayout.show(pages, "login"));
        createAccountButton.addActionListener(e -> {
            cardLayout.show(pages, "create account");
            Main.createAccountPage.refresh();
        });

        navPanel.add(bookNowButton);
        navPanel.add(loginButton);
        navPanel.add(createAccountButton);

        header.add(brandLabel, BorderLayout.WEST);
        header.add(navPanel, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildContentScrollPane(JPanel pages, CardLayout cardLayout) {
        JPanel content = buildContent(pages, cardLayout);
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel buildContent(JPanel pages, CardLayout cardLayout) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(true);
        content.setBackground(UITheme.PAGE_BACKGROUND);
        content.setBorder(new EmptyBorder(24, 30, 24, 30));

        JPanel heroCard = new ImagePanel(heroImage);
        heroCard.setLayout(new BoxLayout(heroCard, BoxLayout.Y_AXIS));
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(26, 30, 26, 30)
        ));
        heroCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroCard.setMaximumSize(new Dimension(860, 360));
        heroCard.setPreferredSize(new Dimension(860, 360));

        JLabel heroTitle = new JLabel("Experience Timeless Luxury");
        heroTitle.setFont(new Font("Serif", Font.BOLD, 38));
        heroTitle.setForeground(Color.WHITE);
        heroTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heroSubtitle = new JLabel("Elegant suites, curated dining, and exceptional hospitality.");
        heroSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        heroSubtitle.setForeground(new Color(245, 245, 245));
        heroSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heroDetail = new JLabel("Plan your stay with comfort and confidence in just a few clicks.");
        heroDetail.setFont(new Font("SansSerif", Font.PLAIN, 15));
        heroDetail.setForeground(new Color(236, 236, 236));
        heroDetail.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel heroActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        heroActions.setOpaque(false);

        JButton reserveButton = createPrimaryButton("Reserve Your Stay");
        JButton exploreButton = createSecondaryButton("Enter Guest Portal");

        reserveButton.addActionListener(e -> cardLayout.show(pages, "make reservation"));
        exploreButton.addActionListener(e -> cardLayout.show(pages, "welcome"));

        heroActions.add(reserveButton);
        heroActions.add(exploreButton);

        heroCard.add(heroTitle);
        heroCard.add(Box.createRigidArea(new Dimension(0, 12)));
        heroCard.add(heroSubtitle);
        heroCard.add(Box.createRigidArea(new Dimension(0, 10)));
        heroCard.add(heroDetail);
        heroCard.add(Box.createRigidArea(new Dimension(0, 24)));
        heroCard.add(heroActions);

        JLabel roomsPreviewTitle = new JLabel("Room Collection Preview");
        roomsPreviewTitle.setFont(new Font("Serif", Font.BOLD, 30));
        roomsPreviewTitle.setForeground(UITheme.TEXT_DARK);
        roomsPreviewTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel roomsPreviewSubtitle = new JLabel("Every room currently available in the 6 Stars database");
        roomsPreviewSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 16));
        roomsPreviewSubtitle.setForeground(UITheme.TEXT_MEDIUM);
        roomsPreviewSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel roomsPreviewPanel = buildRoomsPreviewPanel();

        JPanel highlightsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        highlightsPanel.setOpaque(false);
        highlightsPanel.setMaximumSize(new Dimension(860, 180));

        highlightsPanel.add(createFeatureCard("Signature Suites", "Refined spaces with panoramic city and ocean views."));
        highlightsPanel.add(createFeatureCard("Fine Dining", "Chef-led seasonal menus and handcrafted cocktails."));
        highlightsPanel.add(createFeatureCard("Wellness Retreat", "Spa, pool, and fitness amenities tailored for calm."));

        content.add(heroCard);
        content.add(Box.createRigidArea(new Dimension(0, 18)));
        content.add(highlightsPanel);
        content.add(Box.createRigidArea(new Dimension(0, 26)));
        content.add(roomsPreviewTitle);
        content.add(Box.createRigidArea(new Dimension(0, 8)));
        content.add(roomsPreviewSubtitle);
        content.add(Box.createRigidArea(new Dimension(0, 16)));
        content.add(roomsPreviewPanel);
        content.add(Box.createVerticalGlue());
        return content;
    }

    private JPanel buildRoomsPreviewPanel() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 16, 16));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(860, Integer.MAX_VALUE));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        List<Room> allRooms = roomService.getAllRooms();
        for (Room room : allRooms) {
            panel.add(createRoomPreviewCard(room));
        }

        if (allRooms.isEmpty()) {
            JPanel emptyCard = new JPanel(new BorderLayout());
            emptyCard.setBackground(UITheme.CARD_BACKGROUND);
            emptyCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                    new EmptyBorder(24, 20, 24, 20)
            ));
            JLabel emptyLabel = new JLabel("No rooms found in the database.");
            emptyLabel.setForeground(UITheme.TEXT_MEDIUM);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
            emptyCard.add(emptyLabel, BorderLayout.CENTER);
            panel.add(emptyCard);
        }

        return panel;
    }

    private JPanel createRoomPreviewCard(Room room) {
        JPanel card = new ImagePanel(roomImage);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(14, 16, 14, 16)
        ));
        card.setPreferredSize(new Dimension(422, 215));

        JLabel roomTitle = new JLabel("Room " + room.getRoomNumber());
        roomTitle.setFont(new Font("Serif", Font.BOLD, 26));
        roomTitle.setForeground(Color.WHITE);
        roomTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel bedTypeLabel = createRoomDetailLabel("Bed Type: " + room.getBedType());
        JLabel themeLabel = createRoomDetailLabel("Theme: " + room.getTheme());
        JLabel qualityLabel = createRoomDetailLabel("Quality: " + room.getQualityLevel());
        JLabel smokingLabel = createRoomDetailLabel("Smoking: " + (room.isSmoking() ? "Yes" : "No"));

        card.add(roomTitle);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(bedTypeLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(themeLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(qualityLabel);
        card.add(Box.createRigidArea(new Dimension(0, 5)));
        card.add(smokingLabel);

        return card;
    }

    private JLabel createRoomDetailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(new Color(248, 248, 248));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createFeatureCard(String title, String text) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(18, 16, 18, 16)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 22));
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descLabel = new JLabel("<html><div style='text-align:center;'>" + text + "</div></html>");
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descLabel.setForeground(UITheme.TEXT_MEDIUM);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(descLabel);
        return card;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.CARD_BACKGROUND);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(10, 20, 10, 20)
        ));

        JLabel contactLabel = new JLabel("6 Stars Hotel | 24/7 Concierge | +1 (800) 777-STAR");
        contactLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        contactLabel.setForeground(UITheme.TEXT_MEDIUM);

        JLabel policyLabel = new JLabel("Check-in: 3:00 PM  |  Check-out: 11:00 AM");
        policyLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        policyLabel.setForeground(UITheme.TEXT_MEDIUM);
        policyLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        footer.add(contactLabel, BorderLayout.WEST);
        footer.add(policyLabel, BorderLayout.EAST);
        return footer;
    }

    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(220, 44));
        button.setFont(UITheme.BUTTON_FONT);
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(220, 44));
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
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
        private final Image backgroundImage;

        ImagePanel(Image backgroundImage) {
            this.backgroundImage = backgroundImage;
            setBackground(UITheme.CARD_BACKGROUND);
            setOpaque(true);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                g2.setColor(new Color(20, 20, 20, 90));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }
}
