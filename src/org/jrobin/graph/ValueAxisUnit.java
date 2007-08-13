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
/*
 * Created on 29-aug-2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.jrobin.graph;

import org.jrobin.core.XmlWriter;

import java.util.*;

/**
 * <p>Class used to determine the chart grid shown on the Y (value) axis.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class ValueAxisUnit 
{
	// ================================================================
	// -- Members
	// ================================================================	
	private double gridStep			= 2;
	private double labelStep		= 10;
	
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Creates a ValueAxisUnit based on a minor and major grid step.
	 * Minor grid lines appear at <code>gridStep</code>, major grid lines accompanied by a label
	 * will appear every <code>labelStep</code> value.   
	 * @param gridStep Value step on which a minor grid line will appear.
	 * @param labelStep Value step on which a major grid line with value label will appear.
	 */
	ValueAxisUnit( double gridStep, double labelStep )
	{
		this.gridStep	= gridStep;
		this.labelStep	= labelStep;		
	}
	
	double getGridStep() {
		return gridStep;
	}

	double getLabelStep() {
		return labelStep;
	}


	// ================================================================
	// -- Protected methods
	// ================================================================
	/**
	 * Returns a set of markers making up the grid for the Y axis.
	 * All markers are situated in a given value range.
	 * @param lower Lower value of the value range.
	 * @param upper Upper value of the value range.
	 * @return List of markers as a ValueMarker array.
	 */
	ValueMarker[] getValueMarkers( double lower, double upper )
	{
		double minPoint		= 0.0;
		double majPoint		= 0.0;

		// Find the first visible gridpoint
		if ( lower > 0 )
		{
			minPoint 	= lower;
			double mod 	= ( lower % labelStep );

			if ( mod > 0 )
				majPoint	= lower + (labelStep - mod );
			else
				majPoint	= lower;
		}
		else if ( lower < 0 )
		{
			minPoint 	= lower;
			double mod 	= ( lower % labelStep );

			if ( Math.abs(mod) > 0 )
				majPoint	= lower - mod;
			else
				majPoint	= lower;
		}

		// Now get all value markers.
		// Again we choose to use a series of loops as to avoid unnecessary drawing.
		ArrayList markerList	= new ArrayList();

		while ( minPoint <= upper && majPoint <= upper )
		{
			if ( minPoint < majPoint )
			{
				markerList.add( new ValueMarker(minPoint, false) );
				minPoint = round( minPoint + gridStep );
			}
			else
			{
				if ( minPoint == majPoint )	// Special case, but will happen most of the time
				{
					markerList.add( new ValueMarker(majPoint, true) );
					minPoint = round( minPoint + gridStep );
					majPoint = round( majPoint + labelStep );
				}
				else
				{
					markerList.add( new ValueMarker(majPoint, true) );
					majPoint = round( majPoint + labelStep );
				}
			}
		}

		while ( minPoint <= upper )
		{
			markerList.add( new ValueMarker(minPoint, false) );
			minPoint = round( minPoint + gridStep );
		}

		while ( majPoint <= upper )
		{
			markerList.add( new ValueMarker(majPoint, true) );
			majPoint = round( majPoint + labelStep );
		}

		return (ValueMarker[]) markerList.toArray( new ValueMarker[0] );
	}

	/**
	 * Gets a rounded value that's slightly below the given exact value.
	 * The rounding is based on the given grid specifications of the axis.
	 * @param ovalue Original exact value.
	 * @return Rounded value lower than the given exact value.
	 */
	double getNiceLower( double ovalue )
	{
		// Add some checks
		double gridFactor	= 1.0;
		double mGridFactor	= 1.0;

		double gridStep		= this.gridStep;
		double mGridStep	= this.labelStep;

		while ( gridStep < 10.0 ) {
			gridStep		*= 10;
			gridFactor		*= 10;
		}

		while ( mGridStep < 10.0 ) {
			mGridStep		*= 10;
			mGridFactor		*= 10;
		}

		int sign			= ( ovalue > 0 ? 1 : -1 );

		long lGridStep		= new Double( gridStep ).longValue();
		long lmGridStep		= new Double( mGridStep ).longValue();

		long lValue			= new Double(sign * ovalue * gridFactor).longValue();
		long lmValue		= new Double(sign * ovalue * mGridFactor).longValue();

		long lMod 			= lValue % lGridStep;
		long lmMod			= lmValue % lmGridStep;

		if ( ovalue < 0 )
		{
			if ( lmMod > ( mGridStep * 0.5 ) )
				return ((double) (sign*lmValue + lmMod - lmGridStep ) / mGridFactor);
			else if ( lMod > 0 )
				return ((double) (sign*lValue + lMod - lGridStep) / gridFactor);
			else
				return ((double) (sign*lValue - lGridStep) / gridFactor);
		}
		else
		{
			if ( lmMod < ( mGridStep * 0.5 ) )
				return ((double) (sign*lmValue - lmMod) / mGridFactor);
			else if ( lMod > 0 )
				return ((double) (sign*lValue - lMod) / gridFactor);
			else
				return ((double) (sign*lValue) / gridFactor);
		}
	}
	
	/**
	 * Gets a rounded value that's slightly above the given exact value.
	 * The rounding is based on the given grid specifications of the axis.
	 * @param ovalue Original exact value.
	 * @return Rounded value higher than the given exact value.
	 */
	double getNiceHigher( double ovalue )
	{
		double gridFactor	= 1.0;
		double mGridFactor	= 1.0;

		double gridStep		= this.gridStep;
		double mGridStep	= this.labelStep;

		while ( gridStep < 10.0 ) {
			gridStep		*= 10;
			gridFactor		*= 10;
		}

		while ( mGridStep < 10.0 ) {
			mGridStep		*= 10;
			mGridFactor		*= 10;
		}

		int sign			= ( ovalue > 0 ? 1 : -1 );

		long lGridStep		= new Double( gridStep ).longValue();
		long lmGridStep		= new Double( mGridStep ).longValue();

		long lValue			= new Double(sign * ovalue * gridFactor).longValue();
		long lmValue		= new Double(sign * ovalue * mGridFactor).longValue();

		long lMod 			= lValue % lGridStep;
		long lmMod			= lmValue % lmGridStep;

		if ( ovalue < 0 )
		{
			if ( lmMod < ( mGridStep * 0.5 ) )
				return ((double) (sign*lmValue + lmMod ) / mGridFactor);
			else
				return ((double) (sign*lValue + lMod ) / gridFactor);
		}
		else
		{
			if ( lmMod > ( mGridStep * 0.5 ) )
				return ((double) ( sign * lmValue - lmMod + lmGridStep) / mGridFactor);
			else
				return ((double) ( sign * lValue - lMod + lGridStep) / gridFactor);
		}

	}


	// ================================================================
	// -- Private methods
	// ================================================================
	/**
	 * Rounds a specific double value to 14 decimals.  This is used to avoid strange double values due to the
	 * internal double representation of the JVM.
	 * @param value Original value to round.
	 * @return Value rounded to 14 decimals.
	 */
	private double round( double value )
	{
		return round( value, 14 );		// Big precision
	}
	
	/**
	 * Rounds a specific double value to a given number of decimals.
	 * @param value Original value to round.
	 * @param numDecs Number of decimals to round the value to.
	 * @return Value rounded to given number of decimals.
	 */
	private double round( double value, int numDecs )
	{
		return new java.math.BigDecimal(value).setScale(numDecs , java.math.BigDecimal.ROUND_HALF_EVEN).doubleValue();
	}

	void exportXmlTemplate(XmlWriter xml) {
		xml.startTag("value_axis");
		xml.writeTag("grid_step", getGridStep());
		xml.writeTag("label_step", getLabelStep());
		xml.closeTag(); // value_axis
	}
}
