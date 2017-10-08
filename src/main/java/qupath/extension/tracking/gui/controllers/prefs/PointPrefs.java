package qupath.extension.tracking.gui.controllers.prefs;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javafx.beans.property.*;
import javafx.scene.paint.Color;

public class PointPrefs implements TrackingPref {

    public ObjectProperty lowColor = new SimpleObjectProperty(),
        medColor = new SimpleObjectProperty(),
        highColor = new SimpleObjectProperty();

    public DoubleProperty IVTSpeedThreshold = new SimpleDoubleProperty(),
            IDTDispersionThreshold = new SimpleDoubleProperty(),
            IDTDurationThreshold = new SimpleDoubleProperty();

    public StringProperty fixationType = new SimpleStringProperty();

    BooleanProperty doPaintTrail = new SimpleBooleanProperty(),
            doPaintHeatmap = new SimpleBooleanProperty();

    DoubleProperty thicknessScalar = new SimpleDoubleProperty();


    public JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("fixationType", new JsonPrimitive(fixationType.getValue()));
        object.add("lowColor", colorToJsonObject((Color)(lowColor.getValue())));
        object.add("medColor", colorToJsonObject((Color)(medColor.getValue())));
        object.add("highColor", colorToJsonObject((Color)(highColor.getValue())));
        object.add("IVTSpeedThreshold", new JsonPrimitive(IVTSpeedThreshold.getValue()));
        object.add("IDTDispersionThreshold", new JsonPrimitive(IDTDispersionThreshold.getValue()));
        object.add("IDTDurationThreshold", new JsonPrimitive(IDTDurationThreshold.getValue()));
        object.add("doPaintTrail", new JsonPrimitive(doPaintTrail .getValue()));
        object.add("doPaintHeatmap", new JsonPrimitive(doPaintHeatmap.getValue()));
        object.add("thicknessScalar", new JsonPrimitive(thicknessScalar.getValue()));

        return object;
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

    public void fromJson(JsonObject jsonObject) {
        doPaintTrail.setValue(jsonObject.get("doPaintTrail").getAsBoolean());
        doPaintHeatmap.setValue(jsonObject.get("doPaintHeatmap").getAsBoolean());

        lowColor.setValue(jsonObjectToColor(jsonObject.get("lowColor").getAsJsonObject()));
        medColor.setValue(jsonObjectToColor(jsonObject.get("medColor").getAsJsonObject()));
        highColor.setValue(jsonObjectToColor(jsonObject.get("highColor").getAsJsonObject()));

        IVTSpeedThreshold.setValue(jsonObject.get("IVTSpeedThreshold").getAsDouble());
        IDTDispersionThreshold.setValue(jsonObject.get("IDTDispersionThreshold").getAsDouble());
        IDTDurationThreshold.setValue(jsonObject.get("IDTDurationThreshold").getAsDouble());

        fixationType.setValue(jsonObject.get("fixationType").getAsString());

        thicknessScalar.setValue(jsonObject.get("thicknessScalar").getAsLong());

    }

    public static JsonObject colorToJsonObject(Color color) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("r", new JsonPrimitive(color.getRed()));
        jsonObject.add("g", new JsonPrimitive(color.getGreen()));
        jsonObject.add("b", new JsonPrimitive(color.getBlue()));
        return jsonObject;
    }

    public static Color jsonObjectToColor(JsonObject jsonObject) {
        return new Color(jsonObject.get("r").getAsDouble(),
                jsonObject.get("g").getAsDouble(),
                jsonObject.get("b").getAsDouble(),
                1);
    }
}