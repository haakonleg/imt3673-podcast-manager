package haakoleg.imt3673_podcast_manager;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class AddPodcastDialogFragment extends DialogFragment {
    private PodcastAddedListener listener;
    private View view;
    private EditText urlInput;

    public void setListener(PodcastAddedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        view = inflater.inflate(R.layout.dialog_add_podcast, null);
        urlInput = view.findViewById(R.id.url_input);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_new_podcast);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.dialog_add), (dialog, which) -> {
            String url = urlInput.getText().toString();
            if (!url.isEmpty()) {
                listener.onPodcastAdded(url);
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);
        return builder.create();
    }

    public interface PodcastAddedListener {
        void onPodcastAdded(String url);
    }
}