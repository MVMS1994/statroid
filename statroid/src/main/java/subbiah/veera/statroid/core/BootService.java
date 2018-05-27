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
        if(intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent service = new Intent(context, StatsService.class);
            context.startService(service);
        }
    }
}
