package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import haakoleg.imt3673_podcast_manager.database.AppDatabase;
import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

public class GetEpisodesTask extends Task<List<PodcastEpisode>> {
    private final Context context;
    private final List<Podcast> podcasts;

    public GetEpisodesTask(Context context, List<Podcast> podcasts, OnSuccessListener<List<PodcastEpisode>> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
        this.podcasts = podcasts;
    }

    @Override
    protected int doTask() {
        AppDatabase db = AppDatabase.getDb(context);

        ArrayList<String> podcastUrls = new ArrayList<>();
        for (Podcast podcast : podcasts) {
            podcastUrls.add(podcast.getUrl());
        }

        resultObject = db.podcastEpisodeDAO().getEpisodes(podcastUrls, 50);
        return 0;
    }
}
