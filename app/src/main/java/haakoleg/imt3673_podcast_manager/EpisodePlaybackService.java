package haakoleg.imt3673_podcast_manager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.support.v4.media.app.NotificationCompat.MediaStyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This service is used for playback of podcast episodes, which a client/activity can
 * connect to and control (uses a client/server architecture described here
 * https://developer.android.com/guide/topics/media-apps/audio-app/building-an-audio-app.html).
 * The service is used so that the user can listen to audio when the app is in the background, and when the device
 * is sleeping. The service also creates a notification where playback can be controlled by the user.
 */

public class EpisodePlaybackService extends MediaBrowserServiceCompat {
    public static final int STATE_PREPARED = 123;
    private static final String NOTIFICATION_CHANNEL = "PodcastManagerChannel";
    private static final String NOTIFICATION_ID = "1234";
    private static final int UPDATER_INTERVAL = 1000;

    // Media items for playback is stored in this static field
    private static final ArrayList<MediaBrowserCompat.MediaItem> items = new ArrayList<>();

    private MediaSessionCompat mediaSession;
    private PlaybackStateCompat.Builder playbackStateBuilder;
    private MediaPlayer mediaPlayer;
    private WifiManager.WifiLock wifiLock;
    private NotificationCompat.Builder notificationBuilder;

    private boolean shouldUpdatePosition;

    /**
     * Set media item for playback
     * @param description A MediaDescriptionCompat object which contains information about the media
     */
    public static void setMedia(MediaDescriptionCompat description) {
        items.clear();
        items.add(new MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Intialize the mediasession and set callbacks for handling media and volume buttons
        mediaSession = new MediaSessionCompat(this, "MediaSession");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Assign playbackstate to mediasession
        playbackStateBuilder = new PlaybackStateCompat.Builder();
        playbackStateBuilder.setActions(
                        PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY |
                        PlaybackStateCompat.ACTION_PAUSE |
                        PlaybackStateCompat.ACTION_REWIND |
                        PlaybackStateCompat.ACTION_FAST_FORWARD |
                        PlaybackStateCompat.ACTION_STOP);
        mediaSession.setPlaybackState(playbackStateBuilder.build());

        // Set session token
        setSessionToken(mediaSession.getSessionToken());

        // Set callback
        mediaSession.setCallback(new MediaAdapter());

        // Initialize MediaPlayer
        mediaPlayer = new MediaPlayer();

        // Set wake locks so the mediaplayer will not stop playback when device is sleeping
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "EpisodePlaybackLock");
            wifiLock.acquire();
        }

        // If Android Oreo, needs a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    NOTIFICATION_ID,
                    NOTIFICATION_CHANNEL,
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nManager != null) {
                nManager.createNotificationChannel(channel);
            }
        }

        // Create notification for display of audio playback
        notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);
        notificationBuilder
                .setChannelId(NOTIFICATION_ID)
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_play_arrow_24dp)
                .setColor(ContextCompat.getColor(this, R.color.white))
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_pause_24dp, getString(R.string.play_pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .addAction(new NotificationCompat.Action(
                        R.drawable.ic_stop_24dp, "Stop",
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP)))
                .setStyle(new MediaStyle()
                        .setMediaSession(mediaSession.getSessionToken())
                        .setShowActionsInCompactView(0, 1));
    }

    /**
     * Sets a new playbackstate, so that listeners can be notified when the audio state changes
     * @param state A PlayBackStateCompat state
     */
    private void setPlayBackState(int state) {
        playbackStateBuilder.setState(state, mediaPlayer.getCurrentPosition(), 1.f);
        mediaSession.setPlaybackState(playbackStateBuilder.build());
    }

    /**
     * When the service is destroyed, make sure to release resources and wakelocks
     */
    @Override
    public void onDestroy() {
        mediaPlayer.release();
        wifiLock.release();
    }

    /**
     * Controls access to the mediabrowserservice, since the service is only used by this app
     * by default just accept all connections
     */
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return new BrowserRoot("root", null);
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(items);
    }

    /**
     * This class is responsible for controlling the MediaPlayer which
     * plays back the actual audio. It extends MediaSession.Callback so the MediaPlayer
     * can be controlled by client events.
     */
    private class MediaAdapter extends MediaSessionCompat.Callback implements
            MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnCompletionListener {

        /**
         * Starts position updater which notifies listeners about audio position changes
         * every second.
         */
        private void startPositionUpdater() {
            Handler handler = new Handler();
            shouldUpdatePosition = true;

            Runnable updater = new Runnable() {
                @Override
                public void run() {
                    if (shouldUpdatePosition) {
                        setPlayBackState(mediaSession.getController().getPlaybackState().getState());
                        handler.postDelayed(this, UPDATER_INTERVAL);
                    }
                }
            };
            updater.run();
        }

        /**
         * Displays the audio notification which displays the currently playing podcast episode
         * and brings the service to the foreground
         */
        private void displayNotification() {
            // Get the session metdata
            MediaControllerCompat controller = mediaSession.getController();
            MediaDescriptionCompat description = items.get(0).getDescription();

            notificationBuilder
                    .setContentTitle(description.getTitle())
                    .setContentText(description.getDescription())
                    .setContentIntent(controller.getSessionActivity());
            startForeground(1, notificationBuilder.build());
        }

        @Override
        public void onPrepare() {
            stopForeground(true);

            // Get the media to play
            MediaDescriptionCompat description = items.get(0).getDescription();
            if (description.getMediaUri() == null) {
                Log.e("EpisodePlaybackService", "No media source");
                return;
            }

            // Initialize MediaPlayer
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(description.getMediaUri().toString());
            } catch (IOException e) {
                Log.e("EpisodePlaybackService", Log.getStackTraceString(e));
            }

            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnInfoListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.prepareAsync();
        }

        @Override
        public void onPlay() {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                setPlayBackState(PlaybackStateCompat.STATE_PLAYING);
                displayNotification();
            }
        }

        @Override
        public void onPause() {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                setPlayBackState(PlaybackStateCompat.STATE_PAUSED);
            }
        }

        @Override
        public void onStop() {
            setPlayBackState(PlaybackStateCompat.STATE_STOPPED);
            // Stop service
            stopSelf();
        }

        @Override
        public void onFastForward() {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 30000);
            if (mediaPlayer.isPlaying()) {
                setPlayBackState(PlaybackStateCompat.STATE_PLAYING);
            } else {
                setPlayBackState(PlaybackStateCompat.STATE_PAUSED);
            }
        }

        @Override
        public void onRewind() {
            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 30000);
            if (mediaPlayer.isPlaying()) {
                setPlayBackState(PlaybackStateCompat.STATE_PLAYING);
            } else {
                setPlayBackState(PlaybackStateCompat.STATE_PAUSED);
            }
        }

        @Override
        public void onSeekTo(long pos) {
            mediaPlayer.seekTo((int) pos);
            if (mediaPlayer.isPlaying()) {
                setPlayBackState(PlaybackStateCompat.STATE_PLAYING);
            } else {
                setPlayBackState(PlaybackStateCompat.STATE_PAUSED);
            }
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            if (action.equals("StopUpdater")) {
                shouldUpdatePosition = false;
            } else if (action.equals("StartUpdater")) {
                startPositionUpdater();
            }
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            // Set metadata so we can get duration in the fragment
            MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
            builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.getDuration());
            mediaSession.setMetadata(builder.build());
            setPlayBackState(STATE_PREPARED);
        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(getClass().getName(), "MediaPlayer error: " + Integer.toString(what));
            return false;
        }

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what) {
                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
                    setPlayBackState(PlaybackStateCompat.STATE_BUFFERING);
                    return true;
                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
                    setPlayBackState(PlaybackStateCompat.STATE_PLAYING);
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            setPlayBackState(PlaybackStateCompat.STATE_STOPPED);
            // Stop service
            stopSelf();
        }
    }
}
