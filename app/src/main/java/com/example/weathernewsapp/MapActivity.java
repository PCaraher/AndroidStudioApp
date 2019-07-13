package com.example.weathernewsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private LatLng currentLocation;

    final private int REQUEST_COARSE_ACCESS = 123;
    boolean permissionGranted = false;
    LocationManager lm;
    LocationListener locationListener;

    private class MyLocationListener implements LocationListener{

        @Override
        public void onLocationChanged(Location location) {
            if (location != null){
                LatLng p = new LatLng(location.getLatitude(), location.getLongitude());
                currentLocation = p;
                mMap.addMarker(new MarkerOptions().position(p));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p,12.0f));
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mAuth = FirebaseAuth.getInstance();
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Get the devices current location
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_COARSE_ACCESS);
            return;
        } else {
            permissionGranted = true;
        }
        if (permissionGranted){
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }

    public void onSearchLocation(View view){

        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            String placeName = ((EditText) findViewById(R.id.editTxt_location_search)).getText().toString();
            List<Address> addresses = geocoder.getFromLocationName(placeName, 1);
            Address address = addresses.get(0);

            if (addresses.size() > 0){
                //lm.removeUpdates(locationListener);
                LatLng p = new LatLng(address.getLatitude(), address.getLongitude());
                currentLocation = p;
                mMap.addMarker(new MarkerOptions().position(p));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p,12.0f));
            }
        } catch (IOException  e){
            e.printStackTrace();
        }
    }

    public void onGetWeatherInfo(View view){
        double lat = currentLocation.latitude;
        double lng = currentLocation.longitude;

        Bundle extras = new Bundle();
        extras.putDouble("LATITUDE", lat);
        extras.putDouble("LONGITUDE",lng);
        Intent intent = new Intent(MapActivity.this, ProfileActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void onGetNewsInfo(View view){
        double lat = currentLocation.latitude;
        double lng = currentLocation.longitude;

        Bundle extras = new Bundle();
        extras.putDouble("LATITUDE", lat);
        extras.putDouble("LONGITUDE",lng);
        Intent intent = new Intent(MapActivity.this, NewsActivity.class);
        intent.putExtras(extras);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        switch (requestCode) {
            case REQUEST_COARSE_ACCESS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    permissionGranted = true;
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                } else {
                    permissionGranted = false;
                }
                break;
                default:
                    super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        //Removes the location listener
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_COARSE_ACCESS);
            return;
        } else {
            permissionGranted = true;
        }
        if (permissionGranted) {
            lm.removeUpdates(locationListener);
        }
    }

    public void logOut(View view){

        mAuth.signOut();
        finish();
        startActivity(new Intent(MapActivity.this, SignInActivity.class));
    }
}
