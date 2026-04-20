package com.sixstars.ui;

import com.sixstars.model.CartItem;
import com.sixstars.model.Item;
import com.sixstars.model.ShoppingCart;
import com.sixstars.service.ShopService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;
import java.util.List;

public class ShopPage extends JPanel {

    private final JPanel pages;
    private final CardLayout cardLayout;

    private final ShoppingCart cart;
    private final ShopService shopService;

    private final JPanel inventoryPanel;
    private JPanel cartItemsPanel;
    private final JLabel totalLabel;
    private final JLabel resultsInfoLabel;
    private final JTextField searchField;

    public ShopPage(JPanel pages, CardLayout cardLayout) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.cart = new ShoppingCart();
        this.shopService = new ShopService();

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        inventoryPanel = new JPanel();
        totalLabel = new JLabel("Total: $0.00");
        resultsInfoLabel = new JLabel("0 items");
        searchField = createTextField("Search shop items");

        add(buildTopSection(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        attachSearchListener();
        refreshInventory();
        updateCartDisplay();
    }

    private JPanel buildTopSection() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.PAGE_BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.CARD_BACKGROUND);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JButton backButton = createSecondaryButton("Back to Menu");
        backButton.addActionListener(e -> cardLayout.show(pages, "menu page"));

        JLabel title = new JLabel("Hotel Shop");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Browse travel essentials, snacks, and hotel merchandise");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(title);
        titleBlock.add(Box.createRigidArea(new Dimension(0, 4)));
        titleBlock.add(subtitle);

        header.add(backButton, BorderLayout.WEST);
        header.add(titleBlock, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel(new BorderLayout());
        searchPanel.setBackground(UITheme.CARD_BACKGROUND);
        searchPanel.setBorder(new EmptyBorder(14, 24, 14, 24));

        JPanel searchCard = new JPanel();
        searchCard.setLayout(new BoxLayout(searchCard, BoxLayout.Y_AXIS));
        searchCard.setBackground(new Color(248, 248, 248));
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel searchLabel = new JLabel("Search");
        searchLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        searchLabel.setForeground(UITheme.TEXT_MEDIUM);
        searchLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchCard.add(searchLabel);
        searchCard.add(Box.createRigidArea(new Dimension(0, 6)));
        searchCard.add(searchField);

        searchPanel.add(searchCard, BorderLayout.CENTER);

        top.add(header, BorderLayout.NORTH);
        top.add(searchPanel, BorderLayout.CENTER);

        return top;
    }

    private JPanel buildMainContent() {
        JPanel content = new JPanel(new BorderLayout(18, 0));
        content.setBackground(UITheme.PAGE_BACKGROUND);
        content.setBorder(new EmptyBorder(20, 28, 20, 28));

        JPanel leftSection = new JPanel(new BorderLayout());
        leftSection.setOpaque(false);

        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setOpaque(false);

        JLabel heading = new JLabel("Available Items");
        heading.setFont(new Font("SansSerif", Font.BOLD, 22));
        heading.setForeground(UITheme.TEXT_DARK);

        resultsInfoLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        resultsInfoLabel.setForeground(UITheme.TEXT_MEDIUM);
        resultsInfoLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        infoBar.add(heading, BorderLayout.WEST);
        infoBar.add(resultsInfoLabel, BorderLayout.EAST);

        inventoryPanel.setLayout(new GridLayout(0, 3, 16, 16));
        inventoryPanel.setOpaque(false);
        inventoryPanel.setBorder(new EmptyBorder(8, 8, 8, 8));

        JScrollPane scrollPane = new JScrollPane(inventoryPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        leftSection.add(infoBar, BorderLayout.NORTH);
        leftSection.add(scrollPane, BorderLayout.CENTER);

        JPanel cartPanel = buildCartPanel();

        content.add(leftSection, BorderLayout.CENTER);
        content.add(cartPanel, BorderLayout.EAST);

        return content;
    }

    private JPanel buildCartPanel() {
        JPanel cartPanel = new JPanel(new BorderLayout());
        cartPanel.setBackground(UITheme.CARD_BACKGROUND);
        cartPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(18, 18, 18, 18)
        ));
        cartPanel.setPreferredSize(new Dimension(340, 0));

        JLabel cartTitle = new JLabel("Your Cart");
        cartTitle.setFont(new Font("Serif", Font.BOLD, 24));
        cartTitle.setForeground(UITheme.TEXT_DARK);
        cartTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel cartSubtitle = new JLabel("Review selected items before checkout");
        cartSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cartSubtitle.setForeground(UITheme.TEXT_MEDIUM);
        cartSubtitle.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(cartTitle);
        top.add(Box.createRigidArea(new Dimension(0, 4)));
        top.add(cartSubtitle);
        top.add(Box.createRigidArea(new Dimension(0, 14)));

        cartItemsPanel = new JPanel();
        cartItemsPanel.setLayout(new BoxLayout(cartItemsPanel, BoxLayout.Y_AXIS));
        cartItemsPanel.setOpaque(false);

        JScrollPane cartScroll = new JScrollPane(cartItemsPanel);
        cartScroll.setBorder(BorderFactory.createEmptyBorder());
        cartScroll.getViewport().setBackground(UITheme.CARD_BACKGROUND);
        cartScroll.getVerticalScrollBar().setUnitIncrement(16);

        totalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        totalLabel.setForeground(UITheme.TEXT_DARK);
        totalLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton checkoutButton = createPrimaryButton("Checkout");
        checkoutButton.setForeground(Color.BLACK);
        checkoutButton.addActionListener(e -> handleCheckout());

        JButton clearCartButton = createSecondaryButton("Clear Cart");
        clearCartButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        clearCartButton.addActionListener(e -> {
            cart.clear();
            updateCartDisplay();
        });

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottom.add(totalLabel);
        bottom.add(Box.createRigidArea(new Dimension(0, 12)));
        bottom.add(checkoutButton);
        bottom.add(Box.createRigidArea(new Dimension(0, 8)));
        bottom.add(clearCartButton);

        cartPanel.add(top, BorderLayout.NORTH);
        cartPanel.add(cartScroll, BorderLayout.CENTER);
        cartPanel.add(bottom, BorderLayout.SOUTH);

        return cartPanel;
    }

    private JPanel buildBottomBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.CARD_BACKGROUND);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel helperText = new JLabel("Tip: Search by item name to filter the shop instantly.");
        helperText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        helperText.setForeground(UITheme.TEXT_MEDIUM);

        footer.add(helperText, BorderLayout.WEST);
        return footer;
    }

    private void attachSearchListener() {
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshInventory();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshInventory();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshInventory();
            }
        });
    }

    public void refreshInventory() {
        inventoryPanel.removeAll();

        List<Item> items = shopService.getInventory();
        String searchText = searchField.getText().trim().toLowerCase();

        int shownCount = 0;
        for (Item item : items) {
            if (!matchesSearch(item, searchText)) {
                continue;
            }

            inventoryPanel.add(createItemCard(item));
            shownCount++;
        }

        if (shownCount == 0) {
            inventoryPanel.setLayout(new GridLayout(1, 1));
            inventoryPanel.add(createEmptyStateCard());
            resultsInfoLabel.setText("0 items");
        } else {
            inventoryPanel.setLayout(new GridLayout(0, 3, 16, 16));
            resultsInfoLabel.setText(shownCount + " item" + (shownCount == 1 ? "" : "s"));
        }

        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }

    private boolean matchesSearch(Item item, String searchText) {
        if (searchText.isEmpty()) {
            return true;
        }
        return item.getName().toLowerCase().contains(searchText);
    }

    private JPanel createItemCard(Item item) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(14, 14, 14, 14)
        ));
        card.setPreferredSize(new Dimension(250, 300));

        JLabel imageLabel = buildImageLabel(item);

        JLabel nameLabel = new JLabel(item.getName(), SwingConstants.CENTER);
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        nameLabel.setForeground(UITheme.TEXT_DARK);

        JLabel priceLabel = new JLabel("$" + String.format("%.2f", item.getPrice()), SwingConstants.CENTER);
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        priceLabel.setForeground(UITheme.TEXT_MEDIUM);

        JLabel stockLabel = new JLabel(item.getStock() + " left", SwingConstants.CENTER);
        stockLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        stockLabel.setForeground(UITheme.TEXT_MEDIUM);

        JButton addButton = createPrimaryButton("Add to Cart");
        addButton.setForeground(Color.BLACK);
        addButton.addActionListener(e -> {
            cart.addItem(item);
            updateCartDisplay();
        });

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stockLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(imageLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 6)));
        infoPanel.add(priceLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        infoPanel.add(stockLabel);
        infoPanel.add(Box.createRigidArea(new Dimension(0, 14)));
        infoPanel.add(addButton);

        card.add(infoPanel, BorderLayout.CENTER);
        return card;
    }

    private JLabel buildImageLabel(Item item) {
        JLabel label;

        File file = new File(item.getImagePath());
        if (file.exists()) {
            ImageIcon icon = new ImageIcon(item.getImagePath());
            Image scaled = icon.getImage().getScaledInstance(130, 130, Image.SCALE_SMOOTH);
            label = new JLabel(new ImageIcon(scaled));
        } else {
            label = new JLabel("No Image", SwingConstants.CENTER);
            label.setPreferredSize(new Dimension(130, 130));
            label.setOpaque(true);
            label.setBackground(new Color(245, 245, 245));
            label.setForeground(UITheme.TEXT_MEDIUM);
            label.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1));
        }

        label.setHorizontalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JPanel createCartItemRow(CartItem cartItem) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(UITheme.CARD_BACKGROUND);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(cartItem.getItem().getName() + " x" + cartItem.getQuantity());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setForeground(UITheme.TEXT_DARK);

        JLabel priceLabel = new JLabel("$" + String.format("%.2f", cartItem.getTotalPrice()));
        priceLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        priceLabel.setForeground(UITheme.TEXT_MEDIUM);

        textPanel.add(nameLabel);
        textPanel.add(Box.createRigidArea(new Dimension(0, 3)));
        textPanel.add(priceLabel);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
        buttonsPanel.setOpaque(false);

        JButton minusButton = new JButton("−");
        styleSmallButton(minusButton, UITheme.SECONDARY_BUTTON, UITheme.TEXT_DARK);
        minusButton.addActionListener(e -> {
            cart.decrementItem(cartItem.getItem());
            updateCartDisplay();
        });

        JButton removeButton = new JButton("Remove");
        styleSmallButton(removeButton, new Color(200, 80, 80), Color.WHITE);
        removeButton.addActionListener(e -> {
            cart.removeItem(cartItem.getItem());
            updateCartDisplay();
        });

        buttonsPanel.add(minusButton);
        buttonsPanel.add(Box.createRigidArea(new Dimension(6, 0)));
        buttonsPanel.add(removeButton);

        row.add(textPanel, BorderLayout.CENTER);
        row.add(buttonsPanel, BorderLayout.EAST);

        return row;
    }

    private void styleSmallButton(JButton button, Color bg, Color fg) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(85, 30));
    }

    private JPanel createEmptyStateCard() {
        JPanel empty = new JPanel(new BorderLayout());
        empty.setBackground(UITheme.CARD_BACKGROUND);
        empty.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(26, 20, 26, 20)
        ));

        JLabel text = new JLabel("No shop items match your search.");
        text.setHorizontalAlignment(SwingConstants.CENTER);
        text.setFont(new Font("SansSerif", Font.PLAIN, 16));
        text.setForeground(UITheme.TEXT_MEDIUM);
        empty.add(text, BorderLayout.CENTER);

        return empty;
    }

    private void updateCartDisplay() {
        cartItemsPanel.removeAll();

        if (cart.isEmpty()) {
            JPanel emptyCart = new JPanel(new BorderLayout());
            emptyCart.setOpaque(false);
            emptyCart.setBorder(new EmptyBorder(20, 10, 20, 10));

            JLabel emptyLabel = new JLabel("Your cart is empty.");
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            emptyLabel.setForeground(UITheme.TEXT_MEDIUM);

            emptyCart.add(emptyLabel, BorderLayout.CENTER);
            cartItemsPanel.add(emptyCart);
        } else {
            for (CartItem cartItem : cart.getItems()) {
                cartItemsPanel.add(createCartItemRow(cartItem));
                cartItemsPanel.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }

        totalLabel.setText("Total: $" + String.format("%.2f", cart.getTotal()));

        cartItemsPanel.revalidate();
        cartItemsPanel.repaint();
    }

    private void handleCheckout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your cart is empty.");
            return;
        }

        try {
            double total = shopService.checkout(cart);
            JOptionPane.showMessageDialog(
                    this,
                    "Purchase successful.\nTotal: $" + String.format("%.2f", total),
                    "Checkout Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

            updateCartDisplay();
            refreshInventory();

        } catch (IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private JTextField createTextField(String placeholder) {
        JTextField textField = new JTextField();
        textField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(8, 10, 8, 10)
        ));
        textField.setToolTipText(placeholder);
        return textField;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);

        button.setFont(UITheme.BUTTON_FONT);
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(UITheme.ACCENT_GOLD.darker());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(UITheme.ACCENT_GOLD);
            }
        });

        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}