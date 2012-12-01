/*******************************************************************************
 * Copyright (c) 2001-2005 Sasa Markovic and Ciaran Treanor.
 * Copyright (c) 2011 The OpenNMS Group, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/

package org.jrobin.data;

import org.jrobin.core.ConsolFuns;
import org.jrobin.core.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Aggregator implements ConsolFuns {
	private long timestamps[], step;
	private double[] values;

	Aggregator(long[] timestamps, double[] values) {
		assert timestamps.length == values.length: "Incompatible timestamps/values arrays (unequal lengths)";
		assert timestamps.length >= 2: "At least two timestamps must be supplied";
		this.timestamps = timestamps;
		this.values = values;
		this.step = timestamps[1] - timestamps[0];
	}

	Aggregates getAggregates(long tStart, long tEnd) {
		Aggregates agg = new Aggregates();
		long totalSeconds = 0;
		boolean firstFound = false;
                double SUMx, SUMxy, SUMxx, SUMyy, slope, y_intercept, correl;
                SUMx = 0.0;
                SUMxy = 0.0;
                SUMxx = 0.0;
                SUMyy = 0.0;

                for (int i = 0; i < timestamps.length; i++) {
			long left = Math.max(timestamps[i] - step, tStart);
			long right = Math.min(timestamps[i], tEnd);
			long delta = right - left;

			// delta is only > 0 when the timestamp for a given buck is within the range of tStart and tEnd
			if (delta > 0) {
				double value = values[i];
				agg.min = Util.min(agg.min, value);
				agg.max = Util.max(agg.max, value);
				if (!firstFound) {
					agg.first = value;
					firstFound = true;
					agg.last = value;
				} else if (delta >= step) {  // an entire bucket is included in this range
					agg.last = value;

					/*
					 * Algorithmically, we're only updating last if it's either the first
					 * bucket encountered, or it's a "full" bucket.

					if ( !isInRange(tEnd, left, right) ||
							 (isInRange(tEnd, left, right) && !Double.isNaN(value))
							 ) {
							agg.last = value;
						}
					*/

				}
				if (!Double.isNaN(value)) {
					agg.total = Util.sum(agg.total, delta * value);
					totalSeconds += delta;
                                        SUMx += step;
                                        SUMxx += step * step;
                                        SUMxy = Util.sum(SUMxy, step * delta * value);
                                        SUMyy = Util.sum(SUMyy, delta * value * delta * value);
				}
			}
		}
		agg.average = totalSeconds > 0 ? (agg.total / totalSeconds) : Double.NaN;

                if (totalSeconds > 0) {
                    double stdevSum = 0.0;
                    for (int i = 0; i < timestamps.length; i++) {
                        long left = Math.max(timestamps[i] - step, tStart);
			long right = Math.min(timestamps[i], tEnd);
			long delta = right - left;

			// delta is only > 0 when the timestamp for a given buck is within the range of tStart and tEnd
			if (delta > 0) {
				double value = values[i];
				if (!Double.isNaN(value)) {
                                        stdevSum += Math.pow(((delta * value) - agg.average), 2.0);
				}
			}
                    }
                    agg.stdev = Math.pow(stdevSum / totalSeconds, 0.5);
                } else {
                    agg.stdev = Double.NaN;
		}

                /* Bestfit line by linear least squares method */
                if (totalSeconds > 0) {
                    agg.lslslope = (SUMx * agg.total - totalSeconds * SUMxy) / (SUMx * SUMx - totalSeconds * SUMxx);
                    agg.lslint = (agg.total - agg.lslslope * SUMx) / totalSeconds;
                    agg.lslcorrel =
                       (SUMxy - (SUMx * agg.total) / totalSeconds) /
                       Math.sqrt((SUMxx - (SUMx * SUMx) / totalSeconds) * (SUMyy - (agg.total * agg.total) / totalSeconds));
                }

                return agg;
	}

	double getPercentile(long tStart, long tEnd, double percentile) {
		List<Double> valueList = new ArrayList<Double>();
		// create a list of included datasource values (different from NaN)
		for (int i = 0; i < timestamps.length; i++) {
			long left = Math.max(timestamps[i] - step, tStart);
			long right = Math.min(timestamps[i], tEnd);
			if (right > left && !Double.isNaN(values[i])) {
				valueList.add(values[i]);
			}
		}
		// create an array to work with
		int count = valueList.size();
		if (count > 1) {
			double[] valuesCopy = new double[count];
			for (int i = 0; i < count; i++) {
				valuesCopy[i] = valueList.get(i);
			}
			// sort array
			Arrays.sort(valuesCopy);
			// skip top (100% - percentile) values
			double topPercentile = (100.0 - percentile) / 100.0;
			count -= (int) Math.ceil(count * topPercentile);
			// if we have anything left...
			if (count > 0) {
				return valuesCopy[count - 1];
			}
		}
		// not enough data available
		return Double.NaN;
	}
}
