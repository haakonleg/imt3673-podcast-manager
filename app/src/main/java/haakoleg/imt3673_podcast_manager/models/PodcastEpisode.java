package haakoleg.imt3673_podcast_manager.models;

import android.arch.persistence.room.Entity;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

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

    public String getLink() {
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

    public int getDuration() {
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
}
