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

package org.jrobin.core;

/**
 * Dumb class which contains statistics (AVERAGE, MIN, MAX, LAST and TOTAL) calculated from the
 * {@link FetchData#getStats(String, String) FetchData} returned by a single {@link FetchRequest}.
 */
public class FetchDataStats implements ConsolFuns {
	private long seconds;
	private double total, min, max, last;

	FetchDataStats() {
		// just to prohibit explicit creation
	}

	void setSeconds(long seconds) {
		this.seconds = seconds;
	}

	void setTotal(double total) {
		this.total = total;
	}

	void setMin(double min) {
		this.min = min;
	}

	void setMax(double max) {
		this.max = max;
	}

	void setLast(double last) {
		this.last = last;
	}

	/**
	 * Returns TOTAL of the fetched data
	 * @return TOTAL of the fetched data
	 */
	public double getTotal() {
		return total;
	}

	/**
	 * Returns MIN of the fetched data
	 * @return MIN of the fetched data
	 */
	public double getMin() {
		return min;
	}

	/**
	 * Returns MAX of the fetched data
	 * @return MAX of the fetched data
	 */
	public double getMax() {
		return max;
	}

	/**
	 * Returns LAST of the fetched data
	 * @return LAST of the fetched data
	 */
	public double getLast() {
		return last;
	}

	/**
	 * Returns AVERAGE of the fetched data
	 * @return AVERAGE of the fetched data
	 */
	public double getAverage() {
		return total / (double) seconds;
	}

	/**
	 * Returns aggregated value of the fetch data
	 * @param consolFun Consolidation function to be used
	 * (AVERAGE, MIN, MAX, LAST and TOTAL)
	 * @return Aggregated value
	 * @throws RrdException Thrown if invalid consolidation function is supplied
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
	 * @return A string containing all aggregated values.
	 */
	public String dump() {
		return "AVERAGE: " + Util.formatDouble(getAverage(), true) + "\n" +
			   "MIN:     " + Util.formatDouble(getMin(), true) + "\n" +
			   "MAX:     " + Util.formatDouble(getMax(), true) + "\n" +
			   "LAST:    " + Util.formatDouble(getLast(), true) + "\n" +
			   "TOTAL:   " + Util.formatDouble(getTotal(), true);
	}
}
