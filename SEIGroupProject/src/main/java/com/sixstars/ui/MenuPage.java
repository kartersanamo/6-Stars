package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

import javax.swing.*;
import java.awt.*;

public class MenuPage extends JPanel {
    private JLabel welcomeLabel;
    private JButton btnManageRooms;

    public MenuPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        welcomeLabel = new JLabel("Welcome!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        // Panel for the main buttons
        JPanel menuPanel = new JPanel(new GridLayout(4, 1, 0, 20));
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        JButton btnReserve = new JButton("Make a Reservation");
        JButton btnShop = new JButton("Visit the Store");
        JButton btnAccount = new JButton("My Account");
        btnManageRooms = new JButton("Room Management");
        btnManageRooms.setVisible(false);

        // Navigation Logic
        btnReserve.addActionListener(e -> cardLayout.show(pages, "make reservation"));
        btnShop.addActionListener(e -> JOptionPane.showMessageDialog(this, "Store coming soon"));
        btnAccount.addActionListener(e -> JOptionPane.showMessageDialog(this, "Account details coming soon"));
        btnManageRooms.addActionListener(e -> cardLayout.show(pages, "room management"));
        btnAccount.addActionListener(e -> cardLayout.show(pages, "account details"));

        menuPanel.add(btnReserve);
        menuPanel.add(btnShop);
        menuPanel.add(btnAccount);
        menuPanel.add(btnManageRooms);
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
        Account current = AccountController.currentAccount;

        if (current != null) {
            // 1. Update the Text
            welcomeLabel.setText("Welcome, " + current.getFirstName() + "!");

            // 2. Role-Based Access Control (RBAC)
            // Only show the button if the user is a CLERK
            if (current.getRole() == Role.CLERK) {
                btnManageRooms.setVisible(true);
            } else {
                btnManageRooms.setVisible(false);
            }
        } else {
            // Safety check: if no one is logged in, hide the button and reset text
            welcomeLabel.setText("Welcome!");
            btnManageRooms.setVisible(false);
        }

        // Tell Swing to refresh the layout to show/hide the button immediately
        this.revalidate();
        this.repaint();
    }
}
