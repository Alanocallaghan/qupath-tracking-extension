package qupath.extension.tracking.gui.controllers.prefs;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

public class PointPrefs extends TrackingPref {

    public DoubleProperty IVTSpeedThreshold = new SimpleDoubleProperty(),
            IDTDispersionThreshold = new SimpleDoubleProperty(),
            IDTDurationThreshold = new SimpleDoubleProperty();

    public StringProperty fixationType = new SimpleStringProperty();
    private DoubleProperty durationSizeScalar = new SimpleDoubleProperty();

    JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("fixationType", new JsonPrimitive(fixationType.getValue()));
        object.add("lowColor", colorToJsonObject((Color)(lowColorProperty.getValue())));
        object.add("medColor", colorToJsonObject((Color)(medColorProperty.getValue())));
        object.add("highColor", colorToJsonObject((Color)(highColorProperty.getValue())));
        object.add("IVTSpeedThreshold", new JsonPrimitive(IVTSpeedThreshold.getValue()));
        object.add("IDTDispersionThreshold", new JsonPrimitive(IDTDispersionThreshold.getValue()));
        object.add("IDTDurationThreshold", new JsonPrimitive(IDTDurationThreshold.getValue()));
        object.add("doPaintTrail", new JsonPrimitive(doPaintTrail .getValue()));
        object.add("doPaintHeatmap", new JsonPrimitive(doPaintHeatmap.getValue()));
        object.add("thicknessScalar", new JsonPrimitive(thicknessScalar.getValue()));

        return object;
    }

    void fromJson(JsonObject jsonObject) {
        doPaintTrail.setValue(jsonObject.get("doPaintTrail").getAsBoolean());
        doPaintHeatmap.setValue(jsonObject.get("doPaintHeatmap").getAsBoolean());
        thicknessScalar.setValue(jsonObject.get("thicknessScalar").getAsLong());

        lowColorProperty.setValue(jsonObjectToColor(jsonObject.get("lowColor").getAsJsonObject()));
        medColorProperty.setValue(jsonObjectToColor(jsonObject.get("medColor").getAsJsonObject()));
        highColorProperty.setValue(jsonObjectToColor(jsonObject.get("highColor").getAsJsonObject()));

        IVTSpeedThreshold.setValue(jsonObject.get("IVTSpeedThreshold").getAsDouble());
        IDTDispersionThreshold.setValue(jsonObject.get("IDTDispersionThreshold").getAsDouble());
        IDTDurationThreshold.setValue(jsonObject.get("IDTDurationThreshold").getAsDouble());

        fixationType.setValue(jsonObject.get("fixationType").getAsString());


    }

    static JsonObject colorToJsonObject(Color color) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("r", new JsonPrimitive(color.getRed()));
        jsonObject.add("g", new JsonPrimitive(color.getGreen()));
        jsonObject.add("b", new JsonPrimitive(color.getBlue()));
        return jsonObject;
    }

    static Color jsonObjectToColor(JsonObject jsonObject) {
        return new Color(jsonObject.get("r").getAsDouble(),
                jsonObject.get("g").getAsDouble(),
                jsonObject.get("b").getAsDouble(),
                1);
    }

    public DoubleProperty getDurationSizeScalar() {
        return durationSizeScalar;
    }
}