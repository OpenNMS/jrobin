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

import org.jrobin.core.ConsolFuns;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;

import java.util.Arrays;

abstract class Source implements ConsolFuns {
	final private String name;
	private double[] values;

	Source(String name) {
		this.name = name;
	}

	final String getName() {
		return name;
	}

	final void setValues(double[] values) {
		this.values = values;
	}

	final double[] getValues() {
		return values;
	}

	final double getAggregate(String consolFun, double secondsPerPixel) throws RrdException {
		if(values == null) {
			throw new RrdException("Could not calculate " + consolFun +
					" for datasource [" + name + "], datasource values are still not available");
		}
		if(consolFun.equals(ConsolFuns.CF_FIRST)) {
			return values[1];
		}
		else if(consolFun.equals(ConsolFuns.CF_LAST)) {
			return values[values.length - 1];
		}
		else if(consolFun.equals(ConsolFuns.CF_MIN)) {
			return getMin();
		}
		else if(consolFun.equals(ConsolFuns.CF_MAX)) {
			return getMax();
		}
		else if(consolFun.equals(ConsolFuns.CF_AVERAGE)) {
			return getAverage();
		}
		else if(consolFun.equals(ConsolFuns.CF_TOTAL)) {
			return getTotal(secondsPerPixel);
		}
		else {
			throw new RrdException("Unsupported consolidation function: " + consolFun);
		}
	}

	final double getPercentile(int percentile) throws RrdException {
		if(values == null) {
			throw new RrdException("Could not calculate 95th percentile for datasource [" +
					name + "], datasource values are still not available");
		}
		if(percentile > 100 || percentile <= 0) {
			throw new RrdException("Invalid percentile specified: " + percentile);
		}
		return getPercentile(values, percentile);
	}

	private static double getPercentile(double[] values, int percentile) {
		int count = values.length;
		double[] valuesCopy = new double[count];
		for(int i = 0; i < count; i++) {
			valuesCopy[i] = values[i];
		}
		Arrays.sort(valuesCopy);
		// NaN values are at the end, eliminate them from consideration
		while(count > 0 && Double.isNaN(valuesCopy[count - 1])) {
			count--;
		}
		// skip top [percentile]% values
		int skipCount = (int) Math.ceil(((100 - percentile) * count) / 100.0);
		count -= skipCount;
		// if we have anything left...
		if(count > 0) {
			return valuesCopy[count - 1];
		}
		else {
			// not enough data available
			return Double.NaN;
		}
	}

	private double getTotal(double secondsPerPixel) {
		double sum = 0;
		for(int i = 1; i < values.length; i++) {
			if(!Double.isNaN(values[i])) {
				sum += values[i];
			}
		}
		return sum * secondsPerPixel;
	}

	private double getAverage() {
		double sum = 0;
		int count = 0;
		for(int i = 1; i < values.length; i++) {
			if(!Double.isNaN(values[i])) {
				sum += values[i];
				count++;
			}
		}
		return sum / count;
	}

	private double getMax() {
		double max = Double.NaN;
		for(int i = 1; i < values.length; i++) {
			max = Util.max(max, values[i]);
		}
		return max;
	}

	private double getMin() {
		double min = Double.NaN;
		for(int i = 1; i < values.length; i++) {
			min = Util.min(min, values[i]);
		}
		return min;
	}
}
