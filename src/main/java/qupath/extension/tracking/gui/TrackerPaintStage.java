package qupath.extension.tracking.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import qupath.extension.tracking.gui.controllers.PaintStageController;
import qupath.extension.tracking.overlay.HeatmapOverlay;
import qupath.extension.tracking.overlay.TrackerFeatureOverlay;
import qupath.extension.tracking.tracker.TrackerFeatures;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.ViewTracker;

/**
 * @author Alan O'Callaghan
 * Created by Alan O'Callaghan on 11/03/17.
 */
public class TrackerPaintStage extends Stage {

    private static TrackerFeatures features;
    private Parent root;
    private static TrackerPaintStage instance = null;
    private static ViewTracker tracker;

    private static HeatmapOverlay heatmapOverlay = null;
    private static TrackerFeatureOverlay trackerOverlay = null;
    private PaintStageController paintStageController;
    private static final QuPathViewer viewer = QuPathGUI.getInstance().getViewer();

    private TrackerPaintStage() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource(
                        "FXML/TrackerPaintScene.fxml"));
        try {
            root = loader.load();
            paintStageController = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setTitle("Visualisation options");
        if (root != null) {
            Scene scene = new Scene(root, 500, 350);
            this.setScene(scene);
        }
    }

    public static TrackerPaintStage getInstance() {
        if (instance == null) {
            instance = new TrackerPaintStage();
        }
        instance.setOnCloseRequest(event -> instance.close());
        return instance;
    }

    @Override
    public void close() {
        removeOverlays();
        heatmapOverlay = null;
        trackerOverlay = null;
        features = null;
        tracker = null;
        instance = null;
    }

    public static void setTracker(ViewTracker tracker) {
        TrackerPaintStage.tracker = tracker;
        TrackerFeatures features = new TrackerFeatures(tracker, viewer.getServer());
        TrackerPaintStage.features = features;
        removeOverlays();
        TrackerPaintStage.heatmapOverlay = new HeatmapOverlay(features);
        TrackerPaintStage.trackerOverlay = new TrackerFeatureOverlay(features);
        addOverlays();
    }

    private static void removeOverlays() {
        viewer.removeOverlay(heatmapOverlay);
        viewer.removeOverlay(trackerOverlay);
    }

    private static void addOverlays() {
        viewer.addOverlay(heatmapOverlay);
        viewer.addOverlay(trackerOverlay);
    }

    public PaintStageController getController() {
        return paintStageController;
    }

    public static ViewTracker getTracker() {
        return tracker;
    }

    public static HeatmapOverlay getHeatmapOverlay() {
        return heatmapOverlay;
    }

    public static TrackerFeatureOverlay getTrackerOverlay() {
        return trackerOverlay;
    }

}
