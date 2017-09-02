package qupath.extension.tracking;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Alan O'Callaghan
 * Created by alan on 15/03/17.
 */

public class TrackerPaintStageController implements Initializable {

    @FXML
    public GridPane pStageScene;
    @FXML
    private CheckBox bHCheck, eHCheck, cHCheck, bTCheck, cTCheck, eTCheck, zpCheck, bfCheck, spCheck;
    @FXML
    private ComboBox EyeFixationTypes, CursorFixationTypes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        HeatmapOverlay hOverlay = TrackerPaintStage.getInstance(null).features.hOverlay;
//        TrackerFeatureOverlay tOverlay = TrackerPaintStage.getInstance(null).features.tOverlay;

        bHCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.hOverlay.setDoPaintBoundsHeatmap(bHCheck.isSelected()));
        cHCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.hOverlay.setDoPaintCursorHeatmap(cHCheck.isSelected()));
        eHCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.hOverlay.setDoPaintEyeHeatmap(eHCheck.isSelected()));
        bTCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintBoundsTrail(bTCheck.isSelected()));
        cTCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintCursorTrail(cTCheck.isSelected()));
        eTCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintEyeTrail(eTCheck.isSelected()));
        spCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintSlowPans(spCheck.isSelected()));
        bfCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintBoundFixations(bfCheck.isSelected()));
        zpCheck.setOnAction(event -> TrackerPaintStage.getInstance(null).features.tOverlay.setDoPaintZoomPeaks(zpCheck.isSelected()));
        EyeFixationTypes.valueProperty().addListener((observable, oldValue, newValue) -> TrackerPaintStage.getInstance(null).features.tOverlay.setEyeFixationType((String)newValue));
        CursorFixationTypes.valueProperty().addListener((observable, oldValue, newValue) -> TrackerPaintStage.getInstance(null).features.tOverlay.setCursorFixationType((String)newValue));
    }
}
