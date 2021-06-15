
package com.gildStudios.DiTo.androidApp.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.AlertDialog;

import com.gildStudios.DiTo.androidApp.ActivityUtils;
import com.gildStudios.DiTo.androidApp.customs.CustomNotification;
import com.gildStudios.DiTo.androidApp.R;

import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.gildStudios.DiTo.androidApp.activities.SplashActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.Objects;


public class SettingListFragment extends Fragment {

    public SettingListFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Objects.requireNonNull(((HomeActivity) Objects.requireNonNull(getActivity()))
                .getSupportActionBar()).hide();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View settingView = inflater.inflate(R.layout.fragment_setting_list, container, false);


        TextView myNotificationPath = settingView.findViewById(R.id.notificationPath);
        TextView myUberSettingPath  = settingView.findViewById(R.id.uberSettingPath);
        TextView myNewPasswordPath  = settingView.findViewById(R.id.newPasswordPath);

        TextView myLogOutPath = settingView.findViewById(R.id.logoutPath);
        TextView myRegistrationPath  = settingView.findViewById(R.id.loginPath);

        myNotificationPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyNotificationFragment fragment = new MyNotificationFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                transaction.replace(R.id.homeWrapper, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        myUberSettingPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UberSettingFragment fragment = new UberSettingFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                transaction.replace(R.id.homeWrapper, fragment);
                transaction.addToBackStack(null);
                transaction.commit();

            }

        });

        myNewPasswordPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyCredentialsFragment fragment = new MyCredentialsFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                transaction.replace(R.id.homeWrapper, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

        });

        myLogOutPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final TextView title = new TextView(getContext());
                title.setText("Attenzione:");
                title.setPadding(0,10,0,0);
                title.setTextColor(ContextCompat.getColor(getContext(), R.color.heartGold));
                title.setGravity(Gravity.CENTER);
                title.setTextSize(20);

                AlertDialog.Builder alert = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme);
                alert   .setMessage(getString(R.string.logout_warn))
                        .setCustomTitle(title)
                        .setCancelable(false)
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FirebaseAuth.getInstance().signOut();
                                PreferenceManager.getDefaultSharedPreferences(getContext())
                                        .edit()
                                        .remove(CustomNotification.NOTIFY_prefName)
                                        .apply();

                                ActivityUtils.showActivity(getActivity(), SplashActivity.class, true);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // CANCEL
                            }
                        });
                // Create the AlertDialog object and return it
                AlertDialog dialog = alert.create();
                dialog.show();
                TextView messageView = dialog.findViewById(android.R.id.message);
                messageView.setGravity(Gravity.CENTER);
                Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                Button btnNegative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                layoutParams.weight = 10;
                btnPositive.setLayoutParams(layoutParams);
                btnNegative.setLayoutParams(layoutParams);

                btnNegative.setTextColor(ContextCompat.getColor(getContext(), R.color.chiantiClean));
                btnPositive.setTextColor(ContextCompat.getColor(getContext(), R.color.chiantiClean));
            }
        });

        myRegistrationPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FallRegistrationFragment fragment = new FallRegistrationFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.homeWrapper, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser != null) {
            if(currentUser.isAnonymous()) {
                myUberSettingPath.setEnabled(false);
                myNewPasswordPath.setEnabled(false);
                myNotificationPath.setEnabled(false);

                myNotificationPath.setTextColor(this.getResources().getColor(R.color.grayClean));
                myUberSettingPath.setTextColor(this.getResources().getColor(R.color.grayClean));
                myNewPasswordPath.setTextColor(this.getResources().getColor(R.color.grayClean));

                myLogOutPath.setVisibility(View.INVISIBLE);
                myRegistrationPath.setVisibility(View.VISIBLE);
                myNewPasswordPath.setVisibility(View.VISIBLE);
            } else {
                myLogOutPath.setVisibility(View.VISIBLE);
                myRegistrationPath.setVisibility(View.INVISIBLE);

                boolean email = false;
                for (UserInfo userinfo : currentUser.getProviderData()) {
                    if (userinfo.getProviderId().equals("password")) {
                        email = true;
                        break;
                    }
                }
                if(!email){
                    myNewPasswordPath.setEnabled(false);
                    myNewPasswordPath.setTextColor(this.getResources().getColor(R.color.grayClean));
                }
            }
        }

        return settingView;
    }
}
