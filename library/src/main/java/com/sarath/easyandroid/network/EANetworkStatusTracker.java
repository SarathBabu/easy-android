package com.sarath.easyandroid.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

/**
 * Created by sarath with 23/2/17.
 *
 * Use EANetworkStatusTracker to track the changes with the network state.
 */

public class EANetworkStatusTracker extends BroadcastReceiver {
    private static final String TAG = EANetworkStatusTracker.class.getSimpleName();

    private EANetworkStatusTrackerListener listener;
    private Context context;

    /**
     *
     * @param listener Listener for EANetworkStatusTracker
     */
    public EANetworkStatusTracker(EANetworkStatusTrackerListener listener){
        this.listener = listener;
    }

    public EANetworkStatusTracker with(Context context){
        this.context = context;
        return this;
    }

    public void startTracking(){
        try {
        context.registerReceiver(this,new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }catch (IllegalArgumentException e){}
    }

    public void stopTracking(){
        try {
            context.unregisterReceiver(this);
        }catch (IllegalArgumentException e){}
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfoGroup networkInfoGroup=new NetworkInfoGroupImpl(connMgr);
        listener.onNetworkChange(networkInfoGroup);
    }
}
