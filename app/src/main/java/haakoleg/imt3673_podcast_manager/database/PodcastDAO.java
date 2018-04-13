package haakoleg.imt3673_podcast_manager.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import haakoleg.imt3673_podcast_manager.models.Podcast;

@Dao
public interface PodcastDAO {
    @Query("SELECT * FROM Podcast WHERE url = :url")
    Podcast getPodcast(String url);

    @Query("SELECT * FROM Podcast WHERE url IN (:urls)")
    List<Podcast> getPodcasts(List<String> urls);

    @Query("SELECT * FROM Podcast")
    List<Podcast> getAllPodcasts();

    @Query("DELETE FROM Podcast")
    void deleteAll();

    @Update
    void updatePodcasts(List<Podcast> podcasts);

    @Insert
    void insertPodcast(Podcast podcast);

    @Insert
    void insertPodcasts(Podcast... podcasts);

    @Delete
    void deletePodcast(Podcast podcast);

    @Delete
    void deletePodcasts(List<Podcast> podcasts);
}
