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
import org.jrobin.core.Util;
import org.jrobin.core.RrdException;

/**
 * General purpose class which calculate statistics (AVERAGE, MIN, MAX, LAST, FIRST and TOTAL)
 * for the given array of data values and corresponding timestamps.
 */
public class DataStats implements ConsolFuns {
	private long seconds = 0;
	private double total = 0.0;
	private double min = Double.NaN, max = Double.NaN;
	private double last = Double.NaN, first = Double.NaN;

	/**
	 * Creates statistics for the given array of data values.
	 *
	 * @param times   array of timestamps in seconds
	 * @param values array of corresponding data values
	 * @param tStart Time to be used as starting timestamp (in seconds) for statistics calculation
	 * @param tEnd   Time to be used as ending timestamp (in seconds) for statistics calculation
	 */
	public DataStats(long[] times, double[] values, long tStart, long tEnd) {
		assert times.length == values.length: "Incompatible time/value array lengths";
		assert tStart < tEnd: "Startign timestamp must be less than ending timestamp";
		accumulate(tStart, times[0], values[0]);
		for (int i = 0; i < values.length - 1; i++) {
			long t1 = Math.max(tStart, times[i]);
			long t2 = Math.min(tEnd, times[i + 1]);
			double value = values[i + 1];
			accumulate(t1, t2, value);
		}
		accumulate(times[times.length - 1], tEnd, Double.NaN);
	}

	private boolean firstFound = false;

	private void accumulate(long t1, long t2, double value) {
		long dt = t2 - t1;
		if (dt > 0) {
			if (!Double.isNaN(value)) {
				seconds += dt;
				total += dt * value;
			}
			min = Util.min(min, value);
			max = Util.max(max, value);
			if (!firstFound) {
				first = value;
				firstFound = true;
			}
			last = value;
		}
	}

	/**
	 * Returns data TOTAL
	 *
	 * @return TOTAL value
	 */
	public double getTotal() {
		return total;
	}

	/**
	 * Returns MIN data value
	 *
	 * @return MIN value
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Returns MAX data value
	 *
	 * @return MAX value
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Returns LAST data value
	 *
	 * @return LAST value
	 */
	public double getLast() {
		return last;
	}

	/**
	 * Returns FIRST data value
	 *
	 * @return FIRST value
	 */
	public double getFirst() {
		return first;
	}

	/**
	 * Returns AVERAGE data value
	 *
	 * @return AVERAGE value
	 */
	public double getAverage() {
		return total / (double) seconds;
	}

	/**
	 * Returns aggregated data value
	 *
	 * @param consolFun Consolidation function to be used
	 *                  (AVERAGE, MIN, MAX, LAST, FIRST or TOTAL)
	 * @return Aggregated value
	 * @throws org.jrobin.core.RrdException Thrown if invalid consolidation function is supplied
	 */
	public double getAggregate(String consolFun) throws RrdException {
		if (consolFun.equals(CF_MAX)) {
			return getMax();
		}
		else if (consolFun.equals(CF_MIN)) {
			return getMin();
		}
		else if (consolFun.equals(CF_LAST)) {
			return getLast();
		}
		else if (consolFun.equals(CF_AVERAGE)) {
			return getAverage();
		}
		else if (consolFun.equals(CF_TOTAL)) {
			return getTotal();
		}
		else {
			throw new RrdException("Unsupported consolidation function [" + consolFun + "]");
		}
	}

	/**
	 * Dumps all aggregated values in a human-readable form.
	 *
	 * @return A string containing all aggregated values.
	 */
	public String dump() {
		return "AVERAGE: " + getAverage() + "\n" +
				"MIN:     " + min + "\n" +
				"MAX:     " + max + "\n" +
				"LAST:    " + last + "\n" +
				"FIRST:   " + first + "\n" +
				"TOTAL:   " + total + "\n" +
				"SECONDS: " + seconds;
	}

	/*
	public static void main(String[] args) {
		long[] t =   {10, 12, 15, 20};
		double[] x = { 1,  2,  5,  3};
		long t1 = 11, t2 = 16;
		DataStats s = new DataStats(t, x, t1, t2);
		System.out.println(s.dump());
	}
	*/
}
