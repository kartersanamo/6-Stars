package com.sixstars.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.sixstars.model.Account;
import com.sixstars.model.Role;

public class AccountDAO {
    public void saveAccount(Account account) {
        Account existing = getAccountByEmail(account.getEmail());
        Boolean emailVerified = account.getEmailVerified() != null
                ? account.getEmailVerified()
                : existing != null ? existing.getEmailVerified() : Boolean.FALSE;
        String verificationCodeHash = account.getVerificationCodeHash() != null
                ? account.getVerificationCodeHash()
                : existing != null ? existing.getVerificationCodeHash() : null;
        String verificationExpiresAt = account.getVerificationExpiresAt() != null
                ? account.getVerificationExpiresAt()
                : existing != null ? existing.getVerificationExpiresAt() : null;

        String sql = "INSERT OR REPLACE INTO accounts(email, firstName, lastName, passwordHash, role, email_verified, verification_code_hash, verification_expires_at, profileImagePath) VALUES(?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, account.getEmail());
            pstmt.setString(2, account.getFirstName());
            pstmt.setString(3, account.getLastName());
            pstmt.setString(4, account.getPasswordHash());
            pstmt.setString(5, account.getRole().name());
            pstmt.setInt(6, Boolean.TRUE.equals(emailVerified) ? 1 : 0);
            pstmt.setString(7, verificationCodeHash);
            pstmt.setString(8, verificationExpiresAt);
            pstmt.setString(9, account.getProfileImagePath());
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
                            Role.valueOf(rs.getString("role")),
                            rs.getInt("email_verified") == 1,
                            rs.getString("verification_code_hash"),
                            rs.getString("verification_expires_at"),
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
                        Role.valueOf(rs.getString("role")),
                        rs.getInt("email_verified") == 1,
                        rs.getString("verification_code_hash"),
                        rs.getString("verification_expires_at"),
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

    public void updateVerificationState(String email, boolean verified, String verificationCodeHash, String verificationExpiresAt) {
        String sql = "UPDATE accounts SET email_verified = ?, verification_code_hash = ?, verification_expires_at = ? WHERE email = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, verified ? 1 : 0);
            pstmt.setString(2, verificationCodeHash);
            pstmt.setString(3, verificationExpiresAt);
            pstmt.setString(4, email);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}