package subbiah.veera.statroid.data;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class Constants {
    public static final String CPU = "cpu";
    public static final String REALTIME = "realtime";
    public static final String RAM = "ram";
    public static final String NET = "net_rate";
    public static final String UPLOAD_NET = "upload_net";
    public static final String DOWNLOAD_NET = "download_net";
    public static final int THREAD_SLEEP = 2000;

    public class DBConstants {
        public static final int READ = 0;
        public static final int WRITE = 1;

        static final int DATABASE_VERSION = 2;
        static final String DATABASE_NAME = "Statroid.db";

        static final String TABLE_NAME = "status";

        public static final String _ID = "id";
        public static final String TIME = "time";
        public static final String NET = Constants.NET;
        public static final String CPU = Constants.CPU;


        static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        TIME + " NUMERIC," +
                        NET + " NUMERIC," +
                        UPLOAD_NET + " NUMERIC," +
                        DOWNLOAD_NET + " NUMERIC," +
                        CPU + " NUMERIC" +
                ")";

        static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class INTERVAL {
        public final int INTERVAL;
        public INTERVAL(String instrument) {
            if(instrument.equals(REALTIME)) {
                INTERVAL = 1000;
            } else {
                INTERVAL = 1000 * 60;
            }
        }
    }
}
