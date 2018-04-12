package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import haakoleg.imt3673_podcast_manager.database.AppDatabase;
import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

public class SyncPodcastsTask extends Task<List<PodcastEpisode>> {
    private final Context context;
    private final List<Podcast> podcasts;

    public SyncPodcastsTask(Context context, List<Podcast> podcasts, OnSuccessListener<List<PodcastEpisode>> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
        this.podcasts = podcasts;
    }

    @Override
    protected int doTask() {
        // Save to SQLite database
        AppDatabase db = AppDatabase.getDb(context);

        List<PodcastEpisode> updatedEpisodes = new ArrayList<>();
        for (Podcast podcast : podcasts) {
            // Podcast is not in database, so add it
            if (db.podcastDAO().getPodcast(podcast.getUrl()) == null) {
                db.podcastDAO().insertPodcast(podcast);
            }
            syncPodcastWithFirebase(podcast);
            updatedEpisodes.addAll(syncUpdatedEpisodes(db, podcast));
        }

        // Sync updated episodes
        resultObject = updatedEpisodes;
        return SUCCESSFUL;
    }

    private void syncPodcastWithFirebase(Podcast podcast) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Use hashcode of the podcast object as key
        String key = Integer.toHexString(podcast.hashCode());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("podcasts").child(key);

        // Check if the podcast exists in Firebase, if not add it
        // Also add the user to the podcast list of subscribed users
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    dbRef.setValue(podcast);
                }
                dbRef.child("subscribers").child(user.getUid()).setValue(Boolean.TRUE);
                dbRef.getParent().getParent().child("users").child(user.getUid()).child("subscriptions").child(key).setValue(podcast.getUrl());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private List<PodcastEpisode> syncUpdatedEpisodes(AppDatabase db, Podcast podcast) {
        ArrayList<PodcastEpisode> updated = new ArrayList<>();

        // Get the last updated episode
        long lastUpdated = db.podcastEpisodeDAO().getLastUpdated(podcast.getUrl());

        // Find new/updated episodes
        for (PodcastEpisode episode : podcast.getEpisodes()) {
            if (episode.getUpdated() > lastUpdated) {
                episode.setParentUrl(podcast.getUrl());
                updated.add(episode);
            }
        }

        // Add to room
        db.podcastEpisodeDAO().insertEpisodes(updated);
        return updated;
    }
}
