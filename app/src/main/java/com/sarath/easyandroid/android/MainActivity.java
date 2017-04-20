package com.sarath.easyandroid.android;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sarath.easyandroid.location.LocationTracker;
import com.sarath.easyandroid.permission.PermissionRequestAdapter;

public class MainActivity extends AppCompatActivity implements LocationTracker.LocationTrackerListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    LocationTracker locationTracker;
    PermissionRequestAdapter requestAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAdapter = new PermissionRequestAdapter.Builder()
                .on(this)
                .addLocationPermission("We want location",true)
                .build();
        locationTracker =new LocationTracker.Builder(this,requestAdapter)
                .needAddress(true)
                .callback(this)
                .build();
    }



    @Override
    protected void onStart() {
        super.onStart();
        locationTracker.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationTracker.stop();
    }

    public void onGetLocationClicked(View view) {
        locationTracker.getLocationInfo();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestAdapter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        requestAdapter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLocationFound(Location location) {
        Log.d(TAG, location.toString());
    }

    @Override
    public void onAddressFound(String address) {
        Log.d(TAG, address);
    }

    @Override
    public void onAddressError(String message) {
        Log.d(TAG, message);
    }
}
