package qupath.extension.tracking.tracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import qupath.extension.tracking.TrackerUtils;
import qupath.extension.tracking.gui.controllers.prefs.PointPrefs;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPrefs;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;

import static qupath.extension.tracking.TrackerUtils.calculateDownsample;
import static qupath.extension.tracking.TrackerUtils.getSpeed;
import static qupath.extension.tracking.tracker.TrackerFeatures.FeatureType.EYE;
import static qupath.extension.tracking.TrackerUtils.colorFXtoAWT;


/**
 * @author Alan O'Callaghan
 *
 * Created by Alan O'Callaghan on 15/03/17.
 */

public class Fixations {


    private TrackerFeatureList IVTFixations,
            IDTFixations,
            eyeTribeFixations = null,
            allPointFixations;

    private final TrackerFeatures trackerFeatures;

    private final ViewRecordingFrame[] allFrames;
    private TrackerFeatures.FeatureType featureType;

    private Color highColor = colorFXtoAWT(javafx.scene.paint.Color.RED),
            medColor = colorFXtoAWT(javafx.scene.paint.Color.LIME),
            lowColor = colorFXtoAWT(javafx.scene.paint.Color.BLUE);
    private StringProperty fixationType = new SimpleStringProperty();

    private DoubleProperty IVTSpeedThreshold = new SimpleDoubleProperty(50);
    private DoubleProperty IDTDurationThreshold = new SimpleDoubleProperty(50),
            IDTDispersionThreshold = new SimpleDoubleProperty(1000),
            thicknessScalar = new SimpleDoubleProperty(1);

    // todo: check correlation between this method and EyeTribe method using real tracking data
    Fixations(TrackerFeatures trackerFeatures, String featureType) {
        this.trackerFeatures = trackerFeatures;
        this.setFeatureType(featureType);
        allFrames = TrackerUtils.getFramesAsArray(trackerFeatures.getTracker());

        PointPrefs prefs = featureType.equals("eye") ? TrackingPrefs.eyePointPrefs : TrackingPrefs.cursorPointPrefs;

        IVTSpeedThreshold.bind(prefs.IVTSpeedThreshold);
        IVTSpeedThreshold.addListener((observable, oldValue, newValue) -> {
            IVTFixations = calculateIVTFixations();
            QuPathGUI.getInstance().getViewer().repaint();
        });

        IDTDispersionThreshold.bind(prefs.IDTDispersionThreshold);
        IDTDispersionThreshold.addListener((observable, oldValue, newValue) -> {
            IDTFixations = calculateIDTFixations();
            QuPathGUI.getInstance().getViewer().repaint();
        });
        IDTDurationThreshold.bind(prefs.IDTDurationThreshold);
        IDTDurationThreshold.addListener((observable, oldValue, newValue) -> {
            IDTFixations = calculateIDTFixations();
            QuPathGUI.getInstance().getViewer().repaint();
        });

        IDTFixations = calculateIDTFixations();
        IVTFixations = calculateIVTFixations();
        allPointFixations = makeAllPointFixations();

        if (this.featureType == EYE) {
            eyeTribeFixations = findEyeTribeFixations();
        }

        this.fixationType.bind(prefs.fixationType);
        this.fixationType.addListener((observable, oldValue, newValue) -> QuPathGUI.getInstance().getViewer().repaint());
    }


    private TrackerFeatureList makeAllPointFixations() {
        TrackerFeatureList trackerFeatureList = new TrackerFeatureList();
        for (int i = 0; i < allFrames.length; i ++) {
            TrackerFeature feature = new TrackerFeature(this.trackerFeatures.getTracker());
            feature.add(i);
            trackerFeatureList.add(feature);
        }
        return trackerFeatureList;
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
        ArrayList<Integer> allIndsForMethod = new ArrayList<>(allFrames.length);
        for (int i = 0; i < allFrames.length; i ++) {
            allIndsForMethod.add(i);
        }

        while (allIndsForMethod.size() > 1) {
            TrackerFeature windowPoints = new TrackerFeature(trackerFeatures.getTracker());

            long timeOfWindow = 0;
            int j = 0;

            long timeOfFirstFrame = allFrames[allIndsForMethod.get(0)].getTimestamp();

            while (timeOfWindow <= IDTDurationThreshold.get()) {
                if (++j < allIndsForMethod.size()) {
                    long timeOfFrame = allFrames[allIndsForMethod.get(j)].getTimestamp();
                    timeOfWindow = timeOfFrame - timeOfFirstFrame;
                    windowPoints.add(allIndsForMethod.get(j));
                } else
                    break;
            }

            double dispersion = calculateTranslatedDispersionFromTrackerFeature(windowPoints);
            if (dispersion <= IDTDispersionThreshold.get()) {
                while (calculateTranslatedDispersionFromTrackerFeature(windowPoints) <=
                        IDTDispersionThreshold.get()) {
                    if (++j < allIndsForMethod.size()) {
                        windowPoints.add(allIndsForMethod.get(j));
                    } else
                        break;
                }
                fixations.add(windowPoints);
                allIndsForMethod.removeAll(windowPoints);
            } else {
                allIndsForMethod.remove(allIndsForMethod.get(0));
            }
        }
        return fixations;
    }

    //    todo: meaningful threshold!!!
//    todo: downsample?
    private TrackerFeatureList calculateIVTFixations() {
        boolean[] isFixated = new boolean[trackerFeatures.getTracker().nFrames()];

        TrackerFeatureList fixations = new TrackerFeatureList();

        for (int i = 1; i < allFrames.length; i++) {
            double speed = getSpeed(trackerFeatures.getTracker(), i, i - 1, this.featureType)
                    / calculateDownsample(trackerFeatures.getTracker().getFrame(i));
            isFixated[i] = speed < IVTSpeedThreshold.get();
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
        if (!this.getFixations().isEmpty()) {
            centroids = new Point2D[getFixations().size()];
            int i = 0;
            for (TrackerFeature fixation : getFixations()) {
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
            ViewRecordingFrame frame = inds.getFrame(i);
            frames.add(frame);
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
        double dispersionX = Math.abs(xMaxSoFar - xMinSoFar);
        double dispersionY = Math.abs(yMaxSoFar - yMinSoFar);

        return (dispersionX + dispersionY);
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
            ViewRecordingFrame frame = fixationPoints.getFrame(i);
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


    public double[] calculateSaccadeDistances() {
        Point2D lastPoint = null;
        double[] saccadeDistances = new double[getFixations().size()];
        if (saccadeDistances.length == 0) {
            return new double[1];
        }
        int i = 0;
        for (Point2D point : calculateCentroids()) {
            if (lastPoint != null && point != null) {
                saccadeDistances[i] = TrackerUtils.calculateEuclideanDistance(point, lastPoint);
            }
            lastPoint = point;
            i++;
        }
        return saccadeDistances;
    }

    public double[] getDurations() {
        return getFixations().getDurations();
    }

    public double[] calculateSaccadeDurations() {
        long lastFrameOfPrevious = -1;
        long firstFrameOfCurrent;
        double[] durations = new double[getFixations().size()];
        if (durations.length == 0) {
            return null;
        }
        int i = 0;
        for(TrackerFeature fixation : getFixations()) {
            firstFrameOfCurrent = fixation.getFrameAtFeatureIndex(0).getTimestamp();

            if (lastFrameOfPrevious != -1) {
                long timePassed = firstFrameOfCurrent - lastFrameOfPrevious;
                durations[i] = timePassed;
            }
            lastFrameOfPrevious = fixation.getFrameAtFeatureIndex(fixation.size() - 1).getTimestamp();
            i++;
        }
        return durations;
    }

    public TrackerFeatureList getFixations() {
        switch(fixationType.get().toUpperCase()) {
            case "IDT":
                return IDTFixations;
            case "IVT":
                return IVTFixations;
            case "EYETRIBE":
                return eyeTribeFixations;
            case "ALL POINTS":
                return allPointFixations;
            default:
                return allPointFixations;
        }
    }

    public Point2D[] getCentroids() {
        return calculateCentroids();
    }


    public JsonObject toJSON() {
        JsonObject output = new JsonObject();
        JsonArray centroidArray = new JsonArray();
        for (Point2D centroid: calculateCentroids()) {
            JsonArray centroidJSON = new JsonArray();
            centroidJSON.add(centroid.getX());
            centroidJSON.add(centroid.getY());
            centroidArray.add(centroidJSON);
        }
        JsonArray durationArray = new JsonArray();
        for (TrackerFeature fixation: getFixations()) {
            durationArray.add(fixation.getDuration());
        }
        output.add("fixations", getFixations().toJSON(false));
        output.add("centroids", centroidArray);
        output.add("durations", durationArray);
        return output;
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


    public double getThicknessScalar() {
        return thicknessScalar.get();
    }

    private void setFeatureType(String featureType) {
        TrackerFeatures.FeatureType enumtype = EYE;
        switch(featureType.toLowerCase()) {
            case "eye":
                enumtype = TrackerFeatures.FeatureType.EYE;
                break;
            case "cursor":
                enumtype = TrackerFeatures.FeatureType.CURSOR;
                break;
        }
        this.featureType = enumtype;
    }

    public TrackerFeatures.FeatureType getFeatureType() {
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

}

