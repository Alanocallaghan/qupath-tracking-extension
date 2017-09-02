package qupath.extension.tracking;

import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;
import qupath.lib.images.servers.ImageServer;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


/**
 * @author Alan O'Callaghan
 *
 * Created by alan on 15/03/17.
 */
class TrackerUtils {

    static double calculateDownsample(double regionWidth, double regionHeight, double visibleWidth, double visibleHeight) {
        if(regionWidth != 0) {
            return (regionWidth / visibleWidth);
        } else if(regionHeight != 0) {
            return (regionHeight / visibleHeight);
        }
        return 0;
    }

    static double calculateDownsample(Rectangle regionSize, Dimension canvas) {
        return calculateDownsample(regionSize.getWidth(), regionSize.getHeight(), canvas.getWidth(), canvas.getHeight());
    }

    static ArrayList<ViewRecordingFrame> getFramesAsArrayList(ViewTracker tracker) {
        ArrayList<ViewRecordingFrame> frames = new ArrayList<>(tracker.nFrames());
        for (int i = 0; i < tracker.nFrames(); i++) {
            frames.add(i, tracker.getFrame(i));
        }
        return frames;
    }

    static ViewRecordingFrame[] getFramesAsArray(ViewTracker tracker) {
        ViewRecordingFrame[] frames = new ViewRecordingFrame[tracker.nFrames()];
        Object[] array = getFramesAsArrayList(tracker).toArray();
        for (int i = 0; i < array.length; i++) {
            frames[i] = (ViewRecordingFrame)array[i];
        }
        return frames;
    }

    static double calculateZoom(Rectangle visibleRegion, Dimension viewerSize, ImageServer server) {
        return (visibleRegion.getWidth() / viewerSize.getWidth()) * server.getAveragedPixelSizeMicrons();
    }


    static double calculateEuclideanDistance(double x1, double y1, double x2, double y2) {
        return (sqrt(pow(x1 - y1, 2) + pow(x2 - y2, 2)));
    }

    static double calculateEuclideanDistance(Point2D point1, Point2D point2) {
        return calculateEuclideanDistance(point1.getX(), point1.getY(), point2.getX(), point2.getY());
    }

    public enum SpeedType {
        EYE, BOUNDS, CURSOR
    }

    /**
     * This method takes 2 frames and calculates the instantaneous speed.
     * The "type" parameter may be one of "cursor" or "bounds".
     * Uses the 2-Dimensional Euclidean distance formula.
     * returns "null" as double object if type is incorrect
     *
     * @param frame1
     * @param frame2
     * @param type
     * @return
     */
    static double getSpeed(ViewRecordingFrame frame1, ViewRecordingFrame frame2, SpeedType type) {
        double time = abs(frame1.getTimestamp() - frame2.getTimestamp());
        switch (type) {
            case EYE: {
                Point2D point1 = frame1.getEyePosition();
                Point2D point2 = frame2.getEyePosition();
                double distance = calculateEuclideanDistance(point1, point2); //todo: scale by pixel size
                return (distance / time);
            }
            case CURSOR: {
                Point2D point1 = frame1.getCursorPosition();
                Point2D point2 = frame2.getCursorPosition();
                double distance = calculateEuclideanDistance(point1, point2); //todo: scale by pixel size
                return (distance / time);
            }
            case BOUNDS: {
//        TODO: DISCUSS PROBLEM OF ZOOM/PAN USING ONLY TOPLEFT CO-ORDS.
                double distance = calculateEuclideanDistance(frame1.getImageBounds().getX(), frame1.getImageBounds().getY(), //todo: scale by pixel size
                        frame2.getImageBounds().getX(), frame2.getImageBounds().getY());
                return (distance / time);
            }
            default:
                return Double.parseDouble(null);
        }
    }
}
