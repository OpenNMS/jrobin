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

/**
 * Class which should be used for all calculations based on the data fetched from RRD files. This class
 * supports ordinary DEF datasources (defined in RRD files), CDEF datasources (RPN expressions evaluation),
 * SDEF (static datasources - extension of JRobin) and PDEF (plottables, see
 * {@link org.jrobin.graph.Plottable Plottable} for more information.<p>
 *
 * Typical class usage:<p>
 * <pre>
 * final long t1 = ...
 * final long t2 = ...
 * DataProcessor dp = new DataProcessor(t1, t2);
 * // DEF datasource
 * dp.addDatasource("x", "demo.rrd", "sun", "AVERAGE");
 * // DEF datasource
 * dp.addDatasource("y", "demo.rrd", "shade", "AVERAGE");
 * // CDEF datasource, z = (x + y) / 2
 * dp.addDatasource("z", "x,y,+,2,/");
 * // ACTION!
 * dp.processData();
 * System.out.println(dp.dump());
 * </pre>
 */
public class DataProcessor implements ConsolFuns {
	/**
	 * Constant used to determine the time step used in internal calculations. Note that AVERAGEs and TOTALs
	 * calculated for the data in RRD files are not affected with this value.
 	 */
	public static final int DEFAUL_PIXEL_COUNT = 400;

	private int pixelCount = DEFAUL_PIXEL_COUNT;
	private int pixelsPerStep = 1;
	private boolean poolUsed = false;

	final private long tStart, tEnd;
	private double[] timestamps;
	private Map sources = new LinkedHashMap();

	/**
	 * Creates new DataProcessor object for the given time span.
	 * @param t1 Starting timestamp in seconds without milliseconds
	 * @param t2 Ending timestamp in seconds without milliseconds
	 */
	public DataProcessor(long t1, long t2) {
		assert t1 < t2: "Invalid time span while constructing DataAnalyzer";
		this.tStart = t1;
		this.tEnd = t2;
		createTimestamps();
	}

	/**
	 * Creates new DataProcessor object for the given time span.
	 * @param d1 Starting date
	 * @param d2 Ending date
	 */
	public DataProcessor(Date d1, Date d2) {
		this(Util.getTimestamp(d1), Util.getTimestamp(d2));
	}

	/**
	 * Creates new DataProcessor object for the given time span.
	 * @param gc1 Starting Gregorian calendar date
	 * @param gc2 Ending Gregorian calendar date
	 */
	public DataProcessor(GregorianCalendar gc1, GregorianCalendar gc2) {
		this(Util.getTimestamp(gc1), Util.getTimestamp(gc2));
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

	/**
	 * Returns boolean value representing {@link org.jrobin.core.RrdDbPool RrdDbPool} usage policy.
	 * @return true, if the pool will be used to fetch data from RRD files, false otherwise.
	 */
	public boolean isPoolUsed() {
		return poolUsed;
	}

	/**
	 * Sets the {@link org.jrobin.core.RrdDbPool RrdDbPool} usage policy.
	 * @param poolUsed true, if the pool should be used to fetch data from RRD files, false otherwise.
	 */
	public void setPoolUsed(boolean poolUsed) {
		this.poolUsed = poolUsed;
	}

	/**
	 * Returns the number of pixels (which is used to determine the length of time step
	 * used in internal calculations)
	 * @return Number of pixels (time steps)
	 */
	public int getPixelCount() {
		return pixelCount;
	}

	/**
	 * Sets the number of pixels. This number will be used to determine the length of time step
	 * used in internal calculations. You can freely change this value but, at the same time,
	 * you can be sure that AVERAGE and TOTAL values calculated for datasources in RRD files will not change.
	 * The default number of pixels is defined by {@link #DEFAUL_PIXEL_COUNT}.
	 *
	 * @param pixelCount The number of pixels. If you process RRD data in order to display it on the graph,
	 * this should be the width of your graph.
	 */
	public void setPixelCount(int pixelCount) {
		this.pixelCount = pixelCount;
		createTimestamps();
	}

	/**
	 * Roughly corresponds to the --step option in RRDTool's graph/xport commands. Here is an explanation borrowed
	 * from RRDTool:<p>
	 *
	 * <i>"By default rrdgraph calculates the width of one pixel in the time
	 * domain and tries to get data at that resolution from the RRD. With
	 * this switch you can override this behavior. If you want rrdgraph to
	 * get data at 1 hour resolution from the RRD, then you can set the
	 * step to 3600 seconds. Note, that a step smaller than 1 pixel will
	 * be silently ignored."</i><p>
	 *
	 * I think this option is not that useful, but it's here just for compatibility.<p>
	 *
	 * @param step Time step at which data should be fetched from RRD files.
	 */
	public void setStep(long step) {
		double secondsPerPixel = getSecondsPerPixel();
		pixelsPerStep = Math.max((int) Math.ceil(step / secondsPerPixel), 1);
	}

	private double getSecondsPerPixel() {
		return (double) (tEnd - tStart) / (double) (pixelCount - 1);
	}

	/**
	 * Returns timestamps for all pixels.
	 * @return array of timestamps in seconds - to preserve maximum precision,
	 * this values are not rounded to the nearest second. The length of this array is equal to the
	 * number of pixels.
	 */
	public double[] getTimestamps() {
		return timestamps;
	}

	/**
	 * Returns calculated values for a single datasource. Corresponding timestamps can be obtained from
	 * the {@link #getTimestamps()} method.
	 * @param sourceName Datasource name
	 * @return an array of doubles. The length of this array is equal to the
	 * number of pixels.
	 * @throws RrdException Thrown if invalid datasource name is specified,
	 * or if datasource values are not yet calculated
	 */
	public double[] getValues(String sourceName) throws RrdException {
		Source source = getSource(sourceName);
		double[] values = source.getValues();
		if(values == null) {
			throw new RrdException("Values not available for source [" + sourceName + "]");
		}
		return values;
	}

	/**
	 * Returns aggregated value for a single datasource.
	 *
	 * @param sourceName Datasource name
	 * @param consolFun Consolidation function to be applied to fetched datasource values.
	 *                  Valid consolidation functions are "MIN", "MAX", "LAST", "FIRST", "AVERAGE" and "TOTAL"
	 *                  (these string constants are conveniently defined in the {@link ConsolFuns} class)
	 * @return MIN, MAX, LAST, FIRST, AVERAGE or TOTAL value calculated from the data
	 *         for the given datasource name
	 * @throws RrdException Thrown if invalid datasource name is specified,
	 * or if datasource values are not yet calculated
	 */
	public double getAggregate(String sourceName, String consolFun) throws RrdException {
		Source source = getSource(sourceName);
		return source.getAggregate(consolFun, getSecondsPerPixel());
	}

	/**
	 * Returns array of datasource names defined in this DataProcessor.
	 * @return array of datasource names
	 */
	public String[] getSourceNames() {
		return (String[]) sources.keySet().toArray(new String[0]);
	}

	/**
	 * Returns an array of all datasource values for all datasources. Each row in this two-dimensional
	 * array represents an array of calculated values for a single datasource. The order of rows is the same
	 * as the order in which datasources were added to this DataProcessor object.
	 * @return All datasource values for all datasources. The first index describes is the index of the datasource,
	 * the second index is the index of the datasource value. In other words, the dimension of this array is
	 * <code>numberOfDatasources x number of pixels</code>
	 * @throws RrdException Thrown if invalid datasource name is specified,
	 * or if datasource values are not yet calculated
	 */
	public double[][] getValues() throws RrdException {
		String[] names = getSourceNames();
		double[][] values = new double[names.length][];
		for(int i = 0; i < names.length; i++) {
			values[i] = getValues(names[i]);
		}
		return values;
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

	/**
	 * <p>Adds a custom, {@link org.jrobin.graph.Plottable plottable} datasource (<b>PDEF</b>).
	 * The datapoints should be made available by a class extending
	 * {@link org.jrobin.graph.Plottable Plottable} class.</p>
	 *
	 * @param name source name.
	 * @param plottable class that extends Plottable class and is suited for graphing.
	 */
	public void addDatasource(String name, Plottable plottable) {
		PDef pDef = new PDef(name, plottable);
		sources.put(name, pDef);
	}

	/**
	 * <p>Adds a custom source (<b>PDEF</b>).
	 * The datapoints should be made available by a class extending
	 * {@link org.jrobin.graph.Plottable Plottable}.</p>
	 *
	 * @param name Source name.
	 * @param plottable Class that extends Plottable class and is suited for graphing.
	 * @param index Integer referring to the datasource in the Plottable class.
	 */
	public void addDatasource(String name, Plottable plottable, int index) {
		PDef pDef = new PDef(name, plottable, index);
		sources.put(name, pDef);
	}

	/**
	 * <p>Adds a custom source (<b>PDEF</b>).
	 * The datapoints should be made available by a class extending
	 * {@link org.jrobin.graph.Plottable Plottable}.</p>
	 *
	 * @param name Source name.
	 * @param plottable Class that extends Plottable class and is suited for graphing.
	 * @param sourceName String name referring to the datasource in the Plottable class.
	 */
	public void addDatasource(String name, Plottable plottable, String sourceName) {
		PDef pDef = new PDef(name, plottable, sourceName);
		sources.put(name, pDef);
	}

	/**
	 * <p>Adds complex source (<b>CDEF</b>).
	 * Complex sources are evaluated using the supplied <code>RPN</code> expression.</p>
	 *
	 * <p>Complex source <code>name</code> can be used:</p>
	 * <ul>
	 * <li>To specify sources for line, area and stack plots.</li>
	 * <li>To define other complex sources.</li>
	 * </ul>
	 *
	 * <p>JRobin supports the following RPN functions, operators and constants: +, -, *, /,
	 * %, SIN, COS, LOG, EXP, FLOOR, CEIL, ROUND, POW, ABS, SQRT, RANDOM, LT, LE, GT, GE, EQ,
	 * IF, MIN, MAX, LIMIT, DUP, EXC, POP, UN, UNKN, NOW, TIME, PI, E,
	 * AND, OR, XOR, PREV, PREV(sourceName), INF, NEGINF, STEP, YEAR, MONTH, DATE,
	 * HOUR, MINUTE, SECOND and WEEK.</p>
	 *
	 * <p>JRobin does not force you to specify at least one simple source name as RRDTool.</p>
	 *
	 * <p>For more details on RPN see RRDTool's
	 * <a href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/manual/rrdgraph.html" target="man">
	 * rrdgraph man page</a>.</p>
	 *
	 * @param name source name.
	 * @param rpnExpression RPN expression containig comma (or space) delimited simple and complex
	 * source names, RPN constants, functions and operators.
	 */
	public void addDatasource(String name, String rpnExpression) {
		CDef cDef = new CDef(name, rpnExpression);
		sources.put(name, cDef);
	}

	/**
	 * <p>Adds static source (<b>SDEF</b>). Static sources are the result of a consolidation function applied
	 * to *any* other source that has been defined previously.</p>
	 *
	 * @param name source name.
	 * @param defName Name of the datasource to calculate the value from.
	 * @param consolFun Consolidation function to use for value calculation
	 */
	public void addDatasource(String name, String defName, String consolFun) {
		SDef sDef = new SDef(name, defName, consolFun);
		sources.put(name, sDef);
	}

	/**
	 * <p>Adds simple datasource (<b>DEF</b>). Simple source <code>name</code>
	 * can be used:</p>
	 * <ul>
	 * <li>To specify sources for line, area and stack plots.</li>
	 * <li>To define complex sources
	 * </ul>
	 *
	 * @param name source name.
	 * @param file Path to RRD file.
	 * @param dsName Datasource name defined in the RRD file.
	 * @param consolFunc Consolidation function that will be used to extract data from the RRD
	 * file ("AVERAGE", "MIN", "MAX" or "LAST" - these string constants are conveniently defined
	 * in the {@link org.jrobin.core.ConsolFuns ConsolFuns} class).
	 */
	public void addDatasource(String name, String file, String dsName, String consolFunc) {
		Def def = new Def(name, file, dsName, consolFunc);
		sources.put(name, def);
	}

	/**
	 * <p>Adds simple source (<b>DEF</b>). Source <code>name</code> can be used:</p>
	 * <ul>
	 * <li>To specify sources for line, area and stack plots.</li>
	 * <li>To define complex sources
	 * </ul>
	 *
	 * @param name Source name.
	 * @param file Path to RRD file.
	 * @param dsName Data source name defined in the RRD file.
	 * @param consolFunc Consolidation function that will be used to extract data from the RRD
	 * file ("AVERAGE", "MIN", "MAX" or "LAST" - these string constants are conveniently defined
	 * in the {@link org.jrobin.core.ConsolFuns ConsolFuns} class).
	 * @param backend Name of the RrdBackendFactory that should be used for this RrdDb.
	 */
	public void addDatasource(String name, String file, String dsName, String consolFunc, String backend) {
		Def def = new Def(name, file, dsName, consolFunc, backend);
		sources.put(name, def);
	}

	/////////////////////////////////////////////////////////////////
	// MAIN FUNCTION
	/////////////////////////////////////////////////////////////////

	/**
	 * Method that should be called once all datasources are defined. Data will be fetched from
	 * RRD files, RPN expressions will be calculated, etc.
	 * @throws IOException Thrown in case of I/O error (while fetching data from RRD files)
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
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
	 * @return Dumped object content.
	 * @throws RrdException Thrown if nothing is calculated so far (the method {@link #processData()}
	 * was not called).
	 */
	public String dump() throws RrdException {
		String[] names = getSourceNames();
		double[][] values = getValues();
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
/*
	public static void main(String[] args) throws IOException, RrdException {
		final long t1 = Util.getTimestamp(2003, 4, 1);
		final long t2 = Util.getTimestamp(2003, 5, 1);
		DataProcessor dp = new DataProcessor(t1, t2);
		// DEF datasource
		dp.addDatasource("x", "demo.rrd", "sun", "AVERAGE");
		// DEF datasource
		dp.addDatasource("y", "demo.rrd", "shade", "AVERAGE");
		// CDEF datasource
		dp.addDatasource("z", "x,y,+,2,/");
		// SDEF datasource
		dp.addDatasource("w", "z", "AVERAGE");
		// CDEF datasource
		dp.addDatasource("wz", "w,z,-");
		// SDEF datasource, values should be close to zero
		dp.addDatasource("wzavg", "wz", "AVERAGE");
		// action!
		dp.processData();
		System.out.println(dp.dump());
	}
//*/
}
