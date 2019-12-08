package com.example.administrator.finalproject;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.StreetViewPanoramaOptions;
import com.google.android.gms.maps.StreetViewPanoramaView;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.StreetViewPanoramaCamera;
import com.google.android.gms.maps.model.StreetViewPanoramaLink;
import com.google.android.gms.maps.model.StreetViewPanoramaLocation;
import com.google.android.gms.maps.model.StreetViewSource;

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

public class MapsSearchActivity extends FragmentActivity implements OnStreetViewPanoramaReadyCallback, OnMapReadyCallback {


    private StreetViewPanorama mStreetViewPanoramaView;
    private LatLng startLocation;
    private LatLng endLocation;
    private GoogleMap mMap2;
    private LatLng curPos;
    private ArrayList markerPoints;

    private TextView tv_duration;
    private BroadcastReceiver receiver = null;
    private IntentFilter intentFilter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_search);

        StreetViewPanoramaFragment streetViewPanoramaFragment =
                (StreetViewPanoramaFragment) getFragmentManager()
                        .findFragmentById(R.id.streetviewpanorama);
        streetViewPanoramaFragment.getStreetViewPanoramaAsync(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);


        markerPoints = new ArrayList();
        tv_duration = findViewById(R.id.tv_duration);

        Bundle bundle = getIntent().getParcelableExtra("bundle");
        startLocation = bundle.getParcelable("start");
        endLocation = bundle.getParcelable("end");
        curPos = startLocation;

        Toast.makeText(this, "======" + startLocation + endLocation, Toast.LENGTH_LONG).show();

        intentFilter = new IntentFilter();
        intentFilter.addAction("curpos");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Double curlat = intent.getDoubleExtra("curlat", 0);
                Double curlon = intent.getDoubleExtra("curlon", 0);


                curPos = new LatLng(curlat, curlon);

                mStreetViewPanoramaView.setPosition(curPos, 20, StreetViewSource.OUTDOOR);

                CameraPosition ps = new CameraPosition.Builder().target(curPos).zoom(17.0f).build();
                mMap2.animateCamera(CameraUpdateFactory.newCameraPosition(ps));
                CameraDirection();

                Log.d("kwon", "BroadCast해서 받은 수정된 위치 값 = " + curPos);
                Toast.makeText(MapsSearchActivity.this, "위치가 달라지고 있어요" + curPos, Toast.LENGTH_LONG).show();
                Log.d("CURPOSSS", "===========" + curPos);

            }
        };
    }


    @Override
    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
        mStreetViewPanoramaView = streetViewPanorama;
        mStreetViewPanoramaView.setPosition(startLocation, 20, StreetViewSource.OUTDOOR);


        mStreetViewPanoramaView.setUserNavigationEnabled(true);
        mStreetViewPanoramaView.setPanningGesturesEnabled(true);
        mStreetViewPanoramaView.setStreetNamesEnabled(true);
        mStreetViewPanoramaView.setZoomGesturesEnabled(true);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    public void CameraDirection() {
        StreetViewPanoramaLocation location = mStreetViewPanoramaView.getLocation();
        StreetViewPanoramaCamera camera = mStreetViewPanoramaView.getPanoramaCamera();
        if (location != null && location.links != null) {
            StreetViewPanoramaLink link = findClosestLinkToBearing(location.links, camera.bearing);
            int PAN_BY = (int) link.bearing;
            if (PAN_BY < 30)
                return;
            camera = new StreetViewPanoramaCamera.Builder()
                    .zoom(mStreetViewPanoramaView.getPanoramaCamera().zoom)
                    .tilt(mStreetViewPanoramaView.getPanoramaCamera().tilt)
                    .bearing(mStreetViewPanoramaView.getPanoramaCamera().bearing - PAN_BY)
                    .build();
            mStreetViewPanoramaView.animateTo(camera, 500);
            Toast.makeText(this, "화면 돌아가요 = " + PAN_BY + "각도로", Toast.LENGTH_LONG).show();
        }
    }

    public static StreetViewPanoramaLink findClosestLinkToBearing(StreetViewPanoramaLink[] links,
                                                                  float bearing) {
        float minBearingDiff = 360;
        StreetViewPanoramaLink closestLink = links[0];
        for (StreetViewPanoramaLink link : links) {
            if (minBearingDiff > findNormalizedDifference(bearing, link.bearing)) {
                minBearingDiff = findNormalizedDifference(bearing, link.bearing);
                closestLink = link;
            }
        }
        return closestLink;
    }

    // Find the difference between angle a and b as a value between 0 and 180
    public static float findNormalizedDifference(float a, float b) {
        float diff = a - b;
        float normalizedDiff = diff - (float) (360 * Math.floor(diff / 360.0f));
        return (normalizedDiff < 180.0f) ? normalizedDiff : 360.0f - normalizedDiff;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap2 = googleMap;

        final LatLng start = startLocation;
        mMap2.addMarker(new MarkerOptions().position(start).title("현재 내위치"));
        CameraPosition cp = new CameraPosition.Builder().target(start).zoom(17.0f).build();
        mMap2.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
        configMap();
        addPosition();

        mMap2.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                Intent intent = new Intent(MapsSearchActivity.this, BigMapsActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelable("curPos", curPos);
                intent.putExtra("bundle2", bundle);
                Log.d("CURPOSSS", "===========" + curPos);
                startActivity(intent);
            }
        });
    }

    public void configMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap2.setMyLocationEnabled(true);
        UiSettings ui = mMap2.getUiSettings();
        ui.setCompassEnabled(true);
        ui.setZoomControlsEnabled(true);
        ui.setRotateGesturesEnabled(true);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MyService.class);
        startService(intent);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Intent intent = new Intent(this, MyService.class);

        unregisterReceiver(receiver);
        stopService(intent);


    }

    public void bt_refresh(View view) {

        if(curPos != null && endLocation!=null) {

            String url = getUrl(curPos, endLocation);
            FetchUrl FetchUrl = new FetchUrl();

            FetchUrl.execute(url);

            Toast.makeText(this, "거리 계산 갱신~", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(this, "도착할 곳이 없네~", Toast.LENGTH_LONG).show();
    }

    private void addPosition() {

        // Adding new item to the ArrayList
        if (startLocation != null && endLocation != null) {
            markerPoints.add(startLocation);
            markerPoints.add(endLocation);
            // Creating MarkerOptions
            MarkerOptions options = new MarkerOptions();

            // Setting the position of the marker
            options.position(startLocation);

            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

            // Add new marker to the Google Map Android API V2
            mMap2.addMarker(options);

            options.position(endLocation);

            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            mMap2.addMarker(options);

            // Checks, whether start and end locations are captured
            if (markerPoints.size() >= 2) {
                LatLng origin = (LatLng) markerPoints.get(0);
                LatLng dest = (LatLng) markerPoints.get(1);

                // Getting URL to the Google Directions API
                String url = getUrl(origin, dest);
                Log.d("onMapClick", url.toString());
                FetchUrl FetchUrl = new FetchUrl();

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

            ParserTask parserTask = new ParserTask();

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


                tv_duration =  findViewById(R.id.tv_duration);
                tv_duration.setText("거리 : "+distance);


            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap2.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

}
