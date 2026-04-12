package com.sixstars.database;

import com.sixstars.model.Reservation;
import com.sixstars.model.Room;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public void saveReservation(Reservation res) {
        String resSql = "INSERT INTO reservations(startDate, endDate) VALUES(?,?)";
        String joinSql = "INSERT INTO reservation_rooms(reservation_id, room_number) VALUES(?,?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert the Reservation and get the generated ID
            try (PreparedStatement pstmt = conn.prepareStatement(resSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, res.getStartDate().toString());
                pstmt.setString(2, res.getEndDate().toString());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    res.setId(generatedId);

                    // 2. Insert each room into the join table
                    try (PreparedStatement joinPstmt = conn.prepareStatement(joinSql)) {
                        for (Room room : res.getRooms()) {
                            joinPstmt.setInt(1, generatedId);
                            joinPstmt.setInt(2, room.getRoomNumber());
                            joinPstmt.addBatch(); // Batching for performance
                        }
                        joinPstmt.executeBatch();
                    }
                }
                conn.commit(); // Save everything
            } catch (SQLException e) {
                conn.rollback(); // Undo if error occurs
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM reservations";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate start = LocalDate.parse(rs.getString("startDate"));
                LocalDate end = LocalDate.parse(rs.getString("endDate"));

                // Fetch the rooms associated with this specific reservation
                List<Room> rooms = getRoomsForReservation(id);

                // Manual ID management: Since your constructor sets ID automatically,
                // you might need a setter or a specific constructor for DB loading.
                Reservation res = new Reservation(start, end, rooms);
                // res.setId(id); // If you add a setter to Reservation.java

                reservations.add(res);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    // Helper method to handle the many-to-many relationship
    private List<Room> getRoomsForReservation(int reservationId) {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.* FROM rooms r " +
                "JOIN reservation_rooms rr ON r.roomNumber = rr.room_number " +
                "WHERE rr.reservation_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(new Room(
                            rs.getInt("roomNumber"),
                            com.sixstars.model.BedType.valueOf(rs.getString("bedType")),
                            com.sixstars.model.Theme.valueOf(rs.getString("theme")),
                            com.sixstars.model.QualityLevel.valueOf(rs.getString("qualityLevel")),
                            rs.getInt("isSmoking") == 1
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public boolean isRoomAvailable(int roomNumber, LocalDate start, LocalDate end) {
        // This query checks if any existing reservation overlaps with the requested dates
        String sql = "SELECT COUNT(*) FROM reservations r " +
                "JOIN reservation_rooms rr ON r.id = rr.reservation_id " +
                "WHERE rr.room_number = ? " +
                "AND ? < r.endDate AND ? > r.startDate";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // If count is 0, the room is free
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}