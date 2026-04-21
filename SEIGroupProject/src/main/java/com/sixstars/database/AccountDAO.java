package com.sixstars.database;

import com.sixstars.model.Account;
import java.sql.*;

public class AccountDAO {
    public void saveAccount(Account account) {
        String sql = "INSERT OR REPLACE INTO accounts(email, firstName, lastName, passwordHash, role, totalBill) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getEmail());
            pstmt.setString(2, account.getFirstName());
            pstmt.setString(3, account.getLastName());
            pstmt.setString(4, account.getPasswordHash());
            pstmt.setString(5, account.getRole().name());
            pstmt.setDouble(6, account.getTotalBill());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Account getAccountByEmail(String email) {
        String sql = "SELECT * FROM accounts WHERE email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Account(
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("email"),
                            rs.getString("passwordHash"),
                            com.sixstars.model.Role.valueOf(rs.getString("role")),
                            rs.getDouble("totalBill")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if user isn't found
    }
}