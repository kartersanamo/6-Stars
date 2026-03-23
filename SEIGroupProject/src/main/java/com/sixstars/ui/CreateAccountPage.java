package com.sixstars.ui;


import com.sixstars.logicClasses.AccountController;
import com.sixstars.logicClasses.Role;

import javax.swing.*;
import java.awt.*;

public class CreateAccountPage extends JPanel {
    private final AccountController accountController;
    JPanel formPanel;
    JLabel roleLabel;
    JComboBox<Role> roleComboBox;
    Boolean isAdmin;
    public CreateAccountPage(JPanel pages, CardLayout cardLayout) {




        accountController = new AccountController();
        

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

        formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setMaximumSize(new Dimension(500, 200));

        JLabel firstNameLabel = new JLabel("First Name:");
        JTextField firstNameField = new JTextField();

        JLabel lastNameLabel = new JLabel("Last Name:");
        JTextField lastNameField = new JTextField();

        JLabel emailLabel = new JLabel("Email:");
        JTextField emailField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField();

    
        roleLabel = new JLabel("Role:");
        Role[] roles = {Role.GUEST, Role.CLERK, Role.ADMIN};
        roleComboBox = new JComboBox<>(roles);
        roleComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);


        // Set the sizing and centering
        Dimension fieldSize = new Dimension(300, 25);
        firstNameField.setMaximumSize(fieldSize);
        lastNameField.setMaximumSize(fieldSize);
        emailField.setMaximumSize(fieldSize);
        passwordField.setMaximumSize(fieldSize);
        roleComboBox.setMaximumSize(fieldSize);


        firstNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        firstNameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        lastNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lastNameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        emailLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailField.setAlignmentX(Component.CENTER_ALIGNMENT);

        passwordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        roleComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        formPanel.add(firstNameLabel);
        formPanel.add(firstNameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formPanel.add(lastNameLabel);
        formPanel.add(lastNameField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formPanel.add(emailLabel);
        formPanel.add(emailField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));


        formPanel.add(roleLabel);
        formPanel.add(roleComboBox);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));


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
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            Role roleSet;

            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please fill in all fields.",
                        "Missing Information",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

          


            try {
                // Assuming you are not an admin, default to making a guest:
                if (!isAdmin) {
                    roleSet = Role.GUEST;
                }
                else {
                    roleSet = (Role) roleComboBox.getSelectedItem();
                }
                accountController.createAccount(firstName, lastName, email, password, roleSet);

                JOptionPane.showMessageDialog(
                        this,
                        roleSet + " account created for " + firstName + " " + lastName,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                firstNameField.setText("");
                lastNameField.setText("");
                emailField.setText("");
                passwordField.setText("");

                CardLayout cl = (CardLayout) pages.getLayout();
                cl.show(pages, "welcome");

            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(
                        this,
                        ex.getMessage(),
                        "Account Creation Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        backButton.addActionListener(ac -> {
            cardLayout.show(pages, "welcome");
        });
    }




    public void refresh() {
        isAdmin = AccountController.currentAccount != null &&
                  AccountController.currentAccount.getRole() == Role.ADMIN;

        // Show or hide the role dropdown
        roleLabel.setVisible(isAdmin);
        roleComboBox.setVisible(isAdmin);

        formPanel.revalidate();
        formPanel.repaint();
    }
}
