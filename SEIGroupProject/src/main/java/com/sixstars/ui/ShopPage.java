package com.sixstars.ui;

import com.sixstars.model.CartItem;
import com.sixstars.model.Item;
import com.sixstars.model.ShoppingCart;
import com.sixstars.service.ShopService;

import javax.swing.*;
import java.awt.*;

public class ShopPage extends JPanel {

    private ShoppingCart cart;
    private ShopService shopService;

    private DefaultListModel<String> cartModel;
    private JLabel totalLabel;

    public ShopPage(JPanel pages, CardLayout cardLayout) {
        this.cart = new ShoppingCart();
        this.shopService = new ShopService();

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===== TOP PANEL (Back Button) =====
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        JButton backButton = new JButton("← Back to Menu");
        backButton.addActionListener(e -> cardLayout.show(pages, "menu page"));

        topPanel.add(backButton);
        add(topPanel, BorderLayout.NORTH);

        // ===== LEFT PANEL (SHOP ITEMS) =====
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Shop Items"));

        for (Item item : shopService.getInventory()) {
            JButton btn = new JButton(item.getName() + " - $" + item.getPrice());
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);

            btn.addActionListener(e -> {
                cart.addItem(item);
                updateCartDisplay();
            });

            leftPanel.add(btn);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        // ===== RIGHT PANEL (CART) =====
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Your Cart"));

        cartModel = new DefaultListModel<>();
        JList<String> cartList = new JList<>(cartModel);

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.addActionListener(e -> {
            if (cart.getItems().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Cart is empty.");
                return;
            }

            double total = shopService.checkout(cart);
            JOptionPane.showMessageDialog(this, "Purchase successful! Total: $" + total);
            updateCartDisplay();
        });

        rightPanel.add(totalLabel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);
        rightPanel.add(checkoutBtn, BorderLayout.SOUTH);

        // ===== MAIN SPLIT =====
        add(leftPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);
    }

    private void updateCartDisplay() {
        cartModel.clear();

        for (CartItem ci : cart.getItems()) {
            cartModel.addElement(
                    ci.getItem().getName() + " x" + ci.getQuantity()
            );
        }

        totalLabel.setText("Total: $" + String.format("%.2f", cart.getTotal()));
    }
}