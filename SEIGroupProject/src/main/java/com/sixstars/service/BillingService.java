package com.sixstars.service;

import com.sixstars.database.ReservationDAO;
import com.sixstars.database.ShopOrderDAO;
import com.sixstars.model.Reservation;
import com.sixstars.model.ShopOrder;

import java.util.List;

public class BillingService {
    private final ReservationDAO reservationDAO;
    private final ShopOrderDAO shopOrderDAO;

    public BillingService() {
        reservationDAO = new ReservationDAO();
        shopOrderDAO = new ShopOrderDAO();
    }

    public List<Reservation> getReservationCharges(String email) {
        return reservationDAO.getReservationsByEmail(email);
    }

    public List<ShopOrder> getShopPurchases(String email) {
        return shopOrderDAO.getOrdersByGuestEmail(email);
    }

    public int getReservationTotal(String email) {
        return reservationDAO.getReservationsByEmail(email)
                .stream()
                .mapToInt(Reservation::getTotalCost)
                .sum();
    }

    public double getShopTotal(String email) {
        return shopOrderDAO.getOrdersByGuestEmail(email)
                .stream()
                .mapToDouble(ShopOrder::getTotalCost)
                .sum();
    }

    public double getGrandTotal(String email) {
        return getReservationTotal(email) + getShopTotal(email);
    }
}