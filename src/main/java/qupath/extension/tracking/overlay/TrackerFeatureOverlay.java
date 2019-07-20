package qupath.extension.tracking.overlay;

import javafx.beans.property.*;
import qupath.extension.tracking.TrackerUtils;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPrefs;
import qupath.extension.tracking.tracker.Fixations;
import qupath.extension.tracking.tracker.TrackerFeatures;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.overlays.AbstractOverlay;
import qupath.lib.regions.ImageRegion;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;

/**
 * @author Alan O'Callaghan
 * Created by alan on 15/03/17.
 */
public class TrackerFeatureOverlay extends AbstractOverlay {

    //    TODO: Separate class for bounds features?
    private final TrackerFeatures trackerFeatures;

    private BooleanProperty doPaintBoundsTrail = new SimpleBooleanProperty(false),
            doPaintCursorTrail = new SimpleBooleanProperty(false),
            doPaintEyeTrail = new SimpleBooleanProperty(false);

    private final QuPathViewer viewer;

    //    TODO: Cursor and eye numbers
    private final boolean doPaintNumbers = false;
    private DoubleProperty lowZoomThreshold = new SimpleDoubleProperty(),
            medZoomThreshold = new SimpleDoubleProperty();

    private DoubleProperty boundsThicknessScalar = new SimpleDoubleProperty();
    private ObjectProperty boundsLowColorProperty = new SimpleObjectProperty(),
            boundsMedColorProperty = new SimpleObjectProperty(),
            boundsHighColorProperty = new SimpleObjectProperty();

    public TrackerFeatureOverlay(TrackerFeatures trackerFeatures) {
        this.viewer = QuPathGUI.getInstance().getViewer();
        this.trackerFeatures = trackerFeatures;

        boundsLowColorProperty.bind(TrackingPrefs.boundsPrefs.getLowColorProperty());
        boundsLowColorProperty.addListener((observable, oldValue, newValue) -> viewer.repaint());

        boundsMedColorProperty.bind(TrackingPrefs.boundsPrefs.getMedColorProperty());
        boundsMedColorProperty.addListener((observable, oldValue, newValue) -> viewer.repaint());

        boundsHighColorProperty.bind(TrackingPrefs.boundsPrefs.getHighColorProperty());
        boundsHighColorProperty.addListener((observable, oldValue, newValue) -> viewer.repaint());


        boundsThicknessScalar.bind(TrackingPrefs.boundsPrefs.getThicknessScalarProperty());
        boundsThicknessScalar.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintBoundsTrail.bind(TrackingPrefs.boundsPrefs.getDoPaintTrailProperty());
        doPaintBoundsTrail.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintCursorTrail.bind(TrackingPrefs.cursorPointPrefs.getDoPaintTrailProperty());
        doPaintCursorTrail.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintEyeTrail.bind(TrackingPrefs.eyePointPrefs.getDoPaintTrailProperty());
        doPaintEyeTrail.addListener((observable, oldValue, newValue) -> viewer.repaint());

        lowZoomThreshold.bind(TrackingPrefs.lowZoomThreshold);
        lowZoomThreshold.addListener((observable, oldValue, newValue) -> viewer.repaint());

        medZoomThreshold.bind(TrackingPrefs.medZoomThreshold);
        medZoomThreshold.addListener((observable, oldValue, newValue) -> viewer.repaint());
    }

    @Override
    public void paintOverlay(Graphics2D g2d, ImageRegion imageRegion, double downsampleFactor, ImageObserver observer, boolean paintCompletely) {
        Rectangle clippingRectangle = new Rectangle(imageRegion.getX(),imageRegion.getY(),imageRegion.getWidth(),imageRegion.getHeight());
        if (trackerFeatures != null) {
            if (doPaintBoundsTrail.get()) {
                if (trackerFeatures.getBoundsArray() != null) {
                    drawBoundsTrail(g2d, downsampleFactor, clippingRectangle);
                }
            }
            if (doPaintCursorTrail.get()) {
                if (trackerFeatures.getCursorFixations() != null &&
                        !trackerFeatures.getCursorFixations().getFixations().isEmpty()) {
                    drawPointsTrail(g2d, downsampleFactor, clippingRectangle,
                            trackerFeatures.getCursorFixations());
                }
            }
            if (doPaintEyeTrail.get()) {
                if (trackerFeatures.getEyeFixations() != null &&
                        !trackerFeatures.getEyeFixations().getFixations().isEmpty()) {
                    drawPointsTrail(g2d, downsampleFactor, clippingRectangle,
                            trackerFeatures.getEyeFixations());
                }
            }
        }
    }


    private void drawBoundsTrail(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));
        Rectangle rect, previousRect = null;
        Rectangle[] boundsArray = trackerFeatures.getBoundsArray();
        double[] zoomArray = trackerFeatures.getZoomArray();

        for (int i = 0; i < boundsArray.length; i++) {
            rect = boundsArray[i];
            if ((!rect.equals(previousRect)) && rect.intersects(clippingRectangle)) {
                g2d.setColor(TrackerUtils.colorFXtoAWT((javafx.scene.paint.Color)boundsLowColorProperty.get()));
                if (zoomArray[i] < lowZoomThreshold.get()) {
                    g2d.setColor(TrackerUtils.colorFXtoAWT((javafx.scene.paint.Color)boundsMedColorProperty.get()));
                }
                if (zoomArray[i] < medZoomThreshold.get()) {
                    g2d.setColor(TrackerUtils.colorFXtoAWT((javafx.scene.paint.Color)boundsHighColorProperty.get()));
                }

                g2d.setStroke(new BasicStroke(((downsampleFactor > 1) ? (float) downsampleFactor : 1)
                        * boundsThicknessScalar.floatValue()));
                g2d.draw(rect);
            }
            previousRect = rect;
        }
    }

    private void drawPointsTrail(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle, Fixations fixations) {
        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));

        double[] zoomLevel;
        Point2D[] points = fixations.getCentroids();
        zoomLevel = new double[fixations.getFixations().size()];
        for (int j = 0; j < fixations.getFixations().size(); j++) {
            zoomLevel[j] = fixations.getFixations().get(j).calculateAverageZoom(
                    viewer.getServer().getAveragedPixelSizeMicrons());
        }

        Point2D previousPoint = null;
        for (int i = 0; i < points.length; i++) {
            Point2D point = points[i];

            if (point != null) {
                if (clippingRectangle.contains(point)) {

                    Color lowColor = fixations.getLowColor();
                    Color medColor = fixations.getMedColor();
                    Color highColor = fixations.getHighColor();

                    g2d.setColor(lowColor);
                    if (zoomLevel[i] < lowZoomThreshold.get()) {
                        g2d.setColor(medColor);
                    }
                    if (zoomLevel[i] < medZoomThreshold.get()) {
                        g2d.setColor(highColor);
                    }
                    g2d.setStroke(new BasicStroke((float) (downsampleFactor * fixations.getThicknessScalar())));

                    if (previousPoint != null) {
                        g2d.drawLine(
                                (int) point.getX(), (int) point.getY(),
                                (int) previousPoint.getX(), (int) previousPoint.getY());
                    }

                    if (fixations.getFixations() != null) {
                        g2d.setStroke(new BasicStroke((float) downsampleFactor));
                        double circleSizeCoef = (downsampleFactor * fixations.getDurations()[i] / 30) *
                                fixations.getDurationSizeScalarScalar();

                        g2d.fillOval(
                                (int) point.getX() - (int) (circleSizeCoef / 2),
                                (int) point.getY() - (int) (circleSizeCoef / 2),
                                (int) circleSizeCoef,
                                (int) circleSizeCoef);

                        if (doPaintNumbers) {
                            Font font = new Font("Impact", Font.BOLD, (int) (30 * downsampleFactor));
                            g2d.setFont(font);
                            String str = Integer.toString(i + 1);
                            g2d.setColor(Color.WHITE);
                            g2d.drawString(str, (int) point.getX(), (int) point.getY());

                            GlyphVector gv = font.createGlyphVector(g2d.getFontRenderContext(), str);
                            Shape s = gv.getOutline((int) point.getX(), (int) point.getY());
                            g2d.setColor(Color.BLACK);
                            g2d.draw(s);
                        }
                    }
                }
                previousPoint = point;
            }
        }
    }


    public TrackerFeatures getTrackerFeatures() {
        return trackerFeatures;
    }

}
