package haakoleg.imt3673_podcast_manager.utils;

import android.app.AlertDialog;
import android.content.Context;

import haakoleg.imt3673_podcast_manager.R;

public class Messages {
    public static void showError(Context context, String message, AlertDialog.OnDismissListener cb) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.dialog_error);
        builder.setMessage(message);
        builder.setOnDismissListener(cb);
        builder.create().show();
    }
}
