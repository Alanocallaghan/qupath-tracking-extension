package qupath.extension.tracking.gui.controllers.actions;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.helpers.dialogs.DialogHelperFX;

import java.io.File;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static qupath.extension.tracking.TrackerUtils.saveSnapshot;

/**
 * Created by alan on 06/09/17.
 */
public class SaveSnapshotAction implements EventHandler<ActionEvent> {
    @Override
    public void handle(ActionEvent event) {
        String date = LocalDateTime.now().format(ISO_LOCAL_DATE_TIME);

        File file = QuPathGUI.getSharedDialogHelper().promptToSaveFile("Save snapshot",
                new File(System.getProperty("user.home")),
                "QuPath_snapshot_" + date,
                "png",
                ".png"
        );
        if (file != null) {
            saveSnapshot(QuPathGUI.getInstance().getViewer(), file);
        }
    }
}
