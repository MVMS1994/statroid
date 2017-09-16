package subbiah.veera.statroid.data;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import subbiah.veera.statroid.R;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Metrics extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.metrics, container, false);
        TextView root = (TextView)  view.findViewById(R.id.data);
        root.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
        root.setText("have fun!");

        return view;
    }


}
