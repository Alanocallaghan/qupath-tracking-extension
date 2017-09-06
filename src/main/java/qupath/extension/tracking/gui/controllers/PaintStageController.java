package qupath.extension.tracking.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.controlsfx.control.action.ActionUtils;
import qupath.extension.tracking.gui.TrackerPaintStage;
import qupath.extension.tracking.overlay.HeatmapOverlay;
import qupath.extension.tracking.overlay.TrackerFeatureOverlay;
import qupath.extension.tracking.gui.ExtendedViewTrackerControlPanel;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;
import qupath.lib.gui.viewer.recording.ViewTracker;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Alan O'Callaghan
 * Created by alan on 03/09/17.
 */
public class PaintStageController implements Initializable {

    private ExtendedViewTrackerControlPanel panel;

    void resetOptions() {
        TrackerPaintStage.getInstance().getHeatmapOverlay().setDoPaintBoundsHeatmap(bHCheck.isSelected());
        TrackerPaintStage.getInstance().getHeatmapOverlay().setDoPaintCursorHeatmap(cHCheck.isSelected());
        TrackerPaintStage.getInstance().getHeatmapOverlay().setDoPaintEyeHeatmap(eHCheck.isSelected());
        TrackerPaintStage.getInstance().getTrackerOverlay().setDoPaintBoundsTrail(bTCheck.isSelected());
        TrackerPaintStage.getInstance().getTrackerOverlay().setDoPaintCursorTrail(cTCheck.isSelected());
        TrackerPaintStage.getInstance().getTrackerOverlay().setDoPaintEyeTrail(eTCheck.isSelected());
        TrackerPaintStage.getInstance().getTrackerOverlay().setEyeThicknessScalar(eyeThicknessSlider.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setBoundsThicknessScalar(boundsThicknessSlider.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setCursorThicknessScalar(cursorThicknessSlider.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setEyeFixationType((String)eyeFixationTypes.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setCursorFixationType((String)cursorFixationTypes.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setCursorFixationColor("low", cursorLowPicker.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setCursorFixationColor("med", cursorMedPicker.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setCursorFixationColor("high", cursorHighPicker.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setEyeFixationColor("low", eyeLowPicker.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setEyeFixationColor("med", eyeMedPicker.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setEyeFixationColor("high", eyeHighPicker.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setEyeIVTSpeedThreshold(EyeIVTSpeedSlider.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setEyeIDTDispersionThreshold(EyeIDTDispersionSlider.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setEyeIDTDurationThreshold(EyeIDTDurationSlider.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setCursorIVTSpeedThreshold(CursorIVTSpeedSlider.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setCursorIDTDispersionThreshold(CursorIDTDispersionSlider.getValue());
        TrackerPaintStage.getInstance().getTrackerOverlay().setCursorIDTDurationThreshold(CursorIDTDurationSlider.getValue());
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
                TrackerPaintStage.getInstance().getHeatmapOverlay().setDoPaintBoundsHeatmap(bHCheck.isSelected()));
        cHCheck.setOnAction(event ->
                TrackerPaintStage.getInstance().getHeatmapOverlay().setDoPaintCursorHeatmap(cHCheck.isSelected()));
        eHCheck.setOnAction(event ->
                TrackerPaintStage.getInstance().getHeatmapOverlay().setDoPaintEyeHeatmap(eHCheck.isSelected()));
        bTCheck.setOnAction(event ->
                TrackerPaintStage.getInstance().getTrackerOverlay().setDoPaintBoundsTrail(bTCheck.isSelected()));
        cTCheck.setOnAction(event ->
                TrackerPaintStage.getInstance().getTrackerOverlay().setDoPaintCursorTrail(cTCheck.isSelected()));
        eTCheck.setOnAction(event ->
                TrackerPaintStage.getInstance().getTrackerOverlay().setDoPaintEyeTrail(eTCheck.isSelected()));

//        spCheck.setOnAction(event ->
//                this.features.trackerOverlay.setDoPaintSlowPans(spCheck.isSelected()));
//        bfCheck.setOnAction(event ->
//                this.features.trackerOverlay.setDoPaintBoundFixations(bfCheck.isSelected()));
//        zpCheck.setOnAction(event ->
//                this.features.trackerOverlay.setDoPaintZoomPeaks(zpCheck.isSelected()));



//      Visualisation options
        eyeThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> 
                        TrackerPaintStage.getInstance().getTrackerOverlay().setEyeThicknessScalar(newValue));
        boundsThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> 
                        TrackerPaintStage.getInstance().getTrackerOverlay().setBoundsThicknessScalar(newValue));
        cursorThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> 
                        TrackerPaintStage.getInstance().getTrackerOverlay().setCursorThicknessScalar(newValue));

        eyeFixationTypes.valueProperty().addListener((observable, oldValue, newValue) ->
                TrackerPaintStage.getInstance().getTrackerOverlay().setEyeFixationType((String)newValue));
        cursorFixationTypes.valueProperty().addListener((observable, oldValue, newValue) ->
                TrackerPaintStage.getInstance().getTrackerOverlay().setCursorFixationType((String)newValue));

        cursorLowPicker.setOnAction(event -> TrackerPaintStage.getInstance().getTrackerOverlay().setCursorFixationColor("low", cursorLowPicker.getValue()));
        cursorLowPicker.setValue(Color.BLUE);

        cursorMedPicker.setOnAction(event -> TrackerPaintStage.getInstance().getTrackerOverlay().setCursorFixationColor("med", cursorMedPicker.getValue()));
        cursorMedPicker.setValue(Color.LIME);

        cursorHighPicker.setOnAction(event -> TrackerPaintStage.getInstance().getTrackerOverlay().setCursorFixationColor("high", cursorHighPicker.getValue()));
        cursorHighPicker.setValue(Color.RED);

        eyeLowPicker.setOnAction(event -> TrackerPaintStage.getInstance().getTrackerOverlay().setEyeFixationColor("low", eyeLowPicker.getValue()));
        eyeLowPicker.setValue(Color.BLUE);

        eyeMedPicker.setOnAction(event -> TrackerPaintStage.getInstance().getTrackerOverlay().setEyeFixationColor("med", eyeMedPicker.getValue()));
        eyeMedPicker.setValue(Color.LIME);

        eyeHighPicker.setOnAction(event -> TrackerPaintStage.getInstance().getTrackerOverlay().setEyeFixationColor("high", eyeHighPicker.getValue()));
        eyeHighPicker.setValue(Color.RED);


//      Feature options
        EyeIVTSpeedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getInstance().getTrackerOverlay().setEyeIVTSpeedThreshold(newValue));
        EyeIDTDispersionSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getInstance().getTrackerOverlay().setEyeIDTDispersionThreshold(newValue));
        EyeIDTDurationSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getInstance().getTrackerOverlay().setEyeIDTDurationThreshold(newValue));
        CursorIVTSpeedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getInstance().getTrackerOverlay().setCursorIVTSpeedThreshold(newValue));
        CursorIDTDispersionSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getInstance().getTrackerOverlay().setCursorIDTDispersionThreshold(newValue));
        CursorIDTDurationSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> TrackerPaintStage.getInstance().getTrackerOverlay().setCursorIDTDurationThreshold(newValue));

//      Recording/Playback
        QuPathViewer viewer = QuPathGUI.getInstance().getViewer();
        if (TrackerPaintStage.getTracker() == null) {
            TrackerPaintStage.setTracker(new DefaultViewTracker(viewer));
        }

        panel = new ExtendedViewTrackerControlPanel(
                viewer, TrackerPaintStage.getTracker());
        TrackerBorderPane.setTop(panel.getNode());
    }


    @FXML
    public Menu FileMenu;
    @FXML
    public MenuBar Menubar;
    @FXML
    public MenuItem SaveFeatures, LoadFeatures,
            LoadTracker, SaveTracker,
            SaveImage, Close;
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
    public Slider boundsThicknessSlider,
        cursorThicknessSlider,
        eyeThicknessSlider;
    @FXML
    public Label cursorLabel, eyeLabel,
            BoundThicknessLabel, CursorThicknessLabel, EyeThicknessLabel,
            cursorColorLabel, eyeColorLabel;
    @FXML
    public ComboBox cursorFixationTypes, eyeFixationTypes;
    @FXML
    public ColorPicker cursorLowPicker, cursorMedPicker, cursorHighPicker,
        eyeHighPicker, eyeMedPicker, eyeLowPicker;
    @FXML
    public CheckBox bHCheck, eHCheck, cHCheck,
            bTCheck, cTCheck, eTCheck;
    @FXML
    public javafx.scene.layout.BorderPane TrackerBorderPane;

    @FXML
    public GridPane FeatureOptionPane;
    @FXML
    public Label EyeIVTSpeedLabel;
    @FXML
    public Slider EyeIVTSpeedSlider;
    @FXML
    public Label EyeIDTDurationLabel;
    @FXML
    public Slider EyeIDTDurationSlider;
    @FXML
    public Label EyeIDTDispersionLabel;
    @FXML
    public Slider EyeIDTDispersionSlider;
    @FXML
    public Label CursorIVTSpeedLabel;
    @FXML
    public Slider CursorIVTSpeedSlider;
    @FXML
    public Label CursorIDTDurationLabel;
    @FXML
    public Slider CursorIDTDurationSlider;
    @FXML
    public Label CursorIDTDispersionLabel;
    @FXML
    public Slider CursorIDTDispersionSlider;

    // zpCheck, bfCheck, spCheck;
}
