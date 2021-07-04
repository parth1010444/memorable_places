package com.example.memorableplaces;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> places;
    static ArrayList<LatLng> locations;

    static  customArrayAdoptor arrayAdoptor;

    static SharedPreferences sharedPreferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listView = findViewById(R.id.listView);
        places = new ArrayList<>();
        locations = new ArrayList<LatLng>();
         sharedPreferences = this.getSharedPreferences("com.example.memorableplaces", Context.MODE_PRIVATE);


         ArrayList<String> latitudes = new ArrayList<>();
         ArrayList<String> longitudes = new ArrayList<>();

        places.clear();
        longitudes.clear();
        latitudes.clear();
        locations.clear();

        try{
             places = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("places",ObjectSerializer.serialize(new ArrayList<String>())));
             latitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("latitudes",ObjectSerializer.serialize(new ArrayList<String>())));
             longitudes = (ArrayList<String>) ObjectSerializer.deserialize(sharedPreferences.getString("longitudes",ObjectSerializer.serialize(new ArrayList<String>())));


         }catch (Exception e){
             e.printStackTrace();
         }


        if(places.size() >0 && latitudes.size() >0 && longitudes.size() >0){
            if(places.size() == latitudes.size() && places.size()==longitudes.size()){
                for(int i=0;i<latitudes.size();i++){
                    locations.add(new LatLng(Double.parseDouble(latitudes.get(i)),Double.parseDouble(longitudes.get(i))));
                }
            }
        }else{
            places.add("Add a new place...");
            locations.add(new LatLng(0,0));
        }
        
         arrayAdoptor = new customArrayAdoptor(this, R.layout.list_item_view,places);

         listView.setAdapter(arrayAdoptor);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Intent intent =new Intent(getApplicationContext(),MapsActivity.class);
               intent.putExtra("placenumber",position);
               startActivity(intent);
            }
        });



        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                Log.i("Item Long Click","Success");

                if(position == 0){
                    Toast.makeText(getApplicationContext(),"Sorry this cannot be deleted",Toast.LENGTH_SHORT).show();
                }else
                {
                    new AlertDialog.Builder(MainActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Delete this entry")
                            .setMessage("Are You Sure?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    places.remove(position);
                                    locations.remove(position);
                                    sharedPreferences.edit().clear().commit();
                                    try {

                                        ArrayList<String> latitudes = new ArrayList<>();
                                        ArrayList<String> longitudes = new ArrayList<>();
                                        for (LatLng coordinates : MainActivity.locations) {
                                            latitudes.add(Double.toString(coordinates.latitude));
                                            longitudes.add(Double.toString(coordinates.longitude));
                                        }

                                        MainActivity.sharedPreferences.edit().putString("places", ObjectSerializer.serialize(MainActivity.places)).apply();
                                        MainActivity.sharedPreferences.edit().putString("latitudes", ObjectSerializer.serialize(latitudes)).apply();
                                        MainActivity.sharedPreferences.edit().putString("longitudes", ObjectSerializer.serialize(longitudes)).apply();
                                        arrayAdoptor.notifyDataSetChanged();

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            })
                            .setNegativeButton("No", null).show();


                }

                return true;
            }
        });
    }
}