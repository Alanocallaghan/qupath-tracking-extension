package qupath.extension.tracking.overlay;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPrefs;
import qupath.extension.tracking.tracker.TrackerFeatures;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.overlays.AbstractOverlay;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.regions.ImageRegion;


/**
 * @author Alan O'Callaghan
 *
 * Created by alan on 15/03/17.
 */
public class HeatmapOverlay extends AbstractOverlay {
    private final TrackerFeatures trackerFeatures;
    private final double scalex, scaley;
    public BooleanProperty doPaintEyeHeatmap = new SimpleBooleanProperty(false);
    public BooleanProperty doPaintCursorHeatmap = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty doPaintBoundsHeatmap = new SimpleBooleanProperty(false);

    private BufferedImage eyeHeatmap = null,
            cursorHeatmap = null,
            boundsHeatmap = null;

    private final QuPathViewer viewer;

    private boolean boundsHeatmapIsCalculating = false,
            eyeHeatmapIsCalculating = false,
            cursorHeatmapIsCalculating = false;

    public HeatmapOverlay(TrackerFeatures trackerFeatures) {
        this.trackerFeatures = trackerFeatures;
        this.viewer = QuPathGUI.getInstance().getViewer();

        doPaintBoundsHeatmap.bind(TrackingPrefs.boundsPrefs.getDoPaintHeatmapProperty());
        doPaintBoundsHeatmap.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintCursorHeatmap.bind(TrackingPrefs.cursorPointPrefs.getDoPaintHeatmapProperty());
        doPaintCursorHeatmap.addListener((observable, oldValue, newValue) -> viewer.repaint());

        doPaintEyeHeatmap.bind(TrackingPrefs.eyePointPrefs.getDoPaintHeatmapProperty());
        doPaintEyeHeatmap.addListener((observable, oldValue, newValue) -> viewer.repaint());

        ImageServer server = QuPathGUI.getInstance().getViewer().getServer();

//      thumbnail is null if no image loaded
        if (viewer.getThumbnail() != null) {
            //have to cast to double otherwise this all does nothing!!!
            scalex = (double)viewer.getThumbnail().getWidth() / (double)server.getWidth();
            scaley = (double)viewer.getThumbnail().getHeight() / (double)server.getHeight();
        } else {
            scalex = 0;
            scaley = 0;
        }
    }

    @Override
    public void paintOverlay(Graphics2D g2d, ImageRegion imageRegion, double downsampleFactor, ImageObserver observer, boolean paintCompletely) {
        if (trackerFeatures != null) {
            if (doPaintBoundsHeatmap.get()) {
                paintBoundsHeatmap(g2d);
            }
            if (doPaintCursorHeatmap.get()) {
                paintCursorHeatmap(g2d);
            }
            if (doPaintEyeHeatmap.get()) {
                paintEyeHeatmap(g2d);
            }
        }
    }

    private void paintBoundsHeatmap(Graphics2D g2d) {
        if (boundsHeatmap != null) {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g2d.drawImage(boundsHeatmap, 0, 0, viewer.getServerWidth(), viewer.getServerHeight(), null);
        } else {
            if (!boundsHeatmapIsCalculating) {
                final ProgressBarFrame progressBarFrame = new ProgressBarFrame(trackerFeatures.getTracker().nFrames());
                HeatmapWorker boundsHeatmapWorker = new HeatmapWorker(HeatmapType.BOUNDS, progressBarFrame);
                boundsHeatmapWorker.execute();
                boundsHeatmapWorker.addPropertyChangeListener(progressBarFrame);
                boundsHeatmapIsCalculating = true;
            }
            g2d.drawImage(boundsHeatmap, 0, 0, viewer.getServerWidth(), viewer.getServerHeight(), null);
        }
    }

    private void paintCursorHeatmap(Graphics2D g2d) {
        if (cursorHeatmap != null) {
            g2d.drawImage(cursorHeatmap, 0, 0, viewer.getServerWidth(), viewer.getServerHeight(), null);
        } else {
            if (!cursorHeatmapIsCalculating) {
                final ProgressBarFrame progressBarFrame = new ProgressBarFrame(trackerFeatures.getTracker().nFrames());
                HeatmapWorker cursorHeatmapWorker = new HeatmapWorker(HeatmapType.CURSOR, progressBarFrame);
                cursorHeatmapWorker.execute();
                cursorHeatmapWorker.addPropertyChangeListener(progressBarFrame);
                cursorHeatmapIsCalculating = true;
            }
        }
    }

    private void paintEyeHeatmap(Graphics2D g2d) {
        if(eyeHeatmap != null) {
            g2d.drawImage(eyeHeatmap, 0, 0, viewer.getServerWidth(), viewer.getServerHeight(), null);
        } else {
            if (!eyeHeatmapIsCalculating) {
                final ProgressBarFrame progressBarFrame = new ProgressBarFrame(trackerFeatures.getTracker().nFrames());
                HeatmapWorker eyeHeatmapWorker = new HeatmapWorker(HeatmapType.EYE, progressBarFrame);
                eyeHeatmapWorker.execute();
                eyeHeatmapWorker.addPropertyChangeListener(progressBarFrame);
                eyeHeatmapIsCalculating = true;
            }
        }
    }

    private Rectangle rescaleRectangle(Rectangle rectangle, double scaleX, double scaleY) {
        return new Rectangle((int)(rectangle.getX()*scaleX), (int)(rectangle.getY()*scaleY), (int)(rectangle.getWidth()*scaleX), (int)(rectangle.getHeight()*scaleY));
    }

//    Bresenham's line algorithm
    private void drawLine(int x, int y, int x2, int y2, ImageProcessor ip, long time) {
        int w = x2 - x ;
        int h = y2 - y ;
        int dx1, dy1, dx2, dy2;

        dx1 = (w < 0) ? -1 : 1;
        dy1 = (h < 0) ? -1 : 1;
        dx2 = (w < 0) ? -1 : 1;
        dy2 = (h < 0) ? -1 : 1;

        int longest = Math.abs(w) ;
        int shortest = Math.abs(h) ;
        if (!(longest > shortest)) {
            longest = Math.abs(h) ;
            shortest = Math.abs(w) ;
            dy2 = h < 0 ? -1: 1;
            dx2 = 0 ;
        }
        int numerator = longest >> 1;
        for (int i = 0; i <= longest; i++) {
            if(x > 0 && y > 0 && x < ip.getWidth() && y < ip.getHeight())
                ip.setf(x,y, ip.getf(x, y) + time);
            numerator += shortest ;
            if (!(numerator<longest)) {
                numerator -= longest ;
                x += dx1 ;
                y += dy1 ;
            } else {
                x += dx2 ;
                y += dy2 ;
            }
        }
    }

    private void fillRectangle(Rectangle rectangle, ImageProcessor ip, long time) {
        int x = (int)rectangle.getX();
        int y = (int)rectangle.getY();
        int w = (int)rectangle.getWidth();
        int h = (int)rectangle.getHeight();

        for(int i = x; i < x + w; i++) {
            for(int j = y; j < y + h; j++) {
                if(i >=0 && j >= 0 && i < ip.getWidth() && j < ip.getHeight())
                    ip.setf(i, j, ip.getf(i, j) + time);
            }
        }
    }

    public void setDoPaintBoundsHeatmap(boolean doPaintBoundsHeatmap) {
        this.doPaintBoundsHeatmap.set(doPaintBoundsHeatmap);
        this.viewer.repaint();
    }

    public void setDoPaintCursorHeatmap(boolean doPaintCursorHeatmap) {
        this.doPaintCursorHeatmap.set(doPaintCursorHeatmap);
        this.viewer.repaint();
    }

    public void setDoPaintEyeHeatmap(boolean doPaintEyeHeatmap) {
        this.doPaintEyeHeatmap.set(doPaintEyeHeatmap);
        this.viewer.repaint();
    }

    class ProgressBarFrame extends JFrame implements PropertyChangeListener {

        private final JProgressBar progressBar;
        private final JTextArea textArea = new JTextArea();
        private final int totalTasks;
        private int currentFrame = 0;

        private ProgressBarFrame(int totalTasks) {
            super("Calculating Heatmap...");
            this.totalTasks = totalTasks;
            JPanel panel = new JPanel(new BorderLayout());
            progressBar = new JProgressBar(0, 100);
            panel.add(progressBar);
            textArea.setText("0/" + totalTasks + " added");
            textArea.setEditable(false);
            panel.add(textArea, BorderLayout.SOUTH);
            add(panel);
            pack();
            setSize(new Dimension(500,70));

            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double width = screenSize.getWidth();
            double height = screenSize.getHeight();

            setLocation((int)width/2,(int)height/2);
            setVisible(true);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals("progress")) {
                int progress = (Integer)evt.getNewValue();
                progressBar.setValue(progress);
                textArea.setText(currentFrame + "/" + totalTasks + " added...");
            }
        }

        void setCurrentFrame(int currentFrame) {
            this.currentFrame = currentFrame;
            repaint();
        }
    }

    enum HeatmapType {
        BOUNDS, CURSOR, EYE
    }

    private class HeatmapWorker extends SwingWorker {

        private final HeatmapType type;
        private final ProgressBarFrame progressBarFrame;
        private final int totalTasks;

        private HeatmapWorker(HeatmapType type, ProgressBarFrame progressBarFrame) {
            super();
            this.type = type;
            this.progressBarFrame = progressBarFrame;
            addPropertyChangeListener(progressBarFrame);
            progressBarFrame.toFront();
            progressBarFrame.setAlwaysOnTop(true);
            totalTasks = trackerFeatures.getTracker().nFrames();
        }

        @Override
        protected Object doInBackground() throws Exception {
            if(type == HeatmapType.BOUNDS) {
                boundsHeatmap = makeBoundsHeatmap();
            } else if(type == HeatmapType.CURSOR) {
                cursorHeatmap = makeCursorHeatmap();
            } else if (type == HeatmapType.EYE) {
                eyeHeatmap = makeEyeTrackHeatmap();
            }
            return null;
        }

        @Override
        public void done() {
            progressBarFrame.dispose();
            viewer.repaint();
        }


        private BufferedImage makeBoundsHeatmap() {
            long time = 0;
            ImagePlus imp;
            Rectangle rect;
            FloatProcessor ip = new FloatProcessor(viewer.getThumbnail().getWidth(), viewer.getThumbnail().getHeight());
            Rectangle[] boundsArray = trackerFeatures.getBoundsArray();
            for (int i = 0; i < boundsArray.length; i++) {
                progressBarFrame.setCurrentFrame(i);
                setProgress((int) ((double) (i * 100) / (double) totalTasks));
                if (boundsArray[i] != null) {
                    time = trackerFeatures.getTracker().getFrame(i).getTimestamp() - time;

                    rect = rescaleRectangle(boundsArray[i], scalex, scaley);

                    fillRectangle(rect, ip, time);
                }
            }
            imp = new ImagePlus("Bounds heatmap", ip);
            IJ.run(imp, "Fire", "");
            return imp.getBufferedImage();
        }

        private BufferedImage makeCursorHeatmap() {
            long time = 0;
            ImagePlus imp;
            int lastx = 0, lasty = 0;
            boolean firstpoint = true;

            FloatProcessor ip = new FloatProcessor(viewer.getThumbnail().getWidth(), viewer.getThumbnail().getHeight());
            Point2D[] cursorArray = trackerFeatures.getCursorArray();
            for (int i = 0; i < cursorArray.length; i++) {
                progressBarFrame.setCurrentFrame(i);
                setProgress((int) ((double) (i * 100) / (double)totalTasks));

                if (cursorArray[i] != null) {
                    if (i == 0) {
                        time = trackerFeatures.getTracker().getFrame(i).getTimestamp();
                    } else {
                        time = trackerFeatures.getTracker().getFrame(i).getTimestamp() - time;
                    }
                    int x = (int) (cursorArray[i].getX() * scalex);
                    int y = (int) (cursorArray[i].getY() * scaley);
                    if (!firstpoint) {
                        drawLine(x, y, lastx, lasty, ip, time);
                    }
                    lastx = x;
                    lasty = y;
                    firstpoint = false;
                }
            }
            ip.blurGaussian(10);
            imp = new ImagePlus("Cursor heatmap", ip);
            IJ.run(imp, "Fire", "");
            return imp.getBufferedImage();
        }

        private BufferedImage makeEyeTrackHeatmap() {
            long time = 0;
            ImagePlus imp;
            FloatProcessor ip = new FloatProcessor(viewer.getThumbnail().getWidth(), viewer.getThumbnail().getHeight());

            Point2D[] eyeArray = trackerFeatures.getEyeArray();
            for (int i = 0; i < eyeArray.length; i++) {
                progressBarFrame.setCurrentFrame(i);
                setProgress((int) ((double) (i * 100) / (double)totalTasks));
                if (eyeArray[i] != null) {
                    if (i == 0) {
                        time = trackerFeatures.getTracker().getFrame(i).getTimestamp();
                    } else {
                        time = trackerFeatures.getTracker().getFrame(i).getTimestamp() - time;
                    }

                    int x = (int) (eyeArray[i].getX()*scalex);
                    int y = (int) (eyeArray[i].getY()*scaley);
                    if(x > 0 && y > 0 && x < ip.getWidth() && y < ip.getHeight())
                        ip.setf(x, y, ip.getf(x, y) + 100);
                }
            }
            ip.blurGaussian(25);
            imp = new ImagePlus("Eye heatmap", ip);
            IJ.run(imp, "Fire", "");
            return imp.getBufferedImage();
        }

    }
}

