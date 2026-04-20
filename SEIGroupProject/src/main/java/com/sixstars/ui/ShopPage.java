package com.sixstars.ui;

import com.sixstars.model.CartItem;
import com.sixstars.model.Item;
import com.sixstars.model.ShoppingCart;
import com.sixstars.service.ShopService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ShopPage extends JPanel {

    private final ShoppingCart cart;
    private final ShopService shopService;

    private final DefaultListModel<String> cartModel;
    private final JLabel totalLabel;
    private final JPanel inventoryPanel;
    private final JPanel pages;
    private final CardLayout cardLayout;

    public ShopPage(JPanel pages, CardLayout cardLayout) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.cart = new ShoppingCart();
        this.shopService = new ShopService();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        JButton backButton = new JButton("← Back to Menu");
        backButton.addActionListener(e -> cardLayout.show(pages, "menu page"));
        topPanel.add(backButton);

        add(topPanel, BorderLayout.NORTH);

        inventoryPanel = new JPanel();
        inventoryPanel.setLayout(new BoxLayout(inventoryPanel, BoxLayout.Y_AXIS));
        inventoryPanel.setBorder(BorderFactory.createTitledBorder("Shop Items"));

        JScrollPane inventoryScroll = new JScrollPane(inventoryPanel);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Your Cart"));

        cartModel = new DefaultListModel<>();
        JList<String> cartList = new JList<>(cartModel);

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.addActionListener(e -> handleCheckout());

        rightPanel.add(totalLabel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);
        rightPanel.add(checkoutButton, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inventoryScroll, rightPanel);
        splitPane.setDividerLocation(500);

        add(splitPane, BorderLayout.CENTER);

        refreshInventory();
    }

    public void refreshInventory() {
        inventoryPanel.removeAll();

        List<Item> items = shopService.getInventory();

        for (Item item : items) {
            JButton itemButton = new JButton(
                    item.getName() + " - $" + String.format("%.2f", item.getPrice()) +
                            " (" + item.getStock() + " left)"
            );

            itemButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            itemButton.addActionListener(e -> {
                cart.addItem(item);
                updateCartDisplay();
            });

            inventoryPanel.add(itemButton);
            inventoryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }

    private void updateCartDisplay() {
        cartModel.clear();

        for (CartItem cartItem : cart.getItems()) {
            cartModel.addElement(
                    cartItem.getItem().getName() + " x" + cartItem.getQuantity() +
                            " - $" + String.format("%.2f", cartItem.getTotalPrice())
            );
        }

        totalLabel.setText("Total: $" + String.format("%.2f", cart.getTotal()));
    }

    private void handleCheckout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }

        try {
            double total = shopService.checkout(cart);
            JOptionPane.showMessageDialog(this,
                    "Purchase successful.\nTotal: $" + String.format("%.2f", total));

            updateCartDisplay();
            refreshInventory();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}