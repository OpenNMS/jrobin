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
 * <p>description</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
class Stack extends PlotDef
{
	Stack( String sourceName, Color c )
	{
		super( sourceName, c );
		this.plotType	= PlotDef.PLOT_STACK;
	}

	void draw( ChartGraphics g, int[] xValues, int[] stackValues, int lastPlotType ) throws RrdException
	{
		PlotDef stack = null;
		
		try
		{
			if ( lastPlotType == PlotDef.PLOT_LINE )
				stack = new PlotDef( source, color, true );	
			else if ( lastPlotType == PlotDef.PLOT_AREA )
				stack = new Area( source, color, true );
	
			stack.draw( g, xValues, stackValues, lastPlotType );
		}
		catch (Exception e) 
		{
			throw new RrdException( "Could not stack source: " + sourceName );
		}
	
	}
}
