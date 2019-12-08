package com.example.administrator.finalproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class BigMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, PlacesListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoocleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;

    private List<Marker> previous_marker = null;

    private LatLng currentPosition,newLatLng;
    private LocationManager locationManager = null;
    private LocationListener locationListener = null;
    private DatabaseReference mDatabase;

    private Geocoder mCoder;
    private List<Address> addr;

    private String loc;
    private String uid = "sj";


    public static final int REQUEST_CODE_PERMISSION = 1000;

    private Button sharedLoc, franLoc, addLoc;
    private ToggleButton info;

    private LatLng startLocation;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mListener;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_big_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);

        PlaceAutocompleteFragment autocompleteFragment =
                (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        mAuth = FirebaseAuth.getInstance();
        mListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
            }
        };


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                LatLng location = place.getLatLng();

                CameraPosition cp = new CameraPosition.Builder()
                        .target(location)
                        .zoom(17.0f)
                        .build();

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
                mMap.addMarker(new MarkerOptions().position(location));
            }

            @Override
            public void onError(Status status) {
            }
        });


        Bundle bundle = getIntent().getBundleExtra("bundle2");
        startLocation = bundle.getParcelable("curPos");
        Log.d("^CURPOSSS", "========" + startLocation);

        info = findViewById(R.id.tg_info);
        franLoc = findViewById(R.id.franLoc);

        if (mGoocleApiClient == null) {
            mGoocleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(BigMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(BigMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BigMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_PERMISSION);
            return;
        }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(BigMapsActivity.this,
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                            loc = getCurrentAddress(currentPosition);
                            Log.d("su", "=========" + loc);
                        }
                    }
                });
        Log.d("su", "=========111111" + loc);

        /*sharedLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
*/

        previous_marker = new ArrayList<Marker>();

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (info.isChecked())
                    showPlaceInformation(currentPosition);
                else
                    mMap.clear();
            }
        });

        franLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(user!=null) {

                    mDatabase.child("user").setValue(new User(user.getEmail(), loc));


                    mDatabase.child("user2").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User franLocation = dataSnapshot.getValue(User.class);
                            LatLng loc = getLatLng(franLocation.getloc());
                            mMap.addMarker(new MarkerOptions().position(loc).title("현재 상대방의 위치").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                            CameraPosition cp = new CameraPosition.Builder().target(startLocation).zoom(10).build();
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
                else{
                    Toast.makeText(BigMapsActivity.this,"로그인을 해야 이용 가능한 서비스 입니다.",Toast.LENGTH_LONG).show();
                }
            }
        });
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


        CameraPosition cp = new CameraPosition.Builder()
                .target(startLocation)
                .zoom(17.0f)
                .build();
        mMap.addMarker(new MarkerOptions().position(startLocation).title("현재 내 위치"));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));

        UiSettings ui = mMap.getUiSettings();
        ui.setZoomControlsEnabled(true);
        ui.setScrollGesturesEnabled(true);

        locationListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission
                (BigMapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(BigMapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(BigMapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_PERMISSION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 10, locationListener);
        mMap.setMyLocationEnabled(true);

        mFusedLocationClient.getLastLocation().addOnSuccessListener(BigMapsActivity.this,
                new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());


                        }
                    }
                });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public LatLng getLatLng(String address){
        mCoder = new Geocoder(this);
        try {
            //주소값을 통하여 로케일을 받아온다
            addr = mCoder.getFromLocationName(address, 1);
            Double Lat =  addr.get(0).getLatitude();
            Double Lon =  addr.get(0).getLongitude();
            //해당 로케일로 좌표를 구성한다
            newLatLng = new LatLng(Lat, Lon);
        } catch (Exception e) {
        }
        return newLatLng;
    }

    @Override
    protected void onStart() {
        mGoocleApiClient.connect();
        mAuth.addAuthStateListener(mListener);
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoocleApiClient.disconnect();
        mAuth.removeAuthStateListener(mListener);
        super.onStop();
    }

    @Override
    public void onPlacesFailure(PlacesException e) {
    }

    @Override
    public void onPlacesStart() {
    }

    @Override
    public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {
                    LatLng latLng = new LatLng(place.getLatitude(), place.getLongitude());

                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng).title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    markerOptions.icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));

                    Marker item = mMap.addMarker(markerOptions);

                    previous_marker.add(item);
                }

                //중복 마커 제거

                HashSet<Marker> hashSet = new HashSet<Marker>();

                hashSet.addAll(previous_marker);

                previous_marker.clear();

                previous_marker.addAll(hashSet);
            }
        });
    }

    public void showPlaceInformation(LatLng location) {
        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(BigMapsActivity.this)
                .key("AIzaSyDUkf1lXa-BsAAkW7rzp-VNjLHljy9S0hM")
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(300) //500 미터 내에서 검색
                .type(PlaceType.RESTAURANT) //음식점
                //.type(PlaceType.SUBWAY_STATION)
                .build()
                .execute();
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }


    @Override
    public void onPlacesFinished() {
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            LatLng location = new LatLng(loc.getLatitude(), loc.getLongitude());

            CameraPosition cp = new CameraPosition.Builder()
                    .target(location)
                    .zoom(17.0f)
                    .build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));

            String cityName = null;
            Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
            List<Address> addresses;
            try {
                addresses = gcd.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
                if (addresses.size() > 0) {
                    System.out.println(addresses.get(0).getLocality());
                    cityName = addresses.get(0).getLocality();
                    Toast.makeText(BigMapsActivity.this, "현재위치 :" + cityName, Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
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
}

