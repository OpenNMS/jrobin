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
package jrobin.graph;

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
	
	private int gridUnit		= 1;			// minor grid
	private double gridParts	= 2d;
	private int mGridUnit		= 10;			// major grid
	private double mGridParts	= 1d;
	
	private double gridStep		= 2;
	private double mGridStep	= 10;
	
	ValueAxisUnit( double labelStep, double markStep, int roundStep )
	{
		this.labelStep 	= labelStep;
		this.markStep 	= markStep;
		this.roundStep	= roundStep;
	}
	
	ValueAxisUnit( int gridUnit, double gridParts, int mGridUnit, double mGridParts )
	{
		this.gridUnit	= gridUnit;
		this.gridParts	= gridParts;
		this.mGridUnit	= mGridUnit;
		this.mGridParts	= mGridParts;
				
		gridStep		= gridUnit * gridParts;
		mGridStep		= mGridUnit * mGridParts;		
	}
	
	
	public ValueMarker[] getValueMarkers( double lower, double upper )
	{
		double minPoint	= 0.0d;
		double majPoint	= 0.0d;
		
		// Find the first visible gridpoint
		if ( lower > 0 ) {
			while ( minPoint <= lower ) minPoint += gridStep;
			while ( majPoint <= lower ) majPoint += mGridStep;
		} else {
			while ( minPoint >= lower ) minPoint -= gridStep;
			while ( majPoint >= lower ) majPoint -= mGridStep;
			// Go one up to make it visible
			minPoint += gridStep;
			majPoint += mGridStep;
		}
		
		// Now get all time markers.
		// Again we choose to use a series of loops as to avoid unnecessary drawing.		
		ArrayList markerList	= new ArrayList();
		
		while ( minPoint <= upper && majPoint <= upper )
		{
			if ( minPoint < majPoint )
			{
				markerList.add( new ValueMarker(minPoint, "", false) );
				minPoint += gridStep;	
			}
			else
			{
				String str;
				ValueScaler vs 	= new ValueScaler( majPoint, -1);
				int ival		= new Double(vs.getScaledValue()).intValue();
				if ( ival == vs.getScaledValue() )
					str		= (ival + vs.getPrefix()).trim();
				else
					str		= (vs.getScaledValue() + vs.getPrefix()).trim();
				
				if ( minPoint == majPoint )	// Special case, but will happen most of the time
				{
					markerList.add( new ValueMarker(majPoint, str, true) );
					minPoint += gridStep;
					majPoint += mGridStep;
				}
				else
				{
					markerList.add( new ValueMarker(majPoint, str, true) );
					majPoint += mGridStep;
				}
			}
		}

		while ( minPoint <= upper )
		{
			markerList.add( new ValueMarker(minPoint, "", false) );
			minPoint += gridStep;
		}

		while ( majPoint <= upper )
		{
			String str;
			ValueScaler vs 	= new ValueScaler( majPoint, -1);
			int ival		= new Double(vs.getScaledValue()).intValue();
			if ( ival == vs.getScaledValue() )
				str		= (ival + vs.getPrefix()).trim();
			else
				str		= (vs.getScaledValue() + vs.getPrefix()).trim();
			
			markerList.add( new ValueMarker(majPoint, str, true) );
			majPoint += mGridStep;
		}
		
		return (ValueMarker[]) markerList.toArray( new ValueMarker[0] );	
	}
	
	public double getNiceLower( double value )
	{
		int valueInt		= new Double(value).intValue();
		int roundStep		= new Double(gridUnit * gridParts).intValue();
		int num 			= valueInt / roundStep; 
		int mod 			= valueInt % roundStep;
		double gridValue	= (roundStep * (num - 1)) * 1.0d;
		if ( gridValue - value < (gridParts * gridUnit) / 4 )
			gridValue		-= roundStep;
		
		roundStep			= new Double(mGridUnit * mGridParts).intValue();
		num					= valueInt / roundStep;
		mod					= valueInt % roundStep;
		double mGridValue	= (roundStep * (num - 1)) * 1.0d;

		if ( value != 0.0d )
		{
			if ( mGridValue - gridValue < (mGridParts * mGridUnit) / 2)
				return mGridValue;
			else
				return gridValue;
		}

		return value;
		
		/*
		int valueInt	= new Double(value).intValue();
		int num 		= valueInt / roundStep; 
		int mod 		= valueInt % roundStep;
		
		if ( value != 0 )
			return (roundStep * (num - 1)) * 1.0d;
		
		return value;
		*/
	}
	
	public double getNiceHigher( double value )
	{
		int valueInt		= new Double(value).intValue();
		int roundStep		= new Double(gridUnit * gridParts).intValue();
		int num 			= valueInt / roundStep; 
		int mod 			= valueInt % roundStep;
		double gridValue	= (roundStep * (num + 1)) * 1.0d;
		if ( gridValue - value < (gridParts * gridUnit) / 4 )
			gridValue		+= roundStep;
		
		roundStep			= new Double(mGridUnit * mGridParts).intValue();
		num					= valueInt / roundStep;
		mod					= valueInt % roundStep;
		double mGridValue	= (roundStep * (num + 1)) * 1.0d;
		
		if ( value != 0.0d )
		{
			if ( mGridValue - gridValue < (mGridParts * mGridUnit) / 2)
				return mGridValue;
			else
				return gridValue;
		}
		
		return value;
	}
}
