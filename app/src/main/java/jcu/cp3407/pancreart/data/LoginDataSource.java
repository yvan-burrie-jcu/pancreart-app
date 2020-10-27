package jcu.cp3407.pancreart.data;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import jcu.cp3407.pancreart.data.model.LoggedInUser;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password, String address) {
        try {
            loginLaravelPassport(username, password, address);
            LoggedInUser fakeUser = new LoggedInUser("", "Jane Doe");
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    private void loginLaravelPassport(String username, String password, String address) throws Exception {
        // create JSON object containing login details
        JSONObject formsParamsBuffer = new JSONObject();
        formsParamsBuffer.accumulate("grant_type", "password");
        formsParamsBuffer.accumulate("client_id", "");
        formsParamsBuffer.accumulate("client_secret", "");
        formsParamsBuffer.accumulate("username", username);
        formsParamsBuffer.accumulate("password", password);
        formsParamsBuffer.accumulate("scope", "");
        JSONObject loginPostBuffer = new JSONObject();
        loginPostBuffer.accumulate("form_params", formsParamsBuffer);
        // create connection to login endpoint
        URL loginUrl = new URL("http://" + address + "/oauth/login");
        HttpURLConnection connection = (HttpURLConnection) loginUrl.openConnection();
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        // send JSON object through connection
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(loginPostBuffer.toString().getBytes(StandardCharsets.UTF_8));
        outputStream.close();


    }

    public void logout() {

    }
}
