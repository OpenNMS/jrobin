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

import java.util.StringTokenizer;

/**
 *
 */
class Cdef extends Source {
	private String[] rpnTokens;

	Cdef(String name, String rpn) {
		super(name);
		StringTokenizer st = new StringTokenizer(rpn, ",");
		int count = st.countTokens();
		rpnTokens = new String[count];
		for(int i = 0; st.hasMoreTokens(); i++) {
			rpnTokens[i] = st.nextToken().trim();
		}
	}

	void setInterval(long start, long end) {
		// stubbed, comlpex graph sources do not require time interval
	}

	double getValue(long timestamp, ValueCollection values) throws RrdException {
		RpnCalculator evaluator = new RpnCalculator(timestamp, values, rpnTokens);
		return evaluator.evaluate();
	}
}
