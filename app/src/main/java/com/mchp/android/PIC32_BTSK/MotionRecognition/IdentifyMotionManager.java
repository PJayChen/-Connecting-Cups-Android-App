package com.mchp.android.PIC32_BTSK.MotionRecognition;

import android.util.Log;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import server.FeatureVector;

/**
 * Created by pjay on 2016/1/26.
 */

/**
 * This class creates pools of background threads for identify Motion.
 * The class is implemented as a singleton; the only way to get an IdentifyMotionManager instance
 * is to call {getInstance}.
 */

public class IdentifyMotionManager {

    // Enable debug info
    public static final boolean DEBUG_IDENTIFY = true;

    // Sets the amount of time an idle thread will wait for a task before terminating
    private static final int KEEP_ALIVE_TIME = 1;

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    private static IdentifyMotionManager sInstance = null;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    // A queue of Runnables
    private final BlockingQueue<Runnable> identifyWorkQueue;

    // A managed pool of background identify threads
    private final ThreadPoolExecutor identifyThreadPool;

    static {
        // The time unit for "keep alive" is in seconds
        KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

        // Creates a single static instance of IdentifyMotionManager
        sInstance = new IdentifyMotionManager();
    }

    private IdentifyMotionManager() {

        /*
         * Creates a work queue for the pool of Thread objects used for identifying, using a linked
         * list queue that blocks when the queue is empty.
         */
        identifyWorkQueue = new LinkedBlockingDeque<Runnable>();

        /*
         * Creates a new pool of Thread objects for the identifyMotion work queue
         */
        identifyThreadPool = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, identifyWorkQueue);

    }

    public static IdentifyMotionManager getInstance() {
        return sInstance;
    }

    public static void startIdentifyingMotion(
            String templateName,
            List<List<FeatureVector>> templateFeatureFrameList,
            List<List<FeatureVector>> testingFeatureFrameList,
            Queue<SimilarTemplate> similarityQueue
            ) {

        // Instance and initial the task for identifying motion.
        IdentifyMotionTask identifyTask = new IdentifyMotionTask(templateName,
                templateFeatureFrameList, testingFeatureFrameList, similarityQueue);

        /*
         * "Executes" the tasks' identifyMotion Runnable in order to identify the motion. If no
         * Threads are available in the thread pool, the Runnable waits in the queue.
         */
        sInstance.identifyThreadPool.execute(identifyTask.getIdentifyMotionRunnable());

        if(DEBUG_IDENTIFY)
            Log.d("THREAD_POOL", sInstance.identifyThreadPool.getActiveCount() + " threads is working...");
    }

}
