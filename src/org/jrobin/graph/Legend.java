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

import java.awt.Color;

import org.jrobin.core.RrdException;

/**
 * <p>Represents a PlotDef legend string on the graph.  A Legend item is comprised out of two parts, 
 * a text string, and a legend marker (small rectangle in the same color as the PlotDef).</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class Legend extends Comment 
{
	// ================================================================
	// -- Members
	// ================================================================
	private Color color 	= Color.WHITE;
	private int refPlotDef	= -1;
	
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a Legend object based on a specified text string.
	 * The legend marker for this Legend will be drawn in white color.
	 * @param text Text part of the legend.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	Legend( String text ) throws RrdException
	{
		super(text);
		this.commentType = Comment.CMT_LEGEND;
	}
	
	/**
	 * Constructs a Legend object based on a specified text string and marker color.
	 * @param text Text part of the legend.
	 * @param color Color to use for the rectangular legend marker.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	Legend( String text, Color color ) throws RrdException
	{
		super(text);
		if ( text == null )
			this.commentType = Comment.CMT_NOLEGEND;
		else
			this.commentType = Comment.CMT_LEGEND;
		this.color 			= color;
	}
	
	/**
	 * 
	 * @param text
	 * @param color
	 * @param referredPlotDef
	 * @throws RrdException
	 */
	Legend( String text, Color color, int referredPlotDef ) throws RrdException
	{
		this( text, color );
		
		refPlotDef = referredPlotDef;
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================
	Color getColor() {
		return color;
	}
	
	int getPlofDefIndex() {
		return refPlotDef;
	}
}
