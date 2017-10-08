package qupath.extension.tracking.gui.controllers;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import qupath.extension.tracking.gui.*;
import qupath.extension.tracking.gui.controllers.actions.*;
import qupath.extension.tracking.gui.controllers.prefs.BoundsPrefs;
import qupath.extension.tracking.gui.controllers.prefs.PointPrefs;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPref;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPrefs;
import qupath.extension.tracking.tracker.ExtendedViewTrackerPlayback;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.icons.PathIconFactory;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Alan O'Callaghan
 * Created by alan on 03/09/17.
 */
public class PaintStageController implements Initializable {

    private Action actionRecord;
    public Action actionPlayback;
    private ExtendedViewTrackerPlayback playback;

    private TrackingPrefs trackingPrefs;
    private PointPrefControls eyePointPrefControls;
    private BoundsPrefControls boundsPrefControls;
    private PointPrefControls cursorPointPrefControls;


    public void resetOptions() {
//        TrackerPaintStage.getHeatmapOverlay().setDoPaintBoundsHeatmap(bHCheck.isSelected());
//        TrackerPaintStage.getHeatmapOverlay().setDoPaintCursorHeatmap(cHCheck.isSelected());
//        TrackerPaintStage.getHeatmapOverlay().setDoPaintEyeHeatmap(eHCheck.isSelected());
//
//        TrackerPaintStage.getTrackerOverlay().setDoPaintBoundsTrail(bTCheck.isSelected());
//        TrackerPaintStage.getTrackerOverlay().setDoPaintCursorTrail(cTCheck.isSelected());
//        TrackerPaintStage.getTrackerOverlay().setDoPaintEyeTrail(eTCheck.isSelected());
//
//        TrackerPaintStage.getTrackerOverlay().setDoPaintBoundFixations(bfCheck.isSelected());
//        TrackerPaintStage.getTrackerOverlay().setDoPaintSlowPans(spCheck.isSelected());
//        TrackerPaintStage.getTrackerOverlay().setDoPaintZoomPeaks(zpCheck.isSelected());
//
//        TrackerPaintStage.getTrackerOverlay().setEyeThicknessScalar(eyeThicknessSlider.getValue());
//        TrackerPaintStage.getTrackerOverlay().setBoundsThicknessScalar(boundsThicknessSlider.getValue());
//        TrackerPaintStage.getTrackerOverlay().setCursorThicknessScalar(cursorThicknessSlider.getValue());
//
//
//        TrackerPaintStage.getTrackerOverlay().setCursorFixationType((String)cursorFixationTypes.getValue());
//        TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("low", cursorLowPicker.getValue());
//        TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("med", cursorMedPicker.getValue());
//        TrackerPaintStage.getTrackerOverlay().setCursorFixationColor("high", cursorHighPicker.getValue());
//
//        TrackerPaintStage.getTrackerOverlay().setEyeFixationType((String)eyeFixationTypes.getValue());
//        TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("low", eyeLowPicker.getValue());
//        TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("med", eyeMedPicker.getValue());
//        TrackerPaintStage.getTrackerOverlay().setEyeFixationColor("high", eyeHighPicker.getValue());
//
//        TrackerPaintStage.getTrackerOverlay().setEyeIVTSpeedThreshold(eyeIVTSpeedSlider.getValue());
//        TrackerPaintStage.getTrackerOverlay().setEyeIDTDispersionThreshold(eyeIDTDispersionSlider.getValue());
//        TrackerPaintStage.getTrackerOverlay().setEyeIDTDurationThreshold(eyeIDTDurationSlider.getValue());
//
//        TrackerPaintStage.getTrackerOverlay().setCursorIVTSpeedThreshold(cursorIVTSpeedSlider.getValue());
//        TrackerPaintStage.getTrackerOverlay().setCursorIDTDispersionThreshold(cursorIDTDispersionSlider.getValue());
//        TrackerPaintStage.getTrackerOverlay().setCursorIDTDurationThreshold(cursorIDTDurationSlider.getValue());
//
//        TrackerPaintStage.getTrackerOverlay().setSlowPanTimeThreshold(slowPanTimeSlider.getValue());
//        TrackerPaintStage.getTrackerOverlay().setSlowPanSpeedThreshold(slowPanSpeedSlider.getValue());
//        TrackerPaintStage.getTrackerOverlay().setBoundsFixationTimeThreshold(boundsFixationSlider.getValue());
//        TrackerPaintStage.getTrackerOverlay().setZoomPeakThreshold(zoomPeakSlider.getValue());
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
        SaveFeatures.setOnAction(new SaveFeaturesAction());
        LoadFeatures.setOnAction(new LoadFeaturesAction());
        Close.setOnAction(event -> TrackerPaintStage.getInstance().close());

        
//      Toggles for visualisations
        boundsPrefControls = new BoundsPrefControls(boundsThicknessSlider,
                bTCheck,
                bHCheck,
                zpCheck,
                bfCheck,
                spCheck,
                slowPanTimeSlider,
                slowPanSpeedSlider,
                boundsFixationSlider,
                zoomPeakSlider);

        eyePointPrefControls = new PointPrefControls("eye",
                eyeThicknessSlider,
                eHCheck,
                eyeFixationTypes,
                eTCheck,
                eyeIVTSpeedSlider,
                eyeIDTDispersionSlider,
                eyeIDTDurationSlider,
                eyeLowPicker,
                eyeMedPicker,
                eyeHighPicker);

        cursorPointPrefControls = new PointPrefControls("cursor",
                cursorThicknessSlider,
                cHCheck,
                cursorFixationTypes,
                cTCheck,
                cursorIVTSpeedSlider,
                cursorIDTDispersionSlider,
                cursorIDTDurationSlider,
                cursorLowPicker,
                cursorMedPicker,
                cursorHighPicker);

        TrackingPrefs.medZoomThreshold.bindBidirectional(medZoomSlider.valueProperty());
        TrackingPrefs.lowZoomThreshold.bindBidirectional(lowZoomSlider.valueProperty());


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
        actionRecord.disabledProperty().bindBidirectional(actionPlayback.selectedProperty());

        TrackerPaintStage.getTracker().recordingProperty().addListener((v, o, n) -> {
            if (n) {
                actionRecord.setGraphic(iconRecordStop);
                actionRecord.setText("Stop recording");
                actionPlayback.setDisabled(true);
            } else {
                actionRecord.setGraphic(iconRecord);
                actionRecord.setText("Start recording");
                actionPlayback.setDisabled(TrackerPaintStage.getTracker().isEmpty());
                if (!TrackerPaintStage.getTracker().isEmpty()) {
                    this.TrackerBorderPane.setCenter(ExtendedViewTrackerPlayback.makeTable(
                            viewer, TrackerPaintStage.getTracker()));
                }
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
    public BorderPane TrackerBorderPane;

    @FXML
    public GridPane FeatureOptionPane;

    @FXML
    public GridPane VisualisatonTogglePane, VisualisationOptionPane;

    @FXML
    public Slider medZoomSlider,
            lowZoomSlider;

    @FXML
    private Slider boundsThicknessSlider;
    @FXML
    private Slider cursorThicknessSlider;
    @FXML
    private Slider eyeThicknessSlider;
//    @FXML
//    public Label cursorLabel, eyeLabel,
//            BoundThicknessLabel, CursorThicknessLabel, EyeThicknessLabel,
//            cursorColorLabel, eyeColorLabel;
    @FXML
    private ComboBox cursorFixationTypes;
    @FXML
    private ComboBox eyeFixationTypes;

//    @FXML
//    public Label EyeIVTSpeedLabel, EyeIDTDurationLabel, EyeIDTDispersionLabel,
//            CursorIVTSpeedLabel, CursorIDTDurationLabel, CursorIDTDispersionLabel;
    @FXML
    private Slider eyeIVTSpeedSlider, eyeIDTDurationSlider, eyeIDTDispersionSlider,
        cursorIVTSpeedSlider, cursorIDTDurationSlider, cursorIDTDispersionSlider;
    @FXML
    private Slider slowPanTimeSlider;
    @FXML
    private Slider slowPanSpeedSlider;
    @FXML
    private Slider boundsFixationSlider;
    @FXML
    private Slider zoomPeakSlider;

    @FXML
    private ColorPicker cursorLowPicker, cursorMedPicker, cursorHighPicker,
            eyeHighPicker, eyeMedPicker, eyeLowPicker;
    @FXML
    private CheckBox bHCheck, eHCheck, cHCheck,
            bTCheck, cTCheck, eTCheck,
            zpCheck, bfCheck, spCheck;

}

class PrefControls {
    Slider thicknessSlider;
    CheckBox heatmapCheck, trailCheck;
    PrefControls(TrackingPref prefs,
                 CheckBox heatmapCheck,
                 CheckBox trailCheck,
                 Slider thicknessSlider) {

        this.thicknessSlider = thicknessSlider;
        this.heatmapCheck = heatmapCheck;
        this.trailCheck = trailCheck;

        prefs.getDoPaintHeatmapProperty().bindBidirectional(heatmapCheck.selectedProperty());
        prefs.getDoPaintTrailProperty().bindBidirectional(trailCheck.selectedProperty());
        prefs.getThicknessScalarProperty().bindBidirectional(thicknessSlider.valueProperty());
    }
}

class PointPrefControls extends PrefControls {
    Slider IVTSpeedSlider, IDTDispersionSlider, IDTDurationSlider;
    ColorPicker lowPicker, medPicker, highPicker;
    ComboBox fixationTypes;

    PointPrefControls(Slider thicknessSlider,
                      CheckBox heatmapCheck,
                      CheckBox trailCheck,
                      PointPrefs prefs,
                      ComboBox fixationTypes,
                      Slider IVTSpeedSlider,
                      Slider IDTDispersionSlider,
                      Slider IDTDurationSlider,
                      ColorPicker lowPicker,
                      ColorPicker medPicker,
                      ColorPicker highPicker) {
        super(prefs, heatmapCheck, trailCheck, thicknessSlider);
        this.fixationTypes = fixationTypes;
        this.lowPicker = lowPicker;
        this.medPicker = medPicker;
        this.highPicker = highPicker;
        this.IVTSpeedSlider = IVTSpeedSlider;
        this.IDTDispersionSlider = IDTDispersionSlider;
        this.IDTDurationSlider = IDTDurationSlider;

        prefs.fixationType.bindBidirectional(fixationTypes.valueProperty());

        prefs.IVTSpeedThreshold.bindBidirectional(IVTSpeedSlider.valueProperty());
        prefs.IDTDispersionThreshold.bindBidirectional(IDTDispersionSlider.valueProperty());
        prefs.IDTDurationThreshold.bindBidirectional(IDTDurationSlider.valueProperty());

        prefs.highColor.bindBidirectional(highPicker.valueProperty());
        prefs.medColor.bindBidirectional(medPicker.valueProperty());
        prefs.lowColor.bindBidirectional(lowPicker.valueProperty());

    }

    PointPrefControls(String type,
                      Slider thicknessSlider,
                      CheckBox heatmapCheck,
                      ComboBox fixationTypes,
                      CheckBox trailCheck,
                      Slider IVTSpeedSlider,
                      Slider IDTDispersionSlider,
                      Slider IDTDurationSlider,
                      ColorPicker lowPicker,
                      ColorPicker medPicker,
                      ColorPicker highPicker) {
        this(thicknessSlider,
                heatmapCheck,
                trailCheck,
                type.equals("eye") ? TrackingPrefs.eyePointPrefs: TrackingPrefs.cursorPointPrefs,
                fixationTypes,
                IVTSpeedSlider,
                IDTDispersionSlider,
                IDTDurationSlider,
                lowPicker,
                medPicker,
                highPicker);
    }
}

class BoundsPrefControls extends PrefControls {
    CheckBox zpCheck, bfCheck, spCheck;
    Slider slowPanTimeSlider,
            slowPanSpeedSlider,
            boundsFixationSlider,
            zoomPeakSlider;

    BoundsPrefControls(Slider thicknessSlider,
                       CheckBox trailCheck,
                       CheckBox heatmapCheck,
                       CheckBox zpCheck,
                       CheckBox bfCheck,
                       CheckBox spCheck,
                       Slider slowPanTimeSlider,
                       Slider slowPanSpeedSlider,
                       Slider boundsFixationSlider,
                       Slider zoomPeakSlider) {
        super(TrackingPrefs.boundsPrefs, heatmapCheck, trailCheck, thicknessSlider);

        BoundsPrefs prefs = TrackingPrefs.boundsPrefs;

        this.zpCheck = zpCheck;
        this.bfCheck = bfCheck;
        this.spCheck = spCheck;

        prefs.doPaintZoomPeaks.bindBidirectional(zpCheck.selectedProperty());
        prefs.doPaintFixations.bindBidirectional(bfCheck.selectedProperty());
        prefs.doPaintSlowPans.bindBidirectional(spCheck.selectedProperty());

        this.slowPanTimeSlider = slowPanTimeSlider;
        this.slowPanSpeedSlider = slowPanSpeedSlider;
        this.zoomPeakSlider = zoomPeakSlider;
        this.boundsFixationSlider = boundsFixationSlider;

        prefs.slowPanTimeThreshold.bindBidirectional(slowPanTimeSlider.valueProperty());
        prefs.slowPanSpeedThreshold.bindBidirectional(slowPanSpeedSlider.valueProperty());
        prefs.zoomPeakIterations.bindBidirectional(zoomPeakSlider.valueProperty());
        prefs.boundsFixationTimeThreshold.bindBidirectional(boundsFixationSlider.valueProperty());
    }
}