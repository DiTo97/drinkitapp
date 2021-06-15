package com.gildStudios.DiTo.androidApp.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.gildStudios.DiTo.androidApp.AppStartHandler;
import com.gildStudios.DiTo.androidApp.R;
import com.gildStudios.DiTo.androidApp.fragments.DrinkHistoryFragment;
import com.gildStudios.DiTo.androidApp.fragments.HomeFragment;
import com.gildStudios.DiTo.androidApp.fragments.InfoListFragment;
import com.gildStudios.DiTo.androidApp.fragments.MyDrinksFragment;
import com.gildStudios.DiTo.androidApp.fragments.SettingListFragment;
import com.gildStudios.DiTo.androidApp.fragments.TutorialFragment;
import com.gildStudios.DiTo.androidApp.services.CloudUpdateFlow;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity  {

    private final String TAG = "HomeActivity";
    FragmentManager fragmentManager = getSupportFragmentManager();
    String fragmentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FragmentTransaction homeTransact = fragmentManager.beginTransaction();

        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currentFragment = fragmentManager.getFragments().get(0);
                fragmentName = currentFragment.getClass().getSimpleName();
            }
        });

        int appStartState = AppStartHandler.checkAppStart(this,
                PreferenceManager.getDefaultSharedPreferences(this), true);

        if (appStartState == 0) {
            Log.d("Haza", "Tuts open");
            TutorialFragment tutsFragment = new TutorialFragment();
            homeTransact.add(R.id.homeWrapper, tutsFragment);
        } else {
            HomeFragment homeFragment = new HomeFragment();
            homeTransact.add(R.id.homeWrapper, homeFragment);
            Log.d("Haza", "Default");
        }
        homeTransact.commit();

        // Schedule job to update Firebase
        scheduleJobFirebaseUpdateFlow();
    }


    @Override
    public boolean onCreateOptionsMenu (Menu menu) {

        if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isOffline", false))
            getMenuInflater().inflate(R.menu.menu_main_offline, menu);
        else {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if(currentUser != null && currentUser.isAnonymous())
                getMenuInflater().inflate(R.menu.menu_main_guest, menu);
            else
                getMenuInflater().inflate(R.menu.menu_main, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.actionSettings:
                SettingListFragment fragment = new SettingListFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                transaction.replace(R.id.homeWrapper, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
                return true;


            case R.id.actionInfo:
                InfoListFragment fragment2 = new InfoListFragment();
                FragmentTransaction transaction2 = getSupportFragmentManager().beginTransaction();
                transaction2.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                transaction2.replace(R.id.homeWrapper, fragment2);
                transaction2.addToBackStack(null);
                transaction2.commit();
                return true;

            case R.id.actionMyDrinks:

                final MyDrinksFragment fragment4 = new MyDrinksFragment();
                FragmentTransaction transaction4 = getSupportFragmentManager().beginTransaction();
                transaction4.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                transaction4.replace(R.id.homeWrapper, fragment4);
                transaction4.addToBackStack(null);
                transaction4.commit();
                return true;

            case R.id.actionDrinkHistory:
                DrinkHistoryFragment fragment3 = new DrinkHistoryFragment();
                FragmentTransaction transaction3 = getSupportFragmentManager().beginTransaction();
                transaction3.setCustomAnimations(R.anim.transition_from_right,
                        R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
                transaction3.replace(R.id.homeWrapper, fragment3);
                transaction3.addToBackStack(null);
                transaction3.commit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed(){
        if(fragmentManager.getBackStackEntryCount() == 0) {
            this.finish();
        } else {
            switch(fragmentName){
                case "UberSettingFragment":
                    fragmentManager.popBackStack();
                    break;
                case "GetHomeSafeFragment":
                    fragmentManager.popBackStack();
                    break;
                case "MyCredentialsFragment":
                    fragmentManager.popBackStack();
                    break;
                case "MyNotificationFragment":
                    fragmentManager.popBackStack();
                    break;
                case "CreditFragment":
                    fragmentManager.popBackStack();
                    break;
                case "RiskFragment":
                    fragmentManager.popBackStack();
                    break;
                case "CalculatorFragment":
                    fragmentManager.popBackStack();
                    break;
                case "FallRegistrationFragment":
                    fragmentManager.popBackStack();
                    break;
                case "ReflexTestFragment":
                    fragmentManager.popBackStack();
                    break;
                case "DrinkFragment":
                    fragmentManager.popBackStack();
                    break;
                case "UserDataFragment":
                    fragmentManager.popBackStack();
                    break;
                case "DrinkHistoryFragment":
                    fragmentManager.popBackStack();
                    break;
                case "ListDrinksHistoryFragment":
                    fragmentManager.popBackStack();
                    break;
                case "TutorialFragment":
                    fragmentManager.popBackStack();
                    break;
                default:
                    fragmentManager.popBackStack();
                    this.getSupportActionBar().show();
                    break;
            }
        }
    }

    private void scheduleJobFirebaseUpdateFlow(){
        JobScheduler jobScheduler = (JobScheduler) getApplicationContext()
                .getSystemService(JOB_SCHEDULER_SERVICE);

        if(jobScheduler != null) {
            ComponentName componentName = new ComponentName(this,
                    CloudUpdateFlow.class);

            JobInfo jobInfo = new JobInfo.Builder(CloudUpdateFlow.JOB_serviceId, componentName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(1000 * 6, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
                    .setPeriodic(1000 * 60 * 15)
                        .build();

            jobScheduler.schedule(jobInfo);
        }
    }

}
