package qupath.extension.tracking;

import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;

import java.io.*;

/**
 * @author Alan O'Callaghan
 * Created by Alan O'Callaghan on 11/03/17.
 */
public class DefaultViewTrackerFactory {
    private static QuPathViewer quPath;

    public static void initDefaultViewTrackerFactory(QuPathViewer quPathGUI) {
        quPath = quPathGUI;
    }

    public static DefaultViewTracker createViewTracker(File csvFile) {
        DefaultViewTracker tracker = new DefaultViewTracker(quPath);
        try {
            tracker = (DefaultViewTracker)DefaultViewTracker.parseSummaryString(readFile(csvFile), null, tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tracker;
    }

    private static String readFile(File file) {
        BufferedReader br = null;
        String everything = null;
        try {
            br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            everything = sb.toString();
        } catch (IOException ie) {
            ie.printStackTrace();
        } finally {
            if(br!=null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return everything;
    }

}
