package subbiah.veera.statroid.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import subbiah.veera.statroid.R;
import subbiah.veera.statroid.core.SystemUtils;
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
    private double[] data = new double[0];
    private long[] time = new long[0];
    private volatile boolean stopRunning;
    private Thread runningThread;

    @Nullable
    private DBHelper db;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instrument = getArguments().getString("instrument", "");

        switch (instrument) {
            case RAM:
                return inflater.inflate(R.layout.ram_metrics, container, false);
            case NET:
                return inflater.inflate(R.layout.net_metrics, container, false);
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
                this.view = getActivity().findViewById(R.id.ram_graph);
                ((WebView) this.view).getSettings().setJavaScriptEnabled(true);
                ((WebView) this.view).getSettings().setAppCacheEnabled(true);
                ((WebView) this.view).getSettings().setAppCachePath(getContext().getCacheDir().getPath());
                ((WebView) this.view).getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                ((WebView) this.view).loadUrl("file:///android_asset/ram.html");
                break;
            case NET:
                this.view = getActivity().findViewById(R.id.net_graph);
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
            ((WebView) view).evaluateJavascript(String.format(Locale.US, "window.free = %f; window.tot = %f; if(typeof window.redraw == \"function\") window.redraw()", free, tot), null);
        } else {
            ((WebView) view).loadUrl(String.format(Locale.US, "javascript:window.free = %f; window.tot = %f; if(typeof window.redraw == \"function\") window.redraw()", free, tot));
        }
    }

    private void netGraph() {
        if(time.length == 0) return;
        HashMap<String, Float> netReport = new HashMap<>();

        for(int i = 0; i < time.length; i++) {
            @SuppressLint("SimpleDateFormat")
            String day = new SimpleDateFormat("yyyy-MM-dd").format(new Date(time[i]));
            float curr = netReport.containsKey(day) ? netReport.get(day) : 0;

            netReport.put(day, curr + ((float)data[i] * 60));
        }

        TableLayout.LayoutParams tableProps = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowProps = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);

        TableLayout tableLayout = (TableLayout) view;
        tableLayout.removeAllViews();

        TableRow tableHead = new TableRow(this.getContext());
        tableHead.setLayoutParams(tableProps);

        TextView day = new TextView(this.getContext());
        day.setText(this.getString(R.string.day));
        day.setGravity(Gravity.CENTER);
        day.setPadding(5, 5, 5, 5);
        day.setLayoutParams(rowProps);
        tableHead.addView(day);

        TextView network_used = new TextView(this.getContext());
        network_used.setText(this.getString(R.string.network_used));
        network_used.setGravity(Gravity.CENTER);
        network_used.setPadding(5, 5, 5, 5);
        network_used.setLayoutParams(rowProps);
        tableHead.addView(network_used);

        tableLayout.addView(tableHead);

        TableRow[] tableRows = new TableRow[netReport.size()];
        TextView[] key = new TextView[netReport.size()];
        TextView[] val = new TextView[netReport.size()];

        Set<String> keys = netReport.keySet();

        int i = 0;
        for(String k: keys) {
            tableRows[i] = new TableRow(this.getContext());
            tableRows[i].setLayoutParams(tableProps);

            key[i] = new TextView(this.getContext());
            key[i].setText(k);
            key[i].setTextColor(Color.BLACK);
            key[i].setPadding(5, 5, 5, 5);
            key[i].setGravity(Gravity.CENTER);
            key[i].setLayoutParams(rowProps);
            tableRows[i].addView(key[i]);

            val[i] = new TextView(this.getContext());
            val[i].setText(SystemUtils.convertToSuitableNetworkUnit(netReport.get(k)).replace("/s",""));
            val[i].setTextColor(Color.BLACK);
            val[i].setPadding(5, 5, 5, 5);
            val[i].setGravity(Gravity.CENTER);
            val[i].setLayoutParams(rowProps);
            tableRows[i].addView(val[i]);

            tableLayout.addView(tableRows[i]);
            i++;
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
            cursor = db.read(projection, TIME + " > ?", new String[]{"" + (new Date().getTime() - 1000 * 60 * 60 * 24 * 7)}, TIME);


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
                netGraph();
                break;
            default:
                break;
        }
    }

    private void kill() {
        runningThread = null;
        db = null;
        if(view != null && view instanceof WebView) {
            ((WebView) view).destroy();
            view = null;
        }
    }

    private void create() {
        db = DBHelper.init(getContext(), READ);

        if(runningThread.isAlive()) runningThread.interrupt();
        else runningThread.start();
    }
}
