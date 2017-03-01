package com.sarath.easyandroid.permissions;

import android.app.Activity;

/**
 * Created by sarath on 23/2/17.
 *
 */

public interface PermissionRequestCallback {
    /**
     * Callback for {@link PermissionRequestAdapter#request(Activity, int)}
     *
     * @param requestCode the request code of the permission request call
     * @param permission the permission which is granted one of the permission
     *                   in @{@link android.Manifest}
     */
    void permissionGranted(int requestCode, String permission);

    /**
     * Called when the user being return back to the Activity/Fragment from which he/she was
     * redirected to settings to enable the permissions manually.
     */
    void onReturnFromSettings();

}
