package haakoleg.imt3673_podcast_manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import haakoleg.imt3673_podcast_manager.models.Comment;

public class PostCommentDialogFragment extends DialogFragment {
    private View view;
    private EditText commentInput;
    private RatingBar ratingBar;

    private String podcastId;

    public static PostCommentDialogFragment newInstance(String podcastId) {
        PostCommentDialogFragment fragment = new PostCommentDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("id", podcastId);
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

        podcastId = bundle.getString("id");

        view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_post_comment, null);
        commentInput = view.findViewById(R.id.comment_input);
        ratingBar = view.findViewById(R.id.rating_input);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(R.string.post_comment)
                .setView(view)
                .setPositiveButton(getString(R.string.comment_post), (dialog, which) -> {
                    String comment = commentInput.getText().toString();
                    int rating = (int) ratingBar.getRating();
                    postComment(comment, rating);
                })
                .setNegativeButton(getString(R.string.dialog_cancel), null);

        return builder.create();
    }

    private void postComment(String comment, int rating) {
        if (comment.length() < 3) {
            Toast.makeText(getActivity(),  "Comment must be 3 characters or longer", Toast.LENGTH_SHORT).show();
            return;
        } else if (rating == 0) {
            Toast.makeText(getActivity(),  "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Post comment to firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dbRef =
                FirebaseDatabase.getInstance().getReference().child("comments").child(podcastId);
        dbRef.child(user.getUid()).setValue(new Comment(user.getDisplayName(), comment, rating));

        // Set rating for podcast
        dbRef = FirebaseDatabase.getInstance().getReference().child("podcasts").child(podcastId);
        dbRef.child("ratings").child(user.getUid()).setValue(rating);
    }
}
