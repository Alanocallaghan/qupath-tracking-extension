package qupath.extension.tracking.tracker;

import java.util.ArrayList;

public class TrackerFeatureList extends ArrayList<TrackerFeature> {

    double[] getDurations() {
        double[] out = new double[this.size()];
        for (int i = 0; i < this.size(); i ++) {
            out[i] = this.get(i).getDuration();
        }
        return out;
    }
}
