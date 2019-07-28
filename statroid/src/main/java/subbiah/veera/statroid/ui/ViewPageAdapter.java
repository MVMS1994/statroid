package subbiah.veera.statroid.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class ViewPageAdapter extends FragmentPagerAdapter {

    private final ArrayList<Metrics> topics = new ArrayList<>(3);
    private final HashMap<String, Metrics> fragments = new HashMap<>();

    public ViewPageAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @Override
    @NonNull
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
