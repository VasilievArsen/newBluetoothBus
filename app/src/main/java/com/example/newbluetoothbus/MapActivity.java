package com.example.newbluetoothbus;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yandex.mapkit.MapKit;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.mapkit.user_location.UserLocationLayer;

public class MapActivity extends AppCompatActivity{

    private final String MAPKIT_API_KEY = ("d7fcc7b5-dee2-4dce-9f6a-cbb42cbb6fc0");
    private MapView mapView;
    private UserLocationLayer userLocationLayer;
    private LocationManager locationManager;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 2;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        MapKitFactory.setApiKey("d7fcc7b5-dee2-4dce-9f6a-cbb42cbb6fc0");
        MapKitFactory.initialize(this);
        setContentView(R.layout.maps_layout);
        super.onCreate(savedInstanceState);
        mapView = findViewById(R.id.mapview);
        mapView.getMap().setRotateGesturesEnabled(false);
        mapView.getMap().move(new CameraPosition(new Point(62.027111377251714, 129.73124534040164), 12, 0, 0));
        MapKit mapKit = MapKitFactory.getInstance();
        userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
        userLocationLayer.setVisible(true);
        userLocationLayer.setHeadingEnabled(true);


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MapActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
        }
    }
    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }
    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }
}