package com.example.pietrodimarco.hci;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.google.gson.JsonElement;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.navigation.v5.navigation.MapboxNavigation;
import com.mapbox.services.commons.geojson.Feature;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import pl.openrnd.multilevellistview.MultiLevelListView;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IALocationListener
        , OnMapReadyCallback, MapboxMap.OnMapClickListener, LocationListener {


    private BottomSheetBehavior mBottomSheetBehavior1;

    private MapView mapView;
    private final int CODE_PERMISSIONS = 1;
    private MarkerViewOptions marker;
    private IALocationManager mIALocationManager;
    private IAResourceManager mResourceManager;
    private MapboxMap mapboxMap;
    private boolean mShowIndoorLocation = false;

    private Marker featureMarker;
    private int featureMarkerFloor = 0;

    private PolylineOptions path;
    private ArrayList<LatLng> floor1_points;
    private ArrayList<LatLng> floor2_points;
    private boolean isPathDisplayed = false;





    private LatLng currentLocation = new LatLng(41.869912, -87.647903);
    private int currentFloor = 1;

    private MultiLevelListView multiLevelListView;
    private int displayedFloor = 1 ;
    private boolean isLocating = false;



    private FloatingActionButton sheetbutton;

    private LinearLayout floorButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        String[] neededPermissions = {
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
        };
        ActivityCompat.requestPermissions(this, neededPermissions, CODE_PERMISSIONS);


        Mapbox.getInstance(this, "pk.eyJ1IjoiZ3JvdXAzaGNpIiwiYSI6ImNqOXhkZTU0MDB0bnAzM3Bva2JyY2M2Mm8ifQ.wimKY4mWCu4Pr8SIOlR_Qg");
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);


        mIALocationManager = IALocationManager.create(this);
        mResourceManager = IAResourceManager.create(this);


        MapboxNavigation navigation = new MapboxNavigation(this, "pk.eyJ1IjoiZ3JvdXAzaGNpIiwiYSI6ImNqOXhkZTU0MDB0bnAzM3Bva2JyY2M2Mm8ifQ.wimKY4mWCu4Pr8SIOlR_Qg");


        FloatingActionButton locateButton = (FloatingActionButton) findViewById(R.id.locateButton);
        locateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                if (!isLocating){
                    startLocating();
                }else{
                    focusOnLocation();
                }
            }
        });




        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        FloatingSearchView searchBar= findViewById(R.id.floating_search_view);
        searchBar.attachNavigationDrawerToMenuButton(drawer);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final Button floor1Button = findViewById(R.id.floor1Button);
        final Button floor2Button = findViewById(R.id.floor2Button);
        LinearLayout barLayour=findViewById(R.id.searchBar);
        FloatingSearchView search=findViewById(R.id.floating_search_view);


        floor1Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayFloor(1);
            }
        });

        floor2Button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                displayFloor(2);
            }
        });






        View bottomSheet = findViewById(R.id.sheet1);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheet);
        mBottomSheetBehavior1.setHideable(true);
        mBottomSheetBehavior1.setPeekHeight(300);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);

        sheetbutton = (FloatingActionButton) findViewById(R.id.navigateButton);
        sheetbutton.setVisibility(View.GONE);
        floorButtons = (LinearLayout) findViewById(R.id.floorButtons);


        mBottomSheetBehavior1.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    sheetbutton.setVisibility(View.VISIBLE);

                }
                else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    sheetbutton.setVisibility(View.VISIBLE);

                }
                else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    sheetbutton.setVisibility(View.GONE);

                }
            }

            @Override
            public void onSlide(View bottomSheet, float slideOffset) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }





    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(IALocation iaLocation) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        if (featureMarker != null) {
            mapboxMap.removeMarker(featureMarker);
        }

        final PointF pixel = mapboxMap.getProjection().toScreenLocation(point);
        List<Feature> features = mapboxMap.queryRenderedFeatures(pixel);

        if (features.size() > 0) {
            for (Feature feature : features) {
                if (feature.getProperties() != null) {
                    for (Map.Entry<String, JsonElement> entry : feature.getProperties().entrySet()) {
                        if (entry.getKey().equals("Room")) {
                            featureMarker = mapboxMap.addMarker(new MarkerViewOptions()
                                    .position(point)
                                    .title("Room")
                                    .snippet(String.valueOf(entry.getValue()))
                            );
                            featureMarkerFloor = displayedFloor;
                            //mapboxMap.selectMarker(featureMarker);
                            showBottomSheet(entry);
                            showPathFromCurrentLocation(String.valueOf(entry.getValue()));
                            return;
                        }
                    }
                }
            }
        }
        featureMarker = mapboxMap.addMarker(new MarkerViewOptions()
                .position(point)
                .snippet("hello")
        );

    }


    private void showBottomSheet(Map.Entry<String, JsonElement> entry) {
        View bottomSheet = findViewById(R.id.sheet1);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheet);
        if(mBottomSheetBehavior1.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);

            sheetbutton.setVisibility(View.VISIBLE);

        }

    }


    private void showPathFromCurrentLocation(String room){
        if(isLocating){
            int start = getStartingPoint(currentLocation, currentFloor);
            getWPAndDrawPath(start,room);

        }else{
            Toast toast = Toast.makeText(getApplicationContext(), "Enable localization to show the path", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    public int getStartingPoint(LatLng latLng, int floor){

        WayFinder wayfinder = new WayFinder(getApplicationContext());

        return wayfinder.getStartingPoint(latLng, floor);

    }


    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.mapbox_mylocation_icon_bearing);
        marker = new MarkerViewOptions()
                .position(new LatLng(41.869912, -87.647903))
                .title("Location")
                .snippet("Welcome to you")
                .icon(icon);


        mapboxMap.addMarker(marker);
        marker.getMarker().setVisible(isLocating);
        mapboxMap.setOnMapClickListener(this);
        displayFloor(1);

        getWPAndDrawPath(1, "2068");

    }


    public void getWPAndDrawPath(int startingPoint, String destRoom) {
        WayFinder navigation = new WayFinder(getApplicationContext(), startingPoint, destRoom, this);
        Thread nuovoThread = new Thread(navigation);
        nuovoThread.start();


    }

    public void drawPath(ArrayList<LatLng> points1, ArrayList<LatLng> points2) {
        if (path!=null){
            mapboxMap.removePolyline(path.getPolyline());
        }
        floor1_points = points1;
        floor2_points = points2;
        isPathDisplayed = true;
        if (displayedFloor == 1){
            path = new PolylineOptions()
                    .addAll(points1)
                    .color(Color.parseColor("#3bb2d0"))
                    .width(5);
        }
        if (displayedFloor == 2){
            path = new PolylineOptions()
                    .addAll(points2)
                    .color(Color.parseColor("#3bb2d0"))
                    .width(5);
        }



        mapboxMap.addPolyline(path);


    }








    private void startLocating() {

        Toast toast = Toast.makeText(getApplicationContext(), "Localization Enabled", Toast.LENGTH_LONG);
        toast.show();
        isLocating = true;

        mIALocationManager.requestLocationUpdates(IALocationRequest.create(), this);
        startListeningPlatformLocations();

    }

    private void focusOnLocation() {
        CameraPosition position = new CameraPosition.Builder()
                .target(currentLocation) // Sets the new camera position
                .zoom(20) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position), 1000);

    }

    private void startListeningPlatformLocations() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        }
    }


    private void displayFloor(int floor){
        Button floor1Button = findViewById(R.id.floor1Button);
        Button floor2Button = findViewById(R.id.floor2Button);
        ArrayList<LatLng> points=null;
        if(floor == 1){
            floorVisibility(1,VISIBLE);
            floorVisibility(2,NONE);
            floor1Button.setBackgroundColor(Color.GRAY);
            floor2Button.setBackgroundColor(Color.WHITE);
            displayedFloor = 1;
            points = floor1_points;
        }
        if (floor == 2){
            floorVisibility(2,VISIBLE);
            floorVisibility(1,NONE);
            floor2Button.setBackgroundColor(Color.GRAY);
            floor1Button.setBackgroundColor(Color.WHITE);
            displayedFloor = 2;
            points = floor2_points;
        }
        mapboxMap.removeAnnotations();
        if(isPathDisplayed){
            if (path!=null){
                mapboxMap.removePolyline(path.getPolyline());
            }
            path = new PolylineOptions()
                    .addAll(points)
                    .color(Color.parseColor("#3bb2d0"))
                    .width(5);
            mapboxMap.addPolyline(path);

        }
        if (featureMarkerFloor != displayedFloor){
            if (featureMarker != null) {
                mapboxMap.removeMarker(featureMarker);
            }
        }else{
            if (featureMarker != null) {

                featureMarker = mapboxMap.addMarker(new MarkerViewOptions().position(featureMarker.getPosition()).title(featureMarker.getTitle()));

            }
        }

    }
    public void floorVisibility(int floor, String vis) {


        mapboxMap.getLayer("FLOOR" + floor + "_rooms").setProperties(visibility(vis));
        mapboxMap.getLayer("FLOOR" + floor + "_walls").setProperties(visibility(vis));
        mapboxMap.getLayer("FLOOR" + floor + "_labels").setProperties(visibility(vis));

    }
}
