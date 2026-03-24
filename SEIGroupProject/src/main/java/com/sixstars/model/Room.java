package com.sixstars.model;

public class Room {
    int roomNumber;
    BedType type;
    Boolean smoking;

    public Room(int rn, BedType t, boolean s){
        roomNumber = rn;
        type = t;
        smoking = s;
    }

    @Override
    public String toString(){
        return "Room " + roomNumber + " (" + type + ")";
    }

    public int getRoomNumber() {
        return roomNumber;
    }
    public BedType getBedType() {
        return type;
    }
    public Boolean getSmoking() { return smoking; }
}
