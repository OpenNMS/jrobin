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

import jrobin.core.Util;

/**
 * <p>Class used to represent a number of datapoints for a graph.</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
class Source 
{
	static final int AGG_MINIMUM	= 0;
	static final int AGG_MAXIMUM	= 1;
	static final int AGG_AVERAGE	= 2;
	static final int AGG_FIRST		= 3;
	static final int AGG_LAST		= 4;
	
	private String name;
	protected double[] values;
	
	private double min				= Double.MAX_VALUE;
	private double max				= Double.MIN_VALUE;
	private double lastValue 		= Double.NaN;
	private double totalValue		= 0;
	
	private long lastTime			= 0;
	private long totalTime			= 0; 
	
	Source( String name )
	{
		this.name = name;
	}
	
	void set( int pos, long time, double val )
	{
		aggregate( time, val );		
	}
	
	double get( int pos )
	{
		return values[pos];
	}
	
	String getName()
	{
		return name;	
	}
	
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
	
	double getAggregate( int aggType )
	{
		switch ( aggType )
		{
			case AGG_MINIMUM:
				return min;
				
			case AGG_MAXIMUM:
				return max;
				
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
}
