package com.sixstars.ui;

import com.sixstars.model.BedType;
import com.sixstars.model.QualityLevel;
import com.sixstars.model.Room;
import com.sixstars.model.Theme;
import com.sixstars.service.RoomService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class RoomManagementPage extends JPanel {
    private DefaultListModel<Room> listModel;
    private JList<Room> roomList;
    private JTextField roomNumField;
    private JComboBox<BedType> bedTypeBox;
    private JComboBox<Theme> themeBox;
    private JComboBox<QualityLevel> qualityBox;
    private JCheckBox smokingCheckBox;

    public RoomManagementPage(JPanel pages, CardLayout cardLayout, RoomService roomService) {
        // Match the background of the rest of the app
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        setBackground(UITheme.PAGE_BACKGROUND);

        // 1. Header
        JLabel title = new JLabel("Room Inventory Management", SwingConstants.CENTER);
        title.setFont(UITheme.TITLE_FONT);
        title.setForeground(UITheme.TEXT_DARK);
        add(title, BorderLayout.NORTH);

        // 2. Center Panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        // Add New Room Section (Themed as a Card)
        JPanel addCard = new JPanel(new GridLayout(3, 4, 10, 10));
        addCard.setBackground(UITheme.CARD_BACKGROUND);
        addCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                        "Add New Room",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        UITheme.LABEL_FONT,
                        UITheme.TEXT_MEDIUM
                ),
                new EmptyBorder(15, 15, 15, 15)
        ));

        roomNumField = new JTextField();
        styleComponent(roomNumField);

        bedTypeBox = new JComboBox<>(BedType.values());
        styleComponent(bedTypeBox);

        themeBox = new JComboBox<>(Theme.values());
        styleComponent(themeBox);

        qualityBox = new JComboBox<>(QualityLevel.values());
        styleComponent(qualityBox);

        smokingCheckBox = new JCheckBox("Smoking Allowed");
        smokingCheckBox.setBackground(UITheme.CARD_BACKGROUND);
        smokingCheckBox.setFont(UITheme.LABEL_FONT);
        smokingCheckBox.setForeground(UITheme.TEXT_MEDIUM);

        JButton btnConfirmAdd = new JButton("Add Room");
        styleThemedButton(btnConfirmAdd);

        // Grid layout components
        addCard.add(new JLabel("Room Number:"));
        addCard.add(roomNumField);
        addCard.add(new JLabel("Floor Theme:"));
        addCard.add(themeBox);
        addCard.add(new JLabel("Bed Type:"));
        addCard.add(bedTypeBox);
        addCard.add(new JLabel("Quality:"));
        addCard.add(qualityBox);
        addCard.add(new JLabel("Option:"));
        addCard.add(smokingCheckBox);
        addCard.add(new JLabel("")); // Spacer
        addCard.add(btnConfirmAdd);

        // Inventory List Section
        listModel = new DefaultListModel<>();
        roomList = new JList<>(listModel);
        roomList.setFont(UITheme.INPUT_FONT);
        refreshRoomList(roomService);

        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
                "Current Inventory",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                UITheme.LABEL_FONT,
                UITheme.TEXT_MEDIUM
        ));
        scrollPane.setPreferredSize(new Dimension(500, 200));

        centerPanel.add(addCard);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(scrollPane);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Controls
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);

        JButton btnBack = new JButton("Back to Dashboard");
        styleThemedButton(btnBack);

        // FIX: Navigation string must match "clerk page" in Main.java
        btnBack.addActionListener(e -> cardLayout.show(pages, "clerk page"));

        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        btnConfirmAdd.addActionListener(e -> {
            try {
                int num = Integer.parseInt(roomNumField.getText().trim());
                BedType type = (BedType) bedTypeBox.getSelectedItem();
                Theme theme = (Theme) themeBox.getSelectedItem();
                QualityLevel quality = (QualityLevel) qualityBox.getSelectedItem();
                boolean smoking = smokingCheckBox.isSelected();

                Room newRoom = new Room(num, type, theme, quality, smoking);
                roomService.addRoom(newRoom);

                refreshRoomList(roomService);
                roomNumField.setText("");
                JOptionPane.showMessageDialog(this, "Room " + num + " added!");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid room number.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }

    private void styleComponent(JComponent comp) {
        comp.setFont(UITheme.INPUT_FONT);
        comp.setBackground(Color.WHITE);
        comp.setForeground(UITheme.TEXT_DARK);
    }

    private void styleThemedButton(JButton button) {
        button.setPreferredSize(new Dimension(200, 40));
        button.setFont(UITheme.BUTTON_FONT);
        button.setBackground(UITheme.SECONDARY_BUTTON); // The lighter color
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void refreshRoomList(RoomService roomService) {
        listModel.clear();
        for (Room r : roomService.getAllRooms()) {
            listModel.addElement(r);
        }
    }
}