package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteException;
import android.support.v7.preference.PreferenceManager;
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
    private final int max;

    public SyncPodcastTask(Context context, Podcast podcast, OnSuccessListener<List<PodcastEpisode>> successListener, OnErrorListener errorListener) {
        super(successListener, errorListener);
        this.context = context;
        this.podcast = podcast;

        // Get max episodes per podcast from preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.max = Integer.parseInt(prefs.getString("items_per_podcast", null));
    }

    @Override
    protected int doTask() {
        // Save to SQLite database
        AppDatabase db = AppDatabase.getDb(context);

        // Podcast is not in database, so add it
        try {
            if (db.podcastDAO().getPodcast(podcast.getUrl()) == null) {
                db.podcastDAO().insertPodcast(podcast);
            }
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), Log.getStackTraceString(e));
            return ERROR_SQLITE;
        }

        // Sync podcast with firebase
        syncPodcastWithFirebase(podcast);

        // Sync updated episodes
        try {
            resultObject = syncUpdatedEpisodes(db, podcast);
        } catch (SQLiteException e) {
            Log.e(getClass().getName(), Log.getStackTraceString(e));
            return ERROR_SQLITE;
        }
        return SUCCESSFUL;
    }

    private void syncPodcastWithFirebase(Podcast podcast) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Use hashcode of the podcast object as key
        String key = Integer.toHexString(podcast.hashCode());
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("podcasts").child(key);

        // Check if the podcast exists in Firebase, if not add it
        // Also update the count of subscribed users
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
                // Unused
            }
        });
    }

    private List<PodcastEpisode> syncUpdatedEpisodes(AppDatabase db, Podcast podcast) throws SQLiteException {
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

        // Insert new episodes into SQLite database
        db.podcastEpisodeDAO().insertEpisodes(updated);

        // Check if old episodes need to be deleted, because the max amount of episodes
        // per podcast has been reached or exceeded
        int count = db.podcastEpisodeDAO().getCount(podcast.getUrl());
        if (count > this.max) {
            db.podcastEpisodeDAO().deleteOldEpisodes(podcast.getUrl(), count - max);
        }

        return updated;
    }
}
