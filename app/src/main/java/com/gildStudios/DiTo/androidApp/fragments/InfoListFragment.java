package com.gildStudios.DiTo.androidApp.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.activities.HomeActivity;


public class InfoListFragment extends Fragment {


    // TODO: Rename and change types and number of parameters
    public static InfoListFragment newInstance(String param1, String param2) {
        InfoListFragment fragment = new InfoListFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((HomeActivity) getActivity()).getSupportActionBar().hide();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

      View infoView = inflater.inflate(R.layout.fragment_info_list, container, false);
      TextView myCreditPath = infoView.findViewById(R.id.creditPath);
      TextView myRiskPath = infoView.findViewById(R.id.riskPath);
      TextView myTutorialPath = infoView.findViewById(R.id.tutorial);
      TextView privacyPolicy = infoView.findViewById(R.id.tv_privacy);

        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String privacyUrl = "https://devgildstudios.wixsite.com/privacy-policy";

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(privacyUrl));
                InfoListFragment.this.startActivity(i);
            }
        });


    myCreditPath.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            CreditFragment fragment = new CreditFragment();
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.setCustomAnimations(R.anim.transition_from_right,
                    R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
            transaction.replace(R.id.homeWrapper, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    });

        myRiskPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RiskFragment fragment = new RiskFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                transaction.replace(R.id.homeWrapper, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        myTutorialPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TutorialFragment fragment = new TutorialFragment();
                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left,R.anim.transition_from_left,R.anim.transition_to_right);
                transaction.replace(R.id.homeWrapper, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });


        return infoView;
    }
}
