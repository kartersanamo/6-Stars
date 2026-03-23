package com.sixstars.ui;

import com.sixstars.logicClasses.AccountController;
import javax.swing.*;
import java.awt.*;

public class MenuPage extends JPanel {
    private JLabel welcomeLabel;

    public MenuPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        welcomeLabel = new JLabel("Welcome!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        // Panel for the main buttons
        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        JButton btnReserve = new JButton("Make a Reservation");
        JButton btnShop = new JButton("Visit the Store");
        JButton btnAccount = new JButton("My Account");

        // Navigation Logic
        btnReserve.addActionListener(e -> cardLayout.show(pages, "make reservation"));
        btnShop.addActionListener(e -> JOptionPane.showMessageDialog(this, "Store coming soon"));
        btnAccount.addActionListener(e -> JOptionPane.showMessageDialog(this, "Account details coming soon"));

        menuPanel.add(btnReserve);
        menuPanel.add(btnShop);
        menuPanel.add(btnAccount);
        add(menuPanel, BorderLayout.CENTER);

        // Logout Button at the bottom
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            AccountController.currentAccount = null;
            cardLayout.show(pages, "welcome");
        });

        JPanel footer = new JPanel();
        footer.setBackground(Color.WHITE);
        footer.add(btnLogout);
        add(footer, BorderLayout.SOUTH);
    }

    public void updateWelcomeMessage() {
        if (AccountController.currentAccount != null) {
            String name = AccountController.currentAccount.getFirstName();
            welcomeLabel.setText("Welcome, " + name + "!");
        } else {
            welcomeLabel.setText("Welcome!");
        }
    }
}
