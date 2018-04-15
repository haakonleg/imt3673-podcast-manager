package haakoleg.imt3673_podcast_manager.tasks;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import haakoleg.imt3673_podcast_manager.R;
import haakoleg.imt3673_podcast_manager.utils.Messages;

/**
 * Abstract class which is supposed to be implemented by other tasks. Tasks extending this class
 * overrides doTask() which is the task itself. This method returns either TASK_SUCCESSFUL or an
 * error code.
 * @param <T> The type of resultObject which is passed to the UI thread when the task is finished
 */
public abstract class Task<T> implements Runnable {
    static final int SUCCESSFUL = 0;
    static final int ERROR_NO_INTERNET = 1;
    static final int ERROR_DOWNLOAD = 2;
    static final int ERROR_PARSE = 3;
    static final int ERROR_SQLITE = 4;

    private final Handler mainHandler;
    private final OnSuccessListener<T> successListener;
    private final OnErrorListener errorListener;

    T resultObject;

    Task(OnSuccessListener<T> successListener, OnErrorListener errorListener) {
        // Get handler for UI thread
        mainHandler = new Handler(Looper.getMainLooper());
        this.successListener = successListener;
        this.errorListener = errorListener;
    }

    public static void errorHandler(Context context, int errorCode) {
        switch (errorCode) {
            case ERROR_NO_INTERNET:
                Messages.showError(context, context.getString(R.string.error_no_internet), null);
                break;
            case ERROR_DOWNLOAD:
                Messages.showError(context, context.getString(R.string.error_download), null);
                break;
            case ERROR_PARSE:
                Messages.showError(context, context.getString(R.string.error_parse), null);
                break;
            case ERROR_SQLITE:
                Messages.showError(context, context.getString(R.string.error_sqlite), null);
                break;
            default:
                Messages.showError(context, context.getString(R.string.error_unknown), null);
                break;
        }
    }

    /**
     * Calls the child classes doTask() method, which returns TASK_SUCCESSFUL if the task was
     * without error, or returns an error code. In both cases the callbacks to UI thread are fired
     * using the registered OnSuccessListener or OnErrorListener
     */
    @Override
    public void run() {
        int result = doTask();
        // Task was successful, call back to UI thread
        if (result == SUCCESSFUL) {
            mainHandler.post(() -> successListener.onSuccess(resultObject));
        } else {
            // Task returned an error
            mainHandler.post(() -> errorListener.onError(result));
        }
    }

    // Must be overridden by tasks extending this class
    protected abstract int doTask();

    public interface OnSuccessListener<T> {
        void onSuccess(T result);
    }

    public interface OnErrorListener {
        void onError(int error);
    }
}
