package com.sixstars.ui;

import com.sixstars.model.*; // Import Room, BedType, Theme, QualityLevel
import com.sixstars.service.RoomService;
import javax.swing.*;
import java.awt.*;

public class AddRoomDialog extends JDialog {
    private boolean succeeded = false;

    public AddRoomDialog(Frame parent, RoomService roomService) {
        super(parent, "Clerk: Add New Hotel Room", true);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Form Fields - Now using Enum types for the ComboBoxes
        JTextField numField = new JTextField();
        JComboBox<BedType> bedBox = new JComboBox<>(BedType.values());
        JComboBox<Theme> themeBox = new JComboBox<>(Theme.values()); // Using Enum
        JComboBox<QualityLevel> qualityBox = new JComboBox<>(QualityLevel.values()); // Using Enum
        JCheckBox smokingCheck = new JCheckBox("Smoking Allowed");

        panel.add(new JLabel("Room Number:")); panel.add(numField);
        panel.add(new JLabel("Bed Type:"));    panel.add(bedBox);
        panel.add(new JLabel("Floor Theme:")); panel.add(themeBox);
        panel.add(new JLabel("Quality:"));     panel.add(qualityBox);
        panel.add(new JLabel("Options:"));     panel.add(smokingCheck);

        JButton saveBtn = new JButton("Save Room");
        JButton cancelBtn = new JButton("Cancel");

        saveBtn.addActionListener(e -> {
            try {
                int num = Integer.parseInt(numField.getText());

                // Casting to the specific Enum types
                Room newRoom = new Room(
                        num,
                        (BedType) bedBox.getSelectedItem(),
                        (Theme) themeBox.getSelectedItem(),
                        (QualityLevel) qualityBox.getSelectedItem(),
                        smokingCheck.isSelected()
                );

                roomService.addRoom(newRoom);
                succeeded = true;
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid room number.");
            } catch (IllegalArgumentException ex) {
                // This catches the "Room already exists" error from RoomService
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        cancelBtn.addActionListener(e -> dispose());

        panel.add(saveBtn);
        panel.add(cancelBtn);

        add(panel);
        pack();
        setLocationRelativeTo(parent);
    }

    public boolean isSucceeded() { return succeeded; }
}
