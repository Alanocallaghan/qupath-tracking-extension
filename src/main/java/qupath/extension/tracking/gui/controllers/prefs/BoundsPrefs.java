package qupath.extension.tracking.gui.controllers.prefs;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javafx.beans.property.*;

public class BoundsPrefs implements TrackingPref {

    public BooleanProperty doPaintFixations = new SimpleBooleanProperty(),
            doPaintSlowPans = new SimpleBooleanProperty(),
            doPaintZoomPeaks = new SimpleBooleanProperty();

    BooleanProperty doPaintHeatmap = new SimpleBooleanProperty(),
            doPaintTrail = new SimpleBooleanProperty();

    DoubleProperty thicknessScalar = new SimpleDoubleProperty();

    public DoubleProperty slowPanTimeThreshold = new SimpleDoubleProperty(),
            slowPanSpeedThreshold = new SimpleDoubleProperty(),
            boundsFixationTimeThreshold = new SimpleDoubleProperty();

    public LongProperty zoomPeakIterations = new SimpleLongProperty();

    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("doPaintTrail", new JsonPrimitive(doPaintTrail.getValue()));
        object.add("doPaintHeatmap", new JsonPrimitive(doPaintHeatmap.getValue()));
        object.add("doPaintFixations", new JsonPrimitive(doPaintFixations.getValue()));
        object.add("doPaintSlowPans", new JsonPrimitive(doPaintSlowPans.getValue()));
        object.add("doPaintZoomPeaks", new JsonPrimitive(doPaintZoomPeaks.getValue()));
        object.add("slowPanTimeThreshold", new JsonPrimitive(slowPanTimeThreshold.getValue()));
        object.add("slowPanSpeedThreshold", new JsonPrimitive(slowPanSpeedThreshold.getValue()));
        object.add("boundsFixationTimeThreshold", new JsonPrimitive(boundsFixationTimeThreshold.getValue()));
        object.add("zoomPeakIterations", new JsonPrimitive(zoomPeakIterations.getValue()));
        object.add("thicknessScalar", new JsonPrimitive(thicknessScalar.getValue()));
        return object;
    }

    public void fromJson(JsonObject jsonObject) {
        doPaintTrail.setValue(jsonObject.get("doPaintTrail").getAsBoolean());
        doPaintHeatmap.setValue(jsonObject.get("doPaintHeatmap").getAsBoolean());
        doPaintFixations.setValue(jsonObject.get("doPaintFixations").getAsBoolean());
        doPaintSlowPans.setValue(jsonObject.get("doPaintSlowPans").getAsBoolean());
        doPaintZoomPeaks.setValue(jsonObject.get("doPaintZoomPeaks").getAsBoolean());

        slowPanTimeThreshold.setValue(jsonObject.get("slowPanTimeThreshold").getAsDouble());
        slowPanSpeedThreshold.setValue(jsonObject.get("slowPanSpeedThreshold").getAsDouble());

        boundsFixationTimeThreshold.setValue(jsonObject.get("boundsFixationTimeThreshold").getAsDouble());

        zoomPeakIterations.setValue(jsonObject.get("zoomPeakIterations").getAsLong());

        thicknessScalar.setValue(jsonObject.get("thicknessScalar").getAsLong());
    }

    @Override
    public BooleanProperty getDoPaintHeatmapProperty() {
        return doPaintHeatmap;
    }

    @Override
    public BooleanProperty getDoPaintTrailProperty() {
        return doPaintTrail;
    }

    @Override
    public DoubleProperty getThicknessScalarProperty() {
        return thicknessScalar;
    }

}
