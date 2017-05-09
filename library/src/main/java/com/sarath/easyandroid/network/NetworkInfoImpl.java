package com.sarath.easyandroid.network;

/**
 * Created by sarath with 23/2/17.
 */

public class NetworkInfoImpl implements NetworkInfo{
    private final boolean available;

    public NetworkInfoImpl(boolean available) {
        this.available = available;
    }

    public boolean isAvailable(){
        return available;
    }
}