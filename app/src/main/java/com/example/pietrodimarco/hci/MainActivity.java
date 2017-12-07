package com.example.pietrodimarco.hci;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.text.Html;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.*;
import com.arlib.floatingsearchview.util.Util;
import com.example.pietrodimarco.hci.data.BaseItem;
import com.example.pietrodimarco.hci.data.ColorSuggestion;
import com.example.pietrodimarco.hci.data.ColorWrapper;
import com.example.pietrodimarco.hci.data.CustomDataProvider;
import com.example.pietrodimarco.hci.data.DataHelper;
import com.example.pietrodimarco.hci.data.GroupItem;
import com.example.pietrodimarco.hci.data.Item;
import com.example.pietrodimarco.hci.data.ResSearch;
import com.example.pietrodimarco.hci.data.RoomSuggestion;
import com.example.pietrodimarco.hci.views.LevelBeamView;
import com.google.gson.JsonElement;
import com.indooratlas.android.sdk.IALocation;
import com.indooratlas.android.sdk.IALocationListener;
import com.indooratlas.android.sdk.IALocationManager;
import com.indooratlas.android.sdk.IALocationRequest;
import com.indooratlas.android.sdk.IARegion;
import com.indooratlas.android.sdk.resources.IAResourceManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pl.openrnd.multilevellistview.ItemInfo;
import pl.openrnd.multilevellistview.MultiLevelListAdapter;
import pl.openrnd.multilevellistview.MultiLevelListView;
import pl.openrnd.multilevellistview.OnItemClickListener;

import static com.mapbox.mapboxsdk.style.layers.Property.NONE;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.visibility;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, IALocationListener
        , OnMapReadyCallback, MapboxMap.OnMapClickListener, LocationListener {


    private BottomSheetBehavior mBottomSheetBehavior1;
    private BottomSheetBehavior mBottomSheetBehaviorNav;
    private Button continueNavigationButton;
    private Button terminateNavigationButton;
    FloatingSearchView mSearchView;


    private MapView mapView;
    private final int CODE_PERMISSIONS = 1;
    private MarkerViewOptions marker;
    private IALocationManager mIALocationManager;
    private IAResourceManager mResourceManager;
    private MapboxMap mapboxMap;
    private boolean mShowIndoorLocation = false;
    private String mLastQuery="";

    private Marker featureMarker;
    private int featureMarkerFloor = 0;

    private boolean isInNavigationMode = false;
    private boolean isInFollowMode = false;

    private PolylineOptions path;
    private ArrayList<LatLng> floor1_points;
    private ArrayList<LatLng> floor2_points;
    private boolean isPathDisplayed = false;
    public static final long FIND_SUGGESTION_SIMULATED_DELAY = 250;





    private LatLng currentLocation = new LatLng(41.869912, -87.647903);
    private int currentFloor = 1;
    private float currentBearing = 0;

    private MultiLevelListView multiLevelListView;
    private int displayedFloor = 1 ;
    private boolean isLocating = false;

    private Icon icon;




    private FloatingActionButton sheetbutton;

    private LinearLayout floorButtons;
    public ListAdapter listAdapter;
    public boolean deleteMode;
    public Set<String> recents;
    public Set<String> favourites;

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


        continueNavigationButton = (Button) findViewById(R.id.continueNavigationButton);
        continueNavigationButton.setVisibility(View.GONE);
        continueNavigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInFollowMode = true;
                CameraPosition position = new CameraPosition.Builder()
                        .target(currentLocation) // Sets the new camera position
                        .zoom(20) // Sets the zoom
                        .bearing(currentBearing) // Rotate the camera
                        .tilt(60) // Set the camera tilt
                        .build(); // Creates a CameraPosition from the builder
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
                continueNavigationButton.setVisibility(View.GONE);

            }
        });

        terminateNavigationButton = (Button) findViewById(R.id.terminateNavigationButton);
        terminateNavigationButton.setVisibility(View.GONE);
        terminateNavigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                terminateNavigationButton.setVisibility(View.GONE);
                leaveNavigationMode();
            }
        });


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
        mSearchView= findViewById(R.id.floating_search_view);
        mSearchView.attachNavigationDrawerToMenuButton(drawer);
        DataHelper.load(this);
        setupSearchBar();



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final Button floor1Button = findViewById(R.id.floor1Button);
        final Button floor2Button = findViewById(R.id.floor2Button);



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

        FloatingActionButton navigateButton = (FloatingActionButton) findViewById(R.id.navigateButton);
        navigateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                enterNavigationMode();

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

        View navigationSheet = findViewById(R.id.sheet2);
        mBottomSheetBehaviorNav = BottomSheetBehavior.from(navigationSheet);
        mBottomSheetBehaviorNav.setHideable(true);
        //mBottomSheetBehaviorNav.setPeekHeight(300);
        mBottomSheetBehaviorNav.setState(BottomSheetBehavior.STATE_HIDDEN);


        confMenu();
        //listAdapter.addFavourite("2068");
        //addRecent();
        //addFavourite("Room 2048");

        IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        icon = iconFactory.fromResource(R.drawable.ic_arrow);

    }


    private void setupSearchBar() {
        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {

                    //this shows the top left circular progress
                    //you can call it where ever you want, but
                    //it makes sense to do it when loading something in
                    //the background.
                    mSearchView.showProgress();

                    //simulates a query call to a data source
                    //with a new query.
                    DataHelper.findSuggestions(newQuery, 5,
                            FIND_SUGGESTION_SIMULATED_DELAY, new DataHelper.OnFindSuggestionsListener() {

                                @Override
                                public void onResults(List<ColorSuggestion> results) {

                                    //this will swap the data and
                                    //render the collapse/expand animations as necessary
                                    mSearchView.swapSuggestions(results);

                                    //let the users know that the background
                                    //process has completed
                                    mSearchView.hideProgress();
                                }
                            });
                }

                Log.d(TAG, "onSearchTextChanged()");
                Log.d("Barra", "setOnQueryChangeListener");


            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final com.arlib.floatingsearchview.suggestions.model.SearchSuggestion searchSuggestion) {

                RoomSuggestion colorSuggestion = (RoomSuggestion) searchSuggestion;

                Log.d(TAG, "onSuggestionClicked()");

                mLastQuery = colorSuggestion.getBody();
                showPathFromSearch(DataHelper.findRoom(mLastQuery));
            }

            @Override
            public void onSearchAction(String query) {
                mLastQuery = query;

                showPathFromSearch(DataHelper.findRoom(mLastQuery));
                Log.d(TAG, "onSearchAction()");
            }
        });

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {

                //show suggestions when search bar gains focus (typically history suggestions)
                mSearchView.swapSuggestions(DataHelper.getHistory(3));

                Log.d(TAG, "onFocus()");
            }

            @Override
            public void onFocusCleared() {

                //set the title of the bar so that when focus is returned a new query begins
                mSearchView.setSearchBarTitle(mLastQuery);

                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                //mSearchView.setSearchText(searchSuggestion.getBody());

                Log.d(TAG, "onFocusCleared()");
            }
        });


        //handle menu clicks the same way as you would
        //in a regular activity
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {


            }
        });

        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHome"
        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {

                Log.d(TAG, "onHomeClicked()");
            }
        });

        /*
         * Here you have access to the left icon and the text of a given suggestion
         * item after as it is bound to the suggestion list. You can utilize this
         * callback to change some properties of the left icon and the text. For example, you
         * can load the left icon images using your favorite image loading library, or change text color.
         *
         *
         * Important:
         * Keep in mind that the suggestion list is a RecyclerView, so views are reused for different
         * items in the list.
         */
        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, com.arlib.floatingsearchview.suggestions.model.SearchSuggestion item, int itemPosition) {}


        });
    }

    private void showPathFromSearch(ResSearch room) {
        showBottomSheet(room.getRoom());
        showPathFromCurrentLocation(room.getRoom());

        if (featureMarker != null) {
            mapboxMap.removeMarker(featureMarker);
        }
        featureMarker = mapboxMap.addMarker(new MarkerViewOptions()
                .position(room.getLatLng())
                .title("Room")
                .snippet(room.getRoom())
        );
        featureMarkerFloor = displayedFloor;

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        mIALocationManager.registerRegionListener(mRegionListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mIALocationManager.destroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (mIALocationManager != null) {
            // unregister location & region changes
            mIALocationManager.removeLocationUpdates(this);
            mIALocationManager.registerRegionListener(mRegionListener);
        }

    }


    public void addRecent(){

        Set<String>  recents = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            recents = new ArraySet<>();
        }

        recents.add("2032");

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("Recent",recents);
        editor.commit();
    }

    public void addFavourite(){

        Set<String>  favourites = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            favourites = new ArraySet<>();
        }

        favourites.add("Room 2014");
        favourites.add("Room 2005");
        favourites.add("Delete");

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor.putStringSet("Favourite",favourites);
        }
        editor.commit();
    }


    private void confMenu() {
        multiLevelListView = (MultiLevelListView) findViewById(R.id.multiLevelMenu);

        // custom ListAdapter
        listAdapter = new ListAdapter(this);

        multiLevelListView.setAdapter(listAdapter);
        multiLevelListView.setOnItemClickListener(mOnItemClickListener);


        listAdapter.setDataItems(CustomDataProvider.getInitialItems());
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        private void showItemDescription(Object object, ItemInfo itemInfo) {
            StringBuilder builder = new StringBuilder("\"");
            String name = ((BaseItem) object).getName();
            builder.append(name);
            builder.append("\" clicked!\n");
            builder.append(getItemInfoDsc(itemInfo));

            if(name.equals("Restrooms")) {
                Intent intent = new Intent(MainActivity.this, ListViewAndroid.class);
                startActivity(intent);
            }

            if(name.equals("Delete")) {
                deleteMode = !deleteMode;
                //addFavourite();
                listAdapter.notifyDataSetChanged();
            }

            if(deleteMode && ((BaseItem) object).isFavourite() && !name.equals("Delete")){
                //delete Room
                listAdapter.delRoom(name);
            }

            Toast.makeText(MainActivity.this, builder.toString(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onItemClicked(MultiLevelListView parent, View view, Object item, ItemInfo itemInfo) {
            showItemDescription(item, itemInfo);
        }

        @Override
        public void onGroupItemClicked(MultiLevelListView parent, View view, Object item, ItemInfo itemInfo) {
            showItemDescription(item, itemInfo);
        }
    };

    private class ListAdapter extends MultiLevelListAdapter {

        MainActivity main;

        private class ViewHolder {
            TextView nameView;
            TextView infoView;
            ImageView arrowView;
            ImageView deleteView;
            LevelBeamView levelBeamView;
        }

        public ListAdapter(MainActivity main){
            this.main = main;
        }

        @Override
        public List<?> getSubObjects(Object object) {

            BaseItem baseItem = (BaseItem) object;
            List<BaseItem> result = new ArrayList<>();

            int level = ((GroupItem) baseItem).getLevel() + 1;
            String menuItem = baseItem.getName();

            if (!(baseItem instanceof GroupItem)) {
                throw new IllegalArgumentException("GroupItem required");
            }

            GroupItem groupItem = (GroupItem)baseItem;
            if(groupItem.getLevel() >= 2){
                return null;
            }

            switch (level){
                case 1 :
                    switch (menuItem.toUpperCase()){
                        case "FAVOURITES" :
                            result = getListFavourites();
                            break;
                        case "RECENTS" :
                            result = getListRecents();
                            break;
                    }
                    break;

            }

            return result;
        }

        public void addRecent(String room){
            SharedPreferences sharedPref = main.getPreferences(Context.MODE_PRIVATE);
            Set<String> returnValue = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                returnValue = sharedPref.getStringSet("Recent",new HashSet<String>());
            }

            Set<String> values = new HashSet<String>(returnValue);

            if(!values.contains(room))
                values.add(room);



            SharedPreferences.Editor editor = sharedPref.edit();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.putStringSet("Recent",values);
            }
            editor.commit();

            setDataItems(CustomDataProvider.getInitialItems());
        }

        public void addFavourite(String room){

            SharedPreferences sharedPref = main.getPreferences(Context.MODE_PRIVATE);
            Set<String> returnValue = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                returnValue = sharedPref.getStringSet("Favourite",new HashSet<String>());
            }

            Set<String> values = new HashSet<String>(returnValue);

            if(!values.contains(room))
                values.add(room);



            SharedPreferences.Editor editor = sharedPref.edit();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.putStringSet("Favourite",values);
            }
            editor.commit();
            setDataItems(CustomDataProvider.getInitialItems());
        }

        public void delRoom(String room){
            SharedPreferences sharedPref = main.getPreferences(Context.MODE_PRIVATE);
            Set<String> returnValue = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                returnValue = sharedPref.getStringSet("Favourite",new HashSet<String>());
            }

            Set<String> values = new HashSet<String>(returnValue);

            values.remove(room);


            SharedPreferences.Editor editor = sharedPref.edit();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                editor.putStringSet("Favourite",values);
            }
            editor.commit();
            setDataItems(CustomDataProvider.getInitialItems());
            //listAdapter.notifyDataSetChanged();
        }

        public boolean isInFavourites(String room){
            List<BaseItem> baseItems = getListFavourites();

            for (BaseItem b:
                 baseItems) {
                if(b.getName().equals(room))
                    return true;
            }
            return false;
        }

        private List<BaseItem> getListFavourites(){

            List<BaseItem> list = new ArrayList<>();

            SharedPreferences sharedPref = main.getPreferences(Context.MODE_PRIVATE);
            Set<String> returnValue = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                returnValue = sharedPref.getStringSet("Favourite",null);
            }

            for (String s:
                    returnValue) {
                list.add(new Item(s,true));
            }

            return list;
        }

        private List<BaseItem> getListRecents(){

            List<BaseItem> list = new ArrayList<>();

            SharedPreferences sharedPref = main.getPreferences(Context.MODE_PRIVATE);
            Set<String> returnValue = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
                returnValue = sharedPref.getStringSet("Recent",null);
            }

            for (String s:
                    returnValue) {
                list.add(new Item(s,false));
            }

            return list;
        }

        @Override
        public boolean isExpandable(Object object) {
            return CustomDataProvider.isExpandable((BaseItem) object);
        }

        @Override
        public View getViewForObject(Object object, View convertView, ItemInfo itemInfo) {
            ViewHolder viewHolder;

            String name = ((BaseItem) object).getName();

            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.data_item, null);
                //viewHolder.infoView = (TextView) convertView.findViewById(R.id.dataItemInfo);
                viewHolder.nameView = (TextView) convertView.findViewById(R.id.dataItemName);
                viewHolder.arrowView = (ImageView) convertView.findViewById(R.id.dataItemArrow);
                viewHolder.deleteView = (ImageView) convertView.findViewById(R.id.dataItemDelete);
                viewHolder.levelBeamView = (LevelBeamView) convertView.findViewById(R.id.dataItemLevelBeam);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.nameView.setText(((BaseItem) object).getName());
            //viewHolder.infoView.setText(getItemInfoDsc(itemInfo));

            if (itemInfo.isExpandable()) {
                viewHolder.arrowView.setVisibility(View.VISIBLE);
                viewHolder.deleteView.setVisibility(View.GONE);
                viewHolder.arrowView.setImageResource(itemInfo.isExpanded() ?
                        R.drawable.ic_expand_less : R.drawable.ic_expand_more);
            } else {
                viewHolder.arrowView.setVisibility(View.GONE);
                viewHolder.deleteView.setVisibility(View.GONE);
                if(deleteMode){
                    if(((BaseItem) object).isFavourite() && !name.equals("Delete"))
                        viewHolder.deleteView.setVisibility(View.VISIBLE);
                    else
                        viewHolder.deleteView.setVisibility(View.GONE);

                }
                //////////////////////////////////////////////////////
            }

            viewHolder.levelBeamView.setLevel(itemInfo.getLevel());

            return convertView;
        }
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
        if(mapboxMap == null)
            return;
        if (!mShowIndoorLocation) {
            Log.d(TAG, "new LocationService location received with coordinates: " + location.getLatitude()
                    + "," + location.getLongitude());

            showLocationCircle(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    location.getAccuracy(),location.getBearing() );
        }


    }
    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onLocationChanged(IALocation location) {

        Log.d("myTag", "Location updated");

        final LatLng center = new LatLng(location.getLatitude(), location.getLongitude());

        if (mShowIndoorLocation) {
            showLocationCircle(center, location.getAccuracy(), location.getBearing());
            currentFloor = location.getFloorLevel();
            if(isInFollowMode){
                displayFloor(location.getFloorLevel());
            }

        }


        /*IconFactory iconFactory = IconFactory.getInstance(MainActivity.this);
        Icon icon = iconFactory.fromResource(R.drawable.mapbox_mylocation_icon_default);

        MarkerViewOptions marker2 = new MarkerViewOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .title("Location")
                .snippet(" ")
                .icon(icon);
        mapboxMap.addMarker(marker2);*/

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
                            showBottomSheet(String.valueOf(entry.getValue()));
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


    private void showBottomSheet(final String entry) {
        View bottomSheet = findViewById(R.id.sheet1);
        TextView title = findViewById(R.id.bottomSheet_Title);
        title.setText("Room: " + entry );
        final ImageButton favButton = (ImageButton) findViewById(R.id.favButton);
        favButton.setImageResource(R.drawable.ic_star_off);
        if(listAdapter.isInFavourites(entry)){
            favButton.setImageResource(R.drawable.ic_star_on);
        }
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(! listAdapter.isInFavourites(entry)){
                    listAdapter.addFavourite(entry);
                    Toast toast = Toast.makeText(getApplicationContext(), "Room " + entry + "added to the favorites", Toast.LENGTH_SHORT);
                    toast.show();
                    favButton.setImageResource(R.drawable.ic_star_on);
                }else{
                    listAdapter.delRoom(entry);
                    Toast toast = Toast.makeText(getApplicationContext(), "Room " + entry + "removed from the favorites", Toast.LENGTH_SHORT);
                    toast.show();
                    favButton.setImageResource(R.drawable.ic_star_off);
                }
            }
        });
        mBottomSheetBehavior1 = BottomSheetBehavior.from(bottomSheet);
        if(mBottomSheetBehavior1.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);
            sheetbutton.setVisibility(View.VISIBLE);
        }
    }


    private void showPathFromCurrentLocation(String room){
        if(isLocating){
            int start = getStartingPoint(currentLocation, currentFloor);
            getWPAndDrawPath(start,room);
        }else{
            Toast toast = Toast.makeText(getApplicationContext(), "Enable localization to show the path", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public int getStartingPoint(LatLng latLng, int floor){
        WayFinder wayfinder = new WayFinder(getApplicationContext());
        return wayfinder.getStartingPoint(latLng, floor);
    }


    @Override
    public void onMapReady(MapboxMap mapboxMapready) {
        this.mapboxMap = mapboxMapready;

        marker = new MarkerViewOptions()
                .position(new LatLng(41.869912, -87.647903))
                .title("Location")
                .snippet("Your Location")
                .icon(icon);

        mapboxMap.addMarker(marker);
        marker.getMarker().setVisible(isLocating);
        mapboxMap.setOnMapClickListener(this);
        displayFloor(1);
        //getWPAndDrawPath(1, "2068");
        mapboxMap.getUiSettings().setCompassMargins(10,200,10,0);
        mapboxMap.addOnCameraMoveStartedListener(new MapboxMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason == REASON_API_GESTURE){
                    if(isInFollowMode){
                        isInFollowMode = false;
                        continueNavigationButton.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
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


    private String TAG = "IA";

    private IARegion.Listener mRegionListener = new IARegion.Listener() {
        @Override
        public void onEnterRegion(IARegion region) {
            if (region.getType() == IARegion.TYPE_FLOOR_PLAN) {
                final String newId = region.getId();
                mShowIndoorLocation = true;
            }
        }
        @Override
        public void onExitRegion(IARegion region) {
            mShowIndoorLocation = false;
        }
    };

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



    private void showLocationCircle(LatLng latLng, float accuracy, float bearing) {

        float markerBearing;
        if(isInNavigationMode){
            markerBearing = 0;
        }else{

            double cam = mapboxMap.getCameraPosition().bearing;
            markerBearing = bearing + (float) cam ;
        }
       /* if(!marker.isVisible())
            marker.visible(true);

        marker.getMarker().getPosition().setLatitude(latLng.getLatitude());
        marker.getMarker().getPosition().setLongitude(latLng.getLongitude());
        marker.getMarker().setRotation(markerBearing);
        //drawCircle(mapboxMap, latLng, Color.parseColor("#3bb2d0"),10);*/

       mapboxMap.removeMarker(marker.getMarker());
        marker = new MarkerViewOptions()
                .position(latLng)
                .rotation(markerBearing)
                .title("Location")
                .snippet("Your Location")
                .icon(icon);

        mapboxMap.addMarker(marker);
        if(isInFollowMode){
            CameraPosition position = new CameraPosition.Builder()
                    .target(latLng) // Sets the new camera position
                    .zoom(20) // Sets the zoom
                    .bearing(bearing) // Rotate the camera
                    .tilt(60) // Set the camera tilt
                    .build(); // Creates a CameraPosition from the builder
            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(position), 1000);
        }
        mapboxMap.getMarkerViewManager().updateMarkerViewsPosition();

        currentBearing = bearing;
        currentLocation = latLng;

    }

    private PolygonOptions generatePerimeter(LatLng centerCoordinates, double radiusInKilometers, int numberOfSides) {
        List<LatLng> positions = new ArrayList<>();
        double distanceX = radiusInKilometers / (111.319 * Math.cos(centerCoordinates.getLatitude() * Math.PI / 180));
        double distanceY = radiusInKilometers / 110.574;

        double slice = (2 * Math.PI) / numberOfSides;

        double theta;
        double x;
        double y;
        LatLng position;
        for (int i = 0; i < numberOfSides; ++i) {
            theta = i * slice;
            x = distanceX * Math.cos(theta);
            y = distanceY * Math.sin(theta);

            position = new LatLng(centerCoordinates.getLatitude() + y,
                    centerCoordinates.getLongitude() + x);
            positions.add(position);
        }
        return new PolygonOptions()
                .addAll(positions)
                .fillColor(Color.BLUE)
                .alpha(0.4f);
    }




    private void displayFloor(int floor){

        Button floor1Button = findViewById(R.id.floor1Button);
        Button floor2Button = findViewById(R.id.floor2Button);
        ArrayList<LatLng> points=null;
        if(floor == 1){
            if(isInNavigationMode){
                floorVisibility(1, VISIBLE);
                floorVisibility(2, NONE);
                floorVisibility3D(1,VISIBLE);
                floorVisibility3D(2,NONE);

            }else {
                floorVisibility(1, VISIBLE);
                floorVisibility(2, NONE);
            }
            floor1Button.setBackgroundColor(Color.GRAY);
            floor2Button.setBackgroundColor(Color.WHITE);
            displayedFloor = 1;
            points = floor1_points;
        }
        if (floor == 2){
            if(isInNavigationMode){
                floorVisibility(1, NONE);
                floorVisibility(2, VISIBLE);
                floorVisibility3D(2,VISIBLE);
                floorVisibility3D(1,NONE);

            }else{
                floorVisibility(2,VISIBLE);
                floorVisibility(1,NONE);
            }
            floor2Button.setBackgroundColor(Color.GRAY);
            floor1Button.setBackgroundColor(Color.WHITE);
            displayedFloor = 2;
            points = floor2_points;
        }

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

                mapboxMap.removeMarker(featureMarker);
                featureMarker = mapboxMap.addMarker(new MarkerViewOptions().position(featureMarker.getPosition()).title(featureMarker.getTitle()));

            }
        }

    }

    public void floorVisibility(int floor, String vis) {


        mapboxMap.getLayer("FLOOR" + floor + "_rooms").setProperties(visibility(vis));
        mapboxMap.getLayer("FLOOR" + floor + "_walls").setProperties(visibility(vis));
        mapboxMap.getLayer("FLOOR" + floor + "_labels").setProperties(visibility(vis));

    }

    public void floorVisibility3D(int floor, String vis) {


        mapboxMap.getLayer("FLOOR" + floor + "_rooms_3D").setProperties(visibility(vis));
        //mapboxMap.getLayer("FLOOR" + floor + "_walls_3D").setProperties(visibility(vis));
        mapboxMap.getLayer("FLOOR" + floor + "_labels").setProperties(visibility(vis));

    }


    private void enterNavigationMode(){
        isInNavigationMode = true;
        displayFloor(displayedFloor);

        CameraPosition position = new CameraPosition.Builder()
                .target(currentLocation) // Sets the new camera position
                .zoom(22) // Sets the zoom
                .bearing(currentBearing) // Rotate the camera
                .tilt(60) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
        isInFollowMode = true;

        View navSheet = findViewById(R.id.sheet2);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
        sheetbutton.setVisibility(View.GONE);
        mBottomSheetBehaviorNav.setState(BottomSheetBehavior.STATE_EXPANDED);
        terminateNavigationButton.setVisibility(View.VISIBLE);


    }

    private void leaveNavigationMode() {
        floorVisibility3D(1,NONE);
        floorVisibility3D(2,NONE);
        isInFollowMode = false;
        isInNavigationMode = false;
        displayFloor(currentFloor);

        CameraPosition position = new CameraPosition.Builder()
                .target(currentLocation) // Sets the new camera position
                .zoom(17) // Sets the zoom
                .bearing(currentBearing) // Rotate the camera
                .tilt(0) // Set the camera tilt
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
        mBottomSheetBehaviorNav.setState(BottomSheetBehavior.STATE_HIDDEN);
        continueNavigationButton.setVisibility(View.GONE);


    }


    private String getItemInfoDsc(ItemInfo itemInfo) {
        StringBuilder builder = new StringBuilder();

        builder.append(String.format("level[%d], idx in level[%d/%d]",
                itemInfo.getLevel() + 1, /*Indexing starts from 0*/
                itemInfo.getIdxInLevel() + 1 /*Indexing starts from 0*/,
                itemInfo.getLevelSize()));

        if (itemInfo.isExpandable()) {
            builder.append(String.format(", expanded[%b]", itemInfo.isExpanded()));
        }
        return builder.toString();
    }






}
