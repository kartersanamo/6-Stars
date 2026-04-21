package com.sixstars.service;

import com.sixstars.database.RoomDAO;
import com.sixstars.model.BedType;
import com.sixstars.model.QualityLevel;
import com.sixstars.model.Room;
import com.sixstars.model.Theme;

import java.util.List;

public class RoomService {
    private final RoomDAO roomDAO;

    public RoomService() {
        roomDAO = new RoomDAO();
        if (roomDAO.getAllRooms().isEmpty()){
            initializeRooms();
        }
    }

    public void addRoom(Room newRoom) {
        roomDAO.saveRoom(newRoom);
    }

    private void initializeRooms() {
        roomDAO.saveRoom(new Room(101, BedType.KING, Theme.NATURE_RETREAT, QualityLevel.EXECUTIVE, false));
        roomDAO.saveRoom(new Room(201, BedType.QUEEN, Theme.URBAN_ELEGANCE, QualityLevel.BUSINESS, false));
        roomDAO.saveRoom(new Room(301, BedType.TWIN, Theme.VINTAGE_CHARM, QualityLevel.COMFORT, true));
    }

    public List<Room> getAllRooms() {
        return roomDAO.getAllRooms();
    }
}