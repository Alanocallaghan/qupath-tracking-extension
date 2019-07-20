package qupath.extension.tracking.gui.controllers.actions;

import javafx.event.Event;
import javafx.event.EventHandler;
import qupath.lib.gui.commands.interfaces.PathCommand;

import java.io.File;

public class RunBatchAnalysisAction implements EventHandler, PathCommand {

    File directory;
    boolean recursive;
    String regex;
    public RunBatchAnalysisAction(File directory, String regex, boolean recursive) {
        super();
        this.directory = directory;
        this.regex = regex;
        this.recursive = recursive;
    }
    @Override
    public void handle(Event event) {
//        open new temp window
    }

    @Override
    public void run() {
        handle(null);
    }
}
