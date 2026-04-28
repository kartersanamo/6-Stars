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
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class AccountDetailsPage extends JPanel {
    private static final int AVATAR_SIZE = 168;
    private static final int AVATAR_EXPORT_SIZE = 512;
    private static final long MAX_AVATAR_BYTES = 5L * 1024L * 1024L;
    private static final int MIN_AVATAR_DIMENSION = 128;
    private static final char MASK_ECHO = new JPasswordField().getEchoChar();
    private static final String PREF_NODE_PREFIX = "account-details-";

    private final JPanel pages;
    private final CardLayout cardLayout;
    private final AccountController accountController;
    private final Preferences preferencesRoot = Preferences.userNodeForPackage(AccountDetailsPage.class);

    private final AvatarPanel avatarPanel = new AvatarPanel();
    private final JLabel avatarStatusLabel = new JLabel();
    // Profile completion UI removed per user request
    private final JLabel roleBadgeLabel = new JLabel("Guest");
    private final JLabel accountTypeLabel = new JLabel("Standard Account");
    private final JLabel emailValueLabel = new JLabel("Unknown");
    private final JLabel roleValueLabel = new JLabel("Unknown");
    private final JLabel preferenceHintLabel = new JLabel();
    private final JLabel passwordRulesLabel = new JLabel();

    private final JTextField firstNameField = new JTextField();
    private final JTextField lastNameField = new JTextField();
    private final JPasswordField currentPasswordField = new JPasswordField();
    private final JPasswordField newPasswordField = new JPasswordField();
    private final JPasswordField confirmPasswordField = new JPasswordField();

    private final JCheckBox showPasswordsCheck = new JCheckBox("Show password fields");
    private final JCheckBox emailReceiptsCheck = new JCheckBox("Email receipts");
    private final JCheckBox reservationReminderCheck = new JCheckBox("Reservation reminders");
    private final JCheckBox shopPromotionsCheck = new JCheckBox("Shop promotions");

    private final JButton saveProfileButton = new JButton("Save Profile Changes");
    private final JButton resetProfileButton = new JButton("Reset Profile");
    private final JButton uploadPhotoButton = new JButton("Upload Photo");
    private final JButton removePhotoButton = new JButton("Remove Photo");
    private final JButton updatePasswordButton = new JButton("Update Password");
    private final JButton reservationsButton = new JButton("View Reservations");
    private final JButton shopButton = new JButton("Open Shop");
    private final JButton billingButton = new JButton("View Billing");
    private final JButton dashboardButton = new JButton("Go to Dashboard");
    private final JButton signOutButton = new JButton("Sign Out");
    private final JButton backButton = new JButton("Back");

    private boolean loadingPreferences = false;

    public AccountDetailsPage(JPanel pages, CardLayout cardLayout, AccountController accountController) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.accountController = accountController;

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JPanel contentCard = new JPanel();
        contentCard.setLayout(new BoxLayout(contentCard, BoxLayout.Y_AXIS));
        contentCard.setBackground(UITheme.CARD_BACKGROUND);
        contentCard.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(28, 32, 28, 32)
        ));
        contentCard.setMaximumSize(new Dimension(1080, Integer.MAX_VALUE));
        // Do not force a tall preferred size — allow the card to size itself so the
        // scroll viewport can work naturally. Keep a maximum width so the layout stays centered.

        contentCard.add(buildHeroPanel());
        contentCard.add(Box.createRigidArea(new Dimension(0, 18)));
        contentCard.add(buildMainGrid());
        contentCard.add(Box.createRigidArea(new Dimension(0, 18)));
        contentCard.add(buildFooterBar());

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        // Anchor the content to the top (NORTH) so the scroll viewport grows below it
        wrapper.add(contentCard, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, BorderLayout.CENTER);

        wireEvents();
        refreshInfo();
    }

    private JPanel buildHeroPanel() {
        JPanel hero = new JPanel(new BorderLayout(18, 0));
        hero.setOpaque(true);
        hero.setBackground(new Color(247, 240, 228));
        hero.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(218, 205, 181), 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));

        JPanel heroText = new JPanel();
        heroText.setOpaque(false);
        heroText.setLayout(new BoxLayout(heroText, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Account Center");
        title.setFont(new Font("Serif", Font.BOLD, 30));
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Update your profile photo, security settings, and account preferences in one polished place.");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        badgeRow.setOpaque(false);
        roleBadgeLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        roleBadgeLabel.setBorder(new EmptyBorder(6, 14, 6, 14));
        roleBadgeLabel.setOpaque(true);
        badgeRow.add(roleBadgeLabel);

        accountTypeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        accountTypeLabel.setForeground(UITheme.TEXT_MEDIUM);
        badgeRow.add(accountTypeLabel);

        // Profile completion label and progress bar removed

        heroText.add(title);
        heroText.add(Box.createRigidArea(new Dimension(0, 6)));
        heroText.add(subtitle);
        heroText.add(Box.createRigidArea(new Dimension(0, 12)));
        heroText.add(badgeRow);
        heroText.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel avatarMini = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        avatarMini.setOpaque(false);
        avatarPanel.setPreferredSize(new Dimension(AVATAR_SIZE, AVATAR_SIZE));
        avatarMini.add(avatarPanel);

        hero.add(heroText, BorderLayout.CENTER);
        hero.add(avatarMini, BorderLayout.EAST);
        return hero;
    }

    private JPanel buildMainGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, 18, 18));
        grid.setOpaque(false);
        grid.add(buildProfileCard());
        grid.add(buildSecurityCard());
        grid.add(buildPreferencesCard());
        grid.add(buildQuickActionsCard());
        return grid;
    }

    private JPanel buildProfileCard() {
        JPanel card = createCardPanel();
        card.add(buildSectionTitle("Profile Information", "Update your public-facing account details and avatar."));
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel topRow = new JPanel(new BorderLayout(18, 0));
        topRow.setOpaque(false);

        JPanel avatarStack = new JPanel();
        avatarStack.setOpaque(false);
        avatarStack.setLayout(new BoxLayout(avatarStack, BoxLayout.Y_AXIS));
        avatarPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarStack.add(avatarPanel);
        avatarStack.add(Box.createRigidArea(new Dimension(0, 10)));
        avatarStatusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        avatarStatusLabel.setForeground(UITheme.TEXT_MEDIUM);
        avatarStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarStack.add(avatarStatusLabel);
        avatarStack.add(Box.createRigidArea(new Dimension(0, 8)));
        avatarStack.add(centeredButton(uploadPhotoButton, 176, 38, true));
        avatarStack.add(Box.createRigidArea(new Dimension(0, 8)));
        avatarStack.add(centeredButton(removePhotoButton, 176, 38, false));
        // limit avatar column width so the fields column has room
        avatarStack.setPreferredSize(new Dimension(AVATAR_SIZE + 40, AVATAR_SIZE + 120));

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);
        row1.add(createLabeledFieldCard("First Name", firstNameField, true));
        row1.add(createLabeledFieldCard("Last Name", lastNameField, true));

        JPanel row2 = new JPanel(new GridLayout(1, 2, 10, 0));
        row2.setOpaque(false);
        row2.add(createLabeledFieldCard("Email Address", emailValueLabel, false));
        row2.add(createLabeledFieldCard("Role", roleValueLabel, false));

        fields.add(row1);
        fields.add(Box.createRigidArea(new Dimension(0, 8)));
        fields.add(row2);

        topRow.add(avatarStack, BorderLayout.WEST);
        topRow.add(fields, BorderLayout.CENTER);
        card.add(topRow);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel notePanel = new JPanel(new BorderLayout());
        notePanel.setOpaque(false);
        JLabel note = new JLabel("Photo requirements: JPG, JPEG, PNG, or GIF. Max 5 MB. Minimum size 128×128.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        note.setForeground(UITheme.TEXT_MEDIUM);
        notePanel.add(note, BorderLayout.CENTER);
        card.add(notePanel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0));
        actions.setOpaque(false);
        actions.add(styleActionButton(saveProfileButton, true));
        actions.add(styleActionButton(resetProfileButton, false));
        card.add(actions);

        return card;
    }

    private JPanel buildSecurityCard() {
        JPanel card = createCardPanel();
        card.add(buildSectionTitle("Security Center", "Change your password with current-password verification."));
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel passwordGrid = new JPanel(new GridLayout(3, 1, 10, 10));
        passwordGrid.setOpaque(false);
        passwordGrid.add(createPasswordCard("Current Password", currentPasswordField));
        passwordGrid.add(createPasswordCard("New Password", newPasswordField));
        passwordGrid.add(createPasswordCard("Confirm New Password", confirmPasswordField));
        card.add(passwordGrid);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        showPasswordsCheck.setOpaque(false);
        showPasswordsCheck.setFont(new Font("SansSerif", Font.PLAIN, 13));
        showPasswordsCheck.setForeground(UITheme.TEXT_DARK);
        showPasswordsCheck.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(showPasswordsCheck);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        passwordRulesLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        passwordRulesLabel.setForeground(UITheme.TEXT_MEDIUM);
        passwordRulesLabel.setText("<html><b>Password rules:</b> at least 8 characters, with uppercase, lowercase, number, and special character.</html>");
        card.add(passwordRulesLabel);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        card.add(styleActionButton(updatePasswordButton, true));
        return card;
    }

    private JPanel buildPreferencesCard() {
        JPanel card = createCardPanel();
        card.add(buildSectionTitle("Preferences", "Saved locally for this account on this device."));
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        preferenceHintLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        preferenceHintLabel.setForeground(UITheme.TEXT_MEDIUM);
        preferenceHintLabel.setText("Choose how we should communicate with you and present account-related updates.");
        card.add(preferenceHintLabel);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        stylePreferenceCheckBox(emailReceiptsCheck, "Email receipts for reservations and shop orders");
        stylePreferenceCheckBox(reservationReminderCheck, "Reservation reminder notifications");
        stylePreferenceCheckBox(shopPromotionsCheck, "Occasional shop promotions and special offers");

        card.add(emailReceiptsCheck);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(reservationReminderCheck);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(shopPromotionsCheck);

        return card;
    }

    private JPanel buildQuickActionsCard() {
        JPanel card = createCardPanel();
        card.add(buildSectionTitle("Quick Actions", "Jump to common areas without leaving the page."));
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JPanel buttonGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonGrid.setOpaque(false);

        styleQuickActionButton(reservationsButton);
        styleQuickActionButton(shopButton);
        styleQuickActionButton(billingButton);
        styleQuickActionButton(dashboardButton);

        buttonGrid.add(reservationsButton);
        buttonGrid.add(shopButton);
        buttonGrid.add(billingButton);
        buttonGrid.add(dashboardButton);
        card.add(buttonGrid);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        JLabel helper = new JLabel("Use the dashboard button to return to your role-specific landing page.");
        helper.setFont(new Font("SansSerif", Font.PLAIN, 12));
        helper.setForeground(UITheme.TEXT_MEDIUM);
        card.add(helper);
        card.add(Box.createRigidArea(new Dimension(0, 12)));
        return card;
    }

    private JPanel buildFooterBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(true);
        footer.setBackground(new Color(250, 248, 244));
        footer.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(12, 16, 12, 16)
        ));

        JLabel footerHint = new JLabel("All changes are saved immediately after you choose an action.");
        footerHint.setFont(new Font("SansSerif", Font.PLAIN, 12));
        footerHint.setForeground(UITheme.TEXT_MEDIUM);
        footer.add(footerHint, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(backButton);
        actions.add(signOutButton);
        footer.add(actions, BorderLayout.EAST);

        return footer;
    }

    private JPanel createCardPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(true);
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(18, 18, 18, 18)
        ));
        return card;
    }

    private JPanel buildSectionTitle(String title, String subtitle) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        titleLabel.setForeground(UITheme.TEXT_DARK);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitleLabel.setForeground(UITheme.TEXT_MEDIUM);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 4)));
        panel.add(subtitleLabel);
        // Allow the title block to stretch the full width of its parent card
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
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
        // Let the card expand horizontally to fill the parent container
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
            // Make input fields a consistent height
            textField.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
            textField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        } else if (component instanceof JLabel valueLabel) {
            // Style value labels to visually match inputs for a consistent card look
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

    private JButton styleActionButton(JButton button, boolean primary) {
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

    private void styleQuickActionButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(244, 244, 244));
        button.setForeground(UITheme.TEXT_DARK);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        button.setOpaque(true);
    }

    private void stylePreferenceCheckBox(JCheckBox checkBox, String text) {
        checkBox.setText(text);
        checkBox.setOpaque(false);
        checkBox.setFont(new Font("SansSerif", Font.PLAIN, 13));
        checkBox.setForeground(UITheme.TEXT_DARK);
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
        // Keep fields at a friendly touch-target height
        field.setPreferredSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
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

    private JButton centeredButton(JButton button, int width, int height, boolean primary) {
        button.setPreferredSize(new Dimension(width, height));
        button.setMaximumSize(new Dimension(width, height));
        button.setMinimumSize(new Dimension(width, height));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(primary ? UITheme.ACCENT_GOLD : UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        if (primary) {
            button.setBackground(UITheme.ACCENT_GOLD);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(244, 244, 244));
            button.setForeground(UITheme.TEXT_DARK);
        }
        return button;
    }

    private void wireEvents() {
        saveProfileButton.addActionListener(e -> saveProfileChanges());
        resetProfileButton.addActionListener(e -> refreshInfo());
        uploadPhotoButton.addActionListener(e -> uploadProfilePhoto());
        removePhotoButton.addActionListener(e -> removeProfilePhoto());
        updatePasswordButton.addActionListener(e -> updatePassword());
        reservationsButton.addActionListener(e -> {
            if (Main.reservationsPage != null) {
                Main.reservationsPage.refresh();
            }
            cardLayout.show(pages, "reservations");
        });
        shopButton.addActionListener(e -> {
            if (Main.shopPage != null) {
                Main.shopPage.refreshInventory();
            }
            cardLayout.show(pages, "shop");
        });
        billingButton.addActionListener(e -> {
            if (Main.billingPage != null) {
                Main.billingPage.refresh();
            }
            cardLayout.show(pages, "billing page");
        });
        dashboardButton.addActionListener(e -> navigateBackByRole());
        signOutButton.addActionListener(e -> signOut());
        backButton.addActionListener(e -> navigateBackByRole());
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

        firstNameField.setText(account.getFirstName());
        lastNameField.setText(account.getLastName());
        emailValueLabel.setText(account.getEmail());
        roleValueLabel.setText(formatRole(account.getRole().name()));
        accountTypeLabel.setText(formatAccountType(account.getRole()));
        roleBadgeLabel.setText(formatRole(account.getRole().name()));
        styleRoleBadge(account.getRole());

        avatarPanel.setAccount(account);
        avatarStatusLabel.setText(account.getProfileImagePath() == null || account.getProfileImagePath().isBlank()
                ? "No profile photo uploaded yet."
                : "Profile photo is uploaded and ready.");

        updateQuickActionVisibility(account.getRole());
        loadPreferences(account);
        clearPasswordFields();
        togglePasswordVisibility(showPasswordsCheck.isSelected());
        revalidate();
        repaint();
    }

    private void resetForNoAccount() {
        firstNameField.setText("");
        lastNameField.setText("");
        emailValueLabel.setText("Unknown");
        roleValueLabel.setText("Unknown");
        accountTypeLabel.setText("Standard Account");
        roleBadgeLabel.setText("Guest");
        avatarPanel.clear();
        avatarStatusLabel.setText("Log in to edit your profile.");
        // Profile completion UI removed
        clearPasswordFields();
        showPasswordsCheck.setSelected(false);
        togglePasswordVisibility(false);
        emailReceiptsCheck.setEnabled(false);
        reservationReminderCheck.setEnabled(false);
        shopPromotionsCheck.setEnabled(false);
        updatePasswordButton.setEnabled(false);
        saveProfileButton.setEnabled(false);
        resetProfileButton.setEnabled(false);
        uploadPhotoButton.setEnabled(false);
        removePhotoButton.setEnabled(false);
        reservationsButton.setEnabled(false);
        shopButton.setEnabled(false);
        billingButton.setEnabled(false);
        dashboardButton.setEnabled(false);
        signOutButton.setEnabled(false);
        backButton.setEnabled(true);
        preferenceHintLabel.setText("Log in to personalize your preferences.");
    }

    private void updateQuickActionVisibility(Role role) {
        boolean isAdmin = role == Role.ADMIN;
        reservationsButton.setEnabled(true);
        shopButton.setEnabled(true);
        billingButton.setEnabled(true);
        dashboardButton.setEnabled(true);
        signOutButton.setEnabled(true);
        saveProfileButton.setEnabled(true);
        resetProfileButton.setEnabled(true);
        uploadPhotoButton.setEnabled(true);
        removePhotoButton.setEnabled(true);
        updatePasswordButton.setEnabled(true);
        emailReceiptsCheck.setEnabled(true);
        reservationReminderCheck.setEnabled(true);
        shopPromotionsCheck.setEnabled(true);

        dashboardButton.setText(isAdmin ? "Go to Admin Page" : role == Role.CLERK ? "Go to Clerk Dashboard" : "Go to Home");
    }

    private void saveProfileChanges() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            JOptionPane.showMessageDialog(this, "Please log in to update your profile.", "No Account", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        if (firstName.isEmpty() || lastName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "First name and last name cannot be blank.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            accountController.updateProfileDetails(firstName, lastName, account.getProfileImagePath());
            Main.headerBar.refreshInfo();
            refreshInfo();
            JOptionPane.showMessageDialog(this, "Profile updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Profile Update Failed", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Password updated successfully.", "Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Password Update Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void uploadProfilePhoto() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            JOptionPane.showMessageDialog(this, "Please log in first.", "No Account", JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Profile photo updated.", "Photo Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Photo Upload Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeProfilePhoto() {
        Account account = AccountController.currentAccount;
        if (account == null) {
            return;
        }

        if (account.getProfileImagePath() == null || account.getProfileImagePath().isBlank()) {
            JOptionPane.showMessageDialog(this, "There is no profile photo to remove.", "No Photo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Remove this profile photo and return to the initials avatar?",
                "Remove Photo",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            String previousPath = account.getProfileImagePath();
            accountController.removeProfileImage();
            deleteManagedAvatar(previousPath);
            Main.headerBar.refreshInfo();
            refreshInfo();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Photo Remove Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void signOut() {
        AccountController.currentAccount = null;
        Main.headerBar.refreshInfo();
        cardLayout.show(pages, "login");
    }

    private void navigateBackByRole() {
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

    private void loadPreferences(Account account) {
        loadingPreferences = true;
        try {
            Preferences prefs = preferencesFor(account);
            emailReceiptsCheck.setSelected(prefs.getBoolean("emailReceipts", true));
            reservationReminderCheck.setSelected(prefs.getBoolean("reservationReminders", true));
            shopPromotionsCheck.setSelected(prefs.getBoolean("shopPromotions", false));
            preferenceHintLabel.setText("Saved locally for " + account.getEmail() + ".");
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

    private void styleRoleBadge(Role role) {
        if (role == Role.ADMIN) {
            roleBadgeLabel.setBackground(new Color(220, 198, 117));
            roleBadgeLabel.setForeground(UITheme.TEXT_DARK);
        } else if (role == Role.CLERK) {
            roleBadgeLabel.setBackground(new Color(197, 217, 236));
            roleBadgeLabel.setForeground(UITheme.TEXT_DARK);
        } else {
            roleBadgeLabel.setBackground(new Color(211, 233, 204));
            roleBadgeLabel.setForeground(UITheme.TEXT_DARK);
        }
    }

    // Profile completion tracking removed per user request

    private String formatAccountType(Role role) {
        if (role == Role.ADMIN) return "Administrator Account";
        if (role == Role.CLERK) return "Clerk Account";
        return "Guest Account";
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

    private JPanel createCenteredButtonRow(JButton button, int width, int height, boolean primary) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        row.setOpaque(false);
        row.add(centeredButton(button, width, height, primary));
        return row;
    }

    private void centerActionButton(JButton button, int width, int height, boolean primary) {
        button.setPreferredSize(new Dimension(width, height));
        button.setMaximumSize(new Dimension(width, height));
        button.setMinimumSize(new Dimension(width, height));
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(primary ? UITheme.ACCENT_GOLD : UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        if (primary) {
            button.setBackground(UITheme.ACCENT_GOLD);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(244, 244, 244));
            button.setForeground(UITheme.TEXT_DARK);
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
                Shape clip = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 30, 30);
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
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 30, 30));

                if (image == null) {
                    g2.setColor(new Color(70, 50, 35));
                    g2.setFont(new Font("SansSerif", Font.BOLD, 40));
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

