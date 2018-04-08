package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;

import java.util.List;

import haakoleg.imt3673_podcast_manager.database.AppDatabase;
import haakoleg.imt3673_podcast_manager.models.Podcast;

public class GetPodcastsTask extends Task<List<Podcast>> {
    private Context context;

    public GetPodcastsTask(Context context, OnSuccessListener<List<Podcast>> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
    }

    @Override
    protected int doTask() {
        AppDatabase db = AppDatabase.getDb(context);
        resultObject = db.podcastDAO().getAllPodcasts();
        return SUCCESSFUL;
    }
}
