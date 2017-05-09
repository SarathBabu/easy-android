package com.sarath.easyandroid.location;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.sarath.easyandroid.EasyAndroid;
import com.sarath.easyandroid.R;
import com.sarath.easyandroid.network.EANetworkStatusChecker;
import com.sarath.easyandroid.network.NetworkInfo;
import com.sarath.easyandroid.network.NetworkInfoGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by sarath with 20/4/17.
 */

public class EALocationAddressService extends IntentService{


    private static final String LOCATION_DATA_EXTRA = "LocationDataExtra";
    private static final int FAILURE_RESULT_CODE = 0;
    public static final int SUCCESS_RESULT_CODE = 1;
    public static final String RESULT_DATA_KEY = "ResultDataKey";
    private static final String RECEIVER = "Receiver";
    private static final String TAG = EALocationAddressService.class.getSimpleName();
    private ResultReceiver mReceiver;


    public interface AddressResultReceiverCallback{
        void onSuccess(String resultMessage);
        void onError(String errorMessage);
        void noNetwork();
    }

    public static class AddressResultReceiver extends ResultReceiver {
        private final AddressResultReceiverCallback callback;

        AddressResultReceiver(Handler handler, AddressResultReceiverCallback callback) {
            super(handler);
            this.callback = callback;
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            String resultOutput = resultData.getString(EALocationAddressService.RESULT_DATA_KEY);
            if (resultCode == EALocationAddressService.SUCCESS_RESULT_CODE) {
                callback.onSuccess(resultOutput);
            }else {
                if("NoNetwork".equals(resultOutput))
                    callback.noNetwork();
                else
                    callback.onError(resultOutput);
            }
        }
    }

    public static void start(Context context,AddressResultReceiver receiver,
                             Location location){
        Intent intent = new Intent(context, EALocationAddressService.class);
        intent.putExtra(RECEIVER, receiver);
        intent.putExtra(LOCATION_DATA_EXTRA, location);
        context.startService(intent);
    }

    public EALocationAddressService() {
        super(EALocationAddressService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        Location location = intent.getParcelableExtra(
                LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(RECEIVER);

        NetworkInfoGroup networkInfoGroup = new EANetworkStatusChecker(this)
                .getStatus();
        if(!networkInfoGroup.getNetworkInfo(NetworkInfo.MOBILE).isAvailable() &&
                !networkInfoGroup.getNetworkInfo(NetworkInfo.WIFI).isAvailable()){
            deliverNoNetworkResultToReceiver(FAILURE_RESULT_CODE);
        }

        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException e){
            deliverResultToReceiver(FAILURE_RESULT_CODE, getString(R.string.e_nw_error_no_address));
            return;
        }catch(IllegalArgumentException ioException) {

        }
        if (addresses == null || addresses.size()  == 0) {
            deliverResultToReceiver(FAILURE_RESULT_CODE, getString(R.string.e_no_address_found));
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            deliverResultToReceiver(SUCCESS_RESULT_CODE,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    private void deliverNoNetworkResultToReceiver(int resultCode) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, "NoNetwork");
        mReceiver.send(resultCode, bundle);
    }


    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
