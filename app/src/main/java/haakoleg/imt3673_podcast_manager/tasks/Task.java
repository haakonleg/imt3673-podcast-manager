package haakoleg.imt3673_podcast_manager.tasks;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * Abstract class which is supposed to be implemented by other tasks. Tasks extending this class
 * overrides doTask() which is the task itself. This method returns either TASK_SUCCESSFUL or an
 * error code.
 * @param <T> The type of resultObject which is passed to the UI thread when the task is finished
 */
public abstract class Task<T> implements Runnable {
    public static int TASK_SUCCESSFUL = 0;

    private Handler mainHandler;
    private OnSuccessListener<T> successListener;
    private OnErrorListener errorListener;

    protected T resultObject;

    protected Task(OnSuccessListener<T> successListener, OnErrorListener errorListener) {
        // Get handler for UI thread
        mainHandler = new Handler(Looper.getMainLooper());
        this.successListener = successListener;
        this.errorListener = errorListener;
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
        if (result == TASK_SUCCESSFUL) {
            if (resultObject == null) {
                Log.e("Task", "WARNING: The task did not set a resultObject but returned successful");
            } else {
                mainHandler.post(() -> {
                    successListener.onSuccess(resultObject);
                });
            }
        } else {
            // Task returned an error
            mainHandler.post(() -> {
                errorListener.onError(result);
            });
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
