/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package org.jrobin.mrtg.server;

import java.io.File;

class Config {
	private static final String DELIM = System.getProperty("file.separator");

	private static final String HOME_DIR = System.getProperty("user.home") + DELIM +
		"mrtg" + DELIM;
	private static final String CONF_DIR = HOME_DIR + "conf" + DELIM;
	private static final String RRD_DIR  = HOME_DIR + "rrd" + DELIM;
	private static final String HARDWARE_FILE = CONF_DIR + "mrtg.dat";

	static {
		// create directories if not found
		new File(CONF_DIR).mkdirs();
		new File(RRD_DIR).mkdirs();
	}

	static String getHomeDir() {
		return HOME_DIR;
	}

	static String getConfDir() {
		return CONF_DIR;
	}

	static String getRrdDir() {
		return RRD_DIR;
	}

	static String getHardwareFile() {
        return HARDWARE_FILE;
	}
}
