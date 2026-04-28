package com.sixstars.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SystemSettingsDAO {

    public String getValue(String key) {
        String sql = "SELECT settingValue FROM system_settings WHERE settingKey = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("settingValue");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void upsertValue(String key, String value) {
        String sql = "INSERT INTO system_settings(settingKey, settingValue) VALUES(?, ?) " +
                "ON CONFLICT(settingKey) DO UPDATE SET settingValue = excluded.settingValue";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
