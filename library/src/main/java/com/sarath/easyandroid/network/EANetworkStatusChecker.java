package com.sarath.easyandroid.network;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by sarath with 9/5/17.
 */

public class EANetworkStatusChecker {

    private Context mContext;

    public EANetworkStatusChecker(Context context) {
        this.mContext = context;
    }

    public NetworkInfoGroup getStatus(){
        final ConnectivityManager connMgr = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return new NetworkInfoGroupImpl(connMgr);
    }
}
