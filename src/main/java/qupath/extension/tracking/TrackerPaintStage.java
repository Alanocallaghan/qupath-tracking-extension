package qupath.extension.tracking;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import qupath.lib.gui.viewer.recording.ViewTracker;

import java.io.IOException;

/**
 * Created by alan on 11/03/17.
 */
public class TrackerPaintStage extends Stage {

    private ViewTracker tracker;
    private Parent root;
    private static TrackerPaintStage instance = null;

    private TrackerPaintStage(ViewTracker tracker) {
        this.tracker = tracker;

        try {
            root = FXMLLoader.load(getClass().getResource("TrackerScene.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Scene scene = new Scene(root, 400, 200);
        this.setScene(scene);
    }

    public static TrackerPaintStage getInstance(ViewTracker tracker) {
        if (instance == null) {
            instance = new TrackerPaintStage(tracker);
        }
        return instance;
    }

    public void updateTracker(ViewTracker tracker) {
        instance.tracker = tracker;
    }
}
