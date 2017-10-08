package qupath.extension.tracking.overlay;

import javafx.beans.property.*;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPrefs;
import qupath.extension.tracking.tracker.Fixations;
import qupath.extension.tracking.tracker.TrackerFeature;
import qupath.extension.tracking.tracker.TrackerFeatureList;
import qupath.extension.tracking.tracker.TrackerFeatures;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.overlays.AbstractOverlay;
import qupath.lib.regions.ImageRegion;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;

/**
 * @author Alan O'Callaghan
 * Created by alan on 15/03/17.
 */
public class TrackerFeatureOverlay extends AbstractOverlay {

    //    TODO: Separate class for bounds features?
    private final TrackerFeatures trackerFeatures;

    private BooleanProperty doPaintBoundFixations = new SimpleBooleanProperty(false),
            doPaintSlowPans = new SimpleBooleanProperty(false),
            doPaintZoomPeaks = new SimpleBooleanProperty(false),
            doPaintBoundsTrail = new SimpleBooleanProperty(false),
            doPaintCursorTrail = new SimpleBooleanProperty(false),
            doPaintEyeTrail = new SimpleBooleanProperty(false);

    private final QuPathViewer viewer;

    //    TODO: Cursor and eye numbers
    private final boolean doPaintNumbers = false;
    private final int lowZoomThreshold = 5;
    private final int medZoomThreshold = 1;
    private DoubleProperty boundsThicknessScalar = new SimpleDoubleProperty();

    public TrackerFeatureOverlay(TrackerFeatures trackerFeatures) {
        this.viewer = QuPathGUI.getInstance().getViewer();
        this.trackerFeatures = trackerFeatures;

        boundsThicknessScalar.bind(TrackingPrefs.boundsPrefs.getThicknessScalarProperty());
        boundsThicknessScalar.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintBoundFixations.bind(TrackingPrefs.boundsPrefs.doPaintFixations);
        doPaintBoundFixations.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintCursorTrail.bind(TrackingPrefs.cursorPointPrefs.getDoPaintTrailProperty());
        doPaintCursorTrail.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintEyeTrail.bind(TrackingPrefs.eyePointPrefs.getDoPaintTrailProperty());
        doPaintEyeTrail.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintSlowPans.bind(TrackingPrefs.boundsPrefs.doPaintSlowPans);
        doPaintSlowPans.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintBoundFixations.bind(TrackingPrefs.boundsPrefs.doPaintFixations);
        doPaintBoundFixations.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintZoomPeaks.bind(TrackingPrefs.boundsPrefs.doPaintZoomPeaks);
        doPaintZoomPeaks.addListener((observable, oldValue, newValue) -> viewer.repaint());
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
                if (trackerFeatures.getCursorFixations() != null) {
                    drawTrail(g2d, downsampleFactor, clippingRectangle,
                            trackerFeatures.getCursorFixations());
                }
            }
            if (doPaintEyeTrail.get()) {
                if (trackerFeatures.getEyeFixations() != null) {
                    drawTrail(g2d, downsampleFactor, clippingRectangle,
                            trackerFeatures.getEyeFixations());
                }
            }
            if (doPaintZoomPeaks.get()) {
                if (trackerFeatures.getBoundsArray() != null) {
                    drawZoomPeaks(g2d, downsampleFactor, clippingRectangle);
                }
            }
            if (doPaintSlowPans.get()) {
                if (trackerFeatures.getCursorArray() != null) {
                    drawSlowPans(g2d, downsampleFactor, clippingRectangle);
                }
            }
            if (doPaintBoundFixations.get()) {
                if (trackerFeatures.getBoundsArray() != null) {
                    drawBoundsFixations(g2d, downsampleFactor, clippingRectangle);
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
                g2d.setColor(Color.BLUE);
                if (zoomArray[i] < lowZoomThreshold) {
                    g2d.setColor(Color.GREEN);
                } else if (zoomArray[i] < medZoomThreshold) {
                    g2d.setColor(Color.RED);
                }
                g2d.setStroke(new BasicStroke(boundsThicknessScalar.floatValue()));
                g2d.draw(rect);
            }
            previousRect = rect;
        }
    }

    private void drawZoomPeaks(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
        g2d.setColor(Color.CYAN);
        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));
        Rectangle rect;
        for (int i = 0; i < trackerFeatures.getBoundsFeatures().getZoomPeaks().size(); i++) {
            rect = trackerFeatures.getBoundsFeatures().getZoomPeaks().get(i).getFrameAtFeatureIndex(0).getImageBounds();
            if (rect.intersects(clippingRectangle)) {
                g2d.draw(rect);
            }
        }
    }


    private void drawTrail(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle, Fixations fixations) {
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

//                  TODO: Toggle colors for zoom levels
                    Color lowColor = fixations.getLowColor();
                    Color medColor = fixations.getMedColor();
                    Color highColor = fixations.getHighColor();

                    g2d.setColor(lowColor);
                    if (zoomLevel[i] < lowZoomThreshold) {
                        g2d.setColor(medColor);
                    } else if (zoomLevel[i] < medZoomThreshold) {
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
                        double circleSizeCoef = (downsampleFactor * fixations.getDurations()[i] / 30);

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

    private void drawSlowPans(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));
        g2d.setColor(Color.BLUE);

        TrackerFeatureList slowPans = trackerFeatures.getBoundsFeatures().getSlowPans();

        for (TrackerFeature slowPan : slowPans) {
            Rectangle startRect = slowPan.getFrameAtFeatureIndex(0).getImageBounds();
            Rectangle endRect = slowPan.getFrameAtFeatureIndex(slowPan.size() - 1).getImageBounds();
            g2d.setColor(Color.MAGENTA);
            if (startRect.intersects(clippingRectangle))
                g2d.draw(startRect);
            g2d.setColor(Color.GREEN);
            if (endRect.intersects(clippingRectangle))
                g2d.draw(endRect);
            g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));
            g2d.setColor(Color.RED);

            for (Line2D line : makeSlowPanLines(startRect, endRect)) {
                if (line.intersects(clippingRectangle)) {
                    g2d.draw(line);
                }
            }
        }
    }


    private static Line2D[] makeSlowPanLines(Rectangle currentRect, Rectangle previousRect) {
        return new Line2D[]{
                new Line2D.Double(currentRect.getX(), currentRect.getY(), previousRect.getX(), previousRect.getY()),
                new Line2D.Double(currentRect.getX() + currentRect.getWidth(), currentRect.getY(), previousRect.getX() + previousRect.getWidth(), previousRect.getY()),
                new Line2D.Double(currentRect.getX(), currentRect.getY() + currentRect.getHeight(), previousRect.getX(), previousRect.getY() + previousRect.getHeight()),
                new Line2D.Double(currentRect.getX() + currentRect.getWidth(), currentRect.getY() + currentRect.getHeight(), previousRect.getX() + previousRect.getWidth(), previousRect.getY() + previousRect.getHeight())};
    }

    private void drawBoundsFixations(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke((downsampleFactor > 2) ? (float) (downsampleFactor*2) : 2));
        Rectangle rectangle;
        for (int i = 0; i < trackerFeatures.getBoundsFeatures().getBoundsFixations().size(); i++) {
            rectangle = trackerFeatures.getBoundsFeatures().getBoundsFixations().get(i).getFrameAtFeatureIndex(0).getImageBounds();
            if(rectangle.intersects(clippingRectangle)) {
                g2d.draw(rectangle);
            }
        }
    }


    public TrackerFeatures getTrackerFeatures() {
        return trackerFeatures;
    }

}
