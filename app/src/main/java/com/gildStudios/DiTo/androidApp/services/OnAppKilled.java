package com.gildStudios.DiTo.androidApp.services;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class OnAppKilled extends JobIntentService {

    public static final int JOB_serviceId = 1001;

    private final String TAG = getClass().getSimpleName();

    @Override
    public IBinder onBind(@NonNull Intent bindIntent) {
        return null;
    }

    @Override
    protected void onHandleWork(@NonNull Intent bindIntent) {

    }

    @Override
    public void onTaskRemoved(Intent workIntent) {
        super.onTaskRemoved(workIntent);
        Log.d(TAG, "Service is stopped");
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null && currentUser.isAnonymous()) {
            String userId = currentUser.getUid();

            FirebaseAuth.getInstance().signOut();

            FirebaseDatabase.getInstance()
                    .getReference(FirebaseDbHelper.FIREBASE_columnUsers)
                    .child(userId).setValue(null);

            FirebaseDatabase.getInstance()
                    .getReference(FirebaseDbHelper.FIREBASE_columnUsersInfo)
                    .child(userId).setValue(null);

            currentUser.delete();

            Log.d(TAG, "Anonymous " + userId + " deleted");
        }

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        if(sharedPreferences.getBoolean("isOffline", false)) {
            sharedPreferences.edit().remove("isOffline").apply();
            if(sharedPreferences.getInt("age", 0) != 0) {
                sharedPreferences.edit()
                        .remove("age")
                        .remove("height")
                        .remove("gender")
                        .remove("bonesType")
                            .apply();
            }
        }

        JobScheduler jobScheduler = (JobScheduler) getApplicationContext()
                .getSystemService(JOB_SCHEDULER_SERVICE);

        if(jobScheduler != null) {
            jobScheduler.cancel(CloudUpdateFlow.JOB_serviceId);
        }

        sharedPreferences.edit()
                .putInt("normalShutdown", 1).apply();
    }

    public static void enqueueWork(Context context, Intent workIntent) {
        enqueueWork(context, OnAppKilled.class, JOB_serviceId, workIntent);
    }

}
