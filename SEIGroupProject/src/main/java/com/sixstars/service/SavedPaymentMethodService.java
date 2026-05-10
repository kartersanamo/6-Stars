package com.sixstars.service;

import com.sixstars.database.SavedPaymentMethodDAO;
import com.sixstars.model.SavedPaymentMethod;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SavedPaymentMethodService {

    private final SavedPaymentMethodDAO dao = new SavedPaymentMethodDAO();

    public List<SavedPaymentMethod> listForGuest(String email) {
        return dao.findByGuestEmail(email);
    }

    public int saveMethod(String guestEmail, String nickname, String cardBrand, String lastFour,
            int expMonth, int expYear, String nameOnCard, String line1, String line2,
            String city, String state, String zip, String phone) throws SQLException {
        SavedPaymentMethod row = new SavedPaymentMethod(
                0,
                guestEmail.trim().toLowerCase(),
                nickname,
                cardBrand,
                lastFour,
                expMonth,
                expYear,
                nameOnCard,
                line1,
                line2 == null ? "" : line2,
                city,
                state,
                zip,
                phone == null ? "" : phone,
                LocalDateTime.now()
        );
        return dao.insert(row);
    }

    public void deleteMethod(int id, String guestEmail) throws SQLException {
        dao.delete(id, guestEmail);
    }
}
