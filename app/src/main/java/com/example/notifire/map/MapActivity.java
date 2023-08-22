package com.example.notifire.map;
        import android.animation.Animator;
        import android.annotation.SuppressLint;
        import android.content.Intent;
        import android.content.IntentSender;
        import android.location.Location;
        import android.os.Handler;
        import android.os.ResultReceiver;
        import androidx.annotation.NonNull;
        import androidx.appcompat.app.AppCompatActivity;
        import android.os.Bundle;
        import android.text.Editable;
        import android.text.TextWatcher;
        import android.util.Log;
        import android.view.View;
        import android.view.ViewAnimationUtils;
        import android.view.inputmethod.InputMethodManager;
        import android.widget.Button;
        import android.widget.ImageView;
        import android.widget.ProgressBar;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import com.example.notifire.R;
        import com.google.android.gms.common.api.ApiException;
        import com.google.android.gms.common.api.ResolvableApiException;
        import com.google.android.gms.location.FusedLocationProviderClient;
        import com.google.android.gms.location.LocationCallback;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationResult;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.location.LocationSettingsRequest;
        import com.google.android.gms.location.LocationSettingsResponse;
        import com.google.android.gms.location.SettingsClient;
        import com.google.android.gms.maps.CameraUpdateFactory;
        import com.google.android.gms.maps.GoogleMap;
        import com.google.android.gms.maps.MapFragment;
        import com.google.android.gms.maps.OnMapReadyCallback;
        import com.google.android.gms.maps.model.LatLng;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.OnFailureListener;
        import com.google.android.gms.tasks.OnSuccessListener;
        import com.google.android.gms.tasks.Task;
        import com.google.android.libraries.places.api.Places;
        import com.google.android.libraries.places.api.model.AutocompletePrediction;
        import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
        import com.google.android.libraries.places.api.model.Place;
        import com.google.android.libraries.places.api.net.FetchPlaceRequest;
        import com.google.android.libraries.places.api.net.FetchPlaceResponse;
        import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
        import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
        import com.google.android.libraries.places.api.net.PlacesClient;
        import java.util.ArrayList;
        import java.util.Arrays;
        import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {
    //location
    //location
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Location mLAstKnownLocation;
    private LocationCallback locationCallback;
    private final float DEFAULT_ZOOM = 17;

    //places
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;

    //views
    private View mapView;
    private TextView mDisplayAddressTextView;
    private ProgressBar mProgressBar;
    private ImageView mSmallPinIv;

    //variables
    private String addressOutput;
    private String state;
    private String postalCode;
    private String city;
    private String addressLine;
    private int addressResultCode;
    private boolean isSupportedArea;
    private LatLng currentMarkerPosition;

    //receiving
    private String mApiKey = "" ;
    private String[] mSupportedArea = new String[]{};
    private String mCountry = "";
    private String mLanguage = "en";

    private static final String TAG = MapActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initViews();
        receiveIntent();
        initMapsAndPlaces();
    }

    private void initViews(){
        Button submitLocationButton = findViewById(R.id.submit_location_button);
        mDisplayAddressTextView = findViewById(R.id.tv_display_marker_location);
        mProgressBar = findViewById(R.id.progress_bar);
        mSmallPinIv = findViewById(R.id.small_pin);

        final View icPin = findViewById(R.id.ic_pin);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                revealView(icPin);
            }
        }, 1000);

        submitLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitResultLocation();
            }
        });

    }

    private void receiveIntent(){
        Intent intent = getIntent();

        if (intent.hasExtra(MapInterface.API_KEY)){
            mApiKey = intent.getStringExtra(MapInterface.API_KEY);
        }

        if (intent.hasExtra(MapInterface.COUNTRY)){
            mCountry = intent.getStringExtra(MapInterface.COUNTRY);
        }

        if (intent.hasExtra(MapInterface.LANGUAGE)){
            mLanguage = intent.getStringExtra(MapInterface.LANGUAGE);
        }

        if (intent.hasExtra(MapInterface.SUPPORTED_AREAS)){
            mSupportedArea = intent.getStringArrayExtra(MapInterface.SUPPORTED_AREAS);
        }
    }

    private void initMapsAndPlaces() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Places.initialize(this, mApiKey);
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

    }

    private void submitResultLocation(){
        // if the process of getting address failed or this is not supported area , don't submit
        if (addressResultCode == MapInterface.FAILURE_RESULT) {
            Toast.makeText(MapActivity.this, "failed to select location", Toast.LENGTH_SHORT).show();
        } else {

            Intent data = new Intent();
            data.putExtra(MapInterface.SELECTED_ADDRESS, addressOutput);
            data.putExtra(MapInterface.ADDRESS_POSTCODE,postalCode);
            data.putExtra(MapInterface.ADDRESS_STATE,state);
            data.putExtra(MapInterface.LOCATION_LAT_EXTRA, currentMarkerPosition.latitude);
            data.putExtra(MapInterface.LOCATION_LNG_EXTRA, currentMarkerPosition.longitude);
            data.putExtra(MapInterface.ADDRESS_LINE,addressLine);
            data.putExtra(MapInterface.ADDRESS_CITY,city);
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @SuppressLint("MissingPermission")

    /*
      is triggered when the map is loaded and ready to display
      @param GoogleMap
     *
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        //enable location button
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        //move location button to the required position and adjust params such margin
        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layoutParams.setMargins(0, 0, 60, 500);
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        //if task is successful means the gps is enabled so go and get device location amd move the camera to that location
        task.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        //if task failed means gps is disabled so ask user to enable gps
        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(MapActivity.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                mSmallPinIv.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                Log.i(TAG, "changing address");
                startIntentService();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    /**
     * is triggered whenever we want to fetch device location
     * in order to get device's location we use FusedLocationProviderClient object that gives us the last location
     * if the task of getting last location is successful and not equal to null ,
     * apply this location to mLastLocation instance and move the camera to this location
     * if the task is not successful create new LocationRequest and LocationCallback instances and update lastKnownLocation with location result
     */
    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            mLAstKnownLocation = task.getResult();
                            if (mLAstKnownLocation != null) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLAstKnownLocation.getLatitude(), mLAstKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                            else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(1000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        mLAstKnownLocation = locationResult.getLastLocation();
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLAstKnownLocation.getLatitude(), mLAstKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        //remove location updates in order not to continues check location unnecessarily
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, null);
                            }
                        } else {
                            Toast.makeText(MapActivity.this, "Unable to get last location ", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    protected void startIntentService() {
        currentMarkerPosition = mMap.getCameraPosition().target;
        AddressResultReceiver resultReceiver = new AddressResultReceiver(new Handler());

        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(MapInterface.RECEIVER, resultReceiver);
        intent.putExtra(MapInterface.LOCATION_LAT_EXTRA, currentMarkerPosition.latitude);
        intent.putExtra(MapInterface.LOCATION_LNG_EXTRA, currentMarkerPosition.longitude);
        intent.putExtra(MapInterface.LANGUAGE, mLanguage);

        startService(intent);
    }

    private void updateUi() {
        mDisplayAddressTextView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mMap.clear();
        if (addressResultCode == MapInterface.SUCCESS_RESULT) {
            //check for supported area
            if (isSupportedArea(mSupportedArea)) {
                //supported
                addressOutput = addressOutput.replace("Unnamed Road,", "");
                addressOutput = addressOutput.replace("Unnamed Road،", "");
                addressOutput = addressOutput.replace("Unnamed Road New,", "");
                mSmallPinIv.setVisibility(View.VISIBLE);
                isSupportedArea = true;
                mDisplayAddressTextView.setText(addressOutput);
            } else {
                //not supported
                mSmallPinIv.setVisibility(View.GONE);
                isSupportedArea = false;
                mDisplayAddressTextView.setText("Area not supported");
            }
        } else if (addressResultCode == MapInterface.FAILURE_RESULT) {
            mSmallPinIv.setVisibility(View.GONE);
            mDisplayAddressTextView.setText(addressOutput);
        }
        Log.d("address got: ", addressOutput);
    }

    private boolean isSupportedArea(String[] supportedAreas) {
        if (supportedAreas.length==0)
            return true;

        boolean isSupported = false;
        for (String area : supportedAreas) {
            if (addressOutput.contains(area)) {
                isSupported = true;
                break;
            }
        }
        return isSupported;
    }

    private void revealView(View view) {
        int cx = view.getWidth() / 2;
        int cy = view.getHeight() / 2;
        float finalRadius = (float) Math.hypot(cx, cy);
        Animator anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);
        view.setVisibility(View.VISIBLE);
        anim.start();
    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            addressResultCode = resultCode;
            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            addressOutput = resultData.getString(MapInterface.RESULT_DATA_KEY);
            state = resultData.getString(MapInterface.ADDRESS_STATE);
            postalCode = resultData.getString(MapInterface.ADDRESS_POSTCODE);
            addressLine = resultData.getString(MapInterface.ADDRESS_LINE);
            city = resultData.getString(MapInterface.ADDRESS_CITY);
            if (addressOutput == null) {
                addressOutput = "";
            }
            updateUi();
        }
    }
}

