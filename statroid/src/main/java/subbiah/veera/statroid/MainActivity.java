package subbiah.veera.statroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import subbiah.veera.statroid.core.StatsService;
import subbiah.veera.statroid.core.SystemUtils;
import subbiah.veera.statroid.data.Constants;
import subbiah.veera.statroid.data.DBHelper;
import subbiah.veera.statroid.data.Logger;
import subbiah.veera.statroid.ui.Metrics;
import subbiah.veera.statroid.ui.ViewPageAdapter;

import static subbiah.veera.statroid.data.Constants.DBConstants.READ;

public class MainActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";
    private ViewPageAdapter viewPageAdapter;
    private ViewPager viewPager;
    @Nullable private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Logger.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        Statroid.setActivityAlive(true);
        ((Statroid) getApplication()).setCurrentActivity(this);
        db = DBHelper.init(this, READ);

        CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();
        Fabric.with(this, new Crashlytics.Builder().core(core).build());

        setContentView(R.layout.activity_main);

        initToolbar();
        initPageViewer(savedInstanceState);
        initTabLayout();
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

        if(isFinishing()) {
            Statroid.setActivityAlive(false);
            ((Statroid) getApplication()).setCurrentActivity(null);
            if (db != null) {
                db.reset(READ);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("retainedPages", viewPageAdapter.exportList());
        super.onSaveInstanceState(outState);
    }

    @SuppressWarnings("UnusedParameters")
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

    private void setupViewPager() {
        //viewPageAdapter.addFragment(new Metrics(), Constants.CPU);
        viewPageAdapter.addFragment(new Metrics(), Constants.RAM);
        viewPageAdapter.addFragment(new Metrics(), Constants.NET);
    }

    @SuppressWarnings("ConstantConditions")
    private void setupTabIcons(TabLayout tabLayout) {
        //tabLayout.getTabAt(0).setIcon(R.drawable.ic_desktop_mac_black_24dp);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_developer_board_black_24dp);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_network_check_black_24dp);
    }

    private void initTabLayout() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons(tabLayout);
    }

    @SuppressWarnings("unchecked")
    private void initPageViewer(Bundle savedInstanceState) {
        viewPager = (ViewPager) findViewById(R.id.content);
        viewPageAdapter = new ViewPageAdapter(getSupportFragmentManager());

        if (savedInstanceState != null) {
            ArrayList<Metrics> retainedPages = (ArrayList<Metrics>) savedInstanceState.getSerializable("retainedPages");
            viewPageAdapter.importList(retainedPages);
        } else {
            setupViewPager();
        }

        viewPager.setAdapter(viewPageAdapter);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, android.R.color.white));
        setSupportActionBar(toolbar);
    }
}


