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
import java.util.HashMap;

import jrobin.core.RrdException;

/**
 * <p>Class used to represent a drawn datasource in the graph.</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
class PlotDef
{
	static final int PLOT_LINE 	= 0;
	static final int PLOT_AREA 	= 1;
	static final int PLOT_STACK	= 2;
	static final int PLOT_VRULE	= 3;
	
	protected boolean visible 	= true;
	protected boolean stacked	= false;
	
	protected int plotType		= PLOT_LINE;	// Default plotdef is a line
	protected int lineWidth		= 1;			// Default line width of 1 pixel
	
	protected String sourceName	= "";
	protected Source source		= null;
	protected Color color		= Color.BLACK;	// Default color is black
	
	
	PlotDef( String sourceName, Color color )
	{
		this.sourceName = sourceName;
		this.color		= color;	
	}
	
	PlotDef( String sourceName, Color color, int lineWidth )
	{
		this( sourceName, color );
		this.lineWidth	= lineWidth;
	}
	
	PlotDef( Source source, Color color, boolean stacked )
	{
		this.source		= source;
		this.color		= color;
		this.stacked	= stacked;
	}
	
	// ================================================================
		
	void setSource( Source[] sources, HashMap sourceIndex ) throws RrdException
	{
		if ( sourceIndex.containsKey(sourceName) ) {
			source = sources[ ((Integer) sourceIndex.get(sourceName)).intValue() ];
		}
		else
			throw new RrdException( "Invalid DEF or CDEF: " + sourceName );
	}
	
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
			
			if ( stacked )
				ny += stackValues[i];
			
			if ( visible && nx != 0 && ay != Integer.MIN_VALUE && ny != Integer.MIN_VALUE )
				g.drawLine( ax, ay, nx, ny );

			stackValues[i] 	= ny;
			ax 				= nx;
			ay 				= ny;
		}
		
		g.setStroke( new BasicStroke() );
	}
	
	// ================================================================
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
	
	int getLineWidth() {
		return lineWidth;
	}
}
