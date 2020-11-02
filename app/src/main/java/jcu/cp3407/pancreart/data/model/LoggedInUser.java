package jcu.cp3407.pancreart.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private String userId;
    private String displayName;
    private String accessToken;

    public LoggedInUser(String userId, String displayName, String accessToken) {
        this.userId = userId;
        this.displayName = displayName;
        this.accessToken = accessToken;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAccessToken() {
        return accessToken;
    }
}