package com.gildStudios.DiTo.androidApp.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.ActivityUtils;
import com.gildStudios.DiTo.androidApp.customs.CustomEditText;
import com.gildStudios.DiTo.androidApp.customs.CustomNotification;
import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistrationFragment extends Fragment {
    private static final String ARG_accountType = "Account Type";
    private static final String ARG_tabPosition = "Position";

    private String TAG = getClass().getSimpleName();

    private CustomEditText registrationEmail;
    private CustomEditText registrationFullName;
    private CustomEditText registrationPwd;

    private ProgressDialog pDialog;

    private Button registrationBtn;

    private int mTabPosition;

    private String mAccountType;

    public RegistrationFragment() { }

    public static RegistrationFragment newInstance(int position, String accountType) {
        RegistrationFragment mFragment = new RegistrationFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_tabPosition, position);
        args.putString(ARG_accountType, accountType);

        mFragment.setArguments(args);
        return mFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mTabPosition = getArguments().getInt(ARG_tabPosition);
            mAccountType = getArguments().getString(ARG_accountType);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View regView = layoutInflater.inflate(R.layout.fragment_registration, container, false);

        registrationFullName = regView.findViewById(R.id.registrationName);
        registrationEmail = regView.findViewById(R.id.registrationEmail);
        registrationPwd = regView.findViewById(R.id.registrationPassword);

        registrationBtn = regView.findViewById(R.id.signUpBtn);

        registrationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateFields()){
                    createAccount();
                }
            }
        });

        return regView;
    }

    private boolean validateFields() {
        return ActivityUtils.validateInput(registrationFullName, ActivityUtils.PATTERN_fullName, 30, getString(R.string.error_inputName))
                && ActivityUtils.validateInput(registrationEmail, ActivityUtils.PATTERN_emailAddress,0, getString(R.string.error_inputEmail))
                && ActivityUtils.validateInput(registrationPwd, ActivityUtils.PATTERN_userPwd, 16, getString(R.string.error_inputPwd));
    }

    private void hideProgressDialog() {
        pDialog.dismiss();
    }

    private void showProgressDialog(String displayMsg) {
        pDialog = new ProgressDialog(getActivity());

        pDialog.setMessage(displayMsg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    @SuppressLint({"WrongConstant", "ShowToast"})
    private void createAccount() {
        if(registrationFullName.getText() == null
                || registrationEmail.getText() == null
                || registrationPwd.getText() == null
                || getActivity() == null) {
            Toast.makeText(getActivity(), "Fields are incorrect", 0).show();
            return;
        }

        final String accountEmail    = registrationEmail.getText().toString().trim();
        final String accountFullName = registrationFullName.getText().toString().trim();
        final String accountPassword = registrationPwd.getText().toString();

        showProgressDialog("Signing-in... Please, wait");

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(accountEmail, accountPassword)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            Log.d(TAG, "createUserWithEmail: Success");
                            DatabaseReference refUsers = FirebaseDatabase.getInstance()
                                    .getReference(FirebaseDbHelper.FIREBASE_columnUsers);
                            String userId = FirebaseAuth.getInstance().getUid();

                            if(userId != null) {
                                FirebaseUser registeredUser = FirebaseAuth.getInstance()
                                        .getCurrentUser();

                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(accountFullName)
                                        .build();

                                registeredUser.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    Log.d(TAG, "User updated with display-Name.");
                                                } else {
                                                    Log.d(TAG, "User failed display-Name.");
                                                }
                                            }
                                        });

                                HashMap<String, String> userMap = new HashMap<>();
                                userMap.put("fullName", accountFullName);

                                refUsers.child(userId).setValue(userMap);

                                PreferenceManager.getDefaultSharedPreferences(getContext())
                                        .edit()
                                        .putBoolean(CustomNotification.NOTIFY_prefName, true)
                                        .apply();

                                ActivityUtils.showActivity(getActivity(), HomeActivity.class);
                            }
                        } else {
                            Log.d(TAG, "createUserWithEmail: Failure", task.getException());
                            if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(getActivity(), "Email already used.",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                        hideProgressDialog();
                    }
                });
    }
}

