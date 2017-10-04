package subbiah.veera.statroid.ui;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import subbiah.veera.statroid.R;
import subbiah.veera.statroid.data.Constants;
import subbiah.veera.statroid.data.DBHelper;
import subbiah.veera.statroid.data.Data;
import subbiah.veera.statroid.data.Logger;

import static subbiah.veera.statroid.data.Constants.DBConstants.CPU;
import static subbiah.veera.statroid.data.Constants.DBConstants.READ;
import static subbiah.veera.statroid.data.Constants.DBConstants.TIME;
import static subbiah.veera.statroid.data.Constants.NET;
import static subbiah.veera.statroid.data.Constants.RAM;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Metrics extends Fragment implements Parcelable, Runnable {

    private static final String TAG = "Metrics";
    private String instrument;
    private double[] yData = new double[0];
    private long[] timeInterval = new long[0];
    private volatile boolean stopRunning = true;
    private Thread runningThread;
    @Nullable
    private DBHelper db;
    private boolean chartDrawn = false;
    private Chart chart;

    public Metrics() {
    }

    @SuppressLint("ValidFragment")
    private Metrics(Parcel in) {
        super();
        instrument = in.readString();
        yData = in.createDoubleArray();
        timeInterval = in.createLongArray();
    }

    public static Creator<Metrics> CREATOR = new Creator<Metrics>() {
        @Override
        public Metrics createFromParcel(Parcel in) {
            return new Metrics(in);
        }

        @Override
        public Metrics[] newArray(int size) {
            return new Metrics[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(instrument);
        dest.writeDoubleArray(yData);
        dest.writeLongArray(timeInterval);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instrument = getArguments().getString("instrument", "");

        switch (instrument) {
            case RAM:
                return inflater.inflate(R.layout.ram_metrics, container, false);
            case Constants.CPU:
                return inflater.inflate(R.layout.cpu_metrics, container, false);
            default:
                return inflater.inflate(R.layout.ram_metrics, container, false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            stopRunning = true;
            chartDrawn = false;
            runningThread.interrupt();
            runningThread.join();
        } catch (InterruptedException ignored) {} finally {
            if(isRemoving()) {
                runningThread = null;
                db = null;
                CREATOR = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        runningThread = null;
        db = null;
        CREATOR = null;
    }

    @Override
    public void onResume() {
        if (instrument.equals(Constants.CPU)) {
            chart = (LineChart) getActivity().findViewById(R.id.cpu_chart);
        } else if (instrument.equals(RAM)) {
            chart = (PieChart) getActivity().findViewById(R.id.ram_chart);
        }

        super.onResume();
        db = DBHelper.init(getContext(), READ);
        stopRunning = false;
        if(runningThread == null) {
            runningThread = new Thread(this, "ui_" + instrument);
        }
        if(runningThread.getState() == Thread.State.TIMED_WAITING) {
            runningThread.interrupt();
        } else if(!runningThread.isAlive()) {
            runningThread.start();
        }
    }

    private void drawGraph() {
        if (chart != null) {
            if (instrument.equals(Constants.CPU)) {
                initCPUGraph((LineChart) chart, timeInterval, yData);
            } else if (instrument.equals(RAM)) {
                initRAMGraph((PieChart) chart, (float) Data.init().getRam(), (float) Data.init().getTotalRam());
            }
            drawChart(chart);
        }
    }

    private void initCPUGraph(final LineChart chart, long[] xData, double[] yData) {
        if (xData.length != yData.length || yData.length == 0) return;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < xData.length; i++) {
            entries.add(new Entry(xData[i], (float) yData[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "CPU Usage history");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.colorAccentDark));
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setCircleColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight));
        dataSet.setFillColor(ContextCompat.getColor(getContext(), R.color.colorAccentLight));
        dataSet.setLineWidth(2f);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);

        if(!chartDrawn) {
            final Description desc = new Description();
            // desc.setPosition(chart.getViewPortHandler().contentRight() - 10, chart.getViewPortHandler().contentTop() - 10);
            desc.setText("");


            XAxis x = chart.getXAxis();
            x.setPosition(XAxis.XAxisPosition.BOTTOM);
            x.setAxisLineWidth(1.5f);
            x.setDrawGridLines(false);
            x.setTextSize(12);
            x.setValueFormatter((value, axis) -> getDate((long) value));

            final YAxis left = chart.getAxisLeft();
            left.setAxisMinimum(0);
            left.setAxisMaximum(100);
            left.setAxisLineWidth(1.5f);
            left.setTextSize(12);

            YAxis right = chart.getAxisRight();
            right.setDrawLabels(false);
            right.setDrawGridLines(false);

            chart.setScaleYEnabled(false);
            chart.setPinchZoom(false);
            chart.setDoubleTapToZoomEnabled(false);
            chart.setDescription(desc);
            chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                @Override
                public void onValueSelected(Entry e, Highlight h) {
                    desc.setText("CPU - " + e.getY() + "; Time - " + getDate((long) e.getX()));
                    chart.setDescription(desc);
                }

                @Override
                public void onNothingSelected() {
                    desc.setText("");
                    chart.setDescription(desc);
                }
            });
        }
    }

    private void initRAMGraph(PieChart chart, float free, float tot) {

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(tot - free, "Used RAM"));
        entries.add(new PieEntry(free, "Free RAM"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight), ContextCompat.getColor(getContext(), R.color.colorAccentDark));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12);
        dataSet.setSliceSpace(2);


        chart.setData(new PieData(dataSet));

        if(!chartDrawn) {
            chart.setRotationEnabled(false);
            chart.setDrawHoleEnabled(true);
            chart.setHoleColor(Color.WHITE);
            chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
            chart.getLegend().setEnabled(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
            chart.getDescription().setEnabled(false);
            chart.setDrawCenterText(true);
            chart.setExtraOffsets(10, 10, 10, 10);
            chart.setCenterText("RAM Usage");
        }
    }

    private void drawChart(Chart chart) {
        chart.invalidate();
        chartDrawn = true;
    }

    public void setData(long[] time, double[] yData) {

        this.yData = yData;
        this.timeInterval = time;
    }

    private static String getDate(long value) {
        Date date = new Date(value);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm", Locale.US);
        return simpleDateFormat.format(date);
    }

    public String getInstrument() {
        return instrument;
    }

    private void readFromDB() {
        Cursor cursor = null;
        String[] projection = new String[2];
        if(instrument.equals(Constants.CPU)) {
            projection[0] = TIME;
            projection[1] = CPU;
        } else if(instrument.equals(Constants.NET)) {
            projection[0] = TIME;
            projection[1] = NET;
        }

        if (db != null && !instrument.equals(RAM))
            cursor = db.read(projection, TIME + " > ?", new String[]{"" + (new Date().getTime() - 1000 * 60 * 60)}, TIME);


        if(cursor != null) {
            long[] time = new long[cursor.getCount()];
            double[] yData = new double[cursor.getCount()];

            for(int i=0; cursor.moveToNext(); i++) {
                time[i] = cursor.getLong(0);
                yData[i] = cursor.getDouble(1);
            }

            setData(time, yData);
            getActivity().runOnUiThread(this::drawGraph);
        }

        if(instrument.equals(Constants.RAM)) {
            getActivity().runOnUiThread(this::drawGraph);
        }
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
}
