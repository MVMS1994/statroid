package subbiah.veera.statroid.data;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

import subbiah.veera.statroid.R;

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
        if (instrument.equals(Constants.RAM)) {
            return inflater.inflate(R.layout.ram_metrics, container, false);
        } else {
            return inflater.inflate(R.layout.cpu_metrics, container, false);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        drawGraph();
    }

    private void drawGraph() {
        if(instrument.equals(Constants.RAM)) {
            LineChart chart = (LineChart) getActivity().findViewById(R.id.ram_chart);

            int[] dataObjects = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
            List<Entry> entries = new ArrayList<>();

            for (int data : dataObjects) {
                entries.add(new Entry(data, data * 2));
            }

            LineDataSet dataSet = new LineDataSet(entries, "Test");
            dataSet.setColor(getResources().getColor(R.color.colorAccent));
            dataSet.setValueTextColor(R.color.colorPrimaryLight);

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate();
        }
    }
}
