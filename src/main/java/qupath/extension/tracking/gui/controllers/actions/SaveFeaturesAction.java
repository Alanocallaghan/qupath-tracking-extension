package qupath.extension.tracking.gui.controllers.actions;

import com.google.gson.JsonObject;
import javafx.event.Event;
import javafx.event.EventHandler;
import qupath.extension.tracking.gui.stages.TrackerPaintStage;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPrefs;
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

                try (PrintWriter out = new PrintWriter(fileExport)) {
                    JsonObject json = new JsonObject();
                    json.add("features", features.toJSON());
                    json.add("preferences", TrackingPrefs.toJSON());
                    out.print(json);
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
