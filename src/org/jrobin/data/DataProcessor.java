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

import org.jrobin.graph.Plottable;
import org.jrobin.core.*;

import java.util.*;
import java.io.IOException;

public class DataProcessor implements ConsolFuns {
	public static final int DEFAUL_PIXEL_COUNT = 400;

	private int pixelCount = DEFAUL_PIXEL_COUNT;
	private int pixelsPerStep = 1;
	private boolean poolUsed = false;

	final private long tStart, tEnd;
	private double[] timestamps;
	private Map sources = new LinkedHashMap();

	public DataProcessor(long tStart, long tEnd) {
		assert tStart < tEnd: "Invalid time span while constructing DataAnalyzer";
		this.tStart = tStart;
		this.tEnd = tEnd;
		createTimestamps();
	}

	/////////////////////////////////////////////////////////////////
	// BASIC FUNCTIONS
	/////////////////////////////////////////////////////////////////

	private void createTimestamps() {
		timestamps = new double[pixelCount];
		final double span = tEnd - tStart;
		for(int i = 0; i < pixelCount; i++) {
			timestamps[i] = tStart + ((double) i / (double)(pixelCount - 1)) * span;
		}
	}

	public boolean isPoolUsed() {
		return poolUsed;
	}

	public void setPoolUsed(boolean poolUsed) {
		this.poolUsed = poolUsed;
	}

	public int getPixelCount() {
		return pixelCount;
	}

	public void setPixelCount(int pixelCount) {
		this.pixelCount = pixelCount;
		createTimestamps();
	}

	public void setStep(long step) {
		double secondsPerPixel = getSecondsPerPixel();
		pixelsPerStep = Math.max((int) Math.ceil(step / secondsPerPixel), 1);
	}

	private double getSecondsPerPixel() {
		return (double) (tEnd - tStart) / (double) (pixelCount - 1);
	}

	public double[] getTimestamps() {
		return timestamps;
	}

	public double[] getValues(String sourceName) throws RrdException {
		Source source = getSource(sourceName);
		double[] values = source.getValues();
		if(values == null) {
			throw new RrdException("Values not available for source [" + sourceName + "]");
		}
		return values;
	}

	public double getAggregate(String sourceName, String consolFun) throws RrdException {
		Source source = getSource(sourceName);
		return source.getAggregate(consolFun, getSecondsPerPixel());
	}

	public String[] getSourceNames() {
		return (String[]) sources.keySet().toArray(new String[0]);
	}

	private Source getSource(String sourceName) throws RrdException {
		Object source = sources.get(sourceName);
		if(source != null) {
			return (Source) source;
		}
		throw new RrdException("Unknown source: " + sourceName);
	}

	/////////////////////////////////////////////////////////////////
	// DATASOURCE DEFINITIONS
	/////////////////////////////////////////////////////////////////

	public void addDatasource(String name, Plottable plottable) {
		PDef pDef = new PDef(name, plottable);
		sources.put(name, pDef);
	}

	public void addDatasource(String name, Plottable plottable, int index) {
		PDef pDef = new PDef(name, plottable, index);
		sources.put(name, pDef);
	}

	public void addDatasource(String name, Plottable plottable, String sourceName) {
		PDef pDef = new PDef(name, plottable, sourceName);
		sources.put(name, pDef);
	}

	public void addDatasource(String name, String rpnExpression) {
		CDef cDef = new CDef(name, rpnExpression);
		sources.put(name, cDef);
	}

	public void addDatasource(String name, String defName, String consolFun) {
		SDef sDef = new SDef(name, defName, consolFun);
		sources.put(name, sDef);
	}

	public void addDatasource(String name, String file, String dsName, String consolFunc) {
		Def def = new Def(name, file, dsName, consolFunc);
		sources.put(name, def);
	}

	public void addDatasource(String name, String file, String dsName, String consolFunc, String backend) {
		Def def = new Def(name, file, dsName, consolFunc, backend);
		sources.put(name, def);
	}

	/////////////////////////////////////////////////////////////////
	// MAIN FUNCTION
	/////////////////////////////////////////////////////////////////

	public void processData() throws IOException, RrdException {
		calculateDefs();
		calculatePDefs();
		calculateSdefsAndCdefs();
	}

	/////////////////////////////////////////////////////////////////
	// DEFS CALCULATION
	/////////////////////////////////////////////////////////////////

	private void calculateDefs() throws IOException, RrdException {
		Def[] defs = getDefs();
		for (int i = 0; i < defs.length; i++) {
			if (defs[i].getValues() == null) {
				// not fetched yet
				Set dsNames = new HashSet();
				dsNames.add(defs[i].getDsName());
				// look for all other datasources with the same path and the same consolidation function
				for (int j = i + 1; j < defs.length; j++) {
					if (defs[i].isCompatibleWith(defs[j])) {
						dsNames.add(defs[j].getDsName());
					}
				}
				// now we have everything
				RrdDb rrd = null;
				try {
					rrd = getRrd(defs[i]);
					FetchRequest req = rrd.createFetchRequest(defs[i].getConsolFun(), tStart, tEnd);
					req.setFilter(dsNames);
					FetchData data = req.fetchData();
					//System.out.println(data.getAggregate(defs[i].getDsName(), "AVERAGE"));
					double[] values = data.getValues(defs[i].getDsName());
					normalizeDefValues(data.getTimestamps(), values, defs[i]);
					for (int j = i + 1; j < defs.length; j++) {
						if (defs[i].isCompatibleWith(defs[j])) {
							//System.out.println(data.getAggregate(defs[j].getDsName(), "AVERAGE"));
							values = data.getValues(defs[j].getDsName());
							normalizeDefValues(data.getTimestamps(), values, defs[j]);
						}
					}
				}
				finally {
					if (rrd != null) {
						releaseRrd(rrd, defs[i]);
					}
				}
			}
		}
	}

	private void normalizeDefValues(long[] dsTimestamps, double[] dsValues, Def def) {
		double[] values = new double[pixelCount];
		int dsSegment = 1, accumPixels = 0;
		values[0] = (tStart == dsTimestamps[0])? dsValues[0]: dsValues[1];
		double totalValue = 0D, totalTime = 0D;
		for(int pixel = 1; pixel < pixelCount; pixel++) {
			double t0 = timestamps[pixel - 1], t1 = timestamps[pixel];
			while(t0 < t1) {
				double tLimit = Math.min(dsTimestamps[dsSegment], t1);
				double dt = tLimit - t0;
				double val = dsValues[dsSegment];
				if(!Double.isNaN(val)) {
					totalValue += val * dt;
					totalTime += dt;
				}
				t0 = tLimit;
				if(t0 == dsTimestamps[dsSegment]) {
					dsSegment++;
				}
			}
			if(++accumPixels == pixelsPerStep || pixel == pixelCount - 1) {
				double average = (totalTime > 0)? totalValue / totalTime: Double.NaN;
				for(int i = 0; i < accumPixels; i++) {
					values[pixel - i] = average;
				}
				totalValue = totalTime = 0;
				accumPixels = 0;
			}
		}
		def.setValues(values);
	}

	private Def[] getDefs() {
		List defs = new ArrayList();
		Iterator it = sources.values().iterator();
		while (it.hasNext()) {
			Object source = it.next();
			if(source instanceof Def) {
				defs.add(source);
			}
		}
		return (Def[]) defs.toArray(new Def[defs.size()]);
	}

	private RrdDb getRrd(Def def) throws IOException, RrdException {
		String path = def.getPath(), backend = def.getBackend();
		if (poolUsed && backend == null) {
			return RrdDbPool.getInstance().requestRrdDb(path);
		}
		else if (backend != null) {
			return new RrdDb(path, true, RrdBackendFactory.getFactory(backend));
		}
		else {
			return new RrdDb(path, true);
		}
	}

	private void releaseRrd(RrdDb rrd, Def def) throws IOException, RrdException {
		String backend = def.getBackend();
		if (poolUsed && backend == null) {
			RrdDbPool.getInstance().release(rrd);
		}
		else {
			rrd.close();
		}
	}

	/////////////////////////////////////////////////////////////////
	// PLOTTABLE CALCULATION
	/////////////////////////////////////////////////////////////////

	private void calculatePDefs() {
		PDef[] pDefs = getPDefs();
		for(int i = 0; i < pDefs.length; i++) {
			double[] values = new double[pixelCount];
			for(int j = 0; j < pixelCount; j++) {
				double t = timestamps[j];
				values[j] = pDefs[i].getValue(t);
			}
			pDefs[i].setValues(values);
		}
	}

	private PDef[] getPDefs() {
		List pDefs = new ArrayList();
		Iterator it = sources.values().iterator();
		while (it.hasNext()) {
			Object source = it.next();
			if(source instanceof PDef) {
				pDefs.add(source);
			}
		}
		return (PDef[]) pDefs.toArray(new PDef[pDefs.size()]);
	}

	/////////////////////////////////////////////////////////////////
	// SDEFS AND CDEFS
	/////////////////////////////////////////////////////////////////

	private void calculateSdefsAndCdefs() throws RrdException {
		Iterator it = sources.values().iterator();
		while (it.hasNext()) {
			Object source = it.next();
			if(source instanceof SDef) {
				calculateSDef((SDef) source);
			}
			else if(source instanceof CDef) {
				calculateCDef((CDef) source);
			}
		}
	}

	private void calculateCDef(CDef cDef) throws RrdException {
		RpnCalculator calc = new RpnCalculator(cDef.getRpnExpression(), cDef.getName(), this);
		cDef.setValues(calc.calculateValues());
	}

	private void calculateSDef(SDef sDef) throws RrdException {
		String defName = sDef.getDefName();
		String consolFun = sDef.getConsolFun();
		Source source = getSource(defName);
		double value = source.getAggregate(consolFun, getSecondsPerPixel());
		sDef.setValue(value, pixelCount);
	}

	/////////////////////////////////////////////////////////////////
	// TRIVIA
	/////////////////////////////////////////////////////////////////

	/**
	 * Dumps timestamps and values of all datasources in a tabelar form. Very useful for debugging.
	 * @return Dumped object content
	 */
	public String dump() throws RrdException {
		String[] names = getSourceNames();
		double[][] values = new double[names.length][];
		for(int i = 0; i < names.length; i++) {
			values[i] = getValues(names[i]);
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append(fmt("timestamp", 12));
		for(int i = 0; i < names.length; i++) {
			buffer.append(fmt(names[i], 20));
		}
		buffer.append("\n");
		for(int i = 0; i < timestamps.length; i++) {
			long t = (long) Math.round(timestamps[i]);
			buffer.append(fmt("" + t, 12));
			for(int j = 0; j < names.length; j++) {
				buffer.append(fmt(Util.formatDouble(values[j][i]), 20));
			}
			buffer.append("\n");
		}
		return buffer.toString();
	}

	private static String fmt(String s, int length) {
		StringBuffer b = new StringBuffer(s);
		for(int i = 0; i < length - s.length(); i++) {
			b.append(' ');
		}
		return b.toString();
	}

	public static void main(String[] args) throws IOException, RrdException {
		final long t1 = Util.getTimestamp(2003, 4, 1);
		final long t2 = Util.getTimestamp(2003, 5, 1);
		DataProcessor da = new DataProcessor(t1, t2);
		da.addDatasource("x", "demo.rrd", "sun", "AVERAGE");
		da.addDatasource("y", "demo.rrd", "shade", "AVERAGE");
		da.addDatasource("zzzz", "x,y,+,2,/");
		da.addDatasource("w", "PREV(x),PREV(y),+,2,/,1000,/");
		da.setStep(86400);
		da.setPixelCount(100);
		da.processData();
		//System.out.println("x=" + da.getAggregate("x", "AVERAGE"));
		//System.out.println("y=" + da.getAggregate("y", "AVERAGE"));
		//System.out.println(da.getAggregate("y", "MIN"));
		//System.out.println(da.getAggregate("y", "AVERAGE"));
		//System.out.println(da.getAggregate("y", "MAX"));
		System.out.println(da.dump());
	}
}
