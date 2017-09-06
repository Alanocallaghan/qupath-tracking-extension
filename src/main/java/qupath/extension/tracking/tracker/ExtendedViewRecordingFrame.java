package qupath.extension.tracking.tracker;

import qupath.lib.gui.viewer.recording.ViewRecordingFrame;

import java.awt.*;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by alan on 03/09/17.
 */
public class ExtendedViewRecordingFrame extends ViewRecordingFrame {

    private static final DecimalFormat df = new DecimalFormat("#.##");


    public ExtendedViewRecordingFrame(long timestamp,
                                      Shape region,
                                      Dimension canvasSize) {
        super(timestamp, region, canvasSize);
    }

    public ExtendedViewRecordingFrame(long timestamp,
                                      Shape region,
                                      Dimension canvasSize,
                                      Point2D cursorPosition) {
        super(timestamp,
                region,
                canvasSize,
                cursorPosition);
    }

    public ExtendedViewRecordingFrame(long timestamp,
                                      Shape region,
                                      Dimension canvasSize,
                                      Point2D cursorPosition,
                                      Point2D eyePosition,
                                      Boolean isFixated) {
        super(timestamp,
                region,
                canvasSize,
                cursorPosition,
                eyePosition,
                isFixated);
    }

    public int getNColumns() {
        int nColumns = 7;
        if (hasCursorPosition()) {
            nColumns = +2;
        }
        if (hasEyePosition()) {
            nColumns = +3;
        }
        return nColumns;
    }

    public String[] getColumnNames() {
        String[] columnNames = new String[getNColumns()];
        columnNames[0] = "Timestamp";
        columnNames[1] = "X";
        columnNames[2] = "Y";
        columnNames[3] = "Width";
        columnNames[4] = "Height";
        columnNames[5] = "Canvas width";
        columnNames[6] = "Canvas height";
        if (hasCursorPosition()) {
            columnNames[7] = "Cursor X";
            columnNames[8] = "Cursor Y";
        }
        if (hasEyePosition()) {
            int indFix, indX, indY;
            if (hasCursorPosition()) {
                indX = 9;
                indY = 10;
                indFix = 11;
            } else {
                indX = 7;
                indY = 8;
                indFix = 9;
            }
            columnNames[indX] = "Eye X";
            columnNames[indY] = "Eye Y";
            columnNames[indFix] = "Eye fixated";
        }
        return columnNames;
    }

    private String toDelimitedString(String delim, String[] strings) {
        return String.join(delim, strings);
    }

    public String toDelimitedString(String delim) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add(String.valueOf(getTimestamp()));
        Rectangle bounds = getImageBounds();
        strings.add(String.valueOf(bounds.x));
        strings.add(String.valueOf(bounds.y));
        strings.add(String.valueOf(bounds.width));
        strings.add(String.valueOf(bounds.height));
        Dimension canvasSize = getSize();
        strings.add(String.valueOf(canvasSize.width));
        strings.add(String.valueOf(canvasSize.height));

        Point2D position;
        if (hasCursorPosition()) {
            position = getCursorPosition();
            if (position != null) {
                strings.add(df.format(position.getX()));
                strings.add(df.format(position.getY()));
            } else {
                strings.add("");
                strings.add("");
            }
        }

        if (hasEyePosition()) {
            position = getEyePosition();
            strings.add(df.format(position.getX()));
            strings.add(df.format(position.getY()));
            Boolean fixated = isEyeFixated();
            strings.add(fixated == null ? "": fixated.toString());
        }
        Object[] objectArray = strings.toArray();
        return toDelimitedString(
                delim,
                Arrays.copyOf(objectArray, objectArray.length, String[].class));
    }
}
