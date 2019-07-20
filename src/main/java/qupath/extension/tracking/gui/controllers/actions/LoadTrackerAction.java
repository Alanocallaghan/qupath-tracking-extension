package qupath.extension.tracking.gui.controllers.actions;

import javafx.event.Event;
import javafx.event.EventHandler;
import qupath.extension.tracking.gui.stages.TrackerPaintStage;
import qupath.extension.tracking.tracker.DefaultViewTrackerFactory;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.DisplayHelpers;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;

import java.io.File;
import java.lang.reflect.Field;

import static qupath.extension.tracking.tracker.ExtendedViewTrackerPlayback.makeTable;

/**
 * Created by alan on 03/09/17.
 */
public class LoadTrackerAction implements EventHandler, PathCommand {

    @Override
    public void handle(Event event) {
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();

        if (viewer.getServer() != null) {

            File file = QuPathGUI.getSharedDialogHelper().promptForFile("Open csv",
                    new File(
                            System.getProperty("user.home") +
                                    "/Documents/Tracking Folder/Consultants/Fri 31st 3rd"),
                    "Text files",
                    "*.txt", "*.csv", "*.tsv");

            if (file != null) {
                DefaultViewTracker tracker = DefaultViewTrackerFactory.createViewTracker(file);
                try {
                    Field declaredField = DefaultViewTracker.class.getDeclaredField("doCursorTracking");
                    declaredField.setAccessible(true);
                    declaredField.set(tracker, true);
                } catch(IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }

                TrackerPaintStage.setTracker(tracker);
                TrackerPaintStage.getInstance().getController().TrackerBorderPane.setCenter(
                        makeTable(viewer, tracker));
                TrackerPaintStage.getInstance().getController().actionPlayback.setDisabled(false);
            }
            TrackerPaintStage.getInstance().toFront();
        } else {
            DisplayHelpers.showErrorMessage("No image open!",
                    "Cannot open tracking data when no image is open.");
        }
    }

    @Override
    public void run() {
        handle(null);
    }
}
