package jcu.cp3407.pancreart;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

import jcu.cp3407.pancreart.model.Event;

import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws JSONException {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("jcu.cp3407.pancreart", appContext.getPackageName());

        Storage storage = new Storage(appContext);

        getServerData(storage);

        final String EVENT_TAG = "EVENT";
        long userId = 0;
        long minTime = 1601596800; // 10/02/2020 @ 12:00am (UTC)
        long maxTime = 1601683199; // 10/02/2020 @ 11:59pm (UTC)

        List<Event> events = storage.getEvents(userId, minTime, maxTime);
        Log.i(EVENT_TAG, "Events Array Length: " + events);

        for (Event event : events) {
            Log.i(EVENT_TAG, "userID: " + event.getUserId() + " owner: " + event.getOwner() +
                    " type: " + event.getTypeString() + " time: " + event.getTime() + " amount: " + event.getAmount());
        }
        // Retrieve event from SQLite database
    }

    private void getServerData(Storage storage) throws JSONException {
        final String JSON_TAG = "JSON";
        // Mock JSON data from server

        // User1 time : 1601618400 | 10/02/2020 @ 6:00am (UTC)
        // User1 time : 1601640000 | 10/02/2020 @ 12:00pm (UTC)
        final String SERVER_DATA =
                "{\"events\": [" +
                        "{\"event\": [" +
                            "{\"userId\":\"0\"}," +
                            "{\"type\":\"0\"}," +
                            "{\"time\":\"1601618400\"}," +
                            "{\"amount\":\"8\"}," +
                            "{\"owner\":\"2\"}" +
                            "]" +
                        "}," +
                        "{\"event\": [" +
                            "{\"userId\":\"0\"}," +
                            "{\"type\":\"0\"}," +
                            "{\"time\":\"1601640000\"}," +
                            "{\"amount\":\"4\"}," +
                            "{\"owner\":\"2\"}" +
                            "]" +
                        "}" +
                    "]" +
                "}";
        final int OWNER = 2;
        Event.Type type = Event.Type.GLUCOSE_READING;

        JSONObject dataObject = new JSONObject(SERVER_DATA);
        Log.i(JSON_TAG, "Received Data Object: " + dataObject.toString());
        Log.i(JSON_TAG, "Length: " + dataObject.length());


        // Store in SQLite database
        if (dataObject.has("events")) {
            Log.i(JSON_TAG, "Contains \"events\"");

            JSONArray dataArray = dataObject.getJSONArray("events");
            Log.i(JSON_TAG, "JSON Array: " + dataArray.toString());
            Log.i(JSON_TAG, "JSON Array Length: " + dataArray.length());

            for (int i = 0; i < dataArray.length(); i++) {
                // Get Individual Event JSONObject
                JSONObject eventData = dataArray.getJSONObject(i);
                Log.i(JSON_TAG, "JSON Event Object: " + eventData.toString());

                // Transform to JSONArray
                JSONArray eventArray = eventData.getJSONArray("event");
                Log.i(JSON_TAG, "JSON Array: " + eventArray.toString());
                Log.i(JSON_TAG, "JSON Array Length: " + eventArray.length());

                long userId = 0;
                long time = 0;
                double amount = 0;

                // Get each value within event JSONArray
                for (int j = 0; j < eventArray.length(); j++) {
                    JSONObject value = eventArray.getJSONObject(j);
                    Log.i(JSON_TAG, "Value: " + value.toString());

                    if (value.has("userId")) {
                        userId = value.getLong("userId");
                    } else if (value.has("time")) {
                        time = value.getLong("time");
                    } else if (value.has("amount")) {
                        amount = value.getDouble("amount");
                    }
                }
                 // Create event and store in database
                Event event = new Event(userId, OWNER, type, time, amount);
                storage.addEvent(event);
            }
        }
    }
}