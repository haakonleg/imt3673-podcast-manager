package haakoleg.imt3673_podcast_manager.models;

import java.util.ArrayList;
import java.util.List;

public class Podcast {
    // The URL to the feed itself
    private String url;

    // Data from parsing the feed
    private String title;
    private String link;
    private String description;
    private String category;
    private String image;
    private long updated;

    private List<PodcastEpisode> episodes;

    public Podcast() {
        episodes = new ArrayList<>();
    }

    public void addEpisode(PodcastEpisode episode) {
        episodes.add(episode);
    }

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
}
