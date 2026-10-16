package com.crowdfund.service;

import com.crowdfund.db.ActivityLogDAO;
import com.crowdfund.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple producer / consumer logger that runs on its own thread.
 *
 * <p>The Swing UI threads just call {@link #log(String)} and move on. A single
 * background thread takes messages off a shared queue and writes them to the
 * database. The queue is guarded with {@code synchronized} plus
 * {@code wait()/notify()}, so multiple UI threads can log at the same time
 * without stepping on each other.</p>
 */
public class ActivityLogger {

    private static ActivityLogger instance;

    private final List<String> queue = new ArrayList<>();
    private final ActivityLogDAO activityLogDAO = new ActivityLogDAO();
    private Thread worker;
    private volatile boolean running = true;

    private ActivityLogger() {
        // use getInstance()
    }

    public static synchronized ActivityLogger getInstance() {
        if (instance == null) {
            instance = new ActivityLogger();
        }
        return instance;
    }

    /** Starts the background worker thread (safe to call more than once). */
    public void start() {
        if (worker != null) {
            return;
        }
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                while (running) {
                    String message = nextMessage();
                    if (message != null) {
                        try {
                            activityLogDAO.insert(message);
                        } catch (DataAccessException e) {
                            System.err.println("Activity log failed: " + e.getMessage());
                        }
                    }
                }
            }
        });
        worker.setDaemon(true);
        worker.setName("activity-logger");
        worker.start();
    }

    /** Add a message to the queue. Called from any thread. */
    public synchronized void log(String message) {
        queue.add(message);
        notify(); // wake the worker
    }

    /** The worker waits here until there is something to do. */
    private synchronized String nextMessage() {
        while (queue.isEmpty() && running) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        if (queue.isEmpty()) {
            return null;
        }
        return queue.remove(0);
    }

    /** Stop the worker thread. */
    public void stop() {
        running = false;
        synchronized (this) {
            notify();
        }
    }
}
