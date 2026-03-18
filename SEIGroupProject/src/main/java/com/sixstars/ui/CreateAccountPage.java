package com.sixstars.ui;

import javax.swing.*;
import java.awt.*;

import static javax.swing.text.StyleConstants.setBackground;

public class CreateAccountPage extends JPanel {
    public CreateAccountPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel label = new JLabel("Create Account Page");
        label.setFont(new Font("Times New Roman", Font.BOLD, 25));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        backButton.addActionListener(e -> {
            cardLayout.show(pages, "welcome");
        });

        add(Box.createVerticalGlue());
        add(label);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(backButton);
        add(Box.createVerticalGlue());
    }
}
