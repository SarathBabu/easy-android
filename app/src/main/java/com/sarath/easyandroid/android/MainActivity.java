package com.sarath.easyandroid.android;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sarath.easyandroid.EasyAndroid;
import com.sarath.easyandroid.location.EALocationTracker;
import com.sarath.easyandroid.location.EALocationTrackerListener;
import com.sarath.easyandroid.permission.EAPermissionManager;
import com.sarath.easyandroid.permission.EAPermissionCallback;

public class MainActivity extends AppCompatActivity implements EALocationTrackerListener,EAPermissionCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 3144;

    EALocationTracker EALocationTracker;
    EAPermissionManager requestAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestAdapter = new EAPermissionManager.Builder()
                .with(this)
                .addLocationPermission("We want location",true)
                .build();
        EALocationTracker = new EALocationTracker.Builder(this)
                .callback(this)
                .useGps(true)
                .needAddress(true)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EALocationTracker.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EALocationTracker.onStop();
    }

    public void onGetLocationClicked(View view) {
        EALocationTracker.singleUpdateRequest();
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

    @Override
    public void onLocationPermissionsDisabled() {
        requestAdapter.requestAll(this,REQUEST_CODE);
    }

    @Override
    public void onLocationProviderDisabled() {
        EALocationTracker.showNoGPSDialog(this);
    }

    @Override
    public void onNetworkDisabled() {
        EALocationTracker.showNoNetwork(this);
    }

    @Override
    public void permissionGranted(int requestCode, String permission) {
        EALocationTracker.singleUpdateRequest();
    }

    @Override
    public void onReturnFromSettings() {
        EALocationTracker.singleUpdateRequest();
    }

    public void onOpenNextActivityClicked(View view) {
        startActivity(new Intent(this, SecondActivity.class));
    }
}
