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
import jrobin.core.Util;

import java.io.IOException;

/**
 *
 */
abstract class Source {
	protected String name = "";

	private long lastTime, totalTime;
	private double aggMin, aggMax, aggLast;
	private double lastValue, totalValue;

	private RrdTimeSeries series;

	Source() {
		reset();
	}

	Source(String name) {
		this();
		this.name = name;
	}

	String getName() {
		return name;
	}

	abstract void setInterval(long startTime, long endTime) throws RrdException, IOException;
	abstract double getValue(long timestamp, ValueCollection values) throws RrdException;

	double getValueInternal(long timestamp, ValueCollection values) throws RrdException {
		double value = getValue(timestamp, values);
		series.add(timestamp, value);
		aggregate(timestamp, value);
		values.add(name, value);
		return value;
	}

	void setIntervalInternal(long startTime, long endTime) throws RrdException, IOException {
		setInterval(startTime, endTime);
		reset();
	}

	private void aggregate(long time, double value) {
		aggMin = Util.min(aggMin, value);
		aggMax = Util.max(aggMax, value);
		aggLast = value;
		if(!Double.isNaN(lastValue) && !Double.isNaN(value)) {
			long dt = time - lastTime;
			totalValue += dt * (value + lastValue) / 2.0;
			totalTime += dt;
		}
		lastTime = time;
		lastValue = value;
	}

    private void reset() {
		lastTime = totalTime = 0;
		aggMin = aggMax = aggLast = lastValue = Double.NaN;
		totalValue = 0.0;

		series = new RrdTimeSeries(name);
	}

	double getAggregate(String consolFun) throws RrdException {
		if(consolFun.equals("MAX")) {
			return aggMax;
		}
		else if(consolFun.equals("MIN")) {
			return aggMin;
		}
		else if(consolFun.equals("LAST")) {
			return aggLast;
		}
		else if(consolFun.equals("AVERAGE")) {
			if(totalTime > 0) {
				return totalValue / totalTime;
			}
			else {
				return Double.NaN;
			}
		}
		else {
			throw new RrdException("Unsupported aggregation function");
		}
	}

	RrdTimeSeries getSeries() {
		return series;
	}
}


