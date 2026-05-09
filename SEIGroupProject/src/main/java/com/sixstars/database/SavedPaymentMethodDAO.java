package com.sixstars.database;

import com.sixstars.model.SavedPaymentMethod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SavedPaymentMethodDAO {

    public List<SavedPaymentMethod> findByGuestEmail(String email) {
        String sql = "SELECT id, guest_email, nickname, card_brand, last_four, exp_month, exp_year, name_on_card, "
                + "line1, line2, city, state, zip, phone, created_at "
                + "FROM saved_payment_methods WHERE guest_email = ? ORDER BY created_at DESC";
        List<SavedPaymentMethod> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int insert(SavedPaymentMethod m) throws SQLException {
        String sql = "INSERT INTO saved_payment_methods (guest_email, nickname, card_brand, last_four, exp_month, exp_year, "
                + "name_on_card, line1, line2, city, state, zip, phone, created_at) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            String email = m.getGuestEmail().trim().toLowerCase();
            ps.setString(1, email);
            ps.setString(2, m.getNickname());
            ps.setString(3, m.getCardBrand());
            ps.setString(4, m.getLastFour());
            ps.setInt(5, m.getExpMonth());
            ps.setInt(6, m.getExpYear());
            ps.setString(7, m.getNameOnCard());
            ps.setString(8, m.getLine1());
            ps.setString(9, m.getLine2());
            ps.setString(10, m.getCity());
            ps.setString(11, m.getState());
            ps.setString(12, m.getZip());
            ps.setString(13, m.getPhone());
            ps.setString(14, m.getCreatedAt().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("No generated key for saved_payment_methods");
    }

    public void delete(int id, String guestEmail) throws SQLException {
        String sql = "DELETE FROM saved_payment_methods WHERE id = ? AND guest_email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, guestEmail.trim().toLowerCase());
            ps.executeUpdate();
        }
    }

    private static SavedPaymentMethod mapRow(ResultSet rs) throws SQLException {
        return new SavedPaymentMethod(
                rs.getInt("id"),
                rs.getString("guest_email"),
                rs.getString("nickname"),
                rs.getString("card_brand"),
                rs.getString("last_four"),
                rs.getInt("exp_month"),
                rs.getInt("exp_year"),
                rs.getString("name_on_card"),
                rs.getString("line1"),
                rs.getString("line2"),
                rs.getString("city"),
                rs.getString("state"),
                rs.getString("zip"),
                rs.getString("phone"),
                LocalDateTime.parse(rs.getString("created_at"))
        );
    }
}
