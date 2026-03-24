package com.sixstars.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.sixstars.model.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReservationService {
//    private List<Reservation> allReservations = new ArrayList<>();
    private static final String FILE_PATH = "reservations.json";
    private final Gson gson;

    public ReservationService() {
        // We add a "TypeAdapter" so Gson knows how to turn LocalDates into Strings
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                        LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
                .create();
    }

    private ArrayList<Reservation> loadReservations() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Type listType = new TypeToken<ArrayList<Reservation>>() {}.getType();
            ArrayList<Reservation> reservations = gson.fromJson(reader, listType);
            return (reservations != null) ? reservations : new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private void saveReservations(ArrayList<Reservation> reservations) {
        File file = new File(FILE_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(reservations, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Filters a provided list of rooms based on criteria and availability.
     */
    public List<Room> filterAvailableRooms(List<Room> roomsToSearch, LocalDate start, LocalDate end, BedType type, Theme theme, QualityLevel quality) {
        List<Reservation> currentReservations = loadReservations();
        return roomsToSearch.stream()
                .filter(room -> room.getBedType() == type)
                .filter(room -> room.getTheme() == theme) // New Filter
                .filter(room -> room.getQualityLevel() == quality) // New Filter
                .filter(room -> isRoomAvailableInternal(room, start, end, currentReservations))
                .collect(Collectors.toList());
    }
    // Helper method to check availability against a pre-loaded list
    private boolean isRoomAvailableInternal(Room room, LocalDate start, LocalDate end, List<Reservation> reservations) {
        for (Reservation res : reservations) {
            if (res.getRooms().contains(room)) {
                if (start.isBefore(res.getEndDate()) && end.isAfter(res.getStartDate())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks the internal reservations list for conflicts.
     */
    public boolean isRoomAvailable(Room room, LocalDate start, LocalDate end) {
        ArrayList<Reservation> allReservations = loadReservations();
        for (Reservation res : allReservations) {
            if (res.getRooms().contains(room)) {
                // Logic: An overlap exists if (StartA < EndB) AND (EndA > StartB)
                if (start.isBefore(res.getEndDate()) && end.isAfter(res.getStartDate())) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Logic to finalize and save a booking.
     */
    public Reservation makeReservation(LocalDate start, LocalDate end, List<Room> selectedRooms) {
        ArrayList<Reservation> allReservations = loadReservations();

        for (Room r : selectedRooms) {
            if (!isRoomAvailableInternal(r, start, end, allReservations)) {
                throw new IllegalStateException("Room " + r.getRoomNumber() + " is no longer available.");
            }
        }

        Reservation newBooking = new Reservation(start, end, selectedRooms);
        allReservations.add(newBooking);

        saveReservations(allReservations);

        System.out.println("Reservation created with ID: " + newBooking.getId());
        return newBooking;
    }

    public List<Reservation> getAllReservations() {
        return loadReservations();
    }
}