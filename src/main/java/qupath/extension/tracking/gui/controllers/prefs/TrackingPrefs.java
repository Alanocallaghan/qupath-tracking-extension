package qupath.extension.tracking.gui.controllers.prefs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class TrackingPrefs {

    public static PointPrefs eyePointPrefs = new PointPrefs(),
        cursorPointPrefs = new PointPrefs();

    public static BoundsPrefs boundsPrefs = new BoundsPrefs();

    public static DoubleProperty lowZoomThreshold = new SimpleDoubleProperty(),
        medZoomThreshold = new SimpleDoubleProperty();

    public static JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("boundsFeaturePrefs", boundsPrefs.toJSON());
        object.add("cursorPrefs", cursorPointPrefs.toJSON());
        object.add("eyePrefs", eyePointPrefs.toJSON());

        JsonObject globalPrefs = new JsonObject();
        globalPrefs.add("lowZoomThreshold", new JsonPrimitive(lowZoomThreshold.get()));
        globalPrefs.add("medZoomThreshold", new JsonPrimitive(medZoomThreshold.get()));
        object.add("globalPrefs", globalPrefs);

        return object;
    }

    public static void fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        boundsPrefs.fromJson(jsonObject.get("boundsFeaturePrefs").getAsJsonObject());
        cursorPointPrefs.fromJson(jsonObject.get("cursorPrefs").getAsJsonObject());
        eyePointPrefs.fromJson(jsonObject.get("eyePrefs").getAsJsonObject());
        JsonObject globalPrefs = jsonObject.get("globalPrefs").getAsJsonObject();

        lowZoomThreshold.setValue(globalPrefs.get("lowZoomThreshold").getAsDouble());
        medZoomThreshold.setValue(globalPrefs.get("medZoomThreshold").getAsDouble());
    }
}
