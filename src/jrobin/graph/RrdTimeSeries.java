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

import com.jrefinery.data.SeriesException;
import com.jrefinery.data.TimeSeries;

import java.util.Date;


/**
 *
 */
class RrdTimeSeries extends TimeSeries {
	RrdTimeSeries(String legend) {
		super(legend, RrdSecond.class);
	}

	void add(long timestamp, double value) throws SeriesException {
		RrdSecond second = new RrdSecond(timestamp);
		try {
			add(second, value);
		}
		catch(SeriesException e) {
			System.err.println("Error for " + new Date(timestamp * 1000L) + ": " + e);
			System.exit(-1);
		}
	}

	void fixNaNs(Number newValue) {
        for(int i = 0; i < getItemCount(); i++) {
			if(Double.isNaN(getValue(i).doubleValue())) {
				update(i, newValue);
			}
		}
	}

}
