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

import java.io.IOException;
import java.util.StringTokenizer;

/**
 * <p>Class to represent data source values for the given timestamp. Objects of this
 * class are never created directly (no public constructor is provided). To learn more how
 * to update RRDs, see RRDTool's
 * <a href="../../../../man/rrdupdate.html" target="man">rrdupdate man page</a>.
 *
 * <p>To update a RRD with JRobin use the following procedure:</p>
 *
 * <ol>
 * <li>Obtain empty Sample object by calling method {@link org.jrobin.core.RrdDb#createSample(long)
 * createSample()} on respective {@link org.jrobin.core.RrdDb RrdDb} object.
 * <li>Adjust Sample timestamp if necessary (see {@link #setTime(long) setTime()} method).
 * <li>Supply data source values (see {@link #setValue(java.lang.String, double) setValue()}).
 * <li>Call Sample's {@link #update() update()} method.
 * </ol>
 *
 * <p>Newly created Sample object contains all data source values set to 'unknown'.
 * You should specifify only 'known' data source values. However, if you want to specify
 * 'unknown' values too, use <code>Double.NaN</code>.</p>
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
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
		values = new double[dsNames.length];
		clearCurrentValues();
	}

	private void clearCurrentValues() {
		for(int i = 0; i < values.length; i++) {
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
		for(int i = 0; i < values.length; i++) {
			if(dsNames[i].equals(dsName)) {
				values[i] = value;
				return;
			}
		}
		throw new RrdException("Datasource " + dsName + " not found");
	}

	/**
	 * Sets single datasource value using data source index. Data sources are indexed by
	 * the order specified during RRD creation (zero-based).
	 * @param i Data source index
	 * @param value Data source values
	 * @throws RrdException Thrown if data source index is invalid.
	 */
	public void setValue(int i, double value) throws RrdException {
		if(i < values.length) {
			values[i] = value;
			return;
		}
		throw new RrdException("Sample datasource index " + i + " out of bounds");
	}

	/**
	 * Sets some (possibly all) data source values in bulk. Data source values are
	 * assigned in the order of their definition inside the RRD.
	 *
	 * @param values Data source values.
	 * @throws RrdException Thrown if the number of supplied values is zero or greater
	 * than the number of data sources defined in the RRD.
	 */
	public void setValues(double[] values) throws RrdException {
		if(values.length <= this.values.length) {
			for(int i = 0; i < values.length; i++) {
				this.values[i] = values[i];
			}
		}
		else {
			throw new RrdException("Invalid number of values specified (found " +
				values.length +	", only " + dsNames.length + " allowed)");
		}
	}

	/**
	 * Returns all current data source values in the sample.
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
	 * <p>Sets sample timestamp and data source values in a fashion similar to RRDTool.
	 * Argument string should be composed in the following way:
	 * <code>timestamp:value1:value2:...:valueN</code>.</p>
	 *
	 * <p>You don't have to supply all datasource values. Unspecified values will be treated
	 * as unknowns. To specify unknown value in the argument string, use letter 'U'
	 *
	 * @param timeAndValues String made by concatenating sample timestamp with corresponding
	 * data source values delmited with colons. For example:<p>
	 * <pre>
	 * 1005234132:12.2:35.6:U:24.5
	 * NOW:12.2:35.6:U:24.5
	 * </pre>
	 * 'N' stands for the current timestamp (can be replaced with 'NOW')<p>
	 * Method will throw an exception if timestamp is invalid (cannot be parsed as Long, and is not 'N'
	 * or 'NOW'). Datasource value which cannot be parsed as 'double' will be silently set to NaN.<p>
	 * @throws RrdException Thrown if too many datasource values are supplied
	 */
	public void set(String timeAndValues) throws RrdException {
		StringTokenizer tokenizer = new StringTokenizer(timeAndValues, ":", false);
		int n = tokenizer.countTokens();
		if(n > values.length + 1) {
			throw new RrdException("Invalid number of values specified (found " +
				values.length +	", " + dsNames.length + " allowed)");
		}
		String timeToken = tokenizer.nextToken();
		try {
			time = Long.parseLong(timeToken);
		}
		catch(NumberFormatException nfe) {
			if(timeToken.equalsIgnoreCase("N") || timeToken.equalsIgnoreCase("NOW")) {
				time = Util.getTime();
			}
			else {
				throw new RrdException("Invalid sample timestamp: " + timeToken);
			}
		}
		for(int i = 0; tokenizer.hasMoreTokens(); i++) {
			try {
				values[i] = Double.parseDouble(tokenizer.nextToken());
			}
			catch (NumberFormatException nfe) {
				// NOP, value is already set to NaN
			}
		}
	}

	/**
	 * Stores sample in the corresponding RRD. If the update operation succeedes,
	 * all datasource values in the sample will be set to Double.NaN (unknown) values.
	 *
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin related error.
	 */
	public void update() throws IOException, RrdException {
		parentDb.store(this);
		clearCurrentValues();
	}

	/**
	 * <p>Creates sample with the timestamp and data source values supplied
	 * in the argument string and stores sample in the corresponding RRD.
	 * This method is just a shortcut for:</p>
	 * <pre>
	 *     set(timeAndValues);
	 *     update();
	 * </pre>
	 * @param timeAndValues String made by concatenating sample timestamp with corresponding
	 * data source values delmited with colons. For example:<br>
	 * <code>1005234132:12.2:35.6:U:24.5</code><br>
	 * <code>NOW:12.2:35.6:U:24.5</code>
	 *
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of JRobin related error.
	 */
	public void setAndUpdate(String timeAndValues) throws IOException, RrdException {
		set(timeAndValues);
		update();
	}

	/**
	 * Dumps sample content using the syntax of RRDTool's update command.
	 * @return Sample dump.
	 */
	public String dump() {
		StringBuffer buffer = new StringBuffer("update \"");
		buffer.append(parentDb.getRrdBackend().getPath() + "\" " + time);
		for(int i = 0; i < values.length; i++) {
			buffer.append(":");
			buffer.append(Util.formatDouble(values[i], "U", false));
		}
		return buffer.toString();
	}

	String getRrdToolCommand() {
		return dump();
	}
}
