package com.sixstars.model;

import java.time.LocalDate;
import java.util.List;

public class Reservation {
    int id;
    String guestEmail;
    LocalDate startDate, endDate;
    List<Room> rooms;
    int nightlyRate;
    int nights;
    int totalCost;

    public Reservation(String guestEmail, LocalDate startDate, LocalDate endDate, List<Room> rooms) {
        this.guestEmail = guestEmail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rooms = rooms;

        if (rooms != null && !rooms.isEmpty()) {
            this.nightlyRate = rooms.get(0).getPricePerNight();
        } else {
            this.nightlyRate = 0;
        }

        this.nights = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        if (this.nights < 0) {
            this.nights = 0;
        }

        this.totalCost = this.nightlyRate * this.nights;
    }

    public Reservation(String guestEmail, LocalDate startDate, LocalDate endDate,
                       List<Room> rooms, int nightlyRate, int nights, int totalCost) {
        this.guestEmail = guestEmail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rooms = rooms;
        this.nightlyRate = nightlyRate;
        this.nights = nights;
        this.totalCost = totalCost;
    }

    @Override
    public String toString() {
        String roomDetails = rooms.stream()
                .map(r -> "Room " + r.getRoomNumber() + " (" + r.getTheme() + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse("No Rooms Assigned");

        return String.format("%s | %s to %s | $%d/night | %d night%s | Total: $%d",
                roomDetails,
                startDate,
                endDate,
                nightlyRate,
                nights,
                nights == 1 ? "" : "s",
                totalCost);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public int getNightlyRate() {
        return nightlyRate;
    }

    public void setNightlyRate(int nightlyRate) {
        this.nightlyRate = nightlyRate;
    }

    public int getNights() {
        return nights;
    }

    public void setNights(int nights) {
        this.nights = nights;
    }

    public int getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(int totalCost) {
        this.totalCost = totalCost;
    }
}