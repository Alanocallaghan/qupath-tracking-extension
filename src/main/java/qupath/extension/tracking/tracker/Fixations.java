package qupath.extension.tracking.tracker;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import qupath.extension.tracking.TrackerUtils;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import static qupath.extension.tracking.tracker.Fixations.FeatureType.EYE;
import static qupath.extension.tracking.tracker.Fixations.FixationType.*;
import static qupath.extension.tracking.TrackerUtils.colorFXtoAWT;


/**
 * @author Alan O'Callaghan
 *
 * Created by yourname on 15/03/17.
 */

public class Fixations {

    private ArrayList<ArrayList<ViewRecordingFrame>> fixations;

    private double[] durations;
    private Point2D[] centroids;

    private ArrayList<ArrayList<ViewRecordingFrame>> IVTFixations,
            IDTFixations,
            eyeTribeFixations = null;
    private double[] IVTDurations,
            IDTDurations,
            eyeTribeDurations;
    private Point2D[] IVTCentroids,
            IDTCentroids,
            eyeTribeCentroids;

    private final TrackerFeatures trackerFeatures;

    private final ViewRecordingFrame[] allFrames;
    private FeatureType featureType;

    private Color highColor = colorFXtoAWT(javafx.scene.paint.Color.RED),
            medColor = colorFXtoAWT(javafx.scene.paint.Color.LIME),
            lowColor = colorFXtoAWT(javafx.scene.paint.Color.BLUE);
    private FixationType fixationType;

    public void setIVTSpeedThreshold(double IVTSpeedThreshold) {
        this.IVTSpeedThreshold = IVTSpeedThreshold;
        recalculateFixations();
    }

    private double IVTSpeedThreshold;

    public void setIDTDurationThreshold(double IDTDurationThreshold) {
        this.IDTDurationThreshold = IDTDurationThreshold;
        recalculateFixations();
    }

    private double IDTDurationThreshold;

    public void setIDTDispersionThreshold(double IDTDispersionThreshold) {
        this.IDTDispersionThreshold = IDTDispersionThreshold;
        recalculateFixations();
    }

    private double IDTDispersionThreshold;

    public double getThicknessScalar() {
        return thicknessScalar;
    }

    public void setThicknessScalar(double thicknessScalar) {
        this.thicknessScalar = thicknessScalar;
    }

    private double thicknessScalar = 1;

    public void recalculateFixations() {
        switch(fixationType) {
            case IDT:
                calculateIDTFixations();
                break;
            case IVT:
                calculateIVTFixations();
                break;
            case EYETRIBE:
                findEyeTribeFixations();
                break;
            case ALL_POINTS:
                break;
        }
        this.setFixationType(fixationType);
    }

    // todo: check correlation between this method and EyeTribe method using real tracking data
    Fixations(TrackerFeatures trackerFeatures, String featureType, String fixationType) {
        this.trackerFeatures = trackerFeatures;
        this.setFeatureType(featureType);
        allFrames = TrackerUtils.getFramesAsArray(trackerFeatures.getTracker());
        calculateIDTFixations();
        calculateIVTFixations();

        if (this.featureType == EYE) {
            findEyeTribeFixations();
        }

        this.setFixationType(fixationType);
    }

    private void setFeatureType(String featureType) {
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

    public FeatureType getFeatureType() {
        return featureType;
    }

    void setLowColor(Color lowColor) {
        this.lowColor = lowColor;
    }

    void setMedColor(Color medColor) {
        this.medColor = medColor;
    }

    void setHighColor(Color highColor) {
        this.highColor = highColor;
    }

    public Color getLowColor() {
        return lowColor;
    }

    public Color getHighColor() {
        return highColor;
    }

    public Color getMedColor() {
        return medColor;
    }

    public void setColor(String level, Color color) {
        if (Objects.equals(level, "low")) {
            lowColor = color;
        } else if (Objects.equals(level, "med")) {
            medColor = color;
        } else if (Objects.equals(level, "high")) {
            highColor = color;
        }
    }

    public enum FeatureType {
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
    private static ViewRecordingFrame translateCoords(ViewRecordingFrame frame) {

        double downsample = TrackerUtils.calculateDownsample(
                frame.getImageBounds(),
                frame.getSize());
        Rectangle rectangle = frame.getImageBounds();
        double X = rectangle.getX();
        double Y = rectangle.getY();

        Point2D epoint = frame.getEyePosition();
        if (epoint != null) {
            double eX = epoint.getX();
            double eY = epoint.getY();
            double newX = (eX - X) / downsample;
            double newY = (eY - Y) / downsample;
            epoint = new Point2D.Double(newX, newY);

        }
        Point2D cpoint = frame.getCursorPosition();
        if (cpoint != null) {
            double cX = cpoint.getX();
            double cY = cpoint.getY();
            double newX = (cX - X) / downsample;
            double newY = (cY - Y) / downsample;
            cpoint = new Point2D.Double(newX, newY);
//            todo: why?
//            if(newX > 10000) {
//                System.out.println(downsample);
//            }
        }

        return new ViewRecordingFrame(
                frame.getTimestamp(),
                frame.getImageShape(),
                frame.getSize(),
                cpoint,
                epoint,
                frame.isEyeFixated());
    }

    private void calculateIDTFixations() {

        ArrayList<ArrayList<ViewRecordingFrame>> fixations = new ArrayList<>();
        List<ViewRecordingFrame> allFramesForMethod = new ArrayList<>(
                Arrays.asList(this.allFrames));

        while (allFramesForMethod.size() > 0) {

            ArrayList<ViewRecordingFrame> windowPoints = new ArrayList<>(0);
            ArrayList<ViewRecordingFrame> windowPointsResized = new ArrayList<>();

            ViewRecordingFrame currentFrame = allFramesForMethod.get(0);
            windowPoints.add(currentFrame);

            ViewRecordingFrame currentFrameResized = translateCoords(currentFrame);
            windowPointsResized.add(currentFrameResized);

            int timeOfWindow = 0;
            int j = 0;

//            long timeOfFirstFrame = allFramesForMethod.get(j).getTimestamp();
            long timeOfPreviousFrame = allFramesForMethod.get(j).getTimestamp();

            while (timeOfWindow <= IDTDurationThreshold) {
                if (++j < allFramesForMethod.size()) {
                    long timeOfFrame = allFramesForMethod.get(j).getTimestamp();
                    timeOfWindow += (timeOfFrame -
                            timeOfPreviousFrame);
                    timeOfPreviousFrame = timeOfFrame;
                    windowPointsResized.add(translateCoords(allFramesForMethod.get(j)));
                    windowPoints.add(allFramesForMethod.get(j));
                } else
                    break;
            }

            double dispersion = calculateDispersion(windowPointsResized);
            if (dispersion <= IDTDispersionThreshold) {
                while (calculateDispersion(windowPointsResized) <= IDTDispersionThreshold) {

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
        IDTFixations = fixations;
    }

    //    todo: meaningful threshold!!!
//    todo: downsample?
    private void calculateIVTFixations() {
        ViewRecordingFrame[] allFramesForMethod = this.allFrames;
        double[] eyeSpeedArray = trackerFeatures.getEyeSpeedArray();
        boolean[] isFixated = new boolean[allFramesForMethod.length];

        ArrayList<ArrayList<ViewRecordingFrame>> fixations = new ArrayList<>();

        for (int i = 0; i < eyeSpeedArray.length; i++) {
            isFixated[i] = eyeSpeedArray[i] < IVTSpeedThreshold;
        }

        int i = 0;
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
        IVTFixations = fixations;
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

    private void findEyeTribeFixations() {
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
        this.eyeTribeFixations = fixations;
    }

    private double calculateDispersion(ArrayList<ViewRecordingFrame> windowFrames) {
        double xMaxSoFar = Double.MIN_VALUE,
                xMinSoFar = Double.MAX_VALUE,
                yMaxSoFar = Double.MIN_VALUE,
                yMinSoFar = Double.MAX_VALUE;

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

    private Point2D getPosition(ViewRecordingFrame frame) {
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

    public double calculateAverageZoom(ArrayList<ViewRecordingFrame> frames) {
        double sumzoom = 0;
        for (ViewRecordingFrame frame : frames) {
            sumzoom += TrackerUtils.calculateZoom(
                    frame.getImageBounds(),
                    frame.getSize(),
                    trackerFeatures.getServer());
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

    public ArrayList<ArrayList<ViewRecordingFrame>> getFixations() {
        return fixations;
    }

    public Point2D[] getCentroids() {
        return centroids;
    }

    public double[] getDurations() {
        return durations;
    }

    private void setFixationType(FixationType fixationType) {
        this.fixationType = fixationType;
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
                durations = null;
                fixations = null;
                break;
        }
    }

    public void setFixationType(String type) {
        FixationType enumtype;
        switch(type.toLowerCase()) {
            case "idt":
                enumtype = IDT;
                break;
            case "ivt":
                enumtype = IVT;
                break;
            case "eyetribe":
                enumtype = EYETRIBE;
                break;
            case "all points":
                enumtype = ALL_POINTS;
                break;
            default:
                enumtype = EYETRIBE;
        }
        setFixationType(enumtype);
    }
}

