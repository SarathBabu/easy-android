package com.sarath.easyandroid.location;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import static com.sarath.easyandroid.location.EALocationService.UPDATE_ITERVAL;

/**
 * Created by sarath with 6/5/17.
 */

public class EALocationServiceConnector{

    private final Context mContext;
    private EALocationService mService;
    private boolean mBound = false;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            EALocationService.EALocationServiceBinder binder = (EALocationService.EALocationServiceBinder) service;
            mService = binder.getService();
            mService.addEALocationServiceListener(serviceListener);
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService.removeListener(serviceListener);
            mBound = false;
        }
    };
    private EALocationService.EALocationServiceListener serviceListener;
    private long updateInterval = 5000L;

    public static EALocationServiceConnector getInstance(Context context){
        return  new EALocationServiceConnector(context);
    }


    private EALocationServiceConnector(Context context) {
        mContext = context;
    }

    public EALocationServiceConnector setEALocationServiceListener(EALocationService.EALocationServiceListener listener){
        this.serviceListener = listener;
        return this;
    }

    public EALocationServiceConnector setUpdateInterval(long interval){
        this.updateInterval = interval;
        return this;
    }

    public void disconnect(){
        mContext.unbindService(mConnection);
    }

    public void connect(){
        Intent intent = new Intent(mContext,EALocationService.class);
        intent.putExtra(UPDATE_ITERVAL, updateInterval);
        mContext.bindService(intent,mConnection, Context.BIND_AUTO_CREATE);
    }
}