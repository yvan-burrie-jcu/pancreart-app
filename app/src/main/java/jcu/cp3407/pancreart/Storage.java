package jcu.cp3407.pancreart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Stack;

import jcu.cp3407.pancreart.model.Event;

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
                EventTable.COL_OWNER + " INTEGER" +
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
        values.put(EventTable.COL_USER_ID, event.userId);
        values.put(EventTable.COL_OWNER, event.owner);
        values.put(EventTable.COL_TYPE, event.type.ordinal());
        values.put(EventTable.COL_TIME, event.time);
        values.put(EventTable.COL_AMOUNT, event.amount);

        getWritableDatabase().insert(EventTable.NAME, null, values);
    }

    Stack<Integer> getEvents(long userId, long minTime, long maxTime) {
        // Call to populate graph
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
                            "WHERE " + EventTable.COL_USER_ID + "= " + userId +
                            "AND " + EventTable.COL_TIME + " >= " + minTime +
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

    private void getEventsFromServer(long startTime, long endTime) throws JSONException, IOException {  // add user auth code parameter

        // Retrieve logged in user auth code
        String accessToken = "AIDUS MCGLEETUS";


        final String address = "http://35.222.34.156/events/get";
        // Create JSON object containing login credentials
        JSONObject eventData = new JSONObject();
        eventData.accumulate("startTime", startTime);
        eventData.accumulate("endTime", endTime);

        // Create connection to Events endpoint
        URL eventsURL = new URL(address);
        // authorization  equal to Bearer accessToken
        HttpURLConnection connection = (HttpURLConnection) eventsURL.openConnection();
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Send JSON object through connection
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(eventData.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.close();

        // Prepare the JSON response
        JSONObject response = null;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            String data = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            inputStream.close();
            response = new JSONObject(data);
        }
        assert response != null;
        storeEvents(response);
    }

    private void storeEvents(JSONObject receivedData) throws JSONException {
        final int OWNER = 2;
        long userId = receivedData.getLong("userId");
        Event.Type type = Event.Type.GLUCOSE_READING;

        if (receivedData.has("events")) {
            JSONArray eventsBuffer = receivedData.getJSONArray("events");
            for (int i = 0; i < eventsBuffer.length(); i++) {
                JSONObject eventData = eventsBuffer.getJSONObject(i);

                long time = eventData.getLong("time");
                double amount = eventData.getDouble("amount");
                Event event = new Event(userId, OWNER, type, time, amount);
                addEvent(event);
            }
        }
    }
}
