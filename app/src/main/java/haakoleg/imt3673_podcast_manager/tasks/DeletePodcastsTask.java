package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;

import haakoleg.imt3673_podcast_manager.database.AppDatabase;

public class DeletePodcastsTask extends Task<Void> {
    private Context context;

    public DeletePodcastsTask(Context context, OnSuccessListener<Void> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
    }

    @Override
    protected int doTask() {
        AppDatabase db = AppDatabase.getDb(context);
        db.podcastEpisodeDAO().deleteAll();
        db.podcastDAO().deleteAll();
        return SUCCESSFUL;
    }
}
