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

/**
 * <p>description</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
class Area extends PlotDef
{
	Area( String sourceName, Color c )
	{
		super( sourceName, c );
		this.plotType	= PlotDef.PLOT_AREA;
	}
	
	Area( Source source, Color c, boolean stacked )
	{
		super( source, c, stacked );
	}
	
	void draw( ChartGraphics g, int[] xValues, int[] stackValues, int lastPlotType )
	{
		g.setColor( color );
		
		int ax = 0, ay = 0, py;
		int nx = 0, ny = 0, last = -1;

		for (int i = 0; i < xValues.length; i++)
		{
			py = 0;
			
			nx = xValues[i];
			ny = g.getY( source.values[i] );
		
			if ( stacked ) {
				py 	= stackValues[i];
				ny += stackValues[i];
			}
		
			if (nx > ax + 1)	// More than one pixel hop, draw intermediate pixels too
			{
				// For each pixel between nx and ax, calculate the y, plot the line
				int co 	= (ny - ay) / (nx - ax);
				int j 	= (ax > 0 ? ax : 1 );		// Skip 0 
		
				for (j = ax; j <= nx; j++)
					g.drawLine( j, py, j, ( co * (j - ax) + ay) );
			}
			else if ( nx != 0 && py != Integer.MIN_VALUE && ny != Integer.MIN_VALUE )
				g.drawLine( nx, py, nx, ny );

			stackValues[i] 	= ny;
			ax 				= nx;
			ay 				= ny;
		}
	}
}
