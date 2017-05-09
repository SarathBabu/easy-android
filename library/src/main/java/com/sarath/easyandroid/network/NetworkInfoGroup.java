package com.sarath.easyandroid.network;

/**
 * Created by sarath with 23/2/17.
 */
public interface NetworkInfoGroup {
    /**
     * Select the NetworkInfo from the group.
     *
     * @param type
     * @return
     */
    NetworkInfo getNetworkInfo(int type);
}
