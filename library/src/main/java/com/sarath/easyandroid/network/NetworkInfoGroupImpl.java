package com.sarath.easyandroid.network;

import android.net.ConnectivityManager;

/**
 * Created by sarath on 23/2/17.
 */

public class NetworkInfoGroupImpl implements NetworkInfoGroup {

    private final NetworkInfoFactory factory;

    public NetworkInfoGroupImpl(ConnectivityManager manager) {
        factory = new NetworkInfoFactory(manager);
    }

    @Override
    public NetworkInfo getNetworkInfo(int type) {
        return factory.createNetworkInfo(type);
    }
}
