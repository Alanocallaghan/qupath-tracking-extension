/*-
 * #%L
 * This file is part of a QuPath extension.
 * %%
 * Copyright (C) 2014 - 2016 The Queen's University of Belfast, Northern Ireland
 * Contact: IP Management (ipmanagement@qub.ac.uk)
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

package qupath.extension.tracking;

import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.helpers.dialogs.DialogHelperFX;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.gui.viewer.QuPathViewerPlus;
import qupath.lib.gui.viewer.recording.DefaultViewTracker;

import java.io.File;

/**
 * Command to help with the setup of QuPath and MATLAB integration.
 * 
 * @author Alan O'Callaghan
 *
 */
public class TrackingQuPathLoadCommand implements PathCommand {
	
	private final static Logger logger = LoggerFactory.getLogger(TrackingQuPathLoadCommand.class);
	
	private QuPathGUI qupath;
	
	public TrackingQuPathLoadCommand(final QuPathGUI qupath) {
		this.qupath = qupath;
	}

	public void run() {
        DialogHelperFX dfx = new DialogHelperFX(qupath.getStage());
        File imageFile = dfx.promptForFile(new File (System.getProperty("user.home") + "/Documents/Images/Analysis"));
        QuPathViewerPlus viewer = qupath.getViewer();

        if (qupath.openImage(null, imageFile.getAbsolutePath(), true, true, false)) {
            File file = dfx.promptForFile("Open csv",
                    new File(System.getProperty("user.home") + "/Documents/Tracking Folder/Consultants/Fri 31st 3rd"),
                    "Text files",
                    "txt", "csv", "tsv");
            if (file != null) {
                DefaultViewTracker tracker = DefaultViewTrackerFactory.createViewTracker(file);
                TrackerFeatures features = new TrackerFeatures(tracker, viewer.getServer());
                try {
                    Stage stage = TrackerPaintStage.getInstance(features);
                    stage.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                viewer.addOverlay(features.hOverlay);
            }
        }
	}

    public static void main(String args[]) {

    }


}
