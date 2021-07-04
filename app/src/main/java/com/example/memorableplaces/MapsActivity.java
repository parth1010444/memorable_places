package com.example.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.memorableplaces.databinding.ActivityMapsBinding;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    LocationManager locationManager;
    LocationListener locationListener;

    Location currLocation;

    public void centerMapOnLocation(Location location,String title){
        if(location == null){
            return;
        }
        mMap.clear();
        LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,10));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,150000,1000000,locationListener);
                Location lastKnownLocation =locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation,"Your Location");


            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("onCreate","success");

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        Log.i("onMapReady","success");
        mMap.setOnMapLongClickListener(this::onMapLongClick);
        Intent intent =getIntent();
        int position=intent.getIntExtra("placenumber",0);

        if(position == 0) {
            //Zoom in on user Location
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {

                    Log.i("OnlocationChange","success");
                    centerMapOnLocation(location, "Your Location");
                    currLocation =location;
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.i("onStatusChanged","success");
                }

                @Override
                public void onProviderEnabled( String provider) {
                    Log.i("onProviderEnabled","success");


                }

                @Override
                public void onProviderDisabled( String provider) {
                    Log.i("onProviderDisabled","success");


                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }

            };

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 1000000, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centerMapOnLocation(lastKnownLocation, "Your Location");

            } else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }
        else{
            Location placeLoaction = new Location(LocationManager.GPS_PROVIDER);

            placeLoaction.setLatitude(MainActivity.locations.get(position).latitude);
            placeLoaction.setLongitude(MainActivity.locations.get(position).longitude);

            centerMapOnLocation(placeLoaction,MainActivity.places.get(position));
        }
    }

    @Override
    public void onMapLongClick(LatLng latLng) {


        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address="";

        try{

            List<Address> listAddress = geocoder.getFromLocation(latLng.latitude, latLng.longitude,1);
            if(listAddress != null && listAddress.size() > 0){

                if(listAddress.get(0).getThoroughfare() != null){
                    if(listAddress.get(0).getSubThoroughfare() != null){
                        address += listAddress.get(0).getSubThoroughfare() + " ";
                    }

                    address += listAddress.get(0).getThoroughfare() + " ";
                }
            }


        }catch(Exception e){
            e.printStackTrace();
        }
        if(address.equals("")){
            SimpleDateFormat sdf= new SimpleDateFormat("HH:mm yyyy-MM-dd");
            address += sdf.format(new Date());
        }
        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        //we access Main activity variables here
        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);

        try {

            ArrayList<String> latitudes = new ArrayList<>();
            ArrayList<String> longitudes = new ArrayList<>();
            for(LatLng coordinates : MainActivity.locations){
                latitudes.add(Double.toString(coordinates.latitude));
                longitudes.add(Double.toString(coordinates.longitude));
            }

            MainActivity.sharedPreferences.edit().putString("places",ObjectSerializer.serialize(MainActivity.places)).apply();
            MainActivity.sharedPreferences.edit().putString("latitudes",ObjectSerializer.serialize(latitudes)).apply();
            MainActivity.sharedPreferences.edit().putString("longitudes",ObjectSerializer.serialize(longitudes)).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
        MainActivity.arrayAdoptor.notifyDataSetChanged();

        Toast.makeText(this,"Location Saved",Toast.LENGTH_SHORT).show();

    }

}