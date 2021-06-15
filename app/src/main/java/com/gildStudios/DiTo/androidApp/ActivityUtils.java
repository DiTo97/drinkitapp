package com.gildStudios.DiTo.androidApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import android.os.AsyncTask;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

public class ActivityUtils {
    private static void showActivity(Activity from, Class<?> to, boolean finishCurrent, @Nullable Bundle extraInfo, boolean clearStack) {
        Intent activityIntent = new Intent(from, to);
        if(extraInfo != null) {
            activityIntent.putExtra("extraInfo", extraInfo);
        }
        if(clearStack) {
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        from.startActivity(activityIntent);
        if(finishCurrent) {
            from.finish();
        }
    }

    public static void showActivity(Activity from, Class<?> to) {
        showActivity(from, to, true, null, false);
    }

    public static void showActivity(Activity from, Class<?> to, boolean clearStack) {
        showActivity(from, to, true, null, clearStack);
    }

    public static void printKeyHash(Context context) {
        try {
            final PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),
                            PackageManager.GET_SIGNING_CERTIFICATES);
            final Signature[] apkSignatures = packageInfo.signingInfo.getApkContentsSigners();
            final MessageDigest mD = MessageDigest.getInstance("SHA");

            for(Signature signature : apkSignatures) {
                mD.update(signature.toByteArray());
                Log.d("KeyHash in Base 64", Base64.encodeToString(mD.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static abstract class NonLeakAsyncTask extends AsyncTask <Object, Object, Object> {
        protected WeakReference <Activity> activityReference;

        public NonLeakAsyncTask(Activity currentContext) {
            activityReference = new WeakReference<>(currentContext);
        }
    }

    public static boolean isOffline(Context callingActivity) {
        ConnectivityManager cM = (ConnectivityManager) callingActivity
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cM.getActiveNetworkInfo();

        return netInfo == null || !netInfo.isConnected();
    }

    public static String capitalizeEachWord(String noCapString) {
        String[] strArray = noCapString.split(" ");
        StringBuilder stringBuilder = new StringBuilder();

        for(String s : strArray) {
            String cap = s.substring(0, 1).toUpperCase() + s.substring(1);
            stringBuilder.append(cap).append(" ");
        }
        return stringBuilder.toString();
    }

    public static final Pattern PATTERN_userPwd =
            Pattern.compile("((?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,16})");
    public static final Pattern PATTERN_emailAddress  =
            Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
            );
    public static final Pattern PATTERN_fullName =
            Pattern.compile("^([A-Z]{1}[a-z]{1,30}[- ]{0,1}|[A-Z]{1}[- \']{1}[A-Z]{0,1}" +
                    "[a-z]{1,30}[- ]{0,1}|[a-z]{1,2}[ -\']{1}[A-Z]{1}[a-z]{1,30}){1,5}$"
            );

    public static boolean validateInput(EditText genericInput, Pattern toMatch, int lengthLimit, String validityError){
        String fieldText = genericInput.getText().toString();

        if(fieldText.isEmpty()) {
            genericInput.setError("Field can't be Empty");
            return false;
        }

        if(lengthLimit > 0) {
            if(fieldText.length() > lengthLimit) {
                genericInput.setError("Field shouldn't be more than"
                        + (lengthLimit == 1 ? "1 Character" : lengthLimit + "Characters"));
                return false;
            }
        }

        if(toMatch != null) {
            if(!toMatch.matcher(fieldText).matches()) {
                genericInput.setError(validityError);
                return false;
            }
        }

        genericInput.setError(null);
        return true;
    }

    public static boolean validateInput(EditText genericInput, Pattern toMatch, int lengthLimit){
        return validateInput(genericInput, toMatch, lengthLimit, "Please insert a valid Input");
    }
}
