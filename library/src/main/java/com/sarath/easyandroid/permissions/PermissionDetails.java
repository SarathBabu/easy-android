package com.sarath.easyandroid.permissions;

/**
 * Created by sarath on 23/2/17.
 */

public class PermissionDetails {
    final String  manifestId;
    final String message;
    final boolean isMandatory;

    PermissionDetails(String manifestId, String message, boolean isMandatory) {
        this.manifestId = manifestId;
        this.message = message;
        this.isMandatory = isMandatory;
    }
}
