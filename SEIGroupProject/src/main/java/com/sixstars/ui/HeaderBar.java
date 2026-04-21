package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class HeaderBar extends JPanel {

    private JButton loginButton;
    private JButton createAccountButton;
    private JButton btnAccount;
    private JPanel navPanel;
    private JButton btnMyReservations;

    private JPanel pages;
    private CardLayout cardLayout;

    public HeaderBar(JPanel pages, CardLayout cardLayout) {
        this.pages = pages;
        this.cardLayout = cardLayout;

        setLayout(new BorderLayout());
        setBackground(UITheme.CARD_BACKGROUND);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 28, 16, 28)
        ));

        JButton brandButton = new JButton("6 Stars Hotel");
        brandButton.setFont(new Font("Serif", Font.BOLD, 30));
        brandButton.setForeground(UITheme.TEXT_DARK);

        // Make it look like a label
        brandButton.setBorderPainted(false);
        brandButton.setContentAreaFilled(false);
        brandButton.setFocusPainted(false);
        brandButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        brandButton.addActionListener(e -> {
            cardLayout.show(pages, "home");
        });

        navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);

        JButton bookNowButton = createButton("Book Now");
        bookNowButton.addActionListener(e -> {
            cardLayout.show(pages, "make reservation");
            Main.headerBar.refreshInfo();
        });

        btnMyReservations = createButton("My Reservations");
        btnMyReservations.addActionListener(e -> {
            Main.guestReservationsPage.refresh();
            cardLayout.show(pages, "guest reservations");
        });

        loginButton = createButton("Login");
        loginButton.addActionListener(e -> cardLayout.show(pages, "login"));

        createAccountButton = createButton("Create Account");
        createAccountButton.addActionListener(e -> {
            Main.createAccountPage.refreshInfo();
            cardLayout.show(pages, "create account");
        });

        btnAccount = createButton("My Account");

        // Dropdown menu
        JPopupMenu menu = new JPopupMenu();

        JMenuItem view = new JMenuItem("View Account");
        view.addActionListener(e -> {
            Main.accountDetailsPage.refreshInfo();
            cardLayout.show(pages, "account details");
        });

        JMenuItem logout = new JMenuItem("Logout");
        logout.addActionListener(e -> {
            AccountController.currentAccount = null;
            refreshInfo();
            cardLayout.show(pages, "home");
            JOptionPane.showMessageDialog(this, "Logged out successfully");
        });

        JMenuItem dashboard = new JMenuItem("Dashboard");
        dashboard.addActionListener(e -> {
            if (AccountController.currentAccount != null && AccountController.currentAccount.getRole() == Role.CLERK) {
                refreshInfo();
                cardLayout.show(pages, "clerk page");
            }
            else if (AccountController.currentAccount != null && AccountController.currentAccount.getRole() == Role.ADMIN) {
                refreshInfo();
                cardLayout.show(pages, "admin page");
            }
            else if (AccountController.currentAccount != null) {
                refreshInfo();
                Main.billingPage.refresh();
                cardLayout.show(pages, "billing page");
            }
        });

        menu.add(view);
        menu.add(dashboard);
        menu.add(logout);
        

        btnAccount.addActionListener(e ->
                menu.show(btnAccount, 0, btnAccount.getHeight())
        );

        navPanel.add(bookNowButton);
        navPanel.add(btnMyReservations);
        navPanel.add(loginButton);
        navPanel.add(createAccountButton);
        navPanel.add(btnAccount);

        add(brandButton, BorderLayout.WEST);
        add(navPanel, BorderLayout.EAST);

        refreshInfo();
    }

    public void refreshInfo() {
        Account current = AccountController.currentAccount;
        boolean loggedIn = (current != null);

        loginButton.setVisible(!loggedIn);
        createAccountButton.setVisible(!loggedIn);
        btnAccount.setVisible(loggedIn);

        if (loggedIn) {
            btnMyReservations.setVisible(current.getRole() == Role.GUEST);
        } else {
            btnMyReservations.setVisible(false);
        }

        revalidate();
        repaint();
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}