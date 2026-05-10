package com.sixstars.service;

import com.sixstars.database.ShopItemDAO;
import com.sixstars.database.ShopOrderDAO;
import com.sixstars.database.ShoppingCartDAO;
import com.sixstars.model.CartItem;
import com.sixstars.model.Item;
import com.sixstars.model.NotificationType;
import com.sixstars.model.Reservation;
import com.sixstars.model.Role;
import com.sixstars.model.ShopOrder;
import com.sixstars.model.ShopOrderItem;
import com.sixstars.model.ShoppingCart;
import com.sixstars.model.Account;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShopService {

    private final ShopItemDAO dao = new ShopItemDAO();
    private final ShopOrderDAO shopOrderDAO = new ShopOrderDAO();
    private final ShoppingCartDAO shoppingCartDAO = new ShoppingCartDAO();
    private final ReservationService reservationService = new ReservationService();
    private final AccountService accountService = new AccountService();
    private final NotificationService notificationService = NotificationService.getInstance();

    public List<Item> getInventory() {
        return dao.getAllItems();
    }

    /**
     * Persists the order, updates stock, clears the cart, and publishes guest-facing notifications (shop, order status,
     * optional promos) using the real order id and totals.
     */
    public ShopOrder checkout(String guestEmail, ShoppingCart cart) {
        if (guestEmail == null || guestEmail.isBlank()) {
            throw new IllegalStateException("A guest must be logged in to complete checkout.");
        }

        if (!isGuestCurrentlyCheckedIn(guestEmail)) {
            throw new IllegalStateException("Checkout is only available to guests currently checked in.");
        }

        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Your cart is empty.");
        }

        double total = cart.getTotal();
        List<ShopOrderItem> purchasedItems = new ArrayList<>();

        for (CartItem ci : cart.getItems()) {
            Item item = ci.getItem();

            if (ci.getQuantity() > item.getStock()) {
                throw new IllegalStateException("Not enough stock for " + item.getName() + ".");
            }

            ShopOrderItem orderItem = new ShopOrderItem(
                    item.getId(),
                    item.getName(),
                    item.getPrice(),
                    ci.getQuantity()
            );
            purchasedItems.add(orderItem);
        }

        ShopOrder order = new ShopOrder(
                guestEmail,
                LocalDate.now(),
                total,
                purchasedItems
        );

        shopOrderDAO.saveOrder(order);

        for (CartItem ci : cart.getItems()) {
            Item item = ci.getItem();
            int newStock = item.getStock() - ci.getQuantity();
            dao.updateStock(item.getId(), newStock);
            item.setStock(newStock);
            publishLowStockAlertIfNeeded(item, newStock);
        }

        shoppingCartDAO.clearCart(guestEmail);
        cart.clear();

        int orderId = order.getId();
        String money = String.format(java.util.Locale.US, "%.2f", total);
        notificationService.publish(NotificationType.SHOP_PURCHASES, guestEmail,
                "Order #" + orderId + " confirmed. Total $" + money + ".");
        notificationService.publish(NotificationType.ORDER_STATUS, guestEmail,
                "Order #" + orderId + " is being prepared for delivery to your room.");
        notificationService.publish(NotificationType.IN_ROOM_DINING, guestEmail,
                "In-room route: order #" + orderId + " (" + money + " total) — ring service for timing.");
        if (total >= 50.0) {
            notificationService.publish(NotificationType.SHOP_PROMOTIONS, guestEmail,
                    "You unlocked a member-style offer on your next in-stay purchase (order total $" + money + ").");
        }
        if (orderHasSpecialAmenityKeywords(order)) {
            notificationService.publish(NotificationType.SPECIAL_AMENITIES, guestEmail,
                    "We noted celebration or specialty items in order #" + orderId + " — staff can assist with setup.");
        }
        return order;
    }

    private static boolean orderHasSpecialAmenityKeywords(ShopOrder order) {
        for (ShopOrderItem line : order.getItems()) {
            String n = line.getItemName() == null ? "" : line.getItemName().toLowerCase(java.util.Locale.ROOT);
            if (n.contains("champagne") || n.contains("cake") || n.contains("anniversary") || n.contains("birthday")
                    || n.contains("rose")) {
                return true;
            }
        }
        return false;
    }

    private boolean isGuestCurrentlyCheckedIn(String guestEmail) {
        LocalDate today = LocalDate.now();
        List<Reservation> reservations = reservationService.getGuestReservations(guestEmail);

        for (Reservation reservation : reservations) {
            boolean isCheckedIn = "CHECKED_IN".equalsIgnoreCase(reservation.getStatus());
            boolean isDuringStay = !today.isBefore(reservation.getStartDate()) && today.isBefore(reservation.getEndDate());

            if (isCheckedIn && isDuringStay) {
                return true;
            }
        }

        return false;
    }

    private void publishLowStockAlertIfNeeded(Item item, int newStock) {
        if (newStock > 5) {
            return;
        }
        List<Account> staffAccounts = accountService.getAllAccounts().stream()
                .filter(account -> account.getRole() == Role.CLERK || account.getRole() == Role.ADMIN)
                .toList();

        String message = "Low stock alert: " + item.getName() + " has " + newStock + " unit"
                + (newStock == 1 ? "" : "s") + " remaining.";
        for (Account account : staffAccounts) {
            notificationService.publish(NotificationType.SYSTEM_ALERTS, account.getEmail(), message);
        }
    }
}