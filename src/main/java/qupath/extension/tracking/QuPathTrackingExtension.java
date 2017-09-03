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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.OpenWebpageCommand;
import qupath.lib.gui.extensions.QuPathExtension;

import static qupath.extension.tracking.tracker.DefaultViewTrackerFactory.initDefaultViewTrackerFactory;


/**
 * QuPath extension to help facilitate visualising tracking data.
 *
 * @author Alan O'Callaghan
 *
 */
public class QuPathTrackingExtension implements QuPathExtension {

	private static Logger logger = LoggerFactory.getLogger(QuPathTrackingExtension .class);

	public void installExtension(QuPathGUI qupath) {
		initDefaultViewTrackerFactory(qupath.getViewer());

        QuPathGUI.addMenuItems(
				qupath.getMenu("Extensions>Tracking", true),
				QuPathGUI.createCommandAction(
						new OpenWebpageCommand(qupath,
							"http://github.com/Alanocallaghan/qupath-tracking-extension/issues"),
							"Bug reports/issues")
				);

		QuPathGUI.addMenuItems(
				qupath.getMenu("Extensions>Tracking", true),
				QuPathGUI.createCommandAction(new TrackingQuPathLoadCommand(qupath), "Tracking extension")
				);

        // TODO: Open CSV tracking file
        // TODO: Use md5 sum to tie tracking instance to image (using MessageDigest) http://stackoverflow.com/questions/4187111/how-to-get-the-md5sum-of-a-file-in-java

	}

	public String getName() {
		return "QuPath Tracking extension";
	}

	public String getDescription() {
		return "Helps facilitate tracking in QuPath";
	}


}
