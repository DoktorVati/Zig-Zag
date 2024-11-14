package com.zigzag;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class DailyNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Call the method to send a notification when the alarm goes off
        sendNotification(context);
    }

    private void sendNotification(Context context) {
        // Notification Manager
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create the notification (Same logic as before)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Create notification channel for Android O and above
            NotificationChannel channel = new NotificationChannel(
                    "ZigZagChannelID",
                    "ZigZag Updates",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }

            Notification notification = new Notification.Builder(context, "ZigZagChannelID")
                    .setContentTitle("Don't Miss Out On What's Happening on ZigZag!")
                    .setContentText("See what's buzzing right now!")
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)  // our app icon
                    .setAutoCancel(true)
                    .build();

            if (notificationManager != null) {
                notificationManager.notify(1, notification);
            }
        } else {
            Notification notification = new Notification.Builder(context)
                    .setContentTitle("Don't Miss Out On What's Happening on ZigZag!")
                    .setContentText("See what's buzzing right now!")
                    .setSmallIcon(R.mipmap.ic_launcher_foreground)  // our app icon
                    .setAutoCancel(true)
                    .build();

            if (notificationManager != null) {
                notificationManager.notify(1, notification);
            }
        }
    }
}
