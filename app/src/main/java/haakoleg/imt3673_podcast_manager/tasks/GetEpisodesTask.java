package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import haakoleg.imt3673_podcast_manager.database.AppDatabase;
import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

/**
 * Task which is passed to ThreadManager which returns all podcast episodes from one or more
 * podcasts
 */

public class GetEpisodesTask extends Task<List<PodcastEpisode>> {
    private final Context context;
    private final List<Podcast> podcasts;
    private final int max;

    public GetEpisodesTask(Context context, List<Podcast> podcasts, OnSuccessListener<List<PodcastEpisode>> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
        this.podcasts = podcasts;

        // Get max episodes per podcast from preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.max = Integer.parseInt(prefs.getString("items_per_podcast", null));
    }

    @Override
    protected int doTask() {
        AppDatabase db = AppDatabase.getDb(context);

        ArrayList<String> podcastUrls = new ArrayList<>();
        for (Podcast podcast : podcasts) {
            podcastUrls.add(podcast.getUrl());
        }

        try {
            resultObject = db.podcastEpisodeDAO().getEpisodes(podcastUrls, max);
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), Log.getStackTraceString(e));
            return ERROR_SQLITE;
        }
        return SUCCESSFUL;
    }
}
