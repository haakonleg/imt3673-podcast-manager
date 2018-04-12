package haakoleg.imt3673_podcast_manager;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import haakoleg.imt3673_podcast_manager.models.Comment;
import haakoleg.imt3673_podcast_manager.models.Podcast;

public class DisplayPodcastFragment extends Fragment implements ChildEventListener {
    private Podcast podcast;
    private CommentsRecyclerAdapter adapter;

    private FloatingActionButton addFab;
    private FloatingActionButton commentFab;
    private ImageView podcastImg;
    private TextView podcastTitleTxt;
    private TextView podcastDescriptionTxt;
    private RecyclerView commentsRecycler;

    public static DisplayPodcastFragment newInstance(Podcast podcast) {
        DisplayPodcastFragment fragment = new DisplayPodcastFragment();
        Bundle bundle = new Bundle();
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

        podcast = bundle.getParcelable("podcast");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout and find elements
        View view = inflater.inflate(R.layout.fragment_display_podcast, container, false);
        addFab = view.findViewById(R.id.podcast_add_fab);
        commentFab = view.findViewById(R.id.podcast_comment_fab);
        podcastImg = view.findViewById(R.id.podcast_img);
        podcastTitleTxt = view.findViewById(R.id.podcast_title_txt);
        podcastDescriptionTxt = view.findViewById(R.id.podcast_description_txt);
        commentsRecycler = view.findViewById(R.id.podcast_comments_recycler);

        // Set up elements
        Glide.with(this).load(podcast.getImage()).into(podcastImg);
        podcastTitleTxt.setText(podcast.getTitle());
        podcastDescriptionTxt.setText(podcast.getDescription());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        commentFab.setOnClickListener(v -> {
            PostCommentDialogFragment dialog = PostCommentDialogFragment.newInstance(Integer.toHexString(podcast.hashCode()));
            dialog.show(getActivity().getSupportFragmentManager(), "PostCommentDialog");
        });

        // Create adapter for comments recyclerview
        adapter = new CommentsRecyclerAdapter(new ArrayList<>());
        commentsRecycler.setAdapter(adapter);

        // Start child event listener for displaying comments
        String podcastId = Integer.toHexString(podcast.hashCode());
        DatabaseReference dbRef =
                FirebaseDatabase.getInstance().getReference().child("comments").child(podcastId);
        dbRef.addChildEventListener(this);
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        Comment comment = dataSnapshot.getValue(Comment.class);
        adapter.addComment(comment);
    }

    @Override
    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onChildRemoved(DataSnapshot dataSnapshot) {

    }

    @Override
    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

    }

    @Override
    public void onCancelled(DatabaseError databaseError) {

    }
}
