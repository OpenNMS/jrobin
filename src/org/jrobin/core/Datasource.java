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
 * Class to represent single datasource within RRD file. Each datasource object holds the
 * following information: datasource definition (once set, never changed) and
 * datasource state variables (changed whenever RRD file gets updated).<p>
 *
 * Normally, you don't need to manipluate Datasource objects directly, it's up to
 * JRobin framework to do it for you.
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
 */
public class Datasource implements RrdUpdater {
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
		this.parentDb = parentDb;
		dsName = new RrdString(dsDef.getDsName(), this);
		dsType = new RrdString(dsDef.getDsType(), this);
		heartbeat = new RrdLong(dsDef.getHeartbeat(), this);
		minValue = new RrdDouble(dsDef.getMinValue(), this);
		maxValue = new RrdDouble(dsDef.getMaxValue(), this);
        lastValue = new RrdDouble(Double.NaN, this);
		accumValue = new RrdDouble(0.0, this);
		Header header = parentDb.getHeader();
		nanSeconds = new RrdLong(header.getLastUpdateTime() % header.getStep(), this);
	}

	Datasource(RrdDb parentDb) throws IOException {
		this.parentDb = parentDb;
		dsName = new RrdString(this);
		dsType = new RrdString(this);
		heartbeat = new RrdLong(this);
		minValue = new RrdDouble(this);
		maxValue = new RrdDouble(this);
        lastValue = new RrdDouble(this);
		accumValue = new RrdDouble(this);
		nanSeconds = new RrdLong(this);
	}

	Datasource(RrdDb parentDb, XmlReader reader, int dsIndex) throws IOException, RrdException {
		this.parentDb = parentDb;
		dsName = new RrdString(reader.getDsName(dsIndex), this);
		dsType = new RrdString(reader.getDsType(dsIndex), this);
		heartbeat = new RrdLong(reader.getHeartbeat(dsIndex), this);
		minValue = new RrdDouble(reader.getMinValue(dsIndex), this);
		maxValue = new RrdDouble(reader.getMaxValue(dsIndex), this);
        lastValue = new RrdDouble(reader.getLastValue(dsIndex), this);
		accumValue = new RrdDouble(reader.getAccumValue(dsIndex), this);
		nanSeconds = new RrdLong(reader.getNanSeconds(dsIndex), this);
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
	 * Returns the underlying RrdFile object.
	 * @return Underlying RrdFile object.
	 */
	public RrdFile getRrdFile() {
		return parentDb.getRrdFile();
	}

	/**
	 * Returns datasource name.
	 * @return Datasource name
	 * @throws IOException Thrown in case of IO related error
	 */
	public String getDsName() throws IOException {
		return dsName.get();
	}

	/**
	 * Returns datasource type (GAUGE, COUNTER, DERIVE, ABSOLUTE).
	 *
	 * @return Datasource type.
	 * @throws IOException Thrown in case of IO related error
	 */
	public String getDsType() throws IOException {
		return dsType.get();
	}

	/**
	 * Returns datasource heartbeat
	 *
	 * @return Datasource heartbeat
	 * @throws IOException Thrown in case of IO related error
	 */
	public long getHeartbeat() throws IOException {
		return heartbeat.get();
	}

	/**
	 * Returns mimimal allowed value of the datasource.
	 *
	 * @return Minimal value allowed.
	 * @throws IOException Thrown in case of IO related error
	 */
	public double getMinValue() throws IOException {
		return minValue.get();
	}

	/**
	 * Returns maximal allowed value of the datasource.
	 *
	 * @return Maximal value allowed.
	 * @throws IOException Thrown in case of IO related error
	 */
	public double getMaxValue() throws IOException {
		return maxValue.get();
	}

	/**
	 * Returns last known value of the datasource.
	 *
	 * @return Last datasource value.
	 * @throws IOException Thrown in case of IO related error
	 */
	public double getLastValue() throws IOException {
		return lastValue.get();
	}

	/**
	 * Returns value this datasource accumulated so far.
	 *
	 * @return Accumulated datasource value.
	 * @throws IOException Thrown in case of IO related error
	 */
	public double getAccumValue() throws IOException {
		return accumValue.get();
	}

	/**
	 * Returns the number of accumulated NaN seconds.
	 *
	 * @return Accumulated NaN seconds.
	 * @throws IOException Thrown in case of IO related error
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
        	if(type.equals("GAUGE")) {
				updateValue = newValue;
			}
			else if(type.equals("ABSOLUTE")) {
				if(!Double.isNaN(newValue)) {
					updateValue = newValue / (newTime - oldTime);
				}
			}
			else if(type.equals("DERIVE")) {
				if(!Double.isNaN(newValue) && !Double.isNaN(oldValue)) {
					updateValue = (newValue - oldValue) / (newTime - oldTime);
				}
			}
			else if(type.equals("COUNTER")) {
				if(!Double.isNaN(newValue) && !Double.isNaN(oldValue)) {
					double diff = newValue - oldValue;
					double max32bit = Math.pow(2, 32);
					double max64bit = Math.pow(2, 64);
					if(diff < 0) {
                           diff += max32bit;
					}
					if(diff < 0) {
                           diff += max64bit - max32bit;
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

}
