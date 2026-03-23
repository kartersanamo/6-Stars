package com.sixstars.ui;

import javax.swing.*;
import java.awt.*;
import com.sixstars.logicClasses.LoginController;
import com.sixstars.logicClasses.Account;
import com.sixstars.logicClasses.AccountController;
import com.sixstars.logicClasses.Role;

public class LoginPage extends JPanel {
    public LoginPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel label = new JLabel("Login");
        label.setFont(new Font("Times New Roman", Font.BOLD, 25));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTextField usernameField = new JTextField(15);
        usernameField.setMaximumSize(new Dimension(200, 30));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setMaximumSize(new Dimension(200, 30));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = new JButton("Log In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(_ -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            Account a = LoginController.checkLogin(username, password);
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
        add(userLabel);
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(usernameField);
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
