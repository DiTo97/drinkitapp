package com.gildStudios.DiTo.androidApp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import com.gildStudios.DiTo.androidApp.customs.CustomNotification;
import com.gildStudios.DiTo.androidApp.R;


public class MyNotificationFragment extends Fragment {



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View MyNotificationView = inflater.inflate(R.layout.fragment_my_notification,
                container, false);

        Switch simpleSwitch = MyNotificationView.findViewById(R.id.simple_switch);
        final SharedPreferences notificationPreference = PreferenceManager
                .getDefaultSharedPreferences(getContext());

        if(notificationPreference.getBoolean(CustomNotification.NOTIFY_prefName,false)){
            simpleSwitch.setChecked(true);
        }else{
            simpleSwitch.setChecked(false);
        }

        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                notificationPreference.edit()
                        .putBoolean(CustomNotification.NOTIFY_prefName,isChecked)
                        .apply();
            }
        });


        return MyNotificationView;
    }
}


