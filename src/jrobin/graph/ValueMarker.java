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

/**
 * <p>Represents a value grid marker (grid line with or without label on the Y axis).</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class ValueMarker 
{
	// ================================================================
	// -- Members
	// ================================================================	
	private double value	= 0;
	private boolean major 	= false;


	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Constructs a ValueMarker object by specifying the value at which the grid line
	 * should appear, and specifying if this grid line is a major line or not.  In case of a
	 * major grid line, the value will be shown as a label next to the line.
	 * @param value Value as a double at which the grid line should appear.
	 * @param major True if this marker is a major grid line (with label), false if not.
	 */
	ValueMarker( double value, boolean major )
	{
		this.major	= major;
		this.value 	= value;
	}
	
	// ================================================================
	// -- Protected methods
	// ================================================================	
	double getValue()
	{
		return value;
	}
	
	boolean isMajor()
	{
		return major;
	}
}
