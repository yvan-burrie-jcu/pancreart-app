package jcu.cp3407.pancreart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Stack;

class Storage extends SQLiteOpenHelper {

    private static final String FILE_NAME = "pancreart.sql";

    private static final int VERSION = 1;

    static final class EventTable implements BaseColumns {
        static final String NAME = "event";
        static final String COL_TIMESTAMP = "timestamp";
        static final String COL_UPLOADED = "uploaded";
    }

    Storage(Context context) {
        super(context, FILE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL("CREATE TABLE " + EventTable.NAME + " " +
                "(" +
                EventTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT" +
                "," +
                EventTable.COL_TIMESTAMP + " INTEGER" +
                "," +
                EventTable.COL_UPLOADED + " BOOLEAN" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            database.execSQL("DROP TABLE IF EXISTS " + EventTable.NAME);
            onCreate(database);
        }
    }

    void insertEvent(int timestamp, boolean uploaded) {
        ContentValues values = new ContentValues();
        values.put(EventTable.COL_TIMESTAMP, timestamp);
        values.put(EventTable.COL_UPLOADED, uploaded);
        getWritableDatabase().insert(EventTable.NAME, null, values);
    }

    Stack<Integer> selectEvents(int minTimestamp, int maxTimestamp) {
        if (minTimestamp > maxTimestamp) {
            int tmpTimestamp = minTimestamp;
            minTimestamp = maxTimestamp;
            maxTimestamp = tmpTimestamp;
        }
        SQLiteDatabase database = getReadableDatabase();
        Stack<Integer> selectedEvents = new Stack<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + EventTable.NAME + " " +
                    "WHERE " + EventTable.COL_TIMESTAMP + " >= " + minTimestamp +
                    "AND   " + EventTable.COL_TIMESTAMP + " <= " + maxTimestamp, null);
            if (cursor.moveToFirst()) {
                do {
                    selectedEvents.add(cursor.getInt(cursor.getColumnIndex(EventTable._ID)));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return selectedEvents;
    }
}
