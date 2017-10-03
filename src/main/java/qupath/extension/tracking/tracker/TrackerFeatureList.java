package qupath.extension.tracking.tracker;

import com.google.gson.JsonArray;

import java.util.ArrayList;

public class TrackerFeatureList extends ArrayList<TrackerFeature> {

    double[] getDurations() {
        double[] out;
        if (!this.isEmpty()) {
            out = new double[this.size()];
            for (int i = 0; i < this.size(); i++) {
                out[i] = this.get(i).getDuration();
            }
        } else {
            out = null;
        }
        return out;
    }

    public JsonArray toJSON(boolean includeDurations) {
        JsonArray jsonArray = new JsonArray();
        for (TrackerFeature trackerFeature: this) {
            jsonArray.add(trackerFeature.toJSON(includeDurations));
        }
        return jsonArray;
    }
}
