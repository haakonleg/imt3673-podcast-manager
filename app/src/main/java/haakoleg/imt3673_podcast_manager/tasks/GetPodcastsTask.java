package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import java.util.List;

import haakoleg.imt3673_podcast_manager.database.AppDatabase;
import haakoleg.imt3673_podcast_manager.models.Podcast;

/**
 * Task which is passed to ThreadManager which returns all the users saved podcasts
 */

public class GetPodcastsTask extends Task<List<Podcast>> {
    private final Context context;

    public GetPodcastsTask(Context context, OnSuccessListener<List<Podcast>> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
    }

    @Override
    protected int doTask() {
        AppDatabase db = AppDatabase.getDb(context);
        try {
            resultObject = db.podcastDAO().getAllPodcasts();
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), Log.getStackTraceString(e));
            return ERROR_SQLITE;
        }
        return SUCCESSFUL;
    }
}
