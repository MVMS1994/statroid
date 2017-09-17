package subbiah.veera.statroid.core;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import java.math.BigDecimal;
import java.util.Date;

import subbiah.veera.statroid.Statroid;
import subbiah.veera.statroid.data.DBHelper;
import subbiah.veera.statroid.data.Data;
import subbiah.veera.statroid.data.Logger;

import static subbiah.veera.statroid.data.Constants.DBConstants.*;

/**
 * Created by Veera.Subbiah on 04/09/17.
 */

public class StatsService extends Service implements Runnable {
    private static final String TAG = "StatsService";

    private boolean shouldStop = false;
    private long iterationCount;

    private String[] projection;
    private double[] values;

    private volatile Data data;
    private BroadcastReceiver battery;
    @Nullable
    private SQLiteDatabase db = null;


    @Override
    public void onCreate() {
        super.onCreate();

        db = DBHelper.init(this);
        iterationCount = 0;
        projection = new String[]{TIME, NET, CPU};
        values = new double[]{0, 0, 0};

        NotificationManager.reset(this);
        data = new Data();
        data.setKey("stats");

        battery = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                double total = round((level * 100) / (float) scale, 0);

                Logger.d(TAG, "Battery Used - " + total);
                data.setBat(total);
            }
        };

        netinfo();
        cpuinfo();
        batteryinfo();
        raminfo(this);

        new Thread(this).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        shouldStop = true;
        NotificationManager.reset(this);
        try {
            unregisterReceiver(battery);
        } catch (IllegalArgumentException e) {
            Logger.d(TAG, "battery Receiver not registered");
        }
        DBHelper.reset(db);
        db = null;

        restartService();
        super.onTaskRemoved(rootIntent);
    }

    private void restartService() {
        Intent intent = new Intent(getApplicationContext(), StatsService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, SystemClock.elapsedRealtime() + 500, pendingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void run() {
        try {
            while (!shouldStop) {
                iterationCount++;
                NotificationManager.showNotification(data, this);
                writeToDB(data);
                if(Statroid.isActivityVisible()) {

                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Logger.e(TAG, "This Happened: ", e);
        }
    }

    private void writeToDB(Data data) {
        if (iterationCount <= 60) {
            values[0] = new Date().getTime();
            values[1] += data.getNetwork(); // Net
            values[2] += data.getCpu(); // CPU
        }
        if (db != null && iterationCount == 60) {
            values[1] /= 60.0; // Net
            values[2] /= 60.0; // CPU
            iterationCount = 0;
            DBHelper.write(projection, values, db);
        }
    }

    private void netinfo() {
        final long[] prevNetwork = {-1};
        new Thread("NetworkInfo") {
            @Override
            public void run() {
                while (!shouldStop) {
                    long total = TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
                    if (prevNetwork[0] == -1) {
                        prevNetwork[0] = total;
                        data.setNetwork(0);
                        data.setNetworkUnit("KB/s");
                    }
                    double answer = total - prevNetwork[0];
                    prevNetwork[0] = total;

                    final long KB = 1024, MB = KB * 1024, GB = MB * 1024;
                    String unit = " B/s";
                    if (answer >= GB) {
                        answer /= GB;
                        unit = " GB/s";
                    } else if (answer >= MB) {
                        answer /= MB;
                        unit = " MB/s";
                    } else if (answer >= KB) {
                        answer /= KB;
                        unit = " KB/s";
                    }

                    Logger.d(TAG, "Net Used - " + round(answer, 1) + unit);
                    data.setNetwork(round(answer, 1));
                    data.setNetworkUnit(unit);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Logger.e(TAG, "This Happened: ", e);
                    }
                }
            }
        }.start();
    }

    private void batteryinfo() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(battery, ifilter);
    }


    private void cpuinfo() {
        new Thread("CPU Info") {

            @Override
            public void run() {
                while (!shouldStop) {
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
                        data.setCpu(total);

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Logger.e(TAG, "This Happened: ", e);
                        }
                    } catch (Exception e) {
                        Logger.e(TAG, "This Happened: ", e);
                    }
                }
            }
        }.start();
    }

    private void raminfo(final Context context) {
        new Thread("RAM Info") {
            @Override
            public void run() {
                while (!shouldStop) {
                    ActivityManager actManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
                    actManager.getMemoryInfo(memInfo);
                    double total = round(memInfo.availMem / (1024 * 1024 * 1024.0), 2);
                    Logger.d(TAG, "RAM Used - " + total);

                    data.setRam(total);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Logger.e(TAG, "This Happened: ", e);
                    }
                }
            }
        }.start();
    }

    private static double round(double val, int places) {
        return new BigDecimal(val).setScale(places, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }
}
