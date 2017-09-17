package qupath.extension.tracking.tracker;

import qupath.lib.gui.viewer.recording.ViewRecordingFrame;

import java.util.ArrayList;

public class TrackerFeature extends ArrayList<ViewRecordingFrame> {

    public long getDuration() {
        return this.get(this.size() - 1).getTimestamp() - this.get(0).getTimestamp();
    }
}
