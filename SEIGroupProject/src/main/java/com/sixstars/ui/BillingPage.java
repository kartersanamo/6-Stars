package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Reservation;
import com.sixstars.model.ShopOrder;
import com.sixstars.model.ShopOrderItem;
import com.sixstars.service.BillingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class BillingPage extends JPanel {

    private final BillingService billingService;
    private final JPanel reservationsContainer;
    private final JPanel shopContainer;
    private final JLabel reservationTotalLabel;
    private final JLabel shopTotalLabel;
    private final JLabel grandTotalLabel;

    public BillingPage() {
        billingService = new BillingService();

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        JLabel title = new JLabel("Guest Billing");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Reservations, shop purchases, and your total charges");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(UITheme.CARD_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(18, 26, 18, 26)
        ));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createRigidArea(new Dimension(0, 4)));
        header.add(subtitle);

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

        reservationTotalLabel = createTotalLabel("");
        shopTotalLabel = createTotalLabel("");
        grandTotalLabel = createGrandTotalLabel("");

        totalsCard.add(reservationTotalLabel);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 8)));
        totalsCard.add(shopTotalLabel);
        totalsCard.add(Box.createRigidArea(new Dimension(0, 14)));
        totalsCard.add(grandTotalLabel);

        body.add(totalsCard);

        JScrollPane scrollPane = new JScrollPane(body);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
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
            repaintAndRevalidate();
            return;
        }

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

        reservationTotalLabel.setText(String.format("Reservation Total: $%d.00", reservationTotal));
        shopTotalLabel.setText(String.format("Shop Total: $%.2f", shopTotal));
        grandTotalLabel.setText(String.format("Grand Total: $%.2f", grandTotal));

        repaintAndRevalidate();
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(UITheme.TEXT_DARK);
        return label;
    }

    private JLabel createTotalLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 18));
        label.setForeground(UITheme.TEXT_DARK);
        return label;
    }

    private JLabel createGrandTotalLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 22));
        label.setForeground(new Color(176, 132, 38));
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

        String roomText = reservation.getRooms().stream()
                .map(r -> "Room " + r.getRoomNumber() + " - " + r.getBedType())
                .reduce((a, b) -> a + ", " + b)
                .orElse("No rooms");

        JLabel top = new JLabel(roomText);
        top.setFont(new Font("SansSerif", Font.BOLD, 18));
        top.setForeground(UITheme.TEXT_DARK);

        JLabel dates = new JLabel("Dates: " + reservation.getStartDate() + " to " + reservation.getEndDate());
        dates.setFont(new Font("SansSerif", Font.PLAIN, 14));
        dates.setForeground(UITheme.TEXT_MEDIUM);

        JLabel nightly = new JLabel("Nightly Rate: $" + reservation.getNightlyRate() + ".00");
        nightly.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nightly.setForeground(UITheme.TEXT_MEDIUM);

        JLabel nights = new JLabel("Nights: " + reservation.getNights());
        nights.setFont(new Font("SansSerif", Font.PLAIN, 14));
        nights.setForeground(UITheme.TEXT_MEDIUM);

        JLabel total = new JLabel("Reservation Total: $" + reservation.getTotalCost() + ".00");
        total.setFont(new Font("SansSerif", Font.BOLD, 16));
        total.setForeground(new Color(44, 122, 72));

        card.add(top);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(dates);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(nightly);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(nights);
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
        revalidate();
        repaint();
    }
}