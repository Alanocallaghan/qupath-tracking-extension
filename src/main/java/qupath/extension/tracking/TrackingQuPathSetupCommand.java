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
import qupath.lib.gui.commands.interfaces.PathCommand;

/**
 * Command to help with the setup of QuPath and MATLAB integration.
 * 
 * @author Alan O'Callaghan
 *
 */
public class TrackingQuPathSetupCommand implements PathCommand {
	
	private final static Logger logger = LoggerFactory.getLogger(TrackingQuPathSetupCommand.class);
	
	private QuPathGUI qupath;
	
	public TrackingQuPathSetupCommand(final QuPathGUI qupath) {
		this.qupath = qupath;
	}

	public void run() {

	}

    public static void main(String args[]) {

    }


}
