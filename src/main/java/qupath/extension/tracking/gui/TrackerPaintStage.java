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

    TrackerFeatures features;
    private Parent root;
    private static TrackerPaintStage instance = null;

    public HeatmapOverlay getHeatmapOverlay() {
        return heatmapOverlay;
    }

    public TrackerFeatureOverlay getTrackerOverlay() {
        return trackerOverlay;
    }

    HeatmapOverlay heatmapOverlay;
    TrackerFeatureOverlay trackerOverlay;
    PaintStageController paintStageController;
    QuPathViewer viewer = QuPathGUI.getInstance().getViewer();

    private TrackerPaintStage() {

        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(
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
        return instance;
    }

    public static void exit() {
        removeOverlays();
        instance.close();
        instance = null;
    }

    public static void updateTracker(ViewTracker tracker) {
        TrackerFeatures features = new TrackerFeatures(tracker, instance.viewer.getServer());
        instance.features = features;
        removeOverlays();
        instance.heatmapOverlay = new HeatmapOverlay(features);
        instance.trackerOverlay = new TrackerFeatureOverlay(features);
        addOverlays();
        instance.paintStageController.setHeatmapOverlay(instance.heatmapOverlay);
        instance.paintStageController.setTrackerOverlay(instance.trackerOverlay);
        instance.paintStageController.setTracker(tracker);
    }

    private static void removeOverlays() {
        instance.viewer.removeOverlay(instance.heatmapOverlay);
        instance.viewer.removeOverlay(instance.trackerOverlay);
    }

    private static void addOverlays() {
        instance.viewer.addOverlay(instance.heatmapOverlay);
        instance.viewer.addOverlay(instance.trackerOverlay);
    }

    public PaintStageController getController() {
        return paintStageController;
    }
}
