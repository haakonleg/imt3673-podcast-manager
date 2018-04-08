package haakoleg.imt3673_podcast_manager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class used for managing a pool of threads, contains method execute() which
 * adds a runnable to the queue and executes the task when a thread is available
 */
public class ThreadManager {
    private static final ThreadManager singleInstance;

    private final int NUMBER_OF_THREADS;
    private final int KEEPALIVE;

    private final BlockingQueue<Runnable> taskQueue;
    private final ThreadPoolExecutor poolExecutor;

    static {
        singleInstance = new ThreadManager();
    }

    public static ThreadManager get() {
        return singleInstance;
    }

    /**
     * Private constructor which instantiates the parameters and thread pool
     */
    private ThreadManager() {
        // Set number of threads to number of processor cores
        NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
        // Set thread keep-alive time
        KEEPALIVE = 10;
        taskQueue = new LinkedBlockingQueue<>();
        poolExecutor = new ThreadPoolExecutor(
                NUMBER_OF_THREADS,
                NUMBER_OF_THREADS,
                KEEPALIVE,
                TimeUnit.SECONDS,
                taskQueue);
    }

    public void execute(Runnable task) {
        poolExecutor.execute(task);
    }
}
