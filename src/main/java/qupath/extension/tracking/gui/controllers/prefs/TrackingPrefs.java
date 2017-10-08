package qupath.extension.tracking.gui.controllers.prefs;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class TrackingPrefs {

    public static PointPrefs eyePointPrefs = new PointPrefs(),
        cursorPointPrefs = new PointPrefs();

    public static BoundsPrefs boundsPrefs = new BoundsPrefs();

    public static JsonObject toJSON() {
        JsonObject object = new JsonObject();
        object.add("bounds_feature_prefs", boundsPrefs.toJSON());
        object.add("cursor_prefs", cursorPointPrefs.toJSON());
        object.add("eye_prefs", eyePointPrefs.toJSON());
        return object;
    }

    public static void fromJson(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        boundsPrefs.fromJson(jsonObject.get("bounds_feature_prefs").getAsJsonObject());
        cursorPointPrefs.fromJson(jsonObject.get("cursor_prefs").getAsJsonObject());
        eyePointPrefs.fromJson(jsonObject.get("eye_prefs").getAsJsonObject());
    }
}
