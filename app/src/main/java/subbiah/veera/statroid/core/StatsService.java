package subbiah.veera.statroid.core;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import subbiah.veera.statroid.data.Data;
import subbiah.veera.statroid.data.Logger;

/**
 * Created by Veera.Subbiah on 04/09/17.
 */

public class StatsService extends Service implements Runnable {
    @SuppressWarnings("unused")
    private static final String TAG = "StatsService";

    private boolean shouldStop = false;
    private Intent batteryStatus;

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        batteryStatus = registerReceiver(null, ifilter);

        new Thread(this).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        shouldStop = true;
        NotificationManager.reset(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        try {
            while(!shouldStop) {
                Thread.sleep(1000);
                Data data = new Data()
                        .setKey("stats")
                        .setCpu(cpuinfo())
                        .setRam(raminfo(this))
                        .setNetwork("NA")
                        .setBat(batteryinfo());
                NotificationManager.showNotification(data, this);
            }
        } catch (InterruptedException e) {
            Logger.e(TAG, "This Happened: ", e);
        }
    }

    private double batteryinfo() {
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        return round(level / (float)scale, 2) * 100;
    }


    private static int cpuinfo() {
        try {
            String rawTop = SystemUtils.runADB("top -n 1 -m 1");
            rawTop = rawTop.substring(0, rawTop.indexOf("User", 4));

            String[] entities = rawTop.split(",");
            int total = 0;
            for (String entity : entities) {
                String[] row = entity.split(" ");
                int percent = Integer.parseInt(row[row.length - 1].replace("%", ""));
                total += percent;
            }
            Logger.d(TAG, "CPU Used - " + total);


            return total;
        } catch (Exception e) {
            Logger.e(TAG, "This Happened: ", e);
        }
        return 0;
    }

    private static double raminfo(Context context) {
        ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        actManager.getMemoryInfo(memInfo);
        double total = round(memInfo.availMem / (1024 * 1024 * 1024.0), 2);
        Logger.d(TAG, "RAM Used - " + total);

        return total;
    }

    @SuppressWarnings("SameParameterValue")
    private static double round(double val, int places) {
        if(places == 0) return val;

        val *= Math.pow(10, places);
        val = Math.round(val);
        return val/Math.pow(10, places);
    }
}
