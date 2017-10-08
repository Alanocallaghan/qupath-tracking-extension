package qupath.extension.tracking.tracker;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;
import qupath.lib.images.servers.ImageServer;

import java.awt.*;
import java.awt.geom.Point2D;
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
    private Rectangle[] boundsArray = null;
    private Point2D[] eyeArray = null;
    private Point2D[] cursorArray = null;
    private double[] zoomArray = new double[0];

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
            this.cursorArray = generateCursorArray();
            this.zoomArray = generateBoundsZoomArray();

            boundsFeatures = new BoundsFeatures(this);

            eyeArray = generateEyeArray();
            if (tracker.hasEyeTrackingData()) {
                eyeFixations = new Fixations(this, "eye");
            }
            cursorFixations = new Fixations(this, "cursor");
        }

    }


    private Point2D[] generateCursorArray() {
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

    private Point2D[] generateEyeArray() {
        if (!tracker.hasEyeTrackingData()) {
            return new Point2D[0];
        }

        Point2D currentEye;
        int nFrames = tracker.nFrames();
        Point2D[] point2Ds = new Point2D[nFrames];

        for (int i = 0; i < nFrames; i++) {
            currentEye = tracker.getFrame(i).getEyePosition();
            if (currentEye != null && !(currentEye.getX() == 0 && currentEye.getY() == 0)) {
                point2Ds[i] = currentEye;
            }
        }
        return point2Ds;
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
        for (int i = 1; i < nFrames; i++) {
            double speed = getSpeed(tracker, i, i - 1,
                    FeatureType.BOUNDS) /
                        calculateDownsample(tracker.getFrame(i).getImageBounds(),
                    tracker.getFrame(i).getSize());
            if (!Double.isNaN(speed) && Double.isFinite(speed)) {
                array[i] = speed;
            }
        }
        return array;
    }

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

    public Point2D[] getArray(FeatureType featureType) {
        Point2D[] array;
        if (featureType == FeatureType.CURSOR) {
            array = getCursorArray();
        } else {
            array = getEyeArray();
        }
        return array;
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
        output.add("tracker", new JsonPrimitive(tracker.getSummaryString()));
        output.add("eye_fixations", eyeFixations.toJSON());
        output.add("cursor_fixations", cursorFixations.toJSON());
        output.add("bounds_features", boundsFeatures.toJSON());
        return output;
    }

    public enum FeatureType {
        EYE, CURSOR, BOUNDS;

        @Override
        public String toString() {
            if (this.equals(EYE)) {
                return "Eye";
            } else if (this.equals(CURSOR)){
                return "Cursor";
            } else {
                return "Bounds";
            }
        }
    }
}
