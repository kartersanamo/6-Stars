package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class HeaderBar extends JPanel {

    private final JButton loginButton;
    private final JButton createAccountButton;
    private final JButton btnAccount;
    private final JPanel navPanel;
    private final JButton btnMyReservations;
    private final JButton btnShop;

    private final JPanel pages;
    private final CardLayout cardLayout;

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

        brandButton.setBorderPainted(false);
        brandButton.setContentAreaFilled(false);
        brandButton.setFocusPainted(false);
        brandButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        brandButton.addActionListener(_ -> cardLayout.show(pages, "home"));

        navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);

        JButton bookNowButton = createButton("Book Now");
        bookNowButton.addActionListener(_ -> {
            Main.makeReservationPage.refreshPage();
            cardLayout.show(pages, "make reservation");
            Main.headerBar.refreshInfo();
        });

        btnShop = createButton("View Shop");
        btnShop.addActionListener(_ -> {
            if (Main.shopPage != null) {
                Main.shopPage.refreshPage();
            }
            cardLayout.show(pages, "shop");
        });

        btnMyReservations = createButton("My Reservations");
        btnMyReservations.addActionListener(_ -> {
            Main.reservationsPage.refresh();
            cardLayout.show(pages, "reservations");
        });

        loginButton = createButton("Login");
        loginButton.addActionListener(_ -> cardLayout.show(pages, "login"));

        createAccountButton = createButton("Create Account");
        createAccountButton.addActionListener(_ -> {
            Main.createAccountPage.refreshInfo();
            cardLayout.show(pages, "create account");
        });

        btnAccount = createButton("My Account");
        btnAccount.addActionListener(_ -> {
            if (Main.accountCenterPage != null) {
                Main.accountCenterPage.refreshInfo();
            }
            cardLayout.show(pages, "account center");
        });

        navPanel.add(bookNowButton);
        navPanel.add(btnShop);
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