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
import org.jrobin.graph.RrdExportDef;
import org.jrobin.graph.RrdExport;
import org.jrobin.graph.ExportData;

import java.io.IOException;

class RrdXportCmd extends RrdToolCmd {
	static final String DEFAULT_START = "end-1day";
	static final String DEFAULT_END = "now";

	String getCmdType() {
		return "xport";
	}

	Object execute() throws RrdException, IOException
	{
		// --start
		String startStr = getOptionValue("s", "start", DEFAULT_START);
		TimeSpec spec1 = new TimeParser(startStr).parse();

		// --end
		String endStr = getOptionValue("e", "end", DEFAULT_END);
		TimeSpec spec2 = new TimeParser(endStr).parse();
		long[] timestamps = TimeSpec.getTimestamps(spec1, spec2);

		// --step
		String resolutionStr = getOptionValue("step", null, "1");		// Smallest step possible is default
		long resolution = parseLong(resolutionStr);

		// --maxrows
		String maxrowsStr = getOptionValue("m", "maxrows", "400");
		int maxRows = parseInt(maxrowsStr);

		RrdExportDef exportDef = new RrdExportDef(timestamps[0], timestamps[1]);
		exportDef.setResolution(resolution);
		exportDef.setStrictExport(true);												// Always use strict export in case of RrdTool command

		String[] words = getRemainingWords();

		for (int i = 0; i < words.length; i++) {
			if (words[i].startsWith("DEF:"))
				parseDef(words[i], exportDef);
			else if (words[i].startsWith("CDEF:"))
				parseCdef(words[i], exportDef);
			else if (words[i].startsWith("XPORT:"))
				parseXport(words[i], exportDef);
			else {
				throw new RrdException("Invalid xport syntax: " + words[i]);
			}
		}

		// Now create the export data
		RrdExport export = new RrdExport(exportDef);
		ExportData data = export.fetch(maxRows);

		println(data.exportXml());

		return data;
	}

	/**
	 * DEF:vname=rrd:ds-name:CF
	 */
	private void parseDef(String word, RrdExportDef def) throws RrdException {
		String[] tokens = word.split(":");

		if (tokens.length != 4)
			throw new RrdException("Invalid DEF command: " + word);

		String[] token1 = tokens[1].split("=");

		if (token1.length != 2)
			throw new RrdException("Invalid DEF command: " + word);

		def.datasource(token1[0], token1[1], tokens[2], tokens[3]);
	}

	/**
	 * CDEF:vname=rpn-expression
	 */
	private void parseCdef(String word, RrdExportDef def) throws RrdException {
		String[] tokens = word.split(":");

		if (tokens.length != 2)
			throw new RrdException("Invalid CDEF command: " + word);

		String[] token1 = tokens[1].split("=");

		if (token1.length != 2)
			throw new RrdException("Invalid CDEF command: " + word);

		def.datasource(token1[0], token1[1]);
	}

	/**
	 * XPORT:vname:legend
	 */
	private void parseXport(String word, RrdExportDef def) throws RrdException {
		String[] tokens = word.split(":");

		if (tokens.length < 2 || tokens.length > 3)
			throw new RrdException("Invalid XPORT command: " + word);

		if (tokens.length == 2)
			def.export(tokens[1]);
		else
			def.export(tokens[1], tokens[2]);
	}
	
	/*
	public static void main(String[] args) throws Exception {
		String cmd = "xport --start now-1h --end now DEF:xx=host-inout.lo.rrd:output:AVERAGE DEF:yy=host-inout.lo.rrd:input:AVERAGE CDEF:aa=xx,yy,+,8,* "
				+ "XPORT:xx:\"out bytes\" XPORT:aa:\"in and out bits\"";

		cmd = "xport --start now-1h --end now -m 10 DEF:xx=/code/idea-projects/jrobin/res/demo/eth0.rrd:ifOutOctets:AVERAGE XPORT:xx:outgoing traffic";

		RrdCommander.execute(cmd);
	}
	*/
}
