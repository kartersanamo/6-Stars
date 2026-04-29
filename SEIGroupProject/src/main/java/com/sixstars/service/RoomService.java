package com.sixstars.service;

import com.sixstars.database.RoomDAO;
import com.sixstars.model.BedType;
import com.sixstars.model.QualityLevel;
import com.sixstars.model.Room;
import com.sixstars.model.Theme;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RoomService {
    private static final int TARGET_ROOM_COUNT = 180;
    private final RoomDAO roomDAO;

    public RoomService() {
        roomDAO = new RoomDAO();
        if (roomDAO.getAllRooms().isEmpty()) {
            initializeRooms();
        }
        ensureHotelInventory();
    }

    public void addRoom(Room newRoom) {
        roomDAO.saveRoom(newRoom);
    }
    public void updateRoom(Room room) {
        roomDAO.updateRoom(room);
    }

    private void initializeRooms() {
        roomDAO.saveRoom(new Room(101, BedType.KING, Theme.NATURE_RETREAT, QualityLevel.EXECUTIVE, false));
        roomDAO.saveRoom(new Room(201, BedType.QUEEN, Theme.URBAN_ELEGANCE, QualityLevel.BUSINESS, false));
        roomDAO.saveRoom(new Room(301, BedType.TWIN, Theme.VINTAGE_CHARM, QualityLevel.COMFORT, true));
    }

    private void ensureHotelInventory() {
        List<Room> existingRooms = roomDAO.getAllRooms();
        if (existingRooms.size() >= TARGET_ROOM_COUNT) {
            return;
        }

        Set<Integer> existingRoomNumbers = new HashSet<>();
        for (Room room : existingRooms) {
            existingRoomNumbers.add(room.getRoomNumber());
        }

        // Generate a realistic inventory by floor and room index until target count is reached.
        for (int floor = 1; floor <= 18 && existingRoomNumbers.size() < TARGET_ROOM_COUNT; floor++) {
            for (int roomIndex = 1; roomIndex <= 12 && existingRoomNumbers.size() < TARGET_ROOM_COUNT; roomIndex++) {
                int roomNumber = floor * 100 + roomIndex;
                if (existingRoomNumbers.contains(roomNumber)) {
                    continue;
                }

                BedType bedType = BedType.values()[(roomNumber + floor) % BedType.values().length];
                Theme theme = Theme.values()[(roomNumber + roomIndex) % Theme.values().length];
                QualityLevel quality = QualityLevel.values()[(roomNumber + floor + roomIndex) % QualityLevel.values().length];
                boolean smoking = (roomNumber % 7 == 0);

                roomDAO.saveRoom(new Room(roomNumber, bedType, theme, quality, smoking));
                existingRoomNumbers.add(roomNumber);
            }
        }
    }

    public List<Room> getAllRooms() {
        return roomDAO.getAllRooms();
    }
}