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

import org.jrobin.core.RrdException;

class SDef extends Source {
	private String defName;
	private String consolFun;
	private double value;

	SDef(String name, String defName, String consolFun) {
		super(name);
		this.defName = defName;
		this.consolFun = consolFun;
	}

	String getDefName() {
		return defName;
	}

	String getConsolFun() {
		return consolFun;
	}

	void setValue(double value) {
		this.value = value;
		int count = getTimestamps().length;
		double[] values = new double[count];
		for(int i = 0; i < count; i++) {
			values[i] = value;
		}
		setValues(values);
	}

	Aggregates getAggregates(long tStart, long tEnd) throws RrdException {
		Aggregates agg = new Aggregates();
		agg.first = agg.last = agg.min = agg.max = agg.average = value;
		agg.total = value * (tEnd - tStart);
		return agg;
	}

	double getPercentile(long tStart, long tEnd, double percentile) throws RrdException {
		return value;
	}
}
