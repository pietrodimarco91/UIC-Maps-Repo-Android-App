package com.example.pietrodimarco.hci.data;

/**
 * Copyright (C) 2015 Ari C.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.util.Log;
import android.widget.Filter;

import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.Point;
import com.example.pietrodimarco.hci.LoadJson;
import com.example.pietrodimarco.hci.MainActivity;
import com.example.pietrodimarco.hci.Tag;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DataHelper {

    private static final String COLORS_FILE_NAME = "colors.json";
    private static ArrayList<String> rooms = new ArrayList<>();
    private static ArrayList<ResSearch> resSearches= new ArrayList<>();


    private static List<ColorWrapper> sColorWrappers = new ArrayList<>();

    private static List<ColorSuggestion> sColorSuggestions =
            new ArrayList<>(Arrays.asList(
                    new ColorSuggestion("green"),
                    new ColorSuggestion("blue"),
                    new ColorSuggestion("pink"),
                    new ColorSuggestion("purple"),
                    new ColorSuggestion("brown"),
                    new ColorSuggestion("gray"),
                    new ColorSuggestion("Granny Smith Apple"),
                    new ColorSuggestion("Indigo"),
                    new ColorSuggestion("Periwinkle"),
                    new ColorSuggestion("Mahogany"),
                    new ColorSuggestion("Maize"),
                    new ColorSuggestion("Mahogany"),
                    new ColorSuggestion("Outer Space"),
                    new ColorSuggestion("Melon"),
                    new ColorSuggestion("Yellow"),
                    new ColorSuggestion("Orange"),
                    new ColorSuggestion("Red"),
                    new ColorSuggestion("Orchid")));

    public static void load(MainActivity mainActivity) {

        Context appContext=mainActivity.getApplicationContext();
        String room;
        LatLng latLng;
        Point p;

        LoadJson string=new LoadJson();
        try {
            FeatureCollection geoJSON = (FeatureCollection) GeoJSON.parse(string.loadJSONFromAssetAll(appContext));
            for (int i = 0; i < geoJSON.getFeatures().size(); i++) {
                room = geoJSON.getFeatures().get(i).getProperties().getString("Room");
                p=(Point) geoJSON.getFeatures().get(i).getGeometry();
                latLng=new LatLng(p.getPosition().getLatitude(), p.getPosition().getLongitude());
                Log.d("data",room);
                resSearches.add(new ResSearch(room,latLng));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public static LatLng getLatLng(String room) {

        for(int i=0;i<resSearches.size();i++)
            if(resSearches.get(i).room.equals(room))
                return resSearches.get(i).latLng;
        return null;



    }

    public static ResSearch findRoom(String query) {
        for (int i=0;i<resSearches.size();i++)
            if(resSearches.get(i).getRoom().equals(query))
                return resSearches.get(i);
        return null;
    }

    public interface OnFindColorsListener {
        void onResults(List<ColorWrapper> results);
    }

    public interface OnFindSuggestionsListener {
        void onResults(List<ColorSuggestion> results);
    }

    public static List<RoomSuggestion> getHistory(int count) {
        List<RoomSuggestion> rooms=new ArrayList<>();
        rooms.add(new RoomSuggestion("234"));
        rooms.add(new RoomSuggestion("54"));
        return rooms;
    }

    public static void resetSuggestionsHistory() {
        for (ColorSuggestion colorSuggestion : sColorSuggestions) {
            colorSuggestion.setIsHistory(false);
        }
    }

    public static void findSuggestions( String query, final int limit, final long simulatedDelay,
                                       final OnFindSuggestionsListener listener) {
        new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                try {
                    Thread.sleep(simulatedDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                DataHelper.resetSuggestionsHistory();
                List<RoomSuggestion> roomSuggestions = new ArrayList<>();
                if (!(constraint == null || constraint.length() == 0)) {

                    for (ResSearch rooms : resSearches) {
                        if (rooms.room.toUpperCase()
                                .startsWith(constraint.toString().toUpperCase())) {
                            Log.d("data","true");

                            roomSuggestions.add(new RoomSuggestion(rooms.room));
                            if (limit != -1 && roomSuggestions.size() == limit) {
                                break;
                            }
                        }
                    }
                }

                FilterResults results = new FilterResults();

                results.values = roomSuggestions;
                results.count = roomSuggestions.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (listener != null) {
                    listener.onResults((List<ColorSuggestion>) results.values);
                }
            }
        }.filter(query);

    }


    public static void findColors(Context context, String query, final OnFindColorsListener listener) {
        initColorWrapperList(context);

        new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {


                List<ColorWrapper> suggestionList = new ArrayList<>();

                if (!(constraint == null || constraint.length() == 0)) {

                    for (ColorWrapper color : sColorWrappers) {
                        if (color.getName().toUpperCase()
                                .startsWith(constraint.toString().toUpperCase())) {

                            suggestionList.add(color);
                        }
                    }

                }

                FilterResults results = new FilterResults();
                results.values = suggestionList;
                results.count = suggestionList.size();

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                if (listener != null) {
                    listener.onResults((List<ColorWrapper>) results.values);
                }
            }
        }.filter(query);

    }

    private static void initColorWrapperList(Context context) {

        if (sColorWrappers.isEmpty()) {
            String jsonString = loadJson(context);
            sColorWrappers = deserializeColors(jsonString);
        }
    }

    private static String loadJson(Context context) {

        String jsonString;

        try {
            InputStream is = context.getAssets().open(COLORS_FILE_NAME);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return jsonString;
    }

    private static List<ColorWrapper> deserializeColors(String jsonString) {

        Gson gson = new Gson();

        Type collectionType = new TypeToken<List<ColorWrapper>>() {
        }.getType();
        return gson.fromJson(jsonString, collectionType);
    }

}