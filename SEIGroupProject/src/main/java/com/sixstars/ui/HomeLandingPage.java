package com.sixstars.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HomeLandingPage extends JPanel {
    public HomeLandingPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        add(buildHeader(pages, cardLayout), BorderLayout.NORTH);
        add(buildContent(pages, cardLayout), BorderLayout.CENTER);
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
        createAccountButton.addActionListener(e -> cardLayout.show(pages, "create account"));

        navPanel.add(bookNowButton);
        navPanel.add(loginButton);
        navPanel.add(createAccountButton);

        header.add(brandLabel, BorderLayout.WEST);
        header.add(navPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildContent(JPanel pages, CardLayout cardLayout) {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 30, 24, 30));

        JPanel heroCard = new JPanel();
        heroCard.setLayout(new BoxLayout(heroCard, BoxLayout.Y_AXIS));
        heroCard.setBackground(UITheme.CARD_BACKGROUND);
        heroCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(26, 30, 26, 30)
        ));
        heroCard.setAlignmentX(Component.CENTER_ALIGNMENT);
        heroCard.setMaximumSize(new Dimension(860, 320));

        JLabel heroTitle = new JLabel("Experience Timeless Luxury");
        heroTitle.setFont(new Font("Serif", Font.BOLD, 38));
        heroTitle.setForeground(UITheme.TEXT_DARK);
        heroTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heroSubtitle = new JLabel("Elegant suites, curated dining, and exceptional hospitality.");
        heroSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 18));
        heroSubtitle.setForeground(UITheme.TEXT_MEDIUM);
        heroSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel heroDetail = new JLabel("Plan your stay with comfort and confidence in just a few clicks.");
        heroDetail.setFont(new Font("SansSerif", Font.PLAIN, 15));
        heroDetail.setForeground(UITheme.TEXT_MEDIUM);
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

        JPanel highlightsPanel = new JPanel(new GridLayout(1, 3, 16, 0));
        highlightsPanel.setOpaque(false);
        highlightsPanel.setMaximumSize(new Dimension(860, 180));

        highlightsPanel.add(createFeatureCard("Signature Suites", "Refined spaces with panoramic city and ocean views."));
        highlightsPanel.add(createFeatureCard("Fine Dining", "Chef-led seasonal menus and handcrafted cocktails."));
        highlightsPanel.add(createFeatureCard("Wellness Retreat", "Spa, pool, and fitness amenities tailored for calm."));

        content.add(heroCard);
        content.add(Box.createRigidArea(new Dimension(0, 18)));
        content.add(highlightsPanel);
        return content;
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
}
