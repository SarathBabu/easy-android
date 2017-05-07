package com.sarath.easyandroid.location;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sarath.easyandroid.R;

/**
 * Created by sarath on 20/4/17.
 */

public class LocationTracker implements
        FetchAddressService.AddressResultReceiverCallback,
        GoogleApiClient.ConnectionCallbacks {

    private final GoogleApiClient mGoogleApiClient;
    private LocationTrackerListener listener;
    private boolean useGPS;
    private static final String TAG = LocationTracker.class.getSimpleName();
    private final Context mContext;
    private boolean needAddress = false;
    private boolean requestPending = false;

    private LocationListener singleUpdateListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            notifyLocationUpdate(location);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
    };

    private LocationListener continuousUpdateListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            notifyLocationUpdate(location);
        }
    };

    private void notifyLocationUpdate(Location location) {
        listener.onLocationFound(location);
        if(needAddress)
        FetchAddressService.start(getContext(),
                new FetchAddressService.AddressResultReceiver(new Handler(),this),
                location);
    }


    @Override
    public void onSuccess(String resultMessage) {
        if(listener!=null)
            listener.onAddressFound(resultMessage);
    }


    @Override
    public void onError(String errorMessage) {
        if(listener!=null)
            listener.onAddressFound(errorMessage);
    }

    @Override
    public void noNetwork() {
        showNoNetwork(mContext);
    }

    private void setUseGPS(boolean useGPS) {
        this.useGPS = useGPS;
    }

    private void setNeedAddress(boolean needAddress) {
        this.needAddress = needAddress;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(requestPending) makeSingleRequest();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private LocationTracker(Context context) {
        this.mContext = context;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    public static class Builder{
        LocationTracker locationTracker;

        public Builder(Context context) {
            locationTracker = new LocationTracker(context);
        }

        public Builder needAddress(boolean needAddress) {
            locationTracker.setNeedAddress(needAddress);
            return this;
        }

        public Builder useGPS() {
            locationTracker.setUseGPS(true);
            return this;
        }

        public Builder callback(LocationTrackerListener listener){
            locationTracker.listener = listener;
            return this;
        }

        public LocationTracker build(){
            return locationTracker;
        }
    }

    private Context getContext(){
        return mContext;
    }

    public void onStart() {
        mGoogleApiClient.connect();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
    }


    public void makeSingleRequest() {
        if(!mGoogleApiClient.isConnected()) {
            requestPending = true;
            return;
        }
        requestPending = false;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            listener.onLocationPermissionsDisabled();
        }else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(location ==null){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        LocationRequest.create().setFastestInterval(0),singleUpdateListener);
            }
        }
    }

    public void makeUpdateRequest() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            listener.onLocationPermissionsDisabled();
        }else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(location ==null){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                        LocationRequest.create().setFastestInterval(0),continuousUpdateListener);
            }
        }
    }


    public static void showNoGPSDialog(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Location Services Disabled")
                .setMessage("Please turn your GPS on. Your location is required to determine the prayer timings. " +
                        "It is strongly recommended that you continue as the app may not function properly without GPS services")
                .setPositiveButton(R.string.ea_continue_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })
                .show();
    }

    public static  void showNoNetwork(final Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Internet connection")
                .setMessage("Make sure Wi-Fi or mobile data is turned on. Internet services are required to fetch the address of location." +
                        " It is recommended that you press continue as the app may not function properly without the internet services")
                .setPositiveButton(R.string.ea_continue_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                })
                .show();
    }
}
