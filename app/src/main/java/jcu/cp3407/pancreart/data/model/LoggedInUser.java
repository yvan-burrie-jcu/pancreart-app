package jcu.cp3407.pancreart.data.model;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
public class LoggedInUser {

    private long id;

    private String name;

    private String email;

    private String token;

    public LoggedInUser(long id, String name, String email, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.token = token;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }
}
