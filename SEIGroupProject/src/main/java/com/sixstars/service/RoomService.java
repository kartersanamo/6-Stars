package com.sixstars.service;

import com.sixstars.model.BedType;
import com.sixstars.model.Room;

import java.util.ArrayList;
import java.util.List;

public class RoomService {
    private List<Room> allRooms;

    public RoomService() {
        this.allRooms = new ArrayList<>();
        // You can pre-populate rooms here for testing
        initializeRooms();
    }

    private void initializeRooms() {
        // TODO: only for testing
        allRooms.add(new Room(101, BedType.KING));
        allRooms.add(new Room(102, BedType.QUEEN));
        allRooms.add(new Room(103, BedType.SINGLE));
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(allRooms); // Return a copy for safety
    }
}