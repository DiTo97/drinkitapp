package com.gildStudios.DiTo.androidApp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.gildStudios.DiTo.androidApp.DrinkItApplication;
import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private final String TAG = "HomeFragment";
    private static final String ARG_fragContainer = "homeWrapper";

    private String mParam1;

    public HomeFragment() { }

    public static HomeFragment newInstance(String extraParam) {
        HomeFragment fragment = new HomeFragment();

        Bundle args = new Bundle();
        args.putString(ARG_fragContainer, extraParam);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_fragContainer);
        }
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater homeInflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        final View homeView = homeInflater.inflate(R.layout.fragment_home, container, false);

        final HomeActivity homeActivity = ((HomeActivity) getActivity());
        assert homeActivity != null;

        assert homeActivity.getSupportActionBar() != null;
        homeActivity.getSupportActionBar().show();

        ImageView drinkpath = homeView.findViewById(R.id.drinkview);
        ImageView taxipath = homeView.findViewById(R.id.taxiview);
        ImageView testpath = homeView.findViewById(R.id.testview);
        ImageView profilepath = homeView.findViewById(R.id.profileview);

        //TextView cheerUser = homeView.findViewById(R.id.tvCheerUser);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(DrinkItApplication.isFirstHome) {
          /*  if(currentUser != null && !currentUser.isAnonymous()) {
                String cheerString = getString(R.string.tv_cheerUser, currentUser.getDisplayName());

                cheerUser.setText(cheerString);
                cheerUser.setVisibility(View.VISIBLE);

                Animation shakeText = AnimationUtils.loadAnimation(getContext(), R.anim.anim_shake);
                cheerUser.startAnimation(shakeText);
            } */ // DiTo no, fa cagare
            DrinkItApplication.isFirstHome = false;
        }

        drinkpath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isOffline", false)) {
                    if (PreferenceManager.getDefaultSharedPreferences(getContext()).contains("age")) {
                        DrinkFragment fragment = new DrinkFragment();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.transition_from_right,
                                R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                        transaction.replace(R.id.homeWrapper, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        View alertView = homeInflater.inflate(R.layout.alert5, container, false);
                        final AlertDialog ad = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                                .setView(alertView)
                                .setCancelable(false)
                                .create();

                        TextView continueView = alertView.findViewById(R.id.continueButton);
                        continueView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ad.dismiss();
                                UserDataFragment fragment = new UserDataFragment();

                                Bundle redirectBundle = new Bundle();
                                redirectBundle.putBoolean("Redirect", true);
                                fragment.setArguments(redirectBundle);
                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction.setCustomAnimations(R.anim.transition_from_right,
                                        R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                                transaction.replace(R.id.homeWrapper, fragment);
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }
                        });

                        ad.show();
                    }
                } else {
                    final String userId = FirebaseAuth.getInstance().getUid();
                    DatabaseReference userPref = FirebaseDatabase.getInstance().getReference(FirebaseDbHelper.FIREBASE_columnUsersInfo).child(userId);
                    userPref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                DrinkFragment fragment = new DrinkFragment();
                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                transaction.setCustomAnimations(R.anim.transition_from_right,
                                        R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                                transaction.replace(R.id.homeWrapper, fragment);
                                transaction.addToBackStack(null);
                                transaction.commit();

                            } else {
                                View alertView = homeInflater.inflate(R.layout.alert5, container, false);
                                final AlertDialog ad = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                                        .setView(alertView)
                                        .setCancelable(false)
                                        .create();

                                TextView continueView = alertView.findViewById(R.id.continueButton);
                                continueView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        ad.dismiss();
                                        UserDataFragment fragment = new UserDataFragment();

                                        Bundle redirectBundle = new Bundle();
                                        redirectBundle.putBoolean("Redirect", true);
                                        fragment.setArguments(redirectBundle);
                                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                                        transaction.setCustomAnimations(R.anim.transition_from_right,
                                                R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                                        transaction.replace(R.id.homeWrapper, fragment);
                                        transaction.addToBackStack(null);
                                        transaction.commit();
                                    }
                                });

                                ad.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }

                    });
                }
            }
        });

        taxipath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Click on Taxi Fragment");
                if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isOffline", false)) {
                    Toast.makeText(getContext(), getString(R.string.offline_unusable), Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseUser currentUser = FirebaseAuth.getInstance()
                            .getCurrentUser();
                    if(currentUser != null && currentUser.isAnonymous()) {
                        Toast.makeText(getContext(), getString(R.string.host_unusable), Toast.LENGTH_SHORT).show();
                    } else {
                        GetHomeSafeFragment fragment = new GetHomeSafeFragment();
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        transaction.setCustomAnimations(R.anim.transition_from_right,
                                R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                        transaction.replace(R.id.homeWrapper, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    }
                }
            }
        });

        testpath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReflexTestFragment fragment = new ReflexTestFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                transaction.replace(R.id.homeWrapper, fragment, "fragment_reflexGame");
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        profilepath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDataFragment fragment = new UserDataFragment();

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                transaction.replace(R.id.homeWrapper, fragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }
        });

        return homeView;
    }

}
