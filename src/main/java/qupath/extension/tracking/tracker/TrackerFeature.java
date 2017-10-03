package qupath.extension.tracking.tracker;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;
import qupath.lib.gui.viewer.recording.ViewTracker;

import java.util.ArrayList;

public class TrackerFeature extends ArrayList<Integer> {

    private final ViewTracker tracker;

    TrackerFeature(ViewTracker tracker) {
        super();
        this.tracker = tracker;
    }

    ViewRecordingFrame getFrame(int index) {
        return tracker.getFrame(index);
    }

    public ViewRecordingFrame getFrameAtFeatureIndex(int index) {
        return getFrame(get(index));
    }

    long getDuration() {
        return this.getFrameAtFeatureIndex(this.size() - 1).getTimestamp()
            - this.getFrameAtFeatureIndex(0).getTimestamp();
    }

    JsonObject toJSON(boolean includeDurations) {
        JsonArray array = new JsonArray();
        for (int i: this) {
            array.add(i);
        }
        JsonObject object = new JsonObject();
        if (includeDurations) {
            object.add("duration", new JsonPrimitive(this.getDuration()));
        }
        object.add("indices", array);
        return object;
    }
}
