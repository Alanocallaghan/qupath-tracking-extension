package qupath.extension.tracking;

import qupath.lib.gui.viewer.overlays.PathOverlay;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;
import qupath.lib.images.servers.ImageServer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by alan on 15/03/17.
 */
public class TrackerFeatures {

    private final ImageServer server;
    private final ViewTracker tracker;
    private Rectangle[] boundsArray;
    private Point2D[] eyeArray;
    private Point2D[] cursorArray;
    HeatmapOverlay hOverlay;
    private double[] eyeSpeedArray, zoomArray;

    TrackerFeatures(ViewTracker tracker, ImageServer server) {
        this.server = server;
        this.tracker = tracker;
        this.boundsArray = makeBounds();
        this.cursorArray = makeCursor();
        addEye();
        hOverlay = new HeatmapOverlay(this);
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

    double[] getEyeSpeedArray() { return Arrays.copyOf(eyeSpeedArray, eyeSpeedArray.length);}

    double[] getZoomArray() { return Arrays.copyOf(zoomArray, zoomArray.length); }

    ViewTracker getTracker() {
        return this.tracker;
    }

    ImageServer getServer() {
        return this.server;
    }

    Point2D[] getCursorArray() {
        return this.cursorArray;
    }

    Rectangle[] getBoundsArray() {
        return this.boundsArray;
    }

    Point2D[] getEyeArray() {
        return this.eyeArray;
    }

}
