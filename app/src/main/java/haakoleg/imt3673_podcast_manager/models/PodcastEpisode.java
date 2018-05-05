package haakoleg.imt3673_podcast_manager.models;

import android.arch.persistence.room.Entity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.File;
import java.util.concurrent.TimeUnit;

import haakoleg.imt3673_podcast_manager.MainActivity;
import haakoleg.imt3673_podcast_manager.utils.Hash;

/**
 * Data model for a podcast episode
 */

@Entity(primaryKeys = {"parentUrl", "audioUrl"})
public class PodcastEpisode implements Parcelable {
    @NonNull
    private String parentUrl;

    private String title;
    private String description;
    private String link;
    @NonNull
    private String audioUrl;
    private int duration;
    private long updated;

    public PodcastEpisode() { }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @SuppressWarnings("unused")
    public String getLink() {
        // Required by Firebase DataSnapshot.getValue()
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    @SuppressWarnings("unused")
    public int getDuration() {
        // Required by Firebase DataSnapshot.getValue()
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getUpdated() {
        return updated;
    }

    public void setUpdated(long updated) {
        this.updated = updated;
    }

    public String getParentUrl() {
        return parentUrl;
    }

    public void setParentUrl(String parentUrl) {
        this.parentUrl = parentUrl;
    }

    public String getFormattedDuration() {
        if (duration != 0) {
            return Long.toString(TimeUnit.SECONDS.toMinutes(duration)) + " min";
        }
        return "";
    }

    /**
     * Check if this podcast episode is downloaded to external storage
     * @return True if the podcast is downloaded, else false
     */
    public boolean isDownloaded() {
        return new File(MainActivity.DOWNLOAD_DIR, Integer.toHexString(this.hashCode())).exists();
    }

    public PodcastEpisode(Parcel in) {
        parentUrl = in.readString();
        title = in.readString();
        description = in.readString();
        link = in.readString();
        audioUrl = in.readString();
        duration = in.readInt();
        updated = in.readLong();
    }

    public static final Parcelable.Creator<PodcastEpisode> CREATOR = new Parcelable.Creator<PodcastEpisode>() {
        @Override
        public PodcastEpisode createFromParcel(Parcel source) {
            return new PodcastEpisode(source);
        }

        @Override
        public PodcastEpisode[] newArray(int size) {
            return new PodcastEpisode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(parentUrl);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(link);
        dest.writeString(audioUrl);
        dest.writeInt(duration);
        dest.writeLong(updated);
    }

    @Override
    public int hashCode() {
        return Hash.getHash(parentUrl, audioUrl);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PodcastEpisode)) {
            return false;
        }

        PodcastEpisode o = (PodcastEpisode) obj;
        return this.parentUrl.equals(o.parentUrl) && this.audioUrl.equals(o.audioUrl);
    }
}
