package com.sixstars.model;

import java.time.LocalDate;
import java.util.List;

public class Reservation {
    int id;
    static int nextId = 0;
    LocalDate startDate, endDate;
    List<Room> rooms;

    public Reservation(LocalDate startDate, LocalDate endDate, List<Room> rooms){
        this.id = nextId++;
        this.startDate = startDate;
        this.endDate = endDate;
        this.rooms = rooms;
    }

    public int getId() {
        return id;
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
}
