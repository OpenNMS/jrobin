/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
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

package jrobin.core;

/**
 * Class to represent data source values for the specific timestamp. Objects of this class
 * are created during the fetching process. See {@link jrobin.core.FetchRequest#fetch() fetch()}
 * method of the {@link jrobin.core.FetchRequest FetchRequest} class.
 *
 * @author <a href="mailto:saxon@eunet.yu">Sasa Markovic</a>
 */
public class FetchPoint {

	private long time;
	private double[] values;

	FetchPoint(long time, int size) {
		this.time = time;
		values = new double[size];
		for(int i = 0; i < size; i++) {
			values[i] = Double.NaN;
		}
	}

	/**
	 * Returns timestamp associated with this fetch point.
	 * @return Timestamp in seconds.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Returns array of data source values for the associated timestamp. Data source values
	 * are returned in the order of their definition.
	 *
	 * @return Array of data source values.
	 */
	public double[] getValues() {
		return values;
	}

	/**
	 * Returns number of data source values (same as number od data sources defined in RRD file).
	 * @return Number of data source values.
	 */
	public int getSize() {
		return values.length;
	}

	/**
	 * Returns the i-th data source value. Data source values follow the order of
	 * data sources definition.
	 * @param i Data source index.
	 * @return Value of the i-th data source.
	 */
	public double getValue(int i) {
		return values[i];
	}

	void setValue(int index, double value) {
		values[index] = value;
	}

	/**
	 * Returns string representing timestamp and all data source values.
	 * @return Fetch point dump.
	 */
	public String dump() {
		StringBuffer buffer = new StringBuffer(time + ": ");
		for(int i = 0; i < values.length; i++) {
			buffer.append(Util.formatDouble(values[i]));
			buffer.append(" ");
		}
		return buffer.toString();
	}
}
