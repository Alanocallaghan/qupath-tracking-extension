package qupath.extension.tracking.tracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Alan O'Callaghan
 **/
public class BoundsFeatures implements Serializable {

    private final TrackerFeatures features;
    private final ViewTracker tracker;
    TrackerFeatureList boundsFixations;
    TrackerFeatureList zoomPeaks;
    TrackerFeatureList slowPans;
    double slowPanSpeedThreshold;
    double slowPanTimeThreshold;
    long boundsFixationThreshold;
    int zoomPeakThreshold;

    BoundsFeatures(TrackerFeatures features) {
        this.features = features;
        this.tracker = features.getTracker();
        this.slowPans = findSlowPans();
        this.zoomPeaks = findZoomPeaks();
        this.boundsFixations = findBoundsFixations();
    }


    /**
     * Finds local minima in an array.
     * To be used to find areas of high zoom, because zoom here is defined
     * using area of bounding rectangle.
     * Could also use to find areas of low zoom by finding maxima.
     *
     * @return void
     */
    TrackerFeatureList findZoomPeaks() {

        TrackerFeatureList indList = new TrackerFeatureList();
        TrackerFeature inds = new TrackerFeature(tracker);
        double[] zoomArray = features.getZoomArray();
//        NB: ZOOM HERE IS AREA SO LOCAL MINIMA ARE REQUIRED, NOT MAXIMA!!!!
        for (int iteration = 0; iteration < zoomPeakThreshold; iteration ++) {
            int[] candidates;
            if (iteration == 0) {
                candidates = java.util.stream.IntStream.range(0, tracker.nFrames()).toArray();
            } else {
                int size = 0;
                for (ArrayList indCounter: indList) {
                    size += indCounter.size();
                }
                candidates = new int[size];
                int counter = 0;
                for (ArrayList list: indList) {
                    for (Object j: list) {
                        candidates[counter] = (int)j;
                        counter ++;
                    }
                }
                indList = new TrackerFeatureList();
            }

            double lastdx = -1;
            double dx;

            for (int counter = 0; counter < candidates.length; counter ++) {
                if (counter > 0) {
                    dx = zoomArray[candidates[counter]] - zoomArray[candidates[counter - 1]];
                } else {
                    dx = 0;
                }

                if (dx > 0) {
                    if (lastdx < 0 || counter == (candidates.length - 1)) {
                        inds.add(candidates[counter - 1]);
                        indList.add(inds);
                    }
                    inds = new TrackerFeature(tracker);
                } else if (dx == 0) {
                    inds.add(candidates[counter]);
                } else if (dx < 0) {
                    inds = new TrackerFeature(tracker);
                    inds.add(candidates[counter]);
                }

                if (dx != 0) {
                    lastdx = dx;
                }
            }
        }
        return indList;
    }




    TrackerFeatureList findSlowPans() {
        TrackerFeatureList slowPans = new TrackerFeatureList();
        TrackerFeature feature = new TrackerFeature(tracker);
        double[] zoomArray = features.getZoomArray();
        double[] boundsSpeedArray = features.getBoundsSpeedArray();

        for (int i = 1; i < zoomArray.length; i++) {
            if (zoomArray[i] == zoomArray[i - 1]) {
                if (boundsSpeedArray[i] < slowPanSpeedThreshold) {
                    feature.add(i);
                } else {
                    if (!feature.isEmpty()) {
                        if (feature.getFrameAtFeatureIndex(feature.size() - 1).getTimestamp() -
                                feature.getFrameAtFeatureIndex(0).getTimestamp() > slowPanTimeThreshold) {
                            slowPans.add(feature);
                            feature = new TrackerFeature(tracker);
                        }
                    }
                }
            } else {
                if (!feature.isEmpty()) {
                    if (feature.getFrameAtFeatureIndex(feature.size() - 1).getTimestamp() -
                            feature.getFrameAtFeatureIndex(0).getTimestamp() > slowPanTimeThreshold) {
                        slowPans.add(feature);
                        feature = new TrackerFeature(tracker);
                    }
                }
            }
        }
        return slowPans;
    }


    TrackerFeatureList findBoundsFixations() {
        TrackerFeatureList fixations = new TrackerFeatureList();
        ViewRecordingFrame currentFrame, previousFrame = null;
        TrackerFeature thisFixation = new TrackerFeature(tracker);

        long timeFixated = 0;
        for (int i = 0; i < tracker.nFrames(); i ++) {
            currentFrame = tracker.getFrame(i);
            if (i != 0) {
                if (currentFrame.getImageBounds().equals(previousFrame.getImageBounds())) {
                    thisFixation.add(i);
                    timeFixated += currentFrame.getTimestamp() - previousFrame.getTimestamp();
                } else {
                    if (timeFixated > boundsFixationThreshold) {
                        fixations.add(thisFixation);
                        thisFixation = new TrackerFeature(tracker);
                    } else {
                        thisFixation = new TrackerFeature(tracker);
                    }
                    timeFixated = 0;
                }
            }
            previousFrame = currentFrame;
        }
        return fixations;
    }


    public TrackerFeatureList getBoundsFixations() {
        return boundsFixations;
    }

    public TrackerFeatureList getZoomPeaks() {
        return zoomPeaks;
    }

    public TrackerFeatureList getSlowPans() {
        return slowPans;
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {

    }

    private void readObjectNoData()
            throws ObjectStreamException {

    }

    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("zoom_peaks", zoomPeaks.toJSON(true));
        object.add("slow_pans", slowPans.toJSON(true));
        object.add("bounds_fixations", boundsFixations.toJSON(true));
        return object;
    }

}
