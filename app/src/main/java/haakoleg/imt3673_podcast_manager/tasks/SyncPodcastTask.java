package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

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

public class SyncPodcastTask extends Task<List<PodcastEpisode>> {
    private final Context context;
    private final Podcast podcast;

    public SyncPodcastTask(Context context, Podcast podcast, OnSuccessListener<List<PodcastEpisode>> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
        this.podcast = podcast;
    }

    @Override
    protected int doTask() {
        // Save to SQLite database
        AppDatabase db = AppDatabase.getDb(context);

        // Podcast is not in database, so add it
        if (db.podcastDAO().getPodcast(podcast.getUrl()) == null) {
            db.podcastDAO().insertPodcast(podcast);
        }

        // Sync podcast with firebase
        syncPodcastWithFirebase(podcast);

        // Sync updated episodes
        resultObject = syncUpdatedEpisodes(db, podcast);
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
            if (episode.getAudioUrl() != null && episode.getUpdated() > lastUpdated) {
                episode.setParentUrl(podcast.getUrl());
                updated.add(episode);
            }
        }

        // Add to room
        db.podcastEpisodeDAO().insertEpisodes(updated);
        return updated;
    }
}
