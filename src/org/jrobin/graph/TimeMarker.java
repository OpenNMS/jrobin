/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
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

/**
 * <p>Represents a time grid marker (grid line with or without label on the X axis).</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class TimeMarker 
{
	// ================================================================
	// -- Members
	// ================================================================	
	private long timestamp	= 0;
	private String text		= "";
	private boolean label	= false;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * @param ts Timestamp in seconds where this line should be set.
	 * @param v Text of a possible label for this marker.
	 * @param l True if this marker is a major grid line and is accompanied by a label.
	 */
	TimeMarker( long ts, String v, boolean l )
	{
		this.label	= l;
		timestamp 	= ts;
		text 		= v;
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================		
	boolean isLabel()
	{
		return label;
	}
	
	long getTimestamp() 
	{
		return timestamp / 1000;	
	}
	
	String getLabel()
	{
		return text;
	}
}
