package com.gildStudios.DiTo.androidApp.customs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.gildStudios.DiTo.androidApp.R;

public class CustomNotification {
    public static String NOTIFY_prefName = "Haza";
    public static void createNotification(String aMessage, Context context, Class<?> to) {
        final int NOTIFY_privateID = 0; // ID of Notification

        NotificationManager notifManager;

        String id = context.getString(R.string.notify_defaultChannel);
        String title = context.getString(R.string.notify_defaultTitle);

        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        notifManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(notifManager == null)
            return;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if(mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }

            builder = new NotificationCompat.Builder(context, id);
            intent = new Intent(context, to);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            builder.setContentTitle(aMessage)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentText(context.getString(R.string.app_name))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        } else {
            builder = new NotificationCompat.Builder(context, id);
            intent = new Intent(context, to);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            builder.setContentTitle(aMessage)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentText(context.getString(R.string.app_name))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();

        notifManager.notify(NOTIFY_privateID, notification);
    }
}
