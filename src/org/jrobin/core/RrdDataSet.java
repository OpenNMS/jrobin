/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org)
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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
package org.jrobin.core;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <p>Interface to represent a JRobin dataset.  A dataset is nothing but a table of datasources, indexed
 * by equidistant timestamps.  A dataset allows access to the internal datasources with aggregatoin methods.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public interface RrdDataSet
{
	/**
	 * Returns the number of rows in this dataset.
	 *
	 * @return Number of rows (data samples).
	 */
	public int getRowCount();

    /**
	 * Returns the number of columns in this dataset.
	 *
	 * @return Number of columns (datasources).
	 */
	public int getColumnCount();

	/**
	 * Returns an array of timestamps covering the whole range specified in the
	 * dataset object.
	 *
	 * @return Array of equidistant timestamps.
	 */
	public long[] getTimestamps();

	/**
	 * Returns all values for a single datasource, the returned values
	 * correspond to the timestamps returned with the {@link #getTimestamps() getTimestamps()} method.
	 *
	 * @param dsIndex Datasource index.
	 * @return Array of single datasource values.
	 */
	public double[] getValues( int dsIndex );

	/**
	 * Returns all values for all datasources, the returned values
	 * correspond to the timestamps returned with the {@link #getTimestamps() getTimestamps()} method.
	 *
	 * @return Two-dimensional aray of all datasource values.
	 */
	public double[][] getValues();

	/**
	 * Returns all values for a single datasource, the returned values
	 * correspond to the timestamps returned with the {@link #getTimestamps() getTimestamps()} method.
	 *
	 * @param dsName Datasource name.
	 * @return Array of single datasource values.
	 * @throws RrdException Thrown if no matching datasource name is found.
	 */
	public double[] getValues( String dsName ) throws RrdException;

    /**
	 * Returns the first timestamp in the dataset.
	 *
	 * @return The smallest timestamp.
	 */
	public long getFirstTimestamp();

	/**
	 * Returns the last timestamp in the dataset.
	 *
	 * @return The biggest timestamp.
	 */
	public long getLastTimestamp();

	/**
	 * Returns array of the names of all datasources in the set.
	 *
	 * @return Array of datasource names.
	 */
	public String[] getDsNames();

	/**
	 * Retrieve the table index number of a datasource by name.
	 * Names are case sensitive.
	 *
	 * @param dsName Name of the datasource for which to find the index.
	 * @return Index number of the datasource in the value table.
	 * @throws RrdException Thrown if the given datasource name cannot be found in the dataset.
	 */
	public int getDsIndex( String dsName ) throws RrdException;

	/**
	 * Returns the step of these datasources.
	 *
	 * @return Step as long.
	 */
	public long getStep();

	/**
	 * Returns aggregated value from the dataset for a single datasource.
	 *
	 * @param dsName Datasource name
	 * @param consolFun Consolidation function to be applied to set datasource values datasource.
	 * Valid consolidation functions are MIN, MAX, LAST and AVERAGE (these string constants
	 * are conveniently defined in the {@link ConsolFuns} class)
	 * @return MIN, MAX, LAST or AVERAGE value calculated from the dataset for the given datasource name
	 * @throws RrdException Thrown if the given datasource name cannot be found in the dataset.
	 */
	public double getAggregate( String dsName, String consolFun ) throws RrdException;

	/**
	 * Dumps fetch data to output stream in XML format.
	 *
	 * @param outputStream Output stream to dump dataset to
	 * @throws RrdException Thrown in case of JRobin specific error.
	 * @throws IOException Thrown in case of I/O error
	 */
	public void exportXml( OutputStream outputStream ) throws RrdException, IOException;

	/**
	 * Dumps dataset to file in XML format.
	 *
	 * @param filepath Path to destination file
	 * @throws RrdException Thrown in case of JRobin specific error.
	 * @throws IOException Thrown in case of I/O error
	 */
	public void exportXml( String filepath ) throws RrdException, IOException;

	/**
	 * Dumps the dataset to XML.
	 *
	 * @return XML string format of the dataset.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 * @throws IOException Thrown in case of an I/O related error.
	 */
	public String exportXml() throws RrdException, IOException;
}
