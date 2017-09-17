package subbiah.veera.statroid.data;

/**
 * Created by Veera.Subbiah on 16/09/17.
 */

public class Constants {
    public static final String CPU = "CPU";
    public static final String RAM = "RAM";
    public static final String NET = "NET";

    public class ServiceConstants {
        public static final String UPDATE_GRAPH = "update";
    }

    public class DBConstants {
        static final int DATABASE_VERSION = 1;
        static final String DATABASE_NAME = "Statroid.db";

        static final String TABLE_NAME = "status";

        public static final String _ID = "id";
        public static final String TIME = "time";
        public static final String NET = "net_rate";
        public static final String CPU = "cpu";


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
}
