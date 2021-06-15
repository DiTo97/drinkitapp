package com.gildStudios.DiTo.androidApp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gildStudios.DiTo.androidApp.customs.CustomNotification;

public class NotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATION_channelId     = "notificationId";
    public static String NOTIFICATION_channelString = "notificationString";

    public void onReceive(Context context, Intent intent) {
        if(!PreferenceManager
                .getDefaultSharedPreferences(context)
                .getBoolean(CustomNotification.NOTIFY_prefName, false)) {
            return;
        }

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION_channelString);
        int id = intent.getIntExtra(NOTIFICATION_channelId, 0);

        Log.d("NotificationReceiver", "" + id);
        if(notificationManager != null)
            notificationManager.notify(id, notification);
    }



}
