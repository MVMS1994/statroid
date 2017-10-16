package subbiah.veera.statroid.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.util.DisplayMetrics;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.List;

import subbiah.veera.statroid.data.Logger;

/**
 * Created by Veera.Subbiah on 04/09/17.
 */

@SuppressWarnings("unused")
public class SystemUtils {
    private final static String TAG = "SystemUtils";

    @SuppressLint("SetTextI18n")
    public static String getMacAddr() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return null;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    //res1.append(Integer.toHexString(b & 0xFF) + ":");
                    res1.append(String.format("%02X:",b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }

                return res1.toString();
            }
        } catch (Exception ex) {
            Logger.e(TAG, "This Happened: ", ex);
        }
        return null;
    }

    public static String runADB(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();

            String line, answer = "";
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = in.readLine()) != null) {
                answer += line;
            }
            in.close();

            return answer;
        } catch (Exception e) {
            Logger.e(TAG, "This Happened: ", e);
        }
        return "";
    }

    public static String getIpAddr(Activity activity) {
        try {
            WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(wifiManager.getConnectionInfo().getIpAddress());

            return InetAddress.getByAddress(null, byteBuffer.array()).getHostAddress();
        } catch (Exception e) {
            Logger.e(TAG, "This Happened: ", e);
        }
        return "";
    }

    @SuppressWarnings({"WeakerAccess", "SameParameterValue"})
    public static int dpToPx(int dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static String convertToSuitableNetworkUnit(double net) {
        final long KB = 1024, MB = KB * 1024, GB = MB * 1024;
        String unit = " B/s";
        if (net >= GB) {
            net /= GB;
            unit = " GB/s";
        } else if (net >= MB) {
            net /= MB;
            unit = " MB/s";
        } else if (net >= KB) {
            net /= KB;
            unit = " KB/s";
        }

        return round(net, 1) + unit;
    }

    public static double round(double val, int places) {
        return new BigDecimal(val).setScale(places, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }
}
