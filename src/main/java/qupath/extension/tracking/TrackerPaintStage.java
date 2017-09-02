package qupath.extension.tracking;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Alan O'Callaghan
 * Created by Alan O'Callaghan on 11/03/17.
 */
class TrackerPaintStage extends Stage {

    TrackerFeatures features;
    private Parent root;
    private static TrackerPaintStage instance = null;

    private TrackerPaintStage(TrackerFeatures features) {
        this.features = features;

        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource(
                    "FXML/TrackerScene.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setTitle("Visualisation options");
        if (root != null) {
            Scene scene = new Scene(root, 500, 350);
            this.setScene(scene);
        }
    }

    static TrackerPaintStage getInstance(TrackerFeatures features) {
        if (instance == null) {
            instance = new TrackerPaintStage(features);
        }
        return instance;
    }

    public void updateTracker(TrackerFeatures features) {
        instance.features = features;
    }

}
