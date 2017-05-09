package com.sarath.easyandroid.location;

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sarath with 1/5/17.
 */

public class EALocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String LOG_TAG = EALocationService.class.getCanonicalName();
    private GoogleApiClient mGoogleApiClient;
    private boolean requestPending = false;
    private final IBinder mBinder = new EALocationServiceBinder();
    private final List<EALocationServiceListener> listeners = new ArrayList<>();
    public static final String UPDATE_ITERVAL = "UpdateInterval";
    private long updateInterval = 5000L;


    public class EALocationServiceBinder extends Binder{

        EALocationService getService(){
            return EALocationService.this;
        }
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
        updateInterval = intent.getLongExtra(UPDATE_ITERVAL,5000L);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        mGoogleApiClient.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void addEALocationServiceListener(EALocationServiceListener listener){
        listeners.add(listener);
    }

    public void removeListener(EALocationServiceListener listener){
        listeners.remove(listener);
    }


    public void getLocationInfo() {
        if(!mGoogleApiClient.isConnected()) {
            requestPending = true;
            return;
        }
        requestPending = false;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            notifyListeners(new NoLocationProviderError());
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,new LocationRequest().setInterval(updateInterval), this);

    }

    private void notifyListeners(NoLocationProviderError noLocationProviderError) {
        for(EALocationServiceListener listener:listeners){
            listener.onError(noLocationProviderError);
        }
    }



    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "onConnected");
        getLocationInfo();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(LOG_TAG, "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(LOG_TAG, "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(LOG_TAG, "onLocationChanged");
        notifyListeners(location);
    }

    private void notifyListeners(Location location) {
        for(EALocationServiceListener listener:listeners)
            listener.onLocationUpdate(location);
    }

    public interface EALocationServiceListener {
        void onLocationUpdate(Location location);
        void onError(NoLocationProviderError error);
    }
}
