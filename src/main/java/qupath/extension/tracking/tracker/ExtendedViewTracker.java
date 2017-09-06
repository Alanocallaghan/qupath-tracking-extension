package qupath.extension.tracking.tracker;

import qupath.extension.tracking.TrackerUtils;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;
import qupath.lib.gui.viewer.recording.ViewRecordingFrame;

import java.util.List;

/**
 * Created by alan on 03/09/17.
 */
class ExtendedViewTracker extends DefaultViewTracker {

    private List<ViewRecordingFrame> extendedFrames;

    private ExtendedViewTracker(QuPathViewer viewer) {
        super(viewer);
    }

    public ExtendedViewTracker(QuPathViewer viewer, ExtendedViewRecordingFrame[] frames) {
        this(viewer);
        this.appendFrames(frames);
    }

    private void appendFrames(ExtendedViewRecordingFrame[] frames) {
        for (ViewRecordingFrame frame : frames) {
            appendFrame(frame);
        }
    }

    @Override
    public ExtendedViewRecordingFrame getFrame(int index) {
        return (ExtendedViewRecordingFrame)this.extendedFrames.get(index);
    }

    @Override
    public void doStartRecording() {
        super.doStartRecording();
        System.out.println(nFrames());
    }

    public int getNColumns() {
        int[] nColumns = new int[nFrames()];
        for (int i = 0; i < nFrames(); i++) {
            nColumns[i] = getFrame(i).getNColumns();
        }
        return TrackerUtils.intArrayMax(nColumns);
    }
}
