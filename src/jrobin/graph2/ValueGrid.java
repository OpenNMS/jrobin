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
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class ValueGrid 
{
	private boolean rigid;
	private double lower;
	private double upper;
	
	ValueAxisUnit vAxis;
	
	ValueGrid( boolean rigid, double lower, double upper, ValueAxisUnit vAxis )
	{
		this.rigid	= rigid;
		this.lower	= lower;
		this.upper	= upper;
		this.vAxis	= vAxis;
		
		// Set an appropriate value axis it not given yet
		setValueAxis();
		
		if ( !rigid ) {
			this.lower		= this.vAxis.getNiceLower( lower );
			this.upper		= this.vAxis.getNiceHigher( upper );
		}
	}
	
	private void setValueAxis()
	{
		if ( vAxis != null )
			return;
		
		if ( !rigid && upper == 0 && upper == lower )
			upper = 0.9;
		
		// Determine nice axis grid
		double shifted 	= ( Math.abs(upper) > Math.abs(lower) ? Math.abs(upper) : Math.abs(lower) );
		double mod		= 1.0;
		while ( shifted > 10 ) {
			shifted /= 10;
			mod		*= 10;
		}
		while ( shifted < 1 ) {
			shifted *= 10;
			mod		/= 10;
		}
	
		// Create nice grid based on 'fixed' ranges
		if ( shifted <= 3 )
			vAxis = new ValueAxisUnit( 0.2*mod, 1.0*mod );
		else if ( shifted <= 5 )
			vAxis = new ValueAxisUnit( 0.5*mod, 1.0*mod );
		else if ( shifted <= 9 )
			vAxis = new ValueAxisUnit( 0.5*mod, 2.0*mod );
		else
			vAxis = new ValueAxisUnit( 1.0*mod, 5.0*mod );
	}
	
	protected double getLowerValue() {
		return lower;
	}
	
	protected double getUpperValue() {
		return upper;
	}
	
	protected ValueMarker[] getValueMarkers() {
		return vAxis.getValueMarkers( lower, upper );
	}
}
