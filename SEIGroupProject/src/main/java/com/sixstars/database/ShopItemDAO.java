package com.sixstars.database;

import com.sixstars.model.Item;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShopItemDAO {

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM shop_items";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(new Item(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("stock"),
                        rs.getString("imagePath")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public void updateStock(int itemId, int newStock) {
        String sql = "UPDATE shop_items SET stock = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, newStock);
            pstmt.setInt(2, itemId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}