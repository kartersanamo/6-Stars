package com.sixstars.database;

import com.sixstars.model.ShopOrder;
import com.sixstars.model.ShopOrderItem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShopOrderDAO {

    public void saveOrder(ShopOrder order) {
        String orderSql = "INSERT INTO shop_orders (guestEmail, purchaseDate, totalCost) VALUES (?, ?, ?)";
        String itemSql = "INSERT INTO shop_order_items (order_id, item_id, itemName, unitPrice, quantity, lineTotal) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                orderStmt.setString(1, order.getGuestEmail());
                orderStmt.setString(2, order.getPurchaseDate().toString());
                orderStmt.setDouble(3, order.getTotalCost());
                orderStmt.executeUpdate();

                ResultSet rs = orderStmt.getGeneratedKeys();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    order.setId(orderId);

                    try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                        for (ShopOrderItem item : order.getItems()) {
                            if (item.getItemId() == null) {
                                itemStmt.setNull(2, Types.INTEGER);
                            } else {
                                itemStmt.setInt(2, item.getItemId());
                            }

                            itemStmt.setInt(1, orderId);
                            itemStmt.setString(3, item.getItemName());
                            itemStmt.setDouble(4, item.getUnitPrice());
                            itemStmt.setInt(5, item.getQuantity());
                            itemStmt.setDouble(6, item.getLineTotal());
                            itemStmt.addBatch();
                        }
                        itemStmt.executeBatch();
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

    public List<ShopOrder> getOrdersByGuestEmail(String email) {
        List<ShopOrder> orders = new ArrayList<>();
        String sql = "SELECT * FROM shop_orders WHERE guestEmail = ? ORDER BY purchaseDate DESC, id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int orderId = rs.getInt("id");
                LocalDate purchaseDate = LocalDate.parse(rs.getString("purchaseDate"));
                double totalCost = rs.getDouble("totalCost");
                List<ShopOrderItem> items = getItemsForOrder(orderId);

                ShopOrder order = new ShopOrder(email, purchaseDate, totalCost, items);
                order.setId(orderId);
                orders.add(order);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    private List<ShopOrderItem> getItemsForOrder(int orderId) {
        List<ShopOrderItem> items = new ArrayList<>();
        String sql = "SELECT * FROM shop_order_items WHERE order_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int rawItemId = rs.getInt("item_id");
                Integer itemId = rs.wasNull() ? null : rawItemId;

                ShopOrderItem item = new ShopOrderItem(
                        itemId,
                        rs.getString("itemName"),
                        rs.getDouble("unitPrice"),
                        rs.getInt("quantity")
                );
                item.setId(rs.getInt("id"));
                items.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }
}