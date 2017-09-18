package qupath.extension.tracking.gui.controllers;

import qupath.extension.tracking.gui.TrackerPaintStage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;

/**
 * Created by alan on 06/09/17.
 */
class ResetTrackerAction implements PathCommand {

    @Override
    public void run() {
        TrackerPaintStage.setTracker(new DefaultViewTracker(QuPathGUI.getInstance().getViewer()));
    }
}
