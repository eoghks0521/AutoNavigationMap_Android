package com.example.administrator.finalproject;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

public class SearchActivity extends FragmentActivity implements OnMapReadyCallback {

    private LatLng myLocation;
    private LatLng staLocation;
    private LatLng desLocation;
    private String myLocationName;
    private PlaceAutocompleteFragment autocompleteFragment;
    private PlaceAutocompleteFragment autocompleteFragment2;
    private GoogleMap mMap3;

    private FusedLocationProviderClient mFusedLocationClient;
    private static final int REQUEST_CODE_PERMISSIONS = 2000;

    private ArrayList<LatLng> markerPoints;

    private TextView tv_distance;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);

        myLocationName = "내위치";
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        markerPoints = new ArrayList<LatLng>();

        Bundle bundle = getIntent().getParcelableExtra("bd");
        myLocation = bundle.getParcelable("myLocation");


        autocompleteFragment =
                (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment1);
        autocompleteFragment.setText("내위치");

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                staLocation = place.getLatLng();
                autocompleteFragment.setText(place.getName().toString());
                myLocationName = place.getName().toString();

                Log.e("kwon","myLocation이름은 :: "+myLocationName);
                Log.e("kwon","myLocation이름은 :: "+staLocation);

                addPosition();
                CameraPosition cp = new CameraPosition.Builder().target(staLocation).build();
                mMap3.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
            }

            @Override
            public void onError(Status status) {
                Log.d("kwon", "자동완성 에러 " + status);

            }
        });
        autocompleteFragment2 =
                (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment2);
        autocompleteFragment2.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                desLocation = place.getLatLng();
                autocompleteFragment2.setText(place.getName().toString());

                addPosition();
                CameraPosition cp = new CameraPosition.Builder().target(desLocation).build();
                mMap3.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
            }

            @Override
            public void onError(Status status) {
                Log.d("kwon", "자동완성 에러 " + status);

            }
        });

    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap3 = googleMap;

        LatLng start = myLocation;

        Log.d("kwon", "start는 ?? " + myLocation);
        //mMap3.addMarker(new MarkerOptions().position(start).title("현재 내위치"));
        CameraPosition cp = new CameraPosition.Builder().target(start).build();
        mMap3.animateCamera(CameraUpdateFactory.newCameraPosition(cp));

        UiSettings ui = mMap3.getUiSettings();
        ui.setCompassEnabled(true);
        ui.setZoomControlsEnabled(true);
        ui.setRotateGesturesEnabled(true);
        ui.setZoomGesturesEnabled(true);
        ui.setAllGesturesEnabled(true);


        mMap3.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                // Already two locations
                if(markerPoints.size()>1){
                    markerPoints.clear();
                    mMap3.clear();
                }

                // Adding new item to the ArrayList
                markerPoints.add(point);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                // Setting the position of the marker
                options.position(point);

                /**
                 * For the start location, the color of marker is GREEN and
                 * for the end location, the color of marker is RED.
                 */
                if(markerPoints.size()==1){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                }else if(markerPoints.size()==2){
                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }

                // Add new marker to the Google Map Android API V2
                mMap3.addMarker(options);

                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 2) {
                    LatLng origin = (LatLng) markerPoints.get(0);
                    LatLng dest = (LatLng) markerPoints.get(1);

                    // Getting URL to the Google Directions API
                    String url = getUrl(origin, dest);
                    Log.d("onMapClick", url.toString());
                    SearchActivity.FetchUrl FetchUrl = new SearchActivity.FetchUrl();

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url);
                }
            }
        });
    }



    private void checkMy(){

                    Intent intent = new Intent(SearchActivity.this, MapsSearchActivity.class);
                    Log.d("kwon", "-----------" + myLocation);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("start", myLocation);
                    bundle.putParcelable("end", desLocation);
                    intent.putExtra("bundle", bundle);
                    startActivity(intent);
                    finish();

    }



    public void bt_searchfind(View view) {

        if (myLocationName.equals("내위치")){
            checkMy();
        } else {
            Intent intent = new Intent(this, MapsSearchActivity.class);
            Log.d("kwon", "-----------" + staLocation);
            Bundle bundle = new Bundle();
            bundle.putParcelable("start", staLocation);
            bundle.putParcelable("end", desLocation);
            intent.putExtra("bundle", bundle);
            startActivity(intent);
            finish();
        }
    }

    private void addPosition() {

        MarkerOptions options = new MarkerOptions();

        if(markerPoints.size()>1){
            markerPoints.clear();
            //mMap3.clear();
        }

        if(staLocation!=null) {
            markerPoints.add(staLocation);
            options.position(staLocation);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mMap3.addMarker(options);

        }
        if(desLocation!=null) {
            markerPoints.add(desLocation);
            options.position(desLocation);
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap3.addMarker(options);
        }

        if (staLocation != null && desLocation != null) {


            // Checks, whether start and end locations are captured
            if (markerPoints.size() >= 2) {
                LatLng origin = (LatLng) markerPoints.get(0);
                LatLng dest = (LatLng) markerPoints.get(1);

                // Getting URL to the Google Directions API
                String url = getUrl(origin, dest);
                Log.d("onMapClick", url.toString());
                SearchActivity.FetchUrl FetchUrl = new SearchActivity.FetchUrl();

                // Start downloading json data from Google Directions API
                FetchUrl.execute(url);
            }

        }
    }

    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + "AIzaSyCteX7o0nO8zTD0ZHwuMvq6y9iwlgmx0hs";


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }


    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            SearchActivity.ParserTask parserTask = new SearchActivity.ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;
            String distance = "";
            String duration = "";

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                Log.d("kwon2", "onPostExecute부분이다");
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    if(j==0){    // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1){ // Get duration from the list
                        duration = (String)point.get("duration");
                        continue;
                    }


                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

                tv_distance =  findViewById(R.id.tv_distance);
                tv_distance.setText("거리 : "+distance);

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap3.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }
}
