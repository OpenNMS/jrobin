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

import org.jrobin.core.XmlWriter;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * <p>Class used to determine the chart grid shown on the X (time) axis.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class TimeAxisUnit
{
	// ================================================================
	// -- Members
	// ================================================================	
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

	// Indices in the calendarUnit table
	public static final int SECOND			= 0;				/** constant for seconds */
	public static final int MINUTE			= 1;				/** constant for minutes */
	public static final int HOUR 			= 2;				/** constant for hours */
	public static final int DAY 			= 3;				/** constant for days */
	public static final int WEEK 			= 4;				/** constant for weeks */
	public static final int MONTH 			= 5;				/** constant for months */
	public static final int YEAR 			= 6;				/** constant for years */
	
	// Days of the week
	public static final int MONDAY			= Calendar.MONDAY;
	public static final int TUESDAY			= Calendar.TUESDAY;
	public static final int WEDNESDAY		= Calendar.WEDNESDAY;
	public static final int THURSDAY		= Calendar.THURSDAY;
	public static final int FRIDAY			= Calendar.FRIDAY;
	public static final int SATURDAY		= Calendar.SATURDAY;
	public static final int SUNDAY			= Calendar.SUNDAY;

	private static final String[] UNIT_NAMES = {
		"SECOND", "MINUTE", "HOUR", "DAY", "WEEK", "MONTH", "YEAR"
	};
	
	private static final String[] DAY_NAMES	= {
		"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"
	};

	private int minGridTimeUnit				= HOUR;			// minor grid
	private int minGridUnitSteps			= 1;
	private int majGridTimeUnit				= HOUR;			// major grid
	private int majGridUnitSteps			= 6;
	
	private boolean centerLabels			= false; 
	private SimpleDateFormat dateFormat 	= new SimpleDateFormat("HH:mm", Locale.ENGLISH );
	
	private int firstDayOfWeek				= MONDAY;		// first day of a week
 	
	
	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Creates a TimeAxisUnit object to use as X axis grid specification.
	 * There are both minor and major grid lines, the major lines are accompanied by a time label.
	 * 
	 * To define a grid line you must define a specific time unit, and a number of time steps.
	 * A grid line will appear everey steps*unit.  Possible units are defined in the 
	 * {@link org.jrobin.graph.TimeAxisUnit TimeAxisUnit} class, and are <i>SECOND, MINUTE, HOUR, DAY,
	 * WEEK, MONTH</i> and <i>YEAR</i>.
	 * 
	 * @param minGridTimeUnit Time unit for the minor grid lines.
	 * @param minGridUnitSteps Time unit steps for the minor grid lines.
	 * @param majGridTimeUnit Time unit for the major grid lines.
	 * @param majGridUnitSteps Time unit steps for the major grid lines.
	 * @param dateFormat Format to use to convert the specific time into a label string.
	 * @param centerLabels True if labels (major grid) should be centered between two major grid lines.
	 * @param firstDayOfWeek First day of a calendar week.
	 */
	TimeAxisUnit( int minGridTimeUnit, int minGridUnitSteps,
				  int majGridTimeUnit, int majGridUnitSteps,
				  SimpleDateFormat dateFormat, boolean centerLabels, int firstDayOfWeek )
	{
		this.minGridTimeUnit	= minGridTimeUnit;
		this.minGridUnitSteps	= minGridUnitSteps;
		this.majGridTimeUnit	= majGridTimeUnit;
		this.majGridUnitSteps	= majGridUnitSteps;
		this.dateFormat			= new SimpleDateFormat( dateFormat.toPattern(), Locale.ENGLISH );
		this.centerLabels		= centerLabels;
		this.firstDayOfWeek		= firstDayOfWeek;
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================	
	/**
	 * Returns a set of markers making up the grid for the X axis.
	 * All markers are situated in a given timespan.
	 * @param start Start time in seconds of the timespan.
	 * @param stop End time in seconds of the timespan.
	 * @return List of markers as a TimeMarker array.
	 */
	TimeMarker[] getTimeMarkers( long start, long stop )
	{
		start 	*= 1000;								// Discard milliseconds
		stop	*= 1000;
	
		Calendar cMaj	= Calendar.getInstance();
		Calendar cMin	= Calendar.getInstance();
	
		// Set the start calculation point for the grids
		setStartPoint(cMaj, majGridTimeUnit, start);
		setStartPoint(cMin, minGridTimeUnit, start);
	
		// Find first visible grid point
		long minPoint = cMin.getTimeInMillis();
		long majPoint = cMaj.getTimeInMillis();
	
		while ( majPoint < start )
			majPoint = getNextPoint(cMaj, majGridTimeUnit, majGridUnitSteps);
		while ( minPoint < start )
			minPoint = getNextPoint(cMin, minGridTimeUnit, minGridUnitSteps);
	
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
				minPoint = getNextPoint( cMin, minGridTimeUnit, minGridUnitSteps );
			}
			else if ( minPoint == majPoint )	// Special case, but will happen most of the time
			{
				markerList.add( new TimeMarker( majPoint, dateFormat.format(cMaj.getTime()), true ) );
				majPoint = getNextPoint( cMaj, majGridTimeUnit, majGridUnitSteps );
				minPoint = getNextPoint( cMin, minGridTimeUnit, minGridUnitSteps );
			}
			else
			{
				markerList.add( new TimeMarker( majPoint, dateFormat.format(cMaj.getTime()), true ) );
				majPoint = getNextPoint( cMaj, majGridTimeUnit, majGridUnitSteps );
			}
		}

		while ( minPoint <= stop )
		{
			markerList.add( new TimeMarker( minPoint, "", false ) );
			minPoint = getNextPoint( cMin, minGridTimeUnit, minGridUnitSteps );
		}
	
		while ( majPoint <= stop )
		{
			markerList.add( new TimeMarker( majPoint, dateFormat.format(cMaj.getTime()), true ) );
			majPoint = getNextPoint( cMaj, majGridTimeUnit, majGridUnitSteps );
		}
	
		return (TimeMarker[]) markerList.toArray( new TimeMarker[0] );
	}


	/**
	 * Calculates and returns the number of pixels between two major grid lines.
	 * @return Number of pixels between two major grid lines.
	 */
	long getMajorGridWidth()
	{
		Calendar c 	= Calendar.getInstance();
		long now 	= c.getTimeInMillis() / 1000;
	
		c.add( calendarUnit[majGridTimeUnit], majGridUnitSteps );
	
		return (c.getTimeInMillis() / 1000) - now;
	}

	boolean getCenterLabels() {
		return centerLabels;
	}

	int getMinGridTimeUnit() {
		return minGridTimeUnit;
	}

	int getMinGridUnitSteps() {
		return minGridUnitSteps;
	}

	int getMajGridTimeUnit() {
		return majGridTimeUnit;
	}

	int getMajGridUnitSteps() {
		return majGridUnitSteps;
	}

	boolean isCenterLabels() {
		return centerLabels;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}


	// ================================================================
	// -- Private methods
	// ================================================================	
	/**
	 * Rounds a given exact timestamp to a more rounded timestamp used
	 * for starting grid line calculation.
	 * @param t Calendar to use for calculation.
	 * @param unit Time unit on which to round the value.
	 * @param exactStart Exact timestamp of the time.
	 */
	private void setStartPoint( Calendar t, int unit, long exactStart )
	{
		t.setTimeInMillis( exactStart );
		t.setFirstDayOfWeek( firstDayOfWeek );
		
		for (int i = 0; i < HOUR && i <= unit; i++)
			t.set( calendarUnit[i], 0 );
		
		if ( unit >= HOUR )
			t.set( Calendar.HOUR_OF_DAY, 0 );
		
		if ( unit == WEEK )
			t.set( Calendar.DAY_OF_WEEK, t.getFirstDayOfWeek() );
		else if ( unit == MONTH )
			t.set( Calendar.DAY_OF_MONTH, 1 );
		else if ( unit == YEAR ) 
		{
			t.set( Calendar.DATE, 1 );
			t.set( Calendar.MONTH, 0 );
		}
	}
	
	/**
	 * Retrieves the next grid line point, based on a time unit and time unit step.
	 * @param t Calendar to use for calculation.
	 * @param unit Time unit to add to the given (previous) time.
	 * @param unitSteps Number of times to add the unit to the given (previous) time.
	 * @return Timestamp of the next grid line point.
	 */
	private long getNextPoint( Calendar t, int unit, int unitSteps )
	{
		t.add( calendarUnit[unit], unitSteps );
		
		return t.getTimeInMillis();
	}

	static String getUnitName(int unit) {
		return UNIT_NAMES[unit];
	}
	
	static String getDayName( int dayIndex ) {
		return DAY_NAMES[dayIndex];
	}

	void exportXmlTemplate(XmlWriter xml) {
		xml.startTag("time_axis");
        xml.writeTag("min_grid_time_unit", TimeAxisUnit.getUnitName(getMinGridTimeUnit()));
		xml.writeTag("min_grid_unit_steps", getMinGridUnitSteps());
		xml.writeTag("maj_grid_time_unit", TimeAxisUnit.getUnitName(getMajGridTimeUnit()));
		xml.writeTag("maj_grid_unit_steps", getMajGridUnitSteps());
		xml.writeTag("date_format", getDateFormat().toPattern());
		xml.writeTag("center_labels", getCenterLabels());
		
		if ( firstDayOfWeek != MONDAY )
			xml.writeTag( "first_day_of_week", getDayName(firstDayOfWeek) );
		
		xml.closeTag(); // time_axis
	}

}
