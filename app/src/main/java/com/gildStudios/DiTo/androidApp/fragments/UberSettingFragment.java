package com.gildStudios.DiTo.androidApp.fragments;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.gildStudios.DiTo.androidApp.R;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class UberSettingFragment extends Fragment {

    private static final String ARG_someInt = "Position";

    private String mParam1;
    private TextView address;
    private TextView signInAsHost;
    private String TAG = "UberSettingLog";
    private Button saveData;
    private String newAddress;
    private String latitude;
    private String longitude;

    private static final int REQUEST_SELECT_PLACE = 1000;



    public static com.gildStudios.DiTo.androidApp.fragments.UberSettingFragment newInstance(int position) {
       UberSettingFragment fragment = new UberSettingFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_someInt, position);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_someInt);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View MyUberSettingView = inflater.inflate(R.layout.fragment_uber_setting, container, false);

        final String userId = FirebaseAuth.getInstance().getUid();
        FirebaseDatabase.getInstance().getReference("UsersInfo").child(userId).child(FirebaseDbHelper.FIREBASE_fieldHomeAddress).child("fullAddress").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            address.setText(dataSnapshot.getValue().toString());
                            address.setTextColor(Color.BLACK);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

        address = MyUberSettingView.findViewById(R.id.newHome);
        saveData = MyUberSettingView.findViewById(R.id.saveAddress);

        address.setOnFocusChangeListener(
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
                                startActivityForResult(intent, REQUEST_SELECT_PLACE);
                            } catch (GooglePlayServicesRepairableException |
                                    GooglePlayServicesNotAvailableException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
        );

        address.setOnClickListener(
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
                            startActivityForResult(intent, REQUEST_SELECT_PLACE);
                        } catch (GooglePlayServicesRepairableException |
                                GooglePlayServicesNotAvailableException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        saveData.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String userId = FirebaseAuth.getInstance().getUid();

                        HashMap<String, String> addressMap = new HashMap<>();

                        addressMap.put("fullAddress", newAddress);
                        addressMap.put("lat", latitude);
                        addressMap.put("lon", longitude);

                        FirebaseDatabase.getInstance().getReference(FirebaseDbHelper.FIREBASE_columnUsersInfo)
                                .child(userId).child(FirebaseDbHelper.FIREBASE_fieldHomeAddress).setValue(addressMap).addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                            Toast.makeText(getContext(), "Address saved", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(getContext(), "Saving Address error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                        );
                    }
                }
        );

        return MyUberSettingView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PLACE) {
            if (resultCode == RESULT_OK) {
                newAddress = PlaceAutocomplete.getPlace(getActivity(), data).getAddress().toString();
                LatLng coordinates = PlaceAutocomplete.getPlace(getActivity(), data).getLatLng();
                latitude = String.valueOf(coordinates.latitude);
                longitude = String.valueOf(coordinates.longitude);

                address.setText(newAddress);
                address.setTextColor(Color.BLACK);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                Toast.makeText(getContext(), "Error!", Toast.LENGTH_SHORT).show();
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }
}


