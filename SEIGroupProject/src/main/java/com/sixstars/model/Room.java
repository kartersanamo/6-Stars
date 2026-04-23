package com.sixstars.model;

public class Room {
    private int roomNumber;
    private BedType bedType;
    private Theme theme;
    private QualityLevel qualityLevel;
    private boolean isSmoking;
    private int pricePerNight;
    private String status = "Vacant";

    public Room(int roomNumber, BedType bedType, Theme theme, QualityLevel qualityLevel, boolean isSmoking) {
        this(roomNumber, bedType, theme, qualityLevel, isSmoking,
                calculatePricePerNight(qualityLevel, bedType));
    }

    public Room(int roomNumber, BedType bedType, Theme theme, QualityLevel qualityLevel,
                boolean isSmoking, int pricePerNight) {
        this.roomNumber = roomNumber;
        this.bedType = bedType;
        this.theme = theme;
        this.qualityLevel = qualityLevel;
        this.isSmoking = isSmoking;
        this.pricePerNight = pricePerNight;
    }

    public static int calculatePricePerNight(QualityLevel qualityLevel, BedType bedType) {
        int qualityBase;
        switch (qualityLevel) {
            case ECONOMY:
                qualityBase = 89;
                break;
            case COMFORT:
                qualityBase = 119;
                break;
            case BUSINESS:
                qualityBase = 159;
                break;
            case EXECUTIVE:
                qualityBase = 219;
                break;
            default:
                qualityBase = 99;
        }

        int bedUpcharge;
        switch (bedType) {
            case TWIN:
                bedUpcharge = 0;
                break;
            case SINGLE:
                bedUpcharge = 10;
                break;
            case DOUBLE:
                bedUpcharge = 20;
                break;
            case QUEEN:
                bedUpcharge = 35;
                break;
            case KING:
                bedUpcharge = 50;
                break;
            default:
                bedUpcharge = 0;
        }

        return qualityBase + bedUpcharge;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + bedType + "): " + status.toUpperCase() + " - " + theme
                + " (" + qualityLevel + ") - $" + pricePerNight + "/night";
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public BedType getBedType() {
        return bedType;
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

    public int getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(int pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public String getStatus(){ return status; }

    public void setStatus(String status) { this.status = status; }
}