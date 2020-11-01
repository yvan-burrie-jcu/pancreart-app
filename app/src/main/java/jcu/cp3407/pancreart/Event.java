package jcu.cp3407.pancreart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class Event {
    private int eventType;
    private int userId;
    private int time;
    private float amount;
    private JSONObject eventData;

    public void Event(int startTime, int endTime) throws IOException, JSONException {
      getArrayData(startTime, endTime);
      setEventData();
    }

    public int getUserId() {
        return userId;
    }

    public void setEventData() { // setting all event object features

        // userId = eventData[0]
    }

    private void getArrayData(int startTime, int endTime) throws JSONException, IOException { // handling web server request
        final String address = "34.70.63.29";
        eventData = new JSONObject();
        eventData.accumulate("id", "");
        eventData.accumulate("time", "");
        eventData.accumulate("amount", "");
        eventData.accumulate("eventType", "");
        eventData.accumulate("startTime", startTime);
        eventData.accumulate("endTime", endTime);

        JSONObject eventPostBuffer = new JSONObject();
        eventPostBuffer.accumulate("event_parameters", eventData);

        URL eventGetUrl = new URL("http://" + address + "/events/get");
        HttpURLConnection connection = (HttpURLConnection) eventGetUrl.openConnection();
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(eventPostBuffer.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.close();

        System.out.println(eventPostBuffer.names());

    }

//    //JSON test method for writing values to a file
//    private void testJSON(int startTime, int endTIme) throws IOException, JSONException {
//        getArrayData(12, 2);
//        JSONArray JSONTestArray = new JSONArray();
//        FileWriter JSONTest = new FileWriter("C:\\json_test_output.json");
//        JSONTest.write(JSONTestArray.toString());
//    }

}
