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

package jrobin.core;

import java.io.IOException;

/**
 * <p>Class to represent data source values for the given timestamp. Objects of this
 * class are never created directly (no public constructor is provided). To learn more how
 * to update a RRD file, see RRDTool's
 * <a href="../../../man/rrdupdate.html" target="man">rrdupdate man page</a>.
 *
 * <p>To update a RRD file with JRobin use the following procedure:</p>
 *
 * <ol>
 * <li>Obtain empty Sample object by calling method {@link jrobin.core.RrdDb#createSample(long)
 * createSample()} on respective {@link jrobin.core.RrdDb RrdDb} object.
 * <li>Adjust Sample timestamp if necessary (see {@link #setTime(long) setTime()} method).
 * <li>Supply data source values (see {@link #setValue(java.lang.String, double) setValue()}).
 * <li>Call Sample's {@link #update() update()} method.
 * </ol>
 *
 * <p>Newly created Sample object contains all data source values set to 'unknown'.
 * You should specifify only 'known' data source values. However, if you want to specify
 * 'unknown' values too, use <code>Double.NaN</code>.</p>
 *
 * @author <a href="mailto:saxon@eunet.yu">Sasa Markovic</a>
 */
public class Sample {
	private RrdDb parentDb;
	private long time;
	private String[] dsNames;
	private double[] values;

	Sample(RrdDb parentDb, long time) throws IOException {
		this.parentDb = parentDb;
		this.time = time;
		this.dsNames = parentDb.getDsNames();
		int n = dsNames.length;
		values = new double[n];
		for(int i = 0; i < n; i++) {
			values[i] = Double.NaN;
		}
	}

	/**
	 * Sets single data source value in the sample.
	 * @param dsName Data source name.
	 * @param value Data source value.
	 * @throws RrdException Thrown if invalid data source name is supplied.
	 */
	public void setValue(String dsName, double value) throws RrdException {
		for(int i = 0; i < dsNames.length; i++) {
			if(dsNames[i].equals(dsName)) {
				values[i] = value;
				return;
			}
		}
		throw new RrdException("Datasource " + dsName + " not found");
	}

	/**
	 * Sets single datasource value using data source index. Data sources are indexed using
	 * the order of RRD file creation (zero-based).
	 * @param i Data source index
	 * @param value Data source values
	 * @throws RrdException Thrown if data source index is invalid.
	 */
	public void setValue(int i, double value) throws RrdException {
		if(i < dsNames.length) {
			values[i] = value;
			return;
		}
		throw new RrdException("Sample index " + i + " out of bounds");
	}

	/**
	 * Sets all data source values. You must supply values for all data sources defined in
	 * a RRD file.
	 * @param values Data source values.
	 * @throws RrdException Thrown if number of supplied values is different from number of
	 * data sources defined in a RRD file.
	 */
	public void setValues(double[] values) throws RrdException {
		if(values.length == dsNames.length) {
			this.values = values;
			return;
		}
		throw new RrdException("Invalid number of values specified (found " + values.length +
			", exactly " + dsNames.length + " needed");
	}

	/**
	 * Returns all data source values in the sample.
	 * @return Data source values.
	 */
	public double[] getValues() {
		return values;
	}

	/**
	 * Returns sample timestamp (in seconds, without milliseconds).
	 * @return Sample timestamp.
	 */
	public long getTime() {
		return time;
	}

	/**
	 * Sets sample timestamp. Timestamp should be defined in seconds (without milliseconds).
	 * @param time New sample timestamp.
	 */
	public void setTime(long time) {
		this.time = time;
	}

	/**
	 * Returns an array of all data source names. If you try to set value for the data source
	 * name not in this array, an exception is thrown.
	 * @return Acceptable data source names.
	 */
	public String[] getDsNames() {
		return dsNames;
	}

	/**
	 * Stores sample in the corresponding RRD file.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin related error.
	 */
	public void update() throws IOException, RrdException {
		parentDb.store(this);
	}

	/**
	 * Dumps sample content using the syntax of RRDTool's update command.
	 * @return Sample dump.
	 */
	public String dump() {
		StringBuffer buffer = new StringBuffer(RrdDb.RRDTOOL);
		buffer.append(" update " + parentDb.getRrdFile().getFilePath() + " " + time);
		for(int i = 0; i < values.length; i++) {
			buffer.append(":");
			buffer.append(Util.formatDouble(values[i], true));
		}
		return buffer.toString();
	}

	String getRrdToolCommand() {
		return dump();
	}
}
