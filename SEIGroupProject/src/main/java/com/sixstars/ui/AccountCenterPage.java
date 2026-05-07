package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.CardLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class AccountCenterPage extends JPanel {
    private static final int AVATAR_SIZE = 120;
    private static final int AVATAR_EXPORT_SIZE = 512;
    private static final long MAX_AVATAR_BYTES = 5L * 1024L * 1024L;
    private static final int MIN_AVATAR_DIMENSION = 128;
    private static final char MASK_ECHO = new JPasswordField().getEchoChar();
    private static final String PREF_NODE_PREFIX = "account-center-";
    private static final Color SIDEBAR_BG = new Color(245, 240, 228);
    private static final Color SIDEBAR_HOVER = new Color(235, 225, 208);
    private static final Color SIDEBAR_SELECTED = UITheme.ACCENT_GOLD;
    private static final Color SIDEBAR_PANEL = new Color(252, 249, 241);
    private static final Color SIDEBAR_TEXT_MUTED = new Color(112, 103, 90);

    private final JPanel pages;
    private final CardLayout cardLayout;
    private final AccountController accountController;
    private final Preferences preferencesRoot = Preferences.userNodeForPackage(AccountCenterPage.class);

    // Sidebar navigation buttons
    private JButton accountInfoButton;
    private JButton securityButton;
    private JButton notificationsButton;
    private JButton billingButton;
    private JButton purchasesButton;
    private JButton dangerZoneButton;

    // Content panels for each section
    private final JPanel contentArea;
    private final CardLayout contentLayout;

    // Avatar and quick info
    private final AvatarPanel avatarPanel = new AvatarPanel();
    private final JLabel nameLabel = new JLabel("User");
    private final JLabel emailLabel = new JLabel("email@example.com");
    private final JLabel roleLabel = new JLabel("Guest");

    // Account Info Section Components
    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JLabel emailValueLabel = new JLabel();
    private final JLabel roleValueLabel = new JLabel();
    private final JButton uploadPhotoButton = new JButton("Upload Photo");
    private final JButton removePhotoButton = new JButton("Remove Photo");
    private final JButton saveProfileButton = new JButton("Save Changes");
    private final JLabel avatarStatusLabel = new JLabel();

    // Security Section Components
    private final JPasswordField currentPasswordField = new JPasswordField();
    private final JPasswordField newPasswordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();
    private final JCheckBox showPasswordsCheck = new JCheckBox("Show passwords");
    private final JButton updatePasswordButton = new JButton("Update Password");
    private final JLabel passwordRulesLabel = new JLabel();

    // Notifications Section Components
    private final JCheckBox emailReceiptsCheck = new JCheckBox("Email receipts for reservations and shop orders");
    private final JCheckBox reservationReminderCheck = new JCheckBox("Reservation reminder notifications");
    private final JCheckBox shopPromotionsCheck = new JCheckBox("Occasional shop promotions and special offers");

    private boolean loadingPreferences = false;

    public AccountCenterPage(JPanel pages, CardLayout cardLayout, AccountController accountController) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.accountController = accountController;

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        // Initialize contentLayout FIRST (required by createSidebar)
        contentLayout = new CardLayout();
        contentArea = new JPanel(contentLayout);
        contentArea.setBackground(UITheme.PAGE_BACKGROUND);
        contentArea.add(wrapInScrollPane(createAccountInfoPanel()), "account-info");
        contentArea.add(wrapInScrollPane(createSecurityPanel()), "security");
        contentArea.add(wrapInScrollPane(createNotificationsPanel()), "notifications");
        contentArea.add(wrapInScrollPane(createBillingPanel()), "billing");
        contentArea.add(wrapInScrollPane(createPurchasesPanel()), "purchases");
        contentArea.add(wrapInScrollPane(createDangerZonePanel()), "danger-zone");

        // Create sidebar (now contentLayout is ready)
        JPanel sidebarPanel = createSidebar();
        JScrollPane sidebarScroll = new JScrollPane(sidebarPanel);
        sidebarScroll.setBorder(null);
        sidebarScroll.setOpaque(false);
        sidebarScroll.getViewport().setOpaque(false);
        sidebarScroll.setPreferredSize(new Dimension(280, 0));

        // Main layout: sidebar + content
        add(sidebarScroll, BorderLayout.WEST);
        add(contentArea, BorderLayout.CENTER);

        setupEventListeners();
        refreshInfo();
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_BG);
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UITheme.BORDER_COLOR),
                new EmptyBorder(16, 14, 16, 14)
        ));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(SIDEBAR_PANEL);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(224, 214, 195), 1, true),
                new EmptyBorder(18, 16, 16, 16)
        ));
        headerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        avatarPanel.setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarPanel.setMaximumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(avatarPanel);

        headerPanel.add(Box.createRigidArea(new Dimension(0, 12)));

        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        nameLabel.setForeground(UITheme.TEXT_DARK);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(nameLabel);

        emailLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        emailLabel.setForeground(SIDEBAR_TEXT_MUTED);
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(emailLabel);

        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        roleLabel.setForeground(new Color(133, 106, 53));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(roleLabel);

        sidebar.add(headerPanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));

        accountInfoButton = createNavButton("Account Information", "account-info");
        securityButton = createNavButton("Security", "security");
        notificationsButton = createNavButton("Notifications", "notifications");
        billingButton = createNavButton("Billing", "billing");
        purchasesButton = createNavButton("Purchases", "purchases");
        dangerZoneButton = createNavButton("Danger Zone", "danger-zone");

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(SIDEBAR_PANEL);
        navPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(224, 214, 195), 1, true),
                new EmptyBorder(12, 10, 12, 10)
        ));
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        navPanel.add(createSidebarSectionLabel("Account"));
        navPanel.add(createNavButtonRow(accountInfoButton));
        navPanel.add(createNavButtonRow(securityButton));
        navPanel.add(createNavButtonRow(notificationsButton));
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        navPanel.add(createSidebarSectionLabel("Hotel Services"));
        navPanel.add(createNavButtonRow(billingButton));
        navPanel.add(createNavButtonRow(purchasesButton));
        sidebar.add(navPanel);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel dangerPanel = new JPanel();
        dangerPanel.setLayout(new BoxLayout(dangerPanel, BoxLayout.Y_AXIS));
        dangerPanel.setBackground(new Color(252, 246, 246));
        dangerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(234, 206, 206), 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        dangerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dangerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        dangerPanel.add(createSidebarSectionLabel("Danger Zone"));
        dangerPanel.add(createNavButtonRow(dangerZoneButton));
        sidebar.add(dangerPanel);

        sidebar.add(Box.createVerticalGlue());

        JButton backButton = new JButton("Back to Dashboard");
        backButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        backButton.setForeground(UITheme.TEXT_DARK);
        backButton.setBackground(new Color(246, 241, 230));
        backButton.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(219, 207, 184), 1, true),
                new EmptyBorder(11, 14, 11, 14)
        ));
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setOpaque(true);
        backButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        backButton.addActionListener(_ -> navigateBack());
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                backButton.setBackground(new Color(240, 232, 215));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backButton.setBackground(new Color(246, 241, 230));
            }
        });

        JPanel backPanel = new JPanel();
        backPanel.setOpaque(false);
        backPanel.setLayout(new BoxLayout(backPanel, BoxLayout.X_AXIS));
        backPanel.setBorder(new EmptyBorder(14, 0, 0, 0));
        backPanel.add(backButton);
        backPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        sidebar.add(backPanel);

        // Set initial selection
        selectNavButton(accountInfoButton);
        contentLayout.show(contentArea, "account-info");

        return sidebar;
    }

    private JButton createNavButton(String text, String contentKey) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setForeground(UITheme.TEXT_DARK);
        button.setBackground(SIDEBAR_PANEL);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(11, 12, 11, 12));
        button.setMargin(new java.awt.Insets(0, 0, 0, 0));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setMinimumSize(new Dimension(0, 42));

        button.addActionListener(_ -> {
            selectNavButton(button);
            contentLayout.show(contentArea, contentKey);
        });

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!button.getForeground().equals(Color.WHITE)) {
                    button.setBackground(SIDEBAR_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.getModel().isArmed()) {
                    return;
                }
                if (!button.getForeground().equals(Color.WHITE)) {
                    button.setBackground(SIDEBAR_PANEL);
                }
            }
        });

        return button;
    }

    private void selectNavButton(JButton button) {
        // Deselect all buttons
        accountInfoButton.setBackground(SIDEBAR_PANEL);
        accountInfoButton.setForeground(UITheme.TEXT_DARK);
        securityButton.setBackground(SIDEBAR_PANEL);
        securityButton.setForeground(UITheme.TEXT_DARK);
        notificationsButton.setBackground(SIDEBAR_PANEL);
        notificationsButton.setForeground(UITheme.TEXT_DARK);
        billingButton.setBackground(SIDEBAR_PANEL);
        billingButton.setForeground(UITheme.TEXT_DARK);
        purchasesButton.setBackground(SIDEBAR_PANEL);
        purchasesButton.setForeground(UITheme.TEXT_DARK);
        dangerZoneButton.setBackground(SIDEBAR_PANEL);
        dangerZoneButton.setForeground(UITheme.TEXT_DARK);

        // Select the clicked button
        button.setBackground(SIDEBAR_SELECTED);
        button.setForeground(Color.WHITE);
    }

    private JLabel createSidebarSectionLabel(String text) {
        JLabel label = new JLabel(text.toUpperCase(Locale.ROOT));
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(SIDEBAR_TEXT_MUTED);
        label.setBorder(new EmptyBorder(4, 12, 8, 0));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        label.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        return label;
    }

    private JPanel createNavButtonRow(JButton button) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.add(button);
        return row;
    }


    private JPanel createAccountInfoPanel() {
        JPanel mainPanel = createContentPanel();

        JLabel title = new JLabel("Account Information");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Update your profile details and upload a profile picture.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        // Avatar section
        JPanel avatarSection = createCardPanel();
        avatarSection.add(createSectionTitle("Profile Picture", "Upload or remove your profile photo"));
        avatarSection.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel avatarContent = new JPanel(new BorderLayout(20, 0));
        avatarContent.setOpaque(false);

        JPanel avatarStack = new JPanel();
        avatarStack.setOpaque(false);
        avatarStack.setLayout(new BoxLayout(avatarStack, BoxLayout.Y_AXIS));
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarPanel.setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarStack.add(avatarPanel);
        avatarStack.add(Box.createRigidArea(new Dimension(0, 12)));
        avatarStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        avatarStatusLabel.setForeground(UITheme.TEXT_MEDIUM);
        avatarStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarStack.add(avatarStatusLabel);

        JPanel avatarFieldsPanel = new JPanel();
        avatarFieldsPanel.setOpaque(false);
        avatarFieldsPanel.setLayout(new BoxLayout(avatarFieldsPanel, BoxLayout.Y_AXIS));
        avatarFieldsPanel.add(uploadPhotoButton);
        avatarFieldsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        avatarFieldsPanel.add(removePhotoButton);
        styleButton(uploadPhotoButton, true);
        styleButton(removePhotoButton, false);

        avatarContent.add(avatarStack, BorderLayout.WEST);
        avatarContent.add(avatarFieldsPanel, BorderLayout.CENTER);
        avatarSection.add(avatarContent);
        mainPanel.add(avatarSection);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        // Profile details section
        JPanel detailsSection = createCardPanel();
        detailsSection.add(createSectionTitle("Personal Information", "Your account details"));
        detailsSection.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel nameRow = new JPanel(new GridLayout(1, 2, 12, 0));
        nameRow.setOpaque(false);
        nameRow.add(createLabeledFieldCard("First Name", firstNameField, true));
        nameRow.add(createLabeledFieldCard("Last Name", lastNameField, true));
        detailsSection.add(nameRow);

        detailsSection.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel emailRow = new JPanel(new GridLayout(1, 2, 12, 0));
        emailRow.setOpaque(false);
        emailRow.add(createLabeledFieldCard("Email Address", emailValueLabel, false));
        emailRow.add(createLabeledFieldCard("Role", roleValueLabel, false));
        detailsSection.add(emailRow);

        detailsSection.add(Box.createRigidArea(new Dimension(0, 16)));
        detailsSection.add(styleButton(saveProfileButton, true));

        mainPanel.add(detailsSection);

        return mainPanel;
    }

    private JPanel createSecurityPanel() {
        JPanel mainPanel = createContentPanel();

        JLabel title = new JLabel("Security");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Manage your password and security settings.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel securityCard = createCardPanel();
        securityCard.add(createSectionTitle("Change Password", "Update your password with current-password verification"));
        securityCard.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel passwordGrid = new JPanel(new GridLayout(3, 1, 0, 12));
        passwordGrid.setOpaque(false);
        passwordGrid.add(createPasswordCard("Current Password", currentPasswordField));
        passwordGrid.add(createPasswordCard("New Password", newPasswordField));
        passwordGrid.add(createPasswordCard("Confirm New Password", confirmPasswordField));
        securityCard.add(passwordGrid);

        securityCard.add(Box.createRigidArea(new Dimension(0, 12)));
        showPasswordsCheck.setOpaque(false);
        showPasswordsCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
        showPasswordsCheck.setForeground(UITheme.TEXT_DARK);
        securityCard.add(showPasswordsCheck);

        securityCard.add(Box.createRigidArea(new Dimension(0, 12)));
        passwordRulesLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        passwordRulesLabel.setForeground(UITheme.TEXT_MEDIUM);
        passwordRulesLabel.setText("<html><b>Password requirements:</b> At least 8 characters, including uppercase, lowercase, digit, and special character.</html>");
        securityCard.add(passwordRulesLabel);

        securityCard.add(Box.createRigidArea(new Dimension(0, 16)));
        securityCard.add(styleButton(updatePasswordButton, true));

        mainPanel.add(securityCard);

        return mainPanel;
    }

    private JPanel createNotificationsPanel() {
        JPanel mainPanel = createContentPanel();

        JLabel title = new JLabel("Notifications");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Choose how we should communicate with you. Preferences are saved locally.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel notificationCard = createCardPanel();
        notificationCard.add(createSectionTitle("Email Preferences", "Control your email notifications"));
        notificationCard.add(Box.createRigidArea(new Dimension(0, 16)));

        emailReceiptsCheck.setOpaque(false);
        emailReceiptsCheck.setFont(new Font("SansSerif", Font.PLAIN, 14));
        emailReceiptsCheck.setForeground(UITheme.TEXT_DARK);

        reservationReminderCheck.setOpaque(false);
        reservationReminderCheck.setFont(new Font("SansSerif", Font.PLAIN, 14));
        reservationReminderCheck.setForeground(UITheme.TEXT_DARK);

        shopPromotionsCheck.setOpaque(false);
        shopPromotionsCheck.setFont(new Font("SansSerif", Font.PLAIN, 14));
        shopPromotionsCheck.setForeground(UITheme.TEXT_DARK);

        notificationCard.add(emailReceiptsCheck);
        notificationCard.add(Box.createRigidArea(new Dimension(0, 12)));
        notificationCard.add(reservationReminderCheck);
        notificationCard.add(Box.createRigidArea(new Dimension(0, 12)));
        notificationCard.add(shopPromotionsCheck);

        mainPanel.add(notificationCard);

        return mainPanel;
    }

    private JPanel createBillingPanel() {
        JPanel mainPanel = createContentPanel();

        JLabel title = new JLabel("Billing & Invoices");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("View your invoices and billing information.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel billingCard = createCardPanel();
        billingCard.add(createSectionTitle("View Billing Information", "Access your billing history and invoices"));
        billingCard.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel billingInfo = new JLabel("Click the button below to access your billing information and past invoices.");
        billingInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        billingInfo.setForeground(UITheme.TEXT_MEDIUM);
        billingCard.add(billingInfo);

        billingCard.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton viewBillingButton = new JButton("View Billing & Invoices");
        viewBillingButton.addActionListener(_ -> {
            if (Main.billingPage != null) {
                Main.billingPage.refresh();
            }
            cardLayout.show(pages, "billing page");
        });
        styleButton(viewBillingButton, true);
        billingCard.add(viewBillingButton);

        mainPanel.add(billingCard);

        return mainPanel;
    }

    private JPanel createPurchasesPanel() {
        JPanel mainPanel = createContentPanel();

        JLabel title = new JLabel("Purchases & Shop");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("View your shop purchases and browse the store.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel purchasesCard = createCardPanel();
        purchasesCard.add(createSectionTitle("Your Shop Activity", "Browse and manage your shop purchases"));
        purchasesCard.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel purchasesInfo = new JLabel("Visit the shop to browse our latest items and review your past purchases.");
        purchasesInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        purchasesInfo.setForeground(UITheme.TEXT_MEDIUM);
        purchasesCard.add(purchasesInfo);

        purchasesCard.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton viewShopButton = new JButton("Open Shop");
        viewShopButton.addActionListener(_ -> {
            if (Main.shopPage != null) {
                Main.shopPage.refreshPage();
            }
            cardLayout.show(pages, "shop");
        });
        styleButton(viewShopButton, true);
        purchasesCard.add(viewShopButton);

        mainPanel.add(purchasesCard);

        return mainPanel;
    }

    private JPanel createDangerZonePanel() {
        JPanel mainPanel = createContentPanel();

        JLabel title = new JLabel("Danger Zone");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(new Color(200, 50, 50));

        JLabel subtitle = new JLabel("Irreversible account actions. Proceed with caution.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel logoutCard = createCardPanel();
        logoutCard.setBackground(new Color(252, 248, 248));
        logoutCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 180, 180), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));

        logoutCard.add(createSectionTitle("Sign Out", "Log out of your account on this device"));
        logoutCard.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel logoutInfo = new JLabel("<html>You will be logged out and returned to the home page. Your shopping cart will be saved.</html>");
        logoutInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        logoutInfo.setForeground(UITheme.TEXT_MEDIUM);
        logoutCard.add(logoutInfo);

        logoutCard.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton logoutButton = new JButton("Sign Out");
        logoutButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        logoutButton.setFocusPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setOpaque(true);
        logoutButton.setBorderPainted(false);
        logoutButton.setPreferredSize(new Dimension(10, 42));
        logoutButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        logoutButton.setBackground(new Color(200, 50, 50));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.addActionListener(_ -> logout());
        logoutCard.add(logoutButton);

        mainPanel.add(logoutCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel deleteCard = createCardPanel();
        deleteCard.setBackground(new Color(252, 244, 244));
        deleteCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 150, 150), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        deleteCard.add(createSectionTitle("Delete Account", "Permanently remove your account from the system"));
        deleteCard.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel deleteInfo = new JLabel("<html>This action is permanent and cannot be undone. " +
                "You must verify your email first, then pass two confirmations.</html>");
        deleteInfo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        deleteInfo.setForeground(UITheme.TEXT_MEDIUM);
        deleteCard.add(deleteInfo);
        deleteCard.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton deleteAccountButton = new JButton("Delete My Account");
        deleteAccountButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        deleteAccountButton.setFocusPainted(false);
        deleteAccountButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteAccountButton.setOpaque(true);
        deleteAccountButton.setBorderPainted(false);
        deleteAccountButton.setPreferredSize(new Dimension(10, 42));
        deleteAccountButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        deleteAccountButton.setBackground(new Color(179, 35, 35));
        deleteAccountButton.setForeground(Color.WHITE);
        deleteAccountButton.addActionListener(_ -> deleteAccountWithVerification());
        deleteCard.add(deleteAccountButton);

        mainPanel.add(deleteCard);

        return mainPanel;
    }

    private JPanel createContentPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UITheme.PAGE_BACKGROUND);
        panel.setBorder(new EmptyBorder(32, 32, 32, 32));
        return panel;
    }

    private JPanel wrapInScrollPane(JPanel contentPanel) {
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.PAGE_BACKGROUND);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        return card;
    }

    private JPanel createSectionTitle(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(subtitleLabel);

        return panel;
    }

    private JPanel createLabeledFieldCard(String label, JComponent component, boolean editable) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(new Color(249, 249, 249));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_MEDIUM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (component instanceof JTextField textField) {
            styleTextField(textField);
            textField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
            textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        } else if (component instanceof JLabel valueLabel) {
            valueLabel.setFont(UITheme.INPUT_FONT);
            valueLabel.setForeground(UITheme.TEXT_DARK);
            valueLabel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(UITheme.BORDER_COLOR, 1, true),
                    new EmptyBorder(8, 10, 8, 10)
            ));
            valueLabel.setOpaque(true);
            valueLabel.setBackground(Color.WHITE);
            valueLabel.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
            valueLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        }
        if (!editable && component instanceof JTextField textField) {
            textField.setEditable(false);
        }
        card.add(component);
        return card;
    }

    private JPanel createPasswordCard(String label, JPasswordField field) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(new Color(249, 249, 249));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_MEDIUM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        stylePasswordField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(field);
        return card;
    }

    private JButton styleButton(JButton button, boolean primary) {
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(10, 42));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        if (primary) {
            button.setBackground(UITheme.ACCENT_GOLD);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(UITheme.SECONDARY_BUTTON);
            button.setForeground(UITheme.TEXT_DARK);
        }
        return button;
    }

    private void styleTextField(JTextField field) {
        field.setFont(UITheme.INPUT_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(UITheme.TEXT_DARK);
        field.setCaretColor(UITheme.TEXT_DARK);
    }

    private void stylePasswordField(JPasswordField field) {
        field.setFont(UITheme.INPUT_FONT);
        field.setEchoChar(MASK_ECHO);
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setBackground(Color.WHITE);
        field.setForeground(UITheme.TEXT_DARK);
        field.setCaretColor(UITheme.TEXT_DARK);
    }

    private void setupEventListeners() {
        uploadPhotoButton.addActionListener(e -> uploadProfilePhoto());
        removePhotoButton.addActionListener(e -> removeProfilePhoto());
        saveProfileButton.addActionListener(e -> saveProfileChanges());
        updatePasswordButton.addActionListener(e -> updatePassword());
        showPasswordsCheck.addActionListener(e -> togglePasswordVisibility(showPasswordsCheck.isSelected()));

        ActionListener preferenceListener = e -> savePreferences();
        emailReceiptsCheck.addActionListener(preferenceListener);
        reservationReminderCheck.addActionListener(preferenceListener);
        shopPromotionsCheck.addActionListener(preferenceListener);
    }

    public void refreshInfo() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            resetForNoAccount();
            return;
        }

        nameLabel.setText(account.getFirstName() + " " + account.getLastName());
        emailLabel.setText(account.getEmail());
        roleLabel.setText(formatRole(account.getRole().name()));

        firstNameField.setText(account.getFirstName());
        lastNameField.setText(account.getLastName());
        emailValueLabel.setText(account.getEmail());
        roleValueLabel.setText(formatRole(account.getRole().name()));

        avatarPanel.setAccount(account);
        avatarStatusLabel.setText(account.getProfileImagePath() == null || account.getProfileImagePath().isBlank()
                ? "No profile photo uploaded yet."
                : "Profile photo is uploaded and ready.");

        clearPasswordFields();
        togglePasswordVisibility(showPasswordsCheck.isSelected());
        loadPreferences(account);

        revalidate();
        repaint();
    }

    private void resetForNoAccount() {
        nameLabel.setText("User");
        emailLabel.setText("email@example.com");
        roleLabel.setText("Guest");
        avatarPanel.clear();
        clearPasswordFields();
        showPasswordsCheck.setSelected(false);
        togglePasswordVisibility(false);
    }

    private void loadPreferences(Account account) {
        loadingPreferences = true;
        try {
            Preferences prefs = preferencesFor(account);
            emailReceiptsCheck.setSelected(prefs.getBoolean("emailReceipts", true));
            reservationReminderCheck.setSelected(prefs.getBoolean("reservationReminders", true));
            shopPromotionsCheck.setSelected(prefs.getBoolean("shopPromotions", false));
        } finally {
            loadingPreferences = false;
        }
    }

    private void savePreferences() {
        if (loadingPreferences) {
            return;
        }

        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }

        Preferences prefs = preferencesFor(account);
        prefs.putBoolean("emailReceipts", emailReceiptsCheck.isSelected());
        prefs.putBoolean("reservationReminders", reservationReminderCheck.isSelected());
        prefs.putBoolean("shopPromotions", shopPromotionsCheck.isSelected());
    }

    private Preferences preferencesFor(Account account) {
        return preferencesRoot.node(PREF_NODE_PREFIX + sanitizeNodeName(account.getEmail()));
    }

    private String sanitizeNodeName(String input) {
        return input == null ? "unknown" : input.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase(Locale.ROOT);
    }

    private void togglePasswordVisibility(boolean visible) {
        char echo = visible ? (char) 0 : MASK_ECHO;
        currentPasswordField.setEchoChar(echo);
        newPasswordField.setEchoChar(echo);
        confirmPasswordField.setEchoChar(echo);
    }

    private void clearPasswordFields() {
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    private void saveProfileChanges() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        if (firstName.isEmpty() || lastName.isEmpty()) {
            return;
        }

        try {
            accountController.updateProfileDetails(firstName, lastName, account.getProfileImagePath());
            Main.headerBar.refreshInfo();
            refreshInfo();
        } catch (Exception ex) {
            // Handle error silently or show notification
        }
    }

    private void updatePassword() {
        try {
            accountController.changePassword(
                    new String(currentPasswordField.getPassword()),
                    new String(newPasswordField.getPassword()),
                    new String(confirmPasswordField.getPassword())
            );
            clearPasswordFields();
            Main.headerBar.refreshInfo();
            refreshInfo();
        } catch (Exception ex) {
            // Handle error
        }
    }

    private void uploadProfilePhoto() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a profile photo");
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("Image files (*.png, *.jpg, *.jpeg, *.gif)", "png", "jpg", "jpeg", "gif"));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selected = chooser.getSelectedFile();
        try {
            String newPath = importAvatarImage(selected, account.getEmail());
            String previousPath = account.getProfileImagePath();
            accountController.updateProfileImage(newPath);
            deleteManagedAvatar(previousPath);
            Main.headerBar.refreshInfo();
            refreshInfo();
        } catch (Exception ex) {
            // Handle error
        }
    }

    private void removeProfilePhoto() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }

        if (account.getProfileImagePath() == null || account.getProfileImagePath().isBlank()) {
            return;
        }

        try {
            String previousPath = account.getProfileImagePath();
            accountController.removeProfileImage();
            deleteManagedAvatar(previousPath);
            Main.headerBar.refreshInfo();
            refreshInfo();
        } catch (Exception ex) {
            // Handle error
        }
    }

    private void logout() {
        if (Main.shopPage != null) {
            Main.shopPage.persistCurrentCart();
            Main.shopPage.clearTransientCart();
        }
        AccountController.currentAccount = null;
        Main.headerBar.refreshInfo();
        cardLayout.show(pages, "home");
    }

    private void deleteAccountWithVerification() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }

        String expectedEmail = account.getEmail();
        String emailInput = JOptionPane.showInputDialog(
                this,
                "Enter your account email to continue:",
                "Verify Account Email",
                JOptionPane.QUESTION_MESSAGE
        );
        if (emailInput == null) {
            return;
        }
        if (!expectedEmail.equalsIgnoreCase(emailInput.trim())) {
            JOptionPane.showMessageDialog(this, "Email does not match the logged-in account.", "Verification Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            accountController.sendAccountActionCode(expectedEmail);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Email Verification Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String codeInput = JOptionPane.showInputDialog(
                this,
                "Enter the 6-digit verification code sent to your email:",
                "Email Verification Code",
                JOptionPane.QUESTION_MESSAGE
        );
        if (codeInput == null || codeInput.trim().isEmpty()) {
            return;
        }
        if (!accountController.verifyAccountActionCode(expectedEmail, codeInput.trim())) {
            JOptionPane.showMessageDialog(this, "Invalid or expired verification code.", "Verification Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmStepOne = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to permanently delete this account?",
                "Confirm Account Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (confirmStepOne != JOptionPane.YES_OPTION) {
            return;
        }

        String confirmStepTwo = JOptionPane.showInputDialog(
                this,
                "Type DELETE to permanently remove your account:",
                "Final Confirmation",
                JOptionPane.WARNING_MESSAGE
        );
        if (confirmStepTwo == null) {
            return;
        }
        if (!"DELETE".equals(confirmStepTwo.trim())) {
            JOptionPane.showMessageDialog(this, "Final confirmation text did not match.", "Deletion Cancelled", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String previousImagePath = account.getProfileImagePath();
            accountController.deleteCurrentAccount();
            deleteManagedAvatar(previousImagePath);
            try {
                preferencesFor(account).removeNode();
            } catch (Exception ignored) {
            }
            if (Main.shopPage != null) {
                Main.shopPage.clearTransientCart();
            }
            Main.headerBar.refreshInfo();
            JOptionPane.showMessageDialog(this, "Your account has been deleted.", "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(pages, "home");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Deletion Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void navigateBack() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            cardLayout.show(pages, "home");
            return;
        }

        if (account.getRole() == Role.CLERK) {
            cardLayout.show(pages, "clerk page");
        } else if (account.getRole() == Role.ADMIN) {
            cardLayout.show(pages, "admin page");
        } else {
            cardLayout.show(pages, "home");
        }
    }

    private String formatRole(String text) {
        if (text == null || text.isBlank()) return "Unknown";
        text = text.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private String importAvatarImage(File selected, String email) throws IOException {
        if (selected == null || !selected.exists()) {
            throw new IOException("Please choose a valid image file.");
        }
        if (selected.length() > MAX_AVATAR_BYTES) {
            throw new IOException("Profile photos must be 5 MB or smaller.");
        }

        String name = selected.getName().toLowerCase(Locale.ROOT);
        if (!(name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif"))) {
            throw new IOException("Unsupported file type. Please use PNG, JPG, JPEG, or GIF.");
        }

        BufferedImage source = ImageIO.read(selected);
        if (source == null) {
            throw new IOException("The selected file could not be read as an image.");
        }
        if (source.getWidth() < MIN_AVATAR_DIMENSION || source.getHeight() < MIN_AVATAR_DIMENSION) {
            throw new IOException("Profile photos must be at least 128x128 pixels.");
        }

        Path avatarDir = getAvatarStorageDirectory();
        Files.createDirectories(avatarDir);
        Path target = avatarDir.resolve(sanitizeNodeName(email) + ".png");

        BufferedImage rendered = renderSquareAvatar(source, AVATAR_EXPORT_SIZE);
        ImageIO.write(rendered, "png", target.toFile());
        return target.toAbsolutePath().toString();
    }

    private BufferedImage renderSquareAvatar(BufferedImage source, int size) {
        int cropSize = Math.min(source.getWidth(), source.getHeight());
        int cropX = (source.getWidth() - cropSize) / 2;
        int cropY = (source.getHeight() - cropSize) / 2;

        BufferedImage square = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = square.createGraphics();
        try {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.drawImage(source, 0, 0, size, size, cropX, cropY, cropX + cropSize, cropY + cropSize, null);
        } finally {
            g2.dispose();
        }
        return square;
    }

    private Path getAvatarStorageDirectory() {
        return Paths.get(System.getProperty("user.home"), ".6stars-hotel", "profile-pictures");
    }

    private void deleteManagedAvatar(String path) {
        if (path == null || path.isBlank()) {
            return;
        }
        try {
            Path managedDir = getAvatarStorageDirectory().toAbsolutePath().normalize();
            Path imagePath = Paths.get(path).toAbsolutePath().normalize();
            if (imagePath.startsWith(managedDir)) {
                Files.deleteIfExists(imagePath);
            }
        } catch (Exception ignored) {
        }
    }

    private final class AvatarPanel extends JPanel {
        private BufferedImage image;
        private String initials = "??";

        AvatarPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
            setMinimumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
            setMaximumSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        }

        void setAccount(Account account) {
            initials = buildInitials(account);
            image = null;
            if (account != null && account.getProfileImagePath() != null && !account.getProfileImagePath().isBlank()) {
                try {
                    File file = new File(account.getProfileImagePath());
                    if (file.exists()) {
                        image = ImageIO.read(file);
                    }
                } catch (Exception ignored) {
                    image = null;
                }
            }
            repaint();
        }

        void clear() {
            image = null;
            initials = "??";
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Shape clip = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setClip(clip);

                if (image != null) {
                    drawCoverImage(g2, image);
                    g2.setColor(new Color(0, 0, 0, 48));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    g2.setPaint(new Color(194, 159, 92));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setPaint(new Color(229, 220, 201));
                    g2.fillRect(0, 0, getWidth(), getHeight() / 2);
                }

                g2.setClip(null);
                g2.setColor(new Color(255, 255, 255, 180));
                g2.setStroke(new java.awt.BasicStroke(2f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));

                if (image == null) {
                    g2.setColor(new Color(70, 50, 35));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 32));
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(initials)) / 2;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(initials, x, y);
                }
            } finally {
                g2.dispose();
            }
        }

        private void drawCoverImage(Graphics2D g2, BufferedImage img) {
            int panelW = getWidth();
            int panelH = getHeight();
            int imageW = img.getWidth();
            int imageH = img.getHeight();
            double scale = Math.max((double) panelW / imageW, (double) panelH / imageH);
            int drawW = (int) Math.round(imageW * scale);
            int drawH = (int) Math.round(imageH * scale);
            int drawX = (panelW - drawW) / 2;
            int drawY = (panelH - drawH) / 2;
            g2.drawImage(img, drawX, drawY, drawW, drawH, null);
        }

        private String buildInitials(Account account) {
            if (account == null) {
                return "??";
            }
            String first = account.getFirstName() == null || account.getFirstName().isBlank() ? "" : account.getFirstName().trim();
            String last = account.getLastName() == null || account.getLastName().isBlank() ? "" : account.getLastName().trim();
            StringBuilder sb = new StringBuilder();
            if (!first.isEmpty()) sb.append(Character.toUpperCase(first.charAt(0)));
            if (!last.isEmpty()) sb.append(Character.toUpperCase(last.charAt(0)));
            return sb.length() == 0 ? "??" : sb.toString();
        }
    }
}









