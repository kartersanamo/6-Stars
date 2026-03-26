package com.sixstars.model;

public class Room {
    int roomNumber;
    BedType type;
    Theme theme;
    QualityLevel qualityLevel;
    boolean isSmoking;

    public Room(int roomNumber, BedType type, Theme theme, QualityLevel qualityLevel, boolean isSmoking) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.theme = theme;
        this.qualityLevel = qualityLevel;
        this.isSmoking = isSmoking;
    }

    @Override
    public String toString(){
        return "Room " + roomNumber + " (" + type + "): " + theme + " (" + qualityLevel + ")";
    }

    public int getRoomNumber() {
        return roomNumber;
    }
    public BedType getBedType() {
        return type;
    }
    public Theme getTheme() {
        return theme;
    }

    public QualityLevel getQualityLevel() {
        return qualityLevel;
    }

    public boolean isSmoking() {
        return isSmoking;
    }
}
