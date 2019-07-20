package qupath.extension.tracking.gui.stages;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import qupath.extension.tracking.gui.controllers.BatchAnalysisController;


public class BatchAnalysisStage extends Stage {
    private BatchAnalysisController batchAnalysisController;

    private Parent root;
    private static BatchAnalysisStage instance;

    public static BatchAnalysisStage getInstance() {
        if (instance == null) {
            instance = new BatchAnalysisStage();
        }
        instance.setOnCloseRequest(event -> instance.close());
        return instance;
    }

    public BatchAnalysisStage() {
        FXMLLoader loader = new FXMLLoader(
                getClass().getClassLoader().getResource(
                        "FXML/BatchAnalysisScene.fxml"));
        try {
            root = loader.load();
            batchAnalysisController = loader.getController();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setTitle("Visualisation options");
        if (root != null) {
            Scene scene = new Scene(root, 1000, 500);
            this.setScene(scene);
        }
    }

}
