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
import java.util.HashMap;

import jrobin.core.RrdException;

/**
 * <p>Class used to represent a drawn datasource in the graph.</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
abstract class PlotDef
{
	// ================================================================
	// -- Members
	// ================================================================
	static final int PLOT_LINE 	= 0;
	static final int PLOT_AREA 	= 1;
	static final int PLOT_STACK	= 2;
	
	protected boolean visible 	= true;
	protected boolean stacked	= false;
	protected int plotType		= PLOT_LINE;	// Default plotdef is a line
		
	protected String sourceName	= "";
	protected Source source		= null;
	protected Color color		= Color.BLACK;	// Default color is black
	
	
	// ================================================================
	// -- Constructors
	// ================================================================
	PlotDef() {
	}
	
	PlotDef( String sourceName, Color color )
	{
		this.sourceName = sourceName;
		this.color		= color;
		// If no color is given, we should not plot this source
		if ( color == null ) 
			visible = false;	
	}
		
	PlotDef( Source source, Color color, boolean stacked, boolean visible )
	{
		this.source		= source;
		this.color		= color;
		this.stacked	= stacked;
		this.visible	= visible;
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================	
	void setSource( Source[] sources, HashMap sourceIndex ) throws RrdException
	{
		if ( sourceIndex.containsKey(sourceName) ) {
			source = sources[ ((Integer) sourceIndex.get(sourceName)).intValue() ];
		}
		else
			throw new RrdException( "Invalid DEF or CDEF: " + sourceName );
	}
	
	// Default draw is a standard line
	abstract void draw( ChartGraphics g, int[] xValues, int[] stackValues, int lastPlotType ) throws RrdException;
		
	double getValue( int tblPos, long[] timestamps )
	{
		return source.values[tblPos];	
	}
	
	// ================================================================
	// -- Private methods
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
}
