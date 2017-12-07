package com.example.pietrodimarco.hci;

import com.cocoahero.android.geojson.Point;

/**
 * Created by Drako on 05/12/2017.
 */

public class Restroom {
    String gender;
    int floor;
    String room;
    Point point;
    int availability;

    public Restroom(int floor, String room, String gender, Point point, int availability) {
        this.floor=floor;
        this.room=room;
        this.gender= gender;
        this.point=point;
        this.availability = availability;
    }

    public int getFloor() {
        return floor;
    }

    public Point getPoint() {
        return point;
    }

    public String getGender() {
        return gender;
    }

    public String getRoom() {
        return room;
    }

    public String toString(){
        String avail = new String(" ");
        if(availability==0)
            avail = new String("Low");
        else if(availability ==1)
            avail = new String("Medium");
        else if (availability==2)
            avail = new String("High");
        return this.room + "     " + this.gender + "     " + avail;
    }
}
