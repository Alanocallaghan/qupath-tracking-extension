package qupath.extension.tracking.gui.controllers.actions;

import javafx.event.Event;
import javafx.event.EventHandler;
import qupath.extension.tracking.gui.stages.BatchAnalysisStage;
import qupath.lib.gui.commands.interfaces.PathCommand;

public class BatchAnalysisAction implements EventHandler, PathCommand {

    @Override
    public void handle(Event event) {
//        open new temp window
        BatchAnalysisStage stage = BatchAnalysisStage.getInstance();
        stage.show();
    }

    @Override
    public void run() {
        handle(null);
    }
}
