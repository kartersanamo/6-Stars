package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;

public class HeaderBar extends JPanel {

    private JButton loginButton;
    private JButton createAccountButton;
    private JButton btnAccount;
    private JPanel navPanel;

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

        JLabel brandLabel = new JLabel("6 Stars Hotel");
        brandLabel.setFont(new Font("Serif", Font.BOLD, 30));
        brandLabel.setForeground(UITheme.TEXT_DARK);

        navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navPanel.setOpaque(false);

        JButton bookNowButton = createButton("Book Now");
        bookNowButton.addActionListener(e -> {
            cardLayout.show(pages, "make reservation");
            Main.headerBar2.refreshInfo();
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
            JOptionPane.showMessageDialog(this, "Logged out successfully");
        });

        menu.add(view);
        menu.add(logout);

        btnAccount.addActionListener(e ->
                menu.show(btnAccount, 0, btnAccount.getHeight())
        );

        navPanel.add(bookNowButton);
        navPanel.add(loginButton);
        navPanel.add(createAccountButton);
        navPanel.add(btnAccount);

        add(brandLabel, BorderLayout.WEST);
        add(navPanel, BorderLayout.EAST);

        refreshInfo();
    }

    public void refreshInfo() {
        boolean loggedIn = AccountController.currentAccount != null;

        loginButton.setVisible(!loggedIn);
        createAccountButton.setVisible(!loggedIn);
        btnAccount.setVisible(loggedIn);

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