package com.sixstars.model;

public class Room {
    int roomNumber;
    BedType type;

    public Room(int rn, BedType t){
        roomNumber = rn;
        type = t;
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
}
