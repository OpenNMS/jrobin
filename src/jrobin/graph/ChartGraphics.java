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
package jrobin.graph;

import java.awt.Color;
import java.awt.Stroke;
import java.awt.Graphics2D;

/**
 * <p>Represent a specific Graphics object holding all specifications of the chart area of the entire graph,
 * including a handle to a Graphics2D context.  This class is a wrapper around the graphics context, taking
 * care of some coordinate calculations and translations automatically.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class ChartGraphics 
{
	// ================================================================
	// -- Members
	// ================================================================	
	private Graphics2D g;
	
	private int width, height;
	private long xStart, xEnd;
	private double yStart, yEnd;

	private double widthDelta = 1.0d, heightDelta = 3.0d;


	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Creates a new <code>ChartGraphics</code> object based on a graphics handle.
	 * @param graphics Handle of a Graphics2D context to use.
	 */
	ChartGraphics( Graphics2D graphics )
	{
		g = graphics;
	}


	// ================================================================
	// -- Protected methods
	// ================================================================	
	/**
	 * Draws a line on the graphics context.  The line is specified by
	 * providing begin and end point.
	 * @param x1 X coordinate of the begin point.
	 * @param y1 Y coordinate of the begin point.
	 * @param x2 X coordinate of the end point.
	 * @param y2 Y coordinate of the end point.
	 */
	void drawLine(int x1, int y1, int x2, int y2)
	{
		g.drawLine( x1, -y1, x2, -y2 );
	}

	/**
	 * Draws a filled rectangle on the graphics context.  The rectangle is specified
	 * by providing two points, bottom-left corner and upper-right corner.  This is contrary
	 * to the general Graphics <code>fillRect</code> method, where the rectangle is specified
	 * using a single point and a rectangle height and width.
	 * @param x1 X coordinate of the bottom-left corner.
	 * @param y1 Y coordinate of the bottom-left corner.
	 * @param x2 X coordinate of the upper-right corner.
	 * @param y2 Y coordinate of the upper-right corner.
	 */
	// Contrary to Graphics2D fillRect, this method uses boundary points
	void fillRect(int x1, int y1, int x2, int y2)
	{
		g.fillRect( x1, -y2, x2 - x1, y2 - y1 );
	}
	
	/**
	 * Sets the color of the current brush on the graphics context.
	 * The next items drawn will be in this color.
	 * @param c Color to use.
	 */
	void setColor( Color c )
	{
		g.setColor( c );
	}

	/**
	 * Sets the absolute (pixel) dimensions of the chart area to which this object applies.
	 * @param width Width of the chart area in pixels.
	 * @param height Height of the chart area in pixels.
	 */
	void setDimensions( int width, int height )
	{
		this.width  = width;
		this.height	= height;
	}

	/**
	 * Sets the timerange specified for the chart, used for scaling down timestamps to fit in the X axis pixel range.
	 * @param start Start timestamp (in seconds) of the timespan.
	 * @param end End timestamp (in seconds) of the timespan.
	 */
	void setXRange( long start, long end )
	{
		xStart 	= start;
		xEnd	= end; 
	
		if ( xEnd != xStart )
			widthDelta = width * 1.0d / (( xEnd - xStart) * 1.0d);
		else
			widthDelta = 1.0d;
	}

	/**
	 * Sets the valuerange specified for the chart, used for scaling down values to fit in the Y axis pixel range.
	 * @param lower Lower value of the range.
	 * @param upper Upper value of the range.
	 */
	void setYRange( double lower, double upper )
	{
		yStart 	= lower;
		yEnd	= upper; 
	
		if ( yEnd != yStart )
			heightDelta = height * 1.0d / (( yEnd - yStart) * 1.0d);
		else
			heightDelta = 1.0d;
	}

	/**
	 * Calculates the pixel position on the X axis for a specific timestamp (in seconds).
	 * @param timestamp Timestamp for which to calculate the corresponding X coordinate.
	 * @return X coordinate on the horizontal chart axis.
	 */
	int getX( long timestamp )
	{
		return new Double((timestamp - xStart) * widthDelta).intValue();
	}

	/**
	 * Calculates the pixel position on the Y axis for a specific double value.
	 * @param value Value for which to calculate the corresponding Y coordinate.
	 * @return Y coordinate on the horizontal chart axis.
	 */
	int getY( double value )
	{
		if ( Double.isNaN(value) ) return Integer.MIN_VALUE;
	
		int tmp = new Double( (value - ( yStart < 0 ? 0 : Math.abs(yStart) ) ) * heightDelta).intValue();
	
		return ( tmp > value * heightDelta ? tmp - 1 : tmp ); 
	}
	
	/**
	 * Sets the Stroke to use for graphing on the graphics context.
	 * @param s Specified <code>Stroke</code> to use.
	 */
	void setStroke( Stroke s )
	{
		g.setStroke( s );
	}
	
	/**
	 * Retrieves the lowest X coordinate of the chart area.
	 * @return Lowest X coordinate of the chart area.
	 */
	int getMinX()
	{
		return 0;
	}
	
	/**
	 * Retrieves the highest X coordinate of the chart area.
	 * @return Highest X coordinate of the chart area.
	 */
	int getMaxX()
	{
		return 0 + width;
	}
	
	/**
	 * Retrieves the lowest Y coordinate of the chart area.
	 * @return Lowest Y coordinate of the chart area.
	 */
	int getMinY()
	{
		return 0;
	}
	
	/**
	 * Retrieves the highest Y coordinate of the chart area.
	 * @return Highest Y coordinate of the chart area.
	 */
	int getMaxY()
	{
		return 0 + height;
	}
	
	/**
	 * Retrieves the handle of the Graphics2D context for this object.
	 * @return Handle to the internal Graphics2D context.
	 */
	Graphics2D getGraphics()
	{
		return g;
	}
}
