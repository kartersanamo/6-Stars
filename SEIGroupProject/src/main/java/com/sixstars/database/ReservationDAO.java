package com.sixstars.database;

import com.sixstars.model.Reservation;
import com.sixstars.model.Room;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public void saveReservation(Reservation res) {
        String resSql = "INSERT INTO reservations(startDate, endDate, guestEmail, nightlyRate, nights, totalCost) VALUES(?,?,?,?,?,?)";
        String joinSql = "INSERT INTO reservation_rooms(reservation_id, room_number) VALUES(?,?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(resSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, res.getStartDate().toString());
                pstmt.setString(2, res.getEndDate().toString());
                pstmt.setString(3, res.getGuestEmail());
                pstmt.setInt(4, res.getNightlyRate());
                pstmt.setInt(5, res.getNights());
                pstmt.setInt(6, res.getTotalCost());
                pstmt.executeUpdate();

                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    res.setId(generatedId);

                    try (PreparedStatement joinPstmt = conn.prepareStatement(joinSql)) {
                        for (Room room : res.getRooms()) {
                            joinPstmt.setInt(1, generatedId);
                            joinPstmt.setInt(2, room.getRoomNumber());
                            joinPstmt.addBatch();
                        }
                        joinPstmt.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
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
                String email = rs.getString("guestEmail");
                int nightlyRate = rs.getInt("nightlyRate");
                int nights = rs.getInt("nights");
                int totalCost = rs.getInt("totalCost");

                List<Room> rooms = getRoomsForReservation(id);

                Reservation res = new Reservation(email, start, end, rooms, nightlyRate, nights, totalCost);
                res.setId(id);

                reservations.add(res);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

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
                            rs.getInt("isSmoking") == 1,
                            rs.getInt("pricePerNight")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public boolean isRoomAvailable(int roomNumber, LocalDate start, LocalDate end) {
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
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Reservation> getReservationsByEmail(String email) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT * FROM reservations WHERE guestEmail = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                LocalDate start = LocalDate.parse(rs.getString("startDate"));
                LocalDate end = LocalDate.parse(rs.getString("endDate"));
                int nightlyRate = rs.getInt("nightlyRate");
                int nights = rs.getInt("nights");
                int totalCost = rs.getInt("totalCost");

                List<Room> rooms = getRoomsForReservation(id);

                Reservation res = new Reservation(email, start, end, rooms, nightlyRate, nights, totalCost);
                res.setId(id);
                list.add(res);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void cancelReservation(int id) {
        String sqlJoin = "DELETE FROM reservation_rooms WHERE reservation_id = ?";
        String sqlRes = "DELETE FROM reservations WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement p1 = conn.prepareStatement(sqlJoin);
                 PreparedStatement p2 = conn.prepareStatement(sqlRes)) {
                p1.setInt(1, id);
                p1.executeUpdate();
                p2.setInt(1, id);
                p2.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateReservationDates(int id, LocalDate start, LocalDate end) {
        String selectSql = "SELECT nightlyRate FROM reservations WHERE id = ?";
        String updateSql = "UPDATE reservations SET startDate = ?, endDate = ?, nights = ?, totalCost = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            int nightlyRate = 0;

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, id);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    nightlyRate = rs.getInt("nightlyRate");
                }
            }

            int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(start, end);
            if (nights < 0) {
                nights = 0;
            }
            int totalCost = nightlyRate * nights;

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, start.toString());
                updateStmt.setString(2, end.toString());
                updateStmt.setInt(3, nights);
                updateStmt.setInt(4, totalCost);
                updateStmt.setInt(5, id);
                updateStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}