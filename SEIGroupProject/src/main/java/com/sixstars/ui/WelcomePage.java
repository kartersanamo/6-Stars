package com.sixstars.ui;

import javax.swing.*;
import java.awt.*;

public class WelcomePage extends JPanel {
    public WelcomePage(JPanel pages, CardLayout cardLayout) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(94, 190, 225));

        JLabel label = new JLabel("Welcome to 6 Stars Hotel!");
        label.setFont(new Font("Times New Roman", Font.BOLD, 25));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> {
            cardLayout.show(pages, "login");
        });

        JButton signUpButton = new JButton("Create Account");
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.addActionListener(e->{
            cardLayout.show(pages, "create account");
        });

        JButton makeReservationButton = new JButton("Make Reservation");
        makeReservationButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        makeReservationButton.addActionListener(e->{
            cardLayout.show(pages, "make reservation");
        });

        add(Box.createVerticalGlue());
        add(label);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(loginButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(signUpButton);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(makeReservationButton);
        add(Box.createVerticalGlue());

    }
}