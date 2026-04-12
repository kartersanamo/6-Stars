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
        String sql = "INSERT OR REPLACE INTO rooms(roomNumber, bedType, theme, qualityLevel, isSmoking) VALUES(?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, room.getRoomNumber());
            pstmt.setString(2, room.getBedType().name()); // Store enum name as String
            pstmt.setString(3, room.getTheme().name());   // Store enum name as String
            pstmt.setString(4, room.getQualityLevel().name()); // Store enum name as String
            pstmt.setInt(5, room.isSmoking() ? 1 : 0); // SQLite uses 1/0 for booleans

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving room: " + e.getMessage());
        }
    }

    /**
     * Retrieves all rooms currently stored in the database.
     */
    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT * FROM rooms";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving rooms: " + e.getMessage());
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