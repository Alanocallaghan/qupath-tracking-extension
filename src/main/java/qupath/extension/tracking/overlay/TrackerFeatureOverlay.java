package qupath.extension.tracking.overlay;

import qupath.extension.tracking.tracker.Fixations;
import qupath.extension.tracking.tracker.TrackerFeatures;
import qupath.extension.tracking.TrackerUtils;
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

//    TODO: Singleton class like heatmapoverlay
//    TODO: Separate class for bounds features?
    private final TrackerFeatures trackerFeatures;

    private boolean doPaintBoundFixations, doPaintSlowPans, doPaintZoomPeaks,
            doPaintBoundsTrail, doPaintCursorTrail, doPaintEyeTrail;

    private final QuPathViewer viewer;

    private boolean doPaintNumbers = false;
    private int lowZoomThreshold = 5, medZoomThreshold = 1;
    private double boundsThicknessScalar;

    public TrackerFeatureOverlay(TrackerFeatures trackerFeatures) {
        this.viewer = QuPathGUI.getInstance().getViewer();
        this.trackerFeatures = trackerFeatures;
    }

//    private void drawCursorTrail(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
//        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));
//
//        double[] zoomArray = trackerFeatures.getZoomArray();
//
//        Point2D[] cursorArray = trackerFeatures.getCursorArray();
//        for (int i = 0; i < cursorArray.length; i++) {
//            if (i > 0 && cursorArray[i] != null && cursorArray[i - 1] != null) {
//
//                if(zoomArray[i] < lowZoomThreshold) {
//                    g2d.setColor(Color.GREEN);
//                    if (zoomArray[i] < medZoomThreshold) {
//                        g2d.setColor(Color.RED);
//                    }
//                } else {
//                    g2d.setColor(Color.BLUE);
//                }
//
//                Line2D line2D = new Line2D.Double( cursorArray[i].getX(), cursorArray[i].getY(),
//                        cursorArray[i - 1].getX(), cursorArray[i - 1].getY());
//                if(line2D.intersects(clippingRectangle)) {
//                    g2d.draw(line2D);
//                }
//            }
//        }
//    }

//    private void drawEyeTrail(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
//        g2d.setColor(Color.CYAN);
//        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));
//
//        double[] zoomArray = trackerFeatures.getZoomArray();
//
//        double[] zoomLevel;
//        if (trackerFeatures.eyeFixations == null) {
//            zoomLevel = zoomArray;
//        } else {
//            zoomLevel = new double[trackerFeatures.eyeFixations.getFixations().size()];
//            int j = 0;
//            for (ArrayList<ViewRecordingFrame> frames : trackerFeatures.eyeFixations.getFixations()) {
//                zoomLevel[j] = trackerFeatures.eyeFixations.calculateAverageZoom(trackerFeatures.eyeFixations.getFixations().get(j++));
//            }
//        }
//        Point2D previousPoint = null;
//        for(int i = 0; i < trackerFeatures.eyeFixations.getCentroids().length; i++) {
//            Point2D point = trackerFeatures.eyeFixations.getCentroids()[i];
//            if(point!=null) {
//                if (clippingRectangle.contains(point)) {
//
//                    g2d.setStroke(new BasicStroke((float) downsampleFactor));
//
//                    g2d.setColor(Color.BLUE);
//                    if (zoomLevel[i] < lowZoomThreshold) {
//                        g2d.setColor(Color.GREEN);
//                    }
//                    if (zoomLevel[i] < medZoomThreshold) {
//                        g2d.setColor(Color.RED);
//                    }
//
//                    double circleSizeCoef = (downsampleFactor * trackerFeatures.eyeFixations.getDurations()[i] / 30);
//
//                    g2d.fillOval((int) point.getX() - (int) (circleSizeCoef / 2), (int) point.getY() - (int) (circleSizeCoef / 2),
//                            (int) circleSizeCoef, (int) circleSizeCoef);
//                    if (previousPoint != null) {
//                        g2d.drawLine((int) point.getX(), (int) point.getY(), (int) previousPoint.getX(), (int) previousPoint.getY());
//                    }
//                    if(trackerFeatures.eyeFixations.getFixations() != null && doPaintNumbers) {
//
//                        Font font = new Font("Impact", Font.BOLD, (int) (30 * downsampleFactor));
//                        g2d.setFont(font);
//                        String str = Integer.toString(i + 1);
//                        g2d.setColor(Color.WHITE);
//
//                        g2d.drawString(str, (int)point.getX(), (int)point.getY());
//
//                        GlyphVector gv = font.createGlyphVector(g2d.getFontRenderContext(), str);
//                        Shape s = gv.getOutline((int)point.getX(),(int) point.getY());
//                        g2d.setColor(Color.BLACK);
//
//                        g2d.draw(s);
//                    }
//                }
//                previousPoint = point;
//            }
//        }
//    }


    @Override
    public void paintOverlay(Graphics2D g2d, ImageRegion imageRegion, double downsampleFactor, ImageObserver observer, boolean paintCompletely) {
        Rectangle clippingRectangle = new Rectangle(imageRegion.getX(),imageRegion.getY(),imageRegion.getWidth(),imageRegion.getHeight());
        if (trackerFeatures != null) {
            if (doPaintBoundsTrail) {
                if (trackerFeatures.getBoundsArray() != null) {
                    drawBoundsTrail(g2d, downsampleFactor, clippingRectangle);
                }
            }
            if (doPaintCursorTrail) {
                if (trackerFeatures.getCursorFixations() != null) {
                    drawTrail(g2d, downsampleFactor, clippingRectangle,
                            trackerFeatures.getCursorFixations());
                }
            }
            if (doPaintEyeTrail) {
                if (trackerFeatures.getEyeFixations() != null) {
                    drawTrail(g2d, downsampleFactor, clippingRectangle,
                            trackerFeatures.getEyeFixations());
                }
            }
            if (doPaintZoomPeaks) {
                if (trackerFeatures.getBoundsArray() != null) {
                    //                drawBoundsTrail(g2d, downsampleFactor, clippingRectangle);
                }
            }
            if (doPaintSlowPans) {
                if (trackerFeatures.getCursorArray() != null) {
                    //                drawCursorTrail(g2d, downsampleFactor, clippingRectangle);
                }
            }
            if (doPaintBoundFixations) {
                if (trackerFeatures.getBoundsArray() != null) {
                    //                drawEyeTrail(g2d, downsampleFactor, clippingRectangle);
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
                g2d.draw(rect);
            }
            previousRect = rect;
        }
    }

    //todo: handle all points (no fixations/centroids/durations)
    private void drawTrail(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle, Fixations fixations) {
        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));
        double[] zoomArray = trackerFeatures.getZoomArray();

        double[] zoomLevel;
        Point2D[] points;
        if (fixations.getFixations() == null) {
            zoomLevel = zoomArray;
            points = trackerFeatures.getArray(fixations.getFeatureType());
        } else {
            zoomLevel = new double[fixations.getFixations().size()];
            for (int j = 0; j < fixations.getFixations().size(); j++) {
                zoomLevel[j] = fixations.calculateAverageZoom(fixations.getFixations().get(j));
            }
            points = fixations.getCentroids();
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

    public void setDoPaintCursorTrail(boolean paintCursorTrail) {
        this.doPaintCursorTrail = paintCursorTrail;
        this.trackerFeatures.getCursorFixations().recalculateFixations();
        this.viewer.repaint();
    }

    public void setDoPaintEyeTrail(boolean paintEyeTrail) {
        this.doPaintEyeTrail = paintEyeTrail;
        this.trackerFeatures.getEyeFixations().recalculateFixations();
        this.viewer.repaint();
    }

    public void setDoPaintBoundsTrail(boolean paintBoundsTrail) {
        this.doPaintBoundsTrail = paintBoundsTrail;
        this.viewer.repaint();
    }

    public void setEyeFixationType(String fixationType) {
        if (fixationType == null) fixationType = "Eyetribe";
        this.trackerFeatures.getEyeFixations().setFixationType(fixationType);
        this.viewer.repaint();
    }

    public void setCursorFixationType(String fixationType) {
        if (fixationType == null) fixationType = "IVT";
        this.trackerFeatures.getCursorFixations().setFixationType(fixationType);
        this.viewer.repaint();
    }
//
//    void setDoPaintBoundFixations(boolean doPaintBoundFixations) {
//        this.doPaintBoundFixations = doPaintBoundFixations;
//        this.viewer.repaint();
//    }
//
//    void setDoPaintZoomPeaks(boolean doPaintZoomPeaks) {
//        this.doPaintZoomPeaks = doPaintZoomPeaks;
//        this.viewer.repaint();
//    }
//
//    void setDoPaintSlowPans(boolean doPaintSlowPans) {
//        this.doPaintSlowPans = doPaintSlowPans;
//        this.viewer.repaint();
//    }
//
//
//    public void setDoPaintNumbers(boolean doPaintNumbers) {
//        this.doPaintNumbers = doPaintNumbers;
//        this.viewer.repaint();
//    }

    public void setEyeFixationColor(String level, javafx.scene.paint.Color color) {
        this.trackerFeatures.getEyeFixations().setColor(level,
                TrackerUtils.colorFXtoAWT(color));
        this.viewer.repaint();
    }

    public void setCursorFixationColor(String level, javafx.scene.paint.Color color) {
        this.trackerFeatures.getCursorFixations().setColor(level,
                TrackerUtils.colorFXtoAWT(color));
        this.viewer.repaint();
    }

    public void setEyeThicknessScalar(Number eyeThicknessScalar) {
        this.trackerFeatures.getEyeFixations().setThicknessScalar(eyeThicknessScalar.doubleValue());
        this.viewer.repaint();
    }

    public void setEyeIVTSpeedThreshold(Number IVTSpeedThreshold) {
        this.trackerFeatures.getEyeFixations().setIVTSpeedThreshold(IVTSpeedThreshold.doubleValue());
        this.viewer.repaint();
    }

    public void setEyeIDTDurationThreshold(Number IDTDurationThreshold) {
        this.trackerFeatures.getEyeFixations().setIDTDurationThreshold(IDTDurationThreshold.doubleValue());
        this.viewer.repaint();
    }

    public void setEyeIDTDispersionThreshold(Number IDTDispersionThreshold) {
        this.trackerFeatures.getEyeFixations().setIDTDispersionThreshold(IDTDispersionThreshold.doubleValue());
        this.viewer.repaint();
    }

    public void setCursorIVTSpeedThreshold(Number IVTSpeedThreshold) {
        this.trackerFeatures.getCursorFixations().setIVTSpeedThreshold(IVTSpeedThreshold.doubleValue());
        this.viewer.repaint();
    }

    public void setCursorIDTDurationThreshold(Number IDTDurationThreshold) {
        this.trackerFeatures.getCursorFixations().setIDTDurationThreshold(IDTDurationThreshold.doubleValue());
        this.viewer.repaint();
    }

    public void setCursorIDTDispersionThreshold(Number IDTDispersionThreshold) {
        this.trackerFeatures.getCursorFixations().setIDTDispersionThreshold(IDTDispersionThreshold.doubleValue());
        this.viewer.repaint();
    }

    public void setCursorThicknessScalar(Number cursorThicknessScalar) {
        this.trackerFeatures.getCursorFixations().setThicknessScalar(cursorThicknessScalar.doubleValue());
        this.viewer.repaint();
    }

    public void setBoundsThicknessScalar(Number boundsThicknessScalar) {
        this.boundsThicknessScalar = boundsThicknessScalar.doubleValue();
        this.viewer.repaint();
    }
}
