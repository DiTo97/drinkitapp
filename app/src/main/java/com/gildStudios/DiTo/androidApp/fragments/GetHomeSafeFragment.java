package com.gildStudios.DiTo.androidApp.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.customs.DrawableTextView;

import java.util.Objects;

public class GetHomeSafeFragment extends Fragment {

    private FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFragmentManager = Objects.requireNonNull(getActivity())
                .getSupportFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View getHomeView = inflater.inflate(R.layout.fragment_get_home_safe, container, false);

        final DrawableTextView tvCustomUber      = getHomeView.findViewById(R.id.textViewCallUber);
        final DrawableTextView tvCustomAmbulance = getHomeView.findViewById(R.id.textViewCallAmbulance);
        final DrawableTextView tvCustomPolice   = getHomeView.findViewById(R.id.textViewCallPolice);
        final DrawableTextView tvCustomTaxi      = getHomeView.findViewById(R.id.textViewCallTaxi);

        tvCustomUber.setOnDrawableClickListener(new DrawableTextView.OnDrawableClickListener() {
            @Override
            public void onClick(DrawablePosition drawableTarget) {
                if(drawableTarget == DrawablePosition.Right) {
                    Log.d("DrawableTextView", "goToUber > Done");

                    TaxiFragment taxiFragment = new TaxiFragment();

                    FragmentTransaction fragmentTrans = mFragmentManager.beginTransaction();
                    fragmentTrans.setCustomAnimations(R.anim.transition_from_right,
                            R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                    fragmentTrans.replace(R.id.homeWrapper, taxiFragment);
                    fragmentTrans.addToBackStack(null);

                    fragmentTrans.commit();
                }
            }
        });

        tvCustomUber.setDefaults();

        tvCustomAmbulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewAmbulance) {
                showDevelopmentToast();
            }
        });

        tvCustomPolice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewPolice) {
                showDevelopmentToast();
            }
        });

        tvCustomTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewTaxi) {
                showDevelopmentToast();
            }
        });

        tvCustomAmbulance.setDefaults();
        tvCustomPolice.setDefaults();
        tvCustomTaxi.setDefaults();

        tvCustomAmbulance.developmentMode();
        tvCustomPolice.developmentMode();
        tvCustomTaxi.developmentMode();

        return getHomeView;
    }

    private void showDevelopmentToast() {
        SingleToast.show(getContext(), getString(R.string.getHome_toast_notReady),
                    Toast.LENGTH_SHORT);
    }

    private static class SingleToast {

        private static Toast mToast;

        public static void show(Context actvContext, String text, int duration) {
            if(mToast != null)
                mToast.cancel();

            (mToast = Toast.makeText(actvContext,
                    text, duration)).show();
        }
    }

}
