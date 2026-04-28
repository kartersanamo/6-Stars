package com.sixstars.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sixstars.model.Account;

public class AccountDAO {
    public void saveAccount(Account account) {
        String sql = "INSERT OR REPLACE INTO accounts(email, firstName, lastName, passwordHash, role, profileImagePath) VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getEmail());
            pstmt.setString(2, account.getFirstName());
            pstmt.setString(3, account.getLastName());
            pstmt.setString(4, account.getPasswordHash());
            pstmt.setString(5, account.getRole().name());
            pstmt.setString(6, account.getProfileImagePath());
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
                            rs.getString("profileImagePath")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Return null if user isn't found
    }

    public List<Account> getAllAccounts() {
        String sql = "SELECT * FROM accounts";
        List<Account> aList = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Account a = new Account(
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("passwordHash"),
                        com.sixstars.model.Role.valueOf(rs.getString("role")),
                        rs.getString("profileImagePath")
                    );
                    aList.add(a);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return aList;
    }
}