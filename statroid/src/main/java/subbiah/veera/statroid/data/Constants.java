package subbiah.veera.statroid.data;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

@SuppressWarnings("WeakerAccess")
public class Constants {
    public static final String CPU = "cpu";
    public static final String RAM = "ram";
    public static final String NET = "net_rate";

    public class DBConstants {
        public static final int READ = 0;
        public static final int WRITE = 1;

        static final int DATABASE_VERSION = 1;
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
                        CPU + " NUMERIC" +
                ")";

        static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + "status";
    }

    public static class INTERVAL {
        public final int INTERVAL;
        public INTERVAL(String instrument) {
            if(instrument.equals(RAM)) {
                INTERVAL = 1000;
            } else {
                INTERVAL = 1000 * 60;
            }
        }
    }
}
