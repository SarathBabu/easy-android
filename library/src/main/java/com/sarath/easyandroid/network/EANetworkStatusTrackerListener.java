package com.sarath.easyandroid.network;

/**
 * Created by sarath with 23/2/17.
 */

public interface EANetworkStatusTrackerListener {
    /**
     * The network state has been changed and the changed state is passed as the parameter.
     * Select the NetworkInfo from the group to know its present state.
     *
     * @param networkInfoGroup
     */
    void onNetworkChange(NetworkInfoGroup networkInfoGroup);
}
