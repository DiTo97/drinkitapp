package com.gildStudios.DiTo.androidApp.fragments;

import android.Manifest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.gildStudios.DiTo.androidApp.BuildConfig;
import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.gildStudios.DiTo.androidApp.VolleySingleton;
import com.gildStudios.DiTo.androidApp.R;

import com.gildStudios.DiTo.androidApp.activities.HomeActivity;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.uber.sdk.android.core.Deeplink;
import com.uber.sdk.android.core.UberSdk;
import com.uber.sdk.android.rides.RideParameters;
import com.uber.sdk.android.rides.RideRequestButton;
import com.uber.sdk.android.rides.RideRequestDeeplink;
import com.uber.sdk.core.auth.Scope;
import com.uber.sdk.core.client.SessionConfiguration;

import com.google.android.gms.location.places.AutocompleteFilter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class TaxiFragment extends Fragment {

    private FusedLocationProviderClient locationClient;
    private LocationCallback mLocationCallback;
    private AlertDialog dialogSettings;

    private static final String ARG_taxiParam = "callYourUber";

    private final static String TAG = "TaxiFragment";

    private static final String apiKey = BuildConfig.placesApiKey;

    // TODO: Replace with BetterTaxi API
    public static final String UBER_clientID = BuildConfig.clientID;
    public static final String UBER_clientSecret = BuildConfig.clientSecret;
    public static final String UBER_serverToken = BuildConfig.serverToken;

    public static final String UBER_redirectUri = BuildConfig.redirectURI;
    public final int CODE_locationPermission = 123;

    private LocationRequest mLocationRequest;

    private long UPDATE_INTERVAL  = 1000 * 30;  /* 30 secs */
    private long FASTEST_INTERVAL = 1000 * 10;  /* 10 secs */

    private static final int REQUEST_SELECT_PLACE1 = 1000;
    private static final int REQUEST_SELECT_PLACE2 = 1001;

    private Place pickup = null;
    private Place dropoff = null;

    private LatLng customDropoff;
    private String customDropoffString;

    private boolean autoPosition = false;
    String formattedAddress;

    private EditText et1, et2;

    private LatLng coordinates;

    private String mParam1;

    private SessionConfiguration config;

    public TaxiFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(((HomeActivity) getActivity()).getSupportActionBar() != null)
            ((HomeActivity) getActivity()).getSupportActionBar().hide();
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_taxiParam);
        }

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                onLocationChanged(locationResult.getLastLocation());
            }
        };


        config = new SessionConfiguration.Builder()
                .setClientId(UBER_clientID) //This is necessary
                .setClientSecret(UBER_clientSecret)
                .setServerToken(UBER_serverToken)
                .setRedirectUri(UBER_redirectUri) //This is necessary if you'll be using implicit grant
                .setEnvironment(SessionConfiguration.Environment.PRODUCTION)
                .setScopes(Arrays.asList(Scope.PROFILE, Scope.RIDE_WIDGETS)) //Your scopes for authentication here
                .build();

        UberSdk.initialize(config);
    }

    @Override
    public void onResume() {
        super.onResume();

        if(locationClient != null)
            startLocationUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (locationClient != null) {
            locationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        final View uberView = inflater.inflate(R.layout.fragment_taxi, container, false);

        RideParameters rideParams;

        Button fakeButton = uberView.findViewById(R.id.angry_btn_chianti);
        final RideRequestButton requestButton = uberView.findViewById(R.id.uber_button);

        if(!PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isOffline", false)) {
            final String userId = FirebaseAuth.getInstance().getUid();
            FirebaseDatabase.getInstance().getReference(FirebaseDbHelper.FIREBASE_columnUsersInfo)
                    .child(userId).child(FirebaseDbHelper.FIREBASE_fieldHomeAddress).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()) {
                                if(dataSnapshot.hasChildren()) {
                                    if(dataSnapshot.hasChild("fullAddress")) {
                                        customDropoffString = dataSnapshot.child("fullAddress").getValue().toString();
                                        et2.setText(customDropoffString);
                                        customDropoff = new LatLng(Double.valueOf(dataSnapshot.child("lat").getValue().toString()), Double.valueOf(dataSnapshot.child("lon").getValue().toString()));
                                    }
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    }
            );
        }

        fakeButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestButton.performClick();
                    }
                });

                requestButton.setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (pickup != null && dropoff != null) {
                                    RideParameters rideParams = new RideParameters.Builder()
                                            .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                                            .setPickupLocation(pickup.getLatLng().latitude, pickup.getLatLng().longitude, "Posizione corrente", pickup.getAddress().toString())
                                            .setDropoffLocation(dropoff.getLatLng().latitude, dropoff.getLatLng().longitude, "Destinazione", dropoff.getAddress().toString())
                                            .build();

                                    RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(getContext())
                                            .setSessionConfiguration(config)
                                            .setRideParameters(rideParams)
                                            .setFallback(Deeplink.Fallback.MOBILE_WEB)
                                            .build();

                                    deeplink.execute();
                                    Log.d(TAG, deeplink.getUri().toString());
                                } else if (pickup != null && customDropoff != null) {
                                    RideParameters rideParams = new RideParameters.Builder()
                                            .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                                            .setPickupLocation(pickup.getLatLng().latitude, pickup.getLatLng().longitude, "Posizione corrente", pickup.getAddress().toString())
                                            .setDropoffLocation(customDropoff.latitude, customDropoff.longitude, "Destinazione", customDropoffString)
                                            .build();

                                    RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(getContext())
                                            .setSessionConfiguration(config)
                                            .setRideParameters(rideParams)
                                            .setFallback(Deeplink.Fallback.MOBILE_WEB)
                                            .build();

                                    deeplink.execute();
                                    Log.d(TAG, deeplink.getUri().toString());
                                } else if (autoPosition && dropoff != null) {
                                        RideParameters rideParams = new RideParameters.Builder()
                                                .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                                                .setPickupLocation(coordinates.latitude, coordinates.longitude, "Posizione corrente", formattedAddress)
                                                .setDropoffLocation(dropoff.getLatLng().latitude, dropoff.getLatLng().longitude, "Destinazione", dropoff.getAddress().toString())
                                                .build();

                                        RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(getContext())
                                                .setSessionConfiguration(config)
                                                .setRideParameters(rideParams)
                                                .setFallback(Deeplink.Fallback.MOBILE_WEB)
                                                .build();

                                        deeplink.execute();
                                        Log.d(TAG, deeplink.getUri().toString());
                                } else if(autoPosition && customDropoff != null) {
                                    RideParameters rideParams = new RideParameters.Builder()
                                            .setProductId("a1111c8c-c720-46c3-8534-2fcdd730040d")
                                            .setPickupLocation(coordinates.latitude, coordinates.longitude, "Posizione corrente", formattedAddress)
                                            .setDropoffLocation(customDropoff.latitude, customDropoff.longitude, "Destinazione", customDropoffString)
                                            .build();

                                    RideRequestDeeplink deeplink = new RideRequestDeeplink.Builder(getContext())
                                            .setSessionConfiguration(config)
                                            .setRideParameters(rideParams)
                                            .setFallback(Deeplink.Fallback.MOBILE_WEB)
                                            .build();

                                    deeplink.execute();
                                    Log.d(TAG, deeplink.getUri().toString());
                                } else
                                    Toast.makeText(getActivity(), getString(R.string.missing_fields), Toast.LENGTH_SHORT).show();
                            }
                        }
                );

        TextView tv = uberView.findViewById(R.id.current_position_tv);
        tv.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(locationClient == null) {
                            locationClient = getFusedLocationProviderClient(getContext());
                            startLocationUpdates();
                        } else {
                            getLastLocation();
                        }
                    }
                });


        //AutoCompletePlaces

        et1 = uberView.findViewById(R.id.editText_position1);
        et1.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if(hasFocus) {
                            try {
                                AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                                        //   .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                                        .build();
                                Intent intent = new PlaceAutocomplete.IntentBuilder
                                        (PlaceAutocomplete.MODE_OVERLAY)
                                        .setFilter(typeFilter)
                                        .build(getActivity());
                                startActivityForResult(intent, REQUEST_SELECT_PLACE1);
                            } catch (GooglePlayServicesRepairableException |
                                    GooglePlayServicesNotAvailableException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        et1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                                    //   .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                                    .build();
                            Intent intent = new PlaceAutocomplete.IntentBuilder
                                    (PlaceAutocomplete.MODE_OVERLAY)
                                    .setFilter(typeFilter)
                                    .build(getActivity());
                            startActivityForResult(intent, REQUEST_SELECT_PLACE1);
                        } catch (GooglePlayServicesRepairableException |
                                GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        et1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        et2 = uberView.findViewById(R.id.editText_position2);
        et2.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean hasFocus) {
                        if(hasFocus) {
                            try {
                                AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                                    //   .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                                        .build();
                                Intent intent = new PlaceAutocomplete.IntentBuilder
                                        (PlaceAutocomplete.MODE_OVERLAY)
                                        .setFilter(typeFilter)
                                        .build(getActivity());
                                startActivityForResult(intent, REQUEST_SELECT_PLACE2);
                            } catch (GooglePlayServicesRepairableException |
                                    GooglePlayServicesNotAvailableException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        et2.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                                    //   .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                                    .build();
                            Intent intent = new PlaceAutocomplete.IntentBuilder
                                    (PlaceAutocomplete.MODE_OVERLAY)
                                    .setFilter(typeFilter)
                                    .build(getActivity());
                            startActivityForResult(intent, REQUEST_SELECT_PLACE2);
                        } catch (GooglePlayServicesRepairableException |
                                GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        et2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

        return uberView;
    }

    // Trigger new location updates at interval
    protected void startLocationUpdates() {
        if(dialogSettings != null && dialogSettings.isShowing())
            return;

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        boolean isPermGranted = false;

        String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION };

        for(String permission : permissions) {
            if(ActivityCompat.checkSelfPermission(getContext(), permission)
                    == PackageManager.PERMISSION_GRANTED) {
                isPermGranted = true;
                break;
            }
        }

        Log.d(TAG, "Location update > " + isPermGranted);

        if(isPermGranted) {
            locationClient.requestLocationUpdates(mLocationRequest, mLocationCallback,
                    Looper.myLooper());
        } else {
            Log.d(TAG, "Request update > false");
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    CODE_locationPermission);
        }
    }

    public void onLocationChanged(Location currentLocation) {
        if(currentLocation == null) {
            Toast.makeText(getContext(), getString(R.string.enable_gps),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String locationUpdate = "Location updated to: " +
                Double.toString(currentLocation.getLatitude()) + "," +
                Double.toString(currentLocation.getLongitude());
        Log.d("TaxiFragment", "" + locationUpdate);

        double lat = currentLocation.getLatitude();
        double lon = currentLocation.getLongitude();

        coordinates = new LatLng(lat, lon);

        String url = "http://open.mapquestapi.com/nominatim/v1/reverse.php?key=0GRT334sTne2qALif1H8rShGvrZRVHqZ&format=json&lat=" + Double.toString(coordinates.latitude) + "&lon=" + Double.toString(coordinates.longitude);

        Log.d("TaxiFragment", "URL to get address from: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject address = response.getJSONObject("address");
                    formattedAddress = address.getString("road") + " " + address.getString("building") +
                            ", " + address.getString("postcode") + " " + address.get("city");
                    et1.setText(formattedAddress);
                    autoPosition = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if(volleyError != null) {
                    Log.e(TAG, "volleyError > ", volleyError.getCause());
                }
                Toast.makeText(getContext(), "Connection failed", Toast.LENGTH_SHORT).show();
            }
        }

        );
        VolleySingleton.getInstance(getContext()).addToRequestQueue(request);
    }

    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)

        boolean isPermGranted = false;

        String[] permissions = new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION };

        for(String permission : permissions) {
            if(ActivityCompat.checkSelfPermission(getContext(), permission)
                    == PackageManager.PERMISSION_GRANTED) {
                isPermGranted = true;
                break;
            }
        }

        Log.d(TAG, "Location last > " + isPermGranted);

        if(isPermGranted) {
            Log.d(TAG, "Location last requested");
            locationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            Log.d(TAG, "Location found > " + (location == null));
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                onLocationChanged(location);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        } else {
            requestPermissions(new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    CODE_locationPermission);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "Request done");
        if(permissions.length == 0)
            return;

        switch(requestCode) {
            case CODE_locationPermission:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                } else if(!shouldShowRequestPermissionRationale(permissions[0])) {
                    // User selected the Never Ask Again Option
                    if(dialogSettings == null || !dialogSettings.isShowing())
                        displayNeverAskAgainDialog();
                } else {
                    // Permission denied
                    if(getActivity() != null)
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                                CODE_locationPermission);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PLACE1) {
            if (resultCode == RESULT_OK) {
                pickup = PlaceAutocomplete.getPlace(getActivity(), data);
                et1.setText(pickup.getAddress().toString());
                Log.i(TAG, "Place: " + pickup.getName() + " " + pickup.getLatLng().toString() );
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Toast.makeText(getContext(), getString(R.string.positon_error), Toast.LENGTH_SHORT).show();

                Log.i(TAG, "PlaceAutocomplete status: " + status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // User canceled operation.
            }

            assert getActivity() != null;
            View focusedView = getActivity().getCurrentFocus();

            if(focusedView != null) {
                InputMethodManager inputManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if(inputManager != null)
                    inputManager.hideSoftInputFromWindow(
                            focusedView.getWindowToken(), 0);
            }
        }

        else if (requestCode == REQUEST_SELECT_PLACE2) {
            if (resultCode == RESULT_OK) {
                dropoff = PlaceAutocomplete.getPlace(getActivity(), data);
                et2.setText(dropoff.getAddress().toString());
                Log.i(TAG, "Place: " + dropoff.getName() + " " + dropoff.getLatLng().toString() );
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                // TODO: Handle the error.
                Toast.makeText(getContext(), getString(R.string.positon_error), Toast.LENGTH_SHORT).show();

                Log.i(TAG, "PlaceAutocomplete status: " + status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // User canceled operation.
            }

            assert getActivity() != null;
            View focusedView = getActivity().getCurrentFocus();

            if(focusedView != null) {
                InputMethodManager inputManager = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if(inputManager != null)
                    inputManager.hideSoftInputFromWindow(
                            focusedView.getWindowToken(), 0);
            }
        }
    }

    private void displayNeverAskAgainDialog() {
        dialogSettings = new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.perm_warning_coarseLocation))
                .setCancelable(false)
                .setPositiveButton("Set Manually", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialogSettings.setOnShowListener(new DialogInterface.OnShowListener() {
             @Override
             public void onShow(DialogInterface dialog) {
                 Button btnPositive = dialogSettings.getButton(AlertDialog.BUTTON_POSITIVE);
                 Button btnNegative = dialogSettings.getButton(AlertDialog.BUTTON_NEGATIVE);

                 btnPositive.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         Log.d(TAG, "Positive clicked");
                         dialogSettings.dismiss();
                         Intent intent = new Intent();
                         intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                         Uri uriPackage = Uri.fromParts("package",
                                 getContext().getPackageName(),null);
                         intent.setData(uriPackage);
                         startActivity(intent);
                     }
                 });

                 btnNegative.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         dialogSettings.dismiss();
//                         getActivity().onBackPressed();
                     }
                 });
             }
         });
        dialogSettings.show();
    }
}
