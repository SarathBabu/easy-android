package com.sarath.easyandroid.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.SparseArray;


import com.sarath.easyandroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by sarath on 23/2/17.
 *
 * Use @PermissionRequestAdapter{@link PermissionRequestAdapter} to request for permissions on
 * Activity
 */

public class PermissionRequestAdapter {

    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 3272;
    private final PermissionRequestCallback requester;
    private List<PermissionDetails> permissionDetailsList;
    private SparseArray<List<String>> deniedPermissions = new SparseArray<>();
    private Vector<AlertDialog> dialogs = new Vector<>();
    private Activity activity;

    /**
     *
     * @param requester callback for permission request
     */
    private PermissionRequestAdapter(PermissionRequestCallback requester) {
        permissionDetailsList = new ArrayList<>();
        this.requester = requester;
    }

    /**
     * User builder to create  @{@link PermissionRequestAdapter} object.
     */
    public static class Builder{
        private PermissionRequestAdapter requesterAdapter;

        /**
         *
         * @param requester callback for permission request
         */
        public Builder(PermissionRequestCallback requester) {
            requesterAdapter = new PermissionRequestAdapter(requester);
        }

        /**
         *
         * @param explanation tell why you want this permission
         * @param isMandatory is this permission is mandatory. Mandatory permission will be asked
         *                    on every permission request call by @{@link PermissionRequestAdapter} even if the user rejected the
         *                    permission earlier.
         * @return
         */
        public Builder addLocationPermission(String explanation,boolean isMandatory){
            requesterAdapter.permissionDetailsList.add(new PermissionDetails(Manifest.permission.ACCESS_FINE_LOCATION,
                    explanation,isMandatory));
            return this;
        }
        /**
         *
         * @param explanation tell why you want this permission
         * @param isMandatory is this permission is mandatory. Mandatory permission will be asked
         *                    on every permission request call by @{@link PermissionRequestAdapter} even if the user rejected the
         *                    permission earlier.
         * @return
         */
        public Builder addCameraPermission(String explanation,boolean isMandatory){
            requesterAdapter.permissionDetailsList.add(new PermissionDetails(Manifest.permission.CAMERA,
                    explanation,isMandatory));
            return this;
        }
        /**
         *
         * @param explanation tell why you want this permission
         * @param isMandatory is this permission is mandatory. Mandatory permission will be asked
         *                    on every permission request call by @{@link PermissionRequestAdapter} even if the user rejected the
         *                    permission earlier.
         * @return
         */
        public Builder addStoragePermission(String explanation,boolean isMandatory){
            requesterAdapter.permissionDetailsList.add(new PermissionDetails(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    explanation,isMandatory));
            return this;
        }

        public PermissionRequestAdapter build(){
            return requesterAdapter;
        }
    }

    /**
     * Request permissions which are set for the adapter
     * @param activity activity from which the request is invoked
     * @param requestCode request code
     */
    public void request(Activity activity, int requestCode){
        this.activity = activity;
        ActivityCompat.requestPermissions(activity, getPermissionsIds(), requestCode);
    }

    private String[] getPermissionsIds(){
        String[] permissions = new String[permissionDetailsList.size()];
        int i=0;
        for(PermissionDetails details:permissionDetailsList){
            permissions[i++]=details.manifestId;
        }
        return permissions;
    }

    /**
     * Call this method from {@link Activity#onActivityResult(int, int, Intent)} or
     * {@link android.support.v4.app.Fragment#onActivityResult(int, int, Intent)}
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case SETTINGS_ACTIVITY_REQUEST_CODE:
                requester.onReturnFromSettings();
                break;
        }
    }

    /**
     * Call this method from @{@link Activity#onRequestPermissionsResult(int, String[], int[])}
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        deniedPermissions.put(requestCode, new ArrayList<String>());
        for (int i=0; i< grantResults.length; i++){
            if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                deniedPermissions.get(requestCode).add(permissions[i]);
            }else {
                requester.permissionGranted(requestCode, permissions[i]);
            }
        }
        enforcePermissionsIfNeeded(requestCode);
    }

    /**
     * Call this method from  {@link Activity#onStop()} or {@link Fragment#onStop()}
     */
    public void onStop(){
        for(AlertDialog dialog:dialogs)
            if (dialog.isShowing()) dialog.dismiss();
        dialogs.clear();
    }

    private void enforcePermissionsIfNeeded(int requestCode) {
        openSettings(deniedPermissions.get(requestCode),0);
    }

    private void openSettings(final List<String> deniedPermissions, final int index){
        if(deniedPermissions.size()-1 >= index){
            if(isMandatoryPermission(deniedPermissions.get(index))) {
                AlertDialog alertDialog = new AlertDialog.Builder(activity)
                        .setTitle(R.string.permission)
                        .setMessage(getPermissionMessage(deniedPermissions.get(index)))
                        .setPositiveButton(R.string.open_settings, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                try {
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    activity.startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);
                                }catch (Exception e){
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                    activity.startActivityForResult(intent, SETTINGS_ACTIVITY_REQUEST_CODE);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                openSettings(deniedPermissions, index + 1);
                            }
                        }).create();
                dialogs.add(alertDialog);
                alertDialog.show();
            }else
                openSettings(deniedPermissions, index+1);
        }
    }

    private String getPackageName() {
        return activity.getPackageName();
    }

    private String getPermissionMessage(String s) {
        for(PermissionDetails details:permissionDetailsList){
            if(details.manifestId.equals(s))
                return details.message;
        }
        return null;
    }

    private boolean isMandatoryPermission(String s) {
        for(PermissionDetails details:permissionDetailsList){
            if(details.manifestId.equals(s) && details.isMandatory)
                return true;
        }
        return false;
    }

}
