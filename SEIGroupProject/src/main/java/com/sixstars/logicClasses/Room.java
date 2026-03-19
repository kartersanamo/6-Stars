package com.sixstars.logicClasses;

public class Room {
    int roomNumber;
    BedType type;

    Room(int rn, BedType t){
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
