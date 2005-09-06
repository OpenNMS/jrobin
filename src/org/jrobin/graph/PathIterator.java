/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org)
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
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

class PathIterator {
	private double[] y;
	private int pos = 0;

	PathIterator(double[] y) {
		this.y = y;
	}

	int[] getNextPath() {
		while(pos < y.length) {
			if(Double.isNaN(y[pos])) {
				pos++;
			}
			else {
				int endPos = pos + 1;
				while(endPos < y.length && !Double.isNaN(y[endPos])) {
					endPos++;
				}
				int[] result = { pos, endPos };
				pos = endPos;
				if(result[1] - result[0] >= 2) {
					return result;
				}
			}
		}
		return null;
	}
}
