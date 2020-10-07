package jcu.cp3407.pancreart;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class Application extends android.app.Application {
    public static final String CHANNEL_1_ID = "ChannelOne";
    public static final String CHANNEL_2_ID = "ChannelTwo";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "High Importance Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("Warnings");
            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "Low Importance Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel2.setDescription("Updates");
            NotificationManager manager = getSystemService(NotificationManager.class);
            assert manager != null;
            manager.createNotificationChannel(channel1);
        }
    }
}
