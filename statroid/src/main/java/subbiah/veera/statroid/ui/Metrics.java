package subbiah.veera.statroid.ui;

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

import java.util.ArrayList;
import java.util.List;

import subbiah.veera.statroid.R;
import subbiah.veera.statroid.data.Constants;
import subbiah.veera.statroid.data.Logger;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Metrics extends Fragment {

    private static final String TAG = "Metrics";
    private String instrument;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        instrument = getArguments().getString("instrument", "");
        Logger.d(TAG, instrument);

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
        drawGraph();
    }

    @Nullable
    private DataSet drawGraph() {
        Chart chart = null;
        DataSet dataSet = null;
        if (instrument.equals(Constants.CPU)) {
            chart = (LineChart) getActivity().findViewById(R.id.cpu_chart);
            dataSet = initCPUGraph((LineChart) chart, getData());
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
            case Constants.CPU:
                return new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
            case Constants.RAM:
                return new Float[] {Float.valueOf(80)};
            default:
                return new Float[] {Float.valueOf(80), Float.valueOf(20)};
        }
    }

    private LineDataSet initCPUGraph(LineChart chart, Object[] dataObjects) {
        List<Entry> entries = new ArrayList<>();

        for (int i = dataObjects.length - 1; i >= 0; i--) {
            entries.add(new Entry(-1 * (Integer) dataObjects[i], (Integer) dataObjects[i] * (Integer) dataObjects[i]));
        }

        for (int data : (Integer[]) dataObjects) {
            entries.add(new Entry(data, data * data));
        }


        LineDataSet dataSet = new LineDataSet(entries, "CPU Usage history");
        dataSet.setColor(getResources().getColor(R.color.colorAccentDark));
        dataSet.setValueTextColor(getResources().getColor(R.color.colorPrimaryLight));
        dataSet.setValueTextSize(9);

        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.setScaleYEnabled(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.getDescription().setEnabled(false);

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setAxisLineWidth(2f);
        x.setTextSize(12);
        x.setLabelCount(7);

        YAxis left = chart.getAxisLeft();
        left.setAxisMinimum(0);
        left.setAxisMaximum(100);
        left.setAxisLineWidth(2f);
        left.setTextSize(12);

        YAxis right = chart.getAxisRight();
        right.setDrawLabels(false);
        right.setDrawGridLines(false);

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
        chart.setCenterText("RAM Usage history");

        return dataSet;
    }

    private void drawChart(Chart chart) {
        chart.invalidate();
    }
}
