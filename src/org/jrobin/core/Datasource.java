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

package org.jrobin.core;

import java.io.IOException;

/**
 * Class to represent single datasource within RRD. Each datasource object holds the
 * following information: datasource definition (once set, never changed) and
 * datasource state variables (changed whenever RRD gets updated).<p>
 *
 * Normally, you don't need to manipluate Datasource objects directly, it's up to
 * JRobin framework to do it for you.
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
 */

public class Datasource implements RrdUpdater, DsTypes {
	private static final double MAX_32_BIT = Math.pow(2, 32);
	private static final double MAX_64_BIT = Math.pow(2, 64);
	
	private RrdDb parentDb;
	// definition
	private RrdString dsName, dsType;
	private RrdLong heartbeat;
	private RrdDouble minValue, maxValue;

	// state variables
    private RrdDouble lastValue;
	private RrdLong nanSeconds;
	private RrdDouble accumValue;

	Datasource(RrdDb parentDb, DsDef dsDef) throws IOException {
		boolean shouldInitialize = dsDef != null;
		this.parentDb = parentDb;
		dsName = new RrdString(this, true); 	// constant, may be cached
		dsType = new RrdString(this, true);		// constant, may be cached
		heartbeat = new RrdLong(this);
		minValue = new RrdDouble(this);
		maxValue = new RrdDouble(this);
        lastValue = new RrdDouble(this);
		accumValue = new RrdDouble(this);
		nanSeconds = new RrdLong(this);
		if(shouldInitialize) {
			dsName.set(dsDef.getDsName());
			dsType.set(dsDef.getDsType());
			heartbeat.set(dsDef.getHeartbeat());
			minValue.set(dsDef.getMinValue());
			maxValue.set(dsDef.getMaxValue());
        	lastValue.set(Double.NaN);
			accumValue.set(0.0);
			Header header = parentDb.getHeader();
			nanSeconds.set(header.getLastUpdateTime() % header.getStep());
		}
	}

	Datasource(RrdDb parentDb, DataImporter reader, int dsIndex) throws IOException, RrdException {
		this(parentDb, null);
		dsName.set(reader.getDsName(dsIndex));
		dsType.set(reader.getDsType(dsIndex));
		heartbeat.set(reader.getHeartbeat(dsIndex));
		minValue.set(reader.getMinValue(dsIndex));
		maxValue.set(reader.getMaxValue(dsIndex));
        lastValue.set(reader.getLastValue(dsIndex));
		accumValue.set(reader.getAccumValue(dsIndex));
		nanSeconds.set(reader.getNanSeconds(dsIndex));
	}

	String dump() throws IOException {
		return "== DATASOURCE ==\n" +
			"DS:" + dsName.get() + ":" + dsType.get() + ":" +
			heartbeat.get() + ":" + minValue.get() + ":" +
			maxValue.get() + "\nlastValue:" + lastValue.get() +
			" nanSeconds:" + nanSeconds.get() +
			" accumValue:" + accumValue.get() + "\n";
	}

	/**
	 * Returns datasource name.
	 * @return Datasource name
	 * @throws IOException Thrown in case of I/O error
	 */
	public String getDsName() throws IOException {
		return dsName.get();
	}

	/**
	 * Returns datasource type (GAUGE, COUNTER, DERIVE, ABSOLUTE).
	 *
	 * @return Datasource type.
	 * @throws IOException Thrown in case of I/O error
	 */
	public String getDsType() throws IOException {
		return dsType.get();
	}

	/**
	 * Returns datasource heartbeat
	 *
	 * @return Datasource heartbeat
	 * @throws IOException Thrown in case of I/O error
	 */

	public long getHeartbeat() throws IOException {
		return heartbeat.get();
	}

	/**
	 * Returns mimimal allowed value for this datasource.
	 *
	 * @return Minimal value allowed.
	 * @throws IOException Thrown in case of I/O error
	 */
	public double getMinValue() throws IOException {
		return minValue.get();
	}

	/**
	 * Returns maximal allowed value for this datasource.
	 *
	 * @return Maximal value allowed.
	 * @throws IOException Thrown in case of I/O error
	 */
	public double getMaxValue() throws IOException {
		return maxValue.get();
	}

	/**
	 * Returns last known value of the datasource.
	 *
	 * @return Last datasource value.
	 * @throws IOException Thrown in case of I/O error
	 */
	public double getLastValue() throws IOException {
		return lastValue.get();
	}

	/**
	 * Returns value this datasource accumulated so far.
	 *
	 * @return Accumulated datasource value.
	 * @throws IOException Thrown in case of I/O error
	 */
	public double getAccumValue() throws IOException {
		return accumValue.get();
	}

	/**
	 * Returns the number of accumulated NaN seconds.
	 *
	 * @return Accumulated NaN seconds.
	 * @throws IOException Thrown in case of I/O error
	 */
	public long getNanSeconds() throws IOException {
		return nanSeconds.get();
	}

	void process(long newTime, double newValue) throws IOException, RrdException {
		Header header = parentDb.getHeader();
		long step = header.getStep();
		long oldTime = header.getLastUpdateTime();
		long startTime = Util.normalize(oldTime, step);
		long endTime = startTime + step;
		double oldValue = lastValue.get();
		double updateValue = calculateUpdateValue(oldTime, oldValue, newTime, newValue);
		if(newTime < endTime) {
			accumulate(oldTime, newTime, updateValue);
		}
		else {
			// should store something
			long boundaryTime = Util.normalize(newTime, step);
            accumulate(oldTime, boundaryTime, updateValue);
			double value = calculateTotal(startTime, boundaryTime);
			// how many updates?
			long numSteps= (boundaryTime - endTime) / step + 1L;
			// ACTION!
			parentDb.archive(this, value, numSteps);
			// cleanup
			nanSeconds.set(0);
			accumValue.set(0.0);
			accumulate(boundaryTime, newTime, updateValue);
		}
	}

	private double calculateUpdateValue(long oldTime, double oldValue,
										long newTime, double newValue) throws IOException {
		double updateValue = Double.NaN;
		if(newTime - oldTime <= heartbeat.get()) {
			String type = dsType.get();
        	if(type.equals(DT_GAUGE)) {
				updateValue = newValue;
			}
			else if(type.equals(DT_ABSOLUTE)) {
				if(!Double.isNaN(newValue)) {
					updateValue = newValue / (newTime - oldTime);
				}
			}
			else if(type.equals(DT_DERIVE)) {
				if(!Double.isNaN(newValue) && !Double.isNaN(oldValue)) {
					updateValue = (newValue - oldValue) / (newTime - oldTime);
				}
			}
			else if(type.equals(DT_COUNTER)) {
				if(!Double.isNaN(newValue) && !Double.isNaN(oldValue)) {
					double diff = newValue - oldValue;
					if(diff < 0) {
                           diff += MAX_32_BIT;
					}
					if(diff < 0) {
                           diff += MAX_64_BIT - MAX_32_BIT;
					}
					if(diff >= 0) {
						updateValue = diff / (newTime - oldTime);
					}
				}
			}
			if(!Double.isNaN(updateValue)) {
				double minVal = minValue.get();
				double maxVal = maxValue.get();
				if(!Double.isNaN(minVal) && updateValue < minVal) {
            		updateValue = Double.NaN;
				}
				if(!Double.isNaN(maxVal) && updateValue > maxVal) {
            		updateValue = Double.NaN;
				}
			}
		}
		lastValue.set(newValue);
		return updateValue;
	}

	private void accumulate(long oldTime, long newTime, double updateValue) throws IOException {
		if(Double.isNaN(updateValue)) {
			nanSeconds.set(nanSeconds.get() + (newTime - oldTime));
		}
		else {
			accumValue.set(accumValue.get() + updateValue * (newTime - oldTime));
		}
	}

	private double calculateTotal(long startTime, long boundaryTime) throws IOException {
		double totalValue = Double.NaN;
		long validSeconds = boundaryTime - startTime - nanSeconds.get();
		if(nanSeconds.get() <= heartbeat.get() && validSeconds > 0) {
			totalValue = accumValue.get() / validSeconds;
		}
		return totalValue;
	}

    void appendXml(XmlWriter writer) throws IOException {
		writer.startTag("ds");
		writer.writeTag("name", dsName.get());
		writer.writeTag("type", dsType.get());
		writer.writeTag("minimal_heartbeat", heartbeat.get());
		writer.writeTag("min", minValue.get());
		writer.writeTag("max", maxValue.get());
		writer.writeComment("PDP Status");
		writer.writeTag("last_ds", lastValue.get(), "UNKN");
		writer.writeTag("value", accumValue.get());
		writer.writeTag("unknown_sec", nanSeconds.get());
		writer.closeTag();  // ds
	}

	/**
	 * Copies object's internal state to another Datasource object.
	 * @param other New Datasource object to copy state to
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown if supplied argument is not a Datasource object
	 */
	public void copyStateTo(RrdUpdater other) throws IOException, RrdException {
		if(!(other instanceof Datasource)) {
			throw new RrdException(
				"Cannot copy Datasource object to " + other.getClass().getName());
		}
		Datasource datasource = (Datasource) other;
		if(!datasource.dsName.get().equals(dsName.get())) {
			throw new RrdException("Incomaptible datasource names");
		}
		if(!datasource.dsType.get().equals(dsType.get())) {
			throw new RrdException("Incomaptible datasource types");
		}
		datasource.lastValue.set(lastValue.get());
		datasource.nanSeconds.set(nanSeconds.get());
		datasource.accumValue.set(accumValue.get());
	}

	/**
	 * Returns index of this Datasource object in the RRD.
	 * @return Datasource index in the RRD.
	 * @throws IOException Thrown in case of I/O error
	 */
	public int getDsIndex() throws IOException {
		try {
			return parentDb.getDsIndex(dsName.get());
		}
		catch(RrdException e) {
			return -1;
		}
	}

	/**
	 * Sets datasource heartbeat to a new value.
	 * @param heartbeat New heartbeat value
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown if invalid (non-positive) heartbeat value is specified.
	 */
	public void setHeartbeat(long heartbeat) throws RrdException, IOException {
		if(heartbeat < 1L) {
			throw new RrdException("Invalid heartbeat specified: " + heartbeat);
		}
		this.heartbeat.set(heartbeat);
	}

	/**
	 * Sets minimum allowed value for this datasource. If <code>filterArchivedValues</code>
	 * argment is set to true, all archived values less then <code>minValue</code> will
	 * be fixed to NaN.
	 * @param minValue New minimal value. Specify <code>Double.NaN</code> if no minimal
	 * value should be set
	 * @param filterArchivedValues true, if archived datasource values should be fixed;
	 * false, otherwise.
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown if invalid minValue was supplied (not less then maxValue)
	 */
	public void setMinValue(double minValue, boolean filterArchivedValues)
		throws IOException, RrdException {
		double maxValue = this.maxValue.get();
		if(!Double.isNaN(minValue) && !Double.isNaN(maxValue) && minValue >= maxValue) {
			throw new RrdException("Invalid min/max values: " + minValue + "/" + maxValue);
		}
    	this.minValue.set(minValue);
		if(!Double.isNaN(minValue) && filterArchivedValues) {
			int dsIndex = getDsIndex();
			Archive[] archives = parentDb.getArchives();
			for(int i = 0; i < archives.length; i++) {
				archives[i].getRobin(dsIndex).filterValues(minValue, Double.NaN);
			}
		}
	}

	/**
	 * Sets maximum allowed value for this datasource. If <code>filterArchivedValues</code>
	 * argment is set to true, all archived values greater then <code>maxValue</code> will
	 * be fixed to NaN.
	 * @param maxValue New maximal value. Specify <code>Double.NaN</code> if no max
	 * value should be set.
	 * @param filterArchivedValues true, if archived datasource values should be fixed;
	 * false, otherwise.
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown if invalid maxValue was supplied (not greater then minValue)
	 */
	public void setMaxValue(double maxValue, boolean filterArchivedValues)
		throws IOException, RrdException {
		double minValue = this.minValue.get();
		if(!Double.isNaN(minValue) && !Double.isNaN(maxValue) && minValue >= maxValue) {
			throw new RrdException("Invalid min/max values: " + minValue + "/" + maxValue);
		}
    	this.maxValue.set(maxValue);
		if(!Double.isNaN(maxValue) && filterArchivedValues) {
			int dsIndex = getDsIndex();
			Archive[] archives = parentDb.getArchives();
			for(int i = 0; i < archives.length; i++) {
				archives[i].getRobin(dsIndex).filterValues(Double.NaN, maxValue);
			}
		}
	}

	/**
	 * Sets min/max values allowed for this datasource. If <code>filterArchivedValues</code>
	 * argment is set to true, all archived values less then <code>minValue</code> or
	 * greater then <code>maxValue</code> will be fixed to NaN.
	 * @param minValue New minimal value. Specify <code>Double.NaN</code> if no min
	 * value should be set.
	 * @param maxValue New maximal value. Specify <code>Double.NaN</code> if no max
	 * value should be set.
	 * @param filterArchivedValues true, if archived datasource values should be fixed;
	 * false, otherwise.
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown if invalid min/max values were supplied
	 */
	public void setMinMaxValue(double minValue, double maxValue, boolean filterArchivedValues)
		throws IOException, RrdException {
		if(!Double.isNaN(minValue) && !Double.isNaN(maxValue) && minValue >= maxValue) {
			throw new RrdException("Invalid min/max values: " + minValue + "/" + maxValue);
		}
		this.minValue.set(minValue);
    	this.maxValue.set(maxValue);
		if(!(Double.isNaN(minValue) && Double.isNaN(maxValue)) && filterArchivedValues) {
			int dsIndex = getDsIndex();
			Archive[] archives = parentDb.getArchives();
			for(int i = 0; i < archives.length; i++) {
				archives[i].getRobin(dsIndex).filterValues(minValue, maxValue);
			}
		}
	}

	/**
	 * Returns the underlying storage (backend) object which actually performs all
	 * I/O operations.
	 * @return I/O backend object
	 */
	public RrdBackend getRrdBackend() {
		return parentDb.getRrdBackend();
	}

	/**
	 * Required to implement RrdUpdater interface. You should never call this method directly.
	 * @return Allocator object
	 */
	public RrdAllocator getRrdAllocator() {
		return parentDb.getRrdAllocator();
	}
}

