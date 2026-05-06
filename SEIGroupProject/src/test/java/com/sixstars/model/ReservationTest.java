package com.sixstars.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationTest {

    @Test
    void constructorCalculatesNightsAndTotalCost() {
        Room room = new Room(
                101,
                BedType.QUEEN,
                Theme.NATURE_RETREAT,
                QualityLevel.COMFORT,
                false,
                150
        );

        Reservation reservation = new Reservation(
                "guest@test.com",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 13),
                List.of(room),
                "BOOKED"
        );

        assertEquals(3, reservation.getNights());
        assertEquals(150, reservation.getNightlyRate());
        assertEquals(450, reservation.getTotalCost());
    }

    @Test
    void constructorUsesZeroRateWhenRoomsAreEmpty() {
        Reservation reservation = new Reservation(
                "guest@test.com",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 13),
                List.of(),
                "BOOKED"
        );

        assertEquals(0, reservation.getNightlyRate());
        assertEquals(3, reservation.getNights());
        assertEquals(0, reservation.getTotalCost());
    }

    @Test
    void constructorDoesNotAllowNegativeNights() {
        Room room = new Room(
                101,
                BedType.KING,
                Theme.URBAN_ELEGANCE,
                QualityLevel.EXECUTIVE,
                false,
                250
        );

        Reservation reservation = new Reservation(
                "guest@test.com",
                LocalDate.of(2026, 5, 13),
                LocalDate.of(2026, 5, 10),
                List.of(room),
                "BOOKED"
        );

        assertEquals(0, reservation.getNights());
        assertEquals(0, reservation.getTotalCost());
    }

    @Test
    void settersUpdateReservationFields() {
        Reservation reservation = new Reservation(
                "guest@test.com",
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 12),
                List.of(),
                "BOOKED"
        );

        reservation.setId(7);
        reservation.setGuestEmail("updated@test.com");
        reservation.setNightlyRate(200);
        reservation.setNights(2);
        reservation.setTotalCost(400);
        reservation.setStatus("CANCELLED");
        reservation.setCreatedDate(LocalDate.of(2026, 5, 1));

        assertEquals(7, reservation.getId());
        assertEquals("updated@test.com", reservation.getGuestEmail());
        assertEquals(200, reservation.getNightlyRate());
        assertEquals(2, reservation.getNights());
        assertEquals(400, reservation.getTotalCost());
        assertEquals("CANCELLED", reservation.getStatus());
        assertEquals(LocalDate.of(2026, 5, 1), reservation.getCreatedDate());
    }
}