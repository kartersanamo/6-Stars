package com.sixstars.ui;

import javax.swing.*;
import java.awt.*;
import com.sixstars.controller.LoginController;
import com.sixstars.model.Account;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Role;

public class LoginPage extends JPanel {
    public LoginPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel label = new JLabel("Login");
        label.setFont(new Font("Times New Roman", Font.BOLD, 25));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField emailField = new JTextField(15);
        emailField.setMaximumSize(new Dimension(200, 30));
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setMaximumSize(new Dimension(200, 30));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = new JButton("Log In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(_ -> {
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            Account a = LoginController.checkLogin(email, password);
            if (a != null) {
                AccountController.currentAccount = a;
                if (a.getRole() == Role.ADMIN) {
                    JOptionPane.showMessageDialog(this, "Login successful! (Admin)");
                    cardLayout.show(pages, "admin");
                } else {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    cardLayout.show(pages, "nextPage");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.");
            }
        });

        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(_ -> cardLayout.show(pages, "welcome"));

        add(Box.createVerticalGlue());
        add(label);
        add(Box.createRigidArea(new Dimension(0, 30)));
        add(emailLabel);
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(emailField);
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(passLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(passwordField);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(loginButton);
        add(Box.createRigidArea(new Dimension(0, 8)));
        add(backButton);
        add(Box.createVerticalGlue());
    }
}
