package com.example.pietrodimarco.hci;

import android.app.Fragment;
import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by pietrodimarco on 11/17/17.
 */

public class LoadJson extends Fragment {



    public String loadJSONFromAsset(Context applicationContext) {
        String json = null;
        try {
            InputStream is = applicationContext.getAssets().open("waypointsMerged.geojson");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {

            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String loadJSONFromAssetB(Context applicationContext) {
        String json = null;
        try {
            InputStream is = applicationContext.getAssets().open("RestroomDataset.geojson");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {

            ex.printStackTrace();
            return null;
        }
        return json;
    }


    public String loadJSONFromAssetAll(Context appContext) {
        String json = null;
        try {
            InputStream is = appContext.getAssets().open("map.geojson");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {

            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
