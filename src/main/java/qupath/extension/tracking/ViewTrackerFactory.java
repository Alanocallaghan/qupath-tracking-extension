package qupath.extension.tracking;

import qupath.lib.gui.viewer.recording.ViewTracker;

import java.io.File;

/**
 * Created by alan on 11/03/17.
 */
public interface ViewTrackerFactory {
    ViewTracker createViewTracker(File csvfile);
}
