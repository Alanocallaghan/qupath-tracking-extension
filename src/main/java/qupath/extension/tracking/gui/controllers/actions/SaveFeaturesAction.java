package qupath.extension.tracking.gui.controllers.actions;

import javafx.event.Event;
import javafx.event.EventHandler;
import qupath.extension.tracking.gui.TrackerPaintStage;
import qupath.extension.tracking.tracker.TrackerFeatures;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.DisplayHelpers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class SaveFeaturesAction implements EventHandler, PathCommand {

    @Override
    public void handle(Event event) {
        TrackerFeatures features = TrackerPaintStage.getTrackerOverlay().getTrackerFeatures();
        if (features == null || features.getTracker().isEmpty()) {
            DisplayHelpers.showErrorMessage("Tracking export", "No features - nothing to export!");
        } else {
            String date = LocalDateTime.now().format(ISO_LOCAL_DATE_TIME);

            File fileExport = QuPathGUI.getSharedDialogHelper().promptToSaveFile(
                    null,
                    null,
                    "QuPath_tracking_features_" + date,
                    "QuPath tracking features (json)",
                    ".json");
            if (fileExport != null) {
                PrintWriter out = null;

                try {
                    out = new PrintWriter(fileExport);
                    out.print(features.toJSON());
                } catch (FileNotFoundException fe) {
                    fe.printStackTrace();
                } finally {
                    if(out != null) {
                        out.close();
                    }
                }
            }
        }

    }

    @Override
    public void run() {
        handle(null);
    }
}