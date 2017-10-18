package qupath.extension.tracking.overlay;

import javafx.beans.property.*;
import qupath.extension.tracking.TrackerUtils;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPrefs;
import qupath.extension.tracking.tracker.TrackerFeature;
import qupath.extension.tracking.tracker.TrackerFeatureList;
import qupath.extension.tracking.tracker.TrackerFeatures;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.overlays.AbstractOverlay;
import qupath.lib.regions.ImageRegion;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.ImageObserver;

public class BoundsFeaturesOverlay extends AbstractOverlay {

    private final BooleanProperty doPaintZoomPeaks = new SimpleBooleanProperty(),
            doPaintSlowPans = new SimpleBooleanProperty(),
            doPaintBoundFixations = new SimpleBooleanProperty();
    private final TrackerFeatures trackerFeatures;
    private final QuPathViewer viewer;
    private final SimpleObjectProperty zoomPeakStartColorProperty = new SimpleObjectProperty(),
            zoomPeakPathColorProperty = new SimpleObjectProperty(),
            zoomPeakEndColorProperty = new SimpleObjectProperty();
    private DoubleProperty boundsThicknessScalar = new SimpleDoubleProperty();

    private ObjectProperty boundsLowColorProperty = new SimpleObjectProperty(),
            boundsMedColorProperty = new SimpleObjectProperty(),
            boundsHighColorProperty = new SimpleObjectProperty();
    private DoubleProperty lowZoomThreshold = new SimpleDoubleProperty(),
            medZoomThreshold = new SimpleDoubleProperty();

    public BoundsFeaturesOverlay(TrackerFeatures trackerFeatures) {
        viewer = QuPathGUI.getInstance().getViewer();
        this.trackerFeatures = trackerFeatures;


        boundsLowColorProperty.bind(TrackingPrefs.boundsPrefs.getLowColorProperty());
        boundsLowColorProperty.addListener((observable, oldValue, newValue) -> viewer.repaint());

        boundsMedColorProperty.bind(TrackingPrefs.boundsPrefs.getMedColorProperty());
        boundsMedColorProperty.addListener((observable, oldValue, newValue) -> viewer.repaint());

        boundsHighColorProperty.bind(TrackingPrefs.boundsPrefs.getHighColorProperty());
        boundsHighColorProperty.addListener((observable, oldValue, newValue) -> viewer.repaint());


        lowZoomThreshold.bind(TrackingPrefs.lowZoomThreshold);
        lowZoomThreshold.addListener((observable, oldValue, newValue) -> viewer.repaint());

        medZoomThreshold.bind(TrackingPrefs.medZoomThreshold);
        medZoomThreshold.addListener((observable, oldValue, newValue) -> viewer.repaint());



        zoomPeakStartColorProperty.bind(TrackingPrefs.boundsPrefs.zoomPeakStartColorProperty);
        zoomPeakStartColorProperty.addListener((observable, oldValue, newValue) -> viewer.repaint());

        zoomPeakPathColorProperty.bind(TrackingPrefs.boundsPrefs.zoomPeakPathColorProperty);
        zoomPeakPathColorProperty.addListener((observable, oldValue, newValue) -> viewer.repaint());

        zoomPeakEndColorProperty.bind(TrackingPrefs.boundsPrefs.zoomPeakEndColorProperty);
        zoomPeakEndColorProperty.addListener((observable, oldValue, newValue) -> viewer.repaint());



        boundsThicknessScalar.bind(TrackingPrefs.boundsPrefs.getThicknessScalarProperty());
        boundsThicknessScalar.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintSlowPans.bind(TrackingPrefs.boundsPrefs.doPaintSlowPans);
        doPaintSlowPans.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintBoundFixations.bind(TrackingPrefs.boundsPrefs.doPaintFixations);
        doPaintBoundFixations.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintZoomPeaks.bind(TrackingPrefs.boundsPrefs.doPaintZoomPeaks);
        doPaintZoomPeaks.addListener((observable, oldValue, newValue) -> viewer.repaint());

    }


    private void drawSlowPans(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
        g2d.setStroke(new BasicStroke((float)boundsThicknessScalar.get() *
                ((downsampleFactor > 1) ? (float) downsampleFactor : 1)));
        g2d.setColor(Color.BLUE);

        TrackerFeatureList slowPans = trackerFeatures.getBoundsFeatures().getSlowPans();

        for (TrackerFeature slowPan : slowPans) {
            Rectangle startRect = slowPan.getFrameAtFeatureIndex(0).getImageBounds();
            Rectangle endRect = slowPan.getFrameAtFeatureIndex(slowPan.size() - 1).getImageBounds();
            g2d.setColor(TrackerUtils.colorFXtoAWT(
                    (javafx.scene.paint.Color)zoomPeakStartColorProperty.get()));

            if (startRect.intersects(clippingRectangle))
                g2d.draw(startRect);
            g2d.setColor(TrackerUtils.colorFXtoAWT(
                    (javafx.scene.paint.Color)zoomPeakEndColorProperty.get()));
            if (endRect.intersects(clippingRectangle))
                g2d.draw(endRect);
            g2d.setStroke(new BasicStroke((float)boundsThicknessScalar.get() *
                    ((downsampleFactor > 1) ? (float) downsampleFactor : 1)));
            g2d.setColor(TrackerUtils.colorFXtoAWT(
                    (javafx.scene.paint.Color)zoomPeakPathColorProperty.get()));
            for (Line2D line : makeSlowPanLines(startRect, endRect)) {
                if (line.intersects(clippingRectangle)) {
                    g2d.draw(line);
                }
            }
        }
    }


    private static Line2D[] makeSlowPanLines(Rectangle currentRect, Rectangle previousRect) {
        return new Line2D[] {
                new Line2D.Double(currentRect.getX(),
                        currentRect.getY(),
                        previousRect.getX(),
                        previousRect.getY()),
                new Line2D.Double(currentRect.getX() + currentRect.getWidth(),
                        currentRect.getY(),
                        previousRect.getX() + previousRect.getWidth(),
                        previousRect.getY()),
                new Line2D.Double(currentRect.getX(),
                        currentRect.getY() + currentRect.getHeight(),
                        previousRect.getX(),
                        previousRect.getY() + previousRect.getHeight()),
                new Line2D.Double(currentRect.getX() + currentRect.getWidth(),
                        currentRect.getY() + currentRect.getHeight(),
                        previousRect.getX() + previousRect.getWidth(),
                        previousRect.getY() + previousRect.getHeight())
        };
    }

    private void drawBoundsFixations(Graphics2D g2d, double downsampleFactor,
                                     Rectangle clippingRectangle) {
//        g2d.setColor(Color.BLUE);
        double[] zoomArray = trackerFeatures.getZoomArray();

        g2d.setStroke(new BasicStroke((float)boundsThicknessScalar.get() *
                ((downsampleFactor > 1) ? (float) downsampleFactor : 1)));
        Rectangle rectangle;
        for (int i = 0; i < trackerFeatures.getBoundsFeatures().getBoundsFixations().size(); i++) {

            g2d.setColor(TrackerUtils.colorFXtoAWT(
                    (javafx.scene.paint.Color)boundsLowColorProperty.get()));
            int index = trackerFeatures.getBoundsFeatures().getBoundsFixations().get(i).get(0);
            if (zoomArray[index] < lowZoomThreshold.get()) {
                g2d.setColor(TrackerUtils.colorFXtoAWT(
                        (javafx.scene.paint.Color)boundsMedColorProperty.get()));
            }
            if (zoomArray[index] < medZoomThreshold.get()) {
                g2d.setColor(TrackerUtils.colorFXtoAWT(
                        (javafx.scene.paint.Color)boundsHighColorProperty.get()));
            }

            rectangle = trackerFeatures.getBoundsFeatures().getBoundsFixations()
                    .get(i).getFrameAtFeatureIndex(0).getImageBounds();
            if(rectangle.intersects(clippingRectangle)) {
                g2d.draw(rectangle);
            }
        }
    }

    private void drawZoomPeaks(Graphics2D g2d, double downsampleFactor,
                               Rectangle clippingRectangle) {

        double[] zoomArray = trackerFeatures.getZoomArray();
//        g2d.setColor(Color.CYAN);
        g2d.setStroke(new BasicStroke((float)boundsThicknessScalar.get() *
                ((downsampleFactor > 1) ? (float) downsampleFactor : 1)));
        Rectangle rect;
        for (int i = 0; i < trackerFeatures.getBoundsFeatures().getZoomPeaks().size(); i++) {
            g2d.setColor(TrackerUtils.colorFXtoAWT(
                    (javafx.scene.paint.Color)boundsLowColorProperty.get()));
            int index = trackerFeatures.getBoundsFeatures().getZoomPeaks().get(i).get(0);
            if (zoomArray[index] < lowZoomThreshold.get()) {
                g2d.setColor(TrackerUtils.colorFXtoAWT(
                        (javafx.scene.paint.Color)boundsMedColorProperty.get()));
            }
            if (zoomArray[index] < medZoomThreshold.get()) {
                g2d.setColor(TrackerUtils.colorFXtoAWT(
                        (javafx.scene.paint.Color)boundsHighColorProperty.get()));
            }

            rect = trackerFeatures.getBoundsFeatures().getZoomPeaks()
                    .get(i).getFrameAtFeatureIndex(0).getImageBounds();
            if (rect.intersects(clippingRectangle)) {
                g2d.draw(rect);
            }
        }
    }

    @Override
    public void paintOverlay(Graphics2D g2d, ImageRegion imageRegion,
                             double downsampleFactor, ImageObserver imageObserver, boolean b) {

        Rectangle clippingRectangle = new Rectangle(
                imageRegion.getX(), imageRegion.getY(),
                imageRegion.getWidth(), imageRegion.getHeight());
        if (doPaintZoomPeaks.get()) {
            if (trackerFeatures.getBoundsArray() != null) {
                drawZoomPeaks(g2d, downsampleFactor, clippingRectangle);
            }
        }
        if (doPaintSlowPans.get()) {
            if (trackerFeatures.getBoundsArray() != null) {
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
