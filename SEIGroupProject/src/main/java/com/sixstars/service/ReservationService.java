package com.sixstars.service;

import com.sixstars.database.ReservationDAO;
import com.sixstars.database.RoomDAO;
import com.sixstars.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationService {
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;

    public ReservationService() {
        reservationDAO = new ReservationDAO();
        roomDAO = new RoomDAO();
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
    public void cancelBooking(int id) {
        reservationDAO.cancelReservation(id);
    }

    // Updates existing reservation
    public void updateReservation(int id, LocalDate start, LocalDate end, List<Room> rooms) {
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
    }

    public void updateStatus(int reservationId, String status) {
        // 1. Fetch the reservation to check its dates
        Reservation res = reservationDAO.getAllReservations().stream()
                .filter(r -> r.getId() == reservationId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found."));

        // 2. Validate same-day check-in
        if ("CHECKED_IN".equalsIgnoreCase(status)) {
            LocalDate today = LocalDate.now();
            if (!today.equals(res.getStartDate())) {
                throw new IllegalStateException("Check-in is only permitted on the scheduled start date: " + res.getStartDate());
            }
        }

        // 3. Persist the change
        reservationDAO.updateReservationStatus(reservationId, status);
    }
}