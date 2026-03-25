package com.sixstars.ui;

import com.sixstars.model.BedType;
import com.sixstars.model.QualityLevel;
import com.sixstars.model.Room;
import com.sixstars.model.Theme;
import com.sixstars.service.RoomService;
import javax.swing.*;
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
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        setBackground(Color.WHITE);

        // 1. Header
        JLabel title = new JLabel("Room Management Dashboard", SwingConstants.CENTER);
        title.setFont(new Font("Times New Roman", Font.BOLD, 24));
        add(title, BorderLayout.NORTH);

        // 2. Center Panel (Inputs + List)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JPanel addCard = new JPanel(new GridLayout(3, 4, 10, 10));
        addCard.setBorder(BorderFactory.createTitledBorder("Add New Room"));
        addCard.setBackground(Color.WHITE);

        roomNumField = new JTextField();
        bedTypeBox = new JComboBox<>(BedType.values());
        themeBox = new JComboBox<>(Theme.values());
        qualityBox = new JComboBox<>(QualityLevel.values());
        smokingCheckBox = new JCheckBox("Smoking Allowed");
        smokingCheckBox.setBackground(Color.WHITE);

        JButton btnConfirmAdd = new JButton("Add Room to Inventory");
        btnConfirmAdd.setBackground(new Color(63, 78, 246));
        btnConfirmAdd.setForeground(Color.BLACK);

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

        addCard.add(new JLabel(""));
        addCard.add(btnConfirmAdd);

        listModel = new DefaultListModel<>();
        roomList = new JList<>(listModel);
        refreshRoomList(roomService);
        JScrollPane scrollPane = new JScrollPane(roomList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Current Inventory"));
        scrollPane.setPreferredSize(new Dimension(500, 200));

        centerPanel.add(addCard);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Gap
        centerPanel.add(scrollPane);
        add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Controls
        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("Back to Menu");
        btnBack.addActionListener(e -> cardLayout.show(pages, "menu page"));
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        btnConfirmAdd.addActionListener(e -> {
            try {
                int num = Integer.parseInt(roomNumField.getText());
                BedType type = (BedType) bedTypeBox.getSelectedItem();
                Theme theme = (Theme) themeBox.getSelectedItem();
                QualityLevel quality = (QualityLevel) qualityBox.getSelectedItem();
                boolean smoking = smokingCheckBox.isSelected();

                // Create and Save
                Room newRoom = new Room(num, type, theme, quality, smoking);
                roomService.addRoom(newRoom);

                // Update UI
                refreshRoomList(roomService);
                roomNumField.setText("");
                JOptionPane.showMessageDialog(this, "Room " + num + " added!");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid room number.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }

    private void refreshRoomList(RoomService roomService) {
        listModel.clear();
        for (Room r : roomService.getAllRooms()) {
            listModel.addElement(r);
        }
    }
}