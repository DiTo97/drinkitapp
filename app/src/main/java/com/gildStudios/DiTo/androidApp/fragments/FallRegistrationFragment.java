package com.gildStudios.DiTo.androidApp.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.ActivityUtils;
import com.gildStudios.DiTo.androidApp.customs.CustomEditText;
import com.gildStudios.DiTo.androidApp.FirebaseDbHelper;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.activities.AccessActivity;
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
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class FallRegistrationFragment extends Fragment {

    private CustomEditText regName;
    private CustomEditText regEmail;
    private CustomEditText regPsw;
    private Button regBttn;
    private FirebaseAuth auth;
    private GoogleSignInClient gSIC;
    private final static int RC_signIn = 9001;
    private ProgressDialog pDialog;
    private CallbackManager cMan;
    private LoginButton faceButton;


    public FallRegistrationFragment() {
        // Required empty public constructor
    }

    public static FallRegistrationFragment newInstance(String param1, String param2) {
        FallRegistrationFragment fragment = new FallRegistrationFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
        auth=FirebaseAuth.getInstance();
        GoogleSignInOptions gSIO = new GoogleSignInOptions.Builder()
                .requestProfile()
                .requestId()
                .requestIdToken(getString(R.string.default_webClientId))
                .requestEmail()
                .build();
        gSIC = GoogleSignIn.getClient(Objects.requireNonNull(getActivity()),gSIO);
        cMan = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fallView = inflater.inflate(R.layout.fragment_fall_registration, container, false);

        regName=fallView.findViewById(R.id.registrationName);
        regEmail=fallView.findViewById(R.id.registrationEmail);
        regPsw=fallView.findViewById(R.id.registrationPassword);
        regBttn=fallView.findViewById(R.id.signUpBtn);
        faceButton=fallView.findViewById(R.id.fakeSignInFB);

        fallView.findViewById(R.id.signInGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithGoogle();
            }
        });

        regBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean a = ActivityUtils.validateInput(regName,ActivityUtils.PATTERN_fullName, 30, getString(R.string.error_inputName));
                boolean b = ActivityUtils.validateInput(regEmail,ActivityUtils.PATTERN_emailAddress, 0, getString(R.string.error_inputEmail));
                boolean c = ActivityUtils.validateInput(regPsw,ActivityUtils.PATTERN_userPwd, 16, getString(R.string.error_inputPwd));
                if(a && b && c){
                    String em = regEmail.getText().toString();
                    String ps = regPsw.getText().toString();
                    final FirebaseUser user = auth.getCurrentUser();
                    assert user!=null ;
                    if(user.isAnonymous()){
                        showProgressDialog("Signin-in... Please, wait");
                        AuthCredential credentials = EmailAuthProvider.getCredential(em,ps);
                        linkCredentials(credentials,user);
                    }
                }
            }
        });

        fallView.findViewById(R.id.signInFB).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog("Signin-in... Please, wait");
                faceButton.performClick();
            }
        });

        faceButton.setFragment(this);
        faceButton.setReadPermissions("email","public_profile");
        faceButton.registerCallback(cMan, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                        .getSupportActionBar()).hide();
                hideProgressDialog();
                handleFacebookToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                hideProgressDialog();
            }

            @Override
            public void onError(FacebookException error) {
                hideProgressDialog();
                Toast.makeText(getActivity(),"Sign-in with Facebook failed",Toast.LENGTH_SHORT).show();

            }
        });

        return fallView;
    }

    public void signInWithGoogle(){
        Intent signInIntent = gSIC.getSignInIntent();
        startActivityForResult(signInIntent, RC_signIn);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==RC_signIn){
            Task<GoogleSignInAccount> tGSIA = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInGoogle(tGSIA);
        }else{
            cMan.onActivityResult(requestCode,resultCode,data);
        }
    }

    public void handleSignInGoogle(Task<GoogleSignInAccount> task){
        try{
            GoogleSignInAccount gsia = task.getResult(ApiException.class);
            assert (gsia != null);
            firebaseLinkGoogle(gsia);
        }catch(ApiException e){
            Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
        }
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

    private void firebaseLinkGoogle(final GoogleSignInAccount gsia){
         showProgressDialog("Signin-in... Please, wait");
         AuthCredential credentials = GoogleAuthProvider.getCredential(gsia.getIdToken(),null);
         FirebaseUser user = auth.getCurrentUser();
         assert user != null;
         if(user.isAnonymous()){
             linkCredentials(credentials,user);
         }
    }

    private void linkCredentials(AuthCredential credentials, final FirebaseUser user){
        user.linkWithCredential(credentials).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                        .getSupportActionBar()).hide();
                hideProgressDialog();
                if(task.isSuccessful()){
                    Toast.makeText(getContext(),"User Upgraded",Toast.LENGTH_SHORT).show();
                    String userId=auth.getUid();
                    if(userId!=null){
                        FirebaseDatabase.getInstance()
                                .getReference(FirebaseDbHelper.FIREBASE_columnUsers)
                                .child(userId)
                                .child("fullName")
                                .setValue(FirebaseAuth.getInstance()
                                        .getCurrentUser()
                                        .getDisplayName());
                    }else{
                        Toast.makeText(getActivity(),"Database will save you later",Toast.LENGTH_SHORT).show();
                    }
                    ActivityUtils.showActivity(getActivity(),AccessActivity.class,true);
                }else {
                    if(gSIC!=null){
                        gSIC.revokeAccess();
                    }
                    if(LoginManager.getInstance()!=null){
                        LoginManager.getInstance().logOut();
                    }

                    if(task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getActivity(), "Email already used.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Registration failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void handleFacebookToken(AccessToken token){
        AuthCredential credentials = FacebookAuthProvider.getCredential(token.getToken());
        FirebaseUser user = auth.getCurrentUser();
        assert user != null;
        if(user.isAnonymous()){
            linkCredentials(credentials,user);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
    }

    @Override
    public void onStart() {
        super.onStart();
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
        if (pDialog != null) {
            hideProgressDialog();
        }
    }
}


