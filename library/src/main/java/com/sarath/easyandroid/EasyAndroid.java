package com.sarath.easyandroid;

import android.content.Context;

import com.sarath.easyandroid.location.EALocationTracker;
import com.sarath.easyandroid.location.EALocationTrackerListener;
import com.sarath.easyandroid.network.EANetworkStatusChecker;
import com.sarath.easyandroid.network.EANetworkStatusTracker;
import com.sarath.easyandroid.network.EANetworkStatusTrackerListener;

/**
 * Created by sarath with 9/5/17.
 */

public class EasyAndroid {
    private static EasyAndroid mEasyAndroid = new EasyAndroid();
    private ContextHolder contextHolder;


    public static void init(Context context){
        if(mEasyAndroid.contextHolder ==null)
            mEasyAndroid.contextHolder = new ContextHolder(context);
    }

    public static EALocationTracker getLocationTracker(EALocationTrackerListener listener,
                                                       boolean needAddress, boolean useGps){
        return new EALocationTracker.Builder(mEasyAndroid.contextHolder.context)
                .needAddress(needAddress)
                .useGps(useGps)
                .callback(listener)
                .build();
    }

    public static EANetworkStatusChecker getNetworkStatusChecker(){
        return new EANetworkStatusChecker(mEasyAndroid.contextHolder.context);
    }

    public EANetworkStatusTracker getEANetworkStatusTracker(Context context,
                                                            EANetworkStatusTrackerListener trackerListener){
        return new EANetworkStatusTracker(trackerListener).with(context);
    }



    private static class ContextHolder {
        Context context;
        ContextHolder(Context context) {
            this.context = context;
        }
    }
}
