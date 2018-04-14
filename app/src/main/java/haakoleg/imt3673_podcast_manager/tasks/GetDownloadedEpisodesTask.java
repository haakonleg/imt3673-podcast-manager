package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import haakoleg.imt3673_podcast_manager.database.AppDatabase;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

public class GetDownloadedEpisodesTask extends Task<List<PodcastEpisode>> {
    private final Context context;

    public GetDownloadedEpisodesTask(Context context, OnSuccessListener<List<PodcastEpisode>> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
    }

    @Override
    protected int doTask() {
        AppDatabase db = AppDatabase.getDb(context);
        List<PodcastEpisode> downloadedEpisodes = new ArrayList<>();
        try {
            for (PodcastEpisode episode : db.podcastEpisodeDAO().getAllEpisodes()) {
                if (episode.isDownloaded()) {
                    downloadedEpisodes.add(episode);
                }
            }
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), Log.getStackTraceString(e));
            return ERROR_SQLITE;
        }
        resultObject = downloadedEpisodes;
        return SUCCESSFUL;
    }
}
