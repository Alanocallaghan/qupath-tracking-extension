package qupath.extension.tracking;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;

import java.awt.geom.Point2D;
import java.util.*;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import static javafx.application.Platform.exit;
import static qupath.extension.tracking.Fixations.FeatureType.EYE;
import static qupath.extension.tracking.Fixations.FixationType.EYETRIBE;
import static qupath.extension.tracking.Fixations.FixationType.IDT;
import static qupath.extension.tracking.Fixations.FixationType.IVT;


/**
 * @author Alan O'Callaghan
 *
 * Created by yourname on 15/03/17.
 */

public class Fixations {

    private ArrayList<ArrayList<ViewRecordingFrame>> fixations;

    private double[] durations;
    private Point2D[] centroids;

    private ArrayList<ArrayList<ViewRecordingFrame>> IVTFixations, IDTFixations, eyeTribeFixations = null;
    private double[] IVTDurations, IDTDurations, eyeTribeDurations;
    private Point2D[] IVTCentroids, IDTCentroids, eyeTribeCentroids;

    private final TrackerFeatures trackerFeatures;

    private final ViewRecordingFrame[] allFrames;
    private FeatureType featureType;

    // todo: check correlation between this method and EyeTribe method using real tracking data
    Fixations(TrackerFeatures trackerFeatures, String featureType, String fixationType) {
        this.trackerFeatures = trackerFeatures;
        this.setFeatureType(featureType);
        allFrames = TrackerUtils.getFramesAsArray(trackerFeatures.getTracker());
        IDTFixations = calculateIDTFixations();
        IVTFixations = calculateIVTFixations();
        if (this.featureType == EYE) {
            eyeTribeFixations = findEyeTribeFixations();
        }
        this.setFixationType(fixationType);
    }

    void setFeatureType(String featureType) {
        FeatureType enumtype = EYE;
        switch(featureType.toLowerCase()) {
            case "eye":
                enumtype = FeatureType.EYE;
                break;
            case "cursor":
                enumtype = FeatureType.CURSOR;
                break;
        }
        this.featureType = enumtype;
    }

    enum FeatureType {
        EYE, CURSOR;

        @Override
        public String toString() {
            if (this.equals(EYE)) {
                return "Eye";
            } else {
                return "Cursor";
            }
        }
    }

    public enum FixationType {
        EYETRIBE, IDT, IVT, ALL_POINTS;

        @Override
        public String toString() {
            if (this.equals(EYETRIBE)) {
                return "Eyetribe";
            } else if(this.equals(IDT)) {
                return "IDT";
            } else if(this.equals(IVT)) {
                return "IVT";
            } else {
                return "All Points";
            }
        }
    }


    private ViewRecordingFrame imageSpaceToComponentSpace(ViewRecordingFrame frame) {

        TrackerUtils.calculateDownsample(frame.getImageBounds(), frame.getSize());
        Rectangle rectangle = frame.getImageBounds();
        Point2D point = getPosition(frame);

        if (point != null) {
            double newX = point.getX() - rectangle.getX();
            double newY = point.getY() - rectangle.getY();
            Point2D.Double p = new Point2D.Double(newX, newY);

            if (this.featureType == EYE) {
                frame = new ViewRecordingFrame(
                        frame.getTimestamp(),
                        frame.getImageShape(),
                        frame.getSize(),
                        frame.getCursorPosition(),
                        p,
                        frame.isEyeFixated());
            } else {
                frame = new ViewRecordingFrame(
                        frame.getTimestamp(),
                        frame.getImageShape(),
                        frame.getSize(),
                        p,
                        frame.getEyePosition(),
                        frame.isEyeFixated());
            }
        }
        return frame;
    }

    @NotNull
    public static ViewRecordingFrame translateCoords(ViewRecordingFrame frame) {

//        double downsample = TrackerUtils.calculateDownsample(frame.getImageBounds(), frame.getSize());
        Rectangle rectangle = frame.getImageBounds();
        double X = rectangle.getX();
        double Y = rectangle.getY();

        Point2D epoint = frame.getEyePosition();
        if (epoint != null) {
            double eX = epoint.getX();
            double eY = epoint.getY();
            double newX = (eX - X); // / downsample;
            double newY = (eY - Y); // / downsample;
            epoint = new Point2D.Double(newX, newY);

        }
        Point2D cpoint = frame.getCursorPosition();
        if (cpoint != null) {
            double cX = cpoint.getX();
            double cY = cpoint.getY();
            double newX = (cX - X); // / downsample;
            double newY = (cY - Y); // / downsample;
            cpoint = new Point2D.Double(newX, newY);
//            if(newX > 10000) {
//                System.out.println(downsample);
//            }
        }

        return new ViewRecordingFrame(frame.getTimestamp(), frame.getImageShape(), frame.getSize(),
                cpoint, epoint, frame.isEyeFixated());
    }

    private ArrayList<ArrayList<ViewRecordingFrame>> calculateIDTFixations() {

        ArrayList<ArrayList<ViewRecordingFrame>> fixations = new ArrayList<>();
        List<ViewRecordingFrame> allFramesForMethod = new ArrayList<>(Arrays.asList(this.allFrames));

        while (allFramesForMethod.size() > 0) {

            ArrayList<ViewRecordingFrame> windowPoints = new ArrayList<>(0);
            ArrayList<ViewRecordingFrame> windowPointsResized = new ArrayList<>();

            ViewRecordingFrame currentFrame = allFramesForMethod.get(0);
            windowPoints.add(currentFrame);

            ViewRecordingFrame currentFrameResized = translateCoords(currentFrame);
            windowPointsResized.add(currentFrameResized);

            int timeOfWindow = 0;
            int j = 0;

            long timeOfFirstFrame = allFramesForMethod.get(j).getTimestamp();

            int durationThreshold = 200;
            while (timeOfWindow <= durationThreshold) {
                if (++j < allFramesForMethod.size()) {
                    timeOfWindow += (allFramesForMethod.get(j).getTimestamp() - timeOfFirstFrame);
                    windowPointsResized.add(translateCoords(allFramesForMethod.get(j)));
                    windowPoints.add(allFramesForMethod.get(j));
                } else
                    break;
            }

            int dispersionThreshold = 50;
            if (calculateDispersion(windowPointsResized) <= dispersionThreshold) {
                while (calculateDispersion(windowPointsResized) < dispersionThreshold) {

                    if (++j < allFramesForMethod.size()) {
                        windowPointsResized.add(translateCoords(allFramesForMethod.get(j)));
                        windowPoints.add(allFramesForMethod.get(j));
                    } else
                        break;
                }

                fixations.add(windowPoints);
                allFramesForMethod.removeAll(windowPoints);
            } else {
                allFramesForMethod.remove(currentFrame);
            }
        }
        IDTCentroids = calculateCentroids(fixations);
        IDTDurations = calculateDurations(fixations);
        return fixations;
    }

    //    todo: meaningful threshold!!!
//    todo: downsample?
    private ArrayList<ArrayList<ViewRecordingFrame>> calculateIVTFixations() {

        ViewRecordingFrame[] allFramesForMethod = this.allFrames;
        double[] eyeSpeedArray = trackerFeatures.getEyeSpeedArray();
        boolean[] isFixated = new boolean[allFramesForMethod.length];

        ArrayList<ArrayList<ViewRecordingFrame>> fixations = new ArrayList<>();

        for (int i = 0; i < eyeSpeedArray.length; i++) {
            double fixationSpeedThreshold = 100;
            isFixated[i] = eyeSpeedArray[i] < fixationSpeedThreshold;
        }

        int i=0;
        boolean previousBool = false;
        ArrayList<ViewRecordingFrame> currentFixation = new ArrayList<>();
        for (boolean bool : isFixated) {
            if (bool && previousBool) {
                currentFixation.add(allFramesForMethod[i]);
            } else {
                if (!currentFixation.isEmpty()) {
                    fixations.add(currentFixation);
                    currentFixation = new ArrayList<>();
                }
            }
            previousBool = bool;
            i++;
        }

        IVTCentroids = calculateCentroids(fixations);
        IVTDurations = calculateDurations(fixations);

        return fixations;

    }

    private double[] calculateDurations(ArrayList<ArrayList<ViewRecordingFrame>> arrayListOfFixationArrayLists) {

        double[] durations = new double[arrayListOfFixationArrayLists.size()];
        if(durations.length == 0) {
            return new double[1];
        }
        int i=0;
        for(ArrayList<ViewRecordingFrame> fixation : arrayListOfFixationArrayLists) {
            durations[i] = (fixation.get(fixation.size()-1) .getTimestamp() - fixation.get(0).getTimestamp());
            i++;
        }
        return durations;
    }

    private Point2D[] calculateCentroids(ArrayList<ArrayList<ViewRecordingFrame>> arrayListOfFixationArrayLists) {
        Point2D[] centroids = new Point2D[arrayListOfFixationArrayLists.size()];
        int i = 0;
        for (ArrayList<ViewRecordingFrame> fixation : arrayListOfFixationArrayLists) {
            centroids[i] = calculateCentroid(fixation);
            i++;
        }
        return centroids;
    }

    private ArrayList<ArrayList<ViewRecordingFrame>> findEyeTribeFixations() {
        ArrayList<ArrayList<ViewRecordingFrame>> fixations = new ArrayList<>();
        ArrayList<ViewRecordingFrame> currentFixation = new ArrayList<>();

        boolean fixStarted = false;

        ArrayList<ViewRecordingFrame> frames = new ArrayList<>(Arrays.asList(allFrames));
        for(ViewRecordingFrame frame : frames) {
            if (frame.isEyeFixated()!=null && frame.isEyeFixated()) {
                if(fixStarted) {
                    currentFixation.add(frame);
                } else
                    fixStarted = true;
            } else if (frame.isEyeFixated()!=null && !frame.isEyeFixated()) {
                if(currentFixation.size()!= 0) {
                    fixations.add(currentFixation);
                    currentFixation = new ArrayList<>();
                }
                fixStarted = false;
            }
        }
        eyeTribeCentroids = calculateCentroids(fixations);
        eyeTribeDurations = calculateDurations(fixations);
        return fixations;
    }

    private double calculateDispersion(ArrayList<ViewRecordingFrame> windowFrames) {
        double xMaxSoFar = 0, xMinSoFar = 0, yMaxSoFar = 0, yMinSoFar = 0;

        for (ViewRecordingFrame frame : windowFrames) {
            Point2D point = getPosition(frame);

            if (point != null) {
                if (point.getX() > xMaxSoFar) {
                    xMaxSoFar = point.getX();
                } else if (point.getX() < xMinSoFar) {
                    xMinSoFar = point.getX();
                }
                if (point.getY() > yMaxSoFar) {
                    yMaxSoFar = point.getY();
                } else if (point.getY() < yMinSoFar) {
                    yMinSoFar = point.getY();
                }
            }
        }
        return (xMaxSoFar - xMinSoFar) + (yMaxSoFar - yMinSoFar);
    }

    Point2D getPosition(ViewRecordingFrame frame) {
        Point2D point;
        if (this.featureType == EYE) {
            point = frame.getEyePosition();
        } else {
            point = frame.getCursorPosition();
        }
        return point;
    }

    @Nullable
    private Point2D calculateCentroid(ArrayList<ViewRecordingFrame> fixationPoints) {
        if (fixationPoints.size() == 0) {
            return null;
        }
        int sumX = 0, sumY = 0;

        for (ViewRecordingFrame frame : fixationPoints) {
            Point2D point = getPosition(frame);
            if (point != null) {
                sumX += point.getX();
                sumY += point.getY();
            }
        }
        double meanX, meanY;
        meanX = ((double) sumX) / fixationPoints.size();
        meanY = ((double) sumY) / fixationPoints.size();
        return(new Point2D.Double(meanX, meanY));
    }

    double calculateAverageZoom(ArrayList<ViewRecordingFrame> frames) {
        double sumzoom = 0;
        for(int i = 0; i < frames.size(); i++) {
            ViewRecordingFrame frame = frames.get(i);
            sumzoom += TrackerUtils.calculateZoom(frame.getImageBounds(), frame.getSize(), trackerFeatures.getServer());
        }
        return sumzoom / frames.size();
    }

//    public double[] calculateIDTSaccadeDistances() {
//        Point2D lastPoint = null;
//        double[] saccadeDistances = new double[IDTCentroids.length];
//        if(saccadeDistances.length == 0) {
//            return new double[1];
//        }
//        int i = 0;
//        for(Point2D point : IDTCentroids) {
//            if(lastPoint != null && point != null) {
//                saccadeDistances[i] = TrackerUtils.calculateEuclideanDistance(point, lastPoint);
//            }
//            lastPoint = point;
//            i++;
//        }
//        return saccadeDistances;
//    }
//
//    public double[] calculateIDTSaccadeDurations() {
//        long lastFrameOfPrevious = -1;
//        long firstFrameOfCurrent;
//        double[] durations = new double[IDTFixations.size()];
//        if(durations.length==0) {
//            return new double[1];
//        }
//        int i = 0;
//        for(ArrayList<ViewRecordingFrame> fixation : IDTFixations) {
//            firstFrameOfCurrent = fixation.get(0).getTimestamp();
//
//            if(lastFrameOfPrevious!=-1) {
//                long timePassed = firstFrameOfCurrent - lastFrameOfPrevious;
//                durations[i] = timePassed;
//            }
//            lastFrameOfPrevious = fixation.get(fixation.size()-1).getTimestamp();
//            i++;
//        }
//        return durations;
//    }
//
//    public double[] calculateIVTSaccadeDistances() {
//        Point2D lastPoint = null;
//        double[] saccadeDistances = new double[IVTCentroids.length];
//        if(saccadeDistances.length==0) {
//            return new double[1];
//        }
//        int i = 0;
//        for(Point2D point : IVTCentroids) {
//            if(lastPoint != null && point != null) {
//                saccadeDistances[i] = TrackerUtils.calculateEuclideanDistance(point, lastPoint);
//            }
//            lastPoint = point;
//            i++;
//        }
//        return saccadeDistances;
//    }
//
//    public double[] calculateIVTSaccadeDurations() {
//        long lastFrameOfPrevious = -1;
//        long firstFrameOfCurrent;
//        double[] durations = new double[IVTFixations.size()];
//        if(durations.length==0) {
//            return new double[1];
//        }
//        int i = 0;
//        for(ArrayList<ViewRecordingFrame> fixation : IVTFixations) {
//            firstFrameOfCurrent = fixation.get(0).getTimestamp();
//
//            if(lastFrameOfPrevious!=-1) {
//                long timePassed = firstFrameOfCurrent - lastFrameOfPrevious;
//                durations[i] = timePassed;
//            }
//            lastFrameOfPrevious = fixation.get(fixation.size()-1).getTimestamp();
//            i++;
//        }
//        return durations;
//    }
//
//    public double[] calculateEyetribeSaccadeDistances() {
//        Point2D lastPoint = null;
//        double[] saccadeDistances = new double[eyeTribeCentroids.length];
//        if(saccadeDistances.length==0) {
//            return new double[1];
//        }
//        int i = 0;
//        for(Point2D point : eyeTribeCentroids) {
//            if(lastPoint != null && point != null) {
//                saccadeDistances[i] = TrackerUtils.calculateEuclideanDistance(point, lastPoint);
//            }
//            lastPoint = point;
//            i++;
//        }
//        return saccadeDistances;
//    }
//
//    public double[] calculateEyetribeSaccadeDurations() {
//        long lastFrameOfPrevious = -1;
//        long firstFrameOfCurrent;
//        double[] durations = new double[eyeTribeFixations.size()];
//        if(durations.length==0) {
//            return new double[1];
//        }
//        int i = 0;
//        for(ArrayList<ViewRecordingFrame> fixation : eyeTribeFixations) {
//            firstFrameOfCurrent = fixation.get(0).getTimestamp();
//
//            if(lastFrameOfPrevious!=-1) {
//                long timePassed = firstFrameOfCurrent - lastFrameOfPrevious;
//                durations[i] = timePassed;
//            }
//            lastFrameOfPrevious = fixation.get(fixation.size()-1).getTimestamp();
//            i++;
//        }
//        return durations;
//    }

    ArrayList<ArrayList<ViewRecordingFrame>> getFixations() {
        return fixations;
    }

    Point2D[] getCentroids() {
        return centroids;
    }

    double[] getDurations() {
        return durations;
    }

    void setFixationType(FixationType fixationType) {
        switch(fixationType) {
            case IDT:
                centroids = IDTCentroids;
                durations = IDTDurations;
                fixations = IDTFixations;
                break;
            case IVT:
                centroids = IVTCentroids;
                durations = IVTDurations;
                fixations = IVTFixations;
                break;
            case EYETRIBE:
                centroids = eyeTribeCentroids;
                durations = eyeTribeDurations;
                fixations = eyeTribeFixations;
                break;
            case ALL_POINTS:
                centroids = trackerFeatures.getArray(this.featureType);
                durations = new double[centroids.length];
                fixations = new ArrayList<>(allFrames.length);
                for (int i = 0; i < allFrames.length; i++) {
                    ArrayList<ViewRecordingFrame> fix = new ArrayList<>(1);
                    fix.add(allFrames[i]);
                    fixations.add(fix);
                }
                break;
        }
    }

    void setFixationType(String type) {
        FixationType enumtype;
        switch(type) {
            case "IDT":
                enumtype = IDT;
                break;
            case "IVT":
                enumtype = IVT;
                break;
            case "Eyetribe":
                enumtype = EYETRIBE;
                break;
            default:
                enumtype = EYETRIBE;
        }
        setFixationType(enumtype);
    }
}

