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

import org.jrobin.core.XmlWriter;

/**
 * <p>Represents Y grid specifications for the chart area.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class GridRange 
{
	// ================================================================
	// -- Members
	// ================================================================	
	private double lower 	= Double.NaN;
	private double upper 	= Double.NaN;
	private boolean rigid	= false;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Constructs a <code>GridRange</code> object based on a lower and upper value.
	 * @param lower Lower value of the grid range.
	 * @param upper Upper value of the grid range.
	 */	
	GridRange( double lower, double upper )
	{
		this.lower	= lower;
		this.upper	= upper;	
	}
	
	/**
	 * Constructs a <code>GridRange</code> object based on a lower and upper value and a rigid specification.
	 * If a grid is specified as rigid, then the specified range of lower/upper value will be used as graph boundaries.
	 * If a grid is not rigid, then the boundaries might be scaled to allow for the complete necessary range of values:
	 * if the maximum Y value is higher than upper value, then upper value will be raised, reverse with the lower value.
	 * A non-rigid grid will always at least display a range of lower/upper value, a rigid grid will always display the range
	 * of lower and upper value, no more and no less.
	 * @param lower Lower value of the grid range.
	 * @param upper Upper value of the grid range.
	 * @param rigid True if the grid is rigid, false if not (default: false).
	 */
	GridRange( double lower, double upper, boolean rigid )
	{
		this.lower	= lower;
		this.upper	= upper;
		this.rigid	= rigid;
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================		
	double getLowerValue() {
		return lower;
	}
	
	double getUpperValue() {
		return upper;
	}
	
	boolean isRigid() {
		return rigid;
	}

	void exportXmlTemplate(XmlWriter xml) {
		xml.startTag("grid_range");
		xml.writeTag("lower", getLowerValue());
		xml.writeTag("upper", getUpperValue());
		xml.writeTag("rigid", isRigid());
		xml.closeTag(); // grid_range
	}
}
