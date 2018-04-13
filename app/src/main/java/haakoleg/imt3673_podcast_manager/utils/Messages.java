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

    public static void showConfirmationDialog(Context context, String title, String message, AlertDialog.OnClickListener cb) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(R.string.dialog_no, null);
        builder.setPositiveButton(R.string.dialog_yes, cb);
        builder.create().show();
    }
}
