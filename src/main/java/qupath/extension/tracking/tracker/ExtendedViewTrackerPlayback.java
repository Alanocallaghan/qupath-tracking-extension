package qupath.extension.tracking.tracker;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;
import qupath.lib.objects.PathAnnotationObject;
import qupath.lib.objects.PathObject;
import qupath.lib.roi.PointsROI;

import java.awt.*;
import java.awt.geom.Point2D;

/**
 * Created by alan on 03/09/17.
 */
public class ExtendedViewTrackerPlayback {

    private static final Logger logger = LoggerFactory.getLogger(qupath.extension.tracking.tracker.ExtendedViewTrackerPlayback.class);
    private QuPathViewer viewer;
    private ViewTracker tracker;
    private BooleanProperty playing;
    private Timeline timeline;
    private long startTimestamp;

    public ExtendedViewTrackerPlayback(ViewTracker tracker, QuPathViewer viewer) {
        this.tracker = tracker;
        this.viewer = viewer;
        this.playing = new SimpleBooleanProperty(false);
        this.timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        actionEvent -> qupath.extension.tracking.tracker.ExtendedViewTrackerPlayback.this.handleUpdate(),
                        new KeyValue[0]), new KeyFrame(Duration.millis(50.0D)));
        this.timeline.setCycleCount(-1);
        this.playing.addListener((v, o, n) -> {
            if (n) {
                this.doStartPlayback();
            } else {
                this.doStopPlayback();
            }

        });
    }

    boolean doStartPlayback() {
        if(this.tracker.isEmpty()) {
            return false;
        } else {
            this.startTimestamp = System.currentTimeMillis();
            if(this.timeline.getStatus() == Animation.Status.RUNNING) {
                this.timeline.playFromStart();
            } else {
                this.timeline.play();
            }

            this.playing.set(true);
            return true;
        }
    }

    static void resizeViewer(QuPathViewer viewer, Dimension newSize) {
        if (!viewer.getSize().equals(newSize)) {
            int dw = newSize.width - viewer.getWidth();
            int dh = newSize.height - viewer.getHeight();
            javafx.stage.Window window = viewer.getView().getScene().getWindow();
            window.setWidth(window.getWidth() + (double)dw);
            window.setHeight(window.getHeight() + (double)dh);
        }
    }

    public boolean isPlaying() {
        return this.timeline.getStatus() == Animation.Status.RUNNING;
    }

    void doStopPlayback() {
        this.timeline.stop();
        this.playing.set(false);
    }

    void handleUpdate() {
        if (!this.tracker.isEmpty()) {
            long timestamp = System.currentTimeMillis();
            long timestampOfFirstFrame = this.tracker.nFrames() > 0?this.tracker.getFrame(0).getTimestamp():0L;
            ViewRecordingFrame frame = this.tracker.getFrameForTime(timestamp - this.startTimestamp + timestampOfFirstFrame);
            boolean requestStop;
            if(frame == null) {
                requestStop = true;
            } else {
                setViewerForFrame(this.viewer, frame);
                requestStop = this.tracker.isLastFrame(frame);
            }

            if(requestStop) {
                this.timeline.stop();
                this.playing.set(false);
            }

        }
    }

    public void setPlaying(boolean playing) {
        if(this.isPlaying() != playing) {
            this.playing.set(playing);
        }
    }

    public static void setViewerForFrame(QuPathViewer viewer, ViewRecordingFrame frame) {
        resizeViewer(viewer, frame.getSize());
        Rectangle imageBounds = frame.getImageBounds();
        Dimension canvasSize = frame.getSize();
        double downsampleX = (double)imageBounds.width / (double)canvasSize.width;
        double downsampleY = (double)imageBounds.height / (double)canvasSize.height;
        double downsample = 0.5D * (downsampleX + downsampleY);
        viewer.setDownsampleFactor(downsample);
        viewer.setCenterPixelLocation(
                (double)imageBounds.x + (double)imageBounds.width * 0.5D,
                (double)imageBounds.y + (double)imageBounds.height * 0.5D);
        Point2D p2d;
        if (frame.hasCursorPosition()) {
            p2d = viewer.imagePointToComponentPoint(
                    frame.getCursorPosition(),
                    null,
                    false);
            new Point((int)(p2d.getX() + 0.5D), (int)(p2d.getY() + 0.5D));
        }

        if (frame.hasEyePosition()) {
            p2d = frame.getEyePosition();
            PointsROI point = new PointsROI(p2d.getX(), p2d.getY());
            PathObject pathObject = new PathAnnotationObject(point);
            pathObject.setName("Eye tracking position");
            viewer.setSelectedObject(pathObject);
            logger.debug("Eye position: " + p2d);
        }

    }

    public BooleanProperty playingProperty() {
        return this.playing;
    }
}

