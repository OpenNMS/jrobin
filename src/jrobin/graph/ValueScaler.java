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

class ValueScaler {
	static final int NO_SCALE = -1;
	private static double[] VALUES = new double[] {
		1e18, 1e15, 1e12, 1e9, 1e6, 1e3, 1e0, 1e-3, 1e-6, 1e-9, 1e-12, 1e-15
	};
	private static String[] PREFIXES = new String[] {
		"E",  "P",  "T",  "G", "M", "k", "",  "m", "micro", "n", "p",  "f"
	};
	private String prefix;
	private double scaledValue;
	private int scaleIndex;

	ValueScaler(double value) {
		this(value, NO_SCALE);
	}

	ValueScaler(double value, int scaleIndex) {
		if(scaleIndex == NO_SCALE) {
			this.prefix = "";
			this.scaledValue = value;
			for(int i = 0; i < VALUES.length; i++) {
				if(value >= VALUES[i] && value < VALUES[i] * 1000.0) {
					this.prefix = PREFIXES[i];
					this.scaledValue = value / VALUES[i];
					this.scaleIndex = i;
					return;
				}
			}
		}
		else {
			this.prefix = PREFIXES[scaleIndex];
			this.scaledValue = value / VALUES[scaleIndex];
			this.scaleIndex = scaleIndex;
		}
	}

	String getPrefix() {
		return prefix;
	}

	double getScaledValue() {
		return scaledValue;
	}

	public int getScaleIndex() {
		return scaleIndex;
	}
}
