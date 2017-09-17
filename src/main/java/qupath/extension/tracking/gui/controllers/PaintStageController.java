package qupath.extension.tracking.gui.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import qupath.extension.tracking.gui.TrackerPaintStage;
import qupath.extension.tracking.tracker.ExtendedViewTrackerPlayback;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.icons.PathIconFactory;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @author Alan O'Callaghan
 * Created by alan on 03/09/17.
 */
public class PaintStageController implements Initializable {

    public Action actionRecord, actionPlayback;
    public ExtendedViewTrackerPlayback playback;

    void resetOptions() {
        TrackerPaintStage.getHeatmapOverlay().setDoPaintBoundsHeatmap(bHCheck.isSelected());
        TrackerPaintStage.getHeatmapOverlay().setDoPaintCursorHeatmap(cHCheck.isSelected());
        TrackerPaintStage.getHeatmapOverlay().setDoPaintEyeHeatmap(eHCheck.isSelected());

        TrackerPaintStage.getTrackerOverlay().setDoPaintBoundsTrail(bTCheck.isSelected());
        TrackerPaintStage.getTrackerOverlay().setDoPaintCursorTrail(cTCheck.isSelected());
        TrackerPaintStage.getTrackerOverlay().setDoPaintEyeTrail(eTCheck.isSelected());

        TrackerPaintStage.getTrackerOverlay().setDoPaintBoundFixations(bfCheck.isSelected());
        TrackerPaintStage.getTrackerOverlay().setDoPaintSlowPans(spCheck.isSelected());
        TrackerPaintStage.getTrackerOverlay().setDoPaintZoomPeaks(zpCheck.isSelected());

        TrackerPaintStage.getTrackerOverlay().setEyeThicknessScalar(eyeThicknessSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setBoundsThicknessScalar(boundsThicknessSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorThicknessScalar(cursorThicknessSlider.getValue());


        TrackerPaintStage.getTrackerOverlay().setCursorFixationType((String)cursorFixationTypes.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("low", cursorLowPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("med", cursorMedPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("high", cursorHighPicker.getValue());

        TrackerPaintStage.getTrackerOverlay().setEyeFixationType((String)eyeFixationTypes.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("low", eyeLowPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("med", eyeMedPicker.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("high", eyeHighPicker.getValue());

        TrackerPaintStage.getTrackerOverlay().setEyeIVTSpeedThreshold(EyeIVTSpeedSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeIDTDispersionThreshold(EyeIDTDispersionSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setEyeIDTDurationThreshold(EyeIDTDurationSlider.getValue());

        TrackerPaintStage.getTrackerOverlay().setCursorIVTSpeedThreshold(CursorIVTSpeedSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorIDTDispersionThreshold(CursorIDTDispersionSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setCursorIDTDurationThreshold(CursorIDTDurationSlider.getValue());

        TrackerPaintStage.getTrackerOverlay().setSlowPanTimeThreshold(SlowPanTimeSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setSlowPanSpeedThreshold(SlowPanSpeedSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setBoundsFixationTimeThreshold(BoundsFixationSlider.getValue());
        TrackerPaintStage.getTrackerOverlay().setZoomPeakThreshold(ZoomPeakSlider.getValue());
    }

    private static final Node iconRecord, iconRecordStop,
            iconPlay, iconPlayStop;

    static {
        iconRecord = PathIconFactory.createNode(
                16,
                16,
                PathIconFactory.PathIcons.PLAYBACK_RECORD);
        iconRecordStop = PathIconFactory.createNode(
                16,
                16,
                PathIconFactory.PathIcons.PLAYBACK_RECORD_STOP);
        iconPlay = PathIconFactory.createNode(
                16,
                16,
                PathIconFactory.PathIcons.PLAYBACK_PLAY);
        iconPlayStop = PathIconFactory.createNode(
                16,
                16,
                PathIconFactory.PathIcons.PLAYBACK_PLAY_STOP);
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

        spCheck.setOnAction(event ->
                TrackerPaintStage.getTrackerOverlay().setDoPaintSlowPans(spCheck.isSelected()));
        bfCheck.setOnAction(event ->
                TrackerPaintStage.getTrackerOverlay().setDoPaintBoundFixations(bfCheck.isSelected()));
        zpCheck.setOnAction(event ->
                TrackerPaintStage.getTrackerOverlay().setDoPaintZoomPeaks(zpCheck.isSelected()));


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
                (observable, oldValue, newValue) -> {
                    if (!Objects.equals(newValue, oldValue)) {
                        TrackerPaintStage.getTrackerOverlay().setEyeIVTSpeedThreshold(newValue);
                    }
                });
        EyeIDTDispersionSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!Objects.equals(newValue, oldValue)) {
                        TrackerPaintStage.getTrackerOverlay().setEyeIDTDispersionThreshold(newValue);
                    }
                });
        EyeIDTDurationSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!Objects.equals(newValue, oldValue)) {
                        TrackerPaintStage.getTrackerOverlay().setEyeIDTDurationThreshold(newValue);
                    }
                });
        CursorIVTSpeedSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!Objects.equals(newValue, oldValue)) {
                        TrackerPaintStage.getTrackerOverlay().setCursorIVTSpeedThreshold(newValue);
                    }
                });
        CursorIDTDispersionSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!Objects.equals(newValue, oldValue)) {
                        TrackerPaintStage.getTrackerOverlay().setCursorIDTDispersionThreshold(newValue);
                    }
                });
        CursorIDTDurationSlider.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (!Objects.equals(newValue, oldValue)) {
                        TrackerPaintStage.getTrackerOverlay().setCursorIDTDurationThreshold(newValue);
                    }
                });
        SlowPanTimeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(newValue, oldValue)) {
                TrackerPaintStage.getTrackerOverlay().setSlowPanTimeThreshold(newValue);
            }
        });
        SlowPanSpeedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(newValue, oldValue)) {
                TrackerPaintStage.getTrackerOverlay().setSlowPanSpeedThreshold(newValue);
            }
        });
        BoundsFixationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(newValue, oldValue)) {
                TrackerPaintStage.getTrackerOverlay().setBoundsFixationTimeThreshold(newValue);
            }
        });
        ZoomPeakSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (!Objects.equals(newValue, oldValue)) {
                TrackerPaintStage.getTrackerOverlay().setZoomPeakThreshold(newValue);
            }
        });


//      Recording/Playback
        QuPathViewer viewer = QuPathGUI.getInstance().getViewer();
        if (TrackerPaintStage.getTracker() == null) {
            TrackerPaintStage.setTracker(new DefaultViewTracker(viewer));
        }


//      ViewTracker recording buttons and table
        playback = new ExtendedViewTrackerPlayback(viewer);
        actionRecord = QuPathGUI.createSelectableCommandAction(
                TrackerPaintStage.getTracker().recordingProperty(),
                "Record",
                iconRecord,
                (KeyCombination)null);

        actionPlayback = QuPathGUI.createSelectableCommandAction(
                playback.playingProperty(),
                "Play", iconPlay, (KeyCombination)null);
        actionPlayback.setDisabled(TrackerPaintStage.getTracker().isEmpty());
        actionRecord.disabledProperty().bind(actionPlayback.selectedProperty());

        TrackerPaintStage.getTracker().recordingProperty().addListener((v, o, n) -> {
            if (n) {
                actionRecord.setGraphic(iconRecordStop);
                actionRecord.setText("Stop recording");
                actionPlayback.setDisabled(true);
            } else {
                actionRecord.setGraphic(iconRecord);
                actionRecord.setText("Start recording");
                actionPlayback.setDisabled(TrackerPaintStage.getTracker().isEmpty());
            }
        });

        BooleanProperty playing = playback.playingProperty();
        actionPlayback.graphicProperty().bind(
                Bindings.createObjectBinding(() ->
                                playing.get() ? iconPlayStop : iconPlay,
                        playing));
        actionPlayback.textProperty().bind(
                Bindings.createStringBinding(() ->
                                playing.get() ? "Stop" : "Play",
                        playing));

        ToolBar toolbar = new ToolBar();
        toolbar.getItems().addAll(
                ActionUtils.createToggleButton(actionRecord,
                        ActionUtils.ActionTextBehavior.HIDE),
                ActionUtils.createToggleButton(actionPlayback,
                        ActionUtils.ActionTextBehavior.HIDE),
                new Separator(),
                ActionUtils.createButton(
                        QuPathGUI.createCommandAction(
                                new SaveTrackerAction(),
                                "Export")),
                ActionUtils.createButton(
                        QuPathGUI.createCommandAction(
                                new LoadTrackerAction(),
                                "Import")),
                ActionUtils.createButton(
                        QuPathGUI.createCommandAction(
                                new ResetTrackerAction(),
                                "Reset"))
        );

        TrackerBorderPane.setTop(toolbar);
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
    private ColorPicker cursorLowPicker, cursorMedPicker, cursorHighPicker,
            eyeHighPicker, eyeMedPicker, eyeLowPicker;
    @FXML
    private CheckBox bHCheck, eHCheck, cHCheck,
            bTCheck, cTCheck, eTCheck,
            zpCheck, bfCheck, spCheck;
    @FXML
    public BorderPane TrackerBorderPane;

    @FXML
    public GridPane FeatureOptionPane;
    @FXML
    public Label EyeIVTSpeedLabel, EyeIDTDurationLabel, EyeIDTDispersionLabel,
            CursorIVTSpeedLabel, CursorIDTDurationLabel, CursorIDTDispersionLabel;
    @FXML
    private Slider EyeIVTSpeedSlider, EyeIDTDurationSlider, EyeIDTDispersionSlider,
            CursorIVTSpeedSlider, CursorIDTDurationSlider, CursorIDTDispersionSlider;
    @FXML
    public Slider SlowPanTimeSlider, SlowPanSpeedSlider,
            BoundsFixationSlider, ZoomPeakSlider;
}
