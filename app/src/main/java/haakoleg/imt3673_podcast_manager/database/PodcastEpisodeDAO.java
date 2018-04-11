package haakoleg.imt3673_podcast_manager.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

@Dao
public interface PodcastEpisodeDAO {
    @Query("SELECT * FROM PodcastEpisode ORDER BY updated DESC")
    List<PodcastEpisode> getAllEpisodes();

    @Query("SELECT * FROM PodcastEpisode WHERE parentUrl IN (:parentUrls) ORDER BY updated DESC LIMIT 0,:max")
    List<PodcastEpisode> getEpisodes(List<String> parentUrls, int max);

    @Query("SELECT updated FROM PodcastEpisode WHERE parentUrl = :parentUrl ORDER BY updated DESC LIMIT 1")
    long getLastUpdated(String parentUrl);

    @Insert
    void insertEpisode(PodcastEpisode episode);

    @Insert
    void insertEpisodes(List<PodcastEpisode> episodes);

    @Update
    void updateEpisode(PodcastEpisode episode);

    @Delete
    void deleteEpisode(PodcastEpisode episode);
}
