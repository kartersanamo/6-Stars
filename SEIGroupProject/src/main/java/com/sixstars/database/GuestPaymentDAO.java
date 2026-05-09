package com.sixstars.database;

import com.sixstars.model.GuestPaymentRecord;
import com.sixstars.model.PaymentKind;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GuestPaymentDAO {

    public double sumPaymentsForGuest(String email) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM guest_payments WHERE guest_email = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<GuestPaymentRecord> findByGuestEmail(String email) {
        String sql = "SELECT id, guest_email, amount, payment_kind, method_summary, saved_method_id, created_at "
                + "FROM guest_payments WHERE guest_email = ? ORDER BY created_at DESC";
        List<GuestPaymentRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int sid = rs.getInt("saved_method_id");
                    Integer savedId = rs.wasNull() ? null : sid;
                    list.add(new GuestPaymentRecord(
                            rs.getInt("id"),
                            rs.getString("guest_email"),
                            rs.getDouble("amount"),
                            PaymentKind.fromStored(rs.getString("payment_kind")),
                            rs.getString("method_summary"),
                            savedId,
                            LocalDateTime.parse(rs.getString("created_at"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int insert(String guestEmail, double amount, PaymentKind kind, String methodSummary, Integer savedMethodId)
            throws SQLException {
        String sql = "INSERT INTO guest_payments (guest_email, amount, payment_kind, method_summary, saved_method_id, created_at) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, guestEmail.trim().toLowerCase());
            ps.setDouble(2, amount);
            ps.setString(3, kind.name());
            ps.setString(4, methodSummary);
            if (savedMethodId == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, savedMethodId);
            }
            ps.setString(6, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("guest_payments insert failed");
    }
}
