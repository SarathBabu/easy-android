package com.sarath.easyandroid.permissions;

/**
 * Created by sarath on 23/2/17.
 */

public interface PermissionRequester {
    void permissionGranted(int requestCode, String permission);
    void onReturnFromSettings();

}
