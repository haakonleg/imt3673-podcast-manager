package haakoleg.imt3673_podcast_manager.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

@Database(entities = {Podcast.class, PodcastEpisode.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase dbInstance;

    public abstract PodcastDAO podcastDAO();
    public abstract PodcastEpisodeDAO podcastEpisodeDAO();

    public static AppDatabase getDb(Context context) {
        if (dbInstance == null) {
            dbInstance = Room.databaseBuilder(context, AppDatabase.class, "PodcastDb").build();
        }
        return dbInstance;
    }

    public static void destroy() {
        dbInstance = null;
    }
}
