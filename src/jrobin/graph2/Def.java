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

/**
 * <p>Represents a fetched datasource for a graph.  A Def collects all his datavalues from an existing
 * RRD file.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Def extends Source
{
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a new Def object holding a number of fetched datapoints for a graph.
	 * @param name Name of the datasource in the graph definition.
	 * @param numPoints Number of points used as graph resolution (size of the value table).
	 */
	Def( String name, int numPoints )
	{
		super(name);
		values = new double[ numPoints ];
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================	
	/**
	 * Sets the value of a specific datapoint for this Def.
	 * @param pos Position (index in the value table) of the new datapoint.
	 * @param time Timestamp of the new datapoint in number of seconds.
	 * @param val Double value of the new datapoint.
	 */
	void set( int pos, long timestamp, double val )
	{
		super.set( pos, timestamp, val );
		values[pos] = val;
	}

}
