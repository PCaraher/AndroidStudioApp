package com.example.weathernewsapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextView txtView;
    private static final String OPEN_WEATHER_MAP_API_KEY = "26822f6df8a615f3682ff379e41f2e17";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //Weather code
        double lat, lon;
        Bundle extras = getIntent().getExtras();    //Get the latitude and longitude that was bundled from the previous activity
        if(extras != null){
            //Get the lat and long of the user searched location that
            lat = extras.getDouble("LATITUDE");
            lon = extras.getDouble("LONGITUDE");

            String units = "metric";
            String url = String.format(
                    "http://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&units=%s&appid=%s", // Getting the information from the API
                    lat, lon, units, OPEN_WEATHER_MAP_API_KEY);
            getAddress(lat, lon);
            new GetWeathertask().execute(url);
        }

    }

    private class GetWeathertask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String...strings) {
            String[] weatherStats = new String[5];

            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                InputStream stream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder builder = new StringBuilder();

                String inputString;
                while ((inputString = bufferedReader.readLine()) != null){
                    builder.append(inputString);
                }

                JSONObject topLevel = new JSONObject(builder.toString());
                JSONObject main = topLevel.getJSONObject("main");

                weatherStats[0] = "Temperature: " + String.valueOf(main.getDouble("temp") + "C");
                weatherStats[1] = "Humidity: " + String.valueOf(main.getDouble("humidity") + "%");
                weatherStats[2] = "Pressure: " + String.valueOf(main.getDouble("pressure") + "kPa");
                weatherStats[3] = "Minimum temperature: " + String.valueOf(main.getDouble("temp_min") + "C");
                weatherStats[4] = "Maximum temperature: " + String.valueOf(main.getDouble("temp_max") + "C");

                urlConnection.disconnect();
            } catch (IOException | JSONException e){
                e.printStackTrace();
            }
            return weatherStats;
        }

        @Override
        protected void onPostExecute(String[] temp) {
            ListAdapter listAdapter = new ArrayAdapter<>(ProfileActivity.this, android.R.layout.simple_list_item_1, temp);
            ListView listView = findViewById(R.id.weatherView);
            listView.setAdapter(listAdapter);
        }
    }

    public void logOut(View view){
        mAuth.signOut();
        finish();
        startActivity(new Intent(ProfileActivity.this, SignInActivity.class));
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            String cityName = obj.getLocality();
            TextView v = findViewById(R.id.textView_city);
            v.setText(cityName);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
