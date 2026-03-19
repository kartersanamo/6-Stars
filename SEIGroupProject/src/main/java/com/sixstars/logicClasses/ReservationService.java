package com.sixstars.logicClasses;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationService {
    private List<Reservation> allReservations = new ArrayList<>();

    /**
     * Filters a provided list of rooms based on criteria and availability.
     */
    public List<Room> filterAvailableRooms(List<Room> roomsToSearch, LocalDate start, LocalDate end, BedType type) {
        return roomsToSearch.stream()
                .filter(room -> room.getBedType() == type)
                .filter(room -> isRoomAvailable(room, start, end))
                .collect(Collectors.toList());
    }

    /**
     * Checks the internal reservations list for conflicts.
     */
    public boolean isRoomAvailable(Room room, LocalDate start, LocalDate end) {
        for (Reservation res : allReservations) {
            if (res.getRooms().contains(room)) {
                // Logic: An overlap exists if (StartA < EndB) AND (EndA > StartB)
                if (start.isBefore(res.getEndDate()) && end.isAfter(res.getStartDate())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Logic to finalize and save a booking.
     */
    public Reservation makeReservation(LocalDate start, LocalDate end, List<Room> selectedRooms) {
        // 1. Final Safety Check (Optional but recommended)
        for (Room r : selectedRooms) {
            if (!isRoomAvailable(r, start, end)) {
                throw new IllegalStateException("Room " + r.getRoomNumber() + " is no longer available.");
            }
        }

        // 2. Instantiate the new Reservation (using the constructor we updated earlier)
        Reservation newBooking = new Reservation(start, end, selectedRooms);

        // 3. Persist to our list
        allReservations.add(newBooking);

        System.out.println("Reservation created with ID: " + newBooking.getId());
        return newBooking;
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(allReservations);
    }
}