/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org)
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
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
package org.jrobin.graph;

class ValueScaler {
	static final String UNIT_UNKNOWN = "?";
	static final String UNIT_SYMBOLS[] = {
			"a", "f", "p", "n", "u", "m", " ", "k", "M", "G", "T", "P", "E"
	};
	static final int SYMB_CENTER = 6;

	private final double base;
	private double magfact = -1; // nothing scaled before, rescale
	private String unit;

	ValueScaler(double base) {
		this.base = base;
	}

	Scaled scale(double value, boolean mustRescale) {
		Scaled scaled;
		if (mustRescale) {
			scaled = rescale(value);
		}
		else if (magfact >= 0) {
			// already scaled, need not rescale
			scaled = new Scaled(value / magfact, unit);
		}
		else {
			// scaling not requested, but never scaled before - must rescale anyway
			scaled = rescale(value);
			// if zero, scale again on the next try
			if (scaled.value == 0.0 || Double.isNaN(scaled.value)) {
				magfact = -1.0;
			}
		}
		return scaled;
	}

	private Scaled rescale(double value) {
		int sindex;
		if (value == 0.0 || Double.isNaN(value)) {
			sindex = 0;
			magfact = 1.0;
		}
		else {
			sindex = (int) (Math.floor(Math.log(Math.abs(value)) / Math.log(base)));
			magfact = Math.pow(base, sindex);
		}
		if (sindex <= SYMB_CENTER && sindex >= -SYMB_CENTER) {
			unit = UNIT_SYMBOLS[sindex + SYMB_CENTER];
		}
		else {
			unit = UNIT_UNKNOWN;
		}
		return new Scaled(value / magfact, unit);
	}

	static class Scaled {
		double value;
		String unit;

		public Scaled(double value, String unit) {
			this.value = value;
			this.unit = unit;
		}

		void dump() {
			System.out.println("[" + value + unit + "]");
		}
	}
}
