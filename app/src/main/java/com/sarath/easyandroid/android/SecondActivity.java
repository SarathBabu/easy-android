package com.sarath.easyandroid.android;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sarath.easyandroid.location.EALocationService;
import com.sarath.easyandroid.location.EALocationServiceConnector;
import com.sarath.easyandroid.location.NoLocationProviderError;

/**
 * Created by sarath on 6/5/17.
 */

public class SecondActivity extends Activity implements EALocationService.EALocationServiceListener {

    private static final String LOG_TAG = SecondActivity.class.getCanonicalName();
    private EALocationServiceConnector serviceConnector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        serviceConnector = EALocationServiceConnector.getInstance(this)
                .setUpdateInterval(2000L)
                .setEALocationServiceListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceConnector.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        serviceConnector.disconnect();
    }

    @Override
    public void onLocationUpdate(Location location) {
        Log.d(LOG_TAG, location.toString());
    }

    @Override
    public void onError(NoLocationProviderError error) {

    }
}
