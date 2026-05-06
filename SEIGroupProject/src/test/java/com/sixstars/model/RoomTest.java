package com.sixstars.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    @Test
    void constructorSetsAllRoomFields() {
        Room room = new Room(
                101,
                BedType.QUEEN,
                Theme.NATURE_RETREAT,
                QualityLevel.COMFORT,
                false
        );

        assertEquals(101, room.getRoomNumber());
        assertEquals(BedType.QUEEN, room.getBedType());
        assertEquals(Theme.NATURE_RETREAT, room.getTheme());
        assertEquals(QualityLevel.COMFORT, room.getQualityLevel());
        assertFalse(room.isSmoking());
    }

    @Test
    void defaultStatusIsVacant() {
        Room room = new Room(
                102,
                BedType.KING,
                Theme.URBAN_ELEGANCE,
                QualityLevel.EXECUTIVE,
                true
        );

        assertEquals("Vacant", room.getStatus());
    }

    @Test
    void setStatusUpdatesRoomStatus() {
        Room room = new Room(
                103,
                BedType.DOUBLE,
                Theme.VINTAGE_CHARM,
                QualityLevel.BUSINESS,
                false
        );

        room.setStatus("Booked");

        assertEquals("Booked", room.getStatus());
    }

    @Test
    void calculatePriceForEconomyTwin() {
        assertEquals(
                89,
                Room.calculatePricePerNight(QualityLevel.ECONOMY, BedType.TWIN)
        );
    }

    @Test
    void calculatePriceForComfortQueen() {
        assertEquals(
                154,
                Room.calculatePricePerNight(QualityLevel.COMFORT, BedType.QUEEN)
        );
    }

    @Test
    void calculatePriceForExecutiveKing() {
        assertEquals(
                269,
                Room.calculatePricePerNight(QualityLevel.EXECUTIVE, BedType.KING)
        );
    }

    @Test
    void customPriceConstructorUsesGivenPrice() {
        Room room = new Room(
                201,
                BedType.KING,
                Theme.URBAN_ELEGANCE,
                QualityLevel.EXECUTIVE,
                false,
                500
        );

        assertEquals(500, room.getPricePerNight());
    }

    @Test
    void setPricePerNightUpdatesPrice() {
        Room room = new Room(
                202,
                BedType.SINGLE,
                Theme.NATURE_RETREAT,
                QualityLevel.ECONOMY,
                false
        );

        room.setPricePerNight(150);

        assertEquals(150, room.getPricePerNight());
    }
}