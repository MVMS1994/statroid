package subbiah.veera.statroid;

import android.app.Application;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Statroid extends Application {
    private static boolean activityVisible;

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }
}
