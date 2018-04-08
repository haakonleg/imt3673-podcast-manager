package haakoleg.imt3673_podcast_manager.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Podcast {
    // The URL to the feed itself
    @NonNull
    @PrimaryKey
    private String url;

    // Data from parsing the feed
    private String title;
    private String link;
    private String description;
    private String category;
    private String image;
    private long updated;

    // Exclude from Room and Firebase, as this field is not used there
    @Ignore
    private List<PodcastEpisode> episodes;

    public Podcast() {
        episodes = new ArrayList<>();
    }

    public void addEpisode(PodcastEpisode episode) {
        episodes.add(episode);
    }

    // Exclude episodes from Firebase
    @Exclude
    public List<PodcastEpisode> getEpisodes() {
        return episodes;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.url, this.title, this.link,
                this.description, this.category, this.image, this.updated);
    }
}
