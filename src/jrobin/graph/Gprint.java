/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
 *
 * (C) Copyright 2003, by Sasa Markovic.
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

package jrobin.graph;

import jrobin.core.RrdException;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
class Gprint extends Comment {
	private static final String SCALE_MARKER = "@s";
	private static final String UNIFORM_SCALE_MARKER = "@S";
	private static final String VALUE_MARKER = "@([0-9]{1})";
	private static final Pattern VALUE_PATTERN = Pattern.compile(VALUE_MARKER);
	private Source source;
	private String consolFun;

	Gprint(Source source, String consolFun, String comment) throws RrdException {
    	super(comment);
		this.source = source;
		this.consolFun = consolFun;
	}

	String getMessage() throws RrdException {
		double value = source.getAggregate(consolFun);
		Matcher m = VALUE_PATTERN.matcher(comment);
		if(m.find()) {
			String valueStr = "" + value;
			String prefixStr = "";
			String uniformPrefixStr = "";
			if(!Double.isNaN(value)) {
				int numDec = Integer.parseInt(m.group(1));
				DecimalFormat df = getDecimalFormat(numDec);
				if(shouldScale() && !shouldUniformScale()) {
					ValueScaler scaler = new ValueScaler(value);
					valueStr = df.format(scaler.getScaledValue());
					prefixStr = scaler.getPrefix();
					scaleIndex = scaler.getScaleIndex();
				}
				else if(!shouldScale() && shouldUniformScale()) {
					ValueScaler scaler = new ValueScaler(value, scaleIndex);
					valueStr = df.format(scaler.getScaledValue());
					uniformPrefixStr = scaler.getPrefix();
					scaleIndex = scaler.getScaleIndex();
				}
				else if(!shouldScale() && !shouldUniformScale()) {
					valueStr = df.format(value);
				}
				else if(shouldScale() && shouldUniformScale()) {
					throw new RrdException("You cannot specify uniform and non-uniform value " +
						"scaling at the same time");
				}
			}
            comment = comment.replaceFirst(VALUE_MARKER, valueStr);
			comment = comment.replaceFirst(SCALE_MARKER, prefixStr);
			comment = comment.replaceFirst(UNIFORM_SCALE_MARKER, uniformPrefixStr);
		}
		else {
			throw new RrdException("Could not find where to place value. No @ placeholder found");
		}
		return super.getMessage();
	}

	boolean shouldScale() {
		return comment.indexOf(SCALE_MARKER) >= 0;
	}

	boolean shouldUniformScale() {
		return comment.indexOf(UNIFORM_SCALE_MARKER) >= 0;
	}

	private DecimalFormat getDecimalFormat(int numDec) {
		String formatStr = "#,##0";
		for(int i = 0; i < numDec; i++) {
			if(i == 0) {
				formatStr += ".";
			}
			formatStr += "0";
		}
		return new DecimalFormat(formatStr);
	}

}
