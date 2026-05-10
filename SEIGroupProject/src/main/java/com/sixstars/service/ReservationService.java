package com.sixstars.service;

import com.sixstars.database.ReservationDAO;
import com.sixstars.database.RoomDAO;
import com.sixstars.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationService {
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;
    private final NotificationService notificationService;

    public ReservationService() {
        reservationDAO = new ReservationDAO();
        roomDAO = new RoomDAO();
        notificationService = NotificationService.getInstance();
    }

    public List<Room> filterAvailableRooms(LocalDate start, LocalDate end, BedType type, Theme theme, QualityLevel quality) {
        List<Room> allRooms = roomDAO.getAllRooms();
        return allRooms.stream()
                .filter(room -> type == null || room.getBedType() == type)
                .filter(room -> theme == null || room.getTheme() == theme)
                .filter(room -> quality == null || room.getQualityLevel() == quality)
                .filter(room -> isRoomAvailable(room, start, end))
                .collect(Collectors.toList());
    }

    public boolean isRoomAvailable(Room room, LocalDate start, LocalDate end) {
        if (room == null || start == null || end == null) return false;
        return reservationDAO.isRoomAvailable(room.getRoomNumber(), start, end);
    }

    /**
     * Logic to finalize and save a booking.
     */
    public Reservation makeReservation(String guestEmail, LocalDate start, LocalDate end, List<Room> selectedRooms) {
        // 1. Validate availability (unchanged)
        for (Room r : selectedRooms) {
            if (!reservationDAO.isRoomAvailable(r.getRoomNumber(), start, end)) {
                throw new IllegalStateException("Room " + r.getRoomNumber() + " is already booked for these dates.");
            }
        }

        // 2. Calculate pricing (NEW LOGIC)
        int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(start, end);
        if (nights < 0) {
            nights = 0;
        }

        int nightlyRate = 0;
        if (selectedRooms != null && !selectedRooms.isEmpty()) {
            // Assuming 1 room per reservation (matches your current UI)
            nightlyRate = selectedRooms.get(0).getPricePerNight();
        }

        int totalCost = nightlyRate * nights;

        // 3. Create reservation (same constructor, but now we override values)
        Reservation newBooking = new Reservation(guestEmail, start, end, selectedRooms, "BOOKED");

        // 4. Explicitly set billing fields (IMPORTANT)
        newBooking.setNightlyRate(nightlyRate);
        newBooking.setNights(nights);
        newBooking.setTotalCost(totalCost);

        // 5. Save
        reservationDAO.saveReservation(newBooking);
        notificationService.publish(NotificationType.RESERVATION_CONFIRMED, guestEmail,
                "You're booked " + start + " → " + end + ". Check reminders for pre-arrival details.");

        String roomLine = selectedRooms.stream()
                .map(r -> "Room " + r.getRoomNumber())
                .collect(Collectors.joining(", "));
        notificationService.publish(NotificationType.PRE_ARRIVAL_PREFERENCES, guestEmail,
                "Pre-arrival: reserved " + roomLine + ". Mention bedding, ADA, or arrival time requests at check-in.");
        notificationService.publish(NotificationType.RESERVATION_REMINDERS, guestEmail,
                "Reminder: check-in " + start + ", check-out " + end + ".");
        notificationService.publish(NotificationType.TRANSPORT_AND_PARKING, guestEmail,
                "Parking: valet and self-park are available — ask the desk for overnight rates and directions.");
        DayOfWeek dow = start.getDayOfWeek();
        if (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY) {
            notificationService.publish(NotificationType.LOCAL_EVENTS, guestEmail,
                    "Weekend stay: expect busier dining and local traffic — plan airport or event transfers early.");
        }
        if (totalCost >= 200) {
            notificationService.publish(NotificationType.LOYALTY_AND_POINTS, guestEmail,
                    "Loyalty: this stay total is $" + totalCost + " — eligible stays count toward seasonal tier perks.");
        }
        List<Reservation> guestHistory = reservationDAO.getReservationsByEmail(guestEmail);
        if (guestHistory.size() == 1) {
            notificationService.publish(NotificationType.PERSONALIZED_OFFERS, guestEmail,
                    "Welcome — your first booking is on file. Watch this inbox for tailored offers on future visits.");
        }

        return newBooking;
    }

    public List<Reservation> getAllReservations() {
        return reservationDAO.getAllReservations();
    }

    // Gets all reservations belonging to specific email
    public List<Reservation> getGuestReservations(String email) {
        return reservationDAO.getReservationsByEmail(email);
    }

    // Cancels booking by id
    public String cancelBooking(int id) {
        Reservation res = reservationDAO.getAllReservations().stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        return cancelWithPenalty(res);
    }

    // Updates existing reservation
    public void updateReservation(int id, LocalDate start, LocalDate end, List<Room> rooms) {
        Reservation existingReservation = reservationDAO.getAllReservations().stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        if (start == null || end == null) {
            throw new IllegalStateException("Start date and end date are required.");
        }

        if (!start.isBefore(end)) {
            throw new IllegalStateException("Check-out date must be after check-in date.");
        }

        if (start.isBefore(LocalDate.now())) {
            throw new IllegalStateException("Check-in date cannot be in the past.");
        }

        for (Room room : rooms) {
            if (!reservationDAO.isRoomAvailable(room.getRoomNumber(), start, end, id)) {
                throw new IllegalStateException(
                        "Room " + room.getRoomNumber() + " is already booked for those dates."
                );
            }
        }

        reservationDAO.updateReservationDates(id, start, end);
        notificationService.publish(NotificationType.RESERVATION_UPDATES, existingReservation.getGuestEmail(),
                "Reservation dates updated to " + start + " through " + end + ".");
        notificationService.publish(NotificationType.EARLY_LATE_STAY, existingReservation.getGuestEmail(),
                "Dates changed — ask the front desk about early check-in or late checkout availability.");
    }

    public void updateStatus(int reservationId, String status) {
        Reservation res = reservationDAO.getAllReservations().stream()
                .filter(r -> r.getId() == reservationId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        LocalDate today = LocalDate.now();

        if ("CHECKED_IN".equalsIgnoreCase(status)) {
            // Date validation for checking in
            if (today.isBefore(res.getStartDate())) {
                throw new IllegalStateException("Too early to check in.");
            }
            if (!today.isBefore(res.getEndDate())) {
                throw new IllegalStateException("Reservation has already expired.");
            }
        }
        else if ("CHECKED_OUT".equalsIgnoreCase(status)) {
            // Only allow check-out if they are currently checked in
            if (!"CHECKED_IN".equalsIgnoreCase(res.getStatus())) {
                throw new IllegalStateException("Cannot check out a guest who is not checked in.");
            }
        }

        reservationDAO.updateReservationStatus(reservationId, status);
        if ("CHECKED_IN".equalsIgnoreCase(status)) {
            notificationService.publish(NotificationType.RESERVATION_UPDATES, res.getGuestEmail(),
                    "You have been checked in for reservation #" + reservationId + ".");
            notificationService.publish(NotificationType.RESERVATION_REMINDERS, res.getGuestEmail(),
                    "In-house through " + res.getEndDate() + " — checkout time is printed on your door hanger.");
            notificationService.publish(NotificationType.SPA_WELLNESS_AND_POOL, res.getGuestEmail(),
                    "Spa & pool: pool 7am–10pm, fitness center 24h with room key — spa bookings at the desk.");
            notificationService.publish(NotificationType.CONCIERGE_AND_STAFF, res.getGuestEmail(),
                    "Concierge: dining, tickets, and transport — dial 0 from your room or visit the lobby desk.");
            notificationService.publish(NotificationType.HOUSEKEEPING_AND_SERVICE, res.getGuestEmail(),
                    "Housekeeping: request service timing or turndown from your room phone.");
            notificationService.publish(NotificationType.IN_APP_CHAT_REPLIES, res.getGuestEmail(),
                    "Front desk message: check-in is complete for reservation #" + reservationId + ".");
        } else if ("CHECKED_OUT".equalsIgnoreCase(status)) {
            notificationService.publish(NotificationType.RESERVATION_UPDATES, res.getGuestEmail(),
                    "You have been checked out for reservation #" + reservationId + ".");
            notificationService.publish(NotificationType.HOUSEKEEPING_AND_SERVICE, res.getGuestEmail(),
                    "Thank you for staying — housekeeping may service the room after your departure.");
            notificationService.publish(NotificationType.SURVEYS_AND_FEEDBACK, res.getGuestEmail(),
                    "We would appreciate quick feedback on this stay — mention any issues at checkout or in Account Center.");
        }
    }

    public String getRoomStatus(Room room, List<Reservation> allReservations) {
        LocalDate today = LocalDate.now();
        String status = "Vacant";

        for (Reservation res : allReservations) {
            boolean isRoomInRes = res.getRooms().stream()
                    .anyMatch(r -> r.getRoomNumber() == room.getRoomNumber());

            if (isRoomInRes) {
                // Priority 1: Physically in the room
                if ("CHECKED_IN".equalsIgnoreCase(res.getStatus())) {
                    return "Occupied";
                }

                // Priority 2: Manual Check-out happened today (the end date)
                // This shows the clerk the room needs cleaning today.
                if ("CHECKED_OUT".equalsIgnoreCase(res.getStatus()) && today.equals(res.getEndDate())) {
                    return "Checked Out";
                }

                // Priority 3: Reserved for today but not yet arrived
                if (!today.isBefore(res.getStartDate()) && today.isBefore(res.getEndDate())) {
                    status = "Booked";
                }
            }
        }

        // Once today > res.getEndDate(), the logic above won't find a match,
        // and the room naturally reverts to "Vacant".
        return status;
    }

    public void processAutomaticCheckOuts() {
        List<Reservation> all = reservationDAO.getAllReservations();
        LocalDate today = LocalDate.now();

        for (Reservation res : all) {
            // If today is past the end date and status is not already checked out
            if (today.isAfter(res.getEndDate()) && !"CHECKED_OUT".equalsIgnoreCase(res.getStatus())) {
                reservationDAO.updateReservationStatus(res.getId(), "CHECKED_OUT");
                notificationService.publish(NotificationType.RESERVATION_UPDATES, res.getGuestEmail(),
                        "Reservation #" + res.getId() + " was automatically checked out.");
                notificationService.publish(NotificationType.MAINTENANCE_AND_QUIET_HOURS, res.getGuestEmail(),
                        "Quiet hours 10pm–7am apply through departure morning — thank you for respecting neighbors.");
            }
        }
    }
    public boolean isValidGuest(String email) {
        // You can instantiate a new AccountService here to check the DB
        com.sixstars.service.AccountService accountService = new com.sixstars.service.AccountService();
        com.sixstars.model.Account account = accountService.getAccountByEmail(email);

        // Returns true only if the account exists AND is a Guest
        return account != null && account.getRole() == com.sixstars.model.Role.GUEST;
    }

    public String cancelWithPenalty(Reservation res) {
        if (res == null) {
            throw new IllegalArgumentException("Reservation not found.");
        }

        if ("CANCELLED".equalsIgnoreCase(res.getStatus())) {
            throw new IllegalStateException("This reservation is already cancelled.");
        }

        if ("CHECKED_IN".equalsIgnoreCase(res.getStatus()) || "CHECKED_OUT".equalsIgnoreCase(res.getStatus())) {
            throw new IllegalStateException("Only upcoming reservations can be cancelled.");
        }

        LocalDate today = LocalDate.now();
        if (!today.isBefore(res.getStartDate())) {
            throw new IllegalStateException("Cancellation is only allowed before the reservation start date.");
        }

        long daysSinceCreated = java.time.temporal.ChronoUnit.DAYS.between(res.getCreatedDate(), today);
        int penalty = 0;

        if (daysSinceCreated > 2) {
            penalty = (int) Math.round(res.getNightlyRate() * 0.8);
        }

        reservationDAO.updateReservationStatus(res.getId(), "CANCELLED");
        reservationDAO.updateReservationCost(res.getId(), penalty);
        notificationService.publish(NotificationType.RESERVATION_UPDATES, res.getGuestEmail(),
                "Reservation #" + res.getId() + " was cancelled.");
        notificationService.publish(NotificationType.SURVEYS_AND_FEEDBACK, res.getGuestEmail(),
                "Sorry to see this booking cancelled — optional feedback helps us improve.");

        if (penalty > 0) {
            notificationService.publish(NotificationType.INCIDENTALS_AND_FEES, res.getGuestEmail(),
                    "Cancellation policy: a $" + penalty + " fee was applied to your folio per posted rules.");
            notificationService.publish(NotificationType.FOLIO_AND_CHARGES, res.getGuestEmail(),
                    "Folio: cancellation adjusted reservation #" + res.getId() + " balance by $" + penalty + ".");
            return String.format(
                    "Cancelled. Penalty applied: $%d.00 (80%% of one night at the reservation rate).",
                    penalty
            );
        }
        return "Cancelled successfully. No fee applied (within 2 days of booking).";
    }
}