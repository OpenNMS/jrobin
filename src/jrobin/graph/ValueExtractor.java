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

import jrobin.core.RrdException;

class ValueExtractor {
    private DataPoint[] points;
	private int pos = 0;

	ValueExtractor(DataPoint[] points) throws RrdException {
		this.points = points;
		if(points.length < 2) {
			throw new RrdException("At least two datapoints are required");
		}
	}

	double getValue(long timestamp) throws RrdException {
		if(timestamp < points[pos].getTime()) {
			throw new RrdException("Backward reading not allowed");
		}
		while(pos < points.length - 1) {
			if(points[pos].getTime() <= timestamp && timestamp < points[pos + 1].getTime()) {
				return points[pos + 1].getValue();
			}
			else {
				pos++;
			}
		}
		return Double.NaN;
	}
}
