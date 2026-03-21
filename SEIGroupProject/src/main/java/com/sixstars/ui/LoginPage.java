package com.sixstars.ui;

import javax.swing.*;

import com.sixstars.logicClasses.LoginController;
import com.sixstars.logicClasses.Account;
import com.sixstars.logicClasses.AccountController;

import java.awt.*;

public class LoginPage extends JPanel {
    public LoginPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);

        JLabel label = new JLabel("Login Page");
        label.setFont(new Font("Times New Roman", Font.BOLD, 25));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton backButton = new JButton("Back");
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);


          // Username
          JLabel userLabel = new JLabel("Username:");
          userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
  
          JTextField usernameField = new JTextField(15);
          usernameField.setMaximumSize(new Dimension(200, 30));
          usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);


          // Password
        JLabel passLabel = new JLabel("Password:");
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPasswordField passwordField = new JPasswordField(15);
        passwordField.setMaximumSize(new Dimension(200, 30));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton loginButton = new JButton("Log In");
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            Account a = LoginController.checkLogin(username, password);
            if (a != null) {
                AccountController.currentAccount = a;
                JOptionPane.showMessageDialog(this, "Login successful!");
                cardLayout.show(pages, "nextPage");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.");
            }
        });
        backButton.addActionListener(e -> {
            cardLayout.show(pages, "welcome");
        });

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

        add(Box.createRigidArea(new Dimension(0, 20)));
        add(loginButton);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(backButton);
        add(Box.createVerticalGlue());
    }
}