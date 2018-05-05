package haakoleg.imt3673_podcast_manager.models;

/**
 * Data model for a user
 */

// Suppress unused warnings because setter and getter methods are needed by Firebase
@SuppressWarnings("unused")
public class User {
    private String username;
    private String email;

    public User() {
        // Required for DataSnapshot.getValue()
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
