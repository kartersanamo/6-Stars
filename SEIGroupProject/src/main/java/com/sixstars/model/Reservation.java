package com.sixstars.model;

import java.time.LocalDate;
import java.util.List;

public class Reservation {
    int id;
    String guestEmail;
    LocalDate startDate, endDate;
    List<Room> rooms;

    public Reservation(String guestEmail, LocalDate startDate, LocalDate endDate, List<Room> rooms){
        this.guestEmail = guestEmail;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rooms = rooms;
    }

    @Override
    public String toString() {
        String roomDetails = rooms.stream()
                .map(r -> "Room " + r.getRoomNumber() + " (" + r.getTheme() + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse("No Rooms Assigned");

        return String.format("%s | %s to %s",
                roomDetails, startDate, endDate);
    }

    public int getId() {
        return id;
    }
    public void setId(int id){
        this.id = id;
    }
    public String getGuestEmail() { return guestEmail; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public LocalDate getStartDate() {
        return startDate;
    }
    public LocalDate getEndDate() {
        return endDate;
    }
    public List<Room> getRooms() {
        return rooms;
    }
}
