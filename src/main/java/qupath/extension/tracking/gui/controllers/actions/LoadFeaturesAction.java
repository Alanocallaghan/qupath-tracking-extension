package qupath.extension.tracking.gui.controllers.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javafx.event.Event;
import javafx.event.EventHandler;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.DisplayHelpers;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;
import qupath.lib.gui.viewer.recording.ViewTracker;

import qupath.extension.tracking.gui.stages.TrackerPaintStage;
import qupath.extension.tracking.gui.controllers.prefs.TrackingPrefs;

import java.io.*;
import java.lang.reflect.Field;

import static qupath.extension.tracking.tracker.ExtendedViewTrackerPlayback.makeTable;

public class LoadFeaturesAction implements EventHandler, PathCommand {

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
                JsonParser parser = new JsonParser();
                JsonObject jsonObject;
                JsonElement jsonElement = null;
                try {
                    jsonElement = parser.parse(new FileReader(file));
                } catch (IOException ie) {
                    ie.printStackTrace();
                }
                if (jsonElement != null) {
                    jsonObject = jsonElement.getAsJsonObject();
                    ViewTracker tracker = null;
                    try {
                        String str = jsonObject.get("features").getAsJsonObject().get("tracker").getAsString();
                        TrackingPrefs.fromJson(jsonObject.get("preferences").getAsJsonObject());
                        tracker = DefaultViewTracker.parseSummaryString(
                                str,
                                null,
                                null);
                        try {
                            Field declaredField = DefaultViewTracker.class.getDeclaredField("doCursorTracking");
                            declaredField.setAccessible(true);
                            declaredField.set(tracker, true);
                        } catch(IllegalAccessException | NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    TrackerPaintStage.setTracker(tracker);
                    TrackerPaintStage.getInstance().getController().TrackerBorderPane.setCenter(
                            makeTable(viewer, tracker));
                    TrackerPaintStage.getInstance().getController().actionPlayback.setDisabled(false);

                    // Initialise object
                }
                TrackerPaintStage.getInstance().toFront();
            }
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
