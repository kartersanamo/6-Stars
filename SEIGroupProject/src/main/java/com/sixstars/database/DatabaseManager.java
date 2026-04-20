package com.sixstars.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:hotel_reservation.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Create Accounts Table
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                    "email TEXT PRIMARY KEY, firstName TEXT, lastName TEXT, " +
                    "passwordHash TEXT, role TEXT)");

            // Create Rooms Table
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "roomNumber INTEGER PRIMARY KEY, bedType TEXT, " +
                    "theme TEXT, qualityLevel TEXT, isSmoking INTEGER)");

            // Create Reservations Table
            stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, startDate TEXT, endDate TEXT, guestEmail TEXT)");

            // Create Join Table for Reservation <-> Rooms (Many-to-Many)
            stmt.execute("CREATE TABLE IF NOT EXISTS reservation_rooms (" +
                    "reservation_id INTEGER, room_number INTEGER, " +
                    "FOREIGN KEY(reservation_id) REFERENCES reservations(id), " +
                    "FOREIGN KEY(room_number) REFERENCES rooms(room_number))");
            // Create Shop Items Table
            stmt.execute("CREATE TABLE IF NOT EXISTS shop_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "price REAL NOT NULL, " +
                    "stock INTEGER NOT NULL, " +
                    "imagePath TEXT)");


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}