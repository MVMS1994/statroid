package subbiah.veera.statroid;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Statroid extends Application {
    private static boolean activityVisible = false;
    private static boolean activityAlive = false;
    @Nullable private Activity currentActivity = null;

    @Override public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }

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
