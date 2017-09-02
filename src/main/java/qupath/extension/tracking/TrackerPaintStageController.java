package qupath.extension.tracking;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Alan O'Callaghan
 * Created by alan on 15/03/17.
 */

@SuppressWarnings("ALL")
public class TrackerPaintStageController implements Initializable {

    @FXML
    private GridPane pStageScene;
    @FXML
    private  Label eyeLabel, cursorLabel;
    @FXML
    private  ColorPicker cursorLowPicker, cursorMedPicker, cursorHighPicker;
    @FXML
    private  ColorPicker eyeLowPicker, eyeMedPicker, eyeHighPicker;
    @FXML
    private  Label cursorColorLabel, eyeColorLabel;
    @FXML
    private  Slider boundsThicknessSlider,
            eyeThicknessSlider,
            cursorThicknessSlider;
    @FXML
    private CheckBox bHCheck, eHCheck, cHCheck,
            bTCheck, cTCheck, eTCheck;
    // zpCheck, bfCheck, spCheck;
    @FXML
    private ComboBox eyeFixationTypes, cursorFixationTypes;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        HeatmapOverlay hOverlay = getTrackerPaintStage().features.hOverlay;
//        TrackerFeatureOverlay tOverlay = getTrackerPaintStage().features.tOverlay;

        bHCheck.setOnAction(event ->
                getTrackerPaintStage().features.hOverlay.setDoPaintBoundsHeatmap(bHCheck.isSelected()));
        cHCheck.setOnAction(event ->
                getTrackerPaintStage().features.hOverlay.setDoPaintCursorHeatmap(cHCheck.isSelected()));
        eHCheck.setOnAction(event ->
                getTrackerPaintStage().features.hOverlay.setDoPaintEyeHeatmap(eHCheck.isSelected()));
        bTCheck.setOnAction(event ->
                getTrackerPaintStage().features.tOverlay.setDoPaintBoundsTrail(bTCheck.isSelected()));
        cTCheck.setOnAction(event ->
                getTrackerPaintStage().features.tOverlay.setDoPaintCursorTrail(cTCheck.isSelected()));
        eTCheck.setOnAction(event ->
                getTrackerPaintStage().features.tOverlay.setDoPaintEyeTrail(eTCheck.isSelected()));

        eyeThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> getTrackerPaintStage().
                        features.tOverlay.setEyeThicknessScalar(newValue));
        boundsThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> getTrackerPaintStage().
                        features.tOverlay.setBoundsThicknessScalar(newValue));
        cursorThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> getTrackerPaintStage().
                        features.tOverlay.setCursorThicknessScalar(newValue));

//        spCheck.setOnAction(event ->
//                getTrackerPaintStage().features.tOverlay.setDoPaintSlowPans(spCheck.isSelected()));
//        bfCheck.setOnAction(event ->
//                getTrackerPaintStage().features.tOverlay.setDoPaintBoundFixations(bfCheck.isSelected()));
//        zpCheck.setOnAction(event ->
//                getTrackerPaintStage().features.tOverlay.setDoPaintZoomPeaks(zpCheck.isSelected()));

        eyeFixationTypes.valueProperty().addListener((observable, oldValue, newValue) ->
                getTrackerPaintStage().features.tOverlay.setEyeFixationType((String)newValue));
        cursorFixationTypes.valueProperty().addListener((observable, oldValue, newValue) ->
                getTrackerPaintStage().features.tOverlay.setCursorFixationType((String)newValue));


        cursorLowPicker.setOnAction(event -> getTrackerPaintStage().
                        features.tOverlay.setCursorFixationColor("low", cursorLowPicker.getValue()));
        cursorLowPicker.setValue(Color.BLUE);

        cursorMedPicker.setOnAction(event -> getTrackerPaintStage().
                features.tOverlay.setCursorFixationColor("med", cursorMedPicker.getValue()));
        cursorMedPicker.setValue(Color.LIME);

        cursorHighPicker.setOnAction(event -> getTrackerPaintStage().
                features.tOverlay.setCursorFixationColor("high", cursorHighPicker.getValue()));
        cursorHighPicker.setValue(Color.RED);

        eyeLowPicker.setOnAction(event -> getTrackerPaintStage().
                features.tOverlay.setEyeFixationColor("low", eyeLowPicker.getValue()));
        eyeLowPicker.setValue(Color.BLUE);

        eyeMedPicker.setOnAction(event -> getTrackerPaintStage().
                features.tOverlay.setEyeFixationColor("med", eyeMedPicker.getValue()));
        eyeMedPicker.setValue(Color.LIME);

        eyeHighPicker.setOnAction(event -> getTrackerPaintStage().
                features.tOverlay.setEyeFixationColor("high", eyeHighPicker.getValue()));
        eyeHighPicker.setValue(Color.RED);
    }
    private static TrackerPaintStage getTrackerPaintStage() {
        return TrackerPaintStage.getInstance(null);
    }
}
