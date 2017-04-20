package com.sarath.easyandroid.permission;

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
import android.util.Log;
import android.util.SparseArray;


import com.sarath.easyandroid.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by sarath on 23/2/17.
 *
 * Use @PermissionRequestAdapter{@link PermissionRequestAdapter} to requestAll for permissions on
 * Activity
 */

public class PermissionRequestAdapter {

    private static final String TAG = PermissionRequestAdapter.class.getSimpleName();
    private SparseArray<PermissionRequestCallback> requesters;
    private SparseArray<PermissionDetails> permissionDetailsList;
    private SparseArray<List<String>> deniedPermissions = new SparseArray<>();
    private Vector<AlertDialog> dialogs = new Vector<>();
    private Activity activity;

    public enum PermissionType {
        LOCATION,
        CAMERA,
        STORAGE,
    }


    private PermissionRequestAdapter() {
        permissionDetailsList = new SparseArray<>();
        requesters = new SparseArray<>();
    }

    /**
     * User builder to create  @{@link PermissionRequestAdapter} object.
     */
    public static class Builder{
        private PermissionRequestAdapter requesterAdapter;

        public Builder() {
            requesterAdapter = new PermissionRequestAdapter();
        }

        /**
         *
         * @param activity Activity on which the permissions are asked.
         * @return
         */
        public Builder on(Activity activity){
            requesterAdapter.activity = activity;
            return this;
        }

        /**
         *
         * @param rationale tell why you want this permission
         * @param isMandatory is this permission is mandatory. Mandatory permission will be asked
         *                    on every permission requestAll call by @{@link PermissionRequestAdapter} even if the user rejected the
         *                    permission earlier.
         * @return
         */
        public Builder addLocationPermission(String rationale,boolean isMandatory){
            requesterAdapter.permissionDetailsList.put(PermissionType.LOCATION.ordinal(),new PermissionDetails(Manifest.permission.ACCESS_FINE_LOCATION,
                    rationale,isMandatory));
            return this;
        }
        /**
         *
         * @param rationale tell why you want this permission
         * @param isMandatory is this permission is mandatory. Mandatory permission will be asked
         *                    on every permission requestAll call by @{@link PermissionRequestAdapter} even if the user rejected the
         *                    permission earlier.
         * @return
         */
        public Builder addCameraPermission(String rationale,boolean isMandatory){
            requesterAdapter.permissionDetailsList.put(PermissionType.CAMERA.ordinal(),new PermissionDetails(Manifest.permission.CAMERA,
                    rationale,isMandatory));
            return this;
        }
        /**
         *
         * @param rationale tell why you want this permission
         * @param isMandatory is this permission is mandatory. Mandatory permission will be asked
         *                    on every permission requestAll call by @{@link PermissionRequestAdapter} even if the user rejected the
         *                    permission earlier.
         * @return
         */
        public Builder addStoragePermission(String rationale,boolean isMandatory){
            requesterAdapter.permissionDetailsList.put(PermissionType.STORAGE.ordinal(),new PermissionDetails(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    rationale,isMandatory));
            return this;
        }

        public PermissionRequestAdapter build(){
            return requesterAdapter;
        }
    }

    public boolean hasPermission(PermissionType permissionType){
        return permissionDetailsList.indexOfKey(permissionType.ordinal())>=0;
    }

    /**
     * Request for all permissions which are set for the adapter
     * @param requester Callback fro this request
     * @param requestCode requestAll code
     */
    public void requestAll(PermissionRequestCallback requester, int requestCode){
        requesters.put(requestCode,requester);
        ActivityCompat.requestPermissions(activity, getPermissionsIds(), requestCode);
    }


    /**
     * Request specific permission which is set on the adapter
     * @param permissionType specify the permission request for
     * @param requester Callback fro this request
     * @param requestCode requestAll code
     */
    public void request(PermissionType permissionType, PermissionRequestCallback requester,
                        int requestCode){
        if(permissionDetailsList.get(permissionType.ordinal())==null) return;
        requesters.put(requestCode,requester);
        ActivityCompat.requestPermissions(activity,
                new String[]{permissionDetailsList.get(permissionType.ordinal()).manifestId},
                requestCode);
    }



    private String[] getPermissionsIds(){
        String[] permissions = new String[permissionDetailsList.size()];
        for(int i=0;i<permissionDetailsList.size();i++){
            permissions[i]=permissionDetailsList.valueAt(i).manifestId;
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
        PermissionRequestCallback callback = requesters.get(requestCode-1);
        if(callback!=null)
            callback.onReturnFromSettings();
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
                PermissionRequestCallback callback = requesters.get(requestCode);
                if(callback!=null)
                    callback.permissionGranted(requestCode, permissions[i]);
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
        openSettings(deniedPermissions.get(requestCode),0,requestCode);
    }

    private void openSettings(final List<String> deniedPermissions, final int index, final int requestCode){
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
                                    activity.startActivityForResult(intent, requestCode+1);
                                }catch (Exception e){
                                    Intent intent = new Intent();
                                    intent.setAction(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                    activity.startActivityForResult(intent, requestCode+1);
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                openSettings(deniedPermissions, index + 1, requestCode);
                            }
                        }).create();
                dialogs.add(alertDialog);
                alertDialog.show();
            }else
                openSettings(deniedPermissions, index+1, requestCode);
        }
    }

    private String getPackageName() {
        return activity.getPackageName();
    }

    private String getPermissionMessage(String s) {
        for(int i=0; i<permissionDetailsList.size();i++){
            if(permissionDetailsList.valueAt(i).manifestId.equals(s))
                return permissionDetailsList.valueAt(i).message;
        }
        return null;
    }

    private boolean isMandatoryPermission(String s) {
        for(int i=0; i<permissionDetailsList.size();i++){
            if(permissionDetailsList.valueAt(i).manifestId.equals(s) && permissionDetailsList.valueAt(i).isMandatory)
                return true;
        }
        return false;
    }

}
