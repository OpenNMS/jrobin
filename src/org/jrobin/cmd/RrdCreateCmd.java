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

import org.jrobin.core.RrdException;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdDb;

import java.io.IOException;

class RrdCreateCmd extends RrdToolCmd {
	static final String DEFAULT_START = "now-10s";
	static final String DEFAULT_STEP = "300";

	private RrdDef rrdDef;

	public RrdCreateCmd(RrdCmdScanner cmdScanner) {
		super(cmdScanner);
	}

	String getCmdType() {
		return "create";
	}

	Object execute() throws RrdException, IOException {
		String startStr = cmdScanner.getOptionValue("b", "start", DEFAULT_START);
		TimeSpec spec = new TimeParser(startStr).parse();
		long start = spec.getTimestamp();
		String stepStr = cmdScanner.getOptionValue("s", "step", DEFAULT_STEP);
		long step = parseLong(stepStr);
		String[] words = cmdScanner.getRemainingWords();
		if(words.length < 2) {
			throw new RrdException("RRD file path not specified");
		}
		String path = words[1];
		rrdDef = new RrdDef(path, start, step);
        for(int i = 2; i < words.length; i++) {
			if(words[i].startsWith("DS:")) {
				parseDef(words[i]);
			}
			else if(words[i].startsWith("RRA:")) {
				parseRra(words[i]);
			}
			else {
				throw new RrdException("Invalid word in the rrdcreate syntax: " + words[i]);
			}
		}
		return createRrdDb();
	}

	private void parseDef(String word) throws RrdException {
		// DEF:name:type:heratbeat:min:max
		String[] tokens = word.split(":");
		if(tokens.length < 6) {
			throw new RrdException("Invalid DS definition: " + word);
		}
		String dsName = tokens[1];
		String dsType = tokens[2];
		long heartbeat = parseLong(tokens[3]);
		double min = parseDouble(tokens[4]);
		double max = parseDouble(tokens[5]);
		rrdDef.addDatasource(dsName, dsType, heartbeat, min, max);
	}

	private void parseRra(String word) throws RrdException {
		// RRA:cfun:xff:steps:rows
		String[] tokens = word.split(":");
		if(tokens.length < 5) {
			throw new RrdException("Invalid RRA definition: " + word);
		}
		String cf = tokens[1];
		double xff = parseDouble(tokens[2]);
		int steps = parseInt(tokens[3]);
		int rows = parseInt(tokens[4]);
		rrdDef.addArchive(cf, xff, steps, rows);
	}

	private String createRrdDb() throws IOException, RrdException {
		RrdDb rrdDb = getRrdDbReference(rrdDef);
		releaseRrdDbReference(rrdDb);
		return rrdDef.getPath();
	}
}
