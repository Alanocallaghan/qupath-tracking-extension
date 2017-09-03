package qupath.extension.tracking.gui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import qupath.extension.tracking.gui.controllers.PaintStageController;
import qupath.extension.tracking.overlay.HeatmapOverlay;
import qupath.extension.tracking.overlay.TrackerFeatureOverlay;
import qupath.extension.tracking.tracker.TrackerFeatures;

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

    private TrackerPaintStage(TrackerFeatures trackerFeatures) {
        this.features = trackerFeatures;

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
        heatmapOverlay = new HeatmapOverlay(trackerFeatures);
        trackerOverlay = new TrackerFeatureOverlay(trackerFeatures);
        paintStageController.setHeatmapOverlay(heatmapOverlay);
        paintStageController.setTrackerOverlay(trackerOverlay);
        paintStageController.setTracker(trackerFeatures.getTracker());
    }

    public static TrackerPaintStage getInstance(TrackerFeatures features) {
        if (instance == null) {
            instance = new TrackerPaintStage(features);
        }
        return instance;
    }

    public void updateTracker(TrackerFeatures features) {
        instance.features = features;
    }


    
}
