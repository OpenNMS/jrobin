/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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

import java.io.IOException;

/**
 * Class to represent archive values for a single datasource. Robin class is the heart of
 * the so-called "round robin database" concept. Basically, each Robin object is a
 * fixed length array of double values. Each double value reperesents consolidated archive
 * value for the specific timestamp. When the underlying array of double values gets completely
 * filled, new values will replace the oldest entries.<p>
 *
 * Robin object does not hold values in memory - such object could be quite large.
 * Instead of it, Robin stores all values on the disk and reads them only when necessary.
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
 */
public class Robin implements RrdUpdater {
	private Archive parentArc;
	private RrdInt pointer;
	private RrdDoubleArray values;
	private int rows;

	Robin(Archive parentArc, int rows) throws IOException {
		this.parentArc = parentArc;
		this.rows = rows;
		if(getRrdFile().getMode() == RrdFile.MODE_CREATE) { 
			pointer = new RrdInt(0, this);
			values = new RrdDoubleArray(this, rows, Double.NaN);
		}
		else {
			pointer = new RrdInt(this);
			values = new RrdDoubleArray(this, rows);
		}
	}

	/**
	 * Fetches all Robin archive values from the disk.
	 *
	 * @return Array of double archive values, starting from the oldest one.
	 * @throws IOException Thrown in case of IO specific error.
	 */
	public double[] getValues() throws IOException {
		double[] result = new double[rows];
		int start = pointer.get();
		for(int i = start, j = 0; i < start + rows; i++, j++) {
			result[j] = values.get(i % rows);
		}
		return result;
	}

	void store(double newValue) throws IOException {
		int position = pointer.get();
		values.set(position, newValue);
		pointer.set((position + 1) % rows);
	}

	/**
	 * Returns the underlying RrdFile object.
	 * @return Underlying RrdFile object
	 */
	public RrdFile getRrdFile() {
		return parentArc.getRrdFile();
	}

	String dump() throws IOException {
		StringBuffer buffer = new StringBuffer("Robin " + pointer.get() + "/" + rows + ": ");
		int startPos = pointer.get();
		for(int i = startPos; i < startPos + rows; i++) {
			buffer.append(Util.formatDouble(values.get(i % rows), true) + " ");
		}
		buffer.append("\n");
		return buffer.toString();
	}

	/**
	 * Returns the i-th value from the Robin archive.
	 * @param index Value index
	 * @return Value stored in the i-th position (the oldest value has zero index)
	 */
	public double getValue(int index) throws IOException {
		return values.get((pointer.get() + index) % rows);
	}

	/**
	 * Returns the Archive object to which this Robin object belongs.
	 *
	 * @return Parent Archive object
	 */
	public Archive getParent() {
		return parentArc;
	}

	/**
	 * Returns the size of the underlying array of archive values.
	 *
	 * @return Number of stored values
	 */
	public int getSize() {
		return rows;
	}

	/**
	 * Copies object's internal state to another Robin object.
	 * @param other New Robin object to copy state to
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown if supplied argument is not a Robin object or
	 * is a Robin object with different number of rows
	 */
	public void copyStateTo(RrdUpdater other) throws IOException, RrdException {
		if(!(other instanceof Robin)) {
			throw new RrdException(
				"Cannot copy Archive object to " + other.getClass().getName());
		}
		Robin robin = (Robin) other;
		if(rows != robin.rows) {
			throw new RrdException("Incompatible number of rows: " + rows +
				" != " + robin.rows);
		}
		robin.pointer.set(pointer.get());
		// BULK operation, will speed things up
		robin.values.writeBytes(values.readBytes());
	}

	void filterValues(double minValue, double maxValue) throws IOException {
		for(int i = 0; i < rows; i++) {
			double value = values.get(i);
			if(!Double.isNaN(minValue) && !Double.isNaN(value) && minValue > value) {
				values.set(i, Double.NaN);
			}
			if(!Double.isNaN(maxValue) && !Double.isNaN(value) && maxValue < value) {
				values.set(i, Double.NaN);
			}
		}
	}
}
