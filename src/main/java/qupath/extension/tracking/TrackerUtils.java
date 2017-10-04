package qupath.extension.tracking;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;
import qupath.lib.images.servers.ImageServer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;


/**
 * @author Alan O'Callaghan
 *
 * Created by alan on 15/03/17.
 */
public class TrackerUtils {


    public static int intArrayMax(int[] arr) {
        int max = Integer.MIN_VALUE;

        for(int cur: arr)
            max = Math.max(max, cur);

        return max;
    }

    public static double intArrayMin(int[] arr) {
        int min = Integer.MAX_VALUE;

        for(int cur: arr)
            min = Math.min(min, cur);

        return min;
    }


    private static double calculateDownsample(double regionWidth,
                                              double regionHeight,
                                              double visibleWidth,
                                              double visibleHeight) {
        if (regionWidth != 0) {
            return (regionWidth / visibleWidth);
        } else if (regionHeight != 0) {
            return (regionHeight / visibleHeight);
        }
        return 0;
    }

    public static double calculateArrayMean(double[] array) {
        double sum = 0;
        for (double x: array) {
            sum += x;
        }
        return sum / array.length;
    }

    public static double[] convertDoubles(Double[] doubles) {
        double[] ret = new double[doubles.length];
        int i = 0;
        while(i < doubles.length) {
            ret[i] = doubles[i];
            i++;
        }
        return ret;
    }

    public static boolean saveSnapshot(QuPathViewer viewer, File file) {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

        javafx.scene.canvas.Canvas canvas = viewer.getCanvas();
        WritableImage image = new WritableImage((int)canvas.getWidth(), (int)canvas.getHeight());
        image = canvas.snapshot(new SnapshotParameters(), image);

        try {
            ImageIO.write(SwingFXUtils.fromFXImage(image, null ),
                    "png",
                    byteOutput);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(byteOutput.toByteArray());
            fileOutputStream.close();
            byteOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Color colorFXtoAWT(javafx.scene.paint.Color color) {
        return new Color(
                (float) color.getRed(),
                (float) color.getGreen(),
                (float) color.getBlue());
    }

    public static javafx.scene.paint.Color colorAWTtoFX(Color color) {
        return javafx.scene.paint.Color.color(
                (double)color.getRed(),
                (double)color.getGreen(),
                (double)color.getBlue());
    }

    public static double calculateDownsample(Rectangle regionSize, Dimension canvas) {
        return calculateDownsample(
                regionSize.getWidth(),
                regionSize.getHeight(),
                canvas.getWidth(),
                canvas.getHeight());
    }

    public static ArrayList<ViewRecordingFrame> getFramesAsArrayList(ViewTracker tracker) {
        ArrayList<ViewRecordingFrame> frames = new ArrayList<>(tracker.nFrames());
        for (int i = 0; i < tracker.nFrames(); i++) {
            frames.add(i, tracker.getFrame(i));
        }
        return frames;
    }

    public static ViewRecordingFrame[] getFramesAsArray(ViewTracker tracker) {
        ViewRecordingFrame[] frames = new ViewRecordingFrame[tracker.nFrames()];
        Object[] array = getFramesAsArrayList(tracker).toArray();
        for (int i = 0; i < array.length; i++) {
            frames[i] = (ViewRecordingFrame)array[i];
        }
        return frames;
    }

    public static double calculateZoom(Rectangle visibleRegion, Dimension viewerSize, ImageServer server) {
        return calculateZoom(visibleRegion, viewerSize, server.getAveragedPixelSizeMicrons());
    }

    public static double calculateZoom(Rectangle visibleRegion, Dimension viewerSize, double pixelSize) {
        return (visibleRegion.getWidth() / viewerSize.getWidth()) * pixelSize;
    }

    public static double calculateZoom(Rectangle visibleRegion, Dimension viewerSize) {
        return calculateZoom(visibleRegion, viewerSize, 1);
    }


    private static double calculateEuclideanDistance(double x1, double y1, double x2, double y2) {
        return (sqrt(pow(x1 - y1, 2) + pow(x2 - y2, 2)));
    }

    private static double calculateEuclideanDistance(Point2D point1, Point2D point2) {
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
     */
    public static double getSpeed(ViewRecordingFrame frame1, ViewRecordingFrame frame2, SpeedType type) {
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
