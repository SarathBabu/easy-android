package com.sarath.easyandroid.permissions;

/**
 * Created by sarath on 23/2/17.
 *
 * Details about the permission that you want to enable in the application.
 */

public class PermissionDetails {
    final String  manifestId;
    final String message;
    final boolean isMandatory;

    /**
     * Create a permission's details
     *
     * @param manifestId select one permission from @{@link android.Manifest}
     * @param message the message which explains why you want this permission
     * @param isMandatory is this permission is mandatory, if so this permission will be asked
     *                    every time when the @{@link PermissionRequestCallback} requests.
     */
    PermissionDetails(String manifestId, String message, boolean isMandatory) {
        this.manifestId = manifestId;
        this.message = message;
        this.isMandatory = isMandatory;
    }
}
