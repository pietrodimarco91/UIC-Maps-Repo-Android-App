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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class ListViewAndroid extends Activity {
    ListView listView ;
    List<Restroom> restrooms;
   



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bathrooms_list);

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);


        addRestrooms();

        RestroomManager rm = new RestroomManager(this);
        rm.run();
        restrooms = rm.getRestrooms();
        List<String> stringsR = new ArrayList<>();
        stringsR.add("Restrooms list");
        stringsR.add("Name     Gender     Availability");
        for (Restroom r:
             restrooms) {
            stringsR.add(r.toString());
        }

        String[] values = new String[stringsR.size()];
        stringsR.toArray(values);

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

    }

}