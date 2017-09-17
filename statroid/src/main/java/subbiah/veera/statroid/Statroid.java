package subbiah.veera.statroid;

import android.app.Application;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Statroid extends Application {
    private static boolean activityVisible;
    private static boolean activityAlive;

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
}
