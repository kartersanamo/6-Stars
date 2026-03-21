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

    public CreateAccountPage(JPanel pages, CardLayout cardLayout) {


         // Check if current user is admin (used later):
         final boolean isAdmin = AccountController.currentAccount != null &&
         AccountController.currentAccount.getRole() == Role.ADMIN;


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

        formPanel = new JPanel(new GridLayout(4, 2, 10, 15));
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
        roleComboBox.setMaximumSize(new Dimension(200, 30));
        roleComboBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        

        formPanel.add(firstNameLabel);
        formPanel.add(firstNameField);

        formPanel.add(lastNameLabel);
        formPanel.add(lastNameField);

        formPanel.add(emailLabel);
        formPanel.add(emailField);

        formPanel.add(passwordLabel);
        formPanel.add(passwordField);


        formPanel.add(roleLabel);
        formPanel.add(roleComboBox);


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

            // Assuming you are not an admin, default to making a guest:
            if (!isAdmin) {
                roleSet = Role.GUEST;
            }
            else {
                roleSet = (Role) roleComboBox.getSelectedItem();
            }

            if (roleSet == Role.GUEST) {
                try {
                    accountController.createGuestAccount(firstName, lastName, email, password);

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
            }
            backButton.addActionListener(ac -> {
                CardLayout cl = (CardLayout) pages.getLayout();
                cl.show(pages, "welcome");
            });
        });
    }

    public void refresh() {
        boolean isAdmin = AccountController.currentAccount != null &&
                  AccountController.currentAccount.getRole() == Role.ADMIN;

        // Show or hide the role dropdown
        roleLabel.setVisible(isAdmin);
        roleComboBox.setVisible(isAdmin);

        formPanel.revalidate();
        formPanel.repaint();
    }
}
