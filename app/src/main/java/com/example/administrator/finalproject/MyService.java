package com.example.administrator.finalproject;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class MyService extends Service {
    private LocationManager mLocationManager;
    private Location curLocation = null;
    private LocationListener locationListener;

    public MyService() { }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("kwon", "일단 서비스 작동 중");
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                curLocation = location;
                //mLocationManager.removeUpdates(this);
                Log.d("kwon", "바뀐 현재 위치 입니다아 = " + curLocation);
                Intent intent = new Intent("curpos");
                intent.putExtra("curlat", curLocation.getLatitude());
                intent.putExtra("curlon", curLocation.getLongitude());
                sendBroadcast(intent);
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(String provider) { }

            @Override
            public void onProviderDisabled(String provider) { }
        };

        locationpermission();
    }

    public void locationpermission(){
        if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        if (mLocationManager.getAllProviders().contains(LocationManager.GPS_PROVIDER))
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 4000, 5, locationListener);
            Log.e("kwon","GPS GPS GPS");
        if (mLocationManager.getAllProviders().contains(LocationManager.NETWORK_PROVIDER))
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 4000, 5, locationListener);
    }
}
