/*******************************************************************************
 * Copyright (c) 2001-2005 Sasa Markovic and Ciaran Treanor.
 * Copyright (c) 2011 The OpenNMS Group, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/
package org.jrobin.cmd;

import org.jrobin.core.RrdException;
import org.jrobin.core.Util;
import org.jrobin.core.XmlWriter;
import org.jrobin.data.DataProcessor;
import org.jrobin.graph.RrdGraphConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class RrdXportCmd extends RrdToolCmd implements RrdGraphConstants {
	private DataProcessor dproc;
	private List<XPort> xports;

	String getCmdType() {
		return "xport";
	}

	Object execute() throws RrdException, IOException {
		String startStr = getOptionValue("s", "start", DEFAULT_START);
		String endStr = getOptionValue("e", "end", DEFAULT_END);
		long span[] = Util.getTimestamps(startStr, endStr);
		dproc = new DataProcessor(span[0], span[1]);
		xports = new ArrayList<XPort>();
		long step = parseLong(getOptionValue(null, "step", "1"));
		int maxRows = parseInt(getOptionValue("m", "maxrows", "400"));
		long minStep = (long) Math.ceil((span[1] - span[0]) / (double) (maxRows - 1));
		step = Math.max(step, minStep);
		dproc.setStep(step);
		String[] words = getRemainingWords();
		if (words.length < 2) {
			throw new RrdException("Incomplete XPORT command");
		}
		for (int i = 1; i < words.length; i++) {
			if (words[i].startsWith("DEF:")) {
				parseDef(words[i]);
			}
			else if (words[i].startsWith("CDEF:")) {
				parseCDef(words[i]);
			}
			else if (words[i].startsWith("XPORT:")) {
				parseXport(words[i]);
			}
			else {
				throw new RrdException("Invalid XPORT syntax: " + words[i]);
			}
		}
		String result = xports.size() == 0 ? null : xport();
		println(xports.size() == 0 ? "No XPORT statement found, nothing done" : result);
		return result;
	}

	private String xport() throws IOException, RrdException {
		dproc.processData();
		long[] timestamps = dproc.getTimestamps();
		for (XPort xport : xports) {
			xport.values = dproc.getValues(xport.name);
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		XmlWriter w = new XmlWriter(stream);
		w.startTag("xport");
		w.startTag("meta");
		w.writeTag("start", timestamps[0]);
		w.writeTag("step", timestamps[1] - timestamps[0]);
		w.writeTag("end", timestamps[timestamps.length - 1]);
		w.writeTag("rows", timestamps.length);
		w.writeTag("columns", xports.size());
		w.startTag("legend");
		for (XPort xport1 : xports) {
			w.writeTag("entry", xport1.legend);
		}
		w.closeTag(); // legend
		w.closeTag(); // meta
		w.startTag("data");
		for (int i = 0; i < timestamps.length; i++) {
			w.startTag("row");
			w.writeComment(new Date(timestamps[i] * 1000L));
			w.writeTag("t", timestamps[i]);
			for (XPort xport : xports) {
				w.writeTag("v", xport.values[i]);
			}
			w.closeTag(); // row
		}
		w.closeTag(); // data
		w.closeTag(); // xport
		w.flush();
		String result = stream.toString();
		stream.close();
		return result;
	}

	private void parseDef(String word) throws RrdException {
		// DEF:vname=rrd:ds-name:CF
		String[] tokens1 = new ColonSplitter(word).split();
		if (tokens1.length != 4) {
			throw new RrdException("Invalid DEF syntax: " + word);
		}
		String[] tokens2 = tokens1[1].split("=");
		if (tokens2.length != 2) {
			throw new RrdException("Invalid DEF syntax: " + word);
		}
		dproc.addDatasource(tokens2[0], tokens2[1], tokens1[2], tokens1[3]);
	}

	private void parseCDef(String word) throws RrdException {
		// CDEF:vname=rpn-expression
		String[] tokens1 = new ColonSplitter(word).split();
		if (tokens1.length != 2) {
			throw new RrdException("Invalid CDEF syntax: " + word);
		}
		String[] tokens2 = tokens1[1].split("=");
		if (tokens2.length != 2) {
			throw new RrdException("Invalid CDEF syntax: " + word);
		}
		dproc.addDatasource(tokens2[0], tokens2[1]);
	}

	private void parseXport(String word) throws RrdException {
		// XPORT:vname[:legend]
		String[] tokens = new ColonSplitter(word).split();
		if (tokens.length == 2 || tokens.length == 3) {
			XPort xport = new XPort(tokens[1], tokens.length == 3 ? tokens[2] : null);
			xports.add(xport);
		}
		else {
			throw new RrdException("Invalid XPORT syntax: " + word);
		}
	}

	static class XPort {
		String name, legend;
		double[] values;

		XPort(String name, String legend) {
			this.name = name;
			this.legend = legend != null ? legend : "";
		}
	}
}
