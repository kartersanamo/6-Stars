package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.sixstars.model.Account;
import com.sixstars.service.AccountService;

public class ChangePasswordPage extends JPanel {

    private JList<Account> accountJList;
    private DefaultListModel<Account> listModel;

    private static AccountService accountService;

    public ChangePasswordPage(JPanel pages, CardLayout cardLayout, AccountService as) {
        setLayout(new GridBagLayout());
        setBackground(UITheme.PAGE_BACKGROUND);
        accountService = as;
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.CARD_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(30, 40, 30, 40)
        ));
        card.setPreferredSize(new Dimension(550, 500));

        JLabel title = new JLabel("Reset User Passwords");
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("View all system accounts");
        subtitle.setFont(UITheme.SUBTITLE_FONT);
        subtitle.setForeground(UITheme.TEXT_MEDIUM);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);



        listModel = new DefaultListModel<>();
        accountJList = new JList<>(listModel);
        accountJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountJList.setBackground(UITheme.PAGE_BACKGROUND);
        accountJList.setFont(new Font("SansSerif", Font.PLAIN, 14));

        accountJList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                Account acc = (Account) value;
                label.setText("User: " + acc.getFirstName() + " " + acc.getLastName());
                return label;
            }
        });


        JScrollPane scrollPane = new JScrollPane(accountJList);
        scrollPane.setPreferredSize(new Dimension(660, 250));

        JButton btnReset = createThemedButton("Enter New Password");

        btnReset.addActionListener(e -> {
            Account selected = accountJList.getSelectedValue();

            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Please select a user.");
                return;
            }


            String newPassword = JOptionPane.showInputDialog(
                    this,
                    "Enter new password for " + selected.getFirstName() + " " + selected.getLastName() + ":"
            );

            if (newPassword != null && !newPassword.trim().isEmpty()) {
                int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to reset the password for "
                        + selected.getFirstName() + " " + selected.getLastName() + " to: " + newPassword,
                    "Confirm Password Reset",
                    JOptionPane.YES_NO_OPTION
                );
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return; // user cancelled
                }
                // Later:
                AccountService aService = new AccountService();
                Account newA = new Account(selected.getFirstName(), selected.getLastName(), selected.getEmail(), aService.hashPassword(newPassword), selected.getRole());
                accountService.updateAccount(newA);
            }
});

        JButton btnBack = createThemedButton("Back");
        btnBack.addActionListener(e -> {
            cardLayout.show(pages, "admin page");
        });

        // Layout
        card.add(Box.createVerticalGlue());
        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(subtitle);
        card.add(Box.createRigidArea(new Dimension(0, 25)));
        card.add(scrollPane);
        card.add(Box.createRigidArea(new Dimension(0, 20)));
        card.add(btnReset);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(btnBack);
        
   

        add(card);

        // Load accounts
        refreshAccounts();
    }

    // Call this whenever page is opened
    public void refreshAccounts() {
        listModel.clear();

        List<Account> accounts = accountService.getAllAccounts();

        for (Account acc : accounts) {
            listModel.addElement(acc);
        }
    }

    private JPanel createAccountRow(Account acc) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(UITheme.PAGE_BACKGROUND);
        row.setBorder(new EmptyBorder(10, 15, 10, 15));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel info = new JLabel(
            "<html><b>User:</b> " + acc.getFirstName() +
            " &nbsp;&nbsp; <b>Password:</b> " + acc.getPasswordHash() + "</html>"
        );
        info.setFont(new Font("SansSerif", Font.PLAIN, 14));
        info.setForeground(UITheme.TEXT_DARK);

        row.add(info, BorderLayout.WEST);

        return row;
    }

    private JButton createThemedButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(320, 44));
        button.setMaximumSize(new Dimension(320, 44));
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}