package subbiah.veera.statroid.core;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.os.BatteryManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Date;

import subbiah.veera.statroid.data.DBHelper;
import subbiah.veera.statroid.data.Data;
import subbiah.veera.statroid.data.Logger;

import static subbiah.veera.statroid.core.SystemUtils.convertToSuitableNetworkUnit;
import static subbiah.veera.statroid.core.SystemUtils.round;
import static subbiah.veera.statroid.data.Constants.DBConstants.CPU;
import static subbiah.veera.statroid.data.Constants.DBConstants.NET;
import static subbiah.veera.statroid.data.Constants.DBConstants.TIME;
import static subbiah.veera.statroid.data.Constants.DBConstants.WRITE;
import static subbiah.veera.statroid.data.Constants.DOWNLOAD_NET;
import static subbiah.veera.statroid.data.Constants.UPLOAD_NET;

/**
 * Created by Veera.Subbiah on 04/09/17.
 */

public class StatsService extends Service implements Runnable {
    private static final String TAG = "StatsService";

    private boolean shouldStop = false;
    @SuppressWarnings("deprecation")
    private int prevMinute = new Date().getMinutes() - 1;

    private String[] projection;
    private double[] values;

    private volatile Data data;
    private BroadcastReceiver battery;
    @Nullable private DBHelper db = null;

    // private WebServer webServer;


    @Override
    public void onCreate() {
        super.onCreate();

        db = DBHelper.init(this, WRITE);
        projection = new String[]{TIME, NET, CPU, DOWNLOAD_NET, UPLOAD_NET};
        values = new double[]{0, 0, 0, 0, 0};
        // webServer = new WebServer(4000, this);
        // webServer.start();

        ActivityManager actManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memInfo = new ActivityManager.MemoryInfo();
        if (actManager != null) {
            actManager.getMemoryInfo(memInfo);
        }

        NotificationManager.reset(this);
        data = Data.init();
        data.setKey("stats");
        data.setTotalRam(round(memInfo.totalMem / (1024 * 1024 * 1024.0), 2));

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

        new Thread(this, "stats_service").start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        shouldStop = true;
        // webServer.stop();
        NotificationManager.reset(this);
        try {
            unregisterReceiver(battery);
        } catch (IllegalArgumentException e) {
            Logger.d(TAG, "battery Receiver not registered");
        }
        if (db != null) {
            db.reset(WRITE);
        }
        db = null;
        Data.reset();

        super.onTaskRemoved(rootIntent);

        PendingIntent service = PendingIntent.getService(
                getApplicationContext(),
                1001,
                new Intent(getApplicationContext(), StatsService.class),
                PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 100, service);
        }
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
                NotificationManager.showNotification(data, this);
                writeToDB(data);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Logger.e(TAG, "This Happened: ", e);
        }
    }

    private void writeToDB(Data data) {
        Date date = new Date();

        @SuppressWarnings("deprecation") int currentMin = date.getMinutes();
        if (prevMinute == currentMin) {
            values[1] += data.getNetwork(); // Net
            values[2] += data.getCpu(); // CPU
            values[3] += data.getDownload(); // Download
            values[4] += data.getUpload(); // Upload
        } else {
            if (db != null) {
                values[0] = date.getTime();
                values[1] /= 60.0; // Net
                values[2] /= 60.0; // CPU
                values[3] /= 60.0; // Download
                values[4] /= 60.0; // Upload
                prevMinute = currentMin;
                db.write(projection, values);
            }
        }
    }

    private void netinfo() {
        final long[] prevNetwork = {-1, -1}; // upload, download, total
        new Thread("NetworkInfo") {
            @Override
            public void run() {
                while (!shouldStop) {
                    long upload = TrafficStats.getTotalTxBytes();
                    long download = TrafficStats.getTotalRxBytes();
                    if (prevNetwork[0] == -1) {
                        prevNetwork[0] = upload;
                        prevNetwork[1] = download;
                        data.setNetwork(0, 0);
                    }
                    double newUpload = upload - prevNetwork[0];
                    double newDownload = download - prevNetwork[1];
                    prevNetwork[0] = upload;
                    prevNetwork[1] = download;

                    Logger.d(TAG, "Net Used - " + convertToSuitableNetworkUnit(newUpload + newDownload));
                    data.setNetwork(newUpload, newDownload);
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
                        String rawTop = SystemUtils.runADB("/system/bin/cat /proc/stat");
                        // rawTop = rawTop.substring(0, rawTop.indexOf("User", 4));

                        String[] entities = rawTop.split("\n")[0].split("[ ]+");
                        int total = 0;
//                        for (String entity : entities) {
//                            if(entity.equalsIgnoreCase("cpu")) continue;
//                            int percent = Integer.parseInt(entity);
//                            total += percent;
//                        }
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
                    if(actManager == null) return;

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

    @SuppressWarnings("unused")
    private void startFTPServer() {
        // webServer = new WebServer(4000, this);
        // webServer.start();
    }
}
