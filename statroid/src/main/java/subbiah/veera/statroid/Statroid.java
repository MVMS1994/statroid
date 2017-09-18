package subbiah.veera.statroid;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Statroid extends Application {
    private static boolean activityVisible = false;
    private static boolean activityAlive = false;
    @Nullable private Activity currentActivity = null;

    public static boolean isActivityAlive() {
        return activityAlive;
    }

    public static void setActivityAlive(boolean activityAlive) {
        Statroid.activityAlive = activityAlive;
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void setActivityVisible(boolean activityVisible) {
        Statroid.activityVisible = activityVisible;
    }

    @Nullable
    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(@Nullable Activity currentActivity) {
        this.currentActivity = currentActivity;
    }
}
