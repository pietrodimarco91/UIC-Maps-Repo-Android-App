package com.example.pietrodimarco.hci.data;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by pietrodimarco on 12/6/17.
 */

public class ResSearch {
    
    String room;
    LatLng latLng;
    public ResSearch(String room, LatLng latLng) {
        this.room=room;
        this.latLng=latLng;
    }

    public String getRoom() {
        return room;
    }
    public LatLng getLatLng(){return latLng;}
}
