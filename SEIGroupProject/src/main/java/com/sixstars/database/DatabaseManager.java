package com.sixstars.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

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

//            stmt.execute("ALTER TABLE reservations ADD COLUMN status TEXT DEFAULT 'BOOKED'");
            // Create Rooms Table
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "roomNumber INTEGER PRIMARY KEY, bedType TEXT, " +
                    "theme TEXT, qualityLevel TEXT, isSmoking INTEGER, pricePerNight INTEGER DEFAULT 0)");

            // Create Reservations Table
            stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, startDate TEXT, endDate TEXT, guestEmail TEXT, " +
                    "nightlyRate INTEGER DEFAULT 0, nights INTEGER DEFAULT 0, totalCost INTEGER DEFAULT 0, " +
                    "status TEXT DEFAULT 'BOOKED', createdDate TEXT)");

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

            addColumnIfMissing(conn, "rooms", "pricePerNight", "INTEGER DEFAULT 0");
            addColumnIfMissing(conn, "reservations", "nightlyRate", "INTEGER DEFAULT 0");
            addColumnIfMissing(conn, "reservations", "nights", "INTEGER DEFAULT 0");
            addColumnIfMissing(conn, "reservations", "totalCost", "INTEGER DEFAULT 0");
            addColumnIfMissing(conn, "reservations", "status", "TEXT DEFAULT 'BOOKED'");
            addColumnIfMissing(conn, "reservations", "createdDate", "TEXT");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void addColumnIfMissing(Connection conn, String tableName, String columnName, String columnDefinition)
            throws SQLException {
        String pragmaSql = "PRAGMA table_info(" + tableName + ")";
        boolean columnExists = false;

        try (Statement pragmaStmt = conn.createStatement();
             ResultSet rs = pragmaStmt.executeQuery(pragmaSql)) {
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    columnExists = true;
                    break;
                }
            }
        }

        if (!columnExists) {
            try (Statement alterStmt = conn.createStatement()) {
                alterStmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
            }
        }
    }
}