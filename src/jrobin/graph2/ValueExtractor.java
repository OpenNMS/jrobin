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

import jrobin.core.FetchPoint;
import jrobin.core.RrdException;

/**
 * <p>Class used to extract specific time-based values, out of a number of fetched datasources.</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
class ValueExtractor 
{
	private String[] varNames;			// Name of the variable, NOT it's dsName in the file
	private int[][] varIndices;			// Index of a variable in the FetchPoint value table
	private int[][] timePos;
	private FetchPoint[][] values;
	
	ValueExtractor( String[] names, int[][] indices, FetchPoint[][] values )
	{
		this.varNames	= names;
		this.varIndices	= indices;
		this.values		= values;
		
		timePos			= new int[indices.length][];
		for (int i = 0; i < indices.length; i++)
			timePos[i]	= new int[ indices[i].length ];
	}

	// Return the table position offset for the next datasource
	int extract( long timestamp, Source[] sources, int row, int offset ) throws RrdException
	{
		int tblPos = offset;
		
		for ( int i = 0; i < varIndices.length; i++ ) {
			for (int j = 0; j < varIndices[i].length; j++) 
			{
				if ( timestamp < values[i][ timePos[i][j] ].getTime() ) 
					throw new RrdException("Backward reading not allowed");
				
				while( timePos[i][j] < values[i].length - 1 ) 
				{
					if ( values[i][ timePos[i][j] ].getTime() <= timestamp 
							&& timestamp < values[i][ timePos[i][j] + 1 ].getTime() ) {
						sources[tblPos++].set( row, timestamp, values[i][ timePos[i][j] + 1 ].getValue( varIndices[i][j] ) );
						break;			
					}
					else
						timePos[i][j]++;
				}
			}
		}
		
		return tblPos;
	}
	
	String[] getNames() {
		return varNames;
	}
}
