package com.sixstars.ui;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.sixstars.app.Main;

public class AccountDetailsPage extends JPanel {
    static JLabel firstName = new JLabel("Unknown");
    static JLabel lastName = new JLabel("Unknown");
    static JLabel email = new JLabel("Unknown");
    static JLabel role = new JLabel("Unknown");

    void refreshInfo() {
        try {
            var account = com.sixstars.controller.AccountController.currentAccount;
    
            if (account != null) {
                firstName.setText(account.getFirstName() != null ? account.getFirstName() : "Unknown");
                lastName.setText(account.getLastName() != null ? account.getLastName() : "Unknown");
                email.setText(account.getEmail() != null ? account.getEmail() : "Unknown");
                role.setText(account.getRole() != null ? account.getRole().toString() : "Unknown");
            } else {
                firstName.setText("Unknown");
                lastName.setText("Unknown");
                email.setText("Unknown");
                role.setText("Unknown");
            }
    
            // Refresh UI
            revalidate();
            repaint();
    
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Failed to Refresh Account Details",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

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
        back.addActionListener(e -> {
            Main.headerBar.refreshInfo();
            cardLayout.show(pages, "home");
     });

        add(back);
        add(Box.createVerticalGlue());
    }

}