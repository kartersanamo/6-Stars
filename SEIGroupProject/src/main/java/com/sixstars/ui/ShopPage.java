package com.sixstars.ui;

import com.sixstars.model.*;
import com.sixstars.service.ShopService;

import javax.swing.*;
import java.awt.*;

public class ShopPage extends JPanel {

    private ShoppingCart cart;
    private ShopService shopService;

    private DefaultListModel<String> cartModel;
    private JLabel totalLabel;

    public ShopPage() {
        this.cart = new ShoppingCart();
        this.shopService = new ShopService();

        setLayout(new BorderLayout());

        // LEFT: Inventory
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        for (Item item : shopService.getInventory()) {
            JButton btn = new JButton(item.getName() + " - $" + item.getPrice());
            btn.addActionListener(e -> {
                cart.addItem(item);
                updateCartDisplay();
            });
            leftPanel.add(btn);
        }

        // RIGHT: Cart
        JPanel rightPanel = new JPanel(new BorderLayout());

        cartModel = new DefaultListModel<>();
        JList<String> cartList = new JList<>(cartModel);

        totalLabel = new JLabel("Total: $0.00");

        JButton checkoutBtn = new JButton("Checkout");
        checkoutBtn.addActionListener(e -> {
            double total = shopService.checkout(cart);
            JOptionPane.showMessageDialog(this, "Purchased! Total: $" + total);
            updateCartDisplay();
        });

        rightPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);
        rightPanel.add(totalLabel, BorderLayout.NORTH);
        rightPanel.add(checkoutBtn, BorderLayout.SOUTH);

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
        totalLabel.setText("Total: $" + cart.getTotal());
    }
}