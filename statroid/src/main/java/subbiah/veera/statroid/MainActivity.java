package subbiah.veera.statroid;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import io.fabric.sdk.android.Fabric;
import subbiah.veera.statroid.core.StatsService;
import subbiah.veera.statroid.core.SystemUtils;
import subbiah.veera.statroid.data.Constants;
import subbiah.veera.statroid.data.Data;
import subbiah.veera.statroid.data.Logger;
import subbiah.veera.statroid.ui.Metrics;
import subbiah.veera.statroid.ui.ViewPageAdapter;

public class MainActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";
    @Nullable
    private ViewPageAdapter viewPageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Statroid.setActivityAlive(true);
        ((Statroid) getApplication()).setCurrentActivity(this);

        CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.content);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        viewPageAdapter = setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons(tabLayout);
    }

    @Override
    protected void onStart() {
        Statroid.setActivityVisible(true);
        Intent service = new Intent(this, StatsService.class);
        startService(service);

        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        Statroid.setActivityVisible(false);
    }

    @Override
    protected void onDestroy() {
        Statroid.setActivityAlive(false);
        ((Statroid) getApplication()).setCurrentActivity(null);

        super.onDestroy();
    }

    public void showInfo(View view) {
        String adb = "adb connect " +
                SystemUtils.getIpAddr(this) + ":" +
                SystemUtils.runADB("getprop service.adb.tcp.port");

        new AlertDialog.Builder(this)
                .setTitle(SystemUtils.getIpAddr(this))
                .setIcon(R.drawable.ic_router_black_24dp)
                .setCancelable(true)
                .setMessage(adb)
                .create()
                .show();
    }

    private ViewPageAdapter setupViewPager(ViewPager viewPager) {
        ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());
        viewPageAdapter.addFragment(new Metrics(), Constants.CPU);
        viewPageAdapter.addFragment(new Metrics(), Constants.RAM);
        viewPageAdapter.addFragment(new Metrics(), Constants.NET);

        viewPager.setAdapter(viewPageAdapter);
        return viewPageAdapter;
    }

    @SuppressWarnings("ConstantConditions")
    private void setupTabIcons(TabLayout tabLayout) {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_desktop_mac_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_developer_board_black_24dp);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_network_check_black_24dp);
    }

    public void updateData(Cursor cursor, Data data) {
        long[] time = new long[cursor.getCount()];
        double[] cpu = new double[cursor.getCount()];
        double[] net = new double[cursor.getCount()];

        for (int i = 0; cursor.moveToNext(); i++) {
            time[i] = cursor.getLong(0);
            net[i] = StatsService.round(cursor.getDouble(1), 1);
            cpu[i] = StatsService.round(cursor.getDouble(2), 1);
        }

        if (viewPageAdapter != null) {
            viewPageAdapter.addDataToFragment(time, cpu, Constants.CPU);
            viewPageAdapter.addDataToFragment(time, net, Constants.NET);
        }
    }
}


