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

import jrobin.core.FetchPoint;
import jrobin.core.FetchRequest;
import jrobin.core.RrdDb;
import jrobin.core.RrdException;

import java.io.IOException;

/**
 *
 */
class Def extends Source {
	long start, end;
	private String rrdPath;
	private String dsName;
	private String consolFun;
	private ValueExtractor valueExtractor;

	Def(String name, String rrdPath, String dsName, String consolFun) {
		super(name);
		this.rrdPath = rrdPath;
		this.dsName = dsName;
		this.consolFun = consolFun;
	}
	
	void setValues( FetchPoint[] fetchPoints, int index )
	{
		try {
			
		int numPoints = fetchPoints.length;
		DataPoint[] points = new DataPoint[numPoints];
		
		for(int i = 0; i < numPoints; i++) {
			long time 		= fetchPoints[i].getTime();
			double value 	= fetchPoints[i].getValue(index);
			points[i] 		= new DataPoint(time, value);
		}

		valueExtractor = new ValueExtractor(points);
		
		} catch(Exception e) { e.printStackTrace(); }
	}

	void setInterval(long start, long end) throws RrdException, IOException {
		
		long s1 = Calendar.getInstance().getTimeInMillis();
		
		RrdDb rrd = new RrdDb(rrdPath);
		long rrdStep = rrd.getRrdDef().getStep();
		
		FetchRequest request = rrd.createFetchRequest(consolFun, start, end + rrdStep);
		FetchPoint[] fetchPoints = request.fetch();
		int numPoints = fetchPoints.length;
		
		DataPoint[] points = new DataPoint[numPoints];
		int dsIndex = rrd.getDsIndex(dsName);
		
		for(int i = 0; i < numPoints; i++) {
			long time 		= fetchPoints[i].getTime();
			double value 	= fetchPoints[i].getValue(dsIndex);
			points[i] 		= new DataPoint(time, value);
		}
		rrd.close();
		valueExtractor = new ValueExtractor(points);
		
		long s2 = Calendar.getInstance().getTimeInMillis();
		
		//System.err.println( "Fetched " + dsName + " in " + (s2 - s1) + " ms (source: " + rrdPath + ")" );
	}

	public double getValue(long timestamp, ValueCollection values) throws RrdException {
		if(valueExtractor == null) {
			throw new RrdException("Could not obtain values from graph source [" + name + "]");
		}
		return valueExtractor.getValue(timestamp);
	}
}
