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

/**
 * <p>Plottable Def, reprents a custom datasource that can be graphed by JRobin.
 * All the Pdef needs, is a reference to a Plottable class, and it will get the datapoint values (based
 * on timestamps) from that external class.  Any class extending the public Plottable class will do,
 * meaning that the class could get its values from ANY source... like a RDBMS for example.
 * </p>
 * 
 * @author Arne Vandamme <cobralord@cherrymon.org>
 */
class Pdef extends Source
{
	// ================================================================
	// -- Members
	// ================================================================
	private Plottable plottable;
	
	private int index 				= 0;
	private String sourceName		= null;
	private boolean indexed 		= false;
	private boolean named			= false;


	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a new Plottable Def: a custom external datasource 
	 * (represented as a Plottable class) that can be graphed by JRobin.
	 * @param name Name of the datasource in the graph definition.
	 * @param plottable Reference to the class extending Plottable and providing the datapoints.
	 */
	Pdef( String name, Plottable plottable ) 
	{
		super(name );
		this.plottable = plottable;
	}
	
	/**
	 * Constructs a new Plottable Def: a custom external datasource 
	 * (represented as a Plottable class) that can be graphed by JRobin.
	 * @param name Name of the datasource in the graph definition.
	 * @param plottable Reference to the class extending Plottable and providing the datapoints.
	 * @param index Integer number used for referring to the series of datapoints to use in the Plottable class.
	 */
	Pdef( String name, Plottable plottable, int index ) 
	{
		super(name );
		this.plottable 	= plottable;
		this.index		= index;
		indexed			= true;
	}
	
	/**
	 * Constructs a new Plottable Def: a custom external datasource 
	 * (represented as a Plottable class) that can be graphed by JRobin.
	 * @param name Name of the datasource in the graph definition.
	 * @param plottable Reference to the class extending Plottable and providing the datapoints.
	 * @param sourceName String used for referring to the series of datapoints to use in the Plottable class.
	 */
	Pdef( String name, Plottable plottable, String sourceName) 
	{
		super(name );
		this.plottable 	= plottable;
		this.sourceName	= sourceName;
		named			= true;
	}


	// ================================================================
	// -- Protected methods
	// ================================================================
	/**
	 * Prepares the array that will hold the values.
	 * @param numPoints Number of datapoints that will be used.
	 */
	void prepare( int numPoints, int aggregatePoints )
	{
		// Create values table of correct size
		values 					= new double[numPoints];

		// Set the number of points that should be used for aggregate calculation
		this.aggregatePoints	= aggregatePoints;
	}
	
	/**
	 * Sets the value of a specific datapoint for this Pdef.  The Pdef gets the datapoint by retrieving
	 * the value from the Plottable interface using an appropriate getValue() method.
	 * @param pos Position (index in the value table) of the new datapoint.
	 * @param timestamp Timestamp of the new datapoint in number of seconds.
	 */
	void set( int pos, long timestamp )
	{
		double val = Double.NaN;
		
		if ( indexed )
			val = plottable.getValue( timestamp, index );
		else if ( named )
			val = plottable.getValue( timestamp, sourceName );
		else
			val = plottable.getValue( timestamp );
		
		super.set( pos, timestamp, val );
		
		values[pos] = val;
	}
}
