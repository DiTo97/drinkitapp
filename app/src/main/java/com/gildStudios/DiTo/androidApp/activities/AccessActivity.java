package com.gildStudios.DiTo.androidApp.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.gildStudios.DiTo.androidApp.ActivityUtils;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.fragments.LoginFragment;
import com.gildStudios.DiTo.androidApp.fragments.RegistrationFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccessActivity extends AppCompatActivity {
    private static final String TAG = "AccessActivity";

    private TabLayout tabLayout;
    private ViewPager accessPager;

    private Activity mContext;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access);

        accessPager = findViewById(R.id.pager);
        accessPager.setAdapter(new AccessAdapter(getSupportFragmentManager()));

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(accessPager);

        mAuth = FirebaseAuth.getInstance();

        mContext = this;

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                if(currentUser != null) {
                    Log.d(TAG, "onAuthStateChanged: signedIn: " + currentUser.getUid());
                    ActivityUtils.showActivity(mContext, HomeActivity.class);
                } else {
                    Log.d(TAG, "onAuthStateChanged: signedOut");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    public class AccessAdapter extends FragmentPagerAdapter {
        private String[] tabTitles = {
                getString(R.string.tab_login),
                getString(R.string.tab_registration) };

        public AccessAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return LoginFragment.newInstance(position);
                case 1:
                    return RegistrationFragment.newInstance(position, "DrinkItApp");
            }
            return null;
        }

        @Override
        public int getCount() {
            return tabTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}