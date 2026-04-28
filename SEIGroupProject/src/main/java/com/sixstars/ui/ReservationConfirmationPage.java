package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.sixstars.app.Main;
import com.sixstars.controller.AccountController;
import com.sixstars.model.Account;
import com.sixstars.model.Role;
import com.sixstars.model.Room;
import com.sixstars.service.ReservationService;
import com.sixstars.service.RoomService;

public class ReservationConfirmationPage extends JPanel {
    private final JPanel pages;
    private final CardLayout cardLayout;
    private final ReservationService reservationService;
    private final RoomService roomService;

    // Current draft state
    private Room draftRoom;
    private LocalDate draftStartDate;
    private LocalDate draftEndDate;
    private Account draftAccount;
    private boolean draftIsClerkBooking;
    private String draftClerkEmail;

    // UI Components
    private final JLabel roomNumberLabel;
    private final JLabel roomDetailsLabel;
    private final JLabel roomQualityLabel;
    private final JLabel pricePerNightLabel;
    private final JLabel nightsLabel;
    private final JLabel subtotalLabel;
    private final JLabel taxLabel;
    private final JLabel discountLabel;
    private final JLabel resortFeeLabel;
    private final JLabel grandTotalLabel;
    private final JLabel checkInDateLabel;
    private final JLabel checkOutDateLabel;
    private final JLabel bookingForLabel;
    private final JLabel policyTextArea;

    public ReservationConfirmationPage(JPanel pages, CardLayout cardLayout, ReservationService reservationService, RoomService roomService) {
        this.pages = pages;
        this.cardLayout = cardLayout;
        this.reservationService = reservationService;
        this.roomService = roomService;

        setLayout(new BorderLayout());
        setBackground(UITheme.PAGE_BACKGROUND);

        // Initialize UI component labels
        this.roomNumberLabel = new JLabel();
        this.roomDetailsLabel = new JLabel();
        this.roomQualityLabel = new JLabel();
        this.pricePerNightLabel = new JLabel();
        this.nightsLabel = new JLabel();
        this.subtotalLabel = new JLabel();
        this.taxLabel = new JLabel();
        this.discountLabel = new JLabel();
        this.resortFeeLabel = new JLabel();
        this.grandTotalLabel = new JLabel();
        this.checkInDateLabel = new JLabel();
        this.checkOutDateLabel = new JLabel();
        this.bookingForLabel = new JLabel();
        this.policyTextArea = new JLabel();

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildContentArea(), BorderLayout.CENTER);
        add(buildBottomActionBar(), BorderLayout.SOUTH);
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UITheme.CARD_BACKGROUND);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(16, 24, 16, 24)
        ));

        JLabel title = new JLabel("Confirm Your Reservation");
        title.setFont(new Font("Serif", Font.BOLD, 28));
        title.setForeground(UITheme.TEXT_DARK);

        JLabel subtitle = new JLabel("Review all details carefully before confirming your booking");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(UITheme.TEXT_MEDIUM);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(Box.createRigidArea(new Dimension(0, 4)));
        titles.add(subtitle);

        top.add(titles, BorderLayout.WEST);
        return top;
    }

    private JPanel buildContentArea() {
        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setBackground(UITheme.PAGE_BACKGROUND);
        content.setBorder(new EmptyBorder(24, 36, 24, 36));

        content.add(buildLeftPanel());
        content.add(buildRightPanel());

        return content;
    }

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(UITheme.PAGE_BACKGROUND);

        // Room Details Section
        JPanel roomSection = createSectionPanel("Room Details");
        roomNumberLabel.setFont(new Font("Serif", Font.BOLD, 24));
        roomNumberLabel.setForeground(new Color(151, 121, 66));
        roomDetailsLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roomDetailsLabel.setForeground(UITheme.TEXT_MEDIUM);
        roomQualityLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        roomQualityLabel.setForeground(UITheme.TEXT_MEDIUM);

        roomSection.add(roomNumberLabel);
        roomSection.add(Box.createRigidArea(new Dimension(0, 8)));
        roomSection.add(roomDetailsLabel);
        roomSection.add(Box.createRigidArea(new Dimension(0, 4)));
        roomSection.add(roomQualityLabel);

        left.add(roomSection);
        left.add(Box.createRigidArea(new Dimension(0, 20)));

        // Dates Section
        JPanel datesSection = createSectionPanel("Dates & Duration");
        checkInDateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        checkInDateLabel.setForeground(UITheme.TEXT_MEDIUM);
        checkOutDateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        checkOutDateLabel.setForeground(UITheme.TEXT_MEDIUM);
        nightsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nightsLabel.setForeground(UITheme.TEXT_DARK);

        datesSection.add(checkInDateLabel);
        datesSection.add(Box.createRigidArea(new Dimension(0, 4)));
        datesSection.add(checkOutDateLabel);
        datesSection.add(Box.createRigidArea(new Dimension(0, 8)));
        datesSection.add(nightsLabel);

        left.add(datesSection);
        left.add(Box.createRigidArea(new Dimension(0, 20)));

        // Booking For Section
        JPanel bookingSection = createSectionPanel("Booking For");
        bookingForLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bookingForLabel.setForeground(UITheme.TEXT_MEDIUM);
        bookingSection.add(bookingForLabel);

        left.add(bookingSection);
        left.add(Box.createRigidArea(new Dimension(0, 20)));

        // Pricing Section
        JPanel pricingSection = createSectionPanel("Pricing Breakdown");

        JPanel priceRow1 = createPricingRow("Price per night", pricePerNightLabel);
        JPanel priceRow2 = createPricingRow("Nights", nightsLabel);
        JPanel priceRow3 = createPricingRow("Subtotal", subtotalLabel);
        JPanel priceRow4 = createPricingRow("Tax (12%)", taxLabel);
        JPanel priceRow5 = createPricingRow("Discount", discountLabel);
        JPanel priceRow6 = createPricingRow("Resort Fee", resortFeeLabel);

        pricingSection.add(priceRow1);
        pricingSection.add(Box.createRigidArea(new Dimension(0, 6)));
        pricingSection.add(priceRow2);
        pricingSection.add(Box.createRigidArea(new Dimension(0, 6)));
        pricingSection.add(priceRow3);
        pricingSection.add(Box.createRigidArea(new Dimension(0, 8)));
        pricingSection.add(createDivider());
        pricingSection.add(Box.createRigidArea(new Dimension(0, 8)));
        pricingSection.add(priceRow4);
        pricingSection.add(Box.createRigidArea(new Dimension(0, 6)));
        pricingSection.add(priceRow5);
        pricingSection.add(Box.createRigidArea(new Dimension(0, 6)));
        pricingSection.add(priceRow6);
        pricingSection.add(Box.createRigidArea(new Dimension(0, 8)));
        pricingSection.add(createDivider());
        pricingSection.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel grandTotalRow = new JPanel(new BorderLayout());
        grandTotalRow.setOpaque(false);
        JLabel grandTotalLabelText = new JLabel("Grand Total");
        grandTotalLabelText.setFont(new Font("SansSerif", Font.BOLD, 16));
        grandTotalLabelText.setForeground(UITheme.TEXT_DARK);
        grandTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        grandTotalLabel.setForeground(new Color(151, 121, 66));
        grandTotalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        grandTotalRow.add(grandTotalLabelText, BorderLayout.WEST);
        grandTotalRow.add(grandTotalLabel, BorderLayout.EAST);
        pricingSection.add(grandTotalRow);

        left.add(pricingSection);
        left.add(Box.createRigidArea(new Dimension(0, 20)));

        // Cancellation Policy Section
        JPanel policySection = createSectionPanel("Cancellation & Refund Policy");
        policyTextArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        policyTextArea.setForeground(UITheme.TEXT_MEDIUM);
        policyTextArea.setVerticalAlignment(SwingConstants.TOP);

        String policyText = "<html>" +
                "• <b>Free Cancellation:</b> Cancel free within 2 days of check-in date<br>" +
                "• <b>Penalty:</b> After 2 days, 80% of total cost is non-refundable<br>" +
                "• <b>No Changes:</b> Once check-in begins, no cancellations permitted<br>" +
                "• <b>Special Cases:</b> Manager override available for extraordinary circumstances<br>" +
                "</html>";
        policyTextArea.setText(policyText);

        policySection.add(policyTextArea);
        left.add(policySection);
        left.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(left);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UITheme.PAGE_BACKGROUND);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UITheme.PAGE_BACKGROUND);
        wrapper.add(scrollPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(UITheme.CARD_BACKGROUND);
        right.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel summary = new JLabel("Booking Summary");
        summary.setFont(new Font("SansSerif", Font.BOLD, 18));
        summary.setForeground(UITheme.TEXT_DARK);
        right.add(summary);
        right.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel summaryContent = new JPanel();
        summaryContent.setLayout(new BoxLayout(summaryContent, BoxLayout.Y_AXIS));
        summaryContent.setOpaque(false);

        JLabel summaryLine1 = new JLabel();
        summaryLine1.setFont(new Font("SansSerif", Font.PLAIN, 13));
        summaryLine1.setForeground(UITheme.TEXT_MEDIUM);
        summaryContent.add(summaryLine1);
        summaryContent.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel summaryLine2 = new JLabel();
        summaryLine2.setFont(new Font("SansSerif", Font.PLAIN, 13));
        summaryLine2.setForeground(UITheme.TEXT_MEDIUM);
        summaryContent.add(summaryLine2);
        summaryContent.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel summaryLine3 = new JLabel();
        summaryLine3.setFont(new Font("SansSerif", Font.BOLD, 14));
        summaryLine3.setForeground(new Color(151, 121, 66));
        summaryContent.add(summaryLine3);

        right.add(summaryContent);
        right.add(Box.createRigidArea(new Dimension(0, 24)));
        right.add(createDivider());
        right.add(Box.createRigidArea(new Dimension(0, 24)));

        JLabel confirmation = new JLabel("Please review all details carefully");
        confirmation.setFont(new Font("SansSerif", Font.PLAIN, 12));
        confirmation.setForeground(UITheme.TEXT_MEDIUM);
        confirmation.setHorizontalAlignment(SwingConstants.CENTER);
        right.add(confirmation);
        right.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton confirmButton = createPrimaryButton("Confirm & Book");
        confirmButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        confirmButton.addActionListener(e -> confirmReservation());
        right.add(confirmButton);

        right.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton editButton = createSecondaryButton("Edit Dates");
        editButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        editButton.addActionListener(e -> goBackToMakeReservation());
        right.add(editButton);

        right.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton cancelButton = createCancelButton();
        cancelButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        cancelButton.addActionListener(e -> cancelBooking());
        right.add(cancelButton);

        right.add(Box.createVerticalGlue());

        return right;
    }

    private JPanel buildBottomActionBar() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UITheme.CARD_BACKGROUND);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(10, 16, 10, 16)
        ));

        JLabel helperText = new JLabel("All information will be used for your reservation confirmation and billing.");
        helperText.setFont(new Font("SansSerif", Font.PLAIN, 13));
        helperText.setForeground(UITheme.TEXT_MEDIUM);

        JButton backButton = createSecondaryButton("Back to Browse");
        backButton.addActionListener(e -> goBackToMakeReservation());

        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        actions.setOpaque(false);
        actions.add(backButton);

        footer.add(helperText, BorderLayout.CENTER);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    private JPanel createSectionPanel(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setOpaque(false);

        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionTitle.setForeground(UITheme.TEXT_DARK);
        section.add(sectionTitle);
        section.add(Box.createRigidArea(new Dimension(0, 12)));

        return section;
    }

    private JPanel createPricingRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);

        JLabel labelComponent = new JLabel(label);
        labelComponent.setFont(new Font("SansSerif", Font.PLAIN, 13));
        labelComponent.setForeground(UITheme.TEXT_MEDIUM);

        valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        valueLabel.setForeground(UITheme.TEXT_DARK);
        valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        row.add(labelComponent, BorderLayout.WEST);
        row.add(valueLabel, BorderLayout.EAST);
        return row;
    }

    private JPanel createDivider() {
        JPanel divider = new JPanel();
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setBackground(UITheme.BORDER_COLOR);
        divider.setOpaque(true);
        return divider;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UITheme.BUTTON_FONT);
        button.setBackground(UITheme.ACCENT_GOLD);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setBackground(UITheme.SECONDARY_BUTTON);
        button.setForeground(UITheme.TEXT_DARK);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JButton createCancelButton() {
        JButton button = new JButton("Cancel Booking");
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setBackground(new Color(245, 245, 245));
        button.setForeground(new Color(161, 62, 47));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Show the confirmation page with a draft reservation
     */
    public void showDraft(Room room, LocalDate startDate, LocalDate endDate, Account account, boolean isClerkBooking, String clerkEmail) {
        this.draftRoom = room;
        this.draftStartDate = startDate;
        this.draftEndDate = endDate;
        this.draftAccount = account;
        this.draftIsClerkBooking = isClerkBooking;
        this.draftClerkEmail = clerkEmail;

        refreshDisplay();
    }

    private void refreshDisplay() {
        if (draftRoom == null || draftStartDate == null || draftEndDate == null) {
            return;
        }

        // Room details
        roomNumberLabel.setText("Room " + draftRoom.getRoomNumber());
        roomDetailsLabel.setText(
                draftRoom.getBedType() + " bed | Theme: " + draftRoom.getTheme() +
                " | " + (draftRoom.isSmoking() ? "Smoking" : "Non-smoking")
        );
        roomQualityLabel.setText("Quality: " + draftRoom.getQualityLevel());

        // Dates
        DateFormat df = new SimpleDateFormat("MMM d, yyyy");
        checkInDateLabel.setText("Check-in: " + df.format(java.sql.Date.valueOf(draftStartDate)));
        checkOutDateLabel.setText("Check-out: " + df.format(java.sql.Date.valueOf(draftEndDate)));

        // Calculate nights and pricing
        long nights = ChronoUnit.DAYS.between(draftStartDate, draftEndDate);
        int pricePerNight = draftRoom.getPricePerNight();

        nightsLabel.setText(nights + " night" + (nights == 1 ? "" : "s"));
        pricePerNightLabel.setText("$" + pricePerNight);

        // Pricing calculation
        int subtotal = (int) (pricePerNight * nights);
        int tax = (int) (subtotal * 0.12); // 12% tax
        int discount = nights >= 7 ? (int) (subtotal * 0.10) : 0; // 10% discount for 7+ nights
        int resortFee = 25; // $25 resort fee
        int grandTotal = subtotal + tax - discount + resortFee;

        subtotalLabel.setText("$" + subtotal);
        taxLabel.setText("$" + tax);
        discountLabel.setText(discount > 0 ? "-$" + discount : "$0");
        resortFeeLabel.setText("$" + resortFee);
        grandTotalLabel.setText("$" + grandTotal);

        // Booking for
        if (draftIsClerkBooking && draftClerkEmail != null) {
            bookingForLabel.setText("Email: " + draftClerkEmail);
        } else if (draftAccount != null) {
            bookingForLabel.setText("Email: " + draftAccount.getEmail());
        }
    }

    private void confirmReservation() {
        if (draftRoom == null || draftStartDate == null || draftEndDate == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid reservation data. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Validate room is still available
        if (!reservationService.isRoomAvailable(draftRoom, draftStartDate, draftEndDate)) {
            JOptionPane.showMessageDialog(
                    this,
                    "Unfortunately, this room is no longer available for the selected dates. Please select another room.",
                    "Room Unavailable",
                    JOptionPane.ERROR_MESSAGE
            );
            cardLayout.show(pages, "make reservation");
            return;
        }

        // Determine the email to use for the reservation
        String targetEmail;
        if (draftIsClerkBooking && draftClerkEmail != null) {
            targetEmail = draftClerkEmail;
        } else if (draftAccount != null) {
            targetEmail = draftAccount.getEmail();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Unable to determine guest email. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // Make the reservation
        try {
            reservationService.makeReservation(targetEmail, draftStartDate, draftEndDate, List.of(draftRoom));

            long nights = ChronoUnit.DAYS.between(draftStartDate, draftEndDate);
            int total = draftRoom.getPricePerNight() * (int) nights;

            JOptionPane.showMessageDialog(
                    this,
                    "Reservation confirmed for Room " + draftRoom.getRoomNumber() + "!\n" +
                    "Total: $" + total + " for " + nights + " night" + (nights == 1 ? "" : "s") + ".",
                    "Booking Confirmed",
                    JOptionPane.INFORMATION_MESSAGE
            );

            // Clear draft and go back to home
            clearDraft();
            cardLayout.show(pages, "home");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "An error occurred while confirming the reservation: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void goBackToMakeReservation() {
        clearDraft();
        cardLayout.show(pages, "make reservation");
    }

    private void cancelBooking() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel this booking? You will return to the room browse page.",
                "Cancel Booking",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            clearDraft();
            cardLayout.show(pages, "make reservation");
        }
    }

    private void clearDraft() {
        draftRoom = null;
        draftStartDate = null;
        draftEndDate = null;
        draftAccount = null;
        draftIsClerkBooking = false;
        draftClerkEmail = null;
    }
}

