package com.gildStudios.DiTo.androidApp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gildStudios.DiTo.androidApp.customs.CustomSQLiteDatabase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class DrinkItApplication extends Application {

    private final String TAG = "DrinkItApplication";

    private CustomSQLiteDatabase drinkItAppDb;

    public static ArrayList<Glass> glassesList;
    private static WeakReference<Context> mContext;

    public static boolean isFirstHome;
    public static boolean isFirstReflex;

    @Override
    public void onCreate() {
        super.onCreate();

        // Enable offline Storage
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        mContext = new WeakReference<Context>(this);

        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);

        drinkItAppDb = new CustomSQLiteDatabase(this);
        glassesList  = new ArrayList<>();

        isFirstHome   = true;
        isFirstReflex = true;

        Runnable createSQLiteDb = new Runnable() {
            @Override
            public void run() {
                try {
                    drinkItAppDb.createDatabase();
                    glassesList = drinkItAppDb.getAllGlasses();
                } catch (IOException ioException) {
                    Log.e(TAG, "getAllGlasses >", ioException);
                }
            }
        };

        Runnable updateSQLiteDb = new Runnable() {
            @Override
            public void run() {
                try {
                    drinkItAppDb.updateDatabase();
                    glassesList = drinkItAppDb.getAllGlasses();
                } catch (IOException ioException) {
                    Log.e(TAG, "getAllGlasses >", ioException);
                }
            }
        };

        Runnable fillGlassesList = new Runnable() {
            @Override
            public void run() {
                glassesList = drinkItAppDb.getAllGlasses();
            }
        };

        switch(AppStartHandler.checkAppStart(this, sharedPreferences,false)) {
            case 2:
                // Normal App start
                Log.d(TAG, "Normal App start");
                new Thread(fillGlassesList).start();
                break;
            case 1:
                // First App start of this Version
                Log.d(TAG, "First App start of Version");
                if(drinkItAppDb.checkDatabase()) {
                    new Thread(updateSQLiteDb).start();
                } else {
                    new Thread(createSQLiteDb).start();
                }
                break;
            case 0:
                // First App start ever
                Log.d(TAG, "First App start ever");
                if(!drinkItAppDb.checkDatabase()) {
                    new Thread(createSQLiteDb).start();
                }
                break;
            default:
                break;
        }

        int normalShutdown = sharedPreferences.getInt(
                "normalShutdown", -1);

        if(normalShutdown == 0) {
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

                Log.d("DrinkItApplication", "Anonymous " + userId + " deleted");
            }
        } else {
            sharedPreferences.edit()
                    .putInt("normalShutdown", 0).apply();
        }
    }

    public static Context getContext() {
        return mContext.get();
    }

}