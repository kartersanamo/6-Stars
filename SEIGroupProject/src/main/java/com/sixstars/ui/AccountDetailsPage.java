package com.sixstars.ui;

import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;

import javax.swing.*;
import java.awt.*;

public class AccountDetailsPage extends JPanel {
    static JLabel firstName = new JLabel("Unknown");
    static JLabel lastName = new JLabel("Unknown");
    static JLabel email = new JLabel("Unknown");
    static JLabel role = new JLabel("Unknown");

    public AccountDetailsPage(JPanel pages, CardLayout cardLayout) {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        // Title
        JLabel title = new JLabel("Account Details");
        title.setFont(new Font("Times New Roman", Font.BOLD, 25));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(40));
        add(title);
        add(Box.createVerticalStrut(30));

        // Form panel
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(4, 2, 10, 15));
        form.setMaximumSize(new Dimension(400, 200));
        form.setBackground(Color.WHITE);

        try {
            form.add(new JLabel("First Name:"));
            form.add(firstName);

            form.add(new JLabel("Last Name:"));
            form.add(lastName);

            form.add(new JLabel("Email:"));
            form.add(email);

            form.add(new JLabel("Role:"));
            form.add(role);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Failed to Fetch Account Details",
                    JOptionPane.ERROR_MESSAGE
            );
        }

        add(form);
        add(Box.createVerticalStrut(30));

        // Back button
        JButton back = new JButton("Back");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(e -> cardLayout.show(pages, "menu page"));

        add(back);
        add(Box.createVerticalGlue());
    }

}