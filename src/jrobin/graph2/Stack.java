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

import java.awt.Color;

import jrobin.core.RrdException;

/**
 * <p>Class used to represent a stacked datasource plotted in a graph.  The datasource
 * will be drawn as a line or an area, depending on PlotDef on which it is stacked.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Stack extends PlotDef
{
	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Constructs a <code>Stack</code> PlotDef object based on a datasource name and a graph color. 
	 * @param sourceName Name of the graph definition <code>Source</code> containing the datapoints.
	 * @param color Color of the resulting area or line, if no color is specified, the PlotDef will not be drawn.
	 */
	Stack( String sourceName, Color color )
	{
		super( sourceName, color );
		this.plotType	= PlotDef.PLOT_STACK;
	}


	// ================================================================
	// -- Protected methods
	// ================================================================	
	/**
	 * Draws the actual PlotDef on the chart, depending on the type of the previous PlotDef, 
	 * the Stack will be drawn as a <code>Line</code> or an <code>Area</code>.
	 * @param g ChartGraphics object representing the graphing area.
	 * @param xValues List of relative chart area X positions corresponding to the datapoints.
	 * @param stackValues Datapoint values of previous PlotDefs, used to stack on if necessary.
	 * @param lastPlotType Type of the previous PlotDef, used to determine PlotDef type of a stack.
	 */
	void draw( ChartGraphics g, int[] xValues, int[] stackValues, int lastPlotType ) throws RrdException
	{
		PlotDef stack = null;
		
		try
		{
			if ( lastPlotType == PlotDef.PLOT_LINE )
				stack = new Line( source, color, true, visible );	
			else if ( lastPlotType == PlotDef.PLOT_AREA )
				stack = new Area( source, color, true, visible );
	
			stack.draw( g, xValues, stackValues, lastPlotType );
		}
		catch (Exception e) 
		{
			throw new RrdException( "Could not stack source: " + sourceName );
		}
	
	}
}
