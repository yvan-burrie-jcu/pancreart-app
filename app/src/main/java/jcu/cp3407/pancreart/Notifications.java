package jcu.cp3407.pancreart;

import android.app.PendingIntent;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public class Notifications {
    public Notifications() {
    }

    public NotificationCompat.Builder buildHighPriority(Context context, PendingIntent pendingIntent,
                                                        String title, String contentText) {
        return new NotificationCompat.Builder(context, Application.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_baseline_message_24) // change to logo
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
    }

    public NotificationCompat.Builder buildLowPriority(Context context, PendingIntent pendingIntent,
                                                       String title, String contentText) {
        return new NotificationCompat.Builder(context, Application.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_baseline_message_24) // change to logo
                .setContentTitle(title)
                .setContentText(contentText)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
    }
}
