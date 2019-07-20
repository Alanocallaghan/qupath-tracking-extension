package qupath.extension.tracking.tracker;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.extension.tracking.TrackerUtils;
import qupath.extension.tracking.gui.stages.TrackerPaintStage;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPrefs;
import qupath.extension.tracking.overlay.PlaybackOverlay;
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

    private static final Logger logger = LoggerFactory.getLogger(ExtendedViewTrackerPlayback.class);
    private final QuPathViewer viewer;
    private final BooleanProperty playing;
    private final Timeline timeline;
    private long startTimestamp;
    private static PlaybackOverlay playbackOverlay = new PlaybackOverlay();

    public ExtendedViewTrackerPlayback(QuPathViewer viewer) {
        this.viewer = viewer;
        this.playing = new SimpleBooleanProperty(false);
        this.timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        actionEvent -> ExtendedViewTrackerPlayback.this.handleUpdate(),
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

    private void doStartPlayback() {
        if (TrackerPaintStage.getTracker().isEmpty()) {
        } else {
            this.startTimestamp = System.currentTimeMillis();
            if(this.timeline.getStatus() == Animation.Status.RUNNING) {
                this.timeline.playFromStart();
            } else {
                this.timeline.play();
            }

            this.playing.set(true);
            viewer.addOverlay(playbackOverlay);
        }
    }

    private static void resizeViewer(QuPathViewer viewer, Dimension newSize) {
        if (!viewer.getSize().equals(newSize)) {
            int dw = newSize.width - viewer.getWidth();
            int dh = newSize.height - viewer.getHeight();
            javafx.stage.Window window = viewer.getView().getScene().getWindow();
            window.setWidth(window.getWidth() + (double)dw);
            window.setHeight(window.getHeight() + (double)dh);
        }
    }

    private boolean isPlaying() {
        return this.timeline.getStatus() == Animation.Status.RUNNING;
    }

    private void doStopPlayback() {
        this.timeline.stop();
        this.playing.set(false);
        viewer.removeOverlay(playbackOverlay);
    }

    private void handleUpdate() {
        if (!TrackerPaintStage.getTracker().isEmpty()) {
            long timestamp = System.currentTimeMillis();
            long timestampOfFirstFrame = TrackerPaintStage.getTracker().nFrames() > 0 ?
                    TrackerPaintStage.getTracker().getFrame(0).getTimestamp(): 0L;
            ViewRecordingFrame frame = TrackerPaintStage.getTracker().getFrameForTime(
                    (long)(((double)timestamp - (double)this.startTimestamp) * TrackingPrefs.playbackSpeed.get()) +
                            timestampOfFirstFrame);
            boolean requestStop;
            if (frame == null) {
                requestStop = true;
            } else {
                setViewerForFrame(this.viewer, frame);
                requestStop = TrackerPaintStage.getTracker().isLastFrame(frame);
            }

            if (requestStop) {
                this.timeline.stop();
                this.playing.set(false);
            }

        }
    }

    public void setPlaying(boolean playing) {
        if (this.isPlaying() != playing) {
            this.playing.set(playing);
        }
    }

    private static void setViewerForFrame(QuPathViewer viewer, ViewRecordingFrame frame) {
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

        playbackOverlay.updateIconLocations(frame);

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



    public static TableView<ViewRecordingFrame> makeTable(QuPathViewer viewer, ViewTracker tracker) {

        TableView<ViewRecordingFrame> table = new TableView();

        for (int i = 0; i < nCols(tracker); ++i) {
            TableColumn<ViewRecordingFrame, Object> column = new TableColumn(getColumnName(i));
            final int j = i;
            column.setCellValueFactory(frame -> new SimpleObjectProperty(
                    getColumnValue((ViewRecordingFrame)frame.getValue(), j)));
            table.getColumns().add(column);
        }

        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        table.getSelectionModel().selectedItemProperty().addListener((v, o, frame) -> {
            if (frame != null) {
                setViewerForFrame(viewer, frame);
            }
        });
        ObservableList<ViewRecordingFrame> frameList = FXCollections.observableArrayList(
                TrackerUtils.getFramesAsArrayList(tracker));

        table.setItems(frameList);
        return table;
    }

    public BooleanProperty playingProperty() {
        return this.playing;
    }


    private static Object getColumnValue(ViewRecordingFrame frame, int col) {
        switch (col) {
            case 0:
                return frame.getTimestamp();
            case 1:
                return frame.getImageBounds().x;
            case 2:
                return frame.getImageBounds().y;
            case 3:
                return frame.getImageBounds().width;
            case 4:
                return frame.getImageBounds().height;
            case 5:
                return frame.getSize().width;
            case 6:
                return frame.getSize().height;
            case 7:
                return frame.getCursorPosition() == null ? "" : frame.getCursorPosition().getX();
            case 8:
                return frame.getCursorPosition() == null ? "" : frame.getCursorPosition().getY();
            case 9:
                return frame.getEyePosition() == null ? "" : frame.getEyePosition().getX();
            case 10:
                return frame.getEyePosition() == null ? "" : frame.getEyePosition().getY();
            case 11:
                return frame.isEyeFixated() == null ? "" : frame.isEyeFixated();
            default:
                return null;
        }
    }

    private static String getColumnName(int col) {
        switch(col) {
            case 0:
                return "Timestamp (ms)";
            case 1:
                return "X";
            case 2:
                return "Y";
            case 3:
                return "Width";
            case 4:
                return "Height";
            case 5:
                return "Canvas width";
            case 6:
                return "Canvas height";
            case 7:
                return "Cursor X";
            case 8:
                return "Cursor Y";
            case 9:
                return "Eye X";
            case 10:
                return "Eye Y";
            case 11:
                return "Fixated";
            default:
                return null;
        }
    }

    private static int nCols(ViewTracker tracker) {
        if (tracker == null) {
            return 0;
        } else {
            return tracker.hasEyeTrackingData() ? 12 : 9;
        }
    }



}

