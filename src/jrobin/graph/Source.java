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
package jrobin.graph;

import jrobin.core.Util;

/**
 * <p>Class used to represent a number of datapoints for a graph.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Source 
{
	// ================================================================
	// -- Members
	// ================================================================
	protected static final int AGG_MINIMUM	= 0;
	protected static final int AGG_MAXIMUM	= 1;
	protected static final int AGG_AVERAGE	= 2;
	protected static final int AGG_FIRST	= 3;
	protected static final int AGG_LAST		= 4;
	
	private String name;
	protected double[] values;
	
	private double min						= Double.MAX_VALUE;
	private double max						= Double.MIN_VALUE;
	private double lastValue 				= Double.NaN;
	private double totalValue				= 0;
	
	private long lastTime					= 0;
	private long totalTime					= 0; 
	
	
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a new Source object holding a number of datapoints for a graph.
	 * @param name Name of the datasource in the graph definition.
	 */
	Source( String name )
	{
		this.name = name;
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================
	/**
	 * Stub method, sets the value of a specific datapoint.
	 * Forces this point to be used in aggregate function calculating.
	 * @param pos Position (index in the value table) of the new datapoint.
	 * @param time Timestamp of the new datapoint in number of seconds.
	 * @param val Double value of the new datapoint.
	 */
	void set( int pos, long time, double val )
	{
		aggregate( time, val );		
	}
	
	/**
	 * Get the double value of a datapoint.
	 * @param pos Index in the value table of the datapoint.
	 * @return The double value of the requested datapoint.
	 */
	double get( int pos ) 
	{
		return values[pos];
	}
	
	/**
	 * Gets a specific aggregate of this datasource.
	 * Requested aggregate can be one of the following:
	 * <code>AGG_MINIMUM, AGG_MAXIMUM, AGG_AVERAGE, AGG_FIRST</code>
	 * and <code>AGG_LAST</code>.
	 * @param aggType Type of the aggregate requested.
	 * @return The double value of the requested aggregate.
	 */
	double getAggregate( int aggType )
	{
		switch ( aggType )
		{
			case AGG_MINIMUM:
				return ( min != Double.MIN_VALUE && min != Double.MAX_VALUE ? min : Double.NaN );
				
			case AGG_MAXIMUM:
				return ( max != Double.MIN_VALUE && max != Double.MAX_VALUE ? max : Double.NaN );
				
			case AGG_AVERAGE:
				if ( totalTime > 0 )
					return totalValue / totalTime;
				break;
				
			case AGG_FIRST:
				if ( values != null && values.length > 0)
					return values[0];
				break;
				
			case AGG_LAST:
				if ( values != null && values.length > 0)
					return values[values.length - 1];
				break;	
		}
		
		return Double.NaN;
	}
	
	String getName() {
		return name;	
	}
	
	double[] getValues() {
		return values;
	}
	
	
	// ================================================================
	// -- Private methods
	// ================================================================
	/**
	 * Adds a datapoint to the aggregate function calculation.
	 * @param time Timestamp in seconds of the datapoint.
	 * @param value Double value of the datapoint.
	 */
	private void aggregate( long time, double value ) 
	{
		min = Util.min( min, value );
		max = Util.max( max, value );
		
		if ( !Double.isNaN(lastValue) && !Double.isNaN(value) )
		{
			long timeDelta 	= time - lastTime;
			totalValue		+= timeDelta * ( value + lastValue ) / 2.0;
			totalTime		+= timeDelta;
		}
		
		lastTime	= time;
		lastValue	= value;
	}
}
