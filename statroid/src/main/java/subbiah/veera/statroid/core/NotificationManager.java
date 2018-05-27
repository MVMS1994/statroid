package subbiah.veera.statroid.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

import subbiah.veera.statroid.MainActivity;
import subbiah.veera.statroid.R;
import subbiah.veera.statroid.Statroid;
import subbiah.veera.statroid.data.Data;

/**
 * Created by Veera.Subbiah on 04/09/17.
 */

@SuppressWarnings("WeakerAccess")
public class NotificationManager {
    private static final Map<String, Integer> codes = new HashMap<>();
    private static final Map<String, NotificationCompat.Builder> builders = new HashMap<>();

    public static void showNotification(Data data, Context activity) {
        Context application = activity.getApplicationContext();

        RemoteViews remoteViews = new RemoteViews(application.getPackageName(), R.layout.notification_content);
        remoteViews.setTextViewText(R.id.net, SystemUtils.convertToSuitableNetworkUnit(data.getNetwork()));
        remoteViews.setTextViewText(R.id.cpu, data.getCpu() + "%");
        remoteViews.setTextViewText(R.id.ram, data.getRam() + " GB");
        remoteViews.setTextViewText(R.id.bat, data.getBat() + "%");

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            int padding = SystemUtils.dpToPx(16);
            remoteViews.setViewPadding(R.id.notification, padding, padding, padding, padding);
        } else {
            int padding = SystemUtils.dpToPx(16);
            remoteViews.setViewPadding(R.id.notification, padding, 0, padding, 0);
        }

        if(!Statroid.isActivityVisible()) {
            Intent intent = new Intent(activity, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    activity,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            remoteViews.setOnClickPendingIntent(R.id.app_image, pendingIntent);
        } else {
            remoteViews.setOnClickPendingIntent(R.id.app_image, null);
        }

        if(!builders.containsKey(data.getKey())) {
            int code = (int) (Math.random() * 100);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(application, String.valueOf(code))
                    .setSmallIcon(R.drawable.portrait_black_24dp)
                    .setPriority(NotificationCompat.PRIORITY_MAX);

            builders.put(data.getKey(), builder);
            codes.put(data.getKey(), code);
        }

        Notification mNotification = builders.get(data.getKey())
                .setCustomContentView(remoteViews)
                .build();
        mNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        getNotificationManager(activity).notify(codes.get(data.getKey()), mNotification);
    }

    public static void hideNotifications(Context activity) {
        getNotificationManager(activity).cancelAll();
    }

    private static android.app.NotificationManager getNotificationManager(Context activity) {
        return (android.app.NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void reset(Context activity) {
        hideNotifications(activity);
        codes.clear();
        builders.clear();
    }
}
