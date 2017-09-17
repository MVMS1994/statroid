package subbiah.veera.statroid.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class ViewPageAdapter extends FragmentPagerAdapter {

    private final FragmentManager fm;
    private ArrayList<Metrics> topics = new ArrayList<>(3);
    private HashMap<String, Metrics> fragments = new HashMap<>();

    private static ViewPageAdapter _instance;

    public ViewPageAdapter(FragmentManager fm) {
        super(fm);
        this.fm = fm;
    }

    @Override
    public Fragment getItem(int position) {
        if(topics != null) {
            return topics.get(position);
        } else {
            return null;
        }
    }

    public void addFragment(Metrics topic, String instrument) {
        if (!fragments.containsKey(instrument)) {
            Bundle params = new Bundle();
            params.putString("instrument", instrument);


            topic.setArguments(params);
            fragments.put(instrument, topic);
            topics.add(topic);
        }
    }

    @Override
    public int getCount() {
        return fragments.values().size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    public Set<String> getInstruments() {
        return fragments.keySet();
    }

    public void addDataToFragment(long[] time, double[] yData, String instrument) {
        if (fragments.containsKey(instrument)) {
            Metrics fragment = fragments.get(instrument);
            fragment.setData(time, yData);
        }
    }

    public void reset() {
        for(Metrics fragment: topics) {
            FragmentTransaction trans = fm.beginTransaction();
            trans.remove(fragment);
            trans.commitAllowingStateLoss();
        }
        fragments.clear();
        topics.clear();
    }

    public ArrayList<Metrics> exportList() {
        return topics;
    }

    public void importList(ArrayList<Metrics> fragments) {
        topics = fragments;
        for (Metrics fragment: fragments) {
            if(!this.fragments.containsKey(fragment.getInstrument())) {
                this.fragments.put(fragment.getInstrument(), fragment);
            }
        }
    }
}
