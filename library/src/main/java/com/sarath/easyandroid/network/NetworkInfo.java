package com.sarath.easyandroid.network;

/**
 * Created by sarath with 23/2/17.
 */

public interface NetworkInfo{
    int WIFI = 0;
    int MOBILE = 1;
    int INTERNET = 2;

    /**
     * Determine whether the network is available or not.
     *
     * @return
     */
    boolean isAvailable();
}
