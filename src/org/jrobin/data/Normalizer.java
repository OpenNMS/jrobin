/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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

package org.jrobin.data;

import org.jrobin.core.Util;
import java.util.Arrays;

class Normalizer {
	private long[] timestamps;
	int count;
	long step;

	Normalizer(long[] timestamps) {
		this.timestamps = timestamps;
		this.step = timestamps[1] - timestamps[0];
		this.count = timestamps.length;
	}

	double[] normalize(long[] rawTimestamps, double[] rawValues) {
		int rawCount = rawTimestamps.length;
		long rawStep = rawTimestamps[1] - rawTimestamps[0];
		// check if we have a simple match
		if(rawCount == count && rawStep == step && rawTimestamps[0] == timestamps[0]) {
			return getCopyOf(rawValues);
		}
		// reset all normalized values to NaN
		double[] values = new double[count];
		Arrays.fill(values, Double.NaN);
		for (int rawSeg = 0, seg = 0; rawSeg < rawCount && seg < count; rawSeg++) {
			double rawValue = rawValues[rawSeg];
			if (!Double.isNaN(rawValue)) {
				long rawLeft = rawTimestamps[rawSeg] - rawStep;
				while (seg < count && rawLeft >= timestamps[seg]) {
					seg++;
				}
				boolean overlap = true;
				for (int fillSeg = seg; overlap && fillSeg < count; fillSeg++) {
					long left = timestamps[fillSeg] - step;
					long t1 = Math.max(rawLeft, left);
					long t2 = Math.min(rawTimestamps[rawSeg], timestamps[fillSeg]);
					if (t1 < t2) {
						values[fillSeg] = Util.sum(values[fillSeg], (t2 - t1) * rawValues[rawSeg]);
					}
					else {
						overlap = false;
					}
				}
			}
		}
		for (int seg = 0; seg < count; seg++) {
			values[seg] /= step;
		}
		return values;
	}

	private static double[] getCopyOf(double[] rawValues) {
		int n = rawValues.length;
		double[] values = new double[n];
		for(int i = 0; i < n; i++) {
			values[i] = rawValues[i];
		}
		return values;
	}

	private static void dump(long[] t, double[] v) {
		for(int i = 0; i < v.length; i++) {
			System.out.print("[" + t[i] + "," + v[i] + "] ");
		}
		System.out.println("");
	}

	public static void main(String[] args) {
		long rawTime[] = {100, 120, 140, 160, 180, 200};
		double rawValues[] = {10, 30, 20, Double.NaN, 50, 40};
		long time[] = {60, 100, 140, 180, 220, 260, 300};
		Normalizer n = new Normalizer(time);
		double[] values = n.normalize(rawTime, rawValues);
		dump(rawTime, rawValues);
		dump(time, values);
	}
}
