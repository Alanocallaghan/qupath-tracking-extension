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

    private boolean paintBoundsTrail;
    private boolean paintBoundFixations;
    private boolean paintSlowPans;
    private boolean paintZoomPeaks;
    private boolean paintCursorTrail;
    private boolean paintEyeTrail;

    private final QuPathViewer viewer;


    private boolean[] paintEachZoomPeak;
    private boolean[] paintEachBoundsFixation;
    private boolean[] paintEachSlowPan;

    //    private PaintOptionFrame paintOptionFrame;
    private Rectangle[] boundsArray = null;
    private double[] zoomArray = null;
    private Fixations eyeFixations = null;
    private Point2D[] fixationCentroids;
    private double[] fixationDurations;

    private FixationType fixationType = EYETRIBE;

    private ArrayList<ArrayList<ViewRecordingFrame>> fixations;
    private boolean paintNumbers = false;
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
        viewer.addOverlay(this);
//        trackerFeatures.setTrackerVisualOverlay(this);
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
//
//        if (eyeFixations==null) {
//            eyeFixations = trackerFeatures.getEyeFixations();
//            updateFixationType();
//        }
//

        double[] zoomLevel;
        if (fixations == null) {
            zoomLevel = zoomArray;
        } else {
            zoomLevel = new double[fixations.size()];
            int j = 0;
            for (ArrayList<ViewRecordingFrame> frames : fixations) {
                zoomLevel[j] = eyeFixations.calculateAverageZoom(fixations.get(j++));
            }
        }
        Point2D previousPoint = null;
        for(int i = 0; i < fixationCentroids.length; i++) {
            Point2D point = fixationCentroids[i];
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

                    double circleSizeCoef = (downsampleFactor * fixationDurations[i] / 30);

                    g2d.fillOval((int) point.getX() - (int) (circleSizeCoef / 2), (int) point.getY() - (int) (circleSizeCoef / 2),
                            (int) circleSizeCoef, (int) circleSizeCoef);
                    if (previousPoint != null) {
                        g2d.drawLine((int) point.getX(), (int) point.getY(), (int) previousPoint.getX(), (int) previousPoint.getY());
                    }
                    if(fixations!=null && paintNumbers) {

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
        switch (fixationType) {
            case EYETRIBE:
                fixationCentroids = eyeFixations.getEyeTribeCentroids();
                fixationDurations = eyeFixations.getEyeTribeDurations();
                fixations = eyeFixations.getEyeTribeFixations();
                viewer.repaint();
                break;
            case IDT:
                fixationCentroids = eyeFixations.getIDTCentroids();
                fixationDurations = eyeFixations.getIDTDurations();
                fixations = eyeFixations.getIDTFixations();
                viewer.repaint();
                break;
            case IVT:
                fixationCentroids = eyeFixations.getIVTCentroids();
                fixationDurations = eyeFixations.getIVTDurations();
                fixations = eyeFixations.getIVTFixations();
                viewer.repaint();
                break;
            case ALL_POINTS:
                fixationCentroids = trackerFeatures.getEyeArray();
                fixationDurations = trackerFeatures.getZoomArray();
                fixations = null;
                viewer.repaint();
        }
    }

    @Override
    public void paintOverlay(Graphics2D g2d, ImageRegion imageRegion, double downsampleFactor, ImageObserver observer, boolean paintCompletely) {

        Rectangle clippingRectangle = new Rectangle(imageRegion.getX(),imageRegion.getY(),imageRegion.getWidth(),imageRegion.getHeight());
        if (trackerFeatures.getBoundsArray() != null) {
            if(paintBoundsTrail) {
                drawBoundsTrail(g2d, downsampleFactor, clippingRectangle);
            }
        }

        if (trackerFeatures.getCursorArray() != null) {
            if(paintCursorTrail) {
                drawCursorTrail(g2d, downsampleFactor, clippingRectangle);
            }
        }
        if (trackerFeatures.getEyeArray() != null) {
            if(paintEyeTrail) {
                drawEyeTrail(g2d, downsampleFactor, clippingRectangle);
            }
        }
    }

    public void setPaintCursorTrail(boolean paintCursorTrail) {
        this.paintCursorTrail = paintCursorTrail;
    }

    public void setPaintEyeTrail(boolean paintEyeTrail) {
        this.paintEyeTrail = paintEyeTrail;
    }

    public void setPaintBoundsTrail(boolean paintBoundsTrail) {
        this.paintBoundsTrail = paintBoundsTrail;
    }

    public void setFixationType(FixationType fixationType) {
        this.fixationType = fixationType;
        updateFixationType();
    }

    public void setPaintNumbers(boolean paintNumbers) {
        this.paintNumbers = paintNumbers;
    }

}
