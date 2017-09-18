package subbiah.veera.statroid.ui;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
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
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
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
    private volatile boolean stopRunning = false;
    @Nullable DBHelper db;

    public Metrics() {
    }

    @SuppressLint("ValidFragment")
    private Metrics(Parcel in) {
        super();
        instrument = in.readString();
        yData = in.createDoubleArray();
        timeInterval = in.createLongArray();
    }

    public static final Creator<Metrics> CREATOR = new Creator<Metrics>() {
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stopRunning = false;
        new Thread(this).start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isRemoving()) {
            stopRunning = true;
        }
    }

    @Nullable
    private DataSet drawGraph() {
        Chart chart = null;
        DataSet dataSet = null;

        if (instrument.equals(Constants.CPU)) {
            chart = (LineChart) getActivity().findViewById(R.id.cpu_chart);
            dataSet = initCPUGraph((LineChart) chart, timeInterval, yData);
        } else if (instrument.equals(RAM)) {
            chart = (PieChart) getActivity().findViewById(R.id.ram_chart);
            dataSet = initRAMGraph((PieChart) chart, getData());
        }

        if (chart != null)
            drawChart(chart);

        return dataSet;
    }

    private Object[] getData() {
        switch (instrument) {
            case RAM:
                return new Float[]{Float.valueOf(80)};
            default:
                return new Float[]{Float.valueOf(80), Float.valueOf(20)};
        }
    }

    @Nullable
    private LineDataSet initCPUGraph(final LineChart chart, long[] xData, double[] yData) {
        if (xData.length != yData.length || yData.length == 0) return null;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < xData.length; i++) {
            //noinspection deprecation
            Logger.d(TAG, "initCPUGraph:" + new Date(xData[i]).getMinutes());
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
        final Description desc = new Description();
//        desc.setPosition(chart.getViewPortHandler().contentRight() - 10, chart.getViewPortHandler().contentTop() - 10);
        desc.setText("");


        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setAxisLineWidth(1.5f);
        x.setDrawGridLines(false);
        x.setTextSize(12);
        x.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return getDate((long) value);
            }
        });

        final YAxis left = chart.getAxisLeft();
        left.setAxisMinimum(0);
        left.setAxisMaximum(100);
        left.setAxisLineWidth(1.5f);
        left.setTextSize(12);

        YAxis right = chart.getAxisRight();
        right.setDrawLabels(false);
        right.setDrawGridLines(false);

        chart.setData(lineData);
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

        return dataSet;
    }

    private PieDataSet initRAMGraph(PieChart chart, Object[] dataObjects) {

        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((Float) dataObjects[0], "Used RAM"));
        entries.add(new PieEntry(100 - (Float) dataObjects[0], "Free RAM"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ContextCompat.getColor(getContext(), R.color.colorPrimaryLight), ContextCompat.getColor(getContext(), R.color.colorAccentDark));
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(12);
        dataSet.setSliceSpace(2);

        chart.setData(new PieData(dataSet));
        chart.setRotationEnabled(false);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setDrawCenterText(true);
        chart.setExtraOffsets(10, 10, 10, 10);
        chart.setCenterText("RAM Usage history");

        return dataSet;
    }

    private void drawChart(Chart chart) {
        chart.invalidate();
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
        if(db == null)
            db = DBHelper.init(getContext(), READ);

        new AsyncTask<Void, Void, Cursor>() {

            @Nullable
            @Override
            protected Cursor doInBackground(Void... params) {
                String[] projection = new String[2];
                if(instrument.equals(Constants.CPU)) {
                    projection[0] = TIME;
                    projection[1] = CPU;
                } else if(instrument.equals(Constants.NET)) {
                    projection[0] = TIME;
                    projection[1] = NET;
                }

                if (db != null && !instrument.equals(RAM))
                    return db.read(projection, TIME + " > ?", new String[]{"" + (new Date().getTime() - 1000 * 60 * 60)}, TIME);

                return null;
            }


            @Override
            protected void onPostExecute(@Nullable Cursor cursor) {
                super.onPostExecute(cursor);
                if(cursor != null) {
                    long[] time = new long[cursor.getCount()];
                    double[] yData = new double[cursor.getCount()];

                    for(int i=0; cursor.moveToNext(); i++) {
                        time[i] = cursor.getLong(0);
                        yData[i] = cursor.getDouble(1);
                    }

                    setData(time, yData);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawGraph();
                        }
                    });
                }
                if(instrument.equals(Constants.RAM)) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawGraph();
                        }
                    });
                }
            }
        }.execute();

    }

    @Override
    public void run() {
        while(!stopRunning) {
            try {
                readFromDB();
                Thread.sleep(60 * 1000);
            } catch (InterruptedException e) {
                Logger.e(TAG, "run: ", e);
            }
        }
    }
}
