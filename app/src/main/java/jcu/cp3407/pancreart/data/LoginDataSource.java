package jcu.cp3407.pancreart.data;

import org.json.JSONObject;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import jcu.cp3407.pancreart.data.model.LoggedInUser;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String email, String password, String address) {
        try {
//            JSONObject response = loginLaravelPassport(
//                    "yvan.burrie@hotmail.com",
//                    "_7dXSM6$5ac$v8c",
//                    address);
            JSONObject response = loginLaravelPassport(email, password, address);
            if (response == null) {
                return new Result.Error(new IOException("No response from logging in"));
            }
            long id = 0;
            if (response.has("userId")) {
                id = response.getLong("id");
            }
            String name = "";
            if (response.has("name")) {
                name = response.getString("name");
            }
            String token = "";
            if (response.has("token")) {
                token = response.getString("token");
            }
            System.out.println("\n\n" + token + "\n\n");
            LoggedInUser user = new LoggedInUser(id, name, email, token);
            return new Result.Success<>(user);
        } catch (Exception exception) {
            return new Result.Error(new IOException("Error logging in", exception));
        }
    }

    private JSONObject loginLaravelPassport(String email, String password, String address) throws Exception {
        // Create JSON object containing login credentials
        JSONObject loginPostBuffer = new JSONObject();
        loginPostBuffer.accumulate("email", email);
        loginPostBuffer.accumulate("password", password);

        // Create connection to login endpoint
        URL loginUrl = new URL("http://" + address + "/api/login");
        HttpURLConnection connection = (HttpURLConnection) loginUrl.openConnection();
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);

        // Send JSON object through connection
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(loginPostBuffer.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.close();

        // Prepare the JSON response
        JSONObject response = null;
        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inputStream = connection.getInputStream();
            String data = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            inputStream.close();
            response = new JSONObject(data);
        }
        return response;
    }

    public void logout(String address) {
        logoutLaravelPassport(address);
    }

    private void logoutLaravelPassport(String address) {
        // Create connection to logout endpoint
        try {
            new URL("http://" + address + "/api/logout").openStream();
        } catch (IOException ignored) {}
    }
}
