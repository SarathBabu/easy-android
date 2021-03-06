package com.sarath.easyandroid.location;

import android.location.Location;

/**
 * Created by sarath with 4/5/17.
 */

public interface EALocationTrackerListener {
    void onLocationFound(Location location);
    void onAddressFound(String address);
    void onAddressError(String message);
    void onLocationPermissionsDisabled();
    void onLocationProviderDisabled();
    void onNetworkDisabled();
}