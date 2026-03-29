package com.sixstars.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import com.sixstars.app.Main;

public class WelcomePage extends JPanel {

    public WelcomePage(JPanel pages, CardLayout cardLayout) {
        setLayout(new GridBagLayout());
        setBackground(new Color(24, 44, 72));

        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(new Color(245, 247, 250));
        cardPanel.setBorder(new EmptyBorder(35, 50, 35, 50));
        cardPanel.setPreferredSize(new Dimension(420, 320));

        JLabel titleLabel = new JLabel("Welcome to 6 Stars Hotel");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(24, 44, 72));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Luxury stays made simple");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(90, 90, 90));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = createStyledButton("Login");
        loginButton.addActionListener(e -> cardLayout.show(pages, "login"));

        JButton signUpButton = createStyledButton("Create Account");
        signUpButton.addActionListener(e -> {
            Main.createAccountPage.refresh();
            cardLayout.show(pages, "create account");
        });

        JButton makeReservationButton = createStyledButton("Make Reservation");
        makeReservationButton.addActionListener(e -> cardLayout.show(pages, "make reservation"));

        cardPanel.add(titleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        cardPanel.add(loginButton);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        cardPanel.add(signUpButton);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 12)));
        cardPanel.add(makeReservationButton);

        add(cardPanel);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(260, 45));
        button.setPreferredSize(new Dimension(260, 45));
        button.setFont(new Font("SansSerif", Font.BOLD, 16));

        Color normalColor = new Color(30, 90, 160);
        Color hoverColor = new Color(50, 120, 200);

        button.setBackground(normalColor);
        button.setForeground(Color.WHITE);

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(hoverColor);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(normalColor);
            }
        });

        return button;
    }
}