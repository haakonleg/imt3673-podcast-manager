package haakoleg.imt3673_podcast_manager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

public class PostCommentDialogFragment extends DialogFragment {
    private View view;
    private EditText commentInput;
    private RatingBar ratingBar;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_post_comment, null);
        commentInput = view.findViewById(R.id.comment_input);
        ratingBar = view.findViewById(R.id.rating_input);
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(R.string.post_comment)
                .setView(view)
                .setPositiveButton(getString(R.string.comment_post), (dialog, which) -> {

                })
                .setNegativeButton(getString(R.string.dialog_cancel), null);

        return builder.create();
    }
}
