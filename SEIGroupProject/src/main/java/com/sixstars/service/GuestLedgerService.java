package com.sixstars.service;

import com.sixstars.database.GuestPaymentDAO;
import com.sixstars.model.GuestPaymentRecord;
import com.sixstars.model.PaymentKind;
import com.sixstars.model.SavedPaymentMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Charges from billing minus recorded guest payments → amount due. */
public class GuestLedgerService {

    private final BillingService billingService = new BillingService();
    private final GuestPaymentDAO guestPaymentDAO = new GuestPaymentDAO();

    public double getChargesTotal(String guestEmail) {
        return roundMoney(billingService.getGrandTotal(guestEmail));
    }

    public double getPaymentsApplied(String guestEmail) {
        return roundMoney(guestPaymentDAO.sumPaymentsForGuest(guestEmail));
    }

    public double getAmountDue(String guestEmail) {
        double due = getChargesTotal(guestEmail) - getPaymentsApplied(guestEmail);
        return Math.max(0, roundMoney(due));
    }

    public List<GuestPaymentRecord> listPayments(String guestEmail) {
        return guestPaymentDAO.findByGuestEmail(guestEmail);
    }

    public int recordSimulatedSavedCardPayment(String guestEmail, SavedPaymentMethod method, double amount)
            throws java.sql.SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        String summary = method.getDisplayLabel();
        return guestPaymentDAO.insert(guestEmail, roundMoney(amount), PaymentKind.SAVED_CARD_SIMULATED, summary, method.getId());
    }

    public int recordStripeCheckoutPayment(String guestEmail, double amount, String summary) throws java.sql.SQLException {
        return guestPaymentDAO.insert(guestEmail, roundMoney(amount), PaymentKind.STRIPE_CHECKOUT, summary, null);
    }

    private static double roundMoney(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
