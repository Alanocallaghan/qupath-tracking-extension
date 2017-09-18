package qupath.extension.tracking.tracker;

import qupath.extension.tracking.TrackerUtils;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;
import qupath.lib.images.servers.ImageServer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

import static qupath.extension.tracking.TrackerUtils.calculateDownsample;
import static qupath.extension.tracking.TrackerUtils.getSpeed;

/**
 * @author Alan O'Callaghan
 * Created by Alan O'Callaghan on 15/03/17.
 */
public class TrackerFeatures {

    private final ImageServer server;
    private final ViewTracker tracker;
    private Rectangle[] boundsArray;
    private Point2D[] eyeArray;
    private Point2D[] cursorArray;
    private double[] eyeSpeedArray = new double[0], zoomArray = new double[0];
    private double[] boundsSpeedArray;
    private TrackerFeatureList slowPans;
    private TrackerFeatureList zoomPeaks;
    private TrackerFeatureList boundsFixations;
    private double slowPanSpeedThreshold;
    private double slowPanTimeThreshold;
    private long boundsFixationThreshold;
    private int zoomPeakThreshold;

    private Fixations eyeFixations;
    private Fixations cursorFixations;

    public Fixations getEyeFixations() {
        return eyeFixations;
    }

    public Fixations getCursorFixations() {
        return cursorFixations;
    }

    public TrackerFeatures(ViewTracker tracker, ImageServer server) {
        this.server = server;
        this.tracker = tracker;
        if (tracker != null) {
            this.boundsArray = makeBounds();
            this.boundsSpeedArray = calculateBoundsSpeedArray();
            this.cursorArray = makeCursor();
            this.zoomArray = generateBoundsZoomArray();
            this.zoomPeaks = findZoomPeaks();
            this.slowPans = findSlowPans();
            this.boundsFixations = findBoundsFixations();
            generateEyeArray();
            eyeFixations = new Fixations(this, "eye", "IVT");
            cursorFixations = new Fixations(this, "cursor", "IVT");
        }

    }


    private Point2D[] makeCursor() {
        int nFrames = tracker.nFrames();
        Point2D[] point2Ds = new Point2D[nFrames];

        for (int i = 0; i < nFrames; i++) {
            Point2D currentCursor = tracker.getFrame(i).getCursorPosition();

            if (currentCursor != null && !(currentCursor.getX() == 0 && currentCursor.getY() == 0)) {
                point2Ds[i] = currentCursor;
            } else {
                point2Ds[i] = null;
            }
        }
        return point2Ds;
    }

    private double[] generateBoundsZoomArray() {
        int nFrames = tracker.nFrames();
        ViewRecordingFrame currentFrame;
        double[] zoomArray = new double[nFrames];
        Dimension imageVisible;
        for (int i = 0; i < nFrames; i++) {
            currentFrame = tracker.getFrame(i);
//            double currentZoom = TrackerUtils.calculateZoom(currentFrame.getImageBounds(), currentFrame.getSize(), this.getServer());
            double currentZoom = calculateDownsample(currentFrame.getImageBounds(), currentFrame.getSize());
            zoomArray[i] = currentZoom;
        }
        return zoomArray;
    }

    private void generateEyeArray() {
        if (!tracker.hasEyeTrackingData()) {
            return;
        }

        ViewRecordingFrame currentFrame;
        ViewRecordingFrame previousFrame = null;

        Point2D previousEye = null;
        Point2D currentEye;
        int nFrames = tracker.nFrames();
        Point2D[] point2Ds = new Point2D[nFrames];

        eyeSpeedArray = new double[nFrames];

        for (int i = 0; i < nFrames; i++) {
            currentFrame = tracker.getFrame(i);
            currentEye = tracker.getFrame(i).getEyePosition();
            if (currentEye != null && previousEye != null && previousFrame!=null) {
                eyeSpeedArray[i] = getSpeed(currentFrame, previousFrame, TrackerUtils.SpeedType.EYE)
                        / calculateDownsample(currentFrame.getImageBounds(), currentFrame.getSize());
            }
            if (currentEye != null && !(currentEye.getX() == 0 && currentEye.getY() == 0)) {
                point2Ds[i] = currentEye;
            }
            previousEye = currentEye;
            previousFrame = currentFrame;
        }
        eyeArray = point2Ds;
    }

    private Rectangle[] makeBounds() {
        int nFrames = tracker.nFrames();
        ViewRecordingFrame currentFrame;
        Rectangle rect;
        Rectangle[] rects = new Rectangle[nFrames];

        for (int i = 0; i < nFrames; i++) {
            currentFrame = tracker.getFrame(i);
            rect = currentFrame.getImageBounds();
            rects[i] = rect;
        }
        return rects;
    }

    private double[] calculateBoundsSpeedArray() {
        int nFrames = tracker.nFrames();
        double[] array = new double[nFrames];
        for (int i = 0; i < nFrames; i++) {
            if (i != 0) {
                double speed = getSpeed(tracker.getFrame(i), tracker.getFrame(i - 1),
                        TrackerUtils.SpeedType.BOUNDS) / calculateDownsample(tracker.getFrame(i - 1).getImageBounds(),
                        tracker.getFrame(i - 1).getSize());
                if (!Double.isNaN(speed) && Double.isFinite(speed)) {
                    array[i] = speed;
                }
            }
        }
        return array;
    }

    public double[] getEyeSpeedArray() { return Arrays.copyOf(eyeSpeedArray, eyeSpeedArray.length);}

    public double[] getZoomArray() { return Arrays.copyOf(zoomArray, zoomArray.length); }

    public ViewTracker getTracker() {
        return this.tracker;
    }

    public ImageServer getServer() {
        return this.server;
    }

    public Point2D[] getCursorArray() {
        return this.cursorArray;
    }

    public Rectangle[] getBoundsArray() {
        return this.boundsArray;
    }

    public Point2D[] getEyeArray() {
        return this.eyeArray;
    }

    public Point2D[] getArray(Fixations.FeatureType featureType) {
        Point2D[] array;
        if (featureType == Fixations.FeatureType.CURSOR) {
            array = getCursorArray();
        } else {
            array = getEyeArray();
        }
        return array;
    }


    /**
     * Finds local minima in an array.
     * To be used to find areas of high zoom, because zoom here is defined
     * using area of bounding rectangle.
     * Could also use to find areas of low zoom by finding maxima.
     *
     * @return void
     */
    private TrackerFeatureList findZoomPeaks() {

        ArrayList<Integer> inds = new ArrayList<>();
        ArrayList<ArrayList> indList = new ArrayList<>();

//        NB: ZOOM HERE IS AREA SO LOCAL MINIMA ARE REQUIRED, NOT MAXIMA!!!!
        for (int iteration = 0; iteration < zoomPeakThreshold; iteration ++) {
            int[] candidates;
            if (iteration == 0) {
                candidates = java.util.stream.IntStream.range(0, tracker.nFrames() - 1).toArray();
            } else {
                candidates = new int[indList.size()];
                for (int i = 0; i < indList.size(); i ++) {
                    candidates[i] = (int)(indList.get(i).get(0));
                }
                indList = new ArrayList<>();
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
                    inds = new ArrayList<>();
                } else if (dx == 0) {
                    inds.add(candidates[counter]);
                } else if (dx < 0) {
                    inds = new ArrayList<>();
                    inds.add(candidates[counter]);
                }

                if (dx != 0) {
                    lastdx = dx;
                }
            }
        }
        TrackerFeatureList zoomPeaks = new TrackerFeatureList();
        for (ArrayList<Integer> list: indList) {
            TrackerFeature zoomPeak = new TrackerFeature();
            for (int i: list) {
                zoomPeak.add(tracker.getFrame(i));
            }
            zoomPeaks.add(zoomPeak);
        }
        return zoomPeaks;
    }





    private TrackerFeatureList findSlowPans() {
        ArrayList<ArrayList> slowPanInds = new ArrayList<>(0);
        TrackerFeatureList slowPans = new TrackerFeatureList();
        TrackerFeature feature = new TrackerFeature();


        for (int i = 1; i < zoomArray.length; i++) {
            if (zoomArray[i] == zoomArray[i - 1]) {
                if (boundsSpeedArray[i] < slowPanSpeedThreshold) {
                    feature.add(tracker.getFrame(i));
                } else {
                    if (feature.size() != 0) {
                        if (feature.get(feature.size() - 1).getTimestamp() - feature.get(0).getTimestamp() > slowPanTimeThreshold) {
                            slowPans.add(feature);
                            feature = new TrackerFeature();
                        }
                    }
                }
            } else {
                if (feature.size() != 0) {
                    if (feature.get(feature.size() - 1).getTimestamp() - feature.get(0).getTimestamp() > slowPanTimeThreshold) {
                        slowPans.add(feature);
                        feature = new TrackerFeature();
                    }
                }
            }
        }
        return slowPans;
    }

    private TrackerFeatureList findBoundsFixations() {
        TrackerFeatureList fixations = new TrackerFeatureList();
        ViewRecordingFrame currentFrame, previousFrame = null;
        TrackerFeature thisFixation = new TrackerFeature();

        long timeFixated = 0;
        for (int i = 0; i < tracker.nFrames(); i ++) {
            currentFrame = tracker.getFrame(i);
            if (i != 0) {
                if (currentFrame.getImageBounds().equals(previousFrame.getImageBounds())) {
                    thisFixation.add(currentFrame);
                    timeFixated += currentFrame.getTimestamp() - previousFrame.getTimestamp();
                } else {
                    if (timeFixated > boundsFixationThreshold) {
                        fixations.add(thisFixation);
                        thisFixation = new TrackerFeature();
                    } else {
                        thisFixation = new TrackerFeature();
                    }
                    timeFixated = 0;
                }
            }
            previousFrame = currentFrame;
        }
        return fixations;
    }

    public TrackerFeatureList getZoomPeaks() {
        return zoomPeaks;
    }

    public TrackerFeatureList getSlowPans() {
        return slowPans;
    }

    public TrackerFeatureList getBoundsFixations() {
        return boundsFixations;
    }

    public void setSlowPanTimeThreshold(double slowPanTimeThreshold) {
        this.slowPanTimeThreshold = slowPanTimeThreshold;
        this.slowPans = findSlowPans();
    }

    public void setSlowPanSpeedThreshold(double slowPanSpeedThreshold) {
        this.slowPanSpeedThreshold = slowPanSpeedThreshold;
        this.slowPans = findSlowPans();
    }

    public void setBoundsFixationThreshold(long boundsFixationThreshold) {
        this.boundsFixationThreshold = boundsFixationThreshold;
        this.boundsFixations = findBoundsFixations();
    }

    public void setZoomPeakThreshold(int zoomPeakThreshold) {
        this.zoomPeakThreshold = zoomPeakThreshold;
        this.zoomPeaks = findZoomPeaks();
    }
}
