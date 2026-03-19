package com.sixstars.ui;

import javax.swing.*;
import java.awt.*;

import static javax.swing.text.StyleConstants.setBackground;

public class CreateAccountPage extends JPanel {
    public CreateAccountPage(JPanel pages, CardLayout cardLayout) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JLabel titleLabel = new JLabel("Create Guest Account");
        titleLabel.setFont(new Font("Times New Roman", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(500, 250));

        JLabel firstNameLabel = new JLabel("First Name:");
        JTextField firstNameField = new JTextField();

        JLabel lastNameLabel = new JLabel("Last Name:");
        JTextField lastNameField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

        formPanel.add(firstNameLabel);
        formPanel.add(firstNameField);

        formPanel.add(lastNameLabel);
        formPanel.add(lastNameField);

        formPanel.add(emailLabel);
        formPanel.add(emailField);

        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        mainPanel.add(formPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton createButton = new JButton("Create Account");
        JButton backButton = new JButton("Back");

        buttonPanel.add(createButton);
        buttonPanel.add(backButton);

        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);

        createButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());

            JOptionPane.showMessageDialog(
                    this,
                    "Guest account created for " + firstName + " " + lastName,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
            );

            firstNameField.setText("");
            lastNameField.setText("");
            emailField.setText("");
            passwordField.setText("");
        });

        backButton.addActionListener(e -> {
            CardLayout cl = (CardLayout) pages.getLayout();
            cl.show(pages, "welcome");
        });
    }
}
