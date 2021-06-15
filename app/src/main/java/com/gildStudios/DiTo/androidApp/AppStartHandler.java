package com.gildStudios.DiTo.androidApp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.pm.PackageInfoCompat;
import android.util.Log;

public class AppStartHandler {
    private static final int START_firstTime = 0;
    private static final int START_versionFirstTime = 1;
    private static final int START_normalStart = 2;

    private static final String START_lastVersion = "1";

    private static final String TAG = "AppStartHandler";

    private static int appStart = 2;

    private AppStartHandler() { }

    private static int checkAppStart(long currentVersionCode, long lastVersionCode) {
        if(lastVersionCode == -1) {
            return START_firstTime;
        } else if(lastVersionCode < currentVersionCode) {
            return START_versionFirstTime;
        } else if (lastVersionCode > currentVersionCode) {
            Log.w(TAG, "Current Version code (" + currentVersionCode
                    + ") is less then the one recognized on last startup ("
                    + lastVersionCode
                    + "). Defensively assuming normal App start.");
            return START_normalStart;
        } else {
            return START_normalStart;
        }
    }

    public static int checkAppStart(Context context, SharedPreferences sharedPreferences,boolean editSharedPrefs) {
        PackageInfo pInfo;

        try {
            pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            long lastVersionCode = sharedPreferences.getLong(
                    START_lastVersion, -1);

            long currentVersionCode = PackageInfoCompat.getLongVersionCode(pInfo);
            appStart = checkAppStart(currentVersionCode, lastVersionCode);

            // Update in Preferences
            if(editSharedPrefs) {
                sharedPreferences.edit()
                        .putLong(START_lastVersion, currentVersionCode).apply();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG,
                    "Unable to determine current Version from PackageManager. Defensively assuming normal App start.");
        }
        return appStart;
    }
}
