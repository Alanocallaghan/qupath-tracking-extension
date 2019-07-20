package qupath.extension.tracking.gui.controllers.actions;


import javafx.event.Event;
import javafx.event.EventHandler;
import qupath.extension.tracking.gui.controllers.BatchAnalysisController;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.DisplayHelpers;
import qupath.lib.gui.viewer.QuPathViewer;

import java.io.File;

public class ChooseDirAction implements EventHandler, PathCommand {

    private final BatchAnalysisController b;

    public ChooseDirAction(BatchAnalysisController b) {
        super();
        this.b = b;
    }
    @Override
    public void handle(Event event) {
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();

        if (viewer.getServer() != null) {
            File directory = QuPathGUI.getSharedDialogHelper().promptForDirectory(
                    new File(System.getProperty("user.home"))
            );
            b.setDirectory(directory);
        } else {
            DisplayHelpers.showErrorMessage("An error message",
                    "A helpful description.");
        }
    }


    @Override
    public void run() {
        handle(null);
    }
}