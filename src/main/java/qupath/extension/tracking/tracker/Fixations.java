package qupath.extension.tracking.tracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

    private TrackerFeatureList fixations;

    private double[] durations;
    private Point2D[] centroids;

    private TrackerFeatureList IVTFixations,
            IDTFixations,
            eyeTribeFixations = null;

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
        IDTFixations = calculateIDTFixations();
        IVTFixations = calculateIVTFixations();

        if (this.featureType == EYE) {
            eyeTribeFixations = findEyeTribeFixations();
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
        }

        return new ViewRecordingFrame(
                frame.getTimestamp(),
                frame.getImageShape(),
                frame.getSize(),
                cpoint,
                epoint,
                frame.isEyeFixated());
    }

    private TrackerFeatureList calculateIDTFixations() {

        TrackerFeatureList fixations = new TrackerFeatureList();
        int[] inds = java.util.stream.IntStream.range(0, this.allFrames.length).toArray();
        List allFramesForMethod = Arrays.asList(inds);

        while (allFramesForMethod.size() > 0) {

            TrackerFeature windowPoints = new TrackerFeature(trackerFeatures.getTracker());
            ArrayList<ViewRecordingFrame> windowPointsResized = new ArrayList<>(0);

            int currentIndex = (int)allFramesForMethod.get(0);
            ViewRecordingFrame currentFrame = allFrames[currentIndex];
            windowPoints.add(0);

            int timeOfWindow = 0;
            int j = 0;

//            long timeOfFirstFrame = allFramesForMethod.get(j).getTimestamp();
            long timeOfPreviousFrame = allFrames[j].getTimestamp();

            while (timeOfWindow <= IDTDurationThreshold) {
                if (++j < allFramesForMethod.size()) {
                    long timeOfFrame = allFrames[j].getTimestamp();
                    timeOfWindow += (timeOfFrame - timeOfPreviousFrame);
                    timeOfPreviousFrame = timeOfFrame;
                    windowPoints.add(j);
                } else
                    break;
            }

            double dispersion = calculateDispersion(windowPointsResized);
            if (dispersion <= IDTDispersionThreshold) {

                while (calculateTranslatedDispersionFromTrackerFeature(windowPoints) <= IDTDispersionThreshold) {

                    if (++j < allFramesForMethod.size()) {
                        windowPoints.add(j);
                    } else
                        break;
                }

                fixations.add(windowPoints);
                allFramesForMethod.removeAll(windowPoints);
            } else {
                allFramesForMethod.remove(j);
            }
        }
        return fixations;
    }

    //    todo: meaningful threshold!!!
//    todo: downsample?
    private TrackerFeatureList calculateIVTFixations() {
        ViewRecordingFrame[] allFramesForMethod = this.allFrames;
        double[] eyeSpeedArray = trackerFeatures.getEyeSpeedArray();
        boolean[] isFixated = new boolean[allFramesForMethod.length];

        TrackerFeatureList fixations = new TrackerFeatureList();

        for (int i = 0; i < eyeSpeedArray.length; i++) {
            isFixated[i] = eyeSpeedArray[i] < IVTSpeedThreshold;
        }

        TrackerFeature currentFixation = new TrackerFeature(trackerFeatures.getTracker());
        for (int i = 0; i < isFixated.length; i ++) {
            boolean bool = isFixated[i];
            if (bool) {
                currentFixation.add(i);
            } else {
                if (!currentFixation.isEmpty()) {
                    fixations.add(currentFixation);
                    currentFixation = new TrackerFeature(trackerFeatures.getTracker());
                }
            }
        }
        return fixations;
    }

    private Point2D[] calculateCentroids() {
        Point2D[] centroids;
        if (!this.fixations.isEmpty()) {
            centroids = new Point2D[fixations.size()];
            int i = 0;
            for (TrackerFeature fixation : fixations) {
                centroids[i] = calculateCentroid(fixation);
                i++;
            }
        } else {
            centroids = null;
        }
        return centroids;
    }

    private TrackerFeatureList findEyeTribeFixations() {
        TrackerFeatureList fixations = new TrackerFeatureList();
        TrackerFeature currentFixation = new TrackerFeature(trackerFeatures.getTracker());

        boolean fixStarted = false;

        for (int i = 0; i < allFrames.length; i ++) {
            ViewRecordingFrame frame = allFrames[i];
            if (frame.isEyeFixated()!=null && frame.isEyeFixated()) {
                if(fixStarted) {
                    currentFixation.add(i);
                } else
                    fixStarted = true;
            } else if (frame.isEyeFixated()!=null && !frame.isEyeFixated()) {
                if(currentFixation.size()!= 0) {
                    fixations.add(currentFixation);
                    currentFixation = new TrackerFeature(trackerFeatures.getTracker());
                }
                fixStarted = false;
            }
        }
        return fixations;
    }

    private double calculateTranslatedDispersionFromTrackerFeature(TrackerFeature inds) {
        ArrayList<ViewRecordingFrame> frames = new ArrayList<>(inds.size());
        for (int i : inds) {
            frames.add(inds.getFrameAtFeatureIndex(i));
        }
        return calculateTranslatedDispersion(frames);
    }

    private double calculateTranslatedDispersion(ArrayList<ViewRecordingFrame> frames) {
        ArrayList<ViewRecordingFrame> copy = new ArrayList<>(frames);
        for (int i = 0; i < frames.size(); i ++) {
            copy.set(i, translateCoords(frames.get(i)));
        }
        return calculateDispersion(copy);
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
    private Point2D calculateCentroid(TrackerFeature fixationPoints) {
        if (fixationPoints.size() == 0) {
            return null;
        }
        int sumX = 0, sumY = 0;

        for (int i: fixationPoints) {
            ViewRecordingFrame frame = fixationPoints.getFrameAtFeatureIndex(i);
            Point2D point = getPosition(frame);
            if (point != null) {
                sumX += point.getX();
                sumY += point.getY();
            }
        }
        double meanX, meanY;
        meanX = ((double) sumX) / fixationPoints.size();
        meanY = ((double) sumY) / fixationPoints.size();
        return (new Point2D.Double(meanX, meanY));
    }

    public double calculateAverageZoom(TrackerFeature feature) {
        double sumzoom = 0;
        for (int i : feature) {
            ViewRecordingFrame frame = feature.getFrameAtFeatureIndex(i);
            sumzoom += TrackerUtils.calculateZoom(
                    frame.getImageBounds(),
                    frame.getSize(),
                    trackerFeatures.getServer());
        }
        return sumzoom / feature.size();
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

    public TrackerFeatureList getFixations() {
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
                fixations = IDTFixations;
                break;
            case IVT:
                fixations = IVTFixations;
                break;
            case EYETRIBE:
                fixations = eyeTribeFixations;
                break;
            case ALL_POINTS:
                centroids = trackerFeatures.getArray(this.featureType);
                break;
        }
        centroids = calculateCentroids();
        durations = fixations.getDurations();
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

    public JsonObject toJSON() {
        JsonObject output = new JsonObject();
        JsonArray centroidArray = new JsonArray();
        for (Point2D centroid: centroids) {
            JsonArray centroidJSON = new JsonArray();
            centroidJSON.add(centroid.getX());
            centroidJSON.add(centroid.getY());
            centroidArray.add(centroidJSON);
        }
        JsonArray durationArray = new JsonArray();
        for (TrackerFeature fixation: fixations) {
            durationArray.add(fixation.getDuration());
        }
        output.add("fixations", fixations.toJSON(false));
        output.add("centroids", centroidArray);
        output.add("durations", durationArray);
        return output;
    }
}

