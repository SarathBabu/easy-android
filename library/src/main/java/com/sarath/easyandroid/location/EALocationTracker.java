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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sarath.easyandroid.R;

/**
 * Created by sarath with 20/4/17.
 */

public class EALocationTracker {

    private final GoogleApiClient mGoogleApiClient;
    private EALocationTrackerListener listener;
    private boolean useGPS;
    private static final String TAG = EALocationTracker.class.getSimpleName();
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

    private EALocationAddressService.AddressResultReceiverCallback  callback = new EALocationAddressService.AddressResultReceiverCallback() {
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
            listener.onNetworkDisabled();
        }
    };

    private GoogleApiClient.ConnectionCallbacks callbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            if(requestPending) singleUpdateRequest();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private void notifyLocationUpdate(Location location) {
        if(listener!=null)
            listener.onLocationFound(location);
        if(needAddress)
            EALocationAddressService.start(getContext(),
                    new EALocationAddressService.AddressResultReceiver(new Handler(),callback),
                    location);
    }



    private void setUseGPS(boolean useGPS) {
        this.useGPS = useGPS;
    }

    private void setNeedAddress(boolean needAddress) {
        this.needAddress = needAddress;
    }

    private void setLocationTrackerListener(EALocationTrackerListener listener) {
        this.listener = listener;
    }

    public EALocationTracker(Context context) {
        this.mContext = context;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(callbacks)
                .addApi(LocationServices.API)
                .build();
    }


    public static class Builder{
        EALocationTracker EALocationTracker;

        public Builder(Context context) {
            EALocationTracker = new EALocationTracker(context);
        }

        public Builder needAddress(boolean needAddress) {
            EALocationTracker.setNeedAddress(needAddress);
            return this;
        }

        public Builder useGps(boolean useGps) {
            EALocationTracker.setUseGPS(useGps);
            return this;
        }

        public Builder callback(EALocationTrackerListener listener){
            EALocationTracker.setLocationTrackerListener(listener);
            return this;
        }

        public EALocationTracker build(){
            return EALocationTracker;
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


    public void singleUpdateRequest() {
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
                        LocationRequest.create().setInterval(0),singleUpdateListener);
            }else {
               notifyLocationUpdate(location);
            }
        }
    }

    public static void showNoGPSDialog(final Context context, String rationale) {
        new AlertDialog.Builder(context)
                .setTitle("Location Services is Disabled")
                .setMessage(rationale)
                .setPositiveButton(R.string.ea_continue_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                    }
                })
                .show();
    }

    public static  void showNoNetwork(final Context context,String rationale) {
        new AlertDialog.Builder(context)
                .setTitle("No Network")
                .setMessage(rationale)
                .setPositiveButton(R.string.ea_continue_label, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        context.startActivity(new Intent(Settings.ACTION_SETTINGS));
                    }
                })
                .show();
    }
}
