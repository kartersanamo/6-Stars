package com.sixstars.service.stripe;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.sixstars.model.Reservation;
import com.sixstars.model.ShopOrder;
import com.sixstars.model.ShopOrderItem;
import com.sixstars.service.BillingService;

/** Creates Stripe-hosted Checkout Sessions (test mode recommended) backed by BillingService aggregates. */
public final class StripeCheckoutService {

    public static final int MIN_CHARGE_US_CENTS = 50;

    private final BillingService billingService = new BillingService();

    public StripeCheckoutService() {
        Stripe.apiKey = StripeConfig.getSecretKey();
    }

    /** Full guest balance as separate line items in Checkout (shows in Stripe Dashboard). */
    public SessionCreateResult createCheckoutSessionPayFullGuestBill(String guestEmail,
            String successUrlMustIncludeSessionMacro, String cancelUrl) throws StripeException {
        configureKeyIfAbsent();

        List<Reservation> reservations = billingService.getReservationCharges(guestEmail);
        List<ShopOrder> shopOrders = billingService.getShopPurchases(guestEmail);

        List<SessionCreateParams.LineItem> lineItems = new ArrayList<>();

        for (Reservation reservation : reservations) {
            boolean cancelled = "CANCELLED".equalsIgnoreCase(reservation.getStatus());
            String roomText = reservation.getRooms() == null ? "Reservation"
                    : reservation.getRooms().stream()
                    .map(r -> "Room " + r.getRoomNumber())
                    .collect(Collectors.joining(", "));
            String labelPrefix = cancelled ? "Cancellation penalty" : "Room stay";
            String name = labelPrefix + " — " + roomText + " (" + reservation.getStartDate() + " → " + reservation.getEndDate() + ")";
            long cents = dollarsToUsdCentsRounded(reservation.getTotalCost(), true);
            if (cents > 0) {
                lineItems.add(buildLineUsd(name, truncateName(name), cents));
            }
        }

        for (ShopOrder order : shopOrders) {
            if (order.getItems() == null) {
                continue;
            }
            for (ShopOrderItem item : order.getItems()) {
                long centsEach = dollarsToUsdCentsRounded(item.getUnitPrice(), false);
                long qtyLong = Math.max(1, item.getQuantity());
                BigDecimal qty = BigDecimal.valueOf(qtyLong);
                if (centsEach <= 0) {
                    continue;
                }
                long lineAmount = qty.multiply(BigDecimal.valueOf(centsEach)).longValueExact();
                if (lineAmount > 0) {
                    String name = "Shop — " + item.getItemName() + " ×" + qtyLong + " · " + order.getPurchaseDate();
                    lineItems.add(buildLineUsdUsdQuantity(name, truncateName(name), centsEach, qtyLong));
                }
            }
            if ((order.getItems() == null || order.getItems().isEmpty()) && order.getTotalCost() > 0) {
                String name = "Shop order #" + order.getId() + " · " + order.getPurchaseDate();
                long cents = dollarsToUsdCentsRounded(order.getTotalCost(), false);
                lineItems.add(buildLineUsd(name, truncateName(name), cents));
            }
        }

        if (lineItems.isEmpty()) {
            return SessionCreateResult.fail("Nothing to bill — add reservations or purchases first.");
        }

        long estimatedTotal = lineTotalCents(lineItems);

        BigDecimal bdFromService = BigDecimal.valueOf(billingService.getGrandTotal(guestEmail))
                .setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal bdFromStripe = BigDecimal.valueOf(estimatedTotal).divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY);

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                .setSuccessUrl(successUrlMustIncludeSessionMacro)
                .setCancelUrl(cancelUrl)
                .setCustomerEmail(guestEmail.trim().toLowerCase(Locale.ROOT))
                .setAllowPromotionCodes(Boolean.TRUE);

        appendCustomer(builder, guestEmail);

        SessionCreateParams params = builder
                .addAllLineItem(lineItems)
                .putMetadata("guest_email", guestEmail.trim().toLowerCase(Locale.ROOT))
                .putMetadata("estimated_total_usd_service", bdFromService.toPlainString())
                .putMetadata("estimated_total_usd_stripe_parts", bdFromStripe.toPlainString())
                .build();

        if (estimatedTotal < MIN_CHARGE_US_CENTS && estimatedTotal > 0) {
            return SessionCreateResult.fail(
                    "Stripe requires at least $" + MIN_CHARGE_US_CENTS / 100.0 + " USD — add more charges.");
        }

        Session session = Session.create(params);

        BigDecimal discrepancy = bdFromStripe.subtract(bdFromService).abs();
        if (discrepancy.compareTo(new BigDecimal("2.00")) > 0) {
            return SessionCreateResult.fail("Totals could not line up cleanly for Stripe Checkout. "
                    + "App total $" + bdFromService + ", built line-items $" + bdFromStripe + ".");
        }

        return SessionCreateResult.ok(session.getUrl(), session.getId(), estimatedTotal);
    }

    /** Saved card path — card details captured only on Stripe; we store customer id afterward. */
    public SessionCreateResult createCheckoutSessionAttachPaymentMethodSetup(String guestEmail,
            String successUrlMustIncludeSessionMacro, String cancelUrl) throws StripeException {
        configureKeyIfAbsent();

        SessionCreateParams.Builder builder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SETUP)
                .setSuccessUrl(successUrlMustIncludeSessionMacro)
                .setCancelUrl(cancelUrl)
                .setCustomerCreation(SessionCreateParams.CustomerCreation.IF_REQUIRED);

        SessionCreateParams params = appendCustomer(builder, guestEmail)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .setCustomerEmail(guestEmail.trim().toLowerCase(Locale.ROOT))
                .putMetadata("guest_email", guestEmail.trim().toLowerCase(Locale.ROOT))
                .build();

        Session session = Session.create(params);
        return SessionCreateResult.ok(session.getUrl(), session.getId(), 0);
    }

    private static SessionCreateParams.Builder appendCustomer(SessionCreateParams.Builder builder, String guestEmail) {
        String existing = StripeGuestPreferences.getStripeCustomerId(guestEmail);
        if (existing != null && !existing.isBlank()) {
            return builder.setCustomer(existing);
        }
        return builder;
    }

    private static void configureKeyIfAbsent() {
        if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) {
            Stripe.apiKey = StripeConfig.getSecretKey();
        }
    }

    private static long dollarsToUsdCentsRounded(double dollars, boolean isIntegerReservationDollars) {
        BigDecimal bd;
        if (isIntegerReservationDollars) {
            bd = BigDecimal.valueOf(Math.max(0, (long) dollars));
        } else {
            bd = BigDecimal.valueOf(dollars).setScale(2, RoundingMode.HALF_EVEN);
        }
        bd = bd.movePointRight(2).setScale(0, RoundingMode.HALF_EVEN);
        return bd.longValueExact();
    }

    private static SessionCreateParams.LineItem buildLineUsd(String productNameFull, String productNameShort,
            long amountUsdCents) {
        SessionCreateParams.LineItem.PriceData.ProductData prod = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setDescription(productNameFull)
                .setName(productNameShort)
                .build();
        return SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(amountUsdCents)
                        .setProductData(prod)
                        .build())
                .build();
    }

    private static SessionCreateParams.LineItem buildLineUsdUsdQuantity(String productNameFull, String productNameShort,
            long unitAmountUsdCents, long quantity) {
        SessionCreateParams.LineItem.PriceData.ProductData prod = SessionCreateParams.LineItem.PriceData.ProductData.builder()
                .setDescription(productNameFull)
                .setName(productNameShort)
                .build();
        return SessionCreateParams.LineItem.builder()
                .setQuantity(quantity)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                        .setCurrency("usd")
                        .setUnitAmount(unitAmountUsdCents)
                        .setProductData(prod)
                        .build())
                .build();
    }

    /** Stripe product/name length guard. */
    private static String truncateName(String name) {
        if (name.length() <= 120) {
            return name;
        }
        return name.substring(0, 117) + "...";
    }

    private static long lineTotalCents(List<SessionCreateParams.LineItem> lineItems) {
        long sum = 0;
        for (SessionCreateParams.LineItem item : lineItems) {
            if (item == null || item.getQuantity() == null || item.getPriceData() == null
                    || item.getPriceData().getUnitAmount() == null) {
                continue;
            }
            sum += Math.multiplyExact(item.getQuantity(), item.getPriceData().getUnitAmount());
        }
        return sum;
    }

    public record SessionCreateResult(boolean success, String message, String url, String checkoutSessionId, long centsTotal) {

        public static SessionCreateResult ok(String url, String id, long cents) {
            return new SessionCreateResult(true, "", url, id, cents);
        }

        public static SessionCreateResult fail(String msg) {
            return new SessionCreateResult(false, msg, "", "", 0);
        }
    }
}
