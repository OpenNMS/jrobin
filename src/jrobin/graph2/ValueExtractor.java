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
package jrobin.graph2;

import jrobin.core.FetchData;
import jrobin.core.RrdException;

/**
 * <p>Class used to extract specific time-based values out of a number of fetched datasources.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class ValueExtractor 
{
	// ================================================================
	// -- Members
	// ================================================================	
	private String[] varNames;			// Name of the variable, NOT it's dsName in the file

	private int[] tPos;
	private long[][] timestamps;
	private double[][][] dsValues;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Constructs a ValueExtractor object used to extract fetched datapoints for specific points in time.
	 * @param names Array containing the datasource names in the graph definition.
	 * @param values Array of FetchData objects holding all fetched datasources for a specific RRD file.
	 */
	ValueExtractor( String[] names, FetchData[] values )
	{
		this.varNames	= names;
		
		// Set timestamps
		tPos			= new int[values.length];
		timestamps 		= new long[values.length][];
		dsValues		= new double[values.length][][];
		
		for (int i = 0; i < timestamps.length; i++) {
			if ( values[i] != null ) {
				timestamps[i] 	= values[i].getTimestamps();
				dsValues[i]		= values[i].getValues();
			}
		}
	}


	// ================================================================
	// -- Protected methods
	// ================================================================	
	/**
	 * Extracts a number of values out of the fetched values, and approximates them
	 * to a specific timestamp, to store them in the complete Source array for the graph definition.
	 * @param timestamp Timestamp to which a fetched value should be approximated.
	 * @param sources Array containing all datasources.
	 * @param row Row index in the Source table where the values should stored.
	 * @param offset Offset in the Source table of where to start storing the values.
	 * @return Table position offset for the next datasource.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	int extract( long timestamp, Source[] sources, int row, int offset ) throws RrdException
	{
		int tblPos 	= offset;
			
		for ( int i = 0; i < dsValues.length; i++ ) 
		{
			if ( dsValues[i] == null )
				continue;
			
			int tIndex	= tPos[i];
			
			if ( timestamp < timestamps[i][ tIndex ] )
				throw new RrdException("Backward reading not allowed");
			
			while ( tIndex < timestamps[i].length - 1 )
			{
				if ( timestamps[i][ tIndex ] <= timestamp && timestamp < timestamps[i][ tIndex + 1] ) {
					for (int j = 0; j < dsValues[i].length; j++)
						sources[tblPos++].set( row, timestamp, dsValues[i][j][ tIndex + 1 ] );
						break;				
				}
				else {
					tIndex++;
				}
			}
			
			tPos[i] = tIndex;
		}
		
		return tblPos;
	}
	
	String[] getNames() {
		return varNames;
	}
}
