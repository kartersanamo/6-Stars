package com.sixstars.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
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
import javax.swing.ImageIcon;
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
    private static final String ROOM_IMAGE_PATH = "assets/6Stars-Room.jpg";

    private final JPanel pages;
    private final CardLayout cardLayout;
    private final ReservationService reservationService;
    private final RoomService roomService;
    private final Image roomImage;

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
        this.roomImage = loadImage(ROOM_IMAGE_PATH);

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
        top.setBackground(new Color(245, 242, 235));
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 180, 140)),
                new EmptyBorder(18, 24, 18, 24)
        ));

        JLabel title = new JLabel("Confirm Your Reservation");
        title.setFont(new Font("Serif", Font.BOLD, 32));
        title.setForeground(new Color(70, 50, 35));

        JLabel subtitle = new JLabel("Review all details carefully before confirming your booking");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(100, 100, 100));

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
        BackgroundImagePanel backgroundPanel = new BackgroundImagePanel(roomImage);
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        JPanel overlay = new JPanel(new BorderLayout(22, 0));
        overlay.setOpaque(false);

        JPanel details = buildDetailsPanel();
        details.setPreferredSize(new Dimension(520, 0));

        JPanel summary = buildSummaryPanel();
        summary.setPreferredSize(new Dimension(390, 0));

        overlay.add(details, BorderLayout.CENTER);
        overlay.add(summary, BorderLayout.EAST);

        backgroundPanel.add(overlay, BorderLayout.CENTER);
        return backgroundPanel;
    }

    private JPanel buildDetailsPanel() {
        JPanel details = new JPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.setOpaque(true);
        details.setBackground(new Color(18, 18, 18, 150));
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 70), 1),
                new EmptyBorder(18, 18, 18, 18)
        ));

        // Room Details Section
        JPanel roomSection = createSectionPanel("Room Details");
        roomNumberLabel.setFont(new Font("Serif", Font.BOLD, 26));
        roomNumberLabel.setForeground(new Color(232, 199, 129));
        roomDetailsLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roomDetailsLabel.setForeground(new Color(236, 236, 236));
        roomQualityLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        roomQualityLabel.setForeground(new Color(236, 236, 236));

        roomSection.add(roomNumberLabel);
        roomSection.add(Box.createRigidArea(new Dimension(0, 8)));
        roomSection.add(roomDetailsLabel);
        roomSection.add(Box.createRigidArea(new Dimension(0, 4)));
        roomSection.add(roomQualityLabel);

        details.add(roomSection);
        details.add(Box.createRigidArea(new Dimension(0, 16)));

        // Dates Section
        JPanel datesSection = createSectionPanel("Check-In & Check-Out");
        checkInDateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        checkInDateLabel.setForeground(new Color(236, 236, 236));
        checkOutDateLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        checkOutDateLabel.setForeground(new Color(236, 236, 236));
        nightsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nightsLabel.setForeground(new Color(181, 227, 188));

        datesSection.add(checkInDateLabel);
        datesSection.add(Box.createRigidArea(new Dimension(0, 4)));
        datesSection.add(checkOutDateLabel);
        datesSection.add(Box.createRigidArea(new Dimension(0, 8)));
        datesSection.add(nightsLabel);

        details.add(datesSection);
        details.add(Box.createRigidArea(new Dimension(0, 16)));

        // Booking For Section
        JPanel bookingSection = createSectionPanel("Booking For");
        bookingForLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        bookingForLabel.setForeground(new Color(236, 236, 236));
        bookingSection.add(bookingForLabel);

        details.add(bookingSection);
        details.add(Box.createVerticalGlue());

        return details;
    }

    private JPanel buildSummaryPanel() {
        JPanel summaryCard = new JPanel(new BorderLayout());
        summaryCard.setBackground(UITheme.CARD_BACKGROUND);
        summaryCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 180, 140), 2),
                new EmptyBorder(0, 0, 0, 0)
        ));

        JPanel summary = new JPanel();
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        summary.setBackground(UITheme.CARD_BACKGROUND);
        summary.setBorder(new EmptyBorder(22, 20, 22, 20));

        JLabel title = new JLabel("Pricing & Summary");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(new Color(151, 121, 66));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        summary.add(title);
        summary.add(Box.createRigidArea(new Dimension(0, 14)));

        // Pricing rows
        summary.add(createPricingRow("Price per night", pricePerNightLabel));
        summary.add(Box.createRigidArea(new Dimension(0, 6)));
        summary.add(createPricingRow("Nights", nightsLabel));
        summary.add(Box.createRigidArea(new Dimension(0, 6)));
        summary.add(createPricingRow("Subtotal", subtotalLabel));
        summary.add(Box.createRigidArea(new Dimension(0, 10)));

        summary.add(createDivider());
        summary.add(Box.createRigidArea(new Dimension(0, 10)));

        summary.add(createPricingRow("Tax (12%)", taxLabel));
        summary.add(Box.createRigidArea(new Dimension(0, 6)));
        summary.add(createPricingRow("Discount", discountLabel));
        summary.add(Box.createRigidArea(new Dimension(0, 6)));
        summary.add(createPricingRow("Resort Fee", resortFeeLabel));
        summary.add(Box.createRigidArea(new Dimension(0, 10)));

        summary.add(createDivider());
        summary.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel grandTotalRow = new JPanel(new BorderLayout());
        grandTotalRow.setOpaque(false);
        JLabel grandTotalText = new JLabel("Grand Total");
        grandTotalText.setFont(new Font("SansSerif", Font.BOLD, 16));
        grandTotalText.setForeground(UITheme.TEXT_DARK);
        grandTotalLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        grandTotalLabel.setForeground(new Color(151, 121, 66));
        grandTotalLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        grandTotalRow.add(grandTotalText, BorderLayout.WEST);
        grandTotalRow.add(grandTotalLabel, BorderLayout.EAST);
        summary.add(grandTotalRow);

        summary.add(Box.createRigidArea(new Dimension(0, 16)));

        // Full-width policy preview section under grand total
        JPanel policyBlock = new JPanel();
        policyBlock.setLayout(new BoxLayout(policyBlock, BoxLayout.Y_AXIS));
        policyBlock.setOpaque(false);
        policyBlock.setAlignmentX(LEFT_ALIGNMENT);
        policyBlock.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        policyBlock.setPreferredSize(new Dimension(10, 90));

        JLabel policyTitle = new JLabel("Cancellation Policy");
        policyTitle.setFont(new Font("SansSerif", Font.BOLD, 12));
        policyTitle.setForeground(new Color(161, 62, 47));
        policyTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        policyTitle.setHorizontalAlignment(SwingConstants.LEFT);

        JLabel policyPreview = new JLabel("<html>Cancel free within 2 days.<br>After: 80% non-refundable.</html>");
        policyPreview.setFont(new Font("SansSerif", Font.PLAIN, 11));
        policyPreview.setForeground(UITheme.TEXT_MEDIUM);
        policyPreview.setAlignmentX(Component.LEFT_ALIGNMENT);
        policyPreview.setHorizontalAlignment(SwingConstants.LEFT);

        policyBlock.add(policyTitle);
        policyBlock.add(Box.createRigidArea(new Dimension(0, 6)));
        policyBlock.add(policyPreview);
        summary.add(policyBlock);

        summary.add(Box.createRigidArea(new Dimension(0, 18)));

        JButton confirmButton = createPrimaryButton("Confirm & Book");
        confirmButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        confirmButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        confirmButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        confirmButton.addActionListener(e -> confirmReservation());
        summary.add(confirmButton);

        summary.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton editButton = createSecondaryButton("Edit Dates");
        editButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        editButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        editButton.addActionListener(e -> goBackToMakeReservation());
        summary.add(editButton);

        summary.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton cancelButton = createCancelButton();
        cancelButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        cancelButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        cancelButton.addActionListener(e -> cancelBooking());
        summary.add(cancelButton);

        JScrollPane scrollPane = new JScrollPane(summary);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(UITheme.CARD_BACKGROUND);

        summaryCard.add(scrollPane, BorderLayout.CENTER);
        return summaryCard;
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
        section.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionTitle = new JLabel(title);
        sectionTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        sectionTitle.setForeground(new Color(248, 248, 248));
        sectionTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(sectionTitle);
        section.add(Box.createRigidArea(new Dimension(0, 12)));

        return section;
    }

    private JPanel createPricingRow(String label, JLabel valueLabel) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

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
        divider.setAlignmentX(Component.LEFT_ALIGNMENT);
        divider.setPreferredSize(new Dimension(1, 1));
        divider.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        divider.setBackground(UITheme.BORDER_COLOR);
        divider.setOpaque(true);
        return divider;
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setBackground(new Color(151, 121, 66));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        return button;
    }

    private JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setBackground(new Color(240, 240, 240));
        button.setForeground(new Color(70, 50, 35));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 180, 140), 1));
        return button;
    }

    private JButton createCancelButton() {
        JButton button = new JButton("Cancel Booking");
        button.setFont(new Font("SansSerif", Font.PLAIN, 13));
        button.setBackground(new Color(255, 250, 245));
        button.setForeground(new Color(161, 62, 47));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createLineBorder(new Color(200, 150, 150), 1));
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

    private Image loadImage(String relativePath) {
        ImageIcon icon = new ImageIcon(relativePath);
        if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) {
            return icon.getImage();
        }

        ImageIcon fallbackIcon = new ImageIcon("SEIGroupProject/" + relativePath);
        if (fallbackIcon.getIconWidth() > 0 && fallbackIcon.getIconHeight() > 0) {
            return fallbackIcon.getImage();
        }
        return null;
    }

    private static class BackgroundImagePanel extends JPanel {
        private final Image image;

        BackgroundImagePanel(Image image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                int panelW = getWidth();
                int panelH = getHeight();
                int imageW = image.getWidth(this);
                int imageH = image.getHeight(this);

                if (imageW > 0 && imageH > 0 && panelW > 0 && panelH > 0) {
                    // Cover fit: keep aspect ratio and crop overflow to avoid stretching.
                    double scale = Math.max((double) panelW / imageW, (double) panelH / imageH);
                    int drawW = (int) Math.round(imageW * scale);
                    int drawH = (int) Math.round(imageH * scale);
                    int drawX = (panelW - drawW) / 2;
                    int drawY = (panelH - drawH) / 2;
                    g2.drawImage(image, drawX, drawY, drawW, drawH, this);
                }

                // Darken the background so overlay cards/text are readable.
                g2.setColor(new Color(0, 0, 0, 96));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        }
    }
}

