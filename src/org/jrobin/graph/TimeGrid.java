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

import java.text.SimpleDateFormat;

/**
 * <p>Holds specific information about the Time axis grid of the chart.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class TimeGrid 
{
	// ================================================================
	// -- Members
	// ================================================================	
	private long startTime;
	private long endTime;
	
	private TimeAxisUnit tAxis;
	
	
	// ================================================================
	// -- Constructors
	// ================================================================	
	/**
	 * Creates a time grid based on a timespan and possibly a time axis
	 * unit specification.
	 * @param startTime Start time of the timespan.
	 * @param endTime End time of the timespan.
	 * @param tAxis TimeAxisUnit specified to determine the grid lines, if the given
	 * TimeAxisUnit is null, one will be automatically determined.
	 */
	TimeGrid( long startTime, long endTime, TimeAxisUnit tAxis )
	{
		this.startTime 	= startTime;
		this.endTime	= endTime;
		this.tAxis		= tAxis;
		
		// Set an appropriate time axis it not given yet	
		setTimeAxis();
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================
	long getStartTime() {
		return startTime;
	}

	long getEndTime() {
		return endTime;
	}

	TimeMarker[] getTimeMarkers() {
		return tAxis.getTimeMarkers( startTime, endTime );
	}

	long getMajorGridWidth() {
		return tAxis.getMajorGridWidth();
	}

	boolean centerLabels() {
		return tAxis.centerLabels();
	}	
	
	
	// ================================================================
	// -- Private methods
	// ================================================================		
	/**
	 * Determines a good TimeAxisUnit to use for grid calculation.
	 * A decent grid is selected based on the timespan being used in the chart.
	 */
	private void setTimeAxis()
	{
		if ( tAxis != null )
			return;
		
		double days = (endTime - startTime) / 86400.0;

		if ( days <= 0.75 / 24.0 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.MINUTE, 1, TimeAxisUnit.MINUTE, 5, new SimpleDateFormat("HH:mm"), false );
		}
		else if ( days <= 2.0 / 24.0 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.MINUTE, 5, TimeAxisUnit.MINUTE, 10, new SimpleDateFormat("HH:mm"), false );
		}
		else if ( days <= 3.0 / 24.0 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.MINUTE, 5, TimeAxisUnit.MINUTE, 20, new SimpleDateFormat("HH:mm"), false );
		}
		else if ( days <= 5.0 / 24.0 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.MINUTE, 10, TimeAxisUnit.MINUTE, 30, new SimpleDateFormat("HH:mm"), false );
		}
		else if ( days <= 10.0 / 24.0 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.MINUTE, 15, TimeAxisUnit.HOUR, 1, new SimpleDateFormat("HH:mm"), false );
		}
		else if ( days <= 15.0 / 24.0 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.MINUTE, 30, TimeAxisUnit.HOUR, 2, new SimpleDateFormat("HH:mm"), false );
		}
		else if ( days <= 20.0 / 24.0 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.HOUR, 1, TimeAxisUnit.HOUR, 1, new SimpleDateFormat("HH"), true );
		}
		else if ( days <= 36.0 / 24.0 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.HOUR, 1, TimeAxisUnit.HOUR, 4, new SimpleDateFormat("HH:mm"), false );
		}
		else if ( days <= 2 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.HOUR, 2, TimeAxisUnit.HOUR, 6, new SimpleDateFormat("HH:mm"), false );
		}
		else if ( days <= 3 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.HOUR, 3, TimeAxisUnit.HOUR, 12, new SimpleDateFormat("HH:mm"), false );
		}
		else if ( days < 8 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.HOUR, 6, TimeAxisUnit.DAY, 1, new SimpleDateFormat("EEE dd"), true);
		}
		else if ( days <= 14 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.HOUR, 12, TimeAxisUnit.DAY, 1, new SimpleDateFormat("dd"), true );
		}
		else if ( days <= 43 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.DAY, 1, TimeAxisUnit.WEEK, 1, new SimpleDateFormat("'week' ww"), true );
		}
		else if ( days <= 157 ) {
			tAxis = new TimeAxisUnit( TimeAxisUnit.WEEK, 1, TimeAxisUnit.WEEK, 1, new SimpleDateFormat("ww"), true );
		}
		else {
			tAxis = new TimeAxisUnit( TimeAxisUnit.MONTH, 1, TimeAxisUnit.MONTH, 1, new SimpleDateFormat("MMM"), true );
		}
	}
}
