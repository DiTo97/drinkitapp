
package com.gildStudios.DiTo.androidApp.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.gildStudios.DiTo.androidApp.DrinkItApplication;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;
import com.gildStudios.DiTo.androidApp.adapters.CarListenerAdapter;
import com.gildStudios.DiTo.androidApp.CarOnClickListener;

import com.gildStudios.DiTo.androidApp.R;
import com.github.alexjlockwood.kyrie.KyrieDrawable;

public class ReflexTestFragment extends Fragment {

    private KyrieDrawable carDrawable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View reflexView = inflater.inflate(R.layout.fragment_test, container, false);
        final ImageView carcontainer = reflexView.findViewById(R.id.test_view);
        final Button testButton = reflexView.findViewById(R.id.test_button);
        // carcontainer.setVisibility(View.INVISIBLE);

        Bundle refreshControl = this.getArguments();

        if (refreshControl == null || !(refreshControl.getString("refresh").equals(CarListenerAdapter.coso))) {
            if(DrinkItApplication.isFirstReflex) {
                DrinkItApplication.isFirstReflex = false;
                View alertView = inflater.inflate(R.layout.alert, container, false);
                final AlertDialog ad = new AlertDialog.Builder(getContext(), R.style.AlertDialogTheme)
                        .setView(alertView)
                        .setCancelable(false)
                        .create();

                TextView continueView = alertView.findViewById(R.id.continueAnimation);
                continueView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ad.dismiss();
                        carcontainer.setVisibility(View.VISIBLE);
                    }
                });

                ad.show();
            }
        } else {
            carcontainer.setVisibility(View.VISIBLE);
        }


        testButton.setText(R.string.tv_start_reflex_test);
        carDrawable = KyrieDrawable.create(getContext(), R.drawable.transition_car);
        carcontainer.setImageDrawable(carDrawable);
        testButton.setOnClickListener(new CarOnClickListener(carDrawable,carcontainer,testButton));
        carDrawable.addListener(new CarListenerAdapter(testButton, getActivity(), carcontainer, container, inflater));

        return reflexView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Log.d("fixing", "onDestroyView");

        if(carDrawable != null)
            carDrawable.clearListeners();

        HomeActivity homeActivity = ((HomeActivity) getActivity());
        assert homeActivity != null;

        assert homeActivity.getSupportActionBar() != null;
        homeActivity.getSupportActionBar().show();
    }

}


