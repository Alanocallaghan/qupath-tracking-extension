package qupath.extension.tracking.gui.controllers.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import qupath.extension.tracking.gui.stages.TrackerPaintStage;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.DisplayHelpers;
import qupath.lib.gui.viewer.recording.ViewTracker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by alan on 06/09/17.
 */
public class SaveTrackerAction implements EventHandler<ActionEvent>, PathCommand {

    @Override
    public void handle(ActionEvent event) {
        ViewTracker tracker = TrackerPaintStage.getTracker();
        if (tracker == null || tracker.isEmpty()) {
            DisplayHelpers.showErrorMessage("Tracking export", "Tracker is empty - nothing to export!");
        } else {
            File fileExport = QuPathGUI.getSharedDialogHelper().promptToSaveFile(
                    null,
                    null,
                    null,
                    "QuPath tracking data (xls)",
                    "xls");
            if (fileExport != null) {
                try (PrintWriter out = new PrintWriter(fileExport)) {
                    out.print(tracker.getSummaryString());
                } catch (FileNotFoundException fe) {
                    fe.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        handle(null);
    }
}
