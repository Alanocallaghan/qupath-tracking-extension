package qupath.extension.tracking;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by alan on 15/03/17.
 */

public class TrackerPaintStageController implements Initializable {

    @FXML
    public GridPane pStageScene;
    @FXML
    private CheckBox bHCheck, eHCheck, cHCheck, bTCheck, cTCheck, eTCheck, zpCheck, bfCheck, spCheck;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        bHCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.hOverlay.setDoPaintBoundsHeatmap(bHCheck.isSelected()));
        cHCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.hOverlay.setDoPaintCursorHeatmap(cHCheck.isSelected()));
        eHCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.hOverlay.setDoPaintEyeHeatmap(eHCheck.isSelected()));
        bTCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintBoundsTrail(bTCheck.isSelected()));
        cTCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintCursorTrail(cTCheck.isSelected()));
        eTCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintEyeTrail(eTCheck.isSelected()));
        spCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintSlowPans(spCheck.isSelected()));
        bfCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintBoundFixations(bfCheck.isSelected()));
        zpCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintZoomPeaks(zpCheck.isSelected()));
    }
}
