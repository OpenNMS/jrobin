package org.jrobin.graph;

import java.util.HashMap;

import org.jrobin.core.RrdException;
import org.jrobin.core.XmlWriter;

/**
 * <p>Represents a 'static' datasource for a graph.  A static datasource is a single value (constant),
 * but can only be the result of applying a consolidation function (AVG, MIN, MAX, LAST, FIRST or TOTAL)
 * to one of the other, already defined, datasources.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Sdef extends Cdef
{
	// ================================================================
	// -- Members
	// ================================================================
	private int defIndex		= -1;

	private String defName		= "";
	private String consolFunc	= "AVERAGE";
	private int aggregate		= Source.AGG_AVERAGE;

	private boolean calculated	= false;
	private double[] value		= null;


	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a new Sdef object based on an existing datasource (Def, Cdef or Pdef)
	 * and the consolidation function to apply to that datasource.
	 *
	 * @param name Name of the datasource in the graph definition.
	 * @param defName Name of the datasource this Sdef is derived from.
	 * @param consolFunc Consolidation function to apply to the referring datasource (defName).
	 * @throws RrdException Thrown in case of invalid consolidation function specified.
	 */
	Sdef( String name, String defName, String consolFunc ) throws RrdException
	{
		super( name );

		this.defName	= defName;
		this.consolFunc	= consolFunc;

		// -- Parse the consolidation function to be used
		if ( consolFunc.equalsIgnoreCase("AVERAGE") || consolFunc.equalsIgnoreCase("AVG") )
			aggregate = Source.AGG_AVERAGE;
		else if ( consolFunc.equalsIgnoreCase("MAX") || consolFunc.equalsIgnoreCase("MAXIMUM") )
			aggregate = Source.AGG_MAXIMUM;
		else if ( consolFunc.equalsIgnoreCase("MIN") || consolFunc.equalsIgnoreCase("MINIMUM") )
			aggregate = Source.AGG_MINIMUM;
		else if ( consolFunc.equalsIgnoreCase("LAST") )
			aggregate = Source.AGG_LAST;
		else if ( consolFunc.equalsIgnoreCase("FIRST") )
			aggregate = Source.AGG_FIRST;
		else if ( consolFunc.equalsIgnoreCase("TOTAL") )
			aggregate = Source.AGG_TOTAL;
		else
			throw new RrdException( "Invalid consolidation function specified." );
	}

	// ================================================================
	// -- Protected methods
	// ================================================================
	/**
	 * Prepares the Sdef for faster value calculation by setting the internal
	 * array and references.  Override from Cdef parent class.
	 *
	 * @param sourceIndex Lookup table holding the name - index pairs for all datasources.
	 * @param numPoints Number of points used as graph resolution (size of the value table).
	 * @throws RrdException Thrown in case of the requested datasource is not available in the sourceIndex
	 */
	void prepare( HashMap sourceIndex, int numPoints, int aggregatePoints ) throws RrdException
	{
		if ( sourceIndex.containsKey( defName ) )
			defIndex = ( (Integer) sourceIndex.get( defName ) ).intValue();
		else
			throw new RrdException( "Datasource not found: " + defName );

		values 					= new double[ numPoints ];
		this.aggregatePoints	= aggregatePoints;
	}

	/**
	 * Returns the level this Sdef would have in the calculation tree.  The level defines when
	 * the Sdef can be calculated.  The level of the Sdef will always be one higher than that of
	 * its referring datasource, since it can only be calculated after that datasource has already
	 * been calculated.
	 *
	 * @param levels Array containing the previously calculated calculation levels.
	 * @return Level of this Sdef in the calculation tree.
	 */
	int calculateLevel( int[] levels )
	{
		// An Sdef is always one lower in the tree than its source
		return levels[defIndex] + 1;
	}

	/**
	 * Gets the value of the datapoint at a particular position in the datapoint array.
	 * In case of an Sdef, the value will be the same for every different position.
	 *
	 * @param pos Inherited from Cdef
	 * @return The consolidated value of the referring datasource as double
	 */
	double get( int pos )
	{
		return values[0];
	}

	/**
	 * Calculates the internal value of this Sdef.
	 *
	 * @param sources Array of the calculated datasources.
	 */
	void set( Source[] sources )
	{
		if ( calculated ) return;

		double value = sources[ defIndex ].getAggregate( aggregate );
		for ( int i = 0; i < values.length; i++ )
			values[i] = value;

		calculated = true;
	}

	/**
	 * Override from the corresponding method in the Source parent class,
	 * overridden for faster implementation.
	 *
	 * Five of the possible aggregation methods return the actual value
	 * of the Sdef:
	 * <code>AGG_MINIMUM, AGG_MAXIMUM, AGG_AVERAGE, AGG_FIRST and AGG_LAST</code>
	 *
	 * Only <code>AGG_TOTAL</code> will return value summed over the number of
	 * samples in the graph.
	 *
	 * @param aggType Type of the aggregate requested.
	 * @return The double value of the requested aggregate.
	 */
	double getAggregate( int aggType )
	{
		switch ( aggType )
		{
			case AGG_MINIMUM:
			case AGG_MAXIMUM:
			case AGG_AVERAGE:
			case AGG_FIRST:
			case AGG_LAST:
				return values[0];

			case AGG_TOTAL:
				return (values[0] * values.length) ;
		}

		return Double.NaN;
	}


	void exportXml(XmlWriter xml) {
		xml.startTag( "def" );
		xml.writeTag( "name", getName() );
		xml.writeTag( "datasource", defName );
		xml.writeTag( "cf", consolFunc );
		xml.closeTag(); // def
	}
}
