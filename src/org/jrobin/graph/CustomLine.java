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
import java.awt.BasicStroke;
import java.util.HashMap;

import org.jrobin.core.RrdException;
import org.jrobin.core.XmlWriter;

/**
 * <p>Class used to represent a line defined by two points in a graph.  The line is drawn between those two points.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class CustomLine extends Line
{
	// ================================================================
	// -- Members
	// ================================================================
	private long xVal1;
	private long xVal2;
	
	private double yVal1;
	private double yVal2;
	
	private double dc;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a <code>CustomLine</code> PlotDef object based on a startpoint, endpoint and a graph color.
	 * The resulting line will have a width of 1 pixel.
	 * @param startTime Timestamp of the first datapoint (startpoint).
	 * @param startValue Value of the first datapoint (startpoint).
	 * @param endTime Timestamp of the second datapoint (endpoint).
	 * @param endValue Value of the second datapoint (endpoint).
	 * @param color Color of the resulting line, if no color is specified, the CustomLine will not be drawn.
	 */
	CustomLine( long startTime, double startValue, long endTime, double endValue, Color color )
	{
		this.color = color;
		if ( color == null )
			visible = false;
		
		this.xVal1 = startTime;
		this.xVal2 = endTime;
		this.yVal1 = startValue;
		this.yVal2 = endValue;

		try
		{
			long xc	   = xVal2 - xVal1;
			if ( xc != 0 )
				this.dc		= ( yVal2 - yVal1 ) / xc;
			else
				this.dc		= 0;
		}
		catch (Exception e) {
			this.dc = 0;
		}  
	}
	
	/**
	 * Constructs a <code>CustomLine</code> PlotDef object based on a startpoint, a endpoint, a graph color and a line width.
	 * @param startTime Timestamp of the first datapoint (startpoint).
	 * @param startValue Value of the first datapoint (startpoint).
	 * @param endTime Timestamp of the second datapoint (endpoint).
	 * @param endValue Value of the second datapoint (endpoint).
	 * @param color Color of the resulting line, if no color is specified, the CustomLine will not be drawn.
	 * @param lineWidth Width in pixels of the line to draw.
	 */
	CustomLine( long startTime, double startValue, long endTime, double endValue, Color color, int lineWidth )
	{
		this( startTime, startValue, endTime, endValue, color );
		this.lineWidth = lineWidth;
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================
	/**
	 * Draws the actual CustomLine on the chart.
	 * @param g ChartGraphics object representing the graphing area.
	 * @param xValues List of relative chart area X positions corresponding to the datapoints, obsolete with CustomLine.
	 * @param stackValues Datapoint values of previous PlotDefs, used to stack on if necessary.
	 * @param lastPlotType Type of the previous PlotDef, used to determine PlotDef type of a stack.
	 */	
	void draw( ChartGraphics g, int[] xValues, double[] stackValues, int lastPlotType ) throws RrdException
	{
		g.setColor( color );
		g.setStroke( lineWidth != 1 ? new BasicStroke(lineWidth) : DEF_LINE_STROKE );
		
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

		// Draw the line
		if ( visible )
			g.drawLine( ax, ay, nx, ny );
		 
		// Set the stackvalues
		int rx	= nx - ax;
		if ( rx != 0 )
		{
			double rc = ((ny - ay) * 1.0d) / rx;
			for (int i = 0; i < xValues.length; i++) {
				if ( xValues[i] < ax || xValues[i] > nx ) 
					stackValues[i] = g.getInverseY(0);
				else if ( ay == ny )
					stackValues[i] = g.getInverseY(ay);
				else
					stackValues[i] = g.getInverseY( (int) (rc * (xValues[i] - ax) + ay) );
			}
		}

		g.setStroke( STROKE );
	}

	/**
	 * Retrieves the value for a specific point of the CustomLine.  The corresponding value is calculated based
	 * on the mathematical line function with the timestamp as a X value.
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
		
		// Hrule
		if ( yVal1 == yVal2 )
			return yVal1;
		
		// Vrule
		if ( yVal1 == Double.MIN_VALUE && yVal2 == Double.MAX_VALUE )
			return Double.NaN;
		
		// No line, very rare, will usually be 'out of range' first
		if ( xVal1 == xVal2 )
			return Double.NaN;
				
		// Custom line
		return ( dc * ( time - xVal1 ) + yVal1 );
	}
	
	// Stubbed method, irrelevant for this PlotDef
	void setSource( Source[] sources, HashMap sourceIndex ) throws RrdException	{
	}

	// Stubbed, we don't need to set value for a Custom plotdef
	void setValue( int tableRow, long preciseTime, long[] reducedTimestamps ) {
	}

	void exportXmlTemplate( XmlWriter xml, String legend ) {
		if(yVal1 == yVal2 && xVal1 != xVal2) {
			// hrule
			xml.startTag("hrule");
			xml.writeTag("value", yVal1);
			xml.writeTag("color", color);
			xml.writeTag("legend", legend);
			xml.writeTag("width", lineWidth);
			xml.closeTag(); // hrule
		}
		else if(yVal1 != yVal2 && xVal1 == xVal2) {
			// vrule
			xml.startTag("vrule");
			xml.writeTag("time", xVal1);
			xml.writeTag("color", color);
			xml.writeTag("legend", legend);
			xml.writeTag("width", lineWidth);
			xml.closeTag(); // vrule
		}
		else if(yVal1 != yVal2 && xVal1 != xVal2) {
			// general line
			xml.startTag("line");
			xml.writeTag("time1", xVal1);
			xml.writeTag("value1", yVal1);
			xml.writeTag("time2", xVal2);
			xml.writeTag("value2", yVal2);
			xml.writeTag("color", color);
			xml.writeTag("legend", legend);
			xml.writeTag("width", lineWidth);
			xml.closeTag(); //line
		}
	}
}
