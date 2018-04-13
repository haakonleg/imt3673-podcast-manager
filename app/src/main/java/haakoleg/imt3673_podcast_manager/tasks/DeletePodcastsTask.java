package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import haakoleg.imt3673_podcast_manager.database.AppDatabase;
import haakoleg.imt3673_podcast_manager.models.Podcast;

public class DeletePodcastsTask extends Task<Void> {
    private final Context context;
    private List<Podcast> podcasts;
    private final boolean deleteFromFirebase;

    public DeletePodcastsTask(Context context, List<Podcast> podcasts, boolean deleteFromFirebase, OnSuccessListener<Void> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
        this.podcasts = podcasts;
        this.deleteFromFirebase = deleteFromFirebase;
    }

    @Override
    protected int doTask() {
        AppDatabase db = AppDatabase.getDb(context);

        // Delete podcast episodes
        for (Podcast podcast : podcasts) {
            db.podcastEpisodeDAO().deleteEpisodes(podcast.getUrl());
        }
        // Delete podcasts
        db.podcastDAO().deletePodcasts(podcasts);

        // Delete podcast from user subscription list on firebase
        if (deleteFromFirebase) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(user.getUid()).child("subscriptions");

            for (Podcast podcast : podcasts) {
                dbRef.child(Integer.toHexString(podcast.hashCode())).removeValue();
            }
        }

        return SUCCESSFUL;
    }
}
