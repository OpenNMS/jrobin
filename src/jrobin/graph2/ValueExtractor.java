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
 * <p>Class used to extract specific time-based values, out of a number of fetched datasources.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class ValueExtractor 
{
	private String[] varNames;			// Name of the variable, NOT it's dsName in the file

	private int[] tPos;
	private long[][] timestamps;
	private double[][][] dsValues;
	
	protected ValueExtractor( String[] names, FetchData[] values )
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

	// Return the table position offset for the next datasource
	protected int extract( long timestamp, Source[] sources, int row, int offset ) throws RrdException
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
	
	protected String[] getNames() {
		return varNames;
	}
}
