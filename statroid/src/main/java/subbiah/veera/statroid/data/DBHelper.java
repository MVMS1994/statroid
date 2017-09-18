package subbiah.veera.statroid.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static subbiah.veera.statroid.data.Constants.DBConstants.DATABASE_NAME;
import static subbiah.veera.statroid.data.Constants.DBConstants.DATABASE_VERSION;
import static subbiah.veera.statroid.data.Constants.DBConstants.READ;
import static subbiah.veera.statroid.data.Constants.DBConstants.SQL_CREATE_ENTRIES;
import static subbiah.veera.statroid.data.Constants.DBConstants.SQL_DELETE_ENTRIES;
import static subbiah.veera.statroid.data.Constants.DBConstants.TABLE_NAME;
import static subbiah.veera.statroid.data.Constants.DBConstants.WRITE;

/**
 * Created by Veera.Subbiah on 17/09/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";
    private final SQLiteDatabase db;
    private static DBHelper writeInstance;
    private static DBHelper readInstance;

    public static synchronized DBHelper init(Context context, int MODE) {
        if(MODE == READ && readInstance != null)
            return readInstance;
        else if(MODE == WRITE && writeInstance != null)
            return writeInstance;
        return new DBHelper(context, MODE);
    }

    private DBHelper(Context context, int MODE) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        if (MODE == READ) {
            db = getReadableDatabase();
            readInstance = this;
        } else {
            db = getWritableDatabase();
            writeInstance = this;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public Cursor read(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if(db.isOpen())
            return db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
        else
            return null;
    }

    public long write(String[] projection, double[] values) {
        if(projection.length != values.length) return -1;

        ContentValues record = new ContentValues();
        for (int i = 0; i < projection.length; i++) {
            record.put(projection[i], values[i]);
        }
        return db.insert(TABLE_NAME, null, record);
    }

    public int remove(String selection, String[] selectionArgs) {
        return db.delete(TABLE_NAME, selection, selectionArgs);
    }

    public int update(String[] projection, String[] values, String selection, String[] selectionArgs) {
        if(projection.length != values.length) return -1;

        ContentValues record = new ContentValues();
        for (int i = 0; i < projection.length; i++) {
            record.put(projection[i], values[i]);
        }

        return db.update(TABLE_NAME, record, selection, selectionArgs);
    }

    public void reset(int MODE) {
        Logger.d(TAG, "reset: closing db");
        if(MODE == READ) {
            readInstance = null;
        } else {
            writeInstance = null;
        }
        db.close();
    }

    public static boolean isClosed(int MODE) {
        if(MODE == READ) {
            return readInstance == null || readInstance.db == null || !readInstance.db.isOpen();
        } else {
            return writeInstance == null || writeInstance.db == null || !writeInstance.db.isOpen();
        }
    }
}
