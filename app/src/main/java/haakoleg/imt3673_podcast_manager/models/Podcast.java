package haakoleg.imt3673_podcast_manager.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Podcast implements Parcelable {
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

    public Podcast() { }

    public void addEpisode(PodcastEpisode episode) {
        if (episodes == null) {
            episodes = new ArrayList<>();
        }
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

    /**
     * Constructor for parcelable
     * @param in Parcel to fill with data
     */
    public Podcast(Parcel in) {
        url = in.readString();
        title = in.readString();
        link = in.readString();
        description = in.readString();
        category = in.readString();
        image = in.readString();
        updated = in.readLong();
    }

    public static final Parcelable.Creator<Podcast> CREATOR = new Parcelable.Creator<Podcast>() {
        @Override
        public Podcast createFromParcel(Parcel source) {
            return new Podcast(source);
        }

        @Override
        public Podcast[] newArray(int size) {
            return new Podcast[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeString(title);
        dest.writeString(link);
        dest.writeString(description);
        dest.writeString(category);
        dest.writeString(image);
        dest.writeLong(updated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.url);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Podcast)) {
            return false;
        }

        Podcast o = (Podcast) obj;
        return this.url.equals(o.url);
    }
}
