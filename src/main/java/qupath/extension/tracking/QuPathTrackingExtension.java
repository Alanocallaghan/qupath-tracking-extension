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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.commands.OpenWebpageCommand;
import qupath.lib.gui.commands.interfaces.PathCommand;
import qupath.lib.gui.extensions.QuPathExtension;

/**
 * QuPath extension to help facilitate integration between QuPath and R.
 * 
 * This is mostly concerned with providing scripts, and moving them into the right places.
 * 
 * @author Alan O'Callaghan
 *
 */
public class QuPathTrackingExtension implements QuPathExtension {

	private static Logger logger = LoggerFactory.getLogger(QuPathTrackingExtension .class);


	public void installExtension(QuPathGUI qupath) {
		QuPathGUI.addMenuItems(
				qupath.getMenu("Extensions>Tracking", true),
				QuPathGUI.createCommandAction(new OpenWebpageCommand(qupath, "http://go.qub.ac.uk/qupath-matlab"), "QuPath-MATLAB documentation (web)")
				);
//
//		QuPathGUI.addMenuItems(
//				qupath.getMenu("Extensions>MATLAB", true),
//				QuPathGUI.createCommandAction(new TrackingQuPathSetupCommand(qupath), "Export MATLAB scripts")
//				);
//
//		// Add script for setting engine path
//		QuPathGUI.addMenuItems(
//				qupath.getMenu("Extensions>MATLAB", true),
//				QuPathGUI.createCommandAction(new TrackingQuPathSetupCommand(qupath), "Set path to MATLAB engine"),
//				null
//				);
//
//		// Link useful scripts
//		for (Entry<String, String> entry : readScriptMap("groovy", ".groovy").entrySet()) {
//			Menu menuGroovy = qupath.getMenu("Extensions>MATLAB>Groovy MATLAB samples", true);
//			String scriptName = entry.getKey().replaceAll("_", " ").replaceAll("/", " ").trim();
//			if (scriptName.toLowerCase().endsWith(".groovy"))
//				scriptName = scriptName.substring(0, scriptName.length() - ".groovy".length());
//			MenuItem item = new MenuItem(scriptName);
//			item.setOnAction(e -> {
//				qupath.getScriptEditor().showScript(entry.getKey(), entry.getValue());
//			});
//			QuPathGUI.addMenuItems(menuGroovy, item);
//		}
		
	}

	public String getName() {
		return "QuPath Tracking extension";
	}

	public String getDescription() {
		return "Helps facilitate tracking in QuPath";
	}


}
