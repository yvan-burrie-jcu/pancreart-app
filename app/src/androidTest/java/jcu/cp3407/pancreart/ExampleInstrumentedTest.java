package jcu.cp3407.pancreart;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

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
//
//        Storage storage = new Storage(appContext);
//
//        getServerData(storage);
//        long userId = 1;
//        long minTime = 1601596800; // 10/02/2020 @ 12:00am (UTC)
//        long maxTime = 1601683199; // 10/02/2020 @ 11:59pm (UTC)
//        Stack<Integer> events = storage.getEvents(userId, minTime, maxTime);
//        // Events = [event1 [attribute : value], event2 [attribute : value]]
//        // [id [userId, type, time, amount, owner]]
//
//        for (int i = 0; i <= events.size() - 1; i++) {
//            System.out.println(events.pop());
//        }
//
//        // Retrieve event from SQLite database
//    }
//
//    private void getServerData(Storage storage) throws JSONException {
//        // Mock JSON data from server
//
//        // User1 time : 1601618400 | 10/02/2020 @ 6:00am (UTC)
//        // User1 time : 1601640000 | 10/02/2020 @ 12:00pm (UTC)
//
//        final String SERVER_DATA = "{\"events\":{\"userId\":\"0\",\"type\":\"0\",\"time\":\"1601618400\",\"amount\":\"8\",\"owner\":\"2\"},\"userId\":\"0\",\"type\":\"0\",\"time\":\"1601640000\",\"amount\":\"4\",\"owner\":\"2\"}}";
//        final int OWNER = 2;
//        Event.Type type = Event.Type.GLUCOSE_READING;
//
//        JSONArray dataObject = new JSONArray(SERVER_DATA);
//        Log.i("JSON", dataObject.toString());
//        Log.i("JSON", String.valueOf(dataObject.length()));
//
//
//        // Store in SQLite database
//        if (dataObject.has("events")) {
//            JSONArray dataArray = dataObject.getJSONArray("events");
//            Log.i("JSON", dataArray.toString());
//            Log.i("JSON", "Contains \"events\"");
//            for (int i = 0; i < dataArray.length(); i++) {
//                JSONObject eventData = dataArray.getJSONObject(i);
//                Log.i("JSON", eventData.toString());
//            }
//
//
//                long userId = eventData.getLong("userId");
//                long time = eventData.getLong("time");
//                double amount = eventData.getDouble("amount");
//                Event event = new Event(userId, OWNER, type, time, amount);
//                storage.addEvent(event);
//        }
    }
}