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
import android.util.SparseArray;

import com.sarath.easyandroid.android.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Created by sarath on 23/2/17.
 *
 * Adapter implementation for PermissionRequester
 */

public class PermissionRequesterAdapter {

    private static final int SETTINGS_ACTIVITY_REQUEST_CODE = 3272;
    private final PermissionRequester requester;
    private List<PermissionDetails> permissionDetailsList;
    private SparseArray<List<String>> deniedPermissions = new SparseArray<>();
    private Vector<AlertDialog> dialogs = new Vector<>();
    private Activity activity;

    private PermissionRequesterAdapter(PermissionRequester requester) {
        permissionDetailsList = new ArrayList<>();
        this.requester = requester;
    }

    public static class Builder{
        private PermissionRequesterAdapter requesterAdapter;

        public Builder(PermissionRequester requester) {
            requesterAdapter = new PermissionRequesterAdapter(requester);
        }

        public Builder addLocationPermission(String explanation,boolean isMandatory){
            requesterAdapter.permissionDetailsList.add(new PermissionDetails(Manifest.permission.ACCESS_FINE_LOCATION,
                    explanation,isMandatory));
            return this;
        }
        public Builder addCameraPermission(String explanation,boolean isMandatory){
            requesterAdapter.permissionDetailsList.add(new PermissionDetails(Manifest.permission.CAMERA,
                    explanation,isMandatory));
            return this;
        }

        public Builder addStoragePermission(String explanation,boolean isMandatory){
            requesterAdapter.permissionDetailsList.add(new PermissionDetails(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    explanation,isMandatory));
            return this;
        }

        public PermissionRequesterAdapter build(){
            return requesterAdapter;
        }
    }

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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case SETTINGS_ACTIVITY_REQUEST_CODE:
                requester.onReturnFromSettings();
                break;
        }
    }


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
