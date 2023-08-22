package com.example.notifire.map;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;

import java.util.List;

public class FetchAddressIntentService extends IntentService {
    public static final String TAG = FetchAddressIntentService.class.getSimpleName();
    protected ResultReceiver receiver;


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        receiver = intent.getParcelableExtra(MapInterface.RECEIVER);
        double latitude = intent.getDoubleExtra(MapInterface.LOCATION_LAT_EXTRA, -1);
        double longitude = intent.getDoubleExtra(MapInterface.LOCATION_LNG_EXTRA, -1);
        String language = intent.getStringExtra(MapInterface.LANGUAGE);


        String errorMessage = "";
        List<Address> addresses = null;

        Locale locale = new Locale(language);
        Geocoder geocoder = new Geocoder(this, locale);
        try {
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    1
            );
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "service not available";
            Log.e(TAG, errorMessage, ioException);
        }
        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no address available";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(MapInterface.FAILURE_RESULT, errorMessage);
        } else {
            StringBuilder result = new StringBuilder();
            Address address = addresses.get(0);

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                if (i == address.getMaxAddressLineIndex()) {
                    result.append(address.getAddressLine(i));
                } else {
                    result.append(address.getAddressLine(i) + ",");
                }
            }
            Log.i(TAG, "address found");
            Log.i(TAG, "address : " + result);
            String state = addresses.get(0).getAdminArea();
            String postalCode = addresses.get(0).getPostalCode();
            String country = addresses.get(0).getCountryName();
            String city = addresses.get(0).getLocality();
            String basicAddressLine = address.getAddressLine(0).replace(", "+postalCode+" "+city+", "+state+", "+country, "");
            Log.d("basic address line: ",basicAddressLine);
            deliverResultToReceiver(MapInterface.SUCCESS_RESULT, result.toString(),city,state,postalCode,basicAddressLine);
        }

    }

    private void deliverResultToReceiver(int resultCode, String message, String city, String state,String postcode,String addressline) {
        Bundle bundle = new Bundle();
        bundle.putString(MapInterface.RESULT_DATA_KEY, message);
        bundle.putString(MapInterface.ADDRESS_STATE,state);
        bundle.putString(MapInterface.ADDRESS_POSTCODE,postcode);
        bundle.putString(MapInterface.ADDRESS_CITY,city);
        bundle.putString(MapInterface.ADDRESS_LINE,addressline);
        Log.d("message: ", message);
        receiver.send(resultCode, bundle);
    }
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(MapInterface.RESULT_DATA_KEY, message);
        Log.d("message: ", message);
        receiver.send(resultCode, bundle);
    }
}
