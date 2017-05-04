package com.sarath.easyandroid.location;

import android.Manifest;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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

/**
 * Created by sarath on 1/5/17.
 */

public class FetchLocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String RECEIVER = "Receiver";
    private static final String LOG = FetchLocationService.class.getSimpleName();
    private static final int RESULT_OK = 0;
    private static final int RESULT_ERROR = 1;
    private static final String RESULT_GPS_STATUS= "ResultGPSStatus";
    private static final String RESULT_LOCATION= "ResultLocation";
    private static final String NO_GPS = "NoGPS";
    private static final String LOG_TAG = FetchLocationService.class.getCanonicalName();
    private ResultReceiver receiver;
    private LocationManager mLocationManager;
    private GoogleApiClient mGoogleApiClient;
    private boolean requestPending = false;

    LocationListener singleUpdateListener =  new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }
    };


    LocationListener continuousUpdateListener =new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        receiver = intent.getParcelableExtra(RECEIVER);
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        if(!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Bundle bundle = new Bundle();
            bundle.putBoolean(RESULT_GPS_STATUS,false);
            receiver.send(RESULT_ERROR,bundle);
        }else {
            getLocationInfo();
        }
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
        mGoogleApiClient.disconnect();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getLocationInfo() {
        Log.d(LOG_TAG, "makeSingleRequest");
        if(!mGoogleApiClient.isConnected()) {
            requestPending = true;
            return;
        }
        requestPending = false;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
      //  LocationServices.FusedLocationApi.requestLocationUpdates(
      //          mGoogleApiClient,new LocationRequest().setInterval(0), this);

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                10, new android.location.LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {

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
                });

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(LOG_TAG, "onConnected");
        if(requestPending)
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
        Bundle bundle = new Bundle();
        bundle.putParcelable(RESULT_LOCATION,location);
        receiver.send(RESULT_OK,bundle);
       // LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
    }

    public static class LocationResultReceiver extends ResultReceiver {
        private final LocationResultReceiverCallback callback;
        LocationResultReceiver(Handler handler, LocationResultReceiverCallback callback) {
            super(handler);
            this.callback = callback;
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == RESULT_OK) {
                callback.onSuccess((Location) resultData.getParcelable(RESULT_LOCATION));
            }else {
                callback.notGPS();
            }
        }
    }

    public static void start(Context context, LocationResultReceiver receiver){
        Intent intent = new Intent(context, FetchLocationService.class);
        intent.putExtra(RECEIVER, receiver);
        context.startService(intent);
    }

    public interface LocationResultReceiverCallback {
        void onSuccess(Location location);
        void onError(String errorMessage);
        void notGPS();
    }
}
