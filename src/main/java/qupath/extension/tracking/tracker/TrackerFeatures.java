package qupath.extension.tracking.tracker;

import qupath.extension.tracking.TrackerUtils;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;
import qupath.lib.images.servers.ImageServer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Arrays;

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
    private double[] eyeSpeedArray, zoomArray;

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
            this.cursorArray = makeCursor();
            addEye();
            generateBoundsArray();
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
                eyeSpeedArray[i] = TrackerUtils.getSpeed(currentFrame, previousFrame, TrackerUtils.SpeedType.EYE)
                        / 
                        TrackerUtils.calculateDownsample(currentFrame.getImageBounds(), currentFrame.getSize());
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
                eyeSpeedArray[i] = TrackerUtils.getSpeed(currentFrame, previousFrame, TrackerUtils.SpeedType.EYE)
                        / TrackerUtils.calculateDownsample(currentFrame.getImageBounds(), currentFrame.getSize());
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
}
