package subbiah.veera.statroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import subbiah.veera.statroid.core.StatsService;
import subbiah.veera.statroid.core.SystemUtils;

public class MainActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        String mac = SystemUtils.getMacAddr();
        String adb = "adb connect " +
                SystemUtils.getIpAddr(this) + ":" +
                SystemUtils.runADB("getprop service.adb.tcp.port");

        ((TextView) findViewById(R.id.answer)).setText(mac + "\n" + adb);
        Intent service = new Intent(this, StatsService.class);
        startService(service);

        super.onStart();
    }
}
