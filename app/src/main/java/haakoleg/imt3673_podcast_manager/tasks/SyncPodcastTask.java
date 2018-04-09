package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;
import android.util.Base64;
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
    private Context context;
    private Podcast podcast;

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
        if (db.podcastDAO().getPodcast(this.podcast.getUrl()) == null) {
            db.podcastDAO().insertPodcast(this.podcast);
        }

        syncPodcastWithFirebase(this.podcast);

        // Sync updated episodes
        resultObject = syncUpdatedEpisodes(db, this.podcast);
        return SUCCESSFUL;
    }

    private void syncPodcastWithFirebase(Podcast podcast) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Need to encode the podcast URL to base64 string since URLs can't be stored as key in Firebase
        String encodedUrl = Base64.encodeToString(podcast.getUrl().getBytes(), Base64.NO_WRAP);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("podcasts").child(encodedUrl);

        // Check if the podcast exists in Firebase, if not add it
        // Also add the user to the podcast list of subscribed users
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    dbRef.setValue(podcast);
                }
                dbRef.child("subscribers").child(user.getUid()).setValue(Boolean.TRUE);
                dbRef.getParent().getParent().child("users").child(user.getUid()).child("subscriptions").child(encodedUrl).setValue(podcast.getUrl());
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