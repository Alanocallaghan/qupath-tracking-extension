package qupath.extension.tracking.gui.controllers.prefs;

import javafx.beans.property.*;

public abstract class TrackingPref {

    BooleanProperty doPaintHeatmap = new SimpleBooleanProperty(),
            doPaintTrail = new SimpleBooleanProperty();

    DoubleProperty thicknessScalar = new SimpleDoubleProperty();
    ObjectProperty highColorProperty = new SimpleObjectProperty();
    ObjectProperty lowColorProperty = new SimpleObjectProperty();
    ObjectProperty medColorProperty = new SimpleObjectProperty();

    public BooleanProperty getDoPaintHeatmapProperty() {
        return doPaintHeatmap;
    }

    public BooleanProperty getDoPaintTrailProperty() {
        return doPaintTrail;
    }

    public DoubleProperty getThicknessScalarProperty() {
        return thicknessScalar;
    }

    public ObjectProperty getHighColorProperty() {
        return highColorProperty;
    }

    public ObjectProperty getLowColorProperty() {
        return lowColorProperty;
    }

    public ObjectProperty getMedColorProperty() {
        return medColorProperty;
    }
}
