package qupath.extension.tracking;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by Alan O'Callaghan on 11/03/17.
 */
public class TrackerPaintStage extends Stage {

    TrackerFeatures features;
    private Parent root;
    private static TrackerPaintStage instance = null;

    private TrackerPaintStage(TrackerFeatures features) {
        this.features = features;

        try {
            root = FXMLLoader.load(getClass().getClassLoader().getResource("FXML/TrackerScene.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setTitle("Visualisation options");

        Scene scene = new Scene(root, 400, 200);
        this.setScene(scene);
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
