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

import java.io.IOException;

/**
 * Class used to represent data fetched from the RRD file.
 * Object of this class is created when the method
 * {@link jrobin.core.FetchRequest#fetchData() fetchData()} is
 * called on a {@link jrobin.core.FetchRequest FetchRequest} object.<p>
 *
 * Data returned from the RRD file is, simply, just one big table filled with
 * timestamps and corresponding datasource values.
 * Use {@link #getRowCount() getRowCount()} method to count the number
 * of returned timestamps (table rows).<p>
 *
 * The first table column is filled with timestamps. Time intervals
 * between consecutive timestamps are guaranteed to be equal. Use
 * {@link #getTimestamps() getTimestamps()} method to get an array of
 * timestamps returned.<p>
 *
 * Remaining columns are filled with datasource values for the whole timestamp range,
 * on a column-per-datasource basis. Use {@link #getColumnCount() getColumnCount()} to find
 * the number of datasources and {@link #getValues(int) getValues(i)} method to obtain
 * all values for the i-th datasource. Returned datasource values correspond to
 * the values returned with {@link #getTimestamps() getTimestamps()} method.<p>
 */
public class FetchData {
	private FetchRequest request;
	private Archive matchingArchive;
	private String[] dsNames;
	private long[] timestamps;
	private double[][] values;

	FetchData(Archive matchingArchive, FetchRequest request) throws IOException {
		this.matchingArchive = matchingArchive;
		this.dsNames = request.getFilter();
		if(this.dsNames == null) {
			this.dsNames = matchingArchive.getParentDb().getDsNames();
		}
		this.request = request;
	}

	void setTimestamps(long[] timestamps) {
		this.timestamps = timestamps;
	}

	void setValues(double[][] values) {
		this.values = values;
	}

	/**
	 * Returns the number of rows fetched from the underlying RRD file.
	 * Each row represents datasource values for the specific timestamp.
	 * @return Number of rows.
	 */
    public int getRowCount() {
		return timestamps.length;
	}

    /**
	 * Returns the number of columns fetched from the underlying RRD file.
	 * This number is always equal to the number of datasources defined
	 * in the RRD file. Each column represents values of a single datasource.
	 * @return Number of columns (datasources).
	 */
	public int getColumnCount() {
		return dsNames.length;
	}

	/**
	 * Returns the number of rows fetched from the underlying RRD file.
	 * Each row represents datasource values for the specific timestamp.
	 * @param rowIndex Row index.
	 * @return FetchPoint object which represents datasource values for the
	 * specific timestamp.
	 */
	public FetchPoint getRow(int rowIndex) {
		int numCols = getColumnCount();
		FetchPoint point = new FetchPoint(timestamps[rowIndex], getColumnCount());
		for(int dsIndex = 0; dsIndex < numCols; dsIndex++) {
			point.setValue(dsIndex, values[dsIndex][rowIndex]);
		}
		return point;
	}

	/**
	 * Returns an array of timestamps covering the whole range specified in the
	 * {@link FetchRequest FetchReguest} object.
	 * @return Array of equidistant timestamps.
	 */
	public long[] getTimestamps() {
		return timestamps;
	}

	/**
	 * Returns all archived values for a single datasource.
	 * Returned values correspond to timestamps
	 * returned with {@link #getTimestamps() getTimestamps()} method.
	 * @param dsIndex Datasource index.
	 * @return Array of single datasource values.
	 */
	public double[] getValues(int dsIndex) {
		return values[dsIndex];
	}

	/**
	 * Returns all archived values for all datasources.
	 * Returned values correspond to timestamps
	 * returned with {@link #getTimestamps() getTimestamps()} method.
	 * @return Two-dimensional aray of all datasource values.
	 */
	public double[][] getValues() {
		return values;
	}
	
	/**
	 * Returns all archived values for a single datasource.
	 * Returned values correspond to timestamps
	 * returned with {@link #getTimestamps() getTimestamps()} method.
	 * @param dsName Datasource name.
	 * @return Array of single datasource values.
	 * @throws RrdException Thrown if no matching datasource name is found.
	 */
	public double[] getValues(String dsName) throws RrdException {
		for(int dsIndex = 0; dsIndex < getColumnCount(); dsIndex++) {
			if(dsName.equals(dsNames[dsIndex])) {
				return getValues(dsIndex);
			}
		}
		throw new RrdException("Datasource [" + dsName + "] not found");
	}

	/**
	 * Returns {@link FetchRequest FetchRequest} object used to create this FetchData object.
	 * @return Fetch request object.
	 */
	public FetchRequest getRequest() {
		return request;
	}

    /**
	 * Returns the first timestamp in this FetchData object.
	 * @return The smallest timestamp.
	 */
	public long getFirstTimestamp() {
		return timestamps[0];
	}

	/**
	 * Returns the last timestamp in this FecthData object.
	 * @return The biggest timestamp.
	 */
	public long getLastTimestamp() {
		return timestamps[timestamps.length - 1];
	}

	/**
	 * Returns Archive object which is determined to be the best match for the
	 * timestamps specified in the fetch request. All datasource values are obtained
	 * from round robin archives belonging to this archive.
	 * @return Matching archive.
	 */
	public Archive getMatchingArchive() {
		return matchingArchive;
	}

	/**
	 * Returns array of datasource names found in the underlying RRD file. If the request
	 * was filtered (data was fetched only for selected datasources), only datasources selected
	 * for fetching are returned.
	 * @return Array of datasource names.
	 */
	public String[] getDsNames() {
		return dsNames;
	}
	
	/**
	 * Retrieve the table index number of a datasource by name.  Names are case sensitive.
	 * @param dsName Name of the datasource for which to find the index.
	 * @return Index number of the datasources in the value table.
	 */
	public int getDsIndex( String dsName ) {
		// Let's assume the table of dsNames is always small, so it is not necessary to use a hashmap for lookups
		for (int i = 0; i < dsNames.length; i++)
			if ( dsNames[i].equals(dsName) )
				return i;
		
		return -1;		// Datasource not found !
	}

	/**
	 * Dumps the content of the whole FetchData object to stdout. Useful for debugging.
	 */
	public void dump() {
		for(int i = 0; i < getRowCount(); i++) {
			System.out.println(getRow(i).dump());
		}
	}

	/**
	 * Returns aggregated value from the fetched data for a single datasource.
	 * @param dsName Datasource name
	 * @param consolFun Consolidation function to be applied to fetched datasource values.
	 * Valid consolidation functions are MIN, MAX, LAST and AVERAGE
	 * @return MIN, MAX, LAST or AVERAGE value calculated from the fetched data
	 * for the given datasource name
	 * @throws RrdException Thrown if the given datasource name cannot be found in fetched data.
	 */
	public double getAggregate(String dsName, String consolFun) throws RrdException {
		return getAggregate(dsName, consolFun, null);
	}

	/**
	 * Returns aggregated value from the fetched data for a single datasource.
	 * Before applying aggrregation functions, specified RPN expression is applied to fetched
	 * data. For example, if you have a gauge datasource named 'foots' but you wont to
	 * find the maximum fetched value in meters use something like:</p>
	 * <code>getAggregate("foots", "MAX", "value,0.3048,*");</code>
	 * Note that 'value' in the RPN expression is a reserved word and stands for the
	 * original value (value fetched from RRD file)</p>
	 * @param dsName Datasource name
	 * @param consolFun Consolidation function to be applied to fetched datasource values.
	 * Valid consolidation functions are MIN, MAX, LAST and AVERAGE
	 * @return MIN, MAX, LAST or AVERAGE value calculated from the fetched data
	 * for the given datasource name
	 * @throws RrdException Thrown if the given datasource name cannot be found in fetched data.
	 */
	public double getAggregate(String dsName, String consolFun, String rpnExpression)
		throws RrdException {
		if(consolFun.equals("MAX")) {
			return getMax(dsName, rpnExpression);
		}
		else if(consolFun.equals("MIN")) {
			return getMin(dsName, rpnExpression);
		}
		else if(consolFun.equals("LAST")) {
			return getLast(dsName, rpnExpression);
		}
		else if(consolFun.equals("AVERAGE")) {
			return getAverage(dsName, rpnExpression);
		}
		else {
			throw new RrdException("Unsupported consolidation function [" + consolFun + "]");
		}
	}

	private double getMax(String dsName, String rpnExpression) throws RrdException {
		RpnCalculator rpnCalculator = null;
		if(rpnExpression != null) {
			rpnCalculator = new RpnCalculator(rpnExpression);
		}
		double vals[] = getValues(dsName), max = Double.NaN;
		for(int i = 0; i < vals.length - 1; i++) {
			double value = vals[i + 1];
			if(rpnCalculator != null) {
				rpnCalculator.setValue(value);
				value = rpnCalculator.calculate();
			}
			max = Util.max(max, value);
		}
		return max;
	}

	private double getMin(String dsName, String rpnExpression) throws RrdException {
		RpnCalculator rpnCalculator = null;
		if(rpnExpression != null) {
			rpnCalculator = new RpnCalculator(rpnExpression);
		}
		double vals[] = getValues(dsName), min = Double.NaN;
		for(int i = 0; i < vals.length - 1; i++) {
			double value = vals[i + 1];
			if(rpnCalculator != null) {
				rpnCalculator.setValue(value);
				value = rpnCalculator.calculate();
			}
			min = Util.min(min, value);
		}
		return min;
	}

	private double getLast(String dsName, String rpnExpression) throws RrdException {
		RpnCalculator rpnCalculator = null;
		if(rpnExpression != null) {
			rpnCalculator = new RpnCalculator(rpnExpression);
		}
		double vals[] = getValues(dsName);
		double value = vals[vals.length - 1];
		if(rpnCalculator != null) {
			rpnCalculator.setValue(value);
			value = rpnCalculator.calculate();
		}
		return value;
	}

	private double getAverage(String dsName, String rpnExpression) throws RrdException {
		RpnCalculator rpnCalculator = null;
		if(rpnExpression != null) {
			rpnCalculator = new RpnCalculator(rpnExpression);
		}
		double vals[] = getValues(dsName);
		double totalVal = 0;
		long totalSecs = 0;
		for(int i = 0; i < vals.length - 1; i++) {
			long t1 = Math.max(request.getFetchStart(), timestamps[i]);
			long t2 = Math.min(request.getFetchEnd(), timestamps[i + 1]);
			double value = vals[i + 1];
			if(rpnCalculator != null) {
				rpnCalculator.setValue(value);
				value = rpnCalculator.calculate();
			}
			if(!Double.isNaN(value)) {
                totalSecs += (t2 - t1);
				totalVal += (t2 - t1) * value;
			}
		}
		return totalSecs > 0? totalVal / totalSecs: Double.NaN;
	}

}
