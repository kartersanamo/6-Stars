package com.sixstars.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sixstars.model.BedType;
import com.sixstars.model.QualityLevel;
import com.sixstars.model.Room;
import com.sixstars.model.Theme;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RoomService {
    private List<Room> allRooms;
    private static final String FILE_PATH = "rooms.json";
    private final Gson gson;

    public RoomService() {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        if (!loadRoomsFromFile()){
            this.allRooms = new ArrayList<>();
            initializeRooms();
        }
    }

    public void addRoom(Room newRoom) {
        // Check for duplicates so you don't have two Room 101s
        for (Room r : allRooms) {
            if (r.getRoomNumber() == newRoom.getRoomNumber()) {
                throw new IllegalArgumentException("Room " + r.getRoomNumber() + " already exists!");
            }
        }
        allRooms.add(newRoom);
        saveRoomsToFile(); // Important: writes it to rooms.json!
    }

    private void initializeRooms() {
        // TODO: only for testing
        allRooms.add(new Room(101, BedType.KING, Theme.NATURE_RETREAT, QualityLevel.EXECUTIVE, false));
        allRooms.add(new Room(201, BedType.QUEEN, Theme.URBAN_ELEGANCE, QualityLevel.BUSINESS, false));
        allRooms.add(new Room(301, BedType.TWIN, Theme.VINTAGE_CHARM, QualityLevel.COMFORT, true));
        saveRoomsToFile();
    }

    public void saveRoomsToFile(){
        try (Writer writer = new FileWriter(FILE_PATH)) {
            gson.toJson(allRooms, writer);
        } catch (IOException e) {
            System.err.println("Error saving rooms: " + e.getMessage());
        }
    }
    public boolean loadRoomsFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return false;

        try (Reader reader = new FileReader(FILE_PATH)) {
            // Because of Type Erasure in Java, we need TypeToken to tell
            // Gson exactly what kind of List we are expecting.
            Type listType = new TypeToken<ArrayList<Room>>(){}.getType();
            this.allRooms = gson.fromJson(reader, listType);
            return allRooms != null;
        } catch (IOException e) {
            System.err.println("Error loading rooms: " + e.getMessage());
            return false;
        }
    }
    public List<Room> getAllRooms() {
        return new ArrayList<>(allRooms); // Return a copy for safety
    }
}