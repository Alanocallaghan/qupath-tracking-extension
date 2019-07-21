package qupath.extension.tracking.gui.controllers.actions;


import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.DisplayHelpers;
import qupath.lib.gui.viewer.QuPathViewer;
import java.io.File;

import static qupath.lib.gui.tma.TMASummaryViewer.logger;

public class ChooseDirAction implements EventHandler, PathCommand {

    private TextField f;

    public ChooseDirAction(TextField f) {
        super();
        this.f = f;
    }
    @Override
    public void handle(Event event) {
        QuPathGUI gui = QuPathGUI.getInstance();
        QuPathViewer viewer = gui.getViewer();

        if (viewer.getServer() != null) {
            File directory = QuPathGUI.getSharedDialogHelper().promptForDirectory(
                    new File(System.getProperty("user.home"))
            );
            f.setText(directory.toString());
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