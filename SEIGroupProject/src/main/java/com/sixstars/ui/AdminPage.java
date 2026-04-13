package com.sixstars.ui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;

public class AdminPage extends JPanel {
    public AdminPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel title = new JLabel("Administrator");
        title.setFont(new Font("Times New Roman", Font.BOLD, 25));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("You are signed in as admin.");
        subtitle.setFont(new Font("Times New Roman", Font.PLAIN, 16));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoutButton.addActionListener(e -> {
            cardLayout.show(pages, "welcome");
            AccountController.currentAccount = null;
        });

        JButton signUpButton = new JButton("Create Account");
        signUpButton.addActionListener(_ -> {
            Main.createAccountPage.refresh();
            cardLayout.show(pages, "create account");
        });

        add(Box.createVerticalGlue());
        add(title);
        add(Box.createRigidArea(new Dimension(0, 12)));
        add(subtitle);
        add(Box.createRigidArea(new Dimension(0, 24)));
        add(signUpButton);
        add(Box.createVerticalGlue());
        add(logoutButton);
        add(Box.createVerticalGlue());
    }
}
