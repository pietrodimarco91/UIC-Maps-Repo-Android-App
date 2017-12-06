package com.example.pietrodimarco.hci;

import android.content.Context;
import android.util.Log;

import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by pietrodimarco on 11/17/17.
 */

public class WayFinder implements Runnable{

    ArrayList<Tag> tags;
    ArrayList<Path> possiblePaths;
    Context appContext;
    int startingPoint;
    String destRoom;
    MainActivity main;
    Tag dest=null;

    public WayFinder(Context applicationContext, int startingPoint, String destRoom, MainActivity main) {
        this.appContext=applicationContext;
        this.startingPoint = startingPoint;
        this.destRoom = destRoom;
        this.main =main;
    }

    public WayFinder(Context applicationContext) {
        this.appContext=applicationContext;
    }

    public void startNavigation() {

        Tag startingTag = null;
        for (int i = 0; i < tags.size(); i++){
            if(tags.get(i).getRoom().equals(destRoom)){
                Log.d("clicked: ",tags.get(i).getRoom() );
                dest=tags.get(i);
            }
        }



        for (int i = 0; i < tags.size(); i++){
            if (tags.get(i).getId() == startingPoint)
                startingTag = tags.get(i);
        }

        Log.d("clicked: ", String.valueOf(dest));

        //the frontier represent the nodes that have to be visited
        LinkedList<Tag> frontier = new LinkedList<>();

        //the frontier represent the future possible states
        ArrayList<Tag> futureStates;

        ArrayList<Integer> alreadyVis=new ArrayList<>();



        Tag iterationNode;
        if (!goalState(startingTag, dest)) {
            frontier.add(startingTag);
            while (!frontier.isEmpty() ) {
                iterationNode = frontier.getFirst();
                alreadyVis.add(iterationNode.getId());

                frontier.removeFirst();
                futureStates = getFutureStates(iterationNode,alreadyVis);
                for (int i = 0; i < futureStates.size(); i++) {
                        if (goalState(futureStates.get(i),dest)) {
                            printSolution(startingPoint,futureStates.get(i));
                            return;
                        } else
                            frontier.add(futureStates.get(i));
                    }

                }
        }else printSolution(startingPoint,startingTag);
        Log.d("result","path non trovato");
    }

    private void printSolution(int startingPoint, Tag goal) {
        ArrayList<LatLng> points1 = new ArrayList<LatLng>();
        ArrayList<LatLng> points2 = new ArrayList<LatLng>();
        if(dest.floor==1)
            points1.add(new LatLng(dest.point.getPosition().getLatitude(), dest.point.getPosition().getLongitude()));
        else
            points2.add(new LatLng(dest.point.getPosition().getLatitude(), dest.point.getPosition().getLongitude()));
        Log.d("result", String.valueOf(goal.id));
        while (goal.id!=startingPoint){
            Log.d("result", String.valueOf(goal.id));
            if(goal.floor==1)
                points1.add(new LatLng(goal.point.getPosition().getLatitude(), goal.point.getPosition().getLongitude()));
            else
                points2.add(new LatLng(goal.point.getPosition().getLatitude(), goal.point.getPosition().getLongitude()));

            goal=goal.father;
        }
        if(goal.floor==1)
            points1.add(new LatLng(goal.point.getPosition().getLatitude(), goal.point.getPosition().getLongitude()));
        else
            points2.add(new LatLng(goal.point.getPosition().getLatitude(), goal.point.getPosition().getLongitude()));

        main.drawPath(points1,points2);


    }


    private ArrayList<Tag> getFutureStates(Tag iterationNode, ArrayList<Integer> alreadyVis) {
        ArrayList<Tag> fut=new ArrayList<>();
        if(iterationNode.optW!=0 && !alreadyVis.contains(iterationNode.optW)){
            for (int i=0;i<tags.size();i++)
                if(tags.get(i).id==iterationNode.optW){
                    tags.get(i).addFather(iterationNode);
                    fut.add(tags.get(i));
                }
        }
        if(iterationNode.optS!=0 && !alreadyVis.contains(iterationNode.optS)){
            for (int i=0;i<tags.size();i++)
                if(tags.get(i).id==iterationNode.optS){
                    tags.get(i).addFather(iterationNode);
                    fut.add(tags.get(i));
                }
        }
        if(iterationNode.optN!=0 && !alreadyVis.contains(iterationNode.optN)){
            for (int i=0;i<tags.size();i++)
                if(tags.get(i).id==iterationNode.optN){
                    tags.get(i).addFather(iterationNode);
                    fut.add(tags.get(i));
                }
        }
        if(iterationNode.optE!=0 && !alreadyVis.contains(iterationNode.optE)){
            for (int i=0;i<tags.size();i++)
                if(tags.get(i).id==iterationNode.optE) {
                    tags.get(i).addFather(iterationNode);
                    fut.add(tags.get(i));
                }
        }
        if(iterationNode.optUp!=0 && !alreadyVis.contains(iterationNode.optUp)){
            for (int i=0;i<tags.size();i++)
                if(tags.get(i).id==iterationNode.optUp){
                    tags.get(i).addFather(iterationNode);
                    fut.add(tags.get(i));
                }
        }
        if(iterationNode.optDown!=0 && !alreadyVis.contains(iterationNode.optDown)){
            for (int i=0;i<tags.size();i++)
                if(tags.get(i).id==iterationNode.optDown) {
                    tags.get(i).addFather(iterationNode);
                    fut.add(tags.get(i));
                }
        }
        return fut;
    }


    private boolean goalState(Tag child, Tag destRoom) {
        if(destRoom.optE==child.getId() || destRoom.optN==child.getId() || destRoom.optS==child.getId() || destRoom.optW==child.getId()){
            Log.d("goalstate", String.valueOf(child.id) + destRoom.getRoom());
            return true;
    }
        else return false;
    }


    @Override
    public void run() {
        tags = new ArrayList<>();
        String room;
        LoadJson string=new LoadJson();
        try {
            FeatureCollection geoJSON = (FeatureCollection) GeoJSON.parse(string.loadJSONFromAsset(appContext));
            for (int i = 0; i < geoJSON.getFeatures().size(); i++) {
                if(geoJSON.getFeatures().get(i).getProperties().isNull("room"))
                    room="";
                else{
                    room= geoJSON.getFeatures().get(i).getProperties().getString("room");
                }

                tags.add(new Tag(geoJSON.getFeatures().get(i).getProperties().getInt("id"), geoJSON.getFeatures().get(i).getProperties().getInt("optE"), geoJSON.getFeatures().get(i).getProperties().getInt("optW"), geoJSON.getFeatures().get(i).getProperties().getInt("optS"), geoJSON.getFeatures().get(i).getProperties().getInt("optN"), geoJSON.getFeatures().get(i).getProperties().getInt("floor"), geoJSON.getFeatures().get(i).getProperties().getInt("optUp"), geoJSON.getFeatures().get(i).getProperties().getInt("optDown"),  room, (Point) geoJSON.getFeatures().get(i).getGeometry()));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        startNavigation();
    }

    public int getStartingPoint(LatLng latLng, int floor){
        ArrayList<Tag> tags = new ArrayList<Tag>();
        String room;
        LoadJson string=new LoadJson();
        try {
            FeatureCollection geoJSON = (FeatureCollection) GeoJSON.parse(string.loadJSONFromAsset(appContext));
            for (int i = 0; i < geoJSON.getFeatures().size(); i++) {
                if(geoJSON.getFeatures().get(i).getProperties().isNull("room"))
                    room="";
                else
                    room= geoJSON.getFeatures().get(i).getProperties().getString("room");
                tags.add(new Tag(geoJSON.getFeatures().get(i).getProperties().getInt("id"), geoJSON.getFeatures().get(i).getProperties().getInt("optE"), geoJSON.getFeatures().get(i).getProperties().getInt("optW"), geoJSON.getFeatures().get(i).getProperties().getInt("optS"), geoJSON.getFeatures().get(i).getProperties().getInt("optN"), geoJSON.getFeatures().get(i).getProperties().getInt("floor"), geoJSON.getFeatures().get(i).getProperties().getInt("optUp"), geoJSON.getFeatures().get(i).getProperties().getInt("optDown"), room, (Point) geoJSON.getFeatures().get(i).getGeometry()));
            }
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        double lat = latLng.getLatitude();
        double lng = latLng.getLongitude();
        double minDist = Double.MAX_VALUE;
        Tag nearestTag = null;
        for(Tag tag : tags){
            double distance = Math.sqrt(Math.pow((lat-tag.point.getPosition().getLatitude()), 2) + Math.pow((lng-tag.point.getPosition().getLongitude()), 2));
            if(distance<minDist && tag.floor == floor){
                minDist = distance;
                nearestTag = tag;
            }

        }
        return nearestTag.getId();
    }
}
