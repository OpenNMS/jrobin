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
package jrobin.graph;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * @author cbld
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class TimeAxisUnit 
{
	private static final int[] calendarUnit =
				{
					Calendar.SECOND,
					Calendar.MINUTE,
					Calendar.HOUR_OF_DAY,
					Calendar.DAY_OF_MONTH,
					Calendar.WEEK_OF_YEAR,
					Calendar.MONTH,
					Calendar.YEAR	
				};
	private static final int[] nullValue =
				{
					0,
					0,
					0,
					1,
					1,
					0,
					1970			// Should never be used, but put there to avoid index out of bounds	
				};

	// Indices in the calendarUnit table
	public static final int SECOND		= 0;
	public static final int MINUTE		= 1;
	public static final int HOUR 		= 2;
	public static final int DAY 		= 3;
	public static final int WEEK 		= 4;
	public static final int MONTH 		= 5;
	public static final int YEAR 		= 6;
	
	private int gridTime		= HOUR;			// minor grid
	private int gridUnits		= 1;
	private int mGridTime		= HOUR;			// major grid
	private int mGridUnits		= 6;
	// By default labels are shown at the major grid
	
	private int unitType 		= HOUR;
	private int unitParts 		= 1;
	
	private SimpleDateFormat df = new SimpleDateFormat("HH:mm");
 	
	TimeAxisUnit( int unitType, int unitParts, SimpleDateFormat df )
	{
		this.unitType 	= unitType;
		this.unitParts 	= unitParts;
		//this.df			= df; 
	}
	
	TimeAxisUnit( int gridTime, int gridUnits, int mGridTime, int mGridUnits, SimpleDateFormat df )
	{
		this.gridTime	= gridTime;
		this.gridUnits	= gridUnits;
		this.mGridTime	= mGridTime;
		this.mGridUnits	= mGridUnits;
		this.df			= df;	
	}
	
	private void setStartPoint( Calendar t, int unit, long exactStart )
	{
		t.setTimeInMillis( exactStart );
		for (int i = 0; i < calendarUnit.length && i <= unit; i++)
			t.set( calendarUnit[i], nullValue[i] );
		if ( unit == WEEK )
			t.set( Calendar.DAY_OF_WEEK, t.getFirstDayOfWeek() );
	}
	
	private long getNextPoint( Calendar t, int unit, int unitSteps )
	{
		t.add( calendarUnit[unit], unitSteps );
		return t.getTimeInMillis();
	}
	
	public TimeMarker[] getTimeMarkers( long start, long stop )
	{
		start 	*= 1000;								// Discard milliseconds
		stop	*= 1000;
		
		Calendar cMaj	= Calendar.getInstance();
		Calendar cMin	= Calendar.getInstance();
		
		// Set the start calculation point for the grids
		setStartPoint(cMaj, mGridTime, start);
		setStartPoint(cMin, gridTime, start);
		
		// Find first visible grid point
		long minPoint = cMin.getTimeInMillis();
		long majPoint = cMaj.getTimeInMillis();
		
		while ( majPoint < start )
			majPoint = getNextPoint(cMaj, mGridTime, mGridUnits);
		while ( minPoint < start )
			minPoint = getNextPoint(cMin, gridTime, gridUnits);
		
		ArrayList markerList = new ArrayList();
				
		// Marker list does not care in what order the markers are returned, we could
		// get minor and major grid sequentially, but, if we did that, we might draw the marker
		// more than once if the major and minor overlap, which is most likely slower than
		// this way of calculating the markers. 
		//
		// In short: the first while() loop is not *necessary* to get correct results
		while ( minPoint <= stop && majPoint <= stop )
		{
			if ( minPoint < majPoint )
			{
				markerList.add( new TimeMarker( minPoint, "", false ) );
				minPoint = getNextPoint( cMin, gridTime, gridUnits );	
			}
			else if ( minPoint == majPoint )	// Special case, but will happen most of the time
			{
				markerList.add( new TimeMarker( majPoint, df.format(cMaj.getTime()), true ) );
				majPoint = getNextPoint( cMaj, mGridTime, mGridUnits );
				minPoint = getNextPoint( cMin, gridTime, gridUnits );
			}
			else
			{
				markerList.add( new TimeMarker( majPoint, df.format(cMaj.getTime()), true ) );
				majPoint = getNextPoint( cMaj, mGridTime, mGridUnits );
			}
		}

		while ( minPoint <= stop )
		{
			markerList.add( new TimeMarker( minPoint, "", false ) );
			minPoint = getNextPoint( cMin, gridTime, gridUnits );
		}
		
		while ( majPoint <= stop )
		{
			markerList.add( new TimeMarker( majPoint, df.format(cMaj.getTime()), true ) );
			majPoint = getNextPoint( cMaj, mGridTime, mGridUnits );
		}
		
		return (TimeMarker[]) markerList.toArray( new TimeMarker[0] );
	}
	
	
	public long getMajorGridWidth()
	{
		Calendar c 	= Calendar.getInstance();
		long now 	= c.getTimeInMillis() / 1000;
		
		c.add( calendarUnit[mGridTime], mGridUnits );
		
		return (c.getTimeInMillis() / 1000) - now;
	}
}
