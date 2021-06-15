package com.gildStudios.DiTo.androidApp.services;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.gildStudios.DiTo.androidApp.VolleySingleton;
import com.gildStudios.DiTo.androidApp.utils.UtilCloudFunctions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CloudUpdateFlow extends JobService {

    public static final int JOB_serviceId = 1002;

    private final String TAG = getClass().getSimpleName();

    @Override
    public boolean onStartJob(JobParameters jobParams) {
        callUpdateFlowURI();
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParams) {
        return false;
    }

    private void callUpdateFlowURI() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null && !currentUser.isAnonymous()) {
            String userId = currentUser.getUid();

            String updateFlowEndpoint = UtilCloudFunctions.URI_firebaseServer
                    + UtilCloudFunctions.URI_endpointUpdateFlow;
            String updateFlowUri      = String.format(updateFlowEndpoint, userId);

            StringRequest stringRequest = new StringRequest(Request.Method.GET, updateFlowUri,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String httpResponse) {
                            Log.d(TAG, "callUpdateURI > Response is: "
                                    + httpResponse.substring(0, 500));
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError httpError) {
                    Log.e(TAG, "callUpdateURI > Error is : "
                            + httpError.getMessage().substring(0, 500));
                }
            });

            VolleySingleton.getInstance(getApplicationContext())
                    .addToRequestQueue(stringRequest);
        }
    }

}
