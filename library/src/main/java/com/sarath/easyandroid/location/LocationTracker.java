package com.sarath.easyandroid.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.sarath.easyandroid.permission.PermissionRequestAdapter;
import com.sarath.easyandroid.permission.PermissionRequestCallback;

/**
 * Created by sarath on 20/4/17.
 */

public class LocationTracker implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        PermissionRequestCallback, FetchAddressService.AddressResultReceiverCallback {

    private LocationTrackerListener listener;

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

    public interface LocationTrackerListener{
        void onLocationFound(Location location);
        void onAddressFound(String address);
        void onAddressError(String message);
    }

    private static final String TAG = LocationTracker.class.getSimpleName();
    private final GoogleApiClient mGoogleApiClient;
    private final Context mContext;
    private final PermissionRequestAdapter mRequestAdapter;
    private static final int REQUEST_CODE = 3144;
    private boolean needAddress = false;

    @Override
    public void permissionGranted(int requestCode, String permission) {
        getLocationInfo();
    }

    @Override
    public void onReturnFromSettings() {
        getLocationInfo();
    }

    public void setNeedAddress(boolean needAddress) {
        this.needAddress = needAddress;
    }

    private enum ApiConnectionStatus {
        NOT_CONNECTED,
        CONNECTED,
        CONNECTION_FAILED;
    }
    private ApiConnectionStatus apiConnectionStatus = ApiConnectionStatus.NOT_CONNECTED;
    private boolean requestPending = false;

    private LocationTracker(Context context, PermissionRequestAdapter requestAdapter) {
        this.mContext = context;
        this.mRequestAdapter = requestAdapter;
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public static class Builder{
        LocationTracker locationTracker;

        public Builder(Context context, PermissionRequestAdapter requestAdapter) {
            locationTracker = new LocationTracker(context, requestAdapter);
        }

        public Builder needAddress(boolean needAddress) {
            locationTracker.setNeedAddress(needAddress);
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

    public void start() {
        mGoogleApiClient.connect();
    }

    public void stop() {
        mGoogleApiClient.disconnect();
        apiConnectionStatus=ApiConnectionStatus.NOT_CONNECTED;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        apiConnectionStatus = ApiConnectionStatus.CONNECTED;
        if(requestPending) {
            getLocationInfo();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        apiConnectionStatus = ApiConnectionStatus.NOT_CONNECTED;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        apiConnectionStatus = ApiConnectionStatus.CONNECTION_FAILED;
    }

    public void getLocationInfo() {
        if(apiConnectionStatus!=ApiConnectionStatus.CONNECTED) {
            requestPending = true;
            return;
        }
        requestPending = false;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)!=
                PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            if(this.mRequestAdapter.hasPermission(PermissionRequestAdapter.PermissionType.LOCATION))
                this.mRequestAdapter.requestAll(this,REQUEST_CODE);
            return;
        }
        Location mLastLocation  = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            if(listener!=null) {
                listener.onLocationFound(mLastLocation);
                if (needAddress)
                    getLocationInfo(mLastLocation);
            }
        }
    }

    public void getLocationInfo(@NonNull Location location) {
        FetchAddressService.start(getContext(),
                new FetchAddressService.AddressResultReceiver(new Handler(),this),
                location);
    }
}
