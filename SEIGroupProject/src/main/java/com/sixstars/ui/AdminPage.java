package com.sixstars.ui;

import javax.swing.*;
import java.awt.*;

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
        logoutButton.addActionListener(e -> cardLayout.show(pages, "welcome"));

        add(Box.createVerticalGlue());
        add(title);
        add(Box.createRigidArea(new Dimension(0, 12)));
        add(subtitle);
        add(Box.createRigidArea(new Dimension(0, 24)));
        add(logoutButton);
        add(Box.createVerticalGlue());
    }
}
