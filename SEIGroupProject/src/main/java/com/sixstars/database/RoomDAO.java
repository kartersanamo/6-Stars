package com.sixstars.database;

import com.sixstars.model.Room;
import com.sixstars.model.BedType;
import com.sixstars.model.Theme;
import com.sixstars.model.QualityLevel;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    /**
     * Saves a new room or updates an existing one based on the room number.
     */
    public void saveRoom(Room room) {
        String sql = "INSERT INTO rooms (roomNumber, bedType, theme, qualityLevel, isSmoking, pricePerNight) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pStmt = conn.prepareStatement(sql)) {

            pStmt.setInt(1, room.getRoomNumber());
            pStmt.setString(2, room.getBedType().name()); // Store enum name as String
            pStmt.setString(3, room.getTheme().name());   // Store enum name as String
            pStmt.setString(4, room.getQualityLevel().name()); // Store enum name as String
            pStmt.setInt(5, room.isSmoking() ? 1 : 0); // SQLite uses 1/0 for booleans
            pStmt.setInt(6, room.getPricePerNight());

            pStmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving room: " + e.getMessage());
        }
    }

    /**
     * Retrieves all rooms currently stored in the database.
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT roomNumber, bedType, theme, qualityLevel, isSmoking, pricePerNight FROM rooms";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("roomNumber"),
                        BedType.valueOf(rs.getString("bedType")),
                        Theme.valueOf(rs.getString("theme")),
                        QualityLevel.valueOf(rs.getString("qualityLevel")),
                        rs.getInt("isSmoking") == 1,
                        rs.getInt("pricePerNight")
                );
                rooms.add(room);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return rooms;
    }

    /**
     * Helper method to convert a database row back into a Room object.
     */
    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        int number = rs.getInt("roomNumber");

        // Convert strings back to Enums
        BedType bed = BedType.valueOf(rs.getString("bedType"));
        Theme theme = Theme.valueOf(rs.getString("theme"));
        QualityLevel quality = QualityLevel.valueOf(rs.getString("qualityLevel"));

        // Convert 1/0 back to boolean
        boolean smoking = rs.getInt("isSmoking") == 1;

        return new Room(number, bed, theme, quality, smoking);
    }
}