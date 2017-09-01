package qupath.extension.tracking;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.overlays.AbstractOverlay;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.regions.ImageRegion;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

import static qupath.extension.tracking.TrackerFeatureOverlay.FixationType.EYETRIBE;

/**
 * Created by alan on 15/03/17. 
 * @author Alan O'Callaghan
 */
public class TrackerFeatureOverlay extends AbstractOverlay {

//    TODO: Singleton class like heatmapoverlay
//    TODO: Separate class for bounds features?
    private final TrackerFeatures trackerFeatures;

    boolean doPaintBoundFixations;
    boolean doPaintSlowPans;
    boolean doPaintZoomPeaks;

    boolean doPaintBoundsTrail;
    boolean doPaintCursorTrail;
    boolean doPaintEyeTrail;

    private final QuPathViewer viewer;


    private boolean[] paintEachZoomPeak;
    private boolean[] paintEachBoundsFixation;
    private boolean[] paintEachSlowPan;

    //    private PaintOptionFrame paintOptionFrame;
    private Rectangle[] boundsArray = null;
    private double[] zoomArray = null;
    private Fixations eyeFixations = null;

    private FixationType fixationType = EYETRIBE;

    private boolean doPaintNumbers = false;
    private int lowZoomThreshold = 5, medZoomThreshold = 1;

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
            } else if(this.equals(ALL_POINTS)) {
                return "All Points";
            } else {
                return null;
            }
        }
    }

    public TrackerFeatureOverlay(TrackerFeatures trackerFeatures) {
        this.viewer = QuPathGUI.getInstance().getViewer();
        this.trackerFeatures = trackerFeatures;
        eyeFixations = new Fixations(trackerFeatures);
        setFixationType(null);
        viewer.addOverlay(this);
    }

    private void drawBoundsTrail(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));

        Rectangle rect;
        if(boundsArray == null) {
            this.boundsArray = trackerFeatures.getBoundsArray();
        }
        Rectangle previousRect = null;

        if(zoomArray==null) {
            zoomArray = trackerFeatures.getZoomArray();
        }

        for (int i = 0; i < boundsArray.length; i++) {
            rect = boundsArray[i];
            if ((!rect.equals(previousRect)) && rect.intersects(clippingRectangle)) {
                g2d.setColor(Color.BLUE);
                if(zoomArray[i] < lowZoomThreshold) {
                    g2d.setColor(Color.GREEN);
                } if (zoomArray[i] < medZoomThreshold) {
                    g2d.setColor(Color.RED);
                }
                g2d.draw(rect);
            }
            previousRect = rect;
        }
    }

    private void drawCursorTrail(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));

        if(zoomArray==null) {
            zoomArray = trackerFeatures.getZoomArray();
        }

        Point2D[] cursorArray = trackerFeatures.getCursorArray();
        for (int i = 0; i < cursorArray.length; i++) {
            if (i > 0 && cursorArray[i] != null && cursorArray[i - 1] != null) {

                if(zoomArray[i] < lowZoomThreshold) {
                    g2d.setColor(Color.GREEN);
                    if (zoomArray[i] < medZoomThreshold) {
                        g2d.setColor(Color.RED);
                    }
                } else {
                    g2d.setColor(Color.BLUE);
                }

                Line2D line2D = new Line2D.Double( cursorArray[i].getX(), cursorArray[i].getY(),
                        cursorArray[i - 1].getX(), cursorArray[i - 1].getY());
                if(line2D.intersects(clippingRectangle)) {
                    g2d.draw(line2D);
                }
            }
        }
    }

    private void drawEyeTrail(Graphics2D g2d, double downsampleFactor, Rectangle clippingRectangle) {
        g2d.setColor(Color.CYAN);
        g2d.setStroke(new BasicStroke((downsampleFactor > 1) ? (float) downsampleFactor : 1));

        if (zoomArray==null) {
            zoomArray = trackerFeatures.getZoomArray();
        }

        double[] zoomLevel;
        if (eyeFixations == null) {
            zoomLevel = zoomArray;
        } else {
            zoomLevel = new double[eyeFixations.getFixations().size()];
            int j = 0;
            for (ArrayList<ViewRecordingFrame> frames : eyeFixations.getFixations()) {
                zoomLevel[j] = eyeFixations.calculateAverageZoom(eyeFixations.getFixations().get(j++));
            }
        }
        Point2D previousPoint = null;
        for(int i = 0; i < eyeFixations.getCentroids().length; i++) {
            Point2D point = eyeFixations.getCentroids()[i];
            if(point!=null) {
                if (clippingRectangle.contains(point)) {

                    g2d.setStroke(new BasicStroke((float) downsampleFactor));

                    g2d.setColor(Color.BLUE);
                    if (zoomLevel[i] < lowZoomThreshold) {
                        g2d.setColor(Color.GREEN);
                    }
                    if (zoomLevel[i] < medZoomThreshold) {
                        g2d.setColor(Color.RED);
                    }

                    double circleSizeCoef = (downsampleFactor * eyeFixations.getDurations()[i] / 30);

                    g2d.fillOval((int) point.getX() - (int) (circleSizeCoef / 2), (int) point.getY() - (int) (circleSizeCoef / 2),
                            (int) circleSizeCoef, (int) circleSizeCoef);
                    if (previousPoint != null) {
                        g2d.drawLine((int) point.getX(), (int) point.getY(), (int) previousPoint.getX(), (int) previousPoint.getY());
                    }
                    if(eyeFixations.getFixations() != null && doPaintNumbers) {

                        Font font = new Font("Impact", Font.BOLD, (int) (30 * downsampleFactor));
                        g2d.setFont(font);
                        String str = Integer.toString(i + 1);
                        g2d.setColor(Color.WHITE);

                        g2d.drawString(str, (int)point.getX(), (int)point.getY());

                        GlyphVector gv = font.createGlyphVector(g2d.getFontRenderContext(), str);
                        Shape s = gv.getOutline((int)point.getX(),(int) point.getY());
                        g2d.setColor(Color.BLACK);

                        g2d.draw(s);
                    }
                }
                previousPoint = point;
            }
        }
    }

    private void updateFixationType() {
        eyeFixations.updateFixationType(fixationType);
    }

    @Override
    public void paintOverlay(Graphics2D g2d, ImageRegion imageRegion, double downsampleFactor, ImageObserver observer, boolean paintCompletely) {

        Rectangle clippingRectangle = new Rectangle(imageRegion.getX(),imageRegion.getY(),imageRegion.getWidth(),imageRegion.getHeight());
        if (trackerFeatures.getBoundsArray() != null) {
            if(doPaintBoundsTrail) {
                drawBoundsTrail(g2d, downsampleFactor, clippingRectangle);
            }
        }

        if (trackerFeatures.getCursorArray() != null) {
            if(doPaintCursorTrail) {
                drawCursorTrail(g2d, downsampleFactor, clippingRectangle);
            }
        }
        if (trackerFeatures.getEyeArray() != null) {
            if(doPaintEyeTrail) {
                drawEyeTrail(g2d, downsampleFactor, clippingRectangle);
            }
        }



//        if (trackerFeatures.getBoundsArray() != null) {
//            if(doPaintZoomPeaks) {
//                drawBoundsTrail(g2d, downsampleFactor, clippingRectangle);
//            }
//        }
//
//        if (trackerFeatures.getCursorArray() != null) {
//            if(doPaintSlowPans) {
//                drawCursorTrail(g2d, downsampleFactor, clippingRectangle);
//            }
//        }
//        if (trackerFeatures.() != null) {
//            if(doPaintBoundFixations) {
//                drawEyeTrail(g2d, downsampleFactor, clippingRectangle);
//            }
//        }
    }

    void setDoPaintCursorTrail(boolean paintCursorTrail) {
        this.doPaintCursorTrail = paintCursorTrail;
        this.viewer.repaint();
    }

    void setDoPaintEyeTrail(boolean paintEyeTrail) {
        this.doPaintEyeTrail = paintEyeTrail;
        this.viewer.repaint();
    }

    void setDoPaintBoundsTrail(boolean paintBoundsTrail) {
        this.doPaintBoundsTrail = paintBoundsTrail;
        this.viewer.repaint();
    }

    void setFixationType(FixationType fixationType) {
        if (fixationType == null) fixationType = EYETRIBE;
        this.fixationType = fixationType;
        updateFixationType();
        this.viewer.repaint();
    }

    public void setDoPaintBoundFixations(boolean doPaintBoundFixations) {
        this.doPaintBoundFixations = doPaintBoundFixations;
        this.viewer.repaint();
    }

    public void setDoPaintZoomPeaks(boolean doPaintZoomPeaks) {
        this.doPaintZoomPeaks = doPaintZoomPeaks;
        this.viewer.repaint();
    }

    public void setDoPaintSlowPans(boolean doPaintSlowPans) {
        this.doPaintSlowPans = doPaintSlowPans;
        this.viewer.repaint();
    }


    public void setDoPaintNumbers(boolean doPaintNumbers) {
        this.doPaintNumbers = doPaintNumbers;
        this.viewer.repaint();
    }

}
