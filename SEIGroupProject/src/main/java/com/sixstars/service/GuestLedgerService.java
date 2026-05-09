package com.sixstars.service;

import com.sixstars.database.GuestPaymentDAO;
import com.sixstars.model.GuestPaymentRecord;
import com.sixstars.model.NotificationType;
import com.sixstars.model.PaymentKind;
import com.sixstars.model.SavedPaymentMethod;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/** Charges from billing minus recorded guest payments → amount due. */
public class GuestLedgerService {

    private final BillingService billingService = new BillingService();
    private final GuestPaymentDAO guestPaymentDAO = new GuestPaymentDAO();
    private final NotificationService notificationService = NotificationService.getInstance();

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
        int id = guestPaymentDAO.insert(guestEmail, roundMoney(amount), PaymentKind.SAVED_CARD_SIMULATED, summary, method.getId());
        publishLedgerNotifications(guestEmail, id, roundMoney(amount), summary);
        return id;
    }

    public int recordStripeCheckoutPayment(String guestEmail, double amount, String summary) throws java.sql.SQLException {
        double rounded = roundMoney(amount);
        int id = guestPaymentDAO.insert(guestEmail, rounded, PaymentKind.STRIPE_CHECKOUT, summary, null);
        publishLedgerNotifications(guestEmail, id, rounded, summary);
        return id;
    }

    private void publishLedgerNotifications(String guestEmail, int paymentId, double amount, String summary) {
        String amt = String.format(Locale.US, "%.2f", amount);
        String safeSummary = summary == null ? "" : summary;
        notificationService.publish(NotificationType.PAYMENTS_AND_CARDS, guestEmail,
                "Payment #" + paymentId + " recorded: $" + amt + " — " + safeSummary);
        notificationService.publish(NotificationType.FOLIO_AND_CHARGES, guestEmail,
                "Folio updated: payment #" + paymentId + " for $" + amt + " is on your account.");
        notificationService.publish(NotificationType.INVOICES_AND_RECEIPTS, guestEmail,
                "Tax receipt (summary): reference payment #" + paymentId + ", amount $" + amt + ".");
    }

    private static double roundMoney(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
