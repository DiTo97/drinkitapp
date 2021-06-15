package com.gildStudios.DiTo.androidApp.fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.ActivityUtils;
import com.gildStudios.DiTo.androidApp.customs.CustomEditText;
import com.gildStudios.DiTo.androidApp.customs.CustomNotification;
import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.shaishavgandhi.loginbuttons.FacebookButton;

import java.util.Objects;

import static android.app.Activity.RESULT_CANCELED;

public class LoginFragment extends Fragment implements
        View.OnClickListener {

    private CustomEditText loginEmail;
    private CustomEditText loginPassword;

    private LoginButton fbLoginBtn;

    private static final String ARG_someInt = "Position";

    private static final int RC_signIn = 9001;
    private static final String TAG = "LoginFragment";

    private static final String FB_authType = "rerequest";
    private static final String FB_requestPermissions = "id,name,email";

    private ProgressDialog pDialog;

    private Button loginBtn;

    private String mParam1;

    private ImageView signInAsHost;

    private CallbackManager mCallbackManager;

    private GoogleSignInClient mGoogleSignInClient;


    public LoginFragment() { }

    public static LoginFragment newInstance(int position) {
        LoginFragment fragment = new LoginFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_someInt, position);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_someInt);
        }

        pDialog = new ProgressDialog(getActivity());

        mCallbackManager = CallbackManager.Factory.create();

        GoogleSignInOptions gSO = new GoogleSignInOptions.Builder()
                .requestProfile()
                .requestId()
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()), gSO);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            if(GoogleSignIn.getLastSignedInAccount(getActivity()) != null) {
                mGoogleSignInClient.signOut();
            }

            if(AccessToken.getCurrentAccessToken() != null) {
                LoginManager.getInstance().logOut();
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View loginView = layoutInflater.inflate(R.layout.fragment_login, container, false);

        loginEmail = loginView.findViewById(R.id.loginEmail);
        loginPassword = loginView.findViewById(R.id.loginPassword);
        loginBtn = loginView.findViewById(R.id.loginBtn);

        loginBtn.setOnClickListener(this);

        signInAsHost = loginView.findViewById(R.id.link_signInAsHost2);

        signInAsHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getActivity() == null) {
                    return;
                }
                showProgressDialog("Loading the Menu...");

                FirebaseAuth.getInstance().signInAnonymously()
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @SuppressLint({"WrongConstant", "ShowToast"})
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> authTask) {
                                if(authTask.isSuccessful()) {
                                    Log.d(TAG, "signInAnonymously: Success");

                                    String userId = FirebaseAuth.getInstance().getUid();

                                    if(userId != null) {
                                        FirebaseDatabase.getInstance()
                                                .getReference(FirebaseDbHelper.FIREBASE_columnUsers)
                                                .child(userId).setValue(true);

                                        ActivityUtils.showActivity(getActivity(), HomeActivity.class);
                                    }
                                } else {
                                    Log.w(TAG, "signInAnonymously: Failure", authTask.getException());
                                    if(ActivityUtils.isOffline(getActivity())) {
                                        Toast.makeText(getActivity(), "DrinkItApp in mode-Offline",
                                                1).show();
                                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean("isOffline", true).apply();
                                        ActivityUtils.showActivity(getActivity(), HomeActivity.class);
                                    } else {
                                        Toast.makeText(getActivity(), "Sign-in as Guest failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                                hideProgressDialog();
                            }
                        });
            }
        });

        loginView.findViewById(R.id.signInGoogle).setOnClickListener(this);

        fbLoginBtn = loginView.findViewById(R.id.fakeSignInFB);
        FacebookButton fbButton = loginView.findViewById(R.id.signInFB);

        fbButton.setOnClickListener(this);

//        fbLoginBtn.setAuthType(FB_authType);
        // If using in a Fragment
        fbLoginBtn.setFragment(this);
        fbLoginBtn.setReadPermissions("email", "public_profile");

        fbLoginBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @SuppressLint({"WrongConstant", "ShowToast"})
            @Override
            public void onSuccess(LoginResult loginResult) {
                hideProgressDialog();

                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                hideProgressDialog();
                Log.d(TAG, "Facebook: onCancel");
            }

            @SuppressLint({"WrongConstant", "ShowToast"})
            @Override
            public void onError(FacebookException fbException) {
                hideProgressDialog();
                Toast.makeText(getActivity(),
                        "Sign-in with Facebook failed: " + fbException.getMessage(), 0)
                        .show();
            }
        });
        loginView.clearFocus();

        return loginView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent extraData) {
        super.onActivityResult(requestCode, resultCode, extraData);

        if(requestCode == RC_signIn) {
            if(resultCode == RESULT_CANCELED)
                return;

            Task<GoogleSignInAccount> googleTask = GoogleSignIn.getSignedInAccountFromIntent(extraData);
            handleSignInResult(googleTask);
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, extraData);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount googleAccount = completedTask.getResult(ApiException.class);

            assert googleAccount != null;
            firebaseAuthWithGoogle(googleAccount);
        } catch (ApiException e) {
            Log.w(TAG, "signInResult:Failed. Code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount googleAccount) {
        showProgressDialog("Signing-in... Please, wait");

        AuthCredential authCredential = GoogleAuthProvider
                .getCredential(googleAccount.getIdToken(), null);
        FirebaseAuth.getInstance().signInWithCredential(authCredential)
                .addOnCompleteListener(Objects.requireNonNull(getActivity()),
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> resultTask) {
                                if(resultTask.isSuccessful()) {
                                    Log.d(TAG, "signInWithCredential: Success");
                                    // Signed in successfully, show UI.
                                    updateUI(googleAccount);
                                } else {
                                    Log.w(TAG, "signInWithCredential: Failure", resultTask.getException());
                                    updateUI(null);
                                }
                                hideProgressDialog();
                            }
                        });
    }

    private void handleFacebookAccessToken(AccessToken accessToken) {
        Log.d(TAG, "handleFacebookAccessToken: " + accessToken);

        AuthCredential fbCredential = FacebookAuthProvider
                                            .getCredential(accessToken.getToken());

        FirebaseAuth.getInstance().signInWithCredential(fbCredential)
                                  .addOnCompleteListener(Objects.requireNonNull(getActivity()),
                    new OnCompleteListener<AuthResult>() {
                        @SuppressLint({"WrongConstant", "ShowToast"})
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> resultTask) {
                            if(resultTask.isSuccessful()) {
                                Log.d(TAG, "signInWithCredential: Success");

                                String userId = FirebaseAuth.getInstance().getUid();

                                if(userId != null) {
                                    FirebaseDatabase.getInstance()
                                            .getReference(FirebaseDbHelper.FIREBASE_columnUsers)
                                            .child(userId)
                                            .child("fullName").setValue(
                                                    Objects.requireNonNull(FirebaseAuth.getInstance()
                                                           .getCurrentUser()).getDisplayName());

                                    PreferenceManager.getDefaultSharedPreferences(getContext())
                                            .edit()
                                            .putBoolean(CustomNotification.NOTIFY_prefName, true)
                                            .apply();

                                    ActivityUtils.showActivity(getActivity(), HomeActivity.class);
                                } else {
                                    Toast.makeText(getActivity(), "Couldn't connect to Firebase", 0).show();
                                    FirebaseAuth.getInstance().signOut();
                                    LoginManager.getInstance().logOut();
                                }
                            } else {
                                Log.w(TAG, "signInWithCredential: Failure", resultTask.getException());
                                Toast.makeText(getActivity(), "Authentication failed.",
                                        0).show();
                                LoginManager.getInstance().logOut();
                            }
                        }
                    });
    }

    private void hideProgressDialog() {
        pDialog.dismiss();
    }

    private void showProgressDialog(String displayMsg) {
        pDialog.setMessage(displayMsg);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }


    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_signIn);
    }

    private void revokeAccess() {
        // Firebase sign out
        FirebaseAuth.getInstance().signOut();

        FirebaseAuth.getInstance().getCurrentUser().getProviderId();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(Objects.requireNonNull(getActivity()),
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> resultTask) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(@Nullable GoogleSignInAccount googleAccount) {
        if (googleAccount != null) {
            //  String personName = account.getDisplayName();
            //  String personGivenName = account.getGivenName();
            //  String personFamilyName = account.getFamilyName();
            //  String personEmail = account.getEmail();
            //  String personId = account.getId();
            //  Uri personPhoto = account.getPhotoUrl();

            getView().findViewById(R.id.signInGoogle).setVisibility(View.GONE);

            String userId = FirebaseAuth.getInstance().getUid();

            if(userId != null) {
                FirebaseDatabase.getInstance()
                        .getReference(FirebaseDbHelper.FIREBASE_columnUsers)
                        .child(userId)
                        .child("fullName").setValue(googleAccount.getDisplayName());

                PreferenceManager.getDefaultSharedPreferences(getContext())
                        .edit()
                        .putBoolean(CustomNotification.NOTIFY_prefName, true)
                        .apply();

                ActivityUtils.showActivity(getActivity(), HomeActivity.class);
            } else {
                Toast.makeText(getActivity(), "Couldn't connect to Firebase", Toast.LENGTH_SHORT).show();
                revokeAccess();
            }
        } else {
            getView().findViewById(R.id.signInGoogle).setVisibility(View.VISIBLE);
            Toast.makeText(getActivity(), "Sign-in with Google failed" , Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signInGoogle:
                Log.w(TAG, "Click on signInGoogle");
                signInWithGoogle();
                break;

            case R.id.signInFB:
                showProgressDialog("Signing-in... Please, wait");
                fbLoginBtn.performClick();
                break;

            case R.id.loginBtn:
                if(validateFields()) {
                    String emailText    = Objects.requireNonNull(loginEmail.getText()).toString();
                    String passwordText = Objects.requireNonNull(loginPassword.getText()).toString();

                    showProgressDialog("Signing-in... Please, wait");

                    FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(emailText, passwordText)
                            .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<AuthResult>() {
                                @SuppressLint({"WrongConstant", "ShowToast"})
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> resultTask) {
                                    hideProgressDialog();

                                    if(resultTask.isSuccessful()) {
                                        Log.d(TAG, "signInWithEmail: Success");

                                        PreferenceManager.getDefaultSharedPreferences(getContext())
                                                .edit()
                                                .putBoolean(CustomNotification.NOTIFY_prefName, true)
                                                .apply();

                                        ActivityUtils.showActivity(getActivity(), HomeActivity.class);
                                    } else {
                                        Log.w(TAG, "signInWithEmail: Failure", resultTask.getException());
                                        Toast.makeText(getActivity(), "Authentication failed.\nCredentials may be wrong!",
                                                0).show();
                                    }
                                }
                            });
                }
                break;
        }
    }

    private boolean validateFields() {
        return ActivityUtils.validateInput(loginEmail, ActivityUtils.PATTERN_emailAddress,0, getString(R.string.error_inputEmail))
                && ActivityUtils.validateInput(loginPassword, ActivityUtils.PATTERN_userPwd, 16, getString(R.string.error_inputPwd));
    }

    @Override
    public void onStart() {
        super.onStart();
        hideProgressDialog();
    }
}

