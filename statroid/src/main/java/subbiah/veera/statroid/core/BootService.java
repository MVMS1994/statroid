package subbiah.veera.statroid.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Veera.Subbiah on 05/09/17.
 */

public class BootService extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, StatsService.class);
        context.startService(service);
    }
}
