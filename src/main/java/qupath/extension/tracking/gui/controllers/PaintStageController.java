package qupath.extension.tracking.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import qupath.extension.tracking.gui.TrackerPaintStage;
import qupath.extension.tracking.gui.ExtendedViewTrackerControlPanel;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Alan O'Callaghan
 * Created by alan on 03/09/17.
 */
public class PaintStageController implements Initializable {

    void resetOptions() {
        TrackerPaintStage.getHeatmapOverlay().setDoPaintBoundsHeatmap(bHCheck.isSelected());
        TrackerPaintStage.getHeatmapOverlay().setDoPaintCursorHeatmap(cHCheck.isSelected());
        TrackerPaintStage.getHeatmapOverlay().setDoPaintEyeHeatmap(eHCheck.isSelected());
        TrackerPaintStage.getTrackerOverlay().setDoPaintBoundsTrail(bTCheck.isSelected());
        TrackerPaintStage.getTrackerOverlay().setDoPaintCursorTrail(cTCheck.isSelected());
        TrackerPaintStage.getTrackerOverlay().setDoPaintEyeTrail(eTCheck.isSelected());
        TrackerPaintStage.getTrackerOverlay().setEyeThicknessScalar(eyeThicknessSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setBoundsThicknessScalar(boundsThicknessSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorThicknessScalar(cursorThicknessSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeFixationType((String)eyeFixationTypes.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorFixationType((String)cursorFixationTypes.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("low", cursorLowPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("med", cursorMedPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("high", cursorHighPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("low", eyeLowPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("med", eyeMedPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("high", eyeHighPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeIVTSpeedThreshold(EyeIVTSpeedSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeIDTDispersionThreshold(EyeIDTDispersionSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeIDTDurationThreshold(EyeIDTDurationSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorIVTSpeedThreshold(CursorIVTSpeedSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorIDTDispersionThreshold(CursorIDTDispersionSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorIDTDurationThreshold(CursorIDTDurationSlider.getValue());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        Menus
        SaveImage.setOnAction(new SaveSnapshotAction());

        SaveTracker.setOnAction(new SaveTrackerAction());
        LoadTracker.setOnAction(new LoadTrackerAction());
        SaveFeatures.setOnAction(event -> {});
        LoadFeatures.setOnAction(event -> {
        });
        Close.setOnAction(event -> TrackerPaintStage.getInstance().close());

        
//      Toggles for visualisations
        bHCheck.setOnAction(event ->
                TrackerPaintStage.getHeatmapOverlay().setDoPaintBoundsHeatmap(bHCheck.isSelected()));
        cHCheck.setOnAction(event ->
                TrackerPaintStage.getHeatmapOverlay().setDoPaintCursorHeatmap(cHCheck.isSelected()));
        eHCheck.setOnAction(event ->
                TrackerPaintStage.getHeatmapOverlay().setDoPaintEyeHeatmap(eHCheck.isSelected()));
        bTCheck.setOnAction(event ->
                TrackerPaintStage.getTrackerOverlay().setDoPaintBoundsTrail(bTCheck.isSelected()));
        cTCheck.setOnAction(event ->
                TrackerPaintStage.getTrackerOverlay().setDoPaintCursorTrail(cTCheck.isSelected()));
        eTCheck.setOnAction(event ->
                TrackerPaintStage.getTrackerOverlay().setDoPaintEyeTrail(eTCheck.isSelected()));

//        spCheck.setOnAction(event ->
//                this.features.trackerOverlay.setDoPaintSlowPans(spCheck.isSelected()));
//        bfCheck.setOnAction(event ->
//                this.features.trackerOverlay.setDoPaintBoundFixations(bfCheck.isSelected()));
//        zpCheck.setOnAction(event ->
//                this.features.trackerOverlay.setDoPaintZoomPeaks(zpCheck.isSelected()));



//      Visualisation options
        eyeThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> 
                        TrackerPaintStage.getTrackerOverlay().setEyeThicknessScalar(newValue));
        boundsThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> 
                        TrackerPaintStage.getTrackerOverlay().setBoundsThicknessScalar(newValue));
        cursorThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> 
                        TrackerPaintStage.getTrackerOverlay().setCursorThicknessScalar(newValue));

        eyeFixationTypes.valueProperty().addListener((observable, oldValue, newValue) ->
                TrackerPaintStage.getTrackerOverlay().setEyeFixationType((String)newValue));
        cursorFixationTypes.valueProperty().addListener((observable, oldValue, newValue) ->
                TrackerPaintStage.getTrackerOverlay().setCursorFixationType((String)newValue));

        cursorLowPicker.setOnAction(event -> TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("low", cursorLowPicker.getValue()));
        cursorLowPicker.setValue(Color.BLUE);

        cursorMedPicker.setOnAction(event -> TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("med", cursorMedPicker.getValue()));
        cursorMedPicker.setValue(Color.LIME);

        cursorHighPicker.setOnAction(event -> TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("high", cursorHighPicker.getValue()));
        cursorHighPicker.setValue(Color.RED);

        eyeLowPicker.setOnAction(event -> TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("low", eyeLowPicker.getValue()));
        eyeLowPicker.setValue(Color.BLUE);

        eyeMedPicker.setOnAction(event -> TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("med", eyeMedPicker.getValue()));
        eyeMedPicker.setValue(Color.LIME);

        eyeHighPicker.setOnAction(event -> TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("high", eyeHighPicker.getValue()));
        eyeHighPicker.setValue(Color.RED);


//      Feature options
        EyeIVTSpeedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getTrackerOverlay().setEyeIVTSpeedThreshold(newValue));
        EyeIDTDispersionSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getTrackerOverlay().setEyeIDTDispersionThreshold(newValue));
        EyeIDTDurationSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getTrackerOverlay().setEyeIDTDurationThreshold(newValue));
        CursorIVTSpeedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getTrackerOverlay().setCursorIVTSpeedThreshold(newValue));
        CursorIDTDispersionSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getTrackerOverlay().setCursorIDTDispersionThreshold(newValue));
        CursorIDTDurationSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getTrackerOverlay().setCursorIDTDurationThreshold(newValue));

//      Recording/Playback
        QuPathViewer viewer = QuPathGUI.getInstance().getViewer();
        if (TrackerPaintStage.getTracker() == null) {
            TrackerPaintStage.setTracker(new DefaultViewTracker(viewer));
        }

        ExtendedViewTrackerControlPanel panel = new ExtendedViewTrackerControlPanel(
                viewer, TrackerPaintStage.getTracker());
        TrackerBorderPane.setTop(panel.getNode());
    }


    @FXML
    public Menu FileMenu;
    @FXML
    public MenuBar Menubar;
    @FXML
    private MenuItem SaveFeatures;
    @FXML
    private MenuItem LoadFeatures;
    @FXML
    private MenuItem LoadTracker;
    @FXML
    private MenuItem SaveTracker;
    @FXML
    private MenuItem SaveImage;
    @FXML
    private MenuItem Close;
    @FXML
    public BorderPane BorderPane;
    @FXML
    public TabPane TabPane;
    @FXML
    public Tab RecordingTab,
            VisualisationTab,
            VisualisationOptionTab,
            FeatureOptionsTab;
    @FXML
    public GridPane VisualisatonTogglePane, VisualisationOptionPane;
    @FXML
    private Slider boundsThicknessSlider;
    @FXML
    private Slider cursorThicknessSlider;
    @FXML
    private Slider eyeThicknessSlider;
    @FXML
    public Label cursorLabel, eyeLabel,
            BoundThicknessLabel, CursorThicknessLabel, EyeThicknessLabel,
            cursorColorLabel, eyeColorLabel;
    @FXML
    private ComboBox cursorFixationTypes;
    @FXML
    private ComboBox eyeFixationTypes;
    @FXML
    private ColorPicker cursorLowPicker;
    @FXML
    private ColorPicker cursorMedPicker;
    @FXML
    private ColorPicker cursorHighPicker;
    @FXML
    private ColorPicker eyeHighPicker;
    @FXML
    private ColorPicker eyeMedPicker;
    @FXML
    private ColorPicker eyeLowPicker;
    @FXML
    private CheckBox bHCheck;
    @FXML
    private CheckBox eHCheck;
    @FXML
    private CheckBox cHCheck;
    @FXML
    private CheckBox bTCheck;
    @FXML
    private CheckBox cTCheck;
    @FXML
    private CheckBox eTCheck;
    @FXML
    private javafx.scene.layout.BorderPane TrackerBorderPane;

    @FXML
    public GridPane FeatureOptionPane;
    @FXML
    public Label EyeIVTSpeedLabel;
    @FXML
    private Slider EyeIVTSpeedSlider;
    @FXML
    public Label EyeIDTDurationLabel;
    @FXML
    private Slider EyeIDTDurationSlider;
    @FXML
    public Label EyeIDTDispersionLabel;
    @FXML
    private Slider EyeIDTDispersionSlider;
    @FXML
    public Label CursorIVTSpeedLabel;
    @FXML
    private Slider CursorIVTSpeedSlider;
    @FXML
    public Label CursorIDTDurationLabel;
    @FXML
    private Slider CursorIDTDurationSlider;
    @FXML
    public Label CursorIDTDispersionLabel;
    @FXML
    private Slider CursorIDTDispersionSlider;

    // zpCheck, bfCheck, spCheck;
}
