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

import java.awt.*;
import java.util.HashMap;

import org.jrobin.core.RrdException;
import org.jrobin.core.XmlWriter;

/**
 * <p>Class used to represent a drawn datasource in the graph.
 * This class is abstract, it can only be used by child classes with a specific
 * implementation of the <code>draw()</code> method.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
abstract class PlotDef
{
	// ================================================================
	// -- Members
	// ================================================================
	protected static final int PLOT_LINE 		= 0;
	protected static final int PLOT_AREA 		= 1;
	protected static final int PLOT_STACK		= 2;
	protected static final BasicStroke STROKE	= new BasicStroke();

	protected boolean visible 					= true;
	protected boolean stacked					= false;
	protected int plotType						= PLOT_LINE;	// Unknown plotdef is a line
		
	protected String sourceName					= "";
	protected Source source						= null;
	protected Color color						= Color.BLACK;	// Default color is black

	protected double[] values					= null;

	// ================================================================
	// -- Constructors
	// ================================================================
	PlotDef() {
	}
	
	/**
	 * Constructs a default <code>PlotDef</code> object based on a datasource name and a graph color. 
	 * @param sourceName Name of the graph definition <code>Source</code> containing the datapoints.
	 * @param color Color of the resulting graph for this PlotDef, if no color is specified, the PlotDef will not be drawn.
	 */
	PlotDef( String sourceName, Color color )
	{
		this.sourceName = sourceName;
		this.color		= color;
		
		// If no color is given, we should not plot this source
		if ( color == null ) 
			visible = false;	
	}
	
	/**
	 * Constructs a default <code>PlotDef</code> object based on a Source containing all necessary datapoints and
	 * a color to draw the resulting graph in.  The last two parameters define if this
	 * PlotDef should be drawn, and if it is a stack yes or no.
	 * @param source Source containing all datapoints for this PlotDef.
	 * @param color Color of the resulting graph for this PlotDef.
	 * @param stacked True if this PlotDef is stacked on the previous one, false if not.
	 * @param visible True if this PlotDef should be graphed, false if not.
	 */
	PlotDef( Source source, double[] values, Color color, boolean stacked, boolean visible )
	{
		this.source		= source;
		this.values		= values;
		this.color		= color;
		this.stacked	= stacked;
		this.visible	= visible;
	}

	// ================================================================
	// -- Protected methods
	// ================================================================	
	/**
	 * Sets the Source for this PlotDef, based on the internal datasource name.
	 * @param sources Source table containing all datasources necessary to create the final graph.
	 * @param sourceIndex HashMap containing the sourcename - index keypairs, to retrieve the index 
	 * in the Source table based on the sourcename.
	 */
	void setSource( Source[] sources, HashMap sourceIndex ) throws RrdException
	{
		if ( sourceIndex.containsKey(sourceName) ) {
			source = sources[ ((Integer) sourceIndex.get(sourceName)).intValue() ];
		}
		else
			throw new RrdException( "Invalid DEF or CDEF: " + sourceName );
	}

	void prepareValues( int arraySize )
	{
		values = new double[ arraySize ];
	}

	void setValue( int tableRow, long preciseTime, long[] reducedTimestamps )
	{
		values[ tableRow ] = source.get( preciseTime, reducedTimestamps );
	}

	/**
	 * Retrieves the value for a specific datapoint of the PlotDef.
	 * @param tblPos Table index of the datapoint to be retrieved.
	 * @param timestamps Table containing the timestamps corresponding to all datapoints.
	 * @return Double value of the datapoint.
	 */
	double getValue( int tblPos, long[] timestamps )
	{
		return source.values[tblPos];	
	}
	
	/**
	 * Abstract draw method, must be implemented in all child classes.
	 * This method is responsible for the actual drawing of the PlotDef.
	 */
	abstract void draw( ChartGraphics g, int[] xValues, double[] stackValues, int lastPlotType ) throws RrdException;
	
	Source getSource() {
		return source;
	}
	
	String getSourceName() {
		return sourceName;
	}
	
	int getType() {
		return plotType;
	}
	
	Color getColor() {
		return color;
	}

	void exportXmlTemplate(XmlWriter xml, String legend) {

	}
}
