package qupath.extension.tracking.gui.controllers.actions;

import qupath.extension.tracking.gui.stages.TrackerPaintStage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;

/**
 * Created by alan on 06/09/17.
 */
public class ResetTrackerAction implements PathCommand {

    @Override
    public void run() {
        TrackerPaintStage.setTracker(new DefaultViewTracker(QuPathGUI.getInstance().getViewer()));
    }
}
