package subbiah.veera.statroid;

import android.app.Activity;
import android.app.Application;


import androidx.annotation.Nullable;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.squareup.leakcanary.LeakCanary;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

@SuppressWarnings("unused")
public class Statroid extends Application {
    private static boolean activityVisible = false;
    private static boolean activityAlive = false;
    @Nullable private Activity currentActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().build()).build());
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
