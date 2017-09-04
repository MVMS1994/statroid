package subbiah.veera.statroid.core;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

import subbiah.veera.statroid.MainActivity;
import subbiah.veera.statroid.R;
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
        remoteViews.setTextViewText(R.id.net, data.getNetwork());
        remoteViews.setTextViewText(R.id.cpu, data.getCpu());
        remoteViews.setTextViewText(R.id.ram, data.getRam());


        if(!builders.containsKey(data.getKey())) {
            int code = (int) (Math.random() * 100);
            Intent intent = new Intent(activity, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    activity,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );
            NotificationCompat.Builder builder = new NotificationCompat.Builder(application)
                    .setSmallIcon(R.drawable.portrait_black_24dp)
                    .setContentIntent(pendingIntent);

            builders.put(data.getKey(), builder);
            codes.put(data.getKey(), code);
        }

        Notification mNotification = builders.get(data.getKey())
                .setContent(remoteViews)
                .build();
        mNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        getNotificationManager(activity).notify(codes.get(data.getKey()), mNotification);
    }

    public static void hideNotifications(Context activity) {
        getNotificationManager(activity).cancelAll();
    }

    @NonNull
    private static android.app.NotificationManager getNotificationManager(Context activity) {
        return (android.app.NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void reset(Context activity) {
        hideNotifications(activity);
        codes.clear();
        builders.clear();
    }
}
