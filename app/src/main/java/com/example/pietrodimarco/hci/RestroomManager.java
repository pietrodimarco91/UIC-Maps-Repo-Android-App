package com.example.pietrodimarco.hci;

import android.content.Context;

import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.Point;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;


public class RestroomManager implements Runnable{
    ArrayList<Restroom> restrooms;
    Context appContext;

    public RestroomManager(Context applicationContext) {
        this.appContext=applicationContext;
    }

    public ArrayList<Restroom> getRestrooms() {
        return restrooms;
    }

    @Override
    public void run() {
        restrooms = new ArrayList<>();
        String bathroom;
        LoadJson string=new LoadJson();
        try {
            FeatureCollection geoJSON = (FeatureCollection) GeoJSON.parse(string.loadJSONFromAsset(appContext));
            for (int i = 0; i < geoJSON.getFeatures().size(); i++) {
                if(geoJSON.getFeatures().get(i).getProperties().isNull("room"))
                    bathroom="";
                else{
                    bathroom = geoJSON.getFeatures().get(i).getProperties().getString("room");
                }
                int randomInt = 0;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    randomInt = ThreadLocalRandom.current().nextInt(0, 3);
                }
                restrooms.add(new Restroom(geoJSON.getFeatures().get(i).getProperties().getInt("floor"), bathroom, geoJSON.getFeatures().get(i).getProperties().getString("gender"), (Point) geoJSON.getFeatures().get(i).getGeometry(),randomInt));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
