package com.example.pietrodimarco.hci;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;


public class ListViewAndroid extends Activity {
    ListView listView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bathrooms_list);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);


        addRestrooms();


        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        Set<String> returnValue = sharedPref.getStringSet("Favourite",null);

        Log.d("SUCA", returnValue.toString());
        String[] values = new String[returnValue.size()];
        returnValue.toArray(values);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, values);


        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) listView.getItemAtPosition(position);

                // Show Alert
                Toast.makeText(getApplicationContext(),
                        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                        .show();

            }

        });
    }

    public void addRestrooms(){

        Set<String>  favourites = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            favourites = new ArraySet<>();
        }

        favourites.add("Room 2067");
        favourites.add("Room 1024");

        Log.d("DIO", "PORCO");

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putStringSet("Favourite",favourites);
        editor.commit();
    }

}