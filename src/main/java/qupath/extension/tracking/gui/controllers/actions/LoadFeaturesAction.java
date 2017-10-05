package qupath.extension.tracking.gui.controllers.actions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.event.Event;
import javafx.event.EventHandler;
import qupath.extension.tracking.gui.TrackerPaintStage;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.DisplayHelpers;
import qupath.lib.gui.helpers.dialogs.DialogHelperFX;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;
import qupath.lib.gui.viewer.recording.ViewTracker;

import java.io.*;

import static qupath.extension.tracking.tracker.ExtendedViewTrackerPlayback.makeTable;

public class LoadFeaturesAction implements EventHandler, PathCommand {

    @Override
    public void handle(Event event) {
        QuPathGUI gui = QuPathGUI.getInstance();
        DialogHelperFX dfx = new DialogHelperFX(gui.getStage());
        QuPathViewer viewer = gui.getViewer();

        if (viewer.getServer() != null) {
            File file = dfx.promptForFile("Open csv",
                    new File(
                            System.getProperty("user.home") +
                                    "/Documents/Tracking Folder/Consultants/Fri 31st 3rd"),
                    "Text files",
                    "*.txt", "*.csv", "*.tsv");

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
                    String str = jsonObject.get("tracker").getAsString();
                    tracker = DefaultViewTracker.parseSummaryString(
                            str,
                            null,
                            null);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                TrackerPaintStage.setTracker(tracker);
                TrackerPaintStage.getInstance().getController().resetOptions();
                TrackerPaintStage.getInstance().getController().TrackerBorderPane.setCenter(
                        makeTable(viewer, tracker));
                TrackerPaintStage.getInstance().getController().actionPlayback.setDisabled(false);

                // Initialise object
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
