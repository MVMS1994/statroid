package subbiah.veera.statroid.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static subbiah.veera.statroid.data.Constants.DBConstants.*;

/**
 * Created by Veera.Subbiah on 17/09/17.
 */

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "DBHelper";

    public static SQLiteDatabase init(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        return dbHelper.getReadableDatabase();
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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

    public static Cursor read(String[] projection, String selection, String[] selectionArgs, String sortOrder, SQLiteDatabase db) {
        return db.query(TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    }

    public static long write(String[] projection, double[] values, SQLiteDatabase db) {
        if(projection.length != values.length) return -1;

        ContentValues record = new ContentValues();
        for (int i = 0; i < projection.length; i++) {
            record.put(projection[i], values[i]);
        }
        return db.insert(TABLE_NAME, null, record);
    }

    public static int remove(String selection, String[] selectionArgs, SQLiteDatabase db) {
        return db.delete(TABLE_NAME, selection, selectionArgs);
    }

    public static int update(String[] projection, String[] values, String selection, String[] selectionArgs, SQLiteDatabase db) {
        if(projection.length != values.length) return -1;

        ContentValues record = new ContentValues();
        for (int i = 0; i < projection.length; i++) {
            record.put(projection[i], values[i]);
        }

        return db.update(TABLE_NAME, record, selection, selectionArgs);
    }

    public static void reset(SQLiteDatabase db) {
        db.close();
    }
}
