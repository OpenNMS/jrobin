/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
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
package jrobin.graph2;

import java.util.*;

/**
 * @author cbld
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ValueAxisUnit 
{
	private double labelStep 	= 2;
	private double markStep		= 1;
	private int roundStep 		= 2;
	
	private double gridStep		= 2;
	private double mGridStep	= 10;
	
	ValueAxisUnit( double gridStep, double labelStep )
	{
		this.gridStep	= gridStep;
		this.mGridStep	= labelStep;
	}
	
	private double round( double value )
	{
		return round( value, 14 );		// Big precision
	}
	
	private double round( double value, int numDecs )
	{
		return new java.math.BigDecimal(value).setScale(numDecs , java.math.BigDecimal.ROUND_HALF_EVEN).doubleValue();
	}
	
	protected ValueMarker[] getValueMarkers( double lower, double upper )
	{
		double minPoint	= 0.0d;
		double majPoint	= 0.0d;
		
		// Find the first visible gridpoint
		if ( lower > 0 ) {
			while ( minPoint < lower ) minPoint += gridStep;
			while ( majPoint < lower ) majPoint += mGridStep;
		} else {
			while ( minPoint > lower ) minPoint -= gridStep;
			while ( majPoint > lower ) majPoint -= mGridStep;
			// Go one up to make it visible
			if (minPoint != lower ) minPoint += gridStep;
			if (majPoint != lower ) majPoint += mGridStep;
		}
		
		// Now get all time markers.
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
					majPoint = round( majPoint + mGridStep );
				}
				else
				{
					markerList.add( new ValueMarker(majPoint, true) );
					majPoint = round( majPoint + mGridStep );
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
			majPoint = round( majPoint + mGridStep );
		}
		
		return (ValueMarker[]) markerList.toArray( new ValueMarker[0] );
	}
		
	public double getNiceLower( double ovalue )
	{
		// Add some checks
		double gridFactor	= 1.0;
		double mGridFactor	= 1.0;
		
		double gridStep		= this.gridStep;
		double mGridStep	= this.mGridStep;
		
		if ( gridStep < 1.0 ) {
			gridStep		*= 100;
			gridFactor		= 100;
		}
		
		if ( mGridStep < 1.0 ) {
			mGridStep		*= 100;
			mGridFactor		= 100;
		}
		
		double value		= ovalue * gridFactor;
		int valueInt		= new Double(value).intValue();
		int roundStep		= new Double(gridStep).intValue();
		if ( roundStep == 0 ) roundStep = 1;
		int num 			= valueInt / roundStep; 
		int mod 			= valueInt % roundStep;
		double gridValue	= (roundStep * num) * 1.0d;
		if ( gridValue > value )
			gridValue		-= roundStep;
		
		if ( num == 0 && value >= 0 )
			gridValue		= 0.0;
		else if ( Math.abs(gridValue - value) < (gridStep) / 16 )
			gridValue		-= roundStep;
		
		value				= ovalue * mGridFactor;
		roundStep			= new Double(mGridStep).intValue();
		if ( roundStep == 0 ) roundStep = 1;
		num					= valueInt / roundStep;
		mod					= valueInt % roundStep;
		double mGridValue	= (roundStep * num) * 1.0d;
		if ( mGridValue > value )
			mGridValue		-= roundStep;
		
		if ( value != 0.0d )
		{
			if ( Math.abs(mGridValue - gridValue) < (mGridStep) / 2)
				return mGridValue / mGridFactor;
			else
				return gridValue / gridFactor;
		}

		return ovalue;
	}
	
	public double getNiceHigher( double ovalue )
	{
		// Add some checks
		double gridFactor	= 1.0;
		double mGridFactor	= 1.0;
		
		double gridStep		= this.gridStep;
		double mGridStep	= this.mGridStep;
		
		if ( gridStep < 1.0 ) {
			gridStep 		*= 100;
			gridFactor		= 100;
		}
	
		if ( mGridStep < 1.0 ) {
			mGridStep	*= 100;
			mGridFactor		= 100;
		}
		
		double value		= ovalue * gridFactor;
		int valueInt		= new Double(value).intValue();
		int roundStep		= new Double(gridStep).intValue();
		if ( roundStep == 0 ) roundStep = 1;
		int num 			= valueInt / roundStep; 
		int mod 			= valueInt % roundStep;
		double gridValue	= (roundStep * (num + 1)) * 1.0d;
		if ( gridValue - value < (gridStep) / 8 )
			gridValue		+= roundStep;
		
		value				= ovalue * mGridFactor;
		roundStep			= new Double(mGridStep).intValue();
		if ( roundStep == 0 ) roundStep = 1;
		num					= valueInt / roundStep;
		mod					= valueInt % roundStep;
		double mGridValue	= (roundStep * (num + 1)) * 1.0d;
		
		if ( value != 0.0d )
		{
			if ( Math.abs(mGridValue - gridValue) < (mGridStep) / 2)
				return mGridValue / mGridFactor;
			else
				return gridValue / gridFactor;
		}
		
		return ovalue;
	}
}
