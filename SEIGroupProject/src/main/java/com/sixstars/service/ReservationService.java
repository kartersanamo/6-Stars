package com.sixstars.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.sixstars.database.ReservationDAO;
import com.sixstars.database.RoomDAO;
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
//    private static final String FILE_PATH = "reservations.json";
//    private final Gson gson;
    private final ReservationDAO reservationDAO;
    private final RoomDAO roomDAO;

    public ReservationService() {
        reservationDAO = new ReservationDAO();
        roomDAO = new RoomDAO();
//        // We add a "TypeAdapter" so Gson knows how to turn LocalDates into Strings
//        this.gson = new GsonBuilder()
//                .setPrettyPrinting()
//                .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
//                        new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE)))
//                .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
//                        LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE))
//                .create();
    }

//    private ArrayList<Reservation> loadReservations() {
//        File file = new File(FILE_PATH);
//        if (!file.exists()) {
//            return new ArrayList<>();
//        }
//
//        try (FileReader reader = new FileReader(file)) {
//            Type listType = new TypeToken<ArrayList<Reservation>>() {}.getType();
//            ArrayList<Reservation> reservations = gson.fromJson(reader, listType);
//            return (reservations != null) ? reservations : new ArrayList<>();
//        } catch (IOException e) {
//            e.printStackTrace();
//            return new ArrayList<>();
//        }
//    }
//
//    private void saveReservations(ArrayList<Reservation> reservations) {
//        File file = new File(FILE_PATH);
//        File parentDir = file.getParentFile();
//        if (parentDir != null && !parentDir.exists()) {
//            parentDir.mkdirs();
//        }
//
//        try (FileWriter writer = new FileWriter(file)) {
//            gson.toJson(reservations, writer);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    /**
//     * Filters a provided list of rooms based on criteria and availability.
//     */
public List<Room> filterAvailableRooms(LocalDate start, LocalDate end, BedType type, Theme theme, QualityLevel quality) {
    List<Room> allRooms = roomDAO.getAllRooms();
    return allRooms.stream()
            .filter(room -> type == null || room.getBedType() == type)
            .filter(room -> theme == null || room.getTheme() == theme)
            .filter(room -> quality == null || room.getQualityLevel() == quality)
            .filter(room -> isRoomAvailable(room, start, end))
            .collect(Collectors.toList());
}
//    public List<Room> filterAvailableRooms(List<Room> roomsToSearch, LocalDate start, LocalDate end, BedType type, Theme theme, QualityLevel quality) {

//        List<Reservation> currentReservations = loadReservations();
//        return roomsToSearch.stream()
//                .filter(room -> room.getBedType() == type)
//                .filter(room -> room.getTheme() == theme)
//                .filter(room -> room.getQualityLevel() == quality)
//                .filter(room -> isRoomAvailableInternal(room, start, end, currentReservations))
//                .collect(Collectors.toList());
//    }
    // Helper method to check availability against a pre-loaded list
    private boolean isRoomAvailableInternal(Room room, LocalDate start, LocalDate end, List<Reservation> reservations) {
        for (Reservation res : reservations) {
            boolean containsRoom = res.getRooms().stream()
                    .anyMatch(r -> r.getRoomNumber() == room.getRoomNumber());
            if (containsRoom) {
                if (start.isBefore(res.getEndDate()) && end.isAfter(res.getStartDate())) {
                    return false;
                }
            }
        }
        return true;
    }

//    /**
//     * Checks the internal reservations list for conflicts.
//     */
    public boolean isRoomAvailable(Room room, LocalDate start, LocalDate end) {
        if (room == null || start == null || end == null) return false;
        return reservationDAO.isRoomAvailable(room.getRoomNumber(), start, end);
    }
//    public boolean isRoomAvailable(Room room, LocalDate start, LocalDate end) {
//        ArrayList<Reservation> allReservations = loadReservations();
//        return isRoomAvailableInternal(room, start, end, allReservations);
//    }

    /**
     * Logic to finalize and save a booking.
     */
    public Reservation makeReservation(String guestEmail, LocalDate start, LocalDate end, List<Room> selectedRooms) {
        for (Room r : selectedRooms) {
            // The Service asks the DAO to do the work
            if (!reservationDAO.isRoomAvailable(r.getRoomNumber(), start, end)) {
                throw new IllegalStateException("Room " + r.getRoomNumber() + " is already booked for these dates.");
            }
        }

        // If all checks pass, the Service proceeds with business logic
        Reservation newBooking = new Reservation(guestEmail, start, end, selectedRooms);
        reservationDAO.saveReservation(newBooking);
        return newBooking;
//        ArrayList<Reservation> allReservations = loadReservations();
//
//        for (Room r : selectedRooms) {
//            if (!isRoomAvailableInternal(r, start, end, allReservations)) {
//                throw new IllegalStateException("Room " + r.getRoomNumber() + " is no longer available.");
//            }
//        }
//
//        Reservation newBooking = new Reservation(start, end, selectedRooms);
//        allReservations.add(newBooking);
//
//        saveReservations(allReservations);
//
//        System.out.println("Reservation created with ID: " + newBooking.getId());
//        return newBooking;
    }

    public List<Reservation> getAllReservations() {
        return reservationDAO.getAllReservations();
//        return loadReservations();
    }

    // Gets all reservations belonging to specific email
    public List<Reservation> getGuestReservations(String email) {
        return reservationDAO.getReservationsByEmail(email);
    }

    // Cancels booking by id
    public void cancelBooking(int id) {
        reservationDAO.cancelReservation(id);
    }

    // Updates existing reservation
    public void updateReservation(int id, LocalDate start, LocalDate end) {
        // Business Logic: You could add a check here to ensure the new dates
        // are available before calling the DAO!
        reservationDAO.updateReservationDates(id, start, end);
    }
}