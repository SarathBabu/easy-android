package com.sarath.easyandroid.network;

import android.net.ConnectivityManager;
import android.net.Network;

/**
 * Created by sarath on 23/2/17.
 */

public class NetworkInfoFactory {

    private final ConnectivityManager manager;

    public NetworkInfoFactory(ConnectivityManager manager){
        this.manager = manager;
    }

    public NetworkInfo createNetworkInfo(int type){
        switch (type){
            case NetworkInfo.MOBILE:
                return getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            case NetworkInfo.WIFI:
                return getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            case NetworkInfo.INTERNET:
                return getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            default:
                throw new RuntimeException("Unsupported network type");
        }
    }

    private NetworkInfo getNetworkInfo(int type){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Network[] allNetworks = manager.getAllNetworks();
            for(Network network:allNetworks){
                final android.net.NetworkInfo networkInfo = manager.getNetworkInfo(network);
                if(networkInfo.getType() == type){
                    if(networkInfo.isConnectedOrConnecting()){
                        return new NetworkInfoImpl(true);
                    }
                }
            }
        }else {
            final android.net.NetworkInfo wifi = manager.getNetworkInfo(type);
            if (wifi.isAvailable()) {
                return new NetworkInfoImpl(true);
            }
        }
        return new NetworkInfoImpl(false);
    }
}
