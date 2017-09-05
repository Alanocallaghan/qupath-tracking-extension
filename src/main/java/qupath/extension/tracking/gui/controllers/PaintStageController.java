package qupath.extension.tracking.gui.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import qupath.extension.tracking.gui.TrackerPaintStage;
import qupath.extension.tracking.overlay.HeatmapOverlay;
import qupath.extension.tracking.overlay.TrackerFeatureOverlay;
import qupath.extension.tracking.tracker.ExtendedViewTrackerControlPanel;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.helpers.dialogs.DialogHelperFX;
import qupath.lib.gui.viewer.recording.ViewTracker;

import java.io.File;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static qupath.extension.tracking.TrackerUtils.*;

/**
 * @author Alan O'Callaghan
 * Created by alan on 03/09/17.
 */
public class PaintStageController implements Initializable {

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

    private HeatmapOverlay heatmapOverlay = new HeatmapOverlay(null);
    private TrackerFeatureOverlay trackerOverlay = new TrackerFeatureOverlay(null);
    private ViewTracker tracker = null;

    public HeatmapOverlay getHeatmapOverlay() {
        return heatmapOverlay;
    }

    public void setHeatmapOverlay(HeatmapOverlay heatmapOverlay) {
        this.heatmapOverlay = heatmapOverlay;
    }

    public TrackerFeatureOverlay getTrackerOverlay() {
        return trackerOverlay;
    }

    public void setTrackerOverlay(TrackerFeatureOverlay trackerOverlay) {
        this.trackerOverlay = trackerOverlay;
    }

    void resetOptions() {
        this.heatmapOverlay.setDoPaintBoundsHeatmap(bHCheck.isSelected());
        this.heatmapOverlay.setDoPaintCursorHeatmap(cHCheck.isSelected());
        this.heatmapOverlay.setDoPaintEyeHeatmap(eHCheck.isSelected());
        this.trackerOverlay.setDoPaintBoundsTrail(bTCheck.isSelected());
        this.trackerOverlay.setDoPaintCursorTrail(cTCheck.isSelected());
        this.trackerOverlay.setDoPaintEyeTrail(eTCheck.isSelected());
        this.trackerOverlay.setEyeThicknessScalar(eyeThicknessSlider.getValue());
        this.trackerOverlay.setBoundsThicknessScalar(boundsThicknessSlider.getValue());
        this.trackerOverlay.setCursorThicknessScalar(cursorThicknessSlider.getValue());
        this.trackerOverlay.setEyeFixationType((String)eyeFixationTypes.getValue());
        this.trackerOverlay.setCursorFixationType((String)cursorFixationTypes.getValue());
        this.trackerOverlay.setCursorFixationColor("low", cursorLowPicker.getValue());
        this.trackerOverlay.setCursorFixationColor("med", cursorMedPicker.getValue());
        this.trackerOverlay.setCursorFixationColor("high", cursorHighPicker.getValue());
        this.trackerOverlay.setEyeFixationColor("low", eyeLowPicker.getValue());
        this.trackerOverlay.setEyeFixationColor("med", eyeMedPicker.getValue());
        this.trackerOverlay.setEyeFixationColor("high", eyeHighPicker.getValue());
        this.trackerOverlay.setEyeIVTSpeedThreshold(EyeIVTSpeedSlider.getValue());
        this.trackerOverlay.setEyeIDTDispersionThreshold(EyeIDTDispersionSlider.getValue());
        this.trackerOverlay.setEyeIDTDurationThreshold(EyeIDTDurationSlider.getValue());
        this.trackerOverlay.setCursorIVTSpeedThreshold(CursorIVTSpeedSlider.getValue());
        this.trackerOverlay.setCursorIDTDispersionThreshold(CursorIDTDispersionSlider.getValue());
        this.trackerOverlay.setCursorIDTDurationThreshold(CursorIDTDurationSlider.getValue());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        Menus
        SaveImage.setOnAction(event -> {
            QuPathGUI gui = QuPathGUI.getInstance();
            DialogHelperFX dfx = new DialogHelperFX(gui.getStage());

            String date = LocalDateTime.now().format(ISO_LOCAL_DATE_TIME);

            File file = dfx.promptToSaveFile("Save snapshot",
                    new File(System.getProperty("user.home")),
                    "QuPath_snapshot_" + date,
                    "png",
                    ".png"
            );
            if (file != null) {
                saveSnapshot(gui.getViewer(), file);
            }
        });

        SaveTracker.setOnAction(event -> {

        });

        LoadTracker.setOnAction(new LoadTrackerAction());

        SaveFeatures.setOnAction(event -> {

        });

        LoadFeatures.setOnAction(event -> {

        });
        Close.setOnAction(event -> TrackerPaintStage.exit());


//      Toggles for visualisations
        bHCheck.setOnAction(event ->
                this.heatmapOverlay.setDoPaintBoundsHeatmap(bHCheck.isSelected()));
        cHCheck.setOnAction(event ->
                this.heatmapOverlay.setDoPaintCursorHeatmap(cHCheck.isSelected()));
        eHCheck.setOnAction(event ->
                this.heatmapOverlay.setDoPaintEyeHeatmap(eHCheck.isSelected()));
        bTCheck.setOnAction(event ->
                this.trackerOverlay.setDoPaintBoundsTrail(bTCheck.isSelected()));
        cTCheck.setOnAction(event ->
                this.trackerOverlay.setDoPaintCursorTrail(cTCheck.isSelected()));
        eTCheck.setOnAction(event ->
                this.trackerOverlay.setDoPaintEyeTrail(eTCheck.isSelected()));

//        spCheck.setOnAction(event ->
//                this.features.trackerOverlay.setDoPaintSlowPans(spCheck.isSelected()));
//        bfCheck.setOnAction(event ->
//                this.features.trackerOverlay.setDoPaintBoundFixations(bfCheck.isSelected()));
//        zpCheck.setOnAction(event ->
//                this.features.trackerOverlay.setDoPaintZoomPeaks(zpCheck.isSelected()));



//      Visualisation options
        eyeThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> this.
                        trackerOverlay.setEyeThicknessScalar(newValue));
        boundsThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> this.
                        trackerOverlay.setBoundsThicknessScalar(newValue));
        cursorThicknessSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> this.
                        trackerOverlay.setCursorThicknessScalar(newValue));

        eyeFixationTypes.valueProperty().addListener((observable, oldValue, newValue) ->
                this.trackerOverlay.setEyeFixationType((String)newValue));
        cursorFixationTypes.valueProperty().addListener((observable, oldValue, newValue) ->
                this.trackerOverlay.setCursorFixationType((String)newValue));

        cursorLowPicker.setOnAction(event -> this.
                trackerOverlay.setCursorFixationColor("low", cursorLowPicker.getValue()));
        cursorLowPicker.setValue(Color.BLUE);

        cursorMedPicker.setOnAction(event -> this.
                trackerOverlay.setCursorFixationColor("med", cursorMedPicker.getValue()));
        cursorMedPicker.setValue(Color.LIME);

        cursorHighPicker.setOnAction(event -> this.
                trackerOverlay.setCursorFixationColor("high", cursorHighPicker.getValue()));
        cursorHighPicker.setValue(Color.RED);

        eyeLowPicker.setOnAction(event -> this.
                trackerOverlay.setEyeFixationColor("low", eyeLowPicker.getValue()));
        eyeLowPicker.setValue(Color.BLUE);

        eyeMedPicker.setOnAction(event -> this.
                trackerOverlay.setEyeFixationColor("med", eyeMedPicker.getValue()));
        eyeMedPicker.setValue(Color.LIME);

        eyeHighPicker.setOnAction(event -> this.
                trackerOverlay.setEyeFixationColor("high", eyeHighPicker.getValue()));
        eyeHighPicker.setValue(Color.RED);





//      Feature options
        EyeIVTSpeedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> this.
                        trackerOverlay.setEyeIVTSpeedThreshold(newValue));
        EyeIDTDispersionSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> this.
                        trackerOverlay.setEyeIDTDispersionThreshold(newValue));
        EyeIDTDurationSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> this.
                        trackerOverlay.setEyeIDTDurationThreshold(newValue));
        CursorIVTSpeedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> this.
                        trackerOverlay.setCursorIVTSpeedThreshold(newValue));
        CursorIDTDispersionSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> this.
                        trackerOverlay.setCursorIDTDispersionThreshold(newValue));
        CursorIDTDurationSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> this.
                        trackerOverlay.setCursorIDTDurationThreshold(newValue));


//      Recording/Playback

        ExtendedViewTrackerControlPanel panel = new ExtendedViewTrackerControlPanel(
                QuPathGUI.getInstance().getViewer(), tracker);
        RecordingTab.setContent(panel.getNode());
    }

    @FXML
    public Menu FileMenu;
    public MenuBar Menubar;
    public MenuItem SaveFeatures, LoadFeatures,
            LoadTracker, SaveTracker,
            SaveImage, Close;
    public BorderPane BorderPane;
    public TabPane TabPane;
    public Tab RecordingTab,
            VisualisationTab,
            VisualisationOptionTab,
            FeatureOptionsTab;
    public GridPane VisualisatonTogglePane, VisualisationOptionPane;
    public Slider boundsThicknessSlider,
        cursorThicknessSlider,
        eyeThicknessSlider;
    public Label cursorLabel, eyeLabel,
            BoundThicknessLabel, CursorThicknessLabel, EyeThicknessLabel,
            cursorColorLabel, eyeColorLabel;
    public ComboBox cursorFixationTypes, eyeFixationTypes;
    public ColorPicker cursorLowPicker, cursorMedPicker, cursorHighPicker,
        eyeHighPicker, eyeMedPicker, eyeLowPicker;
    public CheckBox bHCheck, eHCheck, cHCheck,
            bTCheck, cTCheck, eTCheck;

    public void setTracker(ViewTracker tracker) {
        this.tracker = tracker;
    }
    // zpCheck, bfCheck, spCheck;
}
