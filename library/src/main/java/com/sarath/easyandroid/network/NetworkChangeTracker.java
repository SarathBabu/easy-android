package com.sarath.easyandroid.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * Created by sarath on 23/2/17.
 *
 * Use NetworkChangeTracker to track the changes on the network state.
 */

public class NetworkChangeTracker extends BroadcastReceiver {
    private static final String TAG = NetworkChangeTracker.class.getSimpleName();

    private NetworkChangeTrackerListener observer;

    /**
     *
     * @param listener Listener for NetworkChangeTracker
     */
    public NetworkChangeTracker(NetworkChangeTrackerListener listener){
        this.observer = listener;
    }

    public void startTracking(Context context){
        try {
        context.registerReceiver(this,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }catch (IllegalArgumentException e){}
    }

    public void stopTracking(Context context){
        try {
            context.unregisterReceiver(this);
        }catch (IllegalArgumentException e){}
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfoGroup group=new NetworkInfoGroupImpl(connMgr);
        observer.onNetworkChange(group);

    }
}
