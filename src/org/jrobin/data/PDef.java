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

package org.jrobin.data;

import org.jrobin.graph.Plottable;

class PDef extends Source {
	private final Plottable plottable;

	PDef(String name, Plottable plottable) {
		super(name);
		this.plottable = plottable;
	}

	PDef(String name, final Plottable plottable, final int index) {
		super(name);
		this.plottable = new Plottable() {
			public double getValue(long timestamp) {
				return plottable.getValue(timestamp, index);
			}
		};
	}

	PDef(String name, final Plottable plottable, final String sourceName) {
		super(name);
		this.plottable = new Plottable() {
			public double getValue(long timestamp) {
				return plottable.getValue(timestamp, sourceName);
			}
		};
	}

	void calculateValues() {
		long[] times = getTimestamps();
		double[] vals = new double[times.length];
		for(int i = 0; i < times.length; i++) {
			vals[i] = plottable.getValue(times[i]);
		}
		setValues(vals);
	}
}
