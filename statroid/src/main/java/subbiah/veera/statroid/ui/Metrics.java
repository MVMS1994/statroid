package subbiah.veera.statroid.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import subbiah.veera.statroid.MainActivity;
import subbiah.veera.statroid.R;
import subbiah.veera.statroid.data.Constants;
import subbiah.veera.statroid.data.Logger;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Metrics extends Fragment {

    private static final String TAG = "Metrics";
    private String instrument;
    private double[] yData = new double[0];
    private long[] timeInterval = new long[0];
    private Activity activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instrument = getArguments().getString("instrument", "");

        switch (instrument) {
            case Constants.RAM:
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
        Logger.d(TAG, "onActivityCreated() called with: savedInstanceState = [" + savedInstanceState + "]");

        fetchData();
    }

    private void fetchData() {
        if(activity != null) {
            ((MainActivity) activity).fetchData();
        } else {
            Logger.d(TAG, "fetchData: Activity null ");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Logger.d(TAG, "onAttach() called with: context = [" + context + "]");

        activity = (Activity) context;
    }

    @Nullable
    private DataSet drawGraph() {
        Chart chart = null;
        DataSet dataSet = null;

        if (instrument.equals(Constants.CPU)) {
            chart = (LineChart) getActivity().findViewById(R.id.cpu_chart);
            dataSet = initCPUGraph((LineChart) chart, timeInterval, yData);
        } else if (instrument.equals(Constants.RAM)) {
            chart = (PieChart) getActivity().findViewById(R.id.ram_chart);
            dataSet = initRAMGraph((PieChart) chart, getData());
        }

        if (chart != null)
            drawChart(chart);

        return dataSet;
    }

    private Object[] getData() {
        switch (instrument) {
            case Constants.RAM:
                return new Float[]{Float.valueOf(80)};
            default:
                return new Float[]{Float.valueOf(80), Float.valueOf(20)};
        }
    }

    private LineDataSet initCPUGraph(final LineChart chart, long[] xData, double[] yData) {
        if (xData.length != yData.length || yData.length == 0) return null;

        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < xData.length; i++) {
            Logger.d(TAG, "initCPUGraph:" + new Date(xData[i]).getMinutes());
            entries.add(new Entry(xData[i], (float) yData[i]));
        }

        LineDataSet dataSet = new LineDataSet(entries, "CPU Usage history");
        dataSet.setColor(getResources().getColor(R.color.colorAccentDark));
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true);
        dataSet.setCircleColor(getResources().getColor(R.color.colorPrimaryLight));
        dataSet.setFillColor(getResources().getColor(R.color.colorAccentLight));
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
        dataSet.setColors(getResources().getColor(R.color.colorPrimaryLight), getResources().getColor(R.color.colorAccentDark));
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

        if(activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    drawGraph();
                }
            });
        } else {
            Logger.d(TAG, "setData: activity null" );
        }
    }

    private static String getDate(long value) {
        Date date = new Date(value);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm", Locale.US);
        return simpleDateFormat.format(date);
    }
}
