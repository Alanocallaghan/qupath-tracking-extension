package qupath.extension.tracking.gui.controllers;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import qupath.extension.tracking.gui.TrackerPaintStage;
import qupath.extension.tracking.tracker.DefaultViewTrackerFactory;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.dialogs.DialogHelperFX;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;

import java.io.File;

/**
 * Created by alan on 03/09/17.
 */
public class LoadTrackerAction implements EventHandler, PathCommand {
//
//    private final Stage stage;
//
//    LoadTrackerAction(Stage stage) {
//        this.stage = stage;
//    }

    @Override
    public void handle(Event event) {
        QuPathGUI gui = QuPathGUI.getInstance();
        DialogHelperFX dfx = new DialogHelperFX(gui.getStage());
        QuPathViewer viewer = gui.getViewer();

        File file = dfx.promptForFile("Open csv",
                new File(
                        System.getProperty("user.home") +
                                "/Documents/Tracking Folder/Consultants/Fri 31st 3rd"),
                "Text files",
                "*.txt", "*.csv", "*.tsv");

        if (viewer.getServer() != null) {
            if (file != null) {
                DefaultViewTracker tracker = DefaultViewTrackerFactory.createViewTracker(file);
                TrackerPaintStage.updateTracker(tracker);
            }
        }
        TrackerPaintStage.getInstance().getController().resetOptions();
        TrackerPaintStage.getInstance().toFront();
    }

    @Override
    public void run() {
        handle(null);
    }
}
