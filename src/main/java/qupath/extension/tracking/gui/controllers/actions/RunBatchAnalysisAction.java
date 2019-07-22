package qupath.extension.tracking.gui.controllers.actions;

import com.google.gson.JsonObject;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import qupath.extension.tracking.tracker.DefaultViewTrackerFactory;
import qupath.extension.tracking.tracker.TrackerFeatures;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.viewer.recording.ViewTracker;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static qupath.lib.gui.tma.TMASummaryViewer.logger;


public class RunBatchAnalysisAction implements EventHandler, PathCommand {

    private final ListView<File> files;
    private final TextField outputDirectory, outputFileName;

    public RunBatchAnalysisAction(ListView files,
                                  TextField outputDirectoryField,
                                  TextField outputFileName) {
        super();
        this.files = files;
        this.outputDirectory = outputDirectoryField;
        this.outputFileName = outputFileName;
    }

    @Override
    public void handle(Event event) {
        JsonObject jsonObject = new JsonObject();
        for (File file: files.getItems()) {
            ViewTracker tracker = DefaultViewTrackerFactory.createViewTracker(file);

            TrackerFeatures features = new TrackerFeatures(
                    tracker,
                    QuPathGUI.getInstance().getViewer().getServer());

            jsonObject.add(file.toString(), features.toJSON(false));
        }
        try {
            File file = new File(outputDirectory.getText() + File.separator + outputFileName.getText());
            FileWriter fw = new FileWriter(file);
            BufferedWriter writer = new BufferedWriter(fw);
            writer.write(jsonObject.toString());
            writer.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

    @Override
    public void run() {
        handle(null);
    }
}
