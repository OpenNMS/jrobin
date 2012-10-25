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
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphConstants;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraphInfo;

import java.awt.*;
import java.io.IOException;

class RrdGraphCmd extends RrdToolCmd implements RrdGraphConstants {
	static final Color BLIND_COLOR = new Color(0, 0, 0, 0);
	private RrdGraphDef gdef;

	String getCmdType() {
		return "graph";
	}

	Object execute() throws RrdException, IOException {
		gdef = getGraphDef();
		
		// create diagram finally
		RrdGraphInfo info = new RrdGraph(gdef).getRrdGraphInfo();
		if (info.getFilename().equals(RrdGraphConstants.IN_MEMORY_IMAGE)) {
			println(new String(info.getBytes()));
		}
		else {
			println(info.getWidth() + "x" + info.getHeight());
			String[] plines = info.getPrintLines();
			for (String pline : plines) {
				println(pline);
			}
			if (info.getImgInfo() != null && info.getImgInfo().length() > 0) {
				println(info.getImgInfo());
			}
		}
		return info;
	}

    /**
     * @throws RrdException
     * @returns an RRD graph definition
     */
    public RrdGraphDef getGraphDef() throws RrdException {
        RrdGraphDef gdef = new RrdGraphDef();

		// OPTIONS

		// START, END
		String t1 = getOptionValue("s", "start", DEFAULT_START), t2 = getOptionValue("e", "end", DEFAULT_END);
		gdef.setTimeSpan(Util.getTimestamps(t1, t2));
		// X-GRID
		parseXGrid(gdef, getOptionValue("x", "x-grid"));
		// Y-GRID
		parseYGrid(gdef, getOptionValue("y", "y-grid"));
		// ALT-Y-GRID
		gdef.setAltYGrid(getBooleanOption("Y", "alt-y-grid"));
		// NO_MINOR
		gdef.setNoMinorGrid(getBooleanOption(null, "no-minor"));
		// ALT-Y-MRTG
		gdef.setAltYMrtg(getBooleanOption("R", "alt-y-mrtg"));
		// ALT-AUTOSCALE
		gdef.setAltAutoscale(getBooleanOption("A", "alt-autoscale"));
		// ALT-AUTOSCALE-MAX
		gdef.setAltAutoscaleMax(getBooleanOption("M", "alt-autoscale-max"));
		// UNITS-EXPONENT
		String opt = getOptionValue("X", "units-exponent");
		if (opt != null) {
			gdef.setUnitsExponent(parseInt(opt));
		}
		// UNITS-LENGTH
		opt = getOptionValue("L", "units-length");
		if (opt != null) {
			gdef.setUnitsLength(parseInt(opt));
		}
		// VERTICAL LABEL
		opt = getOptionValue("v", "vertical-label");
		if (opt != null) {
			gdef.setVerticalLabel(opt);
		}
		// WIDTH
		opt = getOptionValue("w", "width");
		if (opt != null) {
			gdef.setWidth(parseInt(opt));
		}
		// HEIGHT
		opt = getOptionValue("h", "height");
		if (opt != null) {
			gdef.setHeight(parseInt(opt));
		}
		// INTERLACED
		gdef.setInterlaced(getBooleanOption("i", "interlaced"));
		// IMGINFO
		opt = getOptionValue("f", "imginfo");
		if (opt != null) {
			gdef.setImageInfo(opt);
		}
		// IMGFORMAT
		opt = getOptionValue("a", "imgformat");
		if (opt != null) {
			gdef.setImageFormat(opt);
		}
		// BACKGROUND
		opt = getOptionValue("B", "background");
		if (opt != null) {
			gdef.setBackgroundImage(opt);
		}
		// OVERLAY
		opt = getOptionValue("O", "overlay");
		if (opt != null) {
			gdef.setOverlayImage(opt);
		}
		// UNIT
		opt = getOptionValue("U", "unit");
		if (opt != null) {
			gdef.setUnit(opt);
		}
		// LAZY
		gdef.setLazy(getBooleanOption("z", "lazy"));
		// UPPER-LIMIT
		opt = getOptionValue("u", "upper-limit");
		if (opt != null) {
			gdef.setMaxValue(parseDouble(opt));
		}
		// LOWER-LIMIT
		opt = getOptionValue("l", "lower-limit");
		if (opt != null) {
			gdef.setMinValue(parseDouble(opt));
		}
		// RIGID
		gdef.setRigid(getBooleanOption("r", "rigid"));
		// BASE
		opt = getOptionValue("b", "base");
		if (opt != null) {
			gdef.setBase(parseDouble(opt));
		}
		// LOGARITHMIC
		gdef.setLogarithmic(getBooleanOption("o", "logarithmic"));
		// COLORS
		parseColors(gdef, getMultipleOptionValues("c", "color"));
		// NO-LEGEND
		gdef.setNoLegend(getBooleanOption("g", "no-legend"));
		// ONLY_GRAPH
		gdef.setOnlyGraph(getBooleanOption("j", "only-graph"));
		// FORCE-RULES-LEGEND
		gdef.setForceRulesLegend(getBooleanOption("F", "force-rules-legend"));
		// TITLE
		opt = getOptionValue("t", "title");
		if (opt != null) {
			gdef.setTitle(opt);
		}
		// STEP
		opt = getOptionValue("S", "step");
		if (opt != null) {
			gdef.setStep(parseLong(opt));
		}

		// NON-OPTIONS

		String[] words = getRemainingWords();
		// the first word must be a filename
		if (words.length < 2) {
			throw new RrdException("Image filename must be specified");
		}
		gdef.setFilename(words[1]);
		// parse remaining words, in no particular order
		for (int i = 2; i < words.length; i++) {
			if (words[i].startsWith("DEF:")) {
				parseDef(gdef, words[i]);
			}
			else if (words[i].startsWith("CDEF:")) {
				parseCDef(gdef, words[i]);
			}
			else if (words[i].startsWith("PRINT:")) {
				parsePrint(gdef, words[i]);
			}
			else if (words[i].startsWith("GPRINT:")) {
				parseGPrint(gdef, words[i]);
			}
			else if (words[i].startsWith("COMMENT:")) {
				parseComment(gdef, words[i]);
			}
			else if (words[i].startsWith("HRULE:")) {
				parseHRule(gdef, words[i]);
			}
			else if (words[i].startsWith("VRULE:")) {
				parseVRule(gdef, words[i]);
			}
			else if (words[i].startsWith("LINE1:") || words[i].startsWith("LINE2:") || words[i].startsWith("LINE3:")) {
				parseLine(gdef, words[i]);
			}
			else if (words[i].startsWith("AREA:")) {
				parseArea(gdef, words[i]);
			}
			else if (words[i].startsWith("STACK:")) {
				parseStack(gdef, words[i]);
			}
			else {
				throw new RrdException("Unexpected GRAPH token encountered: " + words[i]);
			}
		}
		
		return gdef;
    }

	private void parseLine(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens1 = new ColonSplitter(word).split();
		if (tokens1.length != 2 && tokens1.length != 3) {
			throw new RrdException("Invalid LINE statement: " + word);
		}
		String[] tokens2 = tokens1[1].split("#");
		if (tokens2.length != 1 && tokens2.length != 2) {
			throw new RrdException("Invalid LINE statement: " + word);
		}
		float width = Integer.parseInt(tokens1[0].substring(tokens1[0].length() - 1));
		String name = tokens2[0];
		Paint color = tokens2.length == 2 ? Util.parseColor(tokens2[1]) : BLIND_COLOR;
		String legend = tokens1.length == 3 ? tokens1[2] : null;
		graphDef.line(name, color, legend, width);
	}

	private void parseArea(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens1 = new ColonSplitter(word).split();
		if (tokens1.length != 2 && tokens1.length != 3) {
			throw new RrdException("Invalid AREA statement: " + word);
		}
		String[] tokens2 = tokens1[1].split("#");
		if (tokens2.length != 1 && tokens2.length != 2) {
			throw new RrdException("Invalid AREA statement: " + word);
		}
		String name = tokens2[0];
		Paint color = tokens2.length == 2 ? Util.parseColor(tokens2[1]) : BLIND_COLOR;
		String legend = tokens1.length == 3 ? tokens1[2] : null;
		graphDef.area(name, color, legend);
	}

	private void parseStack(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens1 = new ColonSplitter(word).split();
		if (tokens1.length != 2 && tokens1.length != 3) {
			throw new RrdException("Invalid STACK statement: " + word);
		}
		String[] tokens2 = tokens1[1].split("#");
		if (tokens2.length != 1 && tokens2.length != 2) {
			throw new RrdException("Invalid STACK statement: " + word);
		}
		String name = tokens2[0];
		Paint color = tokens2.length == 2 ? Util.parseColor(tokens2[1]) : BLIND_COLOR;
		String legend = tokens1.length == 3 ? tokens1[2] : null;
		graphDef.stack(name, color, legend);
	}

	private void parseHRule(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens1 = new ColonSplitter(word).split();
		if (tokens1.length < 2 || tokens1.length > 3) {
			throw new RrdException("Invalid HRULE statement: " + word);
		}
		String[] tokens2 = tokens1[1].split("#");
		if (tokens2.length != 2) {
			throw new RrdException("Invalid HRULE statement: " + word);
		}
		double value = parseDouble(tokens2[0]);
		Paint color = Util.parseColor(tokens2[1]);
		graphDef.hrule(value, color, tokens1.length == 3 ? tokens1[2] : null);
	}

	private void parseVRule(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens1 = new ColonSplitter(word).split();
		if (tokens1.length < 2 || tokens1.length > 3) {
			throw new RrdException("Invalid VRULE statement: " + word);
		}
		String[] tokens2 = tokens1[1].split("#");
		if (tokens2.length != 2) {
			throw new RrdException("Invalid VRULE statement: " + word);
		}
		long timestamp = Util.getTimestamp(tokens2[0]);
		Paint color = Util.parseColor(tokens2[1]);
		graphDef.vrule(timestamp, color, tokens1.length == 3 ? tokens1[2] : null);
	}

	private void parseComment(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens = new ColonSplitter(word).split();
		if (tokens.length != 2) {
			throw new RrdException("Invalid COMMENT specification: " + word);
		}
		graphDef.comment(tokens[1]);
	}


	private void parseDef(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens1 = new ColonSplitter(word).split();
		if (tokens1.length != 4) {
			throw new RrdException("Invalid DEF specification: " + word);
		}
		String[] tokens2 = tokens1[1].split("=");
		if (tokens2.length != 2) {
			throw new RrdException("Invalid DEF specification: " + word);
		}
		graphDef.datasource(tokens2[0], tokens2[1], tokens1[2], tokens1[3]);
	}

	private void parseCDef(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens1 = new ColonSplitter(word).split();
		if (tokens1.length != 2) {
			throw new RrdException("Invalid CDEF specification: " + word);
		}
		String[] tokens2 = tokens1[1].split("=");
		if (tokens2.length != 2) {
			throw new RrdException("Invalid DEF specification: " + word);
		}
		graphDef.datasource(tokens2[0], tokens2[1]);
	}

	private void parsePrint(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens = new ColonSplitter(word).split();
		if (tokens.length != 4) {
			throw new RrdException("Invalid PRINT specification: " + word);
		}
		graphDef.print(tokens[1], tokens[2], tokens[3]);
	}

	private void parseGPrint(RrdGraphDef graphDef, String word) throws RrdException {
		String[] tokens = new ColonSplitter(word).split();
		if (tokens.length != 4) {
			throw new RrdException("Invalid GPRINT specification: " + word);
		}
		graphDef.gprint(tokens[1], tokens[2], tokens[3]);
	}

	private void parseColors(RrdGraphDef graphDef, String[] colorOptions) throws RrdException {
		if (colorOptions == null) {
			return;
		}
		for (String colorOption : colorOptions) {
			String[] tokens = colorOption.split("#");
			if (tokens.length != 2) {
				throw new RrdException("Invalid COLOR specification: " + colorOption);
			}
			String colorName = tokens[0];
			Paint paint = Util.parseColor(tokens[1]);
			graphDef.setColor(colorName, paint);
		}
	}

	private void parseYGrid(RrdGraphDef graphDef, String ygrid) throws RrdException {
		if (ygrid == null) {
			return;
		}
		if (ygrid.equalsIgnoreCase("none")) {
			graphDef.setDrawYGrid(false);
			return;
		}
		String[] tokens = new ColonSplitter(ygrid).split();
		if (tokens.length != 2) {
			throw new RrdException("Invalid YGRID settings: " + ygrid);
		}
		double gridStep = parseDouble(tokens[0]);
		int labelFactor = parseInt(tokens[1]);
		graphDef.setValueAxis(gridStep, labelFactor);
	}

	private void parseXGrid(RrdGraphDef graphDef, String xgrid) throws RrdException {
		if (xgrid == null) {
			return;
		}
		if (xgrid.equalsIgnoreCase("none")) {
			graphDef.setDrawXGrid(false);
			return;
		}
		String[] tokens = new ColonSplitter(xgrid).split();
		if (tokens.length != 8) {
			throw new RrdException("Invalid XGRID settings: " + xgrid);
		}
		int minorUnit = resolveUnit(tokens[0]), majorUnit = resolveUnit(tokens[2]),
				labelUnit = resolveUnit(tokens[4]);
		int minorUnitCount = parseInt(tokens[1]), majorUnitCount = parseInt(tokens[3]),
				labelUnitCount = parseInt(tokens[5]);
		int labelSpan = parseInt(tokens[6]);
		String fmt = tokens[7];
		graphDef.setTimeAxis(minorUnit, minorUnitCount, majorUnit, majorUnitCount,
				labelUnit, labelUnitCount, labelSpan, fmt);
	}

	private int resolveUnit(String unitName) throws RrdException {
		final String[] unitNames = {"SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "YEAR"};
		final int[] units = {SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR};
		for (int i = 0; i < unitNames.length; i++) {
			if (unitName.equalsIgnoreCase(unitNames[i])) {
				return units[i];
			}
		}
		throw new RrdException("Unknown time unit specified: " + unitName);
	}
}
