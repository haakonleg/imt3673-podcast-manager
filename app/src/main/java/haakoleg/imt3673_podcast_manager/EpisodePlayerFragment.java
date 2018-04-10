package haakoleg.imt3673_podcast_manager;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import haakoleg.imt3673_podcast_manager.models.Podcast;
import haakoleg.imt3673_podcast_manager.models.PodcastEpisode;

/**
 * This fragment is responsible for displaying the currently played podcast episode
 * and controlling the audio state, such as pause, forward and displaying the current
 * state of the audio playing
 */

public class EpisodePlayerFragment extends Fragment {
    private PodcastEpisode episode;
    private Podcast podcast;
    private MediaBrowserCompat mediaBrowser;
    private ControllerCallback controllerCallback;

    private ProgressBar progressBar;
    private ImageButton rewindBtn;
    private ImageButton playBtn;
    private ImageButton forwardBtn;
    private TextView durationTxt;
    private TextView podcastTitleTxt;
    private TextView episodeTitleTxt;
    private SeekBar seekBar;

    boolean isFirstInstance = true;

    /**
     * Static factory method for creating a new instance of EpisodePlayerFragment
     * @param episode The PodcastEpisode object to play audio from
     * @param podcast The Podcast that the PodcastEpisode object belongs to
     * @return Returns a new instance of fragment
     */
    public static EpisodePlayerFragment newInstance(PodcastEpisode episode, Podcast podcast) {
        EpisodePlayerFragment fragment = new EpisodePlayerFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("podcast_episode", episode);
        bundle.putParcelable("podcast", podcast);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get bundle from savedInstanceState if the fragment
        // is being recreated, or from getArguments if new fragment
        Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getArguments();
        }

        episode = bundle.getParcelable("podcast_episode");
        podcast = bundle.getParcelable("podcast");
        controllerCallback = new ControllerCallback();
        startPlaybackService();
    }

    /**
     * This starts the MediaBrowserService which is responsible for playback
     * of the audio, and sets the media to play
     */
    private void startPlaybackService() {
        // Start service
        getActivity().startService(new Intent(getActivity(), EpisodePlaybackService.class));
        mediaBrowser = new MediaBrowserCompat(getActivity(), new ComponentName(getActivity(), EpisodePlaybackService.class), new ConnectionCallback(), null);
        // Set media to play
        MediaDescriptionCompat.Builder builder = new MediaDescriptionCompat.Builder();
        builder.setTitle(podcast.getTitle());
        builder.setDescription(episode.getTitle());
        builder.setMediaUri(Uri.parse(episode.getAudioUrl()));
        builder.setMediaId("mediaId");
        builder.setIconUri(Uri.parse(podcast.getImage()));
        EpisodePlaybackService.setMedia(builder.build());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_episode_player, container, false);

        // Find elements
        ImageView playerImg = view.findViewById(R.id.player_img);
        progressBar = view.findViewById(R.id.player_progressbar);
        rewindBtn = view.findViewById(R.id.player_rewind_btn);
        playBtn = view.findViewById(R.id.player_play_btn);
        forwardBtn = view.findViewById(R.id.player_forward_btn);
        durationTxt = view.findViewById(R.id.player_duration);
        podcastTitleTxt = view.findViewById(R.id.player_podcast_title);
        episodeTitleTxt = view.findViewById(R.id.player_episode_title);
        seekBar = view.findViewById(R.id.player_seekbar);

        podcastTitleTxt.setText(podcast.getTitle());
        episodeTitleTxt.setText(episode.getTitle());

        // Load podcast image
        Glide.with(this)
                .load(podcast.getImage())
                .transition(withCrossFade())
                .apply(new RequestOptions().centerCrop())
                .into(playerImg);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("podcast_episode", episode);
        outState.putParcelable("podcast", podcast);
    }

    @Override
    public void onStart() {
        super.onStart();
        progressBar.setVisibility(View.VISIBLE);

        // Connect the activity to the MediaBrowserService
        mediaBrowser.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister callbacks
        MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
        if (controller != null) {
            controller.getTransportControls().sendCustomAction("StopUpdater", null);
            controller.unregisterCallback(controllerCallback);
        }
        // Disconnect activity from the MediaBrowserService
        mediaBrowser.disconnect();
    }

    /**
     * This binds all the UI elements used for controlling the player to their
     * actions.
     */
    private void bindPlayerControls() {
        // Set listener for rewind button
        rewindBtn.setOnClickListener(v -> {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            controller.getTransportControls().rewind();
        });

        // Set listener for play/pause button
        playBtn.setOnClickListener(v -> {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            int state = controller.getPlaybackState().getState();
            if (state == PlaybackStateCompat.STATE_PLAYING) {
                controller.getTransportControls().pause();
            } else if (state == PlaybackStateCompat.STATE_PAUSED) {
                controller.getTransportControls().play();
            } else if (state == PlaybackStateCompat.STATE_STOPPED) {
                progressBar.setVisibility(View.VISIBLE);
                startPlaybackService();
                mediaBrowser.connect();
            }
        });

        // Set listener for forward button
        forwardBtn.setOnClickListener(v -> {
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            controller.getTransportControls().fastForward();
        });

        // Set listener for scrollbar. Only onStopTrackingTouch is overridden so that
        // the action is only triggered when an actual user seeks in the audio
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
                long milliProgress = (long) seekBar.getProgress() * 1000;
                controller.getTransportControls().seekTo(milliProgress);
            }
        });
    }

    /**
     * Callback for when the activity is connected to the MediaBrowserService
     */
    private class ConnectionCallback extends MediaBrowserCompat.ConnectionCallback {
        @Override
        public void onConnected() {
            MediaSessionCompat.Token token = mediaBrowser.getSessionToken();

            // Check if the fragment is being returned to foreground, if it is
            // do not restart playback of the audio, but just register the callbacks
            MediaControllerCompat controller = MediaControllerCompat.getMediaController(getActivity());
            if (!isFirstInstance && controller.getPlaybackState().getState() != PlaybackStateCompat.STATE_STOPPED) {
                controller.registerCallback(controllerCallback);
                controller.getTransportControls().sendCustomAction("StartUpdater", null);
                return;
            }

            try {
                // Initialize MediaController for handling of media controls
                MediaControllerCompat mediaController = new MediaControllerCompat(getActivity(), token);
                MediaControllerCompat.setMediaController(getActivity(), mediaController);
                // Tell the service to prepare the audio for playback
                mediaController.getTransportControls().prepare();

                // Set callback for playbackstate changes so we can update UI elements
                mediaController.registerCallback(controllerCallback);
                isFirstInstance = false;
            } catch (RemoteException e) {
                Log.e("EpisodePlayerFragment", Log.getStackTraceString(e));
            }
        }

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
        }

        @Override
        public void onConnectionFailed() {
            super.onConnectionFailed();
        }
    }

    /**
     * Callback which is fired when there are changes in the playbackstate, such as
     * when audio is paused, buffering, position changes etc
     */
    private class ControllerCallback extends MediaControllerCompat.Callback {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            if (state.getState() == EpisodePlaybackService.STATE_PREPARED) {
                // Audio is ready to play
                bindPlayerControls();
                MediaControllerCompat.getMediaController(getActivity()).getTransportControls().play();
                MediaControllerCompat.getMediaController(getActivity()).getTransportControls().sendCustomAction("StartUpdater", null);
                progressBar.setVisibility(View.GONE);
            } else if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                // Audio is playing
                progressBar.setVisibility(View.GONE);

                // Get duration and position in the audio
                final long duration = MediaControllerCompat.getMediaController(getActivity()).getMetadata().getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
                final long position = state.getPosition();

                // Update seekbar
                seekBar.setMax((int) duration / 1000);
                seekBar.setProgress((int) state.getPosition() / 1000);

                // Update duration text
                // TODO: Maybe improve
                final String formattedDuration = String.format(Locale.getDefault(), "%02d:%02d/%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(position), TimeUnit.MILLISECONDS.toSeconds(position) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(position)),
                        TimeUnit.MILLISECONDS.toMinutes(duration), TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
                durationTxt.setText(formattedDuration);

                playBtn.setImageDrawable(getActivity().getDrawable(R.drawable.ic_pause_24dp));
            } else if (state.getState() == PlaybackStateCompat.STATE_BUFFERING) {
                // Audio is buffering
                progressBar.setVisibility(View.VISIBLE);
            } else if (state.getState() == PlaybackStateCompat.STATE_PAUSED) {
                // Audio is paused
                playBtn.setImageDrawable(getActivity().getDrawable(R.drawable.ic_play_arrow_24dp));
            } else if (state.getState() == PlaybackStateCompat.STATE_STOPPED) {
                // Audio is stopped, disconnect the activity from the playback service
                playBtn.setImageDrawable(getActivity().getDrawable(R.drawable.ic_play_arrow_24dp));
                mediaBrowser.disconnect();
            }
        }
    }
}
