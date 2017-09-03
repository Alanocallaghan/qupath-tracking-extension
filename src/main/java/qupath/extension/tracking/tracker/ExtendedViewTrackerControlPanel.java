package qupath.extension.tracking.tracker;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.scene.Node;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCombination;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.icons.PathIconFactory;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.*;

/**
 * Taken from QuPath source code on 03/09/2017
 * @author Pete Bankhead
 * Modified by Alan O'Callaghan
 * Created by alan on 03/09/17.
 */

public class ExtendedViewTrackerControlPanel {
    private static final Node iconRecord;
    private static final Node iconRecordStop;
    private static final Node iconPlay;
    private static final Node iconPlayStop;
    private ViewTracker tracker = null;
    private ToolBar toolbar = new ToolBar();

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

    public ExtendedViewTrackerControlPanel(QuPathViewer viewer,
                                           ViewTracker tracker) {

        if (tracker == null) {
            this.tracker = new DefaultViewTracker(viewer);
        } else {
            this.tracker = tracker;
        }

        ExtendedViewTrackerPlayback playback = new ExtendedViewTrackerPlayback(
                this.tracker, viewer);
        Action actionRecord = QuPathGUI.createSelectableCommandAction(
                this.tracker.recordingProperty(),
                "Record",
                iconRecord,
                (KeyCombination)null);

        Action actionPlayback = QuPathGUI.createSelectableCommandAction(
                playback.playingProperty(),
                "Play", iconPlay, (KeyCombination)null);
        actionPlayback.setDisabled(this.tracker.isEmpty());
        actionRecord.disabledProperty().bind(actionPlayback.selectedProperty());
        this.tracker.recordingProperty().addListener((v, o, n) -> {
            if (n) {
                actionRecord.setGraphic(iconRecordStop);
                actionRecord.setText("Stop recording");
                actionPlayback.setDisabled(true);
            } else {
                actionRecord.setGraphic(iconRecord);
                actionRecord.setText("Start recording");
                actionPlayback.setDisabled(this.tracker.isEmpty());
            }

        });
        BooleanProperty playing = playback.playingProperty();
        actionPlayback.graphicProperty().bind(
                Bindings.createObjectBinding(() ->
                    playing.get() ? iconPlayStop : iconPlay, playing));
        actionPlayback.textProperty().bind(
                Bindings.createStringBinding(() ->
                    playing.get() ? "Stop" : "Play", playing));
        this.toolbar.getItems().addAll(
                ActionUtils.createToggleButton(actionRecord,
                        ActionUtils.ActionTextBehavior.HIDE),
                ActionUtils.createToggleButton(actionPlayback,
                        ActionUtils.ActionTextBehavior.HIDE),
                new Separator(),
                ActionUtils.createButton(
                        QuPathGUI.createCommandAction(
                            new ViewTrackerExportCommand(viewer, this.tracker),
                                "Export")),
                ActionUtils.createButton(
                        QuPathGUI.createCommandAction(
                                new ViewTrackerExportCommand(viewer, this.tracker),
                                "Import"))
        );
    }

    public Node getNode() {
        return this.toolbar;
    }

    public ViewTracker getViewTracker() {
        return this.tracker;
    }
}
