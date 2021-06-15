package com.gildStudios.DiTo.androidApp.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.customs.CustomEditText;
import com.gildStudios.DiTo.androidApp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class MyCredentialsFragment extends Fragment {
    private static final Pattern PATTERN_userPwd =
            Pattern.compile("((?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{6,20})");
    private static final Pattern PATTERN_emailAddress  =
            Pattern.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
            );


    private CustomEditText myNewPassword;
    private CustomEditText myConfirmPassword;
    private Button confirmPasswordBtn;
    private ProgressDialog progress;
    private FirebaseAuth auth;
    private AlertDialog ad;

    public MyCredentialsFragment() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView( final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        final View myCredentialsView = inflater.inflate(R.layout.fragment_my_credentials, container, false);

        myNewPassword = myCredentialsView.findViewById(R.id.newPassword);
        myConfirmPassword = myCredentialsView.findViewById(R.id.confirmNewPassword);
        progress = new ProgressDialog(getContext());
        auth = FirebaseAuth.getInstance();

        confirmPasswordBtn = (Button)myCredentialsView.findViewById(R.id.saveAddress);

        confirmPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               int b = validateInput(myNewPassword, PATTERN_userPwd);
               int c = validateInput(myConfirmPassword, PATTERN_userPwd);
                if((b == 1) & (c==1)) {
                    final String np = myNewPassword.getText().toString();
                    String cp = myConfirmPassword.getText().toString();
                        if(!np.equals(cp)){
                        myConfirmPassword.setError("Passwords are not the same");
                        }else if(np.equals(cp)){
                            View alertView = inflater.inflate(R.layout.alert4, container, false);
                            ad = new AlertDialog.Builder(getContext()).create();
                            ad.setCancelable(false);
                            ad.setView(alertView);
                            ad.create();
                            final CustomEditText credmail = alertView.findViewById(R.id.credentialMail);
                            final CustomEditText credpsw = alertView.findViewById(R.id.credentialPassword);
                            Button positiveButton = alertView.findViewById(R.id.buttonPositive);
                            Button negativeButton = alertView.findViewById(R.id.buttonNegative);
                                positiveButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        int e = validateInput(credmail,PATTERN_emailAddress);
                                        int p = validateInput(credpsw,PATTERN_userPwd);
                                        if(e == 1 && p == 1) {
                                            final String em = credmail.getText().toString();
                                            final String psw = credpsw.getText().toString();
                                            validateNewPsw(myCredentialsView, np, em, psw);
                                        }
                                    }
                                });
                            negativeButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ad.dismiss();
                                }
                            });
                            ad.show();
                            }

                    }
                }
        });

        return myCredentialsView;
    }

    //controlla se tutti e 3 i dati sono stati inseriti correttamente
    private int validateInput(EditText genericInput, Pattern toMatch){
        String fieldText = genericInput.getText().toString();

        if(fieldText.isEmpty()) {
            genericInput.setError("Please complete this field");
            return 0;
        }

        if(toMatch != null) {
            if(!toMatch.matcher(fieldText).matches()) {
                genericInput.setError("Please insert a correct value");
                return 0;
            }
        }

        genericInput.setError(null);
        return 1;
    }

   private void validateNewPsw(View v,final String np,String email,String op){

       final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
       if(user!=null){
           AuthCredential credentials = EmailAuthProvider.getCredential(email,op);
           user.reauthenticate(credentials).addOnCompleteListener(new OnCompleteListener<Void>() {
               @Override
               public void onComplete(@NonNull Task<Void> task) {
                   if(task.isSuccessful()){
                       ad.dismiss();
                       progress.setMessage("Updating Password ");
                       progress.show();
                       user.updatePassword(np).addOnCompleteListener(new OnCompleteListener<Void>() {
                           @Override
                           public void onComplete(@NonNull Task<Void> task) {
                               if(task.isSuccessful()){
                                   progress.dismiss();
                                   Toast.makeText(getContext(),"Password Updated",Toast.LENGTH_SHORT);
                                   FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                   Fragment fm = new HomeFragment();
                                   transaction.setCustomAnimations(R.anim.transition_from_right,
                                           R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                                   transaction.replace(R.id.homeWrapper, fm);
                                   transaction.addToBackStack(null);
                                   transaction.commit();
                               }else {
                                   Toast.makeText(getContext(),"Couldn't update the password",Toast.LENGTH_SHORT);
                               }
                           }
                       });
                   }else{
                       Toast.makeText(getContext(),"Wrong credentials",Toast.LENGTH_LONG);
                   }
               }
           });
       }

   }

}
