package subbiah.veera.statroid.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class ViewPageAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> topics = new ArrayList<>();
    private List<String> instruments = new ArrayList<>();

    public ViewPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        if(topics != null) {
            return topics.get(position);
        } else {
            return null;
        }
    }

    public void addFragment(Fragment topic, String instrument) {
        Bundle params = new Bundle();
        params.putString("instrument", instrument);

        topic.setArguments(params);
        topics.add(topic);
        instruments.add(instrument);
    }

    @Override
    public int getCount() {
        return topics.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    public List<String> getInstruments() {
        return instruments;
    }
}
