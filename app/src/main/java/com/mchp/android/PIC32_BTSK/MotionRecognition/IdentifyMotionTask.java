package com.mchp.android.PIC32_BTSK.MotionRecognition;
import com.mchp.android.PIC32_BTSK.MotionRecognition.IdentifyMotionRunnable.TaskRunnableIdentifyMotionMethods;

import java.util.List;
import java.util.Queue;

import server.FeatureVector;

/**
 * Created by pjay on 2016/1/27.
 */

/**
 * This class manages IdentifyMotionRunnable object.  It doesn't perform the identify;
 * instead, it manages persistent storage for the tasks that do the work.
 * It does this by implementing the interfaces that the identifyMotion classes define, and
 * then passing itself as an argument to the constructor of a identifyMotion object.
 * In effect, this allows IdentifyMotionTask to start on a Thread,
 * run a identify in a delegate object, then run a identifyMotion process.
 */

public class IdentifyMotionTask implements  TaskRunnableIdentifyMotionMethods{

    // Fields for identifying motion
    private List<List<FeatureVector>> templateFeatureFrameList;
    private List<List<FeatureVector>> testingFeatureFrameList;
    private Queue<SimilarTemplate> similarityQueue;
    private String templateName;

    // Fields containing reference to the runnable object that handle identifying of the motion.
    private Runnable mIdentifyMotionRunnable;

    /*
     * An object that contains the ThreadPool singleton.
     */
    private static IdentifyMotionManager sIdentifyMotionManager;

    public IdentifyMotionTask() {
        this.mIdentifyMotionRunnable = new IdentifyMotionRunnable(this);
        sIdentifyMotionManager = IdentifyMotionManager.getInstance();
    }

    public IdentifyMotionTask(String templateName,
                              List<List<FeatureVector>> templateFeatureFrameList,
                              List<List<FeatureVector>> testingFeatureFrameList,
                              Queue<SimilarTemplate> similarityQueue
    ) {
        this.templateFeatureFrameList = templateFeatureFrameList;
        this.testingFeatureFrameList = testingFeatureFrameList;
        this.similarityQueue = similarityQueue;
        this.templateName = templateName;
        this.mIdentifyMotionRunnable = new IdentifyMotionRunnable(this);
        sIdentifyMotionManager = IdentifyMotionManager.getInstance();
    }

    public Runnable getIdentifyMotionRunnable() {
        return mIdentifyMotionRunnable;
    }

    @Override
    public void setTemplateFeatureFrameList(List<List<FeatureVector>> templateFeatureFrameList) {
        this.templateFeatureFrameList = templateFeatureFrameList;
    }

    @Override
    public void setTestingFeatureFrameList(List<List<FeatureVector>> testingFeatureFrameList) {
        this.testingFeatureFrameList = testingFeatureFrameList;
    }

    @Override
    public List<List<FeatureVector>> getTemplateFeatureFrameList() {
        return this.templateFeatureFrameList;
    }

    @Override
    public List<List<FeatureVector>> getTestingFeatureFrameList() {
        return this.testingFeatureFrameList;
    }

    @Override
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public String getTemplateName() {
        return this.templateName;
    }

    @Override
    public void setResultSimilarityQueue(Queue<SimilarTemplate> similarityQueue) {
        this.similarityQueue = similarityQueue;
    }

    @Override
    public Queue<SimilarTemplate> getResultSimilarityQueue() {
        return this.similarityQueue;
    }
}
