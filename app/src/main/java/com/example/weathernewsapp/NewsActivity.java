package com.example.weathernewsapp;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class NewsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String query;
    String News_Url;
    String[] newsOutput = new String[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        mAuth = FirebaseAuth.getInstance();


        double lat, lon;
        Bundle extras = getIntent().getExtras();    //Get the latitude and longitude that was bundled from the previous activity
        if(extras != null){
            //Get the lat and long of the user searched location that
            lat = extras.getDouble("LATITUDE");
            lon = extras.getDouble("LONGITUDE");
            query = extras.getString("SEARCH");
            //if (query.isEmpty())
                getAddress(lat, lon);

            News_Url = "https://newsapi.org/v2/everything?" +
                    "q="+ query +"&" +
                    "from=2018-12-13&" +
                    "sortBy=popularity&" +
                    "apiKey=50083c37ccad45a8b0e0ffd99474cca3";

            new AsynHttpTask().execute(News_Url);
        }
    }

    public class AsynHttpTask extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... urls) {

            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                String response = streamToString(urlConnection.getInputStream());
                parseResult(response);
                return newsOutput;


            } catch (Exception e) {
                e.printStackTrace();
            }

            return newsOutput;
        }

        @Override
        protected void onPostExecute(String[] temp) {
            ListAdapter listAdapter = new ArrayAdapter<>(NewsActivity.this, android.R.layout.simple_list_item_1, temp);
            ListView listView = findViewById(R.id.newsListView);
            listView.setAdapter(listAdapter);
        }
    }

    String streamToString(InputStream stream) throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream));

        String data;
        String result = "";


        while ((data = bufferedReader.readLine()) != null){
            result  += data;
        }
        if(null != stream){
            stream.close();
        }


        return result;
    }

    private void parseResult(String result){
        JSONObject response = null;
        try {
            response = new JSONObject(result);
            JSONArray articles = response.getJSONArray("articles");

            for (int i = 0; i < articles.length(); i++){
                JSONObject article = articles.getJSONObject(i);
                String title = article.getString("title");
                //Log.i("Titles", title);
                newsOutput[i] = title;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getAddress(double lat, double lng) {
        Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);

            query = obj.getLocality();
            TextView v = findViewById(R.id.textView_city);
            v.setText(query);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void onSearchClick (View view){
        query = ((EditText) findViewById(R.id.editText_search)).getText().toString();
        Intent intent = getIntent().putExtra("SEARCH", query);
        finish();
        startActivity(intent);
    }
}
