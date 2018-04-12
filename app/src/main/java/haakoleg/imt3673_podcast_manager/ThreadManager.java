package haakoleg.imt3673_podcast_manager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class used for managing a pool of threads, contains method execute() which
 * adds a runnable to the queue and executes the task when a thread is available
 */
public class ThreadManager {
    private static ThreadManager singleInstance;

    private final BlockingQueue<Runnable> taskQueue;
    private final ExecutorService poolExecutor;

    public static ThreadManager get() {
        if (singleInstance == null) {
            singleInstance = new ThreadManager();
        }
        return singleInstance;
    }

    /**
     * Private constructor which instantiates the parameters and thread pool
     */
    private ThreadManager() {
        taskQueue = new LinkedBlockingQueue<>();
        poolExecutor = Executors.newCachedThreadPool();
    }

    public void execute(Runnable task) {
        poolExecutor.execute(task);
    }

    public void interruptAll() {
        poolExecutor.shutdownNow();
        singleInstance = null;
    }
}
