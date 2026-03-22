package com.sixstars.ui;

import com.sixstars.logicClasses.AdminAuth;

import javax.swing.*;
import java.awt.*;

public class LoginPage extends JPanel {
    public LoginPage(JPanel pages, CardLayout cardLayout, AdminAuth adminAuth) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel label = new JLabel("Admin Login");
        label.setFont(new Font("Times New Roman", Font.BOLD, 25));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField usernameField = new JTextField(18);
        JPasswordField passwordField = new JPasswordField(18);

        JPanel userRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        userRow.setBackground(Color.WHITE);
        userRow.add(new JLabel("Username:"));
        userRow.add(usernameField);

        JPanel passRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
        passRow.setBackground(Color.WHITE);
        passRow.add(new JLabel("Password:"));
        passRow.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            char[] password = passwordField.getPassword();
            if (username.isBlank() || password.length == 0) {
                JOptionPane.showMessageDialog(this,
                        "Please enter username and password.",
                        "Login",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (adminAuth.authenticate(username, password)) {
                passwordField.setText("");
                cardLayout.show(pages, "admin");
            } else {
                passwordField.setText("");
                JOptionPane.showMessageDialog(this,
                        "Invalid username or password.",
                        "Login",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.addActionListener(e -> cardLayout.show(pages, "welcome"));

        add(Box.createVerticalGlue());
        add(label);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(userRow);
        add(passRow);
        add(Box.createRigidArea(new Dimension(0, 12)));
        add(loginButton);
        add(Box.createRigidArea(new Dimension(0, 8)));
        add(backButton);
        add(Box.createVerticalGlue());
    }
}
