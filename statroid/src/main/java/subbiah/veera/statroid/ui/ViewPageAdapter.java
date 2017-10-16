package subbiah.veera.statroid.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class ViewPageAdapter extends FragmentPagerAdapter {

    private final ArrayList<Metrics> topics = new ArrayList<>(3);
    private final HashMap<String, Metrics> fragments = new HashMap<>();

    public ViewPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return topics.get(position);
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
}
