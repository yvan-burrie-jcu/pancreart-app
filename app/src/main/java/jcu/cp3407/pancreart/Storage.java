package jcu.cp3407.pancreart;

import jcu.cp3407.pancreart.model.Event;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import java.util.Stack;

class Storage extends SQLiteOpenHelper {

    private static final String FILE_NAME = "pancreart.db";

    private static final int VERSION = 2;

    static final class EventTable implements BaseColumns {
        static final String NAME = "event";
        static final String COL_USER_ID = "user_id";
        static final String COL_TYPE = "type";
        static final String COL_TIME = "time";
        static final String COL_AMOUNT = "amount";
        static final String COL_OWNER = "uploaded";
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
                EventTable.COL_USER_ID + " INTEGER" +
                "," +
                EventTable.COL_TYPE + " INTEGER" +
                "," +
                EventTable.COL_TIME + " INTEGER" +
                "," +
                EventTable.COL_AMOUNT + " REAL" +
                "," +
                EventTable.COL_OWNER + " BOOLEAN" +
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

    void addEvent(Event event) {
        ContentValues values = new ContentValues();
        values.put(EventTable.COL_TIME, event.time);
        values.put(EventTable.COL_OWNER, event.owner);
        getWritableDatabase().insert(EventTable.NAME, null, values);
    }

    Stack<Integer> getEvents(long minTime, long maxTime) {
        if (minTime > maxTime) {
            long tempTime = minTime;
            minTime = maxTime;
            maxTime = tempTime;
        }
        SQLiteDatabase database = getReadableDatabase();
        Stack<Integer> selectedEvents = new Stack<>();
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                    "SELECT * FROM " + EventTable.NAME + " " +
                    "WHERE " + EventTable.COL_TIME + " >= " + minTime +
                    "AND   " + EventTable.COL_TIME + " <= " + maxTime, null);
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
