package qupath.extension.tracking.gui.controllers.prefs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;

public interface TrackingPref {
    BooleanProperty getDoPaintHeatmapProperty();

    BooleanProperty getDoPaintTrailProperty();

    DoubleProperty getThicknessScalarProperty();
}
