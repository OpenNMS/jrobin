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
import org.jrobin.core.FetchData;
import org.jrobin.core.RrdException;

import java.io.IOException;

class Def extends Source {
	private String path, dsName, consolFun, backend;
	private FetchData fetchData;
	private long lastValidTimestamp, fetchStep;

	Def(String name, String path, String dsName, String consolFunc) {
		this(name, path, dsName, consolFunc, null);
	}

	Def(String name, String path, String dsName, String consolFunc, String backend) {
		super(name);
		this.path = path;
		this.dsName = dsName;
		this.consolFun = consolFunc;
		this.backend = backend;
	}

	String getPath() {
		return path;
	}

	String getCanonicalPath() throws IOException {
		return Util.getCanonicalPath(path);
	}

	String getDsName() {
		return dsName;
	}

	String getConsolFun() {
		return consolFun;
	}

	String getBackend() {
		return backend;
	}

	boolean isCompatibleWith(Def def) throws IOException {
		return getCanonicalPath().equals(def.getCanonicalPath()) &&
				getConsolFun().equals(def.consolFun) &&
				((backend == null && def.backend == null) ||
				(backend != null && def.backend != null && backend.equals(def.backend)));
	}

	void setFetchData(FetchData fetchData) throws IOException {
		this.fetchData = fetchData;
		this.lastValidTimestamp = fetchData.getMatchingArchive().getEndTime();
		this.fetchStep = fetchData.getMatchingArchive().getArcStep(); 
	}

	long[] getRrdTimestamps() {
		return fetchData.getTimestamps();
	}

	double[] getRrdValues() throws RrdException {
		return fetchData.getValues(dsName);
	}

	long getLastValidTimestamp() {
		return lastValidTimestamp;
	}

	long getFetchStep() {
		return fetchStep;
	}

	Aggregates getAggregates(long tStart, long tEnd) throws RrdException {
		long[] t = getRrdTimestamps();
		double[] v = getRrdValues();
		Aggregator agg = new Aggregator(t, v);
		return agg.getAggregates(tStart, tEnd);
	}

	double get95Percentile(long tStart, long tEnd) throws RrdException {
		long[] t = getRrdTimestamps();
		double[] v = getRrdValues();
		Aggregator agg = new Aggregator(t, v);
		return agg.get95Percentile(tStart, tEnd);
	}
}
