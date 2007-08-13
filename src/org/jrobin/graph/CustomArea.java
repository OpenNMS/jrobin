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
import java.util.HashMap;

import org.jrobin.core.RrdException;
import org.jrobin.core.XmlWriter;

/**
 * <p>Class used to represent an area defined by two points in a graph.  The area is drawn as a rectangle 
 * that has the line startpoint-endpoint as a diagonal.  Direction of plotting the CustomArea can be seen as 
 * drawing a rectangle from the bottom-left corner to the upper-right corner.
 * It is possible to stack another PlotDef on top of the CustomArea, in that case the stacked PlotDef will 
 * always be stacked on top of the (Y) value of the second defined datapoint of the CustomArea.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class CustomArea extends PlotDef
{
	// ================================================================
	// -- Members
	// ================================================================
	private long xVal1;
	private long xVal2;
	
	private double yVal1;
	private double yVal2;


	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a <code>CustomArea</code> PlotDef object based on a startpoint, endpoint and a graph color.
	 * The resulting area will be graphed as a rectangle from the bottom-left corner (startpoint) to the
	 * upper-right coner (endpoint).
	 * @param startTime Timestamp of the first datapoint (startpoint).
	 * @param startValue Value of the first datapoint (startpoint).
	 * @param endTime Timestamp of the second datapoint (endpoint).
	 * @param endValue Value of the second datapoint (endpoint).
	 * @param color Color of the resulting line, if no color is specified, the CustomLine will not be drawn.
	 */
	CustomArea( long startTime, double startValue, long endTime, double endValue, Color color )
	{
		this.color = color;
		if ( color == null )
			visible = false;
		
		this.xVal1 = startTime;
		this.xVal2 = endTime;
		this.yVal1 = startValue;
		this.yVal2 = endValue;
	}


	// ================================================================
	// -- Protected methods
	// ================================================================	
	/**
	 * Draws the actual CustomArea on the chart.
	 * @param g ChartGraphics object representing the graphing area.
	 * @param xValues List of relative chart area X positions corresponding to the datapoints, obsolete with CustomArea.
	 * @param stackValues Datapoint values of previous PlotDefs, used to stack on if necessary.
	 * @param lastPlotType Type of the previous PlotDef, used to determine PlotDef type of a stack.
	 */	
	void draw( ChartGraphics g, int[] xValues, double[] stackValues, int lastPlotType ) throws RrdException
	{
		g.setColor( color );
	
		int ax, ay, nx, ny;
	
		// Get X positions
		if ( xVal1 == Long.MIN_VALUE )
			ax = g.getMinX();
		else if ( xVal1 == Long.MAX_VALUE )
			ax = g.getMaxX();
		else
			ax = g.getX( xVal1 );
	
		if ( xVal2 == Long.MIN_VALUE )
			nx = g.getMinX();
		else if ( xVal2 == Long.MAX_VALUE )
			nx = g.getMaxX();
		else
			nx = g.getX( xVal2 );
	
		// Get Y positions
		if ( yVal1 == Double.MIN_VALUE )
			ay = g.getMinY();
		else if ( yVal1 == Double.MAX_VALUE )
			ay = g.getMaxY();
		else
			ay = g.getY( yVal1 );
	
		if ( yVal2 == Double.MIN_VALUE )
			ny = g.getMinY();
		else if ( yVal2 == Double.MAX_VALUE )
			ny = g.getMaxY();
		else
			ny = g.getY( yVal2 );

		// Draw the area
		if ( visible )
		{
			if ( ny > ay )
				g.fillRect( ax, ay, nx, ny );
			else
				g.fillRect( ax, ny, nx, ay );
		}
		
		// Set the stackvalues
		// Always use the y value of the second specified point to stack on
		if ( yVal2 != Double.MAX_VALUE )
			for (int i = 0; i < stackValues.length; i++)
				if ( xValues[i] < ax || xValues[i] > nx ) 
					stackValues[i] = g.getInverseY(0);
				else
					stackValues[i] = g.getInverseY(ny);
	}

	/**
	 * Retrieves the value for a specific point of the CustomArea.  The CustomArea is always a rectangle,
	 * this means the returned double value will always be equal to the (Y) value of the second datapoint.
	 * In case of an unlimited CustomArea (second datapoint Y value is <code>Double.MAX_VALUE</code>)
	 * the returned value is <code>Double.NaN</code>.
	 * @param tblPos Table index of the datapoint to be retrieved.
	 * @param timestamps Table containing the timestamps corresponding to all datapoints.
	 * @return Y value of the point as a double.
	 */
	double getValue( int tblPos, long[] timestamps )
	{
		long time = timestamps[tblPos];
	
		// Out of range
		if ( time > xVal2 || time < xVal1 )
			return Double.NaN;
	
		if ( yVal2 == Double.MAX_VALUE )
			return Double.NaN;
		
		return yVal2;
	}

	// Stubbed method, irrelevant for this PlotDef
	void setSource( Source[] sources, HashMap sourceIndex ) throws RrdException {
	}

	// Stubbed, we don't need to set value for a Custom plotdef
	void setValue( int tableRow, long preciseTime, long[] reducedTimestamps ) {
	}

	void exportXmlTemplate( XmlWriter xml, String legend ) {
		xml.startTag("area");
		xml.writeTag("time1", xVal1);
		xml.writeTag("value1", yVal1);
		xml.writeTag("time2", xVal2);
		xml.writeTag("value2", yVal2);
		xml.writeTag("color", color);
		xml.writeTag("legend", legend);
		xml.closeTag(); //area
	}
}
