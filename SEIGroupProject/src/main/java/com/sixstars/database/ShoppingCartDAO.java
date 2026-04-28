package com.sixstars.database;

import com.sixstars.model.CartItem;
import com.sixstars.model.Item;
import com.sixstars.model.ShoppingCart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ShoppingCartDAO {

    public List<CartItem> loadCart(String guestEmail) {
        List<CartItem> items = new ArrayList<>();
        if (guestEmail == null || guestEmail.isBlank()) {
            return items;
        }

        String sql = "SELECT sci.quantity, si.id, si.name, si.price, si.stock, si.imagePath " +
                "FROM shop_cart_items sci " +
                "JOIN shop_items si ON si.id = sci.item_id " +
                "WHERE sci.guestEmail = ? " +
                "ORDER BY si.name";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, guestEmail.trim().toLowerCase());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int quantity = Math.max(1, rs.getInt("quantity"));
                    Item item = new Item(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("stock"),
                            rs.getString("imagePath")
                    );
                    items.add(new CartItem(item, quantity));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    public void saveCart(String guestEmail, ShoppingCart cart) {
        if (guestEmail == null || guestEmail.isBlank()) {
            return;
        }

        String normalizedEmail = guestEmail.trim().toLowerCase();
        List<CartItem> items = cart == null ? List.of() : cart.getItems();

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                clearCart(conn, normalizedEmail);

                if (items != null && !items.isEmpty()) {
                    String insertSql = "INSERT INTO shop_cart_items (guestEmail, item_id, quantity) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                        for (CartItem item : items) {
                            if (item == null || item.getItem() == null || item.getQuantity() < 1) {
                                continue;
                            }
                            pstmt.setString(1, normalizedEmail);
                            pstmt.setInt(2, item.getItem().getId());
                            pstmt.setInt(3, item.getQuantity());
                            pstmt.addBatch();
                        }
                        pstmt.executeBatch();
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

    public void clearCart(String guestEmail) {
        if (guestEmail == null || guestEmail.isBlank()) {
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            clearCart(conn, guestEmail.trim().toLowerCase());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearCart(Connection conn, String guestEmail) throws SQLException {
        String sql = "DELETE FROM shop_cart_items WHERE guestEmail = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, guestEmail);
            pstmt.executeUpdate();
        }
    }
}
