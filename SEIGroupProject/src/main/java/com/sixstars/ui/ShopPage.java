package com.sixstars.ui;

import com.sixstars.model.*;
import com.sixstars.service.ShopService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ShopPage extends JPanel {

    private ShoppingCart cart = new ShoppingCart();
    private ShopService shopService = new ShopService();

    private DefaultListModel<String> cartModel = new DefaultListModel<>();
    private JLabel totalLabel = new JLabel("Total: $0.00");

    private JPanel inventoryPanel = new JPanel();

    public ShopPage(JPanel pages, CardLayout cardLayout) {

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // ===== TOP =====
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(new EmptyBorder(15, 20, 10, 20));

        JButton back = new JButton("← Back");
        back.addActionListener(e -> cardLayout.show(pages, "menu page"));

        JLabel title = new JLabel("Hotel Shop", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));

        top.add(back, BorderLayout.WEST);
        top.add(title, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);

        // ===== INVENTORY GRID =====
        inventoryPanel.setLayout(new GridLayout(0, 3, 15, 15));
        inventoryPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        inventoryPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(inventoryPanel);

        // ===== CART =====
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setPreferredSize(new Dimension(300, 0));

        JList<String> cartList = new JList<>(cartModel);

        JButton checkout = new JButton("Checkout");
        checkout.addActionListener(e -> handleCheckout());

        cartPanel.add(new JLabel("Cart", SwingConstants.CENTER), BorderLayout.NORTH);
        cartPanel.add(new JScrollPane(cartList), BorderLayout.CENTER);
        cartPanel.add(totalLabel, BorderLayout.SOUTH);
        cartPanel.add(checkout, BorderLayout.PAGE_END);

        add(scroll, BorderLayout.CENTER);
        add(cartPanel, BorderLayout.EAST);

        refreshInventory();
    }

    private void refreshInventory() {
        inventoryPanel.removeAll();

        List<Item> items = shopService.getInventory();

        for (Item item : items) {
            inventoryPanel.add(createItemCard(item));
        }

        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }

    private JPanel createItemCard(Item item) {

        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // ===== IMAGE =====
        JLabel imageLabel;

        File file = new File(item.getImagePath());
        if (file.exists()) {
            ImageIcon icon = new ImageIcon(item.getImagePath());
            Image scaled = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            imageLabel = new JLabel(new ImageIcon(scaled));
        } else {
            imageLabel = new JLabel("No Image", SwingConstants.CENTER);
        }

        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // ===== INFO =====
        JLabel name = new JLabel(item.getName(), SwingConstants.CENTER);
        JLabel price = new JLabel("$" + item.getPrice(), SwingConstants.CENTER);
        JLabel stock = new JLabel(item.getStock() + " left", SwingConstants.CENTER);

        JButton add = new JButton("Add");
        add.addActionListener(e -> {
            cart.addItem(item);
            updateCart();
        });

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(name);
        info.add(price);
        info.add(stock);
        info.add(add);

        card.add(imageLabel, BorderLayout.CENTER);
        card.add(info, BorderLayout.SOUTH);

        return card;
    }

    private void updateCart() {
        cartModel.clear();

        for (CartItem ci : cart.getItems()) {
            cartModel.addElement(ci.getItem().getName() + " x" + ci.getQuantity());
        }

        totalLabel.setText("Total: $" + String.format("%.2f", cart.getTotal()));
    }

    private void handleCheckout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty");
            return;
        }

        double total = shopService.checkout(cart);

        JOptionPane.showMessageDialog(this, "Purchased! $" + total);

        updateCart();
        refreshInventory();
    }
}