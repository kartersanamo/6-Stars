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
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Comparator;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicButtonUI;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.NotificationType;
import com.sixstars.model.Role;
import com.sixstars.model.Reservation;
import com.sixstars.model.ShopOrder;
import com.sixstars.model.ShopOrderItem;
import com.sixstars.service.NotificationService;
import com.sixstars.service.BillingService;
import com.sixstars.service.GuestLedgerService;
import com.sixstars.service.SavedPaymentMethodService;
import com.sixstars.model.SavedPaymentMethod;
import com.sixstars.service.stripe.PaymentBillingValidator;
import com.sixstars.service.stripe.StripeConfig;
import com.sixstars.service.stripe.StripeConnectOAuthTokenClient;
import com.sixstars.service.stripe.StripeGuestPreferences;
import com.sixstars.service.stripe.StripeHostedLocalServer;
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
    private static final Color BILLING_SECTION_BG = new Color(252, 250, 245);
    private static final Color BILLING_ACCENT = new Color(176, 132, 38);
    private static final Color BILLING_POSITIVE = new Color(44, 122, 72);

    private final JPanel pages;
    private final CardLayout cardLayout;
    private final AccountController accountController;
    private final Preferences preferencesRoot = Preferences.userNodeForPackage(AccountCenterPage.class);
    private final BillingService billingService = new BillingService();
    private final SavedPaymentMethodService savedPaymentMethodService = new SavedPaymentMethodService();
    private final GuestLedgerService guestLedgerService = new GuestLedgerService();

    // Sidebar navigation buttons
    private JButton accountInfoButton;
    private JButton securityButton;
    private JButton notificationsButton;
    private JButton myReservationsButton;
    private JButton billingButton;
    private JButton paymentButton;

    private JPanel stripeConnectBannerOuter;
    private JLabel stripeBannerStatusLarge;
    private JLabel stripeBannerDetailSmall;
    private JButton stripeConnectSoleActionButton;
    private JLabel stripeIdsSummaryLabel;

    private JTextField payFullNameField;
    private JTextField payAddressLine1Field;
    private JTextField payAddressLine2Field;
    private JTextField payCityField;
    private JTextField payStateField;
    private JTextField payZipField;
    private JTextField payPhoneField;
    private JPasswordField payCardNumberField;
    private JTextField payCardExpiryField;
    private JPasswordField payCardCvvField;
    private JTextField payCardNicknameField;
    private JLabel paymentWorkspaceStatusLabel;
    private JPanel paymentMethodsListPanel;
    private JPanel addPaymentFormOuter;
    private JButton toggleAddPaymentFormButton;
    private JLabel paymentBalanceSnapshotLabel;
    private JButton dangerZoneButton;

    // Content panels for each section
    private final JPanel contentArea;
    private final CardLayout contentLayout;

    // Avatar and quick info
    private final AvatarPanel avatarPanel = new AvatarPanel();
    private final AvatarPanel profileAvatarPanel = new AvatarPanel();
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
    private final Map<NotificationType, JCheckBox> emailNotificationChecks = new EnumMap<>(NotificationType.class);
    private final Map<NotificationType, JCheckBox> inAppNotificationChecks = new EnumMap<>(NotificationType.class);
    private final NotificationService notificationService = NotificationService.getInstance();

    // Billing Section Components
    private JPanel myReservationsContainer;
    private JLabel myReservationsTotalLabel;
    private JLabel myReservationsActiveLabel;
    private JLabel myReservationsNextCheckInLabel;
    private JPanel reservationsContainer;
    private JPanel shopContainer;
    private JLabel reservationTotalLabel;
    private JLabel shopTotalLabel;
    private JLabel grandTotalLabel;

    private final JTextField deleteEmailField = new JTextField();
    private final JTextField deleteCodeField = new JTextField();
    private final JButton sendDeleteCodeButton = new JButton("Send Verification Code");
    private final JButton deleteAccountButton = new JButton("Delete My Account");
    private final JLabel deleteStatusLabel = new JLabel(" ");

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
        contentArea.add(wrapInScrollPane(createMyReservationsPanel()), "my-reservations");
        contentArea.add(wrapInScrollPane(createBillingPanel()), "billing");
        contentArea.add(wrapInScrollPane(createPaymentPanel()), "payment");
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
                new EmptyBorder(16, 4, 16, 0)
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

        JPanel headerWrapper = new JPanel(new BorderLayout());
        headerWrapper.setOpaque(false);
        headerWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
        headerWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        headerWrapper.add(headerPanel, BorderLayout.CENTER);
        sidebar.add(headerWrapper);
        sidebar.add(Box.createRigidArea(new Dimension(0, 16)));

        accountInfoButton = createNavButton("Account Information", "account-info");
        securityButton = createNavButton("Security", "security");
        notificationsButton = createNavButton("Notifications", "notifications");
        myReservationsButton = createNavButton("My Reservations", "my-reservations");
        billingButton = createNavButton("Billing", "billing");
        paymentButton = createNavButton("Payment", "payment");
        dangerZoneButton = createNavButton("Danger Zone", "danger-zone");

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBackground(SIDEBAR_PANEL);
        navPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(224, 214, 195), 1, true),
                new EmptyBorder(12, 0, 12, 0)
        ));
        navPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        navPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        navPanel.add(createSidebarSectionLabel("Account"));
        navPanel.add(createNavButtonRow(accountInfoButton));
        navPanel.add(createNavButtonRow(securityButton));
        navPanel.add(createNavButtonRow(notificationsButton));
        navPanel.add(createNavButtonRow(myReservationsButton));
        navPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        navPanel.add(createSidebarSectionLabel("Hotel Services"));
        navPanel.add(createNavButtonRow(billingButton));
        navPanel.add(createNavButtonRow(paymentButton));
        JPanel navWrapper = new JPanel(new BorderLayout());
        navWrapper.setOpaque(false);
        navWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
        navWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        navWrapper.add(navPanel, BorderLayout.CENTER);
        sidebar.add(navWrapper);
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel dangerPanel = new JPanel();
        dangerPanel.setLayout(new BoxLayout(dangerPanel, BoxLayout.Y_AXIS));
        dangerPanel.setBackground(new Color(252, 246, 246));
        dangerPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(234, 206, 206), 1, true),
                new EmptyBorder(10, 0, 10, 0)
        ));
        dangerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dangerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        dangerPanel.add(createSidebarSectionLabel("Danger Zone"));
        dangerPanel.add(createNavButtonRow(dangerZoneButton));
        JPanel dangerWrapper = new JPanel(new BorderLayout());
        dangerWrapper.setOpaque(false);
        dangerWrapper.setBorder(new EmptyBorder(0, 0, 0, 0));
        dangerWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        dangerWrapper.add(dangerPanel, BorderLayout.CENTER);
        sidebar.add(dangerWrapper);

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

        button.addActionListener(_ -> {
            selectNavButton(button);
            // Refresh billing content when switching to billing tab
            if ("billing".equals(contentKey)) {
                refreshBillingContent();
            } else if ("my-reservations".equals(contentKey)) {
                refreshMyReservationsContent();
            } else if ("payment".equals(contentKey)) {
                refreshPaymentWorkspace();
            }
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
        myReservationsButton.setBackground(SIDEBAR_PANEL);
        myReservationsButton.setForeground(UITheme.TEXT_DARK);
        billingButton.setBackground(SIDEBAR_PANEL);
        billingButton.setForeground(UITheme.TEXT_DARK);
        paymentButton.setBackground(SIDEBAR_PANEL);
        paymentButton.setForeground(UITheme.TEXT_DARK);
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
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setPreferredSize(new Dimension(0, 42));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        row.add(button, BorderLayout.CENTER);
        return row;
    }


    private JPanel createAccountInfoPanel() {
        JPanel mainPanel = createContentPanel();

        JLabel title = new JLabel("Account Information");
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Update your profile details and upload a profile picture.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 28)));

        JPanel avatarSection = createCardPanel();
        avatarSection.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));
        avatarSection.add(createSectionTitle("Profile Picture", "Keep your account photo and profile presence up to date"));
        avatarSection.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel avatarContent = new JPanel(new GridLayout(1, 2, 24, 0));
        avatarContent.setOpaque(false);

        JPanel avatarStack = new JPanel();
        avatarStack.setOpaque(false);
        avatarStack.setLayout(new BoxLayout(avatarStack, BoxLayout.Y_AXIS));
        profileAvatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        profileAvatarPanel.setPreferredSize(new Dimension(140, 140));
        profileAvatarPanel.setMaximumSize(new Dimension(140, 140));
        avatarStack.add(profileAvatarPanel);
        avatarStack.add(Box.createRigidArea(new Dimension(0, 14)));
        avatarStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        avatarStatusLabel.setForeground(UITheme.TEXT_MEDIUM);
        avatarStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarStack.add(avatarStatusLabel);

        JPanel avatarFieldsPanel = new JPanel();
        avatarFieldsPanel.setOpaque(false);
        avatarFieldsPanel.setLayout(new BoxLayout(avatarFieldsPanel, BoxLayout.Y_AXIS));
        JLabel profileActionsTitle = new JLabel("Profile Actions");
        profileActionsTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        profileActionsTitle.setForeground(UITheme.TEXT_DARK);
        profileActionsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        avatarFieldsPanel.add(profileActionsTitle);
        avatarFieldsPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        JLabel profileActionsText = new JLabel("Upload a new image or remove your current photo.");
        profileActionsText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        profileActionsText.setForeground(UITheme.TEXT_MEDIUM);
        profileActionsText.setAlignmentX(Component.LEFT_ALIGNMENT);
        avatarFieldsPanel.add(profileActionsText);
        avatarFieldsPanel.add(Box.createRigidArea(new Dimension(0, 18)));
        avatarFieldsPanel.add(uploadPhotoButton);
        avatarFieldsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        avatarFieldsPanel.add(removePhotoButton);
        styleButton(uploadPhotoButton, true);
        styleButton(removePhotoButton, false);
        uploadPhotoButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        removePhotoButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        avatarContent.add(avatarStack);
        avatarContent.add(avatarFieldsPanel);
        avatarSection.add(avatarContent);
        mainPanel.add(avatarSection);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel detailsSection = createCardPanel();
        detailsSection.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(24, 24, 24, 24)
        ));
        detailsSection.add(createSectionTitle("Personal Information", "Your primary account details"));
        detailsSection.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel nameRow = new JPanel(new GridLayout(1, 2, 12, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        nameRow.add(createLabeledFieldCard("First Name", firstNameField, true));
        nameRow.add(createLabeledFieldCard("Last Name", lastNameField, true));
        detailsSection.add(nameRow);

        detailsSection.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel emailRow = new JPanel(new GridLayout(1, 2, 12, 0));
        emailRow.setOpaque(false);
        emailRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        emailRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        emailRow.add(createLabeledFieldCard("Email Address", emailValueLabel, false));
        emailRow.add(createLabeledFieldCard("Role", roleValueLabel, false));
        detailsSection.add(emailRow);

        detailsSection.add(Box.createRigidArea(new Dimension(0, 18)));
        styleButton(saveProfileButton, true);
        saveProfileButton.setPreferredSize(new Dimension(240, 42));
        saveProfileButton.setMaximumSize(new Dimension(240, 42));
        JPanel saveRow = new JPanel(new BorderLayout());
        saveRow.setOpaque(false);
        saveRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        saveRow.add(saveProfileButton, BorderLayout.EAST);
        detailsSection.add(saveRow);

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

        JLabel subtitle = new JLabel("Choose exactly how each notification is delivered: Email, In-App, or both.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel notificationCard = createCardPanel();
        notificationCard.add(createSectionTitle("Delivery Preferences", "Toggle per category for Email and In-App alerts"));
        notificationCard.add(Box.createRigidArea(new Dimension(0, 16)));

        // Wrap notification content in its own non-expanding container
        JPanel notificationContent = new JPanel();
        notificationContent.setLayout(new BoxLayout(notificationContent, BoxLayout.Y_AXIS));
        notificationContent.setOpaque(false);
        notificationContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        notificationContent.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JPanel tableHeader = new JPanel(new GridLayout(1, 3, 12, 0));
        tableHeader.setOpaque(false);
        tableHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
        tableHeader.add(createHeaderCell("Notification Type"));
        tableHeader.add(createHeaderCell("Email"));
        tableHeader.add(createHeaderCell("In-App"));
        notificationContent.add(tableHeader);
        notificationContent.add(Box.createRigidArea(new Dimension(0, 8)));

        emailNotificationChecks.clear();
        inAppNotificationChecks.clear();
        for (NotificationType type : NotificationType.values()) {
            JPanel row = new JPanel(new GridLayout(1, 3, 12, 0));
            row.setOpaque(false);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

            JLabel typeLabel = new JLabel(type.getDisplayName());
            typeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
            typeLabel.setForeground(UITheme.TEXT_DARK);

            JCheckBox emailCheck = new JCheckBox();
            emailCheck.setOpaque(false);
            emailCheck.setHorizontalAlignment(SwingConstants.CENTER);

            JCheckBox inAppCheck = new JCheckBox();
            inAppCheck.setOpaque(false);
            inAppCheck.setHorizontalAlignment(SwingConstants.CENTER);

            emailNotificationChecks.put(type, emailCheck);
            inAppNotificationChecks.put(type, inAppCheck);

            row.add(typeLabel);
            row.add(emailCheck);
            row.add(inAppCheck);
            notificationContent.add(row);
            notificationContent.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        notificationCard.add(notificationContent);
        notificationCard.add(Box.createVerticalGlue());
        mainPanel.add(notificationCard);

        return mainPanel;
    }

    private JPanel createMyReservationsPanel() {
        JPanel mainPanel = createContentPanel();

        JLabel title = new JLabel("My Reservations");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("View and manage all your room reservations.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel metricsRow = new JPanel(new GridLayout(1, 3, 12, 0));
        metricsRow.setOpaque(false);
        metricsRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        myReservationsTotalLabel = createMetricValueLabel();
        myReservationsActiveLabel = createMetricValueLabel();
        myReservationsNextCheckInLabel = createMetricValueLabel();

        metricsRow.add(createMetricCard("Total Reservations", myReservationsTotalLabel));
        metricsRow.add(createMetricCard("Active", myReservationsActiveLabel));
        metricsRow.add(createMetricCard("Next Check-In", myReservationsNextCheckInLabel));
        mainPanel.add(metricsRow);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel listCard = createCardPanel();
        listCard.setBackground(BILLING_SECTION_BG);
        listCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(231, 223, 204), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        listCard.add(createSectionTitle("Reservation Timeline", "Review your upcoming, active, and historical reservations"));
        listCard.add(Box.createRigidArea(new Dimension(0, 12)));

        myReservationsContainer = new JPanel();
        myReservationsContainer.setLayout(new BoxLayout(myReservationsContainer, BoxLayout.Y_AXIS));
        myReservationsContainer.setOpaque(false);
        myReservationsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        listCard.add(myReservationsContainer);

        mainPanel.add(listCard);

        return mainPanel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BILLING_SECTION_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(231, 223, 204), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

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

    private JLabel createMetricValueLabel() {
        JLabel label = new JLabel("0");
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(UITheme.TEXT_DARK);
        return label;
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
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel overviewCard = createCardPanel();
        overviewCard.setBackground(BILLING_SECTION_BG);
        overviewCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 221, 199), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        JLabel overviewTitle = new JLabel("Your Account Billing Summary");
        overviewTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        overviewTitle.setForeground(UITheme.TEXT_DARK);
        overviewTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel overviewSub = new JLabel("Everything in one place: reservation charges, shop purchases, and total due.");
        overviewSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        overviewSub.setForeground(UITheme.TEXT_MEDIUM);
        overviewSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        overviewCard.add(overviewTitle);
        overviewCard.add(Box.createRigidArea(new Dimension(0, 6)));
        overviewCard.add(overviewSub);
        mainPanel.add(overviewCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel reservationSection = createBillingSectionCard("Reservation Charges", "Your room stays and applicable fees");
        reservationsContainer = new JPanel();
        reservationsContainer.setLayout(new BoxLayout(reservationsContainer, BoxLayout.Y_AXIS));
        reservationsContainer.setOpaque(false);
        reservationsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        reservationSection.add(reservationsContainer);
        mainPanel.add(reservationSection);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel shopSection = createBillingSectionCard("Shop Purchases", "Everything ordered from the hotel shop");
        shopContainer = new JPanel();
        shopContainer.setLayout(new BoxLayout(shopContainer, BoxLayout.Y_AXIS));
        shopContainer.setOpaque(false);
        shopContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        shopSection.add(shopContainer);
        mainPanel.add(shopSection);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel totalsCard = createCardPanel();
        totalsCard.setBackground(new Color(253, 250, 241));
        totalsCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(225, 207, 167), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        JLabel totalsTitle = new JLabel("Balance Overview");
        totalsTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        totalsTitle.setForeground(UITheme.TEXT_DARK);
        totalsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        reservationTotalLabel = createTotalLabel("");
        shopTotalLabel = createTotalLabel("");
        grandTotalLabel = createGrandTotalLabel("");

        totalsCard.add(totalsTitle);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 12)));
        totalsCard.add(reservationTotalLabel);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 8)));
        totalsCard.add(shopTotalLabel);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 12)));
        totalsCard.add(new JSeparator());
        totalsCard.add(Box.createRigidArea(new Dimension(0, 12)));
        totalsCard.add(grandTotalLabel);
        mainPanel.add(totalsCard);

        return mainPanel;
    }

    private JPanel createBillingSectionCard(String title, String subtitle) {
        JPanel card = createCardPanel();
        card.setBackground(BILLING_SECTION_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(231, 223, 204), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        card.add(createSectionTitle(title, subtitle));
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        return card;
    }

    private JPanel createPaymentPanel() {
        JPanel mainPanel = createContentPanel();

        JLabel title = new JLabel("Payment & Stripe");
        title.setFont(new Font("Serif", Font.BOLD, 26));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Securely manage billing details, Stripe account linking, and stored payment methods.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        mainPanel.add(title);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        mainPanel.add(subtitle);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        stripeConnectBannerOuter = new JPanel();
        stripeConnectBannerOuter.setLayout(new BoxLayout(stripeConnectBannerOuter, BoxLayout.Y_AXIS));
        stripeConnectBannerOuter.setOpaque(true);
        stripeConnectBannerOuter.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(214, 160, 160), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        stripeConnectBannerOuter.setBackground(new Color(255, 235, 235));
        stripeConnectBannerOuter.setAlignmentX(Component.LEFT_ALIGNMENT);
        stripeConnectBannerOuter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        stripeBannerStatusLarge = new JLabel("Not connected");
        stripeBannerStatusLarge.setFont(new Font("SansSerif", Font.BOLD, 20));
        stripeBannerStatusLarge.setForeground(new Color(160, 40, 40));
        stripeBannerStatusLarge.setAlignmentX(Component.LEFT_ALIGNMENT);

        stripeBannerDetailSmall = new JLabel(stripeNotConnectedInstructionPlain());
        stripeBannerDetailSmall.setFont(new Font("SansSerif", Font.PLAIN, 14));
        stripeBannerDetailSmall.setForeground(UITheme.TEXT_MEDIUM);
        stripeBannerDetailSmall.setAlignmentX(Component.LEFT_ALIGNMENT);

        stripeIdsSummaryLabel = new JLabel(" ");
        stripeIdsSummaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        stripeIdsSummaryLabel.setForeground(UITheme.TEXT_MEDIUM);
        stripeIdsSummaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        stripeConnectSoleActionButton = new JButton("Connect with Stripe");
        stripeConnectSoleActionButton.setUI(new BasicButtonUI());
        stripeConnectSoleActionButton.setFocusPainted(false);
        stripeConnectSoleActionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        stripeConnectSoleActionButton.setFont(new Font("SansSerif", Font.BOLD, 14));

        JPanel soleActionRow = new JPanel(new BorderLayout(0, 0));
        soleActionRow.setOpaque(false);
        soleActionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        soleActionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        soleActionRow.add(stripeConnectSoleActionButton, BorderLayout.CENTER);

        stripeConnectBannerOuter.add(stripeBannerStatusLarge);
        stripeConnectBannerOuter.add(Box.createRigidArea(new Dimension(0, 8)));
        stripeConnectBannerOuter.add(stripeBannerDetailSmall);
        stripeConnectBannerOuter.add(Box.createRigidArea(new Dimension(0, 10)));
        stripeConnectBannerOuter.add(stripeIdsSummaryLabel);
        stripeConnectBannerOuter.add(Box.createRigidArea(new Dimension(0, 16)));
        stripeConnectBannerOuter.add(soleActionRow);

        mainPanel.add(stripeConnectBannerOuter);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 22)));

        JPanel balanceSnapshot = createCardPanel();
        balanceSnapshot.setBackground(BILLING_SECTION_BG);
        balanceSnapshot.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 221, 199), 1, true),
                new EmptyBorder(16, 18, 16, 18)
        ));
        balanceSnapshot.add(createSectionTitle("Balance snapshot", "Charges minus payments recorded in this app"));
        balanceSnapshot.add(Box.createRigidArea(new Dimension(0, 10)));
        paymentBalanceSnapshotLabel = new JLabel(" ");
        paymentBalanceSnapshotLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        paymentBalanceSnapshotLabel.setForeground(UITheme.TEXT_MEDIUM);
        paymentBalanceSnapshotLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        balanceSnapshot.add(paymentBalanceSnapshotLabel);
        mainPanel.add(balanceSnapshot);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel methodsCard = createCardPanel();
        methodsCard.setBackground(BILLING_SECTION_BG);
        methodsCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(231, 223, 204), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        methodsCard.add(createSectionTitle("Saved payment methods",
                "Cards you save here are stored securely in the hotel database (last four digits only)."));
        methodsCard.add(Box.createRigidArea(new Dimension(0, 12)));

        paymentMethodsListPanel = new JPanel();
        paymentMethodsListPanel.setLayout(new BoxLayout(paymentMethodsListPanel, BoxLayout.Y_AXIS));
        paymentMethodsListPanel.setOpaque(false);
        paymentMethodsListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        methodsCard.add(paymentMethodsListPanel);
        methodsCard.add(Box.createRigidArea(new Dimension(0, 14)));

        toggleAddPaymentFormButton = new JButton("Add a payment method");
        styleButton(toggleAddPaymentFormButton, true);
        toggleAddPaymentFormButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggleAddPaymentFormButton.addActionListener(_ -> toggleAddPaymentFormVisibility());
        methodsCard.add(toggleAddPaymentFormButton);
        methodsCard.add(Box.createRigidArea(new Dimension(0, 12)));

        addPaymentFormOuter = new JPanel();
        addPaymentFormOuter.setLayout(new BoxLayout(addPaymentFormOuter, BoxLayout.Y_AXIS));
        addPaymentFormOuter.setOpaque(true);
        addPaymentFormOuter.setBackground(new Color(255, 255, 252));
        addPaymentFormOuter.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(210, 200, 180), 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        addPaymentFormOuter.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPaymentFormOuter.setMaximumSize(new Dimension(Integer.MAX_VALUE, 720));
        addPaymentFormOuter.setVisible(false);

        payFullNameField = new JTextField();
        payAddressLine1Field = new JTextField();
        payAddressLine2Field = new JTextField();
        payCityField = new JTextField();
        payStateField = new JTextField();
        payZipField = new JTextField();
        payPhoneField = new JTextField();
        payCardNicknameField = new JTextField();
        payCardNumberField = new JPasswordField();
        payCardExpiryField = new JTextField();
        payCardCvvField = new JPasswordField();

        JLabel formIntro = new JLabel("<html>All fields are validated before the card is saved. We never store your full card number.</html>");
        formIntro.setFont(new Font("SansSerif", Font.PLAIN, 13));
        formIntro.setForeground(UITheme.TEXT_MEDIUM);
        formIntro.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPaymentFormOuter.add(formIntro);
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 12)));
        addPaymentFormOuter.add(makeLabeledStripeFieldRow("Card nickname (optional)", payCardNicknameField));
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 10)));
        addPaymentFormOuter.add(makeLabeledStripeFieldRow("Name on card", payFullNameField));
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 10)));
        addPaymentFormOuter.add(makeLabeledStripeFieldRow("Billing address line 1", payAddressLine1Field));
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 10)));
        addPaymentFormOuter.add(makeLabeledStripeFieldRow("Address line 2 (optional)", payAddressLine2Field));
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 10)));
        JPanel cityStateZip = new JPanel(new GridLayout(1, 3, 14, 0));
        cityStateZip.setOpaque(false);
        cityStateZip.add(labeledStripeMini("City", payCityField));
        cityStateZip.add(labeledStripeMini("State (2-letter)", payStateField));
        cityStateZip.add(labeledStripeMini("ZIP code", payZipField));
        cityStateZip.setAlignmentX(Component.LEFT_ALIGNMENT);
        cityStateZip.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        addPaymentFormOuter.add(cityStateZip);
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 10)));
        addPaymentFormOuter.add(makeLabeledStripeFieldRow("Phone (optional)", payPhoneField));
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 14)));
        addPaymentFormOuter.add(makeLabeledStripeFieldPassword("Card number", payCardNumberField));
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 10)));
        JPanel expCvvRow = new JPanel(new GridLayout(1, 2, 14, 0));
        expCvvRow.setOpaque(false);
        expCvvRow.add(labeledStripeMini("Expiry MM/YY", payCardExpiryField));
        expCvvRow.add(makeLabeledStripeFieldPasswordMini("CVV", payCardCvvField));
        expCvvRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        expCvvRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        addPaymentFormOuter.add(expCvvRow);
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 16)));
        JButton savePaymentMethodButton = new JButton("Save payment method");
        styleButton(savePaymentMethodButton, true);
        savePaymentMethodButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        savePaymentMethodButton.addActionListener(_ -> saveNewPaymentMethodFromForm());
        addPaymentFormOuter.add(savePaymentMethodButton);
        addPaymentFormOuter.add(Box.createRigidArea(new Dimension(0, 10)));
        paymentWorkspaceStatusLabel = new JLabel(" ");
        paymentWorkspaceStatusLabel.setFont(new Font("SansSerif", Font.ITALIC, 12));
        paymentWorkspaceStatusLabel.setForeground(UITheme.TEXT_MEDIUM);
        paymentWorkspaceStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        addPaymentFormOuter.add(paymentWorkspaceStatusLabel);

        methodsCard.add(addPaymentFormOuter);
        mainPanel.add(methodsCard);

        return mainPanel;
    }

    private JPanel makeLabeledStripeFieldRow(String label, JTextField field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_DARK);
        styleTextField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.add(l);
        row.add(Box.createRigidArea(new Dimension(0, 4)));
        row.add(field);
        return row;
    }

    private JPanel makeLabeledStripeFieldPassword(String label, JPasswordField field) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_DARK);
        stylePasswordField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.add(l);
        row.add(Box.createRigidArea(new Dimension(0, 4)));
        row.add(field);
        return row;
    }

    private JPanel labeledStripeMini(String hint, JTextField field) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        JLabel l = new JLabel(hint);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(UITheme.TEXT_MEDIUM);
        styleTextField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.add(l);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(field);
        return box;
    }

    private JPanel makeLabeledStripeFieldPasswordMini(String hint, JPasswordField field) {
        JPanel box = new JPanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.setOpaque(false);
        JLabel l = new JLabel(hint);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(UITheme.TEXT_MEDIUM);
        stylePasswordField(field);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        box.add(l);
        box.add(Box.createRigidArea(new Dimension(0, 4)));
        box.add(field);
        return box;
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

        JPanel verificationInputs = new JPanel(new GridLayout(1, 2, 12, 0));
        verificationInputs.setOpaque(false);
        verificationInputs.setAlignmentX(Component.LEFT_ALIGNMENT);
        verificationInputs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        verificationInputs.add(createLabeledFieldCard("Account Email", deleteEmailField, true));
        verificationInputs.add(createLabeledFieldCard("Verification Code", deleteCodeField, true));
        deleteCard.add(verificationInputs);
        deleteCard.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel verificationActions = new JPanel(new GridLayout(1, 2, 10, 0));
        verificationActions.setOpaque(false);
        verificationActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        verificationActions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        styleButton(sendDeleteCodeButton, false);
        sendDeleteCodeButton.addActionListener(_ -> sendDeleteVerificationCodeInline());
        verificationActions.add(sendDeleteCodeButton);
        verificationActions.add(Box.createHorizontalStrut(0));
        deleteCard.add(verificationActions);

        deleteCard.add(Box.createRigidArea(new Dimension(0, 8)));
        deleteStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        deleteStatusLabel.setForeground(UITheme.TEXT_MEDIUM);
        deleteStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteCard.add(deleteStatusLabel);
        deleteCard.add(Box.createRigidArea(new Dimension(0, 10)));

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

    private JLabel createHeaderCell(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(UITheme.TEXT_MEDIUM);
        if (!"Notification Type".equals(text)) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return label;
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
        ActionListener preferenceListener = e -> saveNotificationPreferences();
        for (JCheckBox emailCheck : emailNotificationChecks.values()) {
            emailCheck.addActionListener(preferenceListener);
        }
        for (JCheckBox inAppCheck : inAppNotificationChecks.values()) {
            inAppCheck.addActionListener(preferenceListener);
        }
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
        profileAvatarPanel.setAccount(account);
        avatarStatusLabel.setText(account.getProfileImagePath() == null || account.getProfileImagePath().isBlank()
                ? "No profile photo uploaded yet."
                : "Profile photo is uploaded and ready.");
        deleteEmailField.setText("");
        deleteCodeField.setText("");
        deleteStatusLabel.setText(" ");

        clearPasswordFields();
        togglePasswordVisibility(showPasswordsCheck.isSelected());
        loadNotificationPreferences(account);
        refreshMyReservationsContent();
        refreshBillingContent();
        refreshPaymentWorkspace();

        revalidate();
        repaint();
    }

    /** Opens the Payment tab (e.g. from Billing when Stripe needs to be connected). */
    public void navigateToPaymentTab() {
        navigateToPaymentTab(false);
    }

    /**
     * @param expandAddPaymentForm when true, opens the collapsible “add card” form (e.g. from Billing “Add a card”).
     */
    public void navigateToPaymentTab(boolean expandAddPaymentForm) {
        SwingUtilities.invokeLater(() -> {
            selectNavButton(paymentButton);
            contentLayout.show(contentArea, "payment");
            refreshPaymentWorkspace();
            if (expandAddPaymentForm && addPaymentFormOuter != null && toggleAddPaymentFormButton != null) {
                addPaymentFormOuter.setVisible(true);
                toggleAddPaymentFormButton.setText("Hide payment form");
                addPaymentFormOuter.revalidate();
            }
            if (Main.headerBar != null) {
                Main.headerBar.refreshInfo();
            }
        });
    }

    private void resetForNoAccount() {
        nameLabel.setText("User");
        emailLabel.setText("email@example.com");
        roleLabel.setText("Guest");
        avatarPanel.clear();
        profileAvatarPanel.clear();
        deleteEmailField.setText("");
        deleteCodeField.setText("");
        deleteStatusLabel.setText(" ");
        clearPasswordFields();
        showPasswordsCheck.setSelected(false);
        togglePasswordVisibility(false);

        if (paymentWorkspaceStatusLabel != null) {
            paymentWorkspaceStatusLabel.setText(" ");
        }
        if (payFullNameField != null) {
            payFullNameField.setText("");
            payAddressLine1Field.setText("");
            payAddressLine2Field.setText("");
            payCityField.setText("");
            payStateField.setText("");
            payZipField.setText("");
            payPhoneField.setText("");
            payCardNumberField.setText("");
            payCardExpiryField.setText("");
            payCardCvvField.setText("");
        }
        if (payCardNicknameField != null) {
            payCardNicknameField.setText("");
        }
        if (paymentBalanceSnapshotLabel != null) {
            paymentBalanceSnapshotLabel.setText(" ");
        }
        if (addPaymentFormOuter != null) {
            addPaymentFormOuter.setVisible(false);
        }
        if (toggleAddPaymentFormButton != null) {
            toggleAddPaymentFormButton.setText("Add a payment method");
        }
        rebuildPaymentMethodsList();
    }

    private void loadNotificationPreferences(Account account) {
        loadingPreferences = true;
        try {
            for (NotificationType type : NotificationType.values()) {
                JCheckBox emailCheck = emailNotificationChecks.get(type);
                JCheckBox inAppCheck = inAppNotificationChecks.get(type);
                if (emailCheck != null) {
                    emailCheck.setSelected(notificationService.isEmailEnabled(account.getEmail(), type));
                }
                if (inAppCheck != null) {
                    inAppCheck.setSelected(notificationService.isInAppEnabled(account.getEmail(), type));
                }
            }
        } finally {
            loadingPreferences = false;
        }
    }

    private void saveNotificationPreferences() {
        if (loadingPreferences) {
            return;
        }

        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }

        for (NotificationType type : NotificationType.values()) {
            JCheckBox emailCheck = emailNotificationChecks.get(type);
            JCheckBox inAppCheck = inAppNotificationChecks.get(type);
            if (emailCheck != null) {
                notificationService.setEmailEnabled(account.getEmail(), type, emailCheck.isSelected());
            }
            if (inAppCheck != null) {
                notificationService.setInAppEnabled(account.getEmail(), type, inAppCheck.isSelected());
            }
        }
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
            notificationService.publishForCurrentAccount(NotificationType.ACCOUNT_ACTIVITY, "Your profile information was updated.");
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
            notificationService.publishForCurrentAccount(NotificationType.ACCOUNT_ACTIVITY, "Your password was changed successfully.");
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
            notificationService.publishForCurrentAccount(NotificationType.ACCOUNT_ACTIVITY, "Profile photo updated.");
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
            notificationService.publishForCurrentAccount(NotificationType.ACCOUNT_ACTIVITY, "Profile photo removed.");
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
        String emailInput = deleteEmailField.getText().trim();
        if (!expectedEmail.equalsIgnoreCase(emailInput.trim())) {
            deleteStatusLabel.setText("Email does not match the logged-in account.");
            JOptionPane.showMessageDialog(this, "Email does not match the logged-in account.", "Verification Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String codeInput = deleteCodeField.getText().trim();
        if (codeInput.isEmpty()) {
            deleteStatusLabel.setText("Enter the verification code sent to your email.");
            JOptionPane.showMessageDialog(this, "Please enter the verification code.", "Missing Code", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!accountController.verifyAccountActionCode(expectedEmail, codeInput)) {
            deleteStatusLabel.setText("Invalid or expired verification code.");
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
            deleteStatusLabel.setText("Account deletion cancelled.");
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
            deleteStatusLabel.setText("Final confirmation text did not match.");
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
            deleteStatusLabel.setText(" ");
            JOptionPane.showMessageDialog(this, "Your account has been deleted.", "Account Deleted", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(pages, "home");
        } catch (RuntimeException ex) {
            deleteStatusLabel.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Deletion Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendDeleteVerificationCodeInline() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }

        String expectedEmail = account.getEmail();
        String emailInput = deleteEmailField.getText().trim();
        if (!expectedEmail.equalsIgnoreCase(emailInput)) {
            deleteStatusLabel.setText("Enter your exact account email before requesting a code.");
            JOptionPane.showMessageDialog(this, "Email does not match the logged-in account.", "Verification Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            accountController.sendAccountActionCode(expectedEmail);
            deleteStatusLabel.setText("Verification code sent. Check your inbox and enter it above.");
        } catch (RuntimeException ex) {
            deleteStatusLabel.setText(ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Email Verification Failed", JOptionPane.ERROR_MESSAGE);
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

    // Billing panel refresh and helper methods
    private void refreshMyReservationsContent() {
        if (myReservationsContainer == null) {
            return;
        }

        myReservationsContainer.removeAll();

        Account current = AccountController.currentAccount;
        if (current == null) {
            myReservationsContainer.add(createEmptyCard("No account is currently logged in."));
            myReservationsTotalLabel.setText("0");
            myReservationsActiveLabel.setText("0");
            myReservationsNextCheckInLabel.setText("--");
            myReservationsContainer.revalidate();
            myReservationsContainer.repaint();
            return;
        }

        List<Reservation> reservations = billingService.getReservationCharges(current.getEmail()).stream()
                .sorted(Comparator.comparing(Reservation::getStartDate))
                .toList();

        long activeCount = reservations.stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()) && !"CHECKED_OUT".equalsIgnoreCase(r.getStatus()))
                .count();

        String nextCheckIn = reservations.stream()
                .filter(r -> !"CANCELLED".equalsIgnoreCase(r.getStatus()) && !r.getStartDate().isBefore(java.time.LocalDate.now()))
                .min(Comparator.comparing(Reservation::getStartDate))
                .map(r -> r.getStartDate().toString())
                .orElse("--");

        myReservationsTotalLabel.setText(String.valueOf(reservations.size()));
        myReservationsActiveLabel.setText(String.valueOf(activeCount));
        myReservationsNextCheckInLabel.setText(nextCheckIn);

        if (reservations.isEmpty()) {
            myReservationsContainer.add(createEmptyCard("No reservations found yet."));
        } else {
            for (Reservation reservation : reservations) {
                myReservationsContainer.add(createProfileReservationCard(reservation));
                myReservationsContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        myReservationsContainer.revalidate();
        myReservationsContainer.repaint();
    }

    private JPanel createProfileReservationCard(Reservation reservation) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(228, 223, 214), 1, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        String roomText = reservation.getRooms().stream()
                .map(r -> "Room " + r.getRoomNumber())
                .reduce((a, b) -> a + ", " + b)
                .orElse("No rooms");

        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        JLabel roomLabel = new JLabel(roomText);
        roomLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        roomLabel.setForeground(UITheme.TEXT_DARK);

        JLabel statusBadge = new JLabel(reservation.getStatus().replace('_', ' '));
        statusBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
        if ("CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
            statusBadge.setBackground(new Color(255, 233, 233));
            statusBadge.setForeground(new Color(180, 40, 40));
        } else if ("CHECKED_IN".equalsIgnoreCase(reservation.getStatus())) {
            statusBadge.setBackground(new Color(231, 246, 236));
            statusBadge.setForeground(new Color(38, 109, 64));
        } else if ("CHECKED_OUT".equalsIgnoreCase(reservation.getStatus())) {
            statusBadge.setBackground(new Color(235, 235, 235));
            statusBadge.setForeground(new Color(90, 90, 90));
        } else {
            statusBadge.setBackground(new Color(234, 240, 255));
            statusBadge.setForeground(new Color(56, 113, 182));
        }

        topRow.add(roomLabel, BorderLayout.WEST);
        topRow.add(statusBadge, BorderLayout.EAST);
        card.add(topRow);
        card.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel dates = new JLabel("Stay: " + reservation.getStartDate() + " to " + reservation.getEndDate());
        dates.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dates.setForeground(UITheme.TEXT_MEDIUM);
        dates.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(dates);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        JLabel pricing = new JLabel("Rate: $" + reservation.getNightlyRate() + "/night  |  Total: $" + reservation.getTotalCost());
        pricing.setFont(new Font("SansSerif", Font.PLAIN, 13));
        pricing.setForeground(UITheme.TEXT_MEDIUM);
        pricing.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(pricing);

        return card;
    }

    public void refreshBillingContent() {
        if (reservationsContainer == null || shopContainer == null) {
            return;
        }

        reservationsContainer.removeAll();
        shopContainer.removeAll();

        Account current = AccountController.currentAccount;
        if (current == null) {
            reservationsContainer.add(createEmptyCard("No guest is logged in."));
            shopContainer.add(createEmptyCard("No guest is logged in."));
            reservationTotalLabel.setText("Reservation Total  $0.00");
            shopTotalLabel.setText("Shop Purchases  $0.00");
            grandTotalLabel.setText("Grand Total  $0.00");
            reservationsContainer.revalidate();
            reservationsContainer.repaint();
            shopContainer.revalidate();
            shopContainer.repaint();
            return;
        }

        // Load billing data for current account's email
        String email = current.getEmail();
        List<Reservation> reservations = billingService.getReservationCharges(email);
        if (reservations.isEmpty()) {
            reservationsContainer.add(createEmptyCard("No reservations found."));
        } else {
            for (Reservation reservation : reservations) {
                reservationsContainer.add(createReservationCard(reservation));
                reservationsContainer.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }

        List<ShopOrder> orders = billingService.getShopPurchases(email);
        if (orders.isEmpty()) {
            shopContainer.add(createEmptyCard("No shop purchases found."));
        } else {
            for (ShopOrder order : orders) {
                shopContainer.add(createShopOrderCard(order));
                shopContainer.add(Box.createRigidArea(new Dimension(0, 12)));
            }
        }

        int reservationTotal = billingService.getReservationTotal(email);
        double shopTotal = billingService.getShopTotal(email);
        double grandTotal = billingService.getGrandTotal(email);

        reservationTotalLabel.setText(String.format("Reservation Total  $%,.2f", (double) reservationTotal));
        shopTotalLabel.setText(String.format("Shop Purchases  $%,.2f", shopTotal));
        grandTotalLabel.setText(String.format("Grand Total  $%,.2f", grandTotal));

        reservationsContainer.revalidate();
        reservationsContainer.repaint();
        shopContainer.revalidate();
        shopContainer.repaint();
    }

    public void refreshPaymentWorkspace() {
        if (payFullNameField == null) {
            return;
        }
        Account account = AccountController.currentAccount;
        if (account == null) {
            if (paymentWorkspaceStatusLabel != null) {
                paymentWorkspaceStatusLabel.setText("");
            }
            if (payCardNumberField != null) {
                payCardNumberField.setText("");
                payCardCvvField.setText("");
            }
            return;
        }
        String email = account.getEmail();
        StripeGuestPreferences.BillingProfileSnapshot snap = StripeGuestPreferences.loadBillingProfile(email);
        payFullNameField.setText(snap.nameOnCard());
        payAddressLine1Field.setText(snap.line1());
        payAddressLine2Field.setText(snap.line2());
        payCityField.setText(snap.city());
        payStateField.setText(snap.state());
        payZipField.setText(snap.zip());
        payPhoneField.setText(snap.phone());
        payCardNicknameField.setText("");
        payCardNumberField.setText("");
        payCardExpiryField.setText("");
        payCardCvvField.setText("");

        double charges = guestLedgerService.getChargesTotal(email);
        double paid = guestLedgerService.getPaymentsApplied(email);
        double due = guestLedgerService.getAmountDue(email);
        if (paymentBalanceSnapshotLabel != null) {
            paymentBalanceSnapshotLabel.setText(String.format(
                    "<html>Total charges <b>$%,.2f</b> &nbsp;·&nbsp; Payments recorded <b>$%,.2f</b> &nbsp;·&nbsp; "
                            + "<span style='color:#6b4f1a;'>Amount due <b>$%,.2f</b></span></html>",
                    charges, paid, due));
        }

        rebuildPaymentMethodsList();
        updateStripeConnectBannerUi(email);
    }

    private void rebuildPaymentMethodsList() {
        if (paymentMethodsListPanel == null) {
            return;
        }
        paymentMethodsListPanel.removeAll();
        Account account = AccountController.currentAccount;
        if (account == null) {
            paymentMethodsListPanel.add(paymentMethodsHintLabel("Sign in to manage saved cards."));
        } else {
            List<SavedPaymentMethod> list = savedPaymentMethodService.listForGuest(account.getEmail());
            if (list.isEmpty()) {
                paymentMethodsListPanel.add(paymentMethodsHintLabel("No saved cards yet. Expand the form below to add one."));
            } else {
                for (SavedPaymentMethod m : list) {
                    paymentMethodsListPanel.add(buildSavedPaymentMethodRow(m));
                    paymentMethodsListPanel.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
        }
        paymentMethodsListPanel.revalidate();
        paymentMethodsListPanel.repaint();
    }

    private JLabel paymentMethodsHintLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setForeground(UITheme.TEXT_MEDIUM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel buildSavedPaymentMethodRow(SavedPaymentMethod m) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(true);
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(228, 218, 200), 1, true),
                new EmptyBorder(12, 14, 12, 14)
        ));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        String nick = m.getNickname() == null || m.getNickname().isBlank() ? "Unnamed" : escapeHtmlLite(m.getNickname());
        String yy = String.format("%02d", m.getExpYear() % 100);
        JLabel info = new JLabel("<html><div style=\"font-size:13px;\"><b>" + escapeHtmlLite(m.getCardBrand()) + "</b> ending in "
                + "<b>" + escapeHtmlLite(m.getLastFour()) + "</b> &nbsp;·&nbsp; " + nick + "<br/>"
                + "<span style=\"color:#777;\">Exp " + m.getExpMonth() + "/" + yy + " · "
                + escapeHtmlLite(m.getNameOnCard()) + "</span></div></html>");
        JButton remove = new JButton("Remove");
        styleButton(remove, false);
        // styleButton uses width 10 so full-width rows expand in BoxLayout; here we need a real width in BorderLayout.EAST.
        Dimension removeSize = new Dimension(96, 42);
        remove.setPreferredSize(removeSize);
        remove.setMinimumSize(removeSize);
        remove.setMaximumSize(new Dimension(120, 42));
        remove.addActionListener(_ -> deleteSavedPaymentMethod(m));
        JPanel eastWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        eastWrap.setOpaque(false);
        eastWrap.add(remove);
        row.add(info, BorderLayout.CENTER);
        row.add(eastWrap, BorderLayout.EAST);
        return row;
    }

    private static String escapeHtmlLite(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private void deleteSavedPaymentMethod(SavedPaymentMethod m) {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Remove this card from your saved payment methods?",
                "Remove card",
                JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            savedPaymentMethodService.deleteMethod(m.getId(), account.getEmail());
            paymentWorkspaceStatusLabel.setText("Removed saved card.");
            rebuildPaymentMethodsList();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not remove card: " + ex.getMessage(),
                    "Payment methods",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void toggleAddPaymentFormVisibility() {
        if (addPaymentFormOuter == null) {
            return;
        }
        boolean next = !addPaymentFormOuter.isVisible();
        addPaymentFormOuter.setVisible(next);
        toggleAddPaymentFormButton.setText(next ? "Hide payment form" : "Add a payment method");
        if (!next) {
            paymentWorkspaceStatusLabel.setText(" ");
        }
        addPaymentFormOuter.revalidate();
        addPaymentFormOuter.getParent().revalidate();
    }

    private void saveNewPaymentMethodFromForm() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }
        PaymentBillingValidator.ValidationResult nick = PaymentBillingValidator.validateOptionalNickname(payCardNicknameField.getText());
        if (!nick.ok()) {
            JOptionPane.showMessageDialog(this, nick.message(), "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        PaymentBillingValidator.ValidationResult bill = PaymentBillingValidator.validateBillingProfile(
                payFullNameField.getText(),
                payAddressLine1Field.getText(),
                payCityField.getText(),
                payStateField.getText(),
                payZipField.getText());
        if (!bill.ok()) {
            JOptionPane.showMessageDialog(this, bill.message(), "Billing validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String pan = new String(payCardNumberField.getPassword()).trim();
        String cvv = new String(payCardCvvField.getPassword()).trim();
        PaymentBillingValidator.ValidationResult card = PaymentBillingValidator.validateCardPracticeFields(
                pan, payCardExpiryField.getText(), cvv, payZipField.getText());
        if (!card.ok()) {
            JOptionPane.showMessageDialog(this, card.message(), "Card validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int[] exp = PaymentBillingValidator.parseExpiryMonthYear(payCardExpiryField.getText());
        if (exp == null) {
            JOptionPane.showMessageDialog(this, "Invalid expiry.", "Card validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String brand = PaymentBillingValidator.inferCardBrand(pan);
        String last4 = PaymentBillingValidator.lastFourDigits(pan);
        try {
            savedPaymentMethodService.saveMethod(
                    account.getEmail(),
                    payCardNicknameField.getText(),
                    brand,
                    last4,
                    exp[0],
                    exp[1],
                    payFullNameField.getText().trim(),
                    payAddressLine1Field.getText().trim(),
                    payAddressLine2Field.getText(),
                    payCityField.getText().trim(),
                    payStateField.getText().trim().toUpperCase(java.util.Locale.ROOT),
                    payZipField.getText().trim(),
                    payPhoneField.getText());
            StripeGuestPreferences.saveBillingProfile(
                    account.getEmail(),
                    payFullNameField.getText(),
                    payAddressLine1Field.getText(),
                    payAddressLine2Field.getText(),
                    payCityField.getText(),
                    payStateField.getText(),
                    payZipField.getText(),
                    payPhoneField.getText());
            payCardNumberField.setText("");
            payCardCvvField.setText("");
            payCardExpiryField.setText("");
            payCardNicknameField.setText("");
            paymentWorkspaceStatusLabel.setText("Payment method saved.");
            addPaymentFormOuter.setVisible(false);
            toggleAddPaymentFormButton.setText("Add a payment method");
            rebuildPaymentMethodsList();
            refreshPaymentWorkspace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not save payment method: " + ex.getMessage(),
                    "Payment methods",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateStripeConnectBannerUi(String email) {
        if (stripeConnectBannerOuter == null || stripeConnectSoleActionButton == null) {
            return;
        }

        boolean hasSecret = StripeConfig.hasSecretKey();
        boolean hasClient = StripeConfig.hasConnectClientId();
        boolean connected = StripeGuestPreferences.isStripeAccountConnected(email);

        if (!hasSecret || !hasClient) {
            stripeConnectBannerOuter.setBackground(new Color(255, 246, 217));
            stripeConnectBannerOuter.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(234, 207, 146), 1, true),
                    new EmptyBorder(16, 18, 16, 18)
            ));
            stripeBannerStatusLarge.setForeground(new Color(150, 114, 42));
            if (!hasSecret && !hasClient) {
                stripeBannerStatusLarge.setText("Stripe not configured");
                stripeBannerDetailSmall.setText("Add STRIPE_SECRET_KEY and STRIPE_CONNECT_CLIENT_ID to your .env file, then restart the app.");
            } else if (!hasSecret) {
                stripeBannerStatusLarge.setText("Stripe not configured");
                stripeBannerDetailSmall.setText("Add STRIPE_SECRET_KEY to your .env file, then restart the app.");
            } else {
                stripeBannerStatusLarge.setText("Stripe not configured");
                stripeBannerDetailSmall.setText("Add STRIPE_CONNECT_CLIENT_ID to your .env file, then restart the app.");
            }
        } else if (connected) {
            stripeConnectBannerOuter.setBackground(new Color(225, 246, 228));
            stripeConnectBannerOuter.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(132, 199, 150), 1, true),
                    new EmptyBorder(16, 18, 16, 18)
            ));
            stripeBannerStatusLarge.setText("Connected");
            stripeBannerStatusLarge.setForeground(new Color(46, 120, 74));
            stripeBannerDetailSmall.setText("Your Stripe account is linked for this computer (sandbox).");
        } else {
            stripeConnectBannerOuter.setBackground(new Color(255, 235, 235));
            stripeConnectBannerOuter.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(214, 160, 160), 1, true),
                    new EmptyBorder(16, 18, 16, 18)
            ));
            stripeBannerStatusLarge.setText("Not connected");
            stripeBannerStatusLarge.setForeground(new Color(160, 40, 40));
            stripeBannerDetailSmall.setText(stripeNotConnectedInstructionPlain());
        }

        if (!hasSecret || !hasClient) {
            stripeConnectSoleActionButton.setText("Connect with Stripe");
            applyStripeSoleButtonTheme(StripeSoleButtonTheme.WARN_BOX);
            clearStripeSoleActionListeners();
            stripeConnectSoleActionButton.setEnabled(false);
        } else if (connected) {
            stripeConnectSoleActionButton.setText("Disconnect from Stripe");
            applyStripeSoleButtonTheme(StripeSoleButtonTheme.GREEN_BOX);
            clearStripeSoleActionListeners();
            stripeConnectSoleActionButton.addActionListener(_ -> disconnectStripeSandboxLink());
            stripeConnectSoleActionButton.setEnabled(true);
        } else {
            stripeConnectSoleActionButton.setText("Connect with Stripe");
            applyStripeSoleButtonTheme(StripeSoleButtonTheme.ROSE_BOX);
            clearStripeSoleActionListeners();
            stripeConnectSoleActionButton.addActionListener(_ -> beginStripeOAuthFromAccountCenter());
            stripeConnectSoleActionButton.setEnabled(true);
        }

        String acctDisplay = shortenId(StripeGuestPreferences.getConnectedAccountId(email));
        String custDisplay = shortenId(StripeGuestPreferences.getStripeCustomerId(email));
        stripeIdsSummaryLabel.setText("<html><span style=\"color:#545454;\">Stripe Connect account:</span> " + acctDisplay
                + " &nbsp;·&nbsp; <span style=\"color:#545454;\">Saved customer:</span> " + custDisplay + "</html>");
    }

    private static String stripeNotConnectedInstructionPlain() {
        return "Click \"Connect with Stripe\" below to sign in with Stripe (sandbox).";
    }

    private enum StripeSoleButtonTheme {
        /** Matches amber “not configured” banner */
        WARN_BOX,
        /** Matches rose “not connected” banner */
        ROSE_BOX,
        /** Matches mint “connected” banner */
        GREEN_BOX
    }

    private void clearStripeSoleActionListeners() {
        for (ActionListener al : stripeConnectSoleActionButton.getActionListeners()) {
            stripeConnectSoleActionButton.removeActionListener(al);
        }
    }

    private void applyStripeSoleButtonTheme(StripeSoleButtonTheme theme) {
        JButton b = stripeConnectSoleActionButton;
        b.setOpaque(true);
        b.setBorderPainted(true);
        b.setContentAreaFilled(true);
        b.setHorizontalAlignment(SwingConstants.CENTER);
        b.setMinimumSize(new Dimension(120, 46));
        b.setPreferredSize(new Dimension(400, 46));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        switch (theme) {
            case WARN_BOX -> {
                b.setBackground(new Color(244, 218, 165));
                b.setForeground(new Color(105, 72, 18));
                b.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(205, 165, 95), 1, true),
                        new EmptyBorder(12, 20, 12, 20)));
            }
            case ROSE_BOX -> {
                b.setBackground(new Color(214, 118, 118));
                b.setForeground(new Color(255, 252, 250));
                b.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(168, 62, 62), 1, true),
                        new EmptyBorder(12, 20, 12, 20)));
            }
            case GREEN_BOX -> {
                b.setBackground(new Color(142, 198, 152));
                b.setForeground(new Color(22, 78, 38));
                b.setBorder(BorderFactory.createCompoundBorder(
                        new LineBorder(new Color(92, 158, 108), 1, true),
                        new EmptyBorder(12, 20, 12, 20)));
            }
        }
    }

    /**
     * @param confirmContinue if true, OK opens Stripe flow; Cancel aborts. If false, read-only message dialog.
     * @return {@code true} if user chose OK (or info-only mode), {@code false} if canceled when {@code confirmContinue}
     */
    private boolean showStripeOAuthSetupInDialog(boolean confirmContinue) {
        String text = StripeConfig.formatStripeConnectOAuthSetupText();
        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setOpaque(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setRows(18);
        area.setColumns(68);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(580, 280));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        if (confirmContinue) {
            int r = JOptionPane.showConfirmDialog(this, scroll,
                    "Stripe Connect — verify, then continue",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            return r == JOptionPane.OK_OPTION;
        }
        JOptionPane.showMessageDialog(this, scroll,
                "Stripe Connect — setup details",
                JOptionPane.INFORMATION_MESSAGE);
        return true;
    }

    private static String shortenId(String id) {
        if (id == null || id.isBlank()) {
            return "<span style=\"color:#a36b6b;\">none yet</span>";
        }
        if (id.length() <= 16) {
            return "<span style=\"font-family:monospace\">" + id + "</span>";
        }
        return "<span style=\"font-family:monospace\">"
                + id.substring(0, 10)
                + "…"
                + id.substring(id.length() - 4)
                + "</span>";
    }

    private void beginStripeOAuthFromAccountCenter() {
        Account acc = AccountController.currentAccount;
        if (acc == null) {
            return;
        }
        if (!StripeConfig.hasSecretKey()) {
            JOptionPane.showMessageDialog(this,
                    "Add STRIPE_SECRET_KEY (sandbox) to .env before using OAuth.",
                    "Stripe Connect",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!StripeConfig.hasConnectClientId()) {
            JOptionPane.showMessageDialog(this,
                    "<html>Add STRIPE_CONNECT_CLIENT_ID (<code>ca_…</code>) and in Stripe (Test mode) register this OAuth redirect URI exactly:<br/><code>"
                            + StripeConfig.oauthRedirectUri() + "</code><br/>(must include <code>/oauth/</code> in the path.)</html>",
                    "Stripe Connect",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!showStripeOAuthSetupInDialog(true)) {
            paymentWorkspaceStatusLabel.setText("Stripe Connect canceled.");
            return;
        }

        try {
            StripeConfig.logOAuthRedirectForStripeConnect();
            paymentWorkspaceStatusLabel.setText("Opening Stripe… listening on "
                    + StripeConfig.oauthListenHost() + ":" + StripeConfig.oauthListenPort()
                    + " (details were in the dialog; View OAuth setup repeats them).");

            StripeHostedLocalServer.OAuthListening oauth = StripeHostedLocalServer.bindOAuth(
                    code -> SwingUtilities.invokeLater(() -> exchangeStripeOAuthAndPersist(acc.getEmail(), code)));

            if (!StripeHostedLocalServer.browse(oauth.authorizeUrl())) {
                oauth.stopQuietly();
                JOptionPane.showMessageDialog(this,
                        "Could not launch a browser. Complete OAuth manually.",
                        "Stripe Connect",
                        JOptionPane.WARNING_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not bind local Stripe listener:\n" + ex.getMessage(),
                    "Stripe Connect",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exchangeStripeOAuthAndPersist(String email, String authorizationCode) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            paymentWorkspaceStatusLabel.setText("Stripe Connect was interrupted.");
            refreshPaymentWorkspace();
            return;
        }

        new SwingWorker<String, Void>() {
            String error;

            @Override
            protected String doInBackground() {
                try {
                    return StripeConnectOAuthTokenClient.exchangeCodeForStripeUserId(authorizationCode);
                } catch (StripeConnectOAuthTokenClient.StripeOAuthException ex) {
                    error = ex.getMessage();
                } catch (IOException | InterruptedException ex) {
                    error = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                    if (ex instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                String acctId;
                try {
                    acctId = get();
                } catch (Exception ex) {
                    error = error != null ? error : ex.getMessage();
                    JOptionPane.showMessageDialog(AccountCenterPage.this,
                            error, "Stripe Connect", JOptionPane.ERROR_MESSAGE);
                    refreshPaymentWorkspace();
                    return;
                }
                if (error != null || acctId == null) {
                    JOptionPane.showMessageDialog(AccountCenterPage.this,
                            error != null ? error : "Stripe did not accept the OAuth handshake.",
                            "Stripe Connect",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    StripeGuestPreferences.setConnectedAccountId(email, acctId);
                    JOptionPane.showMessageDialog(AccountCenterPage.this,
                            "Stripe account linked locally for sandbox demos.\nStripe user id:\n" + acctId,
                            "Stripe Connect",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                paymentWorkspaceStatusLabel.setText(" ");
                refreshPaymentWorkspace();
            }
        }.execute();
    }

    private void disconnectStripeSandboxLink() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }
        int decision = JOptionPane.showConfirmDialog(this,
                "Remove the saved Stripe Connect account id only on this computer?",
                "Disconnect Stripe Connect",
                JOptionPane.OK_CANCEL_OPTION);
        if (decision != JOptionPane.OK_OPTION) {
            return;
        }
        StripeGuestPreferences.setConnectedAccountId(account.getEmail(), "");
        if (paymentWorkspaceStatusLabel != null) {
            paymentWorkspaceStatusLabel.setText("Disconnected Stripe sandbox link locally.");
        }
        refreshPaymentWorkspace();
    }

    private JPanel createReservationCard(Reservation reservation) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(228, 223, 214), 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Check if this is a cancelled booking
        boolean isCancelled = "CANCELLED".equalsIgnoreCase(reservation.getStatus());

        String roomText = reservation.getRooms().stream()
                .map(r -> "Room " + r.getRoomNumber())
                .reduce((a, b) -> a + ", " + b)
                .orElse("No rooms");

        JPanel topRow = new JPanel(new BorderLayout(12, 0));
        topRow.setOpaque(false);
        topRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel top = new JLabel(roomText);
        top.setFont(new Font("SansSerif", Font.BOLD, 17));
        top.setForeground(UITheme.TEXT_DARK);

        JLabel statusBadge = new JLabel(isCancelled ? "CANCELLED" : "CONFIRMED");
        statusBadge.setFont(new Font("SansSerif", Font.BOLD, 11));
        statusBadge.setOpaque(true);
        statusBadge.setBorder(new EmptyBorder(4, 8, 4, 8));
        if (isCancelled) {
            statusBadge.setBackground(new Color(255, 233, 233));
            statusBadge.setForeground(new Color(180, 40, 40));
        } else {
            statusBadge.setBackground(new Color(231, 246, 236));
            statusBadge.setForeground(new Color(38, 109, 64));
        }
        topRow.add(top, BorderLayout.WEST);
        topRow.add(statusBadge, BorderLayout.EAST);

        JLabel dates = new JLabel("Dates: " + reservation.getStartDate() + " to " + reservation.getEndDate());
        dates.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dates.setForeground(UITheme.TEXT_MEDIUM);
        dates.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(topRow);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(dates);

        // Only show Nightly Rate details if the reservation is ACTIVE
        if (!isCancelled) {
            JLabel nightly = new JLabel("Nightly Rate: $" + reservation.getNightlyRate() + ".00");
            nightly.setFont(new Font("SansSerif", Font.PLAIN, 14));
            nightly.setForeground(UITheme.TEXT_MEDIUM);
            nightly.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel nights = new JLabel("Nights: " + reservation.getNights());
            nights.setFont(new Font("SansSerif", Font.PLAIN, 14));
            nights.setForeground(UITheme.TEXT_MEDIUM);
            nights.setAlignmentX(Component.LEFT_ALIGNMENT);

            card.add(Box.createRigidArea(new Dimension(0, 4)));
            card.add(nightly);
            card.add(Box.createRigidArea(new Dimension(0, 4)));
            card.add(nights);
        }

        // The Charge Label
        JLabel total = new JLabel();
        if (isCancelled) {
            total.setText("Cancellation Fee: $" + reservation.getTotalCost() + ".00");
            total.setForeground(new Color(184, 45, 45));
        } else {
            total.setText("Reservation Total: $" + reservation.getTotalCost() + ".00");
            total.setForeground(BILLING_POSITIVE);
        }
        total.setFont(new Font("SansSerif", Font.BOLD, 16));
        total.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(total);

        return card;
    }

    private JPanel createShopOrderCard(ShopOrder order) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(228, 223, 214), 1, true),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel top = new JLabel("Purchase Date: " + order.getPurchaseDate());
        top.setFont(new Font("SansSerif", Font.BOLD, 18));
        top.setForeground(UITheme.TEXT_DARK);
        top.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(top);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        for (ShopOrderItem item : order.getItems()) {
            JPanel line = new JPanel(new BorderLayout(8, 0));
            line.setOpaque(false);
            line.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel itemLabel = new JLabel(item.getItemName() + "  (x" + item.getQuantity() + ")");
            itemLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            itemLabel.setForeground(UITheme.TEXT_MEDIUM);

            JLabel lineTotal = new JLabel(String.format("$%.2f", item.getLineTotal()));
            lineTotal.setFont(new Font("SansSerif", Font.BOLD, 14));
            lineTotal.setForeground(UITheme.TEXT_DARK);

            line.add(itemLabel, BorderLayout.WEST);
            line.add(lineTotal, BorderLayout.EAST);
            card.add(line);
            card.add(Box.createRigidArea(new Dimension(0, 6)));
        }

        JLabel total = new JLabel(String.format("Order Total: $%.2f", order.getTotalCost()));
        total.setFont(new Font("SansSerif", Font.BOLD, 16));
        total.setForeground(BILLING_POSITIVE);
        total.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(total);

        return card;
    }

    private JPanel createEmptyCard(String text) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(253, 251, 246));
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(232, 224, 207), 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 14));
        label.setForeground(UITheme.TEXT_MEDIUM);
        card.add(label, BorderLayout.CENTER);
        return card;
    }

    private JLabel createTotalLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 17));
        label.setForeground(UITheme.TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createGrandTotalLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(BILLING_ACCENT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
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













