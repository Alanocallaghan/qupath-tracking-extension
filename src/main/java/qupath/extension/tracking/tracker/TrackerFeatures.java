package qupath.extension.tracking.tracker;

import com.google.gson.JsonObject;
import qupath.extension.tracking.TrackerUtils;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;
import qupath.lib.images.servers.ImageServer;

import javax.swing.text.View;
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
    private Fixations eyeFixations;
    private Fixations cursorFixations;

    private BoundsFeatures boundsFeatures;

    public TrackerFeatures(ViewTracker tracker, ImageServer server) {
        this.server = server;
        this.tracker = tracker;
        if (tracker != null) {
            this.boundsArray = makeBounds();
            this.boundsSpeedArray = calculateBoundsSpeedArray();
            this.cursorArray = makeCursor();
            this.zoomArray = generateBoundsZoomArray();

            boundsFeatures = new BoundsFeatures(this);

            generateEyeArray();
            if (tracker.hasEyeTrackingData()) {
                eyeFixations = new Fixations(this, "eye", "IVT");
            }
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
        for (int i = 0; i < nFrames; i++) {
            currentFrame = tracker.getFrame(i);
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

    double[] getEyeSpeedArray() { return Arrays.copyOf(eyeSpeedArray, eyeSpeedArray.length);}

    public double[] getZoomArray() { return Arrays.copyOf(zoomArray, zoomArray.length); }

    public ViewTracker getTracker() {
        return this.tracker;
    }

    ImageServer getServer() {
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

    public void setSlowPanTimeThreshold(double slowPanTimeThreshold) {
        this.boundsFeatures.slowPanTimeThreshold = slowPanTimeThreshold;
        this.boundsFeatures.slowPans = boundsFeatures.findSlowPans();
    }

    public void setSlowPanSpeedThreshold(double slowPanSpeedThreshold) {
        this.boundsFeatures.slowPanSpeedThreshold = slowPanSpeedThreshold;
        this.boundsFeatures.slowPans = boundsFeatures.findSlowPans();
    }

    public void setBoundsFixationThreshold(long boundsFixationThreshold) {
        this.boundsFeatures.boundsFixationThreshold = boundsFixationThreshold;
        this.boundsFeatures.boundsFixations = boundsFeatures.findBoundsFixations();
    }

    public void setZoomPeakThreshold(int zoomPeakThreshold) {
        this.boundsFeatures.zoomPeakThreshold = zoomPeakThreshold;
        this.boundsFeatures.zoomPeaks = boundsFeatures.findZoomPeaks();
    }

    double[] getBoundsSpeedArray() {
        return boundsSpeedArray;
    }

    public BoundsFeatures getBoundsFeatures() {
        return boundsFeatures;
    }

    public Fixations getEyeFixations() {
        return eyeFixations;
    }

    public Fixations getCursorFixations() {
        return cursorFixations;
    }

    public JsonObject toJSON() {
        JsonObject output = new JsonObject();
        output.add("eye_fixations", eyeFixations.toJSON());
        output.add("cursor_fixations", cursorFixations.toJSON());
        output.add("bounds_features", boundsFeatures.toJSON());
        return output;
    }
}
