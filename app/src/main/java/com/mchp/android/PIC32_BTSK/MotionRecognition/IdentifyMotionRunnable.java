package com.mchp.android.PIC32_BTSK.MotionRecognition;

import android.util.Log;

import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import net.sf.javaml.distance.fastdtw.dtw.FastDTW;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;

import java.util.List;
import java.util.Queue;

import server.FeatureVector;

/**
 * Created by pjay on 2016/1/26.
 */

/**
 * This runnable identify motions.
 *
 * Objects of this class are instantiated and managed by instances of IdentifyMotionManager, which
 * implements the methods {@link TaskRunnableIdentifyMotionMethods}.
 * IdentifyMotionTask objects call
 * {@link #IdentifyMotionRunnable(TaskRunnableIdentifyMotionMethods) IdentifyMotionRunnable()}
 * with themselves as the argument.
 * In effect, an IdentifyMotionTask object and a
 * IdentifyMotionRunnable object communicate through the fields of the IdentifyMotionTask.
 *
 */

public class IdentifyMotionRunnable implements Runnable {

    // Defines a field that contains the calling object of type PhotoTask.
    final TaskRunnableIdentifyMotionMethods mIdentifyMotionTask;

    /**
     *
     * An interface that defines methods that IdentifyMotionTask implements. An instance of
     * IdentifyMotionTask passes itself to an IdentifyMotionRunnable instance through the
     * IdentifyMotionRunnable constructor, after which the two instances can access each other's
     * variables.
     */
    interface TaskRunnableIdentifyMotionMethods {

        void setTemplateFeatureFrameList (List<List<FeatureVector>> templateFeatureFrameList);
        void setTestingFeatureFrameList(List<List<FeatureVector>> testingFeatureFrameList);

        List<List<FeatureVector>> getTemplateFeatureFrameList ();
        List<List<FeatureVector>> getTestingFeatureFrameList();

        void setTemplateName(String templateName);
        String getTemplateName();

        void setResultSimilarityQueue(Queue<SimilarTemplate> similarityQueue);
        Queue<SimilarTemplate> getResultSimilarityQueue();

    }

    public IdentifyMotionRunnable(TaskRunnableIdentifyMotionMethods mIdentifyMotionTask) {
        this.mIdentifyMotionTask = mIdentifyMotionTask;
    }

    // Z-score normalization
    private FeatureVector normalization(FeatureVector fv, String type) {

        FeatureVector normalizedFV = null;

        if (type.equals("Z_SCORE")) {
            if (MotionRecognitionThread.DEBUG_PARSING)
                System.out.println("Z_SCORE");
            double average = (fv.getAcceleration_x() + fv.getAcceleration_y() + fv.getAcceleration_z()) / 3;

            double standardDeviation = Math
                    .sqrt((Math.pow(fv.getAcceleration_x() - average, 2) + Math.pow(fv.getAcceleration_y() - average, 2)
                            + Math.pow(fv.getAcceleration_z() - average, 2)) / (3 - 1));

            // The result is multiply 10 to be able to make it as a integer
            // number.
            int zScore_x = (int) ((fv.getAcceleration_x() - average) / standardDeviation * 10);
            int zScore_y = (int) ((fv.getAcceleration_y() - average) / standardDeviation * 10);
            int zScore_z = (int) ((fv.getAcceleration_z() - average) / standardDeviation * 10);

            // normalized data. magnitude just bypass to output.
            normalizedFV = new FeatureVector(zScore_x, zScore_y, zScore_z, fv.getAcceleration_magnitude());

        } else if (type.equals("NORM")) {
            if (MotionRecognitionThread.DEBUG_PARSING)
                System.out.println("NORM");
            int norm = (int) (Math.sqrt(Math.pow(fv.getAcceleration_x(), 2) + Math.pow(fv.getAcceleration_y(), 2)
                    + Math.pow(fv.getAcceleration_z(), 2)));
            int norm_x = fv.getAcceleration_x() / norm * 10;
            int norm_y = fv.getAcceleration_y() / norm * 10;
            int norm_z = fv.getAcceleration_z() / norm * 10;

            normalizedFV = new FeatureVector(norm_x, norm_y, norm_z, norm);
        }
        if (MotionRecognitionThread.DEBUG_PARSING)
            System.out.println("None");
        return normalizedFV;
    }

    private double calSimilarity(List<List<FeatureVector>> templateFeatureFrameList,
                                 List<List<FeatureVector>> testingFeatureFrameList) {

        // ----- extract feature vectors from templateFeatureFrameList -----
        double[] d_x = new double[templateFeatureFrameList.size() * templateFeatureFrameList.get(0).size()],
                d_y = new double[templateFeatureFrameList.size() * templateFeatureFrameList.get(0).size()],
                d_z = new double[templateFeatureFrameList.size() * templateFeatureFrameList.get(0).size()],
                d_m = new double[templateFeatureFrameList.size() * templateFeatureFrameList.get(0).size()];

        // extract features frame by frame
        for (int j = 0; j < templateFeatureFrameList.size(); j++) {
            // extracting features from each frame
            for (int i = 0; i < templateFeatureFrameList.get(j).size(); i++) {

                // Normalized acceleration data
                FeatureVector fv = normalization(
                        new FeatureVector(templateFeatureFrameList.get(j).get(i).getAcceleration_x(),
                                templateFeatureFrameList.get(j).get(i).getAcceleration_y(),
                                templateFeatureFrameList.get(j).get(i).getAcceleration_z(),
                                templateFeatureFrameList.get(j).get(i).getAcceleration_magnitude()),
                        "Z_SCORE");
                d_x[j * templateFeatureFrameList.get(j).size() + i] = (double) fv.getAcceleration_x();
                d_y[j * templateFeatureFrameList.get(j).size() + i] = (double) fv.getAcceleration_y();
                d_z[j * templateFeatureFrameList.get(j).size() + i] = (double) fv.getAcceleration_z();
                d_m[j * templateFeatureFrameList.get(j).size() + i] = (double) fv.getAcceleration_magnitude();

                // Raw acceleration data
                // d_x[j * templateFeatureFrameList.get(j).size() + i] =
                // (double) templateFeatureFrameList.get(j).get(i)
                // .getAcceleration_x();
                // d_y[j * templateFeatureFrameList.get(j).size() + i] =
                // (double) templateFeatureFrameList.get(j).get(i)
                // .getAcceleration_y();
                // d_z[j * templateFeatureFrameList.get(j).size() + i] =
                // (double) templateFeatureFrameList.get(j).get(i)
                // .getAcceleration_z();
                // d_m[j * templateFeatureFrameList.get(j).size() + i] =
                // (double) templateFeatureFrameList.get(j).get(i)
                // .getAcceleration_magnitude();
            }
        }
        // if (DEBUG_SIMILARITY) {
        // for (int i=0;i<d_x.length;i++)
        // System.out.println("[" + d_x[i] + ", " + d_y[i] + ", " + d_z[i] + ",
        // " + d_m[i] + "], ");
        // System.out.println("\n");
        // }
        Instance templateInstance_x = new DenseInstance(d_x);
        Instance templateInstance_y = new DenseInstance(d_y);
        Instance templateInstance_z = new DenseInstance(d_z);
        Instance templateInstance_m = new DenseInstance(d_m);
        // -----------------------------------------------------------------

        // ----- extract feature vectors from testingFeatureFrameList -----
        d_x = new double[testingFeatureFrameList.size() * testingFeatureFrameList.get(0).size()];
        d_y = new double[testingFeatureFrameList.size() * testingFeatureFrameList.get(0).size()];
        d_z = new double[testingFeatureFrameList.size() * testingFeatureFrameList.get(0).size()];
        d_m = new double[testingFeatureFrameList.size() * testingFeatureFrameList.get(0).size()];

        // extract features frame by frame
        for (int j = 0; j < testingFeatureFrameList.size(); j++) {
            // extracting features from each frame
            for (int i = 0; i < testingFeatureFrameList.get(j).size(); i++) {

                // Normalized acceleration data
                FeatureVector fv = normalization(
                        new FeatureVector(testingFeatureFrameList.get(j).get(i).getAcceleration_x(),
                                testingFeatureFrameList.get(j).get(i).getAcceleration_y(),
                                testingFeatureFrameList.get(j).get(i).getAcceleration_z(),
                                testingFeatureFrameList.get(j).get(i).getAcceleration_magnitude()),
                        "Z_SCORE");
                d_x[j * testingFeatureFrameList.get(j).size() + i] = (double) fv.getAcceleration_x();
                d_y[j * testingFeatureFrameList.get(j).size() + i] = (double) fv.getAcceleration_y();
                d_z[j * testingFeatureFrameList.get(j).size() + i] = (double) fv.getAcceleration_z();
                d_m[j * testingFeatureFrameList.get(j).size() + i] = (double) fv.getAcceleration_magnitude();

                // Raw acceleration data
                // d_x[j * testingFeatureFrameList.get(j).size() + i] = (double)
                // testingFeatureFrameList.get(j).get(i)
                // .getAcceleration_x();
                // d_y[j * testingFeatureFrameList.get(j).size() + i] = (double)
                // testingFeatureFrameList.get(j).get(i)
                // .getAcceleration_y();
                // d_z[j * testingFeatureFrameList.get(j).size() + i] = (double)
                // testingFeatureFrameList.get(j).get(i)
                // .getAcceleration_z();
                // d_m[j * testingFeatureFrameList.get(j).size() + i] = (double)
                // testingFeatureFrameList.get(j).get(i)
                // .getAcceleration_magnitude();
            }
        }
        // if (DEBUG_SIMILARITY) {
        // for (int i=0;i<d_x.length;i++)
        // System.out.println("[" + d_x[i] + ", " + d_y[i] + ", " + d_z[i] + ",
        // " + d_m[i] + "], ");
        // System.out.println("\n");
        // }

        Instance testingInstance_x = new DenseInstance(d_x);
        Instance testingInstance_y = new DenseInstance(d_y);
        Instance testingInstance_z = new DenseInstance(d_z);
        Instance testingInstance_m = new DenseInstance(d_m);
        // -----------------------------------------------------------------

        // ----- calculate distance by fastDTW -----
        double dist = (FastDTW.getWarpDistBetween(new TimeSeries(templateInstance_x), new TimeSeries(testingInstance_x),
                30)
                + FastDTW.getWarpDistBetween(new TimeSeries(templateInstance_y), new TimeSeries(testingInstance_y), 30)
                + FastDTW.getWarpDistBetween(new TimeSeries(templateInstance_z), new TimeSeries(testingInstance_z), 30))/3;
//        double dist = FastDTW.getWarpDistBetween(new TimeSeries(templateInstance_m), new TimeSeries(testingInstance_m), 30);

        if (MotionRecognitionThread.DEBUG_SIMILARITY) {
            System.out.printf("Similarity: %d\n", (int) dist);
        }
        // -----------------------------------------

        return dist;
    }

    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        if (IdentifyMotionManager.DEBUG_IDENTIFY)
            Log.d("THREAD_POOL", "Thread " + String.valueOf(Thread.currentThread().getId()) +
                    " executing...");

        // calculate the similarity between testing frames and template frames
        double dist = calSimilarity(mIdentifyMotionTask.getTemplateFeatureFrameList(),
                mIdentifyMotionTask.getTestingFeatureFrameList());

        // store the calculated result into the similarityQueue
        mIdentifyMotionTask.getResultSimilarityQueue().add(
                new SimilarTemplate(mIdentifyMotionTask.getTemplateName(), (int) dist));
    }
}
