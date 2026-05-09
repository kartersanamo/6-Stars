package com.sixstars.ui;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.GuestPaymentRecord;
import com.sixstars.model.Reservation;
import com.sixstars.model.Role;
import com.sixstars.model.SavedPaymentMethod;
import com.sixstars.model.ShopOrder;
import com.sixstars.model.ShopOrderItem;
import com.sixstars.service.BillingService;
import com.sixstars.service.GuestLedgerService;
import com.sixstars.service.SavedPaymentMethodService;
import com.sixstars.service.stripe.StripeCheckoutService;
import com.sixstars.service.stripe.StripeCheckoutSessionReader;
import com.sixstars.service.stripe.StripeConfig;
import com.sixstars.service.stripe.StripeGuestPreferences;
import com.sixstars.service.stripe.StripeHostedLocalServer;
import com.stripe.exception.StripeException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BillingPage extends JPanel {

    private static final DateTimeFormatter RECEIPT_TIME =
            DateTimeFormatter.ofPattern("MMMM d, yyyy 'at' h:mm a", Locale.US);

    private static final Color STRIPE_ZONE_BG = new Color(244, 249, 255);
    private static final Color SAVED_ZONE_BG = new Color(255, 252, 245);
    private static final Color HISTORY_ZONE_BG = new Color(250, 250, 252);

    private final BillingService billingService;
    private final GuestLedgerService guestLedgerService = new GuestLedgerService();
    private final SavedPaymentMethodService savedPaymentMethodService = new SavedPaymentMethodService();
    private final StripeCheckoutService stripeCheckoutService = new StripeCheckoutService();

    private final JPanel reservationsContainer;
    private final JPanel shopContainer;
    private final JLabel reservationTotalLabel;
    private final JLabel shopTotalLabel;
    private final JLabel grandTotalLabel;
    private final JLabel paymentsAppliedLabel;
    private final JLabel amountDueLabel;

    private final JLabel stripePayTitle;
    private final JLabel stripePayBlurb;
    private final JButton btnPayWithStripe;
    private final JButton btnConnectStripeFromBilling;

    private final JLabel savedPayBlurb;
    private final JComboBox<SavedPaymentMethod> savedMethodCombo;
    private final JButton btnPayWithCard;
    private final JButton btnAddPaymentMethod;

    private final JPanel paymentHistoryInner;

    public BillingPage() {
        billingService = new BillingService();

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JLabel title = new JLabel("Guest Billing");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Review charges, pay what you owe, and open receipts anytime.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(UITheme.CARD_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(18, 20, 18, 20)
        ));

        JPanel titleText = new JPanel();
        titleText.setLayout(new BoxLayout(titleText, BoxLayout.Y_AXIS));
        titleText.setOpaque(false);

        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleText.add(title);
        titleText.add(Box.createRigidArea(new Dimension(0, 4)));
        titleText.add(subtitle);

        JButton btnBack = new JButton("Back");
        styleSecondaryButton(btnBack);

        btnBack.addActionListener(e -> {
            Account acc = AccountController.currentAccount;
            Container parent = getParent();

            if (parent.getLayout() instanceof CardLayout cl) {
                if (acc != null && acc.getRole() == Role.CLERK) {
                    cl.show(parent, "clerk page");
                } else {
                    cl.show(parent, "home");
                }
            }
        });

        header.add(titleText, BorderLayout.CENTER);
        header.add(btnBack, BorderLayout.EAST);

        reservationsContainer = new JPanel();
        reservationsContainer.setLayout(new BoxLayout(reservationsContainer, BoxLayout.Y_AXIS));
        reservationsContainer.setOpaque(false);

        shopContainer = new JPanel();
        shopContainer.setLayout(new BoxLayout(shopContainer, BoxLayout.Y_AXIS));
        shopContainer.setOpaque(false);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(24, 30, 24, 30));

        body.add(createSectionTitle("Reservation Charges"));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(reservationsContainer);
        body.add(Box.createRigidArea(new Dimension(0, 24)));
        body.add(createSectionTitle("Shop Purchases"));
        body.add(Box.createRigidArea(new Dimension(0, 10)));
        body.add(shopContainer);
        body.add(Box.createRigidArea(new Dimension(0, 24)));

        JPanel totalsCard = new JPanel();
        totalsCard.setLayout(new BoxLayout(totalsCard, BoxLayout.Y_AXIS));
        totalsCard.setBackground(UITheme.CARD_BACKGROUND);
        totalsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));
        totalsCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalsCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 360));

        reservationTotalLabel = createTotalLabel("");
        shopTotalLabel = createTotalLabel("");
        grandTotalLabel = createGrandTotalLabel("");
        paymentsAppliedLabel = createMutedTotalLabel("");
        amountDueLabel = createDueLabel("");

        totalsCard.add(reservationTotalLabel);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 8)));
        totalsCard.add(shopTotalLabel);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 10)));
        totalsCard.add(grandTotalLabel);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 12)));
        totalsCard.add(new JSeparator());
        totalsCard.add(Box.createRigidArea(new Dimension(0, 12)));
        totalsCard.add(paymentsAppliedLabel);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 6)));
        totalsCard.add(amountDueLabel);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 14)));
        JLabel totalsPayHint = new JLabel("Saved cards, Stripe Connect, and quick pay live in Account Center.");
        totalsPayHint.setFont(new Font("SansSerif", Font.PLAIN, 13));
        totalsPayHint.setForeground(UITheme.TEXT_MEDIUM);
        totalsPayHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        totalsCard.add(totalsPayHint);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 8)));
        JButton btnOpenPaymentFromTotals = new JButton("Open Account Center → Payment");
        styleTotalsPaymentLinkButton(btnOpenPaymentFromTotals);
        btnOpenPaymentFromTotals.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnOpenPaymentFromTotals.addActionListener(e -> navigateToAccountCenterPayment(false));
        totalsCard.add(btnOpenPaymentFromTotals);

        body.add(totalsCard);
        body.add(Box.createRigidArea(new Dimension(0, 22)));

        JPanel payNowShell = new JPanel();
        payNowShell.setLayout(new BoxLayout(payNowShell, BoxLayout.Y_AXIS));
        payNowShell.setOpaque(true);
        payNowShell.setBackground(UITheme.CARD_BACKGROUND);
        payNowShell.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 190, 170), 1, true),
                new EmptyBorder(22, 22, 22, 22)
        ));
        payNowShell.setAlignmentX(Component.LEFT_ALIGNMENT);
        payNowShell.setMaximumSize(new Dimension(Integer.MAX_VALUE, 920));

        JLabel payNowHeading = new JLabel("Pay now");
        payNowHeading.setFont(new Font("SansSerif", Font.BOLD, 22));
        payNowHeading.setForeground(UITheme.TEXT_DARK);
        payNowHeading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel payNowSub = new JLabel("<html><div style=\"width:620px\">Choose Stripe after you have connected your account in "
                + "<b>Account Center → Payment</b>, or pay the amount due with a saved card "
                + "<span style=\"color:#666;\">(simulated in this demo — no real charge is made).</span></div></html>");
        payNowSub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        payNowSub.setForeground(UITheme.TEXT_MEDIUM);
        payNowSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        payNowShell.add(payNowHeading);
        payNowShell.add(Box.createRigidArea(new Dimension(0, 8)));
        payNowShell.add(payNowSub);
        payNowShell.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel payHeroRow = new JPanel(new GridLayout(1, 2, 14, 0));
        payHeroRow.setOpaque(false);
        payHeroRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        payHeroRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));

        btnPayWithStripe = new JButton("Pay with Stripe");
        stylePrimaryGoldButton(btnPayWithStripe);
        btnPayWithStripe.addActionListener(e -> startStripeSandboxCheckout());

        btnPayWithCard = new JButton("Pay with card");
        stylePrimaryGoldButton(btnPayWithCard);
        btnPayWithCard.addActionListener(e -> payWithSelectedSavedMethod());

        payHeroRow.add(btnPayWithStripe);
        payHeroRow.add(btnPayWithCard);
        payNowShell.add(payHeroRow);
        payNowShell.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel stripeZone = softInsetPanel(STRIPE_ZONE_BG);
        stripeZone.setLayout(new BoxLayout(stripeZone, BoxLayout.Y_AXIS));
        stripeZone.setAlignmentX(Component.LEFT_ALIGNMENT);
        stripeZone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));

        stripePayTitle = new JLabel("Stripe");
        stripePayTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        stripePayTitle.setForeground(UITheme.TEXT_DARK);
        stripePayTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        stripePayBlurb = new JLabel(" ");
        stripePayBlurb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        stripePayBlurb.setForeground(UITheme.TEXT_MEDIUM);
        stripePayBlurb.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel stripeBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        stripeBtnRow.setOpaque(false);
        stripeBtnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnConnectStripeFromBilling = new JButton("Connect Stripe in Account Center");
        styleSecondaryButton(btnConnectStripeFromBilling);
        btnConnectStripeFromBilling.addActionListener(e -> navigateToAccountCenterPayment(false));

        stripeBtnRow.add(btnConnectStripeFromBilling);

        stripeZone.add(stripePayTitle);
        stripeZone.add(Box.createRigidArea(new Dimension(0, 6)));
        stripeZone.add(stripePayBlurb);
        stripeZone.add(Box.createRigidArea(new Dimension(0, 14)));
        stripeZone.add(stripeBtnRow);

        payNowShell.add(stripeZone);
        payNowShell.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel savedZone = softInsetPanel(SAVED_ZONE_BG);
        savedZone.setLayout(new BoxLayout(savedZone, BoxLayout.Y_AXIS));
        savedZone.setAlignmentX(Component.LEFT_ALIGNMENT);
        savedZone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));

        JLabel savedTitle = new JLabel("Pay with a saved card");
        savedTitle.setFont(new Font("SansSerif", Font.BOLD, 17));
        savedTitle.setForeground(UITheme.TEXT_DARK);
        savedTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        savedPayBlurb = new JLabel(" ");
        savedPayBlurb.setFont(new Font("SansSerif", Font.PLAIN, 13));
        savedPayBlurb.setForeground(UITheme.TEXT_MEDIUM);
        savedPayBlurb.setAlignmentX(Component.LEFT_ALIGNMENT);

        savedMethodCombo = new JComboBox<>();
        savedMethodCombo.setFont(UITheme.INPUT_FONT);
        savedMethodCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        savedMethodCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel savedBtnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        savedBtnRow.setOpaque(false);
        savedBtnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnAddPaymentMethod = new JButton("Add or manage cards");
        styleSecondaryButton(btnAddPaymentMethod);
        btnAddPaymentMethod.addActionListener(e -> navigateToAccountCenterPayment(true));

        savedBtnRow.add(btnAddPaymentMethod);

        savedZone.add(savedTitle);
        savedZone.add(Box.createRigidArea(new Dimension(0, 6)));
        savedZone.add(savedPayBlurb);
        savedZone.add(Box.createRigidArea(new Dimension(0, 10)));
        savedZone.add(savedMethodCombo);
        savedZone.add(Box.createRigidArea(new Dimension(0, 12)));
        savedZone.add(savedBtnRow);

        payNowShell.add(savedZone);
        body.add(payNowShell);

        body.add(Box.createRigidArea(new Dimension(0, 22)));

        JPanel historyShell = new JPanel();
        historyShell.setLayout(new BoxLayout(historyShell, BoxLayout.Y_AXIS));
        historyShell.setOpaque(true);
        historyShell.setBackground(UITheme.CARD_BACKGROUND);
        historyShell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));
        historyShell.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyShell.setMaximumSize(new Dimension(Integer.MAX_VALUE, 420));

        JLabel histTitle = new JLabel("Payment history & receipts");
        histTitle.setFont(new Font("SansSerif", Font.BOLD, 18));
        histTitle.setForeground(UITheme.TEXT_DARK);
        histTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel histSub = new JLabel("Every payment appears here. Open a receipt to review details.");
        histSub.setFont(new Font("SansSerif", Font.PLAIN, 13));
        histSub.setForeground(UITheme.TEXT_MEDIUM);
        histSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        paymentHistoryInner = new JPanel();
        paymentHistoryInner.setLayout(new BoxLayout(paymentHistoryInner, BoxLayout.Y_AXIS));
        paymentHistoryInner.setOpaque(true);
        paymentHistoryInner.setBackground(HISTORY_ZONE_BG);
        paymentHistoryInner.setBorder(new EmptyBorder(12, 12, 12, 12));
        paymentHistoryInner.setAlignmentX(Component.LEFT_ALIGNMENT);

        JScrollPane historyScroll = new JScrollPane(paymentHistoryInner);
        historyScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 228)));
        historyScroll.getViewport().setBackground(HISTORY_ZONE_BG);
        historyScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        historyScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        historyShell.add(histTitle);
        historyShell.add(Box.createRigidArea(new Dimension(0, 4)));
        historyShell.add(histSub);
        historyShell.add(Box.createRigidArea(new Dimension(0, 12)));
        historyShell.add(historyScroll);

        body.add(historyShell);

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private static JPanel softInsetPanel(Color bg) {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(bg);
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(220, 220, 230), 1, true),
                new EmptyBorder(14, 16, 16, 16)
        ));
        return p;
    }

    public void refresh() {
        reservationsContainer.removeAll();
        shopContainer.removeAll();

        Account current = AccountController.currentAccount;
        if (current == null) {
            reservationsContainer.add(createEmptyCard("No guest is logged in."));
            shopContainer.add(createEmptyCard("No guest is logged in."));
            reservationTotalLabel.setText("Reservation Total: $0.00");
            shopTotalLabel.setText("Shop Total: $0.00");
            grandTotalLabel.setText("Grand Total: $0.00");
            paymentsAppliedLabel.setText("Payments applied: $0.00");
            amountDueLabel.setText("Amount due: $0.00");
            updatePayNowForSignedOut();
            rebuildPaymentHistory(null);
            repaintAndRevalidate();
            return;
        }

        refreshForEmail(current.getEmail());
    }

    private void styleSecondaryButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setPreferredSize(new Dimension(160, 40));
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(190, 184, 172), 1, true),
                new EmptyBorder(8, 14, 8, 14)));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void stylePrimaryGoldButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setPreferredSize(new Dimension(220, 44));
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(88, 68, 36), 1, true),
                new EmptyBorder(10, 16, 10, 16)));
        button.setBorderPainted(true);
        applyPrimaryGoldButtonColors(button, button.isEnabled());
        button.addPropertyChangeListener("enabled", e ->
                applyPrimaryGoldButtonColors(button, (Boolean) e.getNewValue()));
    }

    private static void applyPrimaryGoldButtonColors(JButton button, boolean enabled) {
        if (enabled) {
            button.setBackground(UITheme.ACCENT_GOLD);
            button.setForeground(Color.BLACK);
        } else {
            button.setBackground(new Color(218, 212, 200));
            button.setForeground(new Color(105, 98, 88));
        }
    }

    /** Prominent link-style control under the billing totals (navigates to Account Center Payment). */
    private static void styleTotalsPaymentLinkButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBackground(new Color(44, 108, 72));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(28, 78, 52), 1, true),
                new EmptyBorder(10, 18, 10, 18)));
        button.setBorderPainted(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    public void refreshForEmail(String email) {
        reservationsContainer.removeAll();
        shopContainer.removeAll();

        List<Reservation> reservations = billingService.getReservationCharges(email);
        if (reservations.isEmpty()) {
            reservationsContainer.add(createEmptyCard("No reservations found for " + email));
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
        double paymentsApplied = guestLedgerService.getPaymentsApplied(email);
        double amountDue = guestLedgerService.getAmountDue(email);

        reservationTotalLabel.setText(String.format("Reservation Total: $%d.00", reservationTotal));
        shopTotalLabel.setText(String.format("Shop Total: $%.2f", shopTotal));
        grandTotalLabel.setText(String.format("Grand Total: $%.2f", grandTotal));
        paymentsAppliedLabel.setText(String.format("Payments applied (this app): $%.2f", paymentsApplied));
        amountDueLabel.setText(String.format("Amount due: $%.2f", amountDue));

        updatePayNowUi(email, amountDue);
        rebuildPaymentHistory(email);
        repaintAndRevalidate();
    }

    private void updatePayNowForSignedOut() {
        stripePayTitle.setText("Stripe");
        stripePayBlurb.setText("<html><div style=\"width:600px\">Sign in as a guest to see Stripe and saved-card options.</div></html>");
        btnPayWithStripe.setEnabled(false);
        btnConnectStripeFromBilling.setEnabled(false);
        btnConnectStripeFromBilling.setText("Connect Stripe in Account Center");
        savedPayBlurb.setText(" ");
        savedMethodCombo.removeAllItems();
        savedMethodCombo.setEnabled(false);
        btnPayWithCard.setEnabled(false);
        btnAddPaymentMethod.setEnabled(false);
    }

    private void updatePayNowUi(String email, double amountDue) {
        boolean hasSecret = StripeConfig.hasSecretKey();
        boolean hasClient = StripeConfig.hasConnectClientId();
        boolean connected = StripeGuestPreferences.isStripeAccountConnected(email);
        boolean stripeReady = hasSecret && hasClient && connected;

        if (!hasSecret) {
            stripePayBlurb.setText("<html><div style=\"width:600px\"><b>Stripe is not configured</b> on this computer. "
                    + "Add <code>STRIPE_SECRET_KEY</code> to <code>.env</code> and restart.</div></html>");
        } else if (!hasClient) {
            stripePayBlurb.setText("<html><div style=\"width:600px\"><b>Stripe Connect is not configured.</b> "
                    + "Add <code>STRIPE_CONNECT_CLIENT_ID</code> to <code>.env</code>, register the OAuth redirect URI, then restart.</div></html>");
        } else if (!connected) {
            stripePayBlurb.setText("<html><div style=\"width:600px\">Link your Stripe account once so we can open Checkout for the "
                    + "<b>exact amount still due</b> in this app. Use the button on the right to jump to "
                    + "<b>Account Center → Payment</b>.</div></html>");
        } else {
            stripePayBlurb.setText("<html><div style=\"width:600px\">We will open Stripe Checkout for <b>$"
                    + String.format(Locale.US, "%.2f", amountDue) + "</b> — matching your current amount due "
                    + "(after any simulated saved-card payments).</div></html>");
        }

        btnPayWithStripe.setEnabled(stripeReady && amountDue > 0.009);
        btnConnectStripeFromBilling.setEnabled(true);
        if (connected) {
            btnConnectStripeFromBilling.setText("Open Payment settings");
        } else {
            btnConnectStripeFromBilling.setText("Connect Stripe in Account Center");
        }

        if (stripeReady && amountDue <= 0.009) {
            btnPayWithStripe.setToolTipText("You are caught up — nothing to pay.");
        } else if (!stripeReady) {
            btnPayWithStripe.setToolTipText("Connect Stripe from Account Center first.");
        } else {
            btnPayWithStripe.setToolTipText("Opens Stripe Checkout in your browser (test mode).");
        }

        List<SavedPaymentMethod> methods = savedPaymentMethodService.listForGuest(email);
        savedMethodCombo.removeAllItems();
        for (SavedPaymentMethod m : methods) {
            savedMethodCombo.addItem(m);
        }
        boolean hasSaved = !methods.isEmpty();
        savedMethodCombo.setEnabled(hasSaved);
        btnPayWithCard.setEnabled(hasSaved && amountDue > 0.009);
        if (savedMethodCombo.getItemCount() > 0) {
            savedMethodCombo.setSelectedIndex(0);
        }
        if (!hasSaved) {
            btnPayWithCard.setToolTipText("Add a saved card in Account Center → Payment first.");
        } else if (amountDue <= 0.009) {
            btnPayWithCard.setToolTipText("Nothing due right now.");
        } else {
            btnPayWithCard.setToolTipText("Records a simulated payment for the full amount due (demo — no bank charge).");
        }
        btnAddPaymentMethod.setEnabled(true);

        if (!hasSaved) {
            savedPayBlurb.setText("<html><div style=\"width:600px\">No saved cards yet. Add one from Account Center — we only store "
                    + "brand and last four digits.</div></html>");
        } else if (amountDue <= 0.009) {
            savedPayBlurb.setText("<html><div style=\"width:600px\">You are all paid up in this demo ledger. Saved cards remain on file for next time.</div></html>");
        } else {
            savedPayBlurb.setText("<html><div style=\"width:600px\">This demo <b>does not charge</b> your bank. We simply record a payment "
                    + "and reduce your amount due so you can open a receipt below.</div></html>");
        }
    }

    private void rebuildPaymentHistory(String email) {
        paymentHistoryInner.removeAll();
        if (email == null || email.isBlank()) {
            paymentHistoryInner.add(historyHint("Sign in to see receipts."));
            paymentHistoryInner.revalidate();
            paymentHistoryInner.repaint();
            return;
        }
        List<GuestPaymentRecord> rows = guestLedgerService.listPayments(email);
        if (rows.isEmpty()) {
            paymentHistoryInner.add(historyHint("No payments recorded yet."));
        } else {
            for (GuestPaymentRecord r : rows) {
                paymentHistoryInner.add(buildHistoryRow(r));
                paymentHistoryInner.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        paymentHistoryInner.revalidate();
        paymentHistoryInner.repaint();
    }

    private JLabel historyHint(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 14));
        l.setForeground(UITheme.TEXT_MEDIUM);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private JPanel buildHistoryRow(GuestPaymentRecord r) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(true);
        row.setBackground(Color.WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 230, 238), 1, true),
                new EmptyBorder(10, 12, 10, 12)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 56));

        String when = r.getCreatedAt().format(RECEIPT_TIME);
        JLabel left = new JLabel("<html><div style=\"font-size:13px;\"><b>$" + String.format(Locale.US, "%.2f", r.getAmount())
                + "</b> &nbsp;·&nbsp; " + esc(r.getKind().getDisplay()) + "<br/><span style=\"color:#666;\">"
                + esc(r.getMethodSummary()) + " · " + esc(when) + "</span></div></html>");
        JButton receipt = new JButton("Receipt");
        styleSecondaryButton(receipt);
        receipt.setPreferredSize(new Dimension(100, 34));
        receipt.addActionListener(_ -> showPaymentReceipt(r));

        row.add(left, BorderLayout.CENTER);
        row.add(receipt, BorderLayout.EAST);
        return row;
    }

    private static String esc(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private void showPaymentReceipt(GuestPaymentRecord r) {
        String body = "Receipt #" + r.getId() + "\n"
                + r.getCreatedAt().format(RECEIPT_TIME) + "\n\n"
                + "Amount: $" + String.format(Locale.US, "%.2f", r.getAmount()) + "\n"
                + "Type: " + r.getKind().getDisplay() + "\n"
                + "Method: " + r.getMethodSummary();
        JOptionPane.showMessageDialog(this, body, "Payment receipt", JOptionPane.INFORMATION_MESSAGE);
    }

    private void navigateToAccountCenterPayment(boolean expandAddCardForm) {
        Container parent = getParent();
        if (!(parent != null && parent.getLayout() instanceof CardLayout cl)) {
            JOptionPane.showMessageDialog(this,
                    "Open Account Center from your profile menu to manage Stripe and saved cards.",
                    "Account Center",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (Main.accountCenterPage != null) {
            Main.accountCenterPage.refreshInfo();
            Main.accountCenterPage.navigateToPaymentTab(expandAddCardForm);
        }
        cl.show(parent, "account center");
    }

    private void payWithSelectedSavedMethod() {
        Account current = AccountController.currentAccount;
        if (current == null) {
            return;
        }
        SavedPaymentMethod method = (SavedPaymentMethod) savedMethodCombo.getSelectedItem();
        if (method == null) {
            JOptionPane.showMessageDialog(this, "Choose a saved card first.", "Pay with saved card",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String email = current.getEmail();
        double due = guestLedgerService.getAmountDue(email);
        if (due <= 0.009) {
            JOptionPane.showMessageDialog(this, "There is no amount due.", "Pay with saved card",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Record a payment of $" + String.format(Locale.US, "%.2f", due) + " using:\n" + method.getDisplayLabel()
                        + "\n\nNo real charge will be made in this demo.",
                "Confirm simulated payment",
                JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            guestLedgerService.recordSimulatedSavedCardPayment(email, method, due);
            JOptionPane.showMessageDialog(this,
                    "Recorded $" + String.format(Locale.US, "%.2f", due) + " as paid.\n"
                            + "You can open the new receipt in Payment history below.",
                    "Payment recorded",
                    JOptionPane.INFORMATION_MESSAGE);
            refreshForEmail(email);
            if (Main.accountCenterPage != null) {
                Main.accountCenterPage.refreshPaymentWorkspace();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not record payment: " + ex.getMessage(),
                    "Payment error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(UITheme.TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createTotalLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(UITheme.TEXT_DARK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createGrandTotalLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(new Color(176, 132, 38));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createMutedTotalLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(UITheme.TEXT_MEDIUM);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createDueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(new Color(140, 70, 40));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JPanel createReservationCard(Reservation reservation) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean isCancelled = "CANCELLED".equalsIgnoreCase(reservation.getStatus());

        String roomText = reservation.getRooms().stream()
                .map(r -> "Room " + r.getRoomNumber())
                .reduce((a, b) -> a + ", " + b)
                .orElse("No rooms");

        JLabel top = new JLabel(isCancelled ? "CANCELLED: " + roomText : roomText);
        top.setFont(new Font("SansSerif", Font.BOLD, 18));
        top.setForeground(isCancelled ? Color.RED : UITheme.TEXT_DARK);

        JLabel dates = new JLabel("Dates: " + reservation.getStartDate() + " to " + reservation.getEndDate());
        dates.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dates.setForeground(UITheme.TEXT_MEDIUM);

        card.add(top);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(dates);

        if (!isCancelled) {
            JLabel nightly = new JLabel("Nightly Rate: $" + reservation.getNightlyRate() + ".00");
            nightly.setFont(new Font("SansSerif", Font.PLAIN, 14));
            nightly.setForeground(UITheme.TEXT_MEDIUM);

            JLabel nights = new JLabel("Nights: " + reservation.getNights());
            nights.setFont(new Font("SansSerif", Font.PLAIN, 14));
            nights.setForeground(UITheme.TEXT_MEDIUM);

            card.add(Box.createRigidArea(new Dimension(0, 4)));
            card.add(nightly);
            card.add(Box.createRigidArea(new Dimension(0, 4)));
            card.add(nights);
        }

        JLabel total = new JLabel();
        if (isCancelled) {
            total.setText("Cancellation Penalty Fee: $" + reservation.getTotalCost() + ".00");
            total.setForeground(Color.RED);
        } else {
            total.setText("Reservation Total: $" + reservation.getTotalCost() + ".00");
            total.setForeground(new Color(44, 122, 72));
        }
        total.setFont(new Font("SansSerif", Font.BOLD, 16));

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(total);

        return card;
    }

    private JPanel createShopOrderCard(ShopOrder order) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(16, 16, 16, 16)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel top = new JLabel("Purchase Date: " + order.getPurchaseDate());
        top.setFont(new Font("SansSerif", Font.BOLD, 18));
        top.setForeground(UITheme.TEXT_DARK);
        card.add(top);
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        for (ShopOrderItem item : order.getItems()) {
            JLabel line = new JLabel(String.format("%s | Qty: %d | $%.2f each | Line Total: $%.2f",
                    item.getItemName(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal()));
            line.setFont(new Font("SansSerif", Font.PLAIN, 14));
            line.setForeground(UITheme.TEXT_MEDIUM);
            card.add(line);
            card.add(Box.createRigidArea(new Dimension(0, 4)));
        }

        JLabel total = new JLabel(String.format("Order Total: $%.2f", order.getTotalCost()));
        total.setFont(new Font("SansSerif", Font.BOLD, 16));
        total.setForeground(new Color(44, 122, 72));
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(total);

        return card;
    }

    private JPanel createEmptyCard(String text) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(UITheme.TEXT_MEDIUM);
        card.add(label, BorderLayout.CENTER);
        return card;
    }

    private void repaintAndRevalidate() {
        reservationsContainer.revalidate();
        reservationsContainer.repaint();
        shopContainer.revalidate();
        shopContainer.repaint();
        paymentHistoryInner.revalidate();
        paymentHistoryInner.repaint();
        revalidate();
        repaint();
    }

    private boolean isStripeConnectReady(String email) {
        return StripeConfig.hasSecretKey()
                && StripeConfig.hasConnectClientId()
                && StripeGuestPreferences.isStripeAccountConnected(email);
    }

    private void startStripeSandboxCheckout() {
        Account current = AccountController.currentAccount;
        if (current == null) {
            JOptionPane.showMessageDialog(this,
                    "Please log in as a guest to pay your bill.", "Stripe checkout", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String email = current.getEmail();
        if (!isStripeConnectReady(email)) {
            JOptionPane.showMessageDialog(this,
                    "Connect Stripe from Account Center → Payment before using Checkout.",
                    "Stripe checkout", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (!StripeConfig.hasSecretKey()) {
            JOptionPane.showMessageDialog(this,
                    "Stripe is not configured. Add STRIPE_SECRET_KEY (test key) to your .env before using sandbox Checkout.",
                    "Stripe checkout", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double due = guestLedgerService.getAmountDue(email);
        if (due <= 0.009) {
            JOptionPane.showMessageDialog(this,
                    "There is no balance due right now.", "Stripe checkout", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        new SwingWorker<Void, Void>() {
            private volatile String fatal;

            @Override
            protected Void doInBackground() {
                StripeHostedLocalServer.BindHandle binder = null;
                try {
                    binder = StripeHostedLocalServer.bindCheckout(
                            sid -> SwingUtilities.invokeLater(() -> handleStripeHostedReturn(email, sid)),
                            () -> SwingUtilities.invokeLater(() ->
                                    JOptionPane.showMessageDialog(BillingPage.this,
                                            "Stripe Checkout was canceled. No payment was finalized.",
                                            "Stripe checkout",
                                            JOptionPane.INFORMATION_MESSAGE))
                    );

                    StripeCheckoutService.SessionCreateResult session = stripeCheckoutService.createCheckoutSessionPayAmountDue(
                            email.trim().toLowerCase(Locale.ROOT),
                            due,
                            StripeConfig.checkoutSuccessUrlTemplateWithSessionMacro(),
                            StripeConfig.checkoutCancelUrl());

                    if (!session.success()) {
                        fatal = session.message();
                        binder.stopQuietly();
                        return null;
                    }
                    boolean opened = StripeHostedLocalServer.browse(session.url());
                    if (!opened) {
                        fatal = "Unable to launch a browser.";
                    }
                    if (!opened && binder != null) {
                        binder.stopQuietly();
                    }
                    return null;
                } catch (StripeException | IOException ex) {
                    fatal = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                    if (binder != null) {
                        binder.stopQuietly();
                    }
                    return null;
                }
            }

            @Override
            protected void done() {
                if (fatal != null && !fatal.isBlank()) {
                    JOptionPane.showMessageDialog(BillingPage.this,
                            fatal, "Stripe checkout", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void handleStripeHostedReturn(String guestEmail, String checkoutSessionId) {
        try {
            StripeCheckoutSessionReader.StripeCheckoutSummary snapshot =
                    StripeCheckoutSessionReader.read(checkoutSessionId);
            String customer = snapshot.stripeCustomerId();
            if (customer != null && !customer.isBlank()) {
                StripeGuestPreferences.setStripeCustomerId(guestEmail, customer);
            }
            if (snapshot.suggestsPaymentSucceeded()) {
                double usd = snapshot.amountTotalUsdCents() / 100.0;
                try {
                    guestLedgerService.recordStripeCheckoutPayment(
                            guestEmail,
                            usd,
                            "Stripe Checkout session " + checkoutSessionId);
                    JOptionPane.showMessageDialog(this,
                            "Stripe reports the session as paid. We recorded $" + String.format(Locale.US, "%.2f", usd)
                                    + " on your account. Open Payment history for a receipt.",
                            "Payment recorded",
                            JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Stripe paid, but we could not save the ledger entry:\n" + ex.getMessage(),
                            "Ledger error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Stripe returned to the app, but the session is not marked paid yet.\n"
                                + "If you finished paying, check Stripe Dashboard (test mode) or try again.",
                        "Stripe checkout",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (StripeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not refresh session details:\n" + ex.getMessage(),
                    "Stripe checkout",
                    JOptionPane.WARNING_MESSAGE);
        }
        refresh();
        if (Main.accountCenterPage != null) {
            Main.accountCenterPage.refreshPaymentWorkspace();
        }
    }

    /** Same as clicking Pay with Stripe on this page (used from Account Center quick actions). */
    public void triggerPayWithStripe() {
        startStripeSandboxCheckout();
    }

    /** Same as clicking Pay with card on this page (uses the selected saved card in the combo). */
    public void triggerPayWithCard() {
        payWithSelectedSavedMethod();
    }
}
