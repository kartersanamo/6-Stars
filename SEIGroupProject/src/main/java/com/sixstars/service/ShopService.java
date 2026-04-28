package com.sixstars.service;

import com.sixstars.database.ShopItemDAO;
import com.sixstars.database.ShopOrderDAO;
import com.sixstars.database.ShoppingCartDAO;
import com.sixstars.model.CartItem;
import com.sixstars.model.Item;
import com.sixstars.model.Reservation;
import com.sixstars.model.ShopOrder;
import com.sixstars.model.ShopOrderItem;
import com.sixstars.model.ShoppingCart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ShopService {

    private final ShopItemDAO dao = new ShopItemDAO();
    private final ShopOrderDAO shopOrderDAO = new ShopOrderDAO();
    private final ShoppingCartDAO shoppingCartDAO = new ShoppingCartDAO();
    private final ReservationService reservationService = new ReservationService();

    public List<Item> getInventory() {
        List<Item> available = new ArrayList<>();

        for (Item item : dao.getAllItems()) {
            if (item.getStock() > 0) {
                available.add(item);
            }
        }

        return available;
    }

    public double checkout(String guestEmail, ShoppingCart cart) {
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
        }

        shoppingCartDAO.clearCart(guestEmail);
        cart.clear();
        return total;
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
}