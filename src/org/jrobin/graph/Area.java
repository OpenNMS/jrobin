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

import org.jrobin.core.XmlWriter;

import java.awt.Color;

/**
 * <p>Class used to represent a datasource plotted as an area in a graph.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Area extends PlotDef
{
	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Constructs a <code>Area</code> PlotDef object based on a datasource name and a graph color. 
	 * @param sourceName Name of the graph definition <code>Source</code> containing the datapoints.
	 * @param color Color of the resulting area, if no color is specified, the Area will not be drawn.
	 */
	Area( String sourceName, Color color )
	{
		super( sourceName, color );
		this.plotType	= PlotDef.PLOT_AREA;
	}
	
	/**
	 * Constructs a <code>Area</code> object based on a Source containing all necessary datapoints and
	 * a color to draw the resulting graph in.  The last two parameters define if the
	 * Area should be drawn, and if it is stacked onto a previous PlotDef yes or no.
	 * @param source Source containing all datapoints for this Area.
	 * @param color Color of the resulting graphed area.
	 * @param stacked True if this PlotDef is stacked on the previous one, false if not.
	 * @param visible True if this PlotDef should be graphed, false if not.
	 */
	Area( Source source, Color color, boolean stacked, boolean visible )
	{
		super( source, color, stacked, visible );
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================	
	/**
	 * Draws the actual Area on the chart.
	 * @param g ChartGraphics object representing the graphing area.
	 * @param xValues List of relative chart area X positions corresponding to the datapoints.
	 * @param stackValues Datapoint values of previous PlotDefs, used to stack on if necessary.
	 * @param lastPlotType Type of the previous PlotDef, used to determine PlotDef type of a stack.
	 */
	void draw( ChartGraphics g, int[] xValues, int[] stackValues, int lastPlotType )
	{
		g.setColor( color );
		
		double[] values = source.getValues();
		
		int ax = 0, ay = 0, py;
		int nx = 0, ny = 0, last = -1;

		for (int i = 0; i < xValues.length; i++)
		{
			py = 0;
			
			nx = xValues[i];
			ny = g.getY( values[i] );
		
			if ( !Double.isNaN(values[i]) )
			{
				if ( stacked ) {
					py 	= stackValues[i];
					ny += ( stackValues[i] == Integer.MIN_VALUE ? Integer.MIN_VALUE : stackValues[i] );
				}
			
				if ( visible )
				{
					if (nx > ax + 1)	// More than one pixel hop, draw intermediate pixels too
					{
						// For each pixel between nx and ax, calculate the y, plot the line
						int co 	= (ny - ay) / (nx - ax);
						int j 	= (ax > 0 ? ax : 1 );		// Skip 0 
				
						for (j = ax; j <= nx; j++)
							if ( ay != Integer.MIN_VALUE && ny != Integer.MIN_VALUE )
								g.drawLine( j, py, j, ( co * (j - ax) + ay) );
					}
					else if ( nx != 0 && py != Integer.MIN_VALUE && ny != Integer.MIN_VALUE )
						g.drawLine( nx, py, nx, ny );
				}
			
				
			}
			
			// Special case with NaN doubles
			
			stackValues[i] 	= ny;
			ax 				= nx;
			ay 				= ny;
		}
	}
	
	void exportXmlTemplate( XmlWriter xml, String legend )
	{
		xml.startTag("area");
		xml.writeTag("datasource", sourceName);
		xml.writeTag("color", color);
		xml.writeTag("legend", legend);
		xml.closeTag(); // area
	}
}
