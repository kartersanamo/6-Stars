package com.sixstars.model;

import java.time.LocalDate;
import java.util.List;

public class Reservation {
    int id;
    LocalDate startDate, endDate;
    List<Room> rooms;

    public Reservation(LocalDate startDate, LocalDate endDate, List<Room> rooms){
        this.startDate = startDate;
        this.endDate = endDate;
        this.rooms = rooms;
    }

    public int getId() {
        return id;
    }
    public void setId(int id){
        this.id = id;
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
