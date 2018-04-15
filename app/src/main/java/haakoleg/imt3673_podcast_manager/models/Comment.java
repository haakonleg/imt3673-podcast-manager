package haakoleg.imt3673_podcast_manager.models;

public class Comment {
    private String username;
    private String comment;
    private int rating;

    public Comment() {
        // Empty constructor required by Firebase getValue
    }

    public Comment(String username, String comment, int rating) {
        this.username = username;
        this.comment = comment;
        this.rating = rating;
    }

    public String getUsername() {
        return username;
    }

    public String getComment() {
        return comment;
    }

    public int getRating() {
        return rating;
    }
}
