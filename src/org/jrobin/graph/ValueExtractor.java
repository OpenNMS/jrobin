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
package org.jrobin.graph;

import org.jrobin.core.FetchData;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;

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

	private int reduceFactor = 1;

	private int[] tPos;
	private long[] steps;
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
	ValueExtractor( String[] names, FetchData[] values, int reduceFactor )
	{
		this.varNames		= names;
		this.reduceFactor	= reduceFactor;

		// Set timestamps
		tPos				= new int[values.length];
		steps				= new long[values.length];
		timestamps 			= new long[values.length][];

		dsValues			= new double[values.length][][];

		for (int i = 0; i < timestamps.length; i++)
		{
			if ( values[i] != null )
			{
				timestamps[i] 	= values[i].getTimestamps();
				dsValues[i]		= values[i].getValues();

				if ( timestamps[i].length >= 2 )
					steps[i] = (timestamps[i][1] - timestamps[i][0]);
			}
		}
	}


	// ================================================================
	// -- Protected methods
	// ================================================================
	int prepareSources( Source[] sources, int offset )
	{
		int tblPos 	= offset;

		for ( int i = 0; i < dsValues.length; i++ )
		{
			if ( dsValues[i] != null )
			{
				for (int x = 0; x < dsValues[i].length; x++)
				{
					sources[tblPos++].setFetchedStep( steps[i] );
				}
			}
		}

		return tblPos;
	}

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

			int tIndex		= tPos[i];

			double[] nValue	= new double[ dsValues[i].length ];
			int[] vValue 	= new int[ nValue.length ];				// Counts the actual valid values

			for ( int j = 0; j < nValue.length; j++ )
				nValue[j] = Double.NaN;

			// Combine the rows
			int j;

			for ( j = 0; j < reduceFactor && timestamps[i][tIndex] <= timestamp; j++ )
			{
				for (int x = 0; x < dsValues[i].length; x++)
				{
					if ( Double.isNaN(dsValues[i][x][tIndex]) )
						continue;

					vValue[x]++;

					if ( Double.isNaN(nValue[x]) )
						nValue[x] = dsValues[i][x][tIndex];
					else
					{
						switch ( i )
						{
							case FetchSource.AVG:
								nValue[x] += dsValues[i][x][tIndex];
								break;
							case FetchSource.MAX:
								nValue[x] = Util.max( nValue[x], dsValues[i][x][tIndex] );
								break;
							case FetchSource.MIN:
								nValue[x] = Util.min( nValue[x], dsValues[i][x][tIndex] );
								break;
							case FetchSource.LAST:
								nValue[x] = dsValues[i][x][tIndex];
								break;
						}
					}
				}

				tIndex++;
			}

			// See if we are using a stretched timespan
			if ( j == 0 && row > 0 )
			{
				sources[tblPos++].set( row, timestamp, Double.POSITIVE_INFINITY );
			}
			else
			{
				// Finalize
				for (int x = 0; x < dsValues[i].length; x++)
				{
					if ( i == FetchSource.AVG )
						nValue[x] /= vValue[x];

					sources[tblPos++].set( row, timestamp, nValue[x] );
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
