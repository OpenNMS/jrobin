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

/**
 * <p>description</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 */
class GridRange 
{
	private double lower 	= Double.NaN;
	private double upper 	= Double.NaN;
	private boolean rigid	= false;
	
	GridRange( double lower, double upper )
	{
		this.lower	= lower;
		this.upper	= upper;	
	}
	
	GridRange( double lower, double upper, boolean rigid )
	{
		this.lower	= lower;
		this.upper	= upper;
		this.rigid	= rigid;
	}
	
	double getLowerValue() {
		return lower;
	}
	
	double getUpperValue() {
		return upper;
	}
	
	boolean isRigid() {
		return rigid;
	}
}
