package subbiah.veera.statroid.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import java.util.Date;
import java.util.Locale;

import subbiah.veera.statroid.R;
import subbiah.veera.statroid.data.Constants;
import subbiah.veera.statroid.data.DBHelper;
import subbiah.veera.statroid.data.Data;
import subbiah.veera.statroid.data.Logger;

import static subbiah.veera.statroid.data.Constants.DBConstants.READ;
import static subbiah.veera.statroid.data.Constants.DBConstants.TIME;
import static subbiah.veera.statroid.data.Constants.NET;
import static subbiah.veera.statroid.data.Constants.RAM;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Metrics extends Fragment implements Runnable {

    private static final String TAG = "Metrics";
    private String instrument;
    private double[] data;
    private long[] time;
    private volatile boolean stopRunning;
    private Thread runningThread;

    @Nullable
    private DBHelper db;
    private WebView webView;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instrument = getArguments().getString("instrument", "");

        switch (instrument) {
            case RAM:
                return inflater.inflate(R.layout.ram_metrics, container, false);
            case NET:
                return inflater.inflate(R.layout.ram_metrics, container, false);
            default:
                return null;
        }
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switch (instrument) {
            case RAM:
                webView = (WebView) getActivity().findViewById(R.id.ram_webview);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadUrl("file:///android_asset/ram.html");
                break;
            case NET:
                webView = (WebView) getActivity().findViewById(R.id.ram_webview);
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadUrl("file:///android_asset/ram.html");
                break;
            default:
                break;
        }
        runningThread = runningThread == null ? new Thread(this, "ui_" + instrument) : runningThread;
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            stopRunning = true;
            runningThread.interrupt();
            runningThread.join();
        } catch (InterruptedException ignored) {} finally {
            if(isRemoving()) {
                kill();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        stopRunning = false;
        create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        kill();
    }







    private void ramGraph(float free, float tot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(String.format(Locale.US, "window.free = %f; window.tot = %f; window.redraw()", free, tot), null);
        } else {
            webView.loadUrl(String.format(Locale.US, "javascript:window.free = %f; window.tot = %f; window.redraw()", free, tot));
        }
    }

    private void netGraph(float free, float tot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(String.format(Locale.US, "window.free = %f; window.tot = %f; window.redraw()", free, tot), null);
        } else {
            webView.loadUrl(String.format(Locale.US, "javascript:window.free = %f; window.tot = %f; window.redraw()", free, tot));
        }
    }

    private void readFromDB() {
        Cursor cursor = null;
        String[] projection = new String[2];
        if(instrument.equals(Constants.NET)) {
            projection[0] = TIME;
            projection[1] = NET;
        }

        if (db != null && !instrument.equals(RAM))
            cursor = db.read(projection, TIME + " > ?", new String[]{"" + (new Date().getTime() - 1000 * 60 * 60)}, TIME);


        if(cursor != null) {
            long[] time = new long[cursor.getCount()];
            double[] data = new double[cursor.getCount()];

            for(int i=0; cursor.moveToNext(); i++) {
                time[i] = cursor.getLong(0);
                data[i] = cursor.getDouble(1);
            }

            setData(time, data);
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawGraph();
            }
        });
    }

    @Override
    public void run() {
        int INTERVAL = new Constants.INTERVAL(instrument).INTERVAL;
        while(!stopRunning) {
            try {
                readFromDB();
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                Logger.d(TAG, "run: Interrupted from sleep " + Thread.currentThread().getName());
            }
        }
    }





    private void setData(long[] time, double[] data) {
        this.data = data;
        this.time = time;
    }

    private void drawGraph() {
        switch (instrument) {
            case RAM:
                ramGraph((float) Data.init().getRam(), (float) Data.init().getTotalRam());
                break;
            case NET:
                netGraph((float) Data.init().getRam(), (float) Data.init().getTotalRam());
                break;
            default:
                break;
        }
    }

    private void kill() {
        runningThread = null;
        db = null;
        if(webView != null) {
            webView.destroy();
            webView = null;
        }
    }

    private void create() {
        db = DBHelper.init(getContext(), READ);

        if(runningThread.isAlive()) runningThread.interrupt();
        else runningThread.start();
    }
}
