package com.gildStudios.DiTo.androidApp.fragments;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.gildStudios.DiTo.androidApp.customs.CustomNotification;
import com.gildStudios.DiTo.androidApp.Drink;
import com.gildStudios.DiTo.androidApp.NotificationPublisher;
import com.gildStudios.DiTo.androidApp.adapters.GlassListenerAdapter;
import com.gildStudios.DiTo.androidApp.R;
import com.github.alexjlockwood.kyrie.KyrieDrawable;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class CalculatorFragment extends Fragment {
    private View rootView;
    private final static String CHANNEL_ID = "notifChannel";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootView = null;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            Bundle resultKey = this.getArguments();
            if (resultKey != null) {
                rootView = inflater.inflate(R.layout.fragment_calculator, container, false);
                final double tax = Double.parseDouble(Objects.requireNonNull(resultKey
                        .getString("tasso")));
                Log.d("fama",Double.toString(tax));
                final int stomachStatus = resultKey.getInt("stomaco");
                final float alcoholLimit = resultKey.getFloat("alcoholLimit");
                final ImageView glasscontainer = rootView.findViewById(R.id.glasscontainer);
                final TextView resultView = rootView.findViewById(R.id.resultview);
                final TextView warningView = rootView.findViewById(R.id.warningview);
                final TextView whenSober = rootView.findViewById(R.id.whenSober);
                final TextView whenDrive= rootView.findViewById(R.id.whenDrive);
                if(PreferenceManager
                        .getDefaultSharedPreferences(getContext())
                        .getBoolean(CustomNotification.NOTIFY_prefName, false)) {
                    final int hToMs = 60 * 60 * 1000;
                    final int mToMs = 60 * 1000;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String displayName = null;

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            if(currentUser != null && !currentUser.isAnonymous())
                                displayName = currentUser.getDisplayName();

                            if(displayName == null) {
                                displayName = getContext().getString(R.string.notif_now_maiusc);
                            } else {
                                displayName += " " + getContext().getString(R.string.notif_now);
                            }
//                            BigDecimal limitBD = new BigDecimal(String.valueOf(alcoholLimit));
//                            BigDecimal taxBD   = new BigDecimal(String.valueOf(tax));
//
//                            if(taxBD.compareTo(limitBD) > 0) {
//                                scheduleChDrive(getNotification(displayName + " dovresti essere in condizione di Guidare"),
//                                        hToMs * (int) Drink.canDrive(tax, stomachStatus));
//                            }
                            Log.d("CalculatorFragment", "Name in display: " + displayName);

                            scheduleChDrive(getNotification(displayName + " " + getString(R.string.notif_drive)),
                                    Drink.msCalc(Drink.canDrive(tax, stomachStatus)));

                            scheduleChSober(getNotification(displayName + " " + getString(R.string.notif_sober)),
                                    Drink.msCalc(Drink.soberUp(tax, stomachStatus)));

                        }
                    }).run();
                }
                warningView.setMovementMethod(new ScrollingMovementMethod());
                resultView.setVisibility(View.INVISIBLE);
                int[] soberUp= Drink.soberUp(tax, stomachStatus);
                if(tax>0.5) {
                    int[] canDrive= Drink.canDrive(tax,stomachStatus);
                    if(canDrive[0] >= 24)
                        whenDrive.setText(getString(R.string.calc_h));
                    else if(canDrive[0] == 0 && canDrive[1] > 0)
                        whenDrive.setText(canDrive[1] + " min");
                    else if(canDrive[0] == 1)
                        whenDrive.setText(canDrive[0]+" h & "+canDrive[1]+" min");
                    else
                        whenDrive.setText(canDrive[0]+" h & "+canDrive[1]+" min");
                } else whenDrive.setText(getString(R.string.calc_now));

                if(soberUp[0] >= 24)
                    whenSober.setText(getString(R.string.calc_h));
                else if(soberUp[0] == 0 && soberUp[1] > 0)
                    whenSober.setText(soberUp[1] + " min");
                else if(soberUp[0] == 0 && soberUp[1] == 0)
                    whenSober.setText(getString(R.string.calc_now));
                else if(soberUp[0] == 1)
                    whenSober.setText(soberUp[0]+" h & " + soberUp[1] + " min");
                else
                    whenSober.setText(soberUp[0]+" h & "+soberUp[1]+" min");


//                riskPath.setOnClickListener(
//                        new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                RiskFragment fragment = new RiskFragment();
//                                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//                                transaction.setCustomAnimations(R.anim.transition_from_right,
//                                        R.anim.transition_to_left, R.anim.transition_from_left, R.anim.transition_to_right);
//                                transaction.replace(R.id.homeWrapper, fragment);
//                                transaction.addToBackStack(null);
//                                transaction.commit();
//                            }
//                        }
//                );

                final KyrieDrawable glassAnimation = KyrieDrawable.create(getContext(), R.drawable.transition_fill);
                glasscontainer.setImageDrawable(glassAnimation);
                glassAnimation.addListener(new GlassListenerAdapter(resultView, warningView, tax, stomachStatus, alcoholLimit, getContext()));
                glassAnimation.start();
            }
        }
        return rootView;
    }

    private void scheduleChSober(Notification notification, int delay) {
        scheduleNotification(notification, delay, 1);
    }

    private void scheduleChDrive(Notification notification, int delay) {
        scheduleNotification(notification, delay, 2);
    }

    private void scheduleNotification(Notification notification, int delay, int notifId) {

        Intent notificationIntent = new Intent(getContext(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_channelId, notifId);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_channelString, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), notifId, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) Objects.requireNonNull(getContext())
                .getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null)
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private Notification getNotification(String notifMessage) {
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Log.d("CalculatorFragment", "Notifying: " + notifMessage);


        Notification.Builder notifBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notifBuilder = new Notification.Builder(getContext(),CHANNEL_ID);
        }else {notifBuilder = new Notification.Builder(getContext());}
        String NOTIFICATION_groupKey = "com.gildStudios.DiTo.androidApp.NOTIFICATION_groupKey";
        return notifBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(notifMessage)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setGroup(NOTIFICATION_groupKey)
                .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                .setSmallIcon(R.drawable.ic_notifications)
                .setPriority(Notification.PRIORITY_HIGH)
                .build();
    }

}





