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
    private TrackerFeatureList boundsFixations;
    private Rectangle[] boundsArray;
    private Point2D[] eyeArray;
    private Point2D[] cursorArray;
    private double[] eyeSpeedArray = new double[0], zoomArray = new double[0];
    private double[] boundsSpeedArray;
    private static double slowPanSpeedThreshold = 100;
    static private double slowPanTimeThreshold = 100;
    private static int boundsFixationThreshold = 1000;
    private TrackerFeatureList slowPans;
    private TrackerFeatureList zoomPeaks;


    public Fixations getEyeFixations() {
        return eyeFixations;
    }

    public Fixations getCursorFixations() {
        return cursorFixations;
    }

    private Fixations eyeFixations;
    private Fixations cursorFixations;

    public TrackerFeatures(ViewTracker tracker, ImageServer server) {
        this.server = server;
        this.tracker = tracker;
        if (tracker != null) {
            this.boundsArray = makeBounds();
            this.boundsSpeedArray = calculateBoundsSpeedArray();
            this.cursorArray = makeCursor();
            addEye();
            generateBoundsArray();
            this.zoomPeaks = findZoomPeaks();
            this.slowPans = findSlowPans();
            this.boundsFixations = findBoundsFixations();
            generateEyeArray();
            eyeFixations = new Fixations(this, "eye", "IVT");
            cursorFixations = new Fixations(this, "cursor", "IVT");
        }

    }

    private void addEye() {
        if (!tracker.hasEyeTrackingData()) {
            return;
        }
        ViewRecordingFrame currentFrame;
        ViewRecordingFrame previousFrame = null;

        Point2D previousEye = null;
        Point2D currentEye;
        int nFrames = tracker.nFrames();
        Point2D[] point2Ds = new Point2D[nFrames];


        double[] eyeSpeedArray = new double[nFrames];

        for (int i = 0; i < nFrames; i++) {
            currentFrame = tracker.getFrame(i);
            currentEye = tracker.getFrame(i).getEyePosition();
            if (currentEye != null && previousEye != null && previousFrame!=null) {
                eyeSpeedArray[i] = getSpeed(currentFrame, previousFrame, TrackerUtils.SpeedType.EYE)
                        / 
                        calculateDownsample(currentFrame.getImageBounds(), currentFrame.getSize());
            }
            if (currentEye != null && !(currentEye.getX() == 0 && currentEye.getY() == 0)) {
                point2Ds[i] = currentEye;
            }
            previousEye = currentEye;
            previousFrame = currentFrame;
        }
        this.eyeSpeedArray = eyeSpeedArray;
        this.eyeArray = point2Ds;
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

    private void generateBoundsArray() {
        int nFrames = tracker.nFrames();

        Rectangle rect;
        ViewRecordingFrame currentFrame;

        this.zoomArray = new double[nFrames];

        Dimension imageVisible;
        for (int i = 0; i < nFrames; i++) {
            currentFrame = tracker.getFrame(i);
            rect = currentFrame.getImageBounds();

            imageVisible = currentFrame.getSize();

            double currentZoom = TrackerUtils.calculateZoom(rect, imageVisible, this.getServer());
            zoomArray[i] = currentZoom;
        }
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
        TrackerFeatureList zoomPeaks = new TrackerFeatureList();
        TrackerFeature zoomPeak = new TrackerFeature();

//        NB: ZOOM HERE IS AREA SO LOCAL MINIMA ARE REQUIRED, NOT MAXIMA!!!!
        double lastdx = 0;
        for (int i = 1; i < boundsArray.length - 1; i++) {
            double dx = zoomArray[i] - zoomArray[i - 1];
//          if decreasing or flat
            if ((dx > 0 || dx == 0) && lastdx < 0) {

//              if flat
                if (dx == 0) {
                    zoomPeak.add(tracker.getFrame(i));
//              if last nonzero slope was positive, then it's a peak
                } else if (dx > 0) {
                    if (zoomArray[i - 2] - zoomArray[i - 1] < 0) {
                        zoomPeak.add(tracker.getFrame(i - 1));

                        if (!zoomPeak.isEmpty()) {
                            zoomPeaks.add(zoomPeak);
                        }
                    } else if (lastdx < 0) {
                        zoomPeak.add(tracker.getFrame(i));

                        if (!zoomPeak.isEmpty()) {
                            zoomPeaks.add(zoomPeak);
                        }
                    }
                    zoomPeak = new TrackerFeature();
                }
            }
            if (dx != 0) {
                lastdx = dx;
            }
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
}
