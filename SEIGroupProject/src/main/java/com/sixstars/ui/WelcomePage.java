package com.sixstars.ui;

import javax.swing.*;
import java.awt.*;

public class WelcomePage extends JFrame {
    public WelcomePage() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(94, 190, 225));

        JLabel label = new JLabel("Welcome to 6 Stars Hotel!");
        label.setFont(new Font("Times New Roman", Font.BOLD, 25));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton signUpButton = new JButton("Create Account");
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(Box.createVerticalGlue());
        panel.add(label);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(loginButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(signUpButton);
        panel.add(Box.createVerticalGlue());

        add(panel);

        setSize(700, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }
}