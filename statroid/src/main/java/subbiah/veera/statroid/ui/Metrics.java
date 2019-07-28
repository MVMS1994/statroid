package subbiah.veera.statroid.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

import subbiah.veera.statroid.R;
import subbiah.veera.statroid.core.SystemUtils;
import subbiah.veera.statroid.data.Constants;
import subbiah.veera.statroid.data.DBHelper;
import subbiah.veera.statroid.data.Data;
import subbiah.veera.statroid.data.Logger;

import static subbiah.veera.statroid.data.Constants.DBConstants.READ;
import static subbiah.veera.statroid.data.Constants.DBConstants.TIME;
import static subbiah.veera.statroid.data.Constants.DOWNLOAD_NET;
import static subbiah.veera.statroid.data.Constants.NET;
import static subbiah.veera.statroid.data.Constants.REALTIME;
import static subbiah.veera.statroid.data.Constants.UPLOAD_NET;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Metrics extends Fragment implements Runnable {

    private static final String TAG = "Metrics";
    private String instrument;
    private JSONArray data = new JSONArray();
    private volatile boolean stopRunning;
    private Thread runningThread;

    @Nullable
    private DBHelper db;
    private View view;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instrument = getArguments() != null ? getArguments().getString("instrument", "") : "";

        switch (instrument) {
            case REALTIME:
                return inflater.inflate(R.layout.ram_metrics, container, false);
            case NET:
                return inflater.inflate(R.layout.net_metrics, container, false);
            default:
                return null;
        }
    }

    @Override
    @SuppressLint("SetJavaScriptEnabled")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(getActivity() == null || getContext() == null) return;

        switch (instrument) {
            case REALTIME:
                this.view = getActivity().findViewById(R.id.ram_graph);
                ((WebView) this.view).getSettings().setJavaScriptEnabled(true);
                ((WebView) this.view).getSettings().setAppCacheEnabled(true);
                ((WebView) this.view).getSettings().setAppCachePath(getContext().getCacheDir().getPath());
                ((WebView) this.view).getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
                ((WebView) this.view).loadUrl("file:///android_asset/realtime.html");
                break;
            case NET:
                this.view = getActivity().findViewById(R.id.net_graph);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            stopRunning = true;
            runningThread.interrupt();
            runningThread.join();
            clearGraph();
            db = null;
        } catch (InterruptedException ignored) {} finally {
            if(isRemoving()) {
                kill();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        stopRunning = false;
        create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        kill();
    }




    private void cpuGraph(float usage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ((WebView) view).evaluateJavascript(String.format(Locale.US, "window.cpu_usage = %f; if(typeof window.cpu_redraw == \"function\") window.cpu_redraw()", usage), null);
        } else {
            ((WebView) view).loadUrl(String.format(Locale.US, "javascript:window.cpu_usage = %f; if(typeof window.cpu_redraw == \"function\") window.cpu_redraw()", usage));
        }
    }

    private void ramGraph(float free, float tot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ((WebView) view).evaluateJavascript(String.format(Locale.US, "window.ram_free = %f; window.ram_tot = %f; if(typeof window.ram_redraw == \"function\") window.ram_redraw()", free, tot), null);
        } else {
            ((WebView) view).loadUrl(String.format(Locale.US, "javascript:window.ram_free= %f; window.ram_tot = %f; if(typeof window.ram_redraw == \"function\") window.ram_redraw()", free, tot));
        }
    }

    private void netGraph() {
        if(data.length() == 0) return;
        TreeMap<String, JSONObject> netReport = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return t1.compareTo(s);
            }
        });

        for(int i = 0; i < data.length(); i++) {
            try {
                JSONObject datum = data.getJSONObject(i);
                @SuppressLint("SimpleDateFormat")
                String day = new SimpleDateFormat("yyyy-MM-dd").format(new Date(datum.getLong(TIME)));
                JSONObject curr = netReport.containsKey(day) ? netReport.get(day) : new JSONObject();

                String[] projections = getProjection(instrument);
                for(String projection: projections) {
                    if(projection.equals(TIME)) continue;
                    double prev = 0;
                    if(curr.has(projection)) {
                        prev = curr.getDouble(projection);
                    }
                    curr.put(projection, prev + (datum.getDouble(projection) * 60));
                }

                netReport.put(day, curr);
            } catch (Exception e) {
                Logger.e(TAG, "Couldn't read the value", e);
            }
        }

        TableLayout.LayoutParams tableProps = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams rowProps = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.0f);

        // ------------------ Add Header ------------------
        TableLayout tableLayout = (TableLayout) view;
        tableLayout.removeAllViews();

        TableRow tableHead = new TableRow(this.getContext());
        tableHead.setLayoutParams(tableProps);

        tableHead.addView(getTableHeader(this.getString(R.string.day), rowProps));
        tableHead.addView(getTableHeader(this.getString(R.string.network_used), rowProps));
        tableHead.addView(getTableHeader(this.getString(R.string.network_download), rowProps));
        tableHead.addView(getTableHeader(this.getString(R.string.network_upload), rowProps));

        tableLayout.addView(tableHead);


        // ------------------- Add Body -------------------
        TableRow tableRows;
        Iterator<String> keys = netReport.keySet().iterator();

        for(int i = 0; keys.hasNext(); i++) {
            String k = keys.next();
            tableRows = new TableRow(this.getContext());
            tableRows.setLayoutParams(tableProps);
            tableRows.setBackgroundColor((i % 2 == 0) ? Color.WHITE : Color.rgb(245, 245, 245));

            tableRows.addView(getTableCell(k, rowProps));

            JSONObject data = netReport.get(k);
            Iterator<String> entries = data.keys();

            while(entries.hasNext()) {
                try {
                    double entry = data.getDouble(entries.next());
                    tableRows.addView(getTableCell(
                            SystemUtils.convertToSuitableNetworkUnit(entry).replace("/s", ""),
                            rowProps
                    ));
                } catch (Exception e) {
                    Logger.e(TAG, "Couldn't read the value", e);
                }
            }
            tableLayout.addView(tableRows);
        }
    }

    private void readFromDB() {
        if(getActivity() == null) return;
        Cursor cursor;
        String[] projection;
        JSONArray data;

        projection = getProjection(instrument);
        cursor = getCursor(instrument, projection);
        data = getData(instrument, projection, cursor);
        setData(data);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                drawGraph();
            }
        });
    }

    private String[] getProjection(String instrument) {
        if(instrument.equals(Constants.NET)) {
            String[] projection = new String[4];
            projection[0] = TIME;
            projection[1] = NET;
            projection[2] = DOWNLOAD_NET;
            projection[3] = UPLOAD_NET;

            return projection;
        } else {
            return new String[]{};
        }
    }

    @Nullable
    private Cursor getCursor(String instrument, String[] projection) {
        if (db != null && !instrument.equals(REALTIME))
            return db.read(projection, TIME + " > ?", new String[]{"" + (new Date().getTime() - 1000 * 60 * 60 * 24 * 7)}, TIME);
        return null;
    }

    private JSONArray getData(String instrument, String[] projections, Cursor cursor) {
        if(cursor != null) {
            switch (instrument) {
                case NET:
                    JSONArray data = new JSONArray();

                    while(cursor.moveToNext()) {
                        JSONObject datum = new JSONObject();
                        for(int j = 0; j < projections.length; j++) {
                            try {
                                datum.put(projections[j], cursor.getLong(j));
                            } catch (Exception e) {
                                Logger.e(TAG, "Couldn't read the value", e);
                            }
                        }
                        data.put(datum);
                    }

                    return data;
                default:
                    break;
            }
        }
        return null;
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





    private void setData(JSONArray data) {
        this.data = data;
    }

    private void drawGraph() {
        switch (instrument) {
            case REALTIME:
                ramGraph((float) Data.init().getRam(), (float) Data.init().getTotalRam());
                cpuGraph((float) Data.init().getCpu());
                break;
            case NET:
                netGraph();
                break;
            default:
                break;
        }
    }

    private void clearGraph() {
        switch (instrument) {
            case REALTIME:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    ((WebView) view).evaluateJavascript("window.onDestroy()", null);
                } else {
                    ((WebView) view).loadUrl("javascript:window.onDestroy()");
                }
                break;
            default:
                break;
        }
    }

    private void kill() {
        runningThread = null;
        if(view != null && view instanceof WebView) {
            ((WebView) view).destroy();
            view = null;
        }
    }

    private void create() {
        db = DBHelper.init(getContext(), READ);
        runningThread = runningThread == null || !runningThread.isAlive() ? new Thread(this, "ui_" + instrument) : runningThread;

        if (runningThread.isAlive()) runningThread.interrupt();
        else runningThread.start();
    }

    private View getTableHeader(String text, TableRow.LayoutParams props) {
        TextView tableHeader = new TextView(this.getContext());
        tableHeader.setText(text);
        tableHeader.setPadding(5, 5, 5, 5);
        tableHeader.setLayoutParams(props);
        tableHeader.setGravity(Gravity.CENTER);

        return tableHeader;
    }

    private View getTableCell(String text, TableRow.LayoutParams props) {
        TextView cell = new TextView(this.getContext());
        cell.setText(text);
        cell.setPadding(5, 5, 5, 5);
        cell.setLayoutParams(props);
        cell.setTextColor(Color.BLACK);
        cell.setGravity(Gravity.CENTER);

        return cell;
    }
}
