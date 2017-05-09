package com.sarath.easyandroid.permission;

/**
 * Created by sarath with 23/2/17.
 *
 */

public interface EAPermissionCallback {
    /**
     * Callback for {@link EAPermissionManager#requestAll(EAPermissionCallback, int)}} and
     * {@link EAPermissionManager#request(EAPermissionManager.PermissionType, EAPermissionCallback, int)}
     *
     * @param requestCode the requestAll code of the permission requestAll call
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
