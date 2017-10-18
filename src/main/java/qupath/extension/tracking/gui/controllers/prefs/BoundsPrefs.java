package qupath.extension.tracking.gui.controllers.prefs;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javafx.beans.property.*;

public class BoundsPrefs extends TrackingPref {

    public BooleanProperty doPaintFixations = new SimpleBooleanProperty(),
            doPaintSlowPans = new SimpleBooleanProperty(),
            doPaintZoomPeaks = new SimpleBooleanProperty();

    public DoubleProperty slowPanTimeThreshold = new SimpleDoubleProperty(),
            slowPanSpeedThreshold = new SimpleDoubleProperty(),
            boundsFixationTimeThreshold = new SimpleDoubleProperty();

    public LongProperty zoomPeakIterations = new SimpleLongProperty();
    public ObjectProperty zoomPeakStartColorProperty = new SimpleObjectProperty(),
            zoomPeakPathColorProperty = new SimpleObjectProperty(),
            zoomPeakEndColorProperty = new SimpleObjectProperty();

    JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("doPaintTrail", new JsonPrimitive(doPaintTrail.getValue()));
        object.add("doPaintHeatmap", new JsonPrimitive(doPaintHeatmap.getValue()));
        object.add("doPaintFixations", new JsonPrimitive(doPaintFixations.getValue()));
        object.add("doPaintSlowPans", new JsonPrimitive(doPaintSlowPans.getValue()));
        object.add("doPaintZoomPeaks", new JsonPrimitive(doPaintZoomPeaks.getValue()));
        object.add("thicknessScalar", new JsonPrimitive(thicknessScalar.getValue()));
        object.add("slowPanTimeThreshold", new JsonPrimitive(slowPanTimeThreshold.getValue()));
        object.add("slowPanSpeedThreshold", new JsonPrimitive(slowPanSpeedThreshold.getValue()));
        object.add("boundsFixationTimeThreshold", new JsonPrimitive(boundsFixationTimeThreshold.getValue()));
        object.add("zoomPeakIterations", new JsonPrimitive(zoomPeakIterations.getValue()));
        return object;
    }

    void fromJson(JsonObject jsonObject) {
//        doPaintTrail.setValue(jsonObject.get("doPaintTrail").getAsBoolean());
//        doPaintHeatmap.setValue(jsonObject.get("doPaintHeatmap").getAsBoolean());
//        doPaintFixations.setValue(jsonObject.get("doPaintFixations").getAsBoolean());
//        doPaintSlowPans.setValue(jsonObject.get("doPaintSlowPans").getAsBoolean());
//        doPaintZoomPeaks.setValue(jsonObject.get("doPaintZoomPeaks").getAsBoolean());
//        thicknessScalar.setValue(jsonObject.get("thicknessScalar").getAsLong());

        slowPanTimeThreshold.setValue(jsonObject.get("slowPanTimeThreshold").getAsDouble());
        slowPanSpeedThreshold.setValue(jsonObject.get("slowPanSpeedThreshold").getAsDouble());
        boundsFixationTimeThreshold.setValue(jsonObject.get("boundsFixationTimeThreshold").getAsDouble());
        zoomPeakIterations.setValue(jsonObject.get("zoomPeakIterations").getAsLong());
    }

}
