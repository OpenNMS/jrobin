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

package org.jrobin.cmd;

import org.jrobin.core.*;
import java.io.IOException;

class RrdUpdateCommand extends RrdToolCmd {
	private String[] dsNames;

	RrdUpdateCommand(RrdCmdScanner cmdScanner) {
		super(cmdScanner);
	}

	String getCmdType() {
		return "update";
	}

	Object execute() throws RrdException, IOException {
		String template = cmdScanner.getOptionValue("t", "template");
		if (template != null) {
			dsNames = template.split(":");
		}
		String[] words = cmdScanner.getRemainingWords();
		if (words.length < 3) {
			throw new RrdException("Insufficent number of parameters for rrdupdate");
		}
		String path = words[1];
		RrdDb rrdDb = getRrdDbReference(path);
		try {
			if (dsNames != null) {
				// template specified, check datasource names
				for (int i = 0; i < dsNames.length; i++) {
					rrdDb.getDsIndex(dsNames[i]); // will throw exception if not found
				}
			}
			// parse update strings
			long timestamp = -1;
			for (int i = 2; i < words.length; i++) {
				String[] tokens = words[i].split(":");
				if (dsNames != null && dsNames.length + 1 != tokens.length) {
					throw new RrdException("Template required " + dsNames.length + " values, " +
							(tokens.length - 1) + " value(s) found in: " + words[i]);
				}
				int dsCount = rrdDb.getHeader().getDsCount();
				if (dsNames == null && dsCount + 1 != tokens.length) {
					throw new RrdException("Expected " + dsCount + " values, " +
							(tokens.length - 1) + " value(s) found in: " + words[i]);
				}
				TimeSpec spec = new TimeParser(tokens[0]).parse();
				timestamp = spec.getTimestamp();
				Sample sample = rrdDb.createSample(timestamp);
				for (int j = 1; j < tokens.length; j++) {
					if (dsNames == null) {
						sample.setValue(j - 1, parseDouble(tokens[j]));
					} else {
						sample.setValue(dsNames[j - 1], parseDouble(tokens[j]));
					}
				}
				sample.update();
			}
			return new Long(timestamp);
		} finally {
			releaseRrdDbReference(rrdDb);
		}
	}
}
