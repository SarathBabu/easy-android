package com.sarath.easyandroid.location;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by sarath on 1/5/17.
 */

public class FetchLocationService {

    private static final String RECEIVER = "Receiver";

    public static class LocationResultReceiver extends ResultReceiver {
        private final FetchAddressService.AddressResultReceiverCallback callback;

        LocationResultReceiver(Handler handler, FetchAddressService.AddressResultReceiverCallback callback) {
            super(handler);
            this.callback = callback;
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String resultOutput = resultData.getString(FetchAddressService.RESULT_DATA_KEY);
            if (resultCode == FetchAddressService.SUCCESS_RESULT) {
                callback.onSuccess(resultOutput);
            }else {
                callback.onError(resultOutput);
            }
        }
    }

    public static void start(Context context, LocationResultReceiver receiver){
        Intent intent = new Intent(context, FetchAddressService.class);
        intent.putExtra(RECEIVER, receiver);
        context.startService(intent);
    }

}
