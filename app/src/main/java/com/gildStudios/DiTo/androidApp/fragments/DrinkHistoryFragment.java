package com.gildStudios.DiTo.androidApp.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.ActivityUtils;
import com.gildStudios.DiTo.androidApp.Drink;
import com.gildStudios.DiTo.androidApp.DrinkItApplication;
import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.gildStudios.DiTo.androidApp.History;
import com.gildStudios.DiTo.androidApp.R;

import com.gildStudios.DiTo.androidApp.adapters.HistoryAdapter;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class DrinkHistoryFragment extends Fragment {
    private final String TAG = getClass().getSimpleName();
    private ProgressDialog pDialog;

    private Activity mActivity;

    private ArrayList<History> historyList;
    private HistoryAdapter historyAdapter;
    private TextView noHistory;
    private TextView BAC;
    private TextView STOMACH;
    private TextView TIME;
    private ListView historyListView;

    public DrinkHistoryFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();

        mActivity = getActivity();
        historyList    = new ArrayList<>();
        historyAdapter = new HistoryAdapter(getActivity(), R.layout.list_view_history, historyList);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View historyView = layoutInflater.inflate(R.layout.fragment_drink_history, container,
                false);
        historyListView = historyView.findViewById(R.id.personalListView);
        BAC = historyView.findViewById(R.id.listBestScore);
        STOMACH = historyView.findViewById(R.id.listLastScore);
        TIME = historyView.findViewById(R.id.listPlayerName);

        noHistory = historyView.findViewById(R.id.noHistoryList);
        Log.d("DrinkNoHistory", "noHistory is: " + (noHistory == null ? "Null" : "Not null"));

        historyListView.setAdapter(historyAdapter);
        historyListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentView, View currentView, int itemPosition,
                                    long itemId) {
                Log.d(TAG, "Clicked row-Number: " + itemPosition);

                Bundle historyBundle = new Bundle();
                ArrayList<Drink> td = historyList.get(itemPosition).getHistoryDrinks();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                Fragment fm = new ListDrinksHistoryFragment();
                if(td != null && !td.isEmpty()) {
                    historyBundle.putParcelableArrayList("historyList", td);
                    historyBundle.putString("date",historyList.get(itemPosition).getTimestampDate());
                    fm.setArguments(historyBundle);
                }
                    transaction.setCustomAnimations(R.anim.transition_from_right,
                            R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                    transaction.replace(R.id.homeWrapper, fm);
                    transaction.addToBackStack(null);
                    transaction.commit();

            }
        });
        return historyView;
    }

    @Override
    public void onResume() {
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
        super.onResume();
        Log.d(TAG, "onResume");
        new LoadHistoryAsyncTask(mActivity).execute();
    }

    private class LoadHistoryAsyncTask extends ActivityUtils.NonLeakAsyncTask {
        LoadHistoryAsyncTask(Activity currentContext) {
            super(currentContext);
        }

        @Override
        protected void onPreExecute() {
            Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                    .getSupportActionBar()).hide();
            Log.d(TAG, "LoadHistoryAsyncTask > onPreExecute");

            displayProgressDialog("Loading the History...");
        }

        @Override
        protected Object doInBackground(Object... taskParams) {
            Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                    .getSupportActionBar()).hide();
            Log.d(TAG, "LoadHistoryAsyncTask > doInBackground");

            String userId = FirebaseAuth.getInstance().getUid();

            DatabaseReference historyRef = FirebaseDatabase.getInstance()
                    .getReference(FirebaseDbHelper.FIREBASE_columnDrinkHistory)
                    .child(userId);

            // TODO: Adjust params from Firebase
            historyRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                            .getSupportActionBar()).hide();
                    Log.d(TAG, "historyAdapter > clear");
                    historyAdapter.clear();

                    if(dataSnapshot.exists()) {
                        Log.d(TAG, dataSnapshot.getKey());
                        if(dataSnapshot.hasChildren()) {
                            for(DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                                long utcHistoryTime = Long.valueOf(dataSnap.getKey());

                                Log.d(TAG, "utcHistoryTime > " + utcHistoryTime);

                                int stomachStatus = Integer.valueOf(dataSnap.child("stomachStatus")
                                        .getValue().toString());

                                Log.d(TAG, "stomachStatus > " + stomachStatus);

                                double resultTax = Double.valueOf(dataSnap.child("resultTax")
                                        .getValue().toString());

                                Log.d(TAG, "resultTax > " + resultTax);

                                History aHistory = new History(utcHistoryTime, resultTax, stomachStatus, getContext());


                                DataSnapshot drinksListSnap = dataSnap.child("drinksList");

                                ArrayList<Drink> drinksList = new ArrayList<>();

                                for(DataSnapshot utcTimesSnap : drinksListSnap.getChildren()) {
                                    for(DataSnapshot drinksSnap : utcTimesSnap.getChildren()) {
                                        for(DataSnapshot glassesSnap : drinksSnap.getChildren()) {
                                            int glassNum = Integer.valueOf(glassesSnap
                                                    .getValue().toString());

                                            for(int k = 0; k < glassNum; k++) {
                                                Date utcDate = new Date(Long.valueOf(utcTimesSnap.getKey()) * 1000);
                                                DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                                dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                                                String timeSnap = dateFormat.format(utcDate);
                                                String remoteTag = drinksSnap.getKey();
                                                String drinkName = getStringResourceByTag(remoteTag);
                                                Drink historyDrink = new Drink(remoteTag, drinkName, glassesSnap.getKey(),timeSnap);
                                                drinksList.add(historyDrink);
                                            }
                                        }
                                    }
                                }

                                aHistory.setHistoryDrinks(drinksList);
                                historyAdapter.add(aHistory);
                            }
                        }
                    }

                    Activity callingActivity = activityReference.get();

                    if(callingActivity == null || callingActivity.isFinishing()) {
                        return;
                    } else {
                        pDialog.dismiss();
                    }

                    try {
                        callingActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.d(TAG, "historyAdapter > notifyDataSetChanged");
                                historyAdapter.notifyDataSetChanged();


                                if(historyListView != null) {
                                    if(historyAdapter.isEmpty()) {
                                        noHistory.setVisibility(View.VISIBLE);

                                        historyListView.setVisibility(View.INVISIBLE);
                                    } else {
                                        noHistory.setVisibility(View.INVISIBLE);
                                        historyListView.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        });
                    } catch(ClassCastException | NullPointerException e){
                        e.printStackTrace();
                        Toast.makeText(callingActivity, getString(R.string.history_fail), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                            .getSupportActionBar()).hide();
                    historyAdapter.notifyDataSetInvalidated();

                    if(historyListView != null) {
                        if(historyAdapter.isEmpty()) {
                            BAC.setVisibility(View.INVISIBLE);
                            STOMACH.setVisibility(View.INVISIBLE);
                            TIME.setVisibility(View.INVISIBLE);
                            noHistory.setVisibility(View.VISIBLE);
                            historyListView.setVisibility(View.INVISIBLE);
                        } else {
                            noHistory.setVisibility(View.INVISIBLE);
                            historyListView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            return historyList;
        }

        @Override
        protected void onPostExecute(Object taskResult) {
            Log.d(TAG, "LoadHistoryAsyncTask completed");
        }
    }

    private String getStringResourceByTag(String resourceTag) {
        Context appContext = DrinkItApplication.getContext();
        String packageName = appContext.getPackageName();

        try {
            int resourceId = appContext.getResources()
                    .getIdentifier(resourceTag, "string", packageName);

            return appContext.getString(resourceId);
        } catch(Resources.NotFoundException resNotFound) {
            return resourceTag;
        }
    }

    private void displayProgressDialog(String displayMsg) {
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
        pDialog = new ProgressDialog(mActivity);

        pDialog.setMessage(displayMsg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }
}