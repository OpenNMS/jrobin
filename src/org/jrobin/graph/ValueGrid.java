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

/**
 * <p>Holds specific information about the Value axis grid of the chart.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class ValueGrid 
{
	// ================================================================
	// -- Members
	// ================================================================	
	private boolean rigid;
	private double lower;
	private double upper;
	
	private double baseValue		= ValueFormatter.DEFAULT_BASE;
	private double[] scaleValues	= new double[] {
											1e18, 1e15, 1e12, 1e9, 1e6, 1e3, 1e0, 1e-3, 1e-6, 1e-9, 1e-12, 1e-15
										};
	
	private ValueAxisUnit vAxis;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Creates a value grid based on a value range and possibly a value axis
	 * unit specification.  The grid can also be specified to be rigid, to prevent
	 * auto scaling of the displayed value range.
	 * @param gr Grid range object.
	 * @param low Lower value of the value range.
	 * @param up Upper value of the value range.
	 * @param vAxis ValueAxisUnit specified to determine the grid lines, if the given
	 * ValueAxisUnit is null, one will be automatically determined.
	 */
	ValueGrid( GridRange gr, double low, double up, ValueAxisUnit vAxis, double base )
	{
		double grLower = Double.MAX_VALUE;
		double grUpper = Double.MIN_VALUE;

		if ( gr != null )
		{
			this.rigid		= gr.isRigid();
			grLower			= gr.getLowerValue();
			grUpper			= gr.getUpperValue(); 	
		}
		
		this.lower	= low;
		this.upper	= up;
		this.vAxis	= vAxis;
		baseValue	= base;
		
		// Fill in the scale values
		double tmp 			= 1;
		for (int i = 1; i < 7; i++) {
			tmp 				*= baseValue;
			scaleValues[6 - i] 	= tmp;
		}
		tmp = 1;
		for (int i = 7; i < scaleValues.length; i++) {
			tmp					*= baseValue;
			scaleValues[i]	 	= ( 1 / tmp );
		}
		
		// Set an appropriate value axis it not given yet
		setValueAxis();

		if ( !rigid ) {
			this.lower		= ( lower == grLower ? grLower : this.vAxis.getNiceLower( lower ) );
			this.upper		= ( upper == grUpper ? grUpper : this.vAxis.getNiceHigher( upper ) );
		}
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

	ValueMarker[] getValueMarkers() {
		return vAxis.getValueMarkers( lower, upper );
	}
	
		
	// ================================================================
	// -- Private methods
	// ================================================================		
	/**
	 * Determines a good ValueAxisUnit to use for grid calculation.
	 * A decent grid is selected based on the value range being used in the chart.
	 */
	private void setValueAxis()
	{
		if ( vAxis != null )
			return;

		if ( Double.isNaN(upper) || upper == Double.MIN_VALUE || upper == Double.MAX_VALUE )
			upper = 0.9;
		if ( Double.isNaN(lower) || lower == Double.MAX_VALUE || lower == Double.MIN_VALUE )
			lower = 0;
		
		if ( !rigid && upper == 0 && upper == lower )
			upper = 0.9;

		// Determine nice axis grid
		double shifted = Math.abs(upper - lower);
		if ( shifted == 0 )			// Special case, no 'range' available
			shifted = upper;

		// Find the scaled unit for this range
		double mod		= 1.0;
		int scaleIndex 	=  scaleValues.length - 1;
		while ( scaleIndex >= 0 && scaleValues[scaleIndex] < shifted ) 
			scaleIndex--;

		// Keep the rest of division
		shifted 		= shifted / scaleValues[++scaleIndex];

		// While rest > 10, divide by 10
		while ( shifted > 10.0 ) {
			shifted /= 10;
			mod	*= 10;
		}
		
		while ( shifted < 1.0 ) {
			shifted *= 10;
			mod /= 10;
		}

		// Create nice grid based on 'fixed' ranges
		if ( shifted <= 1.5 )
			vAxis = new ValueAxisUnit( 0.1 * mod * scaleValues[scaleIndex], 0.5 * mod * scaleValues[scaleIndex] );
		else if ( shifted <= 3 )
			vAxis = new ValueAxisUnit( 0.2 * mod * scaleValues[scaleIndex], 1.0 * mod * scaleValues[scaleIndex] );
		else if ( shifted <= 5 )
			vAxis = new ValueAxisUnit( 0.5 * mod * scaleValues[scaleIndex], 1.0 * mod * scaleValues[scaleIndex] );
		else
			vAxis = new ValueAxisUnit( 1.0 * mod * scaleValues[scaleIndex], 2.0 * mod * scaleValues[scaleIndex] );
	}
}
