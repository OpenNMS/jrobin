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
import java.awt.BasicStroke;

import jrobin.core.RrdException;

/**
 * <p>description</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Line extends PlotDef
{
	// ================================================================
	// -- Members
	// ================================================================
	protected int lineWidth		= 1;			// Default line width of 1 pixel
	
	// ================================================================
	// -- Constructors
	// ================================================================
	Line() {
		super();
	} 
		
	Line( String sourceName, Color color )
	{
		super( sourceName, color );
	}
	
	Line( String sourceName, Color color, int lineWidth )
	{
		this( sourceName, color );
		this.lineWidth	= lineWidth;
	}
	
	Line( Source source, Color color, boolean stacked, boolean visible )
	{
		super( source, color, stacked, visible);
	}
	
	// ================================================================
	// -- Protected methods
	// ================================================================
	void draw( ChartGraphics g, int[] xValues, int[] stackValues, int lastPlotType ) throws RrdException
	{
		g.setColor( color );
		g.setStroke( new BasicStroke(lineWidth) );

		int ax = 0, ay = 0;
		int nx = 0, ny = 0, last = -1;
	
		for (int i = 0; i < xValues.length; i++)
		{
			nx = xValues[i];
			ny = g.getY( source.values[i] );
		
			if ( stacked && ny != Integer.MIN_VALUE )
				ny += stackValues[i];
		
			if ( visible && ny != Double.NaN && nx != 0 && ay != Integer.MIN_VALUE && ny != Integer.MIN_VALUE )
				g.drawLine( ax, ay, nx, ny );
		
			stackValues[i] 	= ny;
			ax 				= nx;
			ay 				= ny;
		}
	
		g.setStroke( new BasicStroke() );
	}
	
	int getLineWidth() {
		return lineWidth;
	}
}
