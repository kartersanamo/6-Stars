package com.sixstars.service.stripe;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

/** Reads Checkout Session payloads after redirects (sandbox / local testing). */
public final class StripeCheckoutSessionReader {

    private StripeCheckoutSessionReader() {
    }

    public static StripeCheckoutSummary read(String checkoutSessionId) throws StripeException {
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) {
            Stripe.apiKey = StripeConfig.getSecretKey();
        }
        Session s = Session.retrieve(checkoutSessionId);
        String customerId = s.getCustomer();
        String mode = s.getMode();
        Long amountTotal = s.getAmountTotal();
        String paymentStatus = s.getPaymentStatus();
        return new StripeCheckoutSummary(customerId, mode,
                amountTotal == null ? 0 : amountTotal, paymentStatus);
    }

    public record StripeCheckoutSummary(String stripeCustomerId, String mode,
            long amountTotalUsdCents, String paymentStatus) {

        /** Returns true once the guest completed Hosted Checkout for saving a card/setup intent. */
        public boolean suggestsSetupSucceeded() {
            return "setup".equalsIgnoreCase(mode);
        }

        /** Returns true once a one-time Checkout payment finalized. */
        public boolean suggestsPaymentSucceeded() {
            return "payment".equalsIgnoreCase(mode) && "paid".equalsIgnoreCase(paymentStatus);
        }
    }
}
