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
 * Class to represent single RRD archive in a RRD file with its internal state.
 * Normally, you don't need methods to manipulate archive objects directly
 * because JRobin framework does it automatically for you.<p>
 *
 * Each archive object consists of three parts: archive definition, archive state objects
 * (one state object for each datasource) and round robin archives (one round robin for
 * each datasource). API (read-only) is provided to access each of theese parts.<p>
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
 */
public class Archive implements RrdUpdater {
	private RrdDb parentDb;
	// definition
	private RrdString consolFun;
	private RrdDouble xff;
	private RrdInt steps, rows; 
	// state
	private Robin[] robins;
	private ArcState[] states;

	// first time creation
	Archive(RrdDb parentDb, ArcDef arcDef) throws IOException {
		this.parentDb = parentDb;
		consolFun = new RrdString(arcDef.getConsolFun(), this);
		xff = new RrdDouble(arcDef.getXff(), this);
		steps = new RrdInt(arcDef.getSteps(), this);
		rows = new RrdInt(arcDef.getRows(), this);
		int n = parentDb.getHeader().getDsCount();
		robins = new Robin[n];
		states = new ArcState[n];
		for(int i = 0; i < n; i++) {
            states[i] = new ArcState(this);
			robins[i] = new Robin(this, rows.get());
		}
	}

	// read from file
	Archive(RrdDb parentDb) throws IOException {
		this.parentDb = parentDb;
		consolFun = new RrdString(this);
		xff = new RrdDouble(this);
		steps = new RrdInt(this);
		rows = new RrdInt(this);
		int n = parentDb.getHeader().getDsCount();
		states = new ArcState[n];
		robins = new Robin[n];
		for(int i = 0; i < n; i++) {
			states[i] = new ArcState(this);
            robins[i] = new Robin(this, rows.get());
		}
	}

	// read from XML
	Archive(RrdDb parentDb, XmlReader reader, int arcIndex) throws IOException, RrdException {
		this.parentDb = parentDb;
		consolFun = new RrdString(reader.getConsolFun(arcIndex), this);
		xff = new RrdDouble(reader.getXff(arcIndex), this);
		steps = new RrdInt(reader.getSteps(arcIndex), this);
		rows = new RrdInt(reader.getRows(arcIndex), this);
		int dsCount = reader.getDsCount();
		states = new ArcState[dsCount];
		robins = new Robin[dsCount];
		for(int dsIndex = 0; dsIndex < dsCount; dsIndex++) {
			// restore state
            states[dsIndex] = new ArcState(this);
			states[dsIndex].setAccumValue(reader.getStateAccumValue(arcIndex, dsIndex));
			states[dsIndex].setNanSteps(reader.getStateNanSteps(arcIndex, dsIndex));
			// restore robins
			robins[dsIndex] = new Robin(this, rows.get());
			double[] values = reader.getValues(arcIndex, dsIndex);
			for(int j = 0; j < values.length; j++) {
				robins[dsIndex].store(values[j]);
			}
		}
	}

	/**
	 * Returns archive time step in seconds. Archive step is equal to RRD file step
	 * multiplied with the number of archive steps.
	 *
	 * @return Archive time step in seconds
	 */
	public long getArcStep() {
		long step = parentDb.getHeader().getStep();
		return step * steps.get();
	} 

	String dump() throws IOException {
		StringBuffer buffer = new StringBuffer("== ARCHIVE ==\n");
		buffer.append("RRA:" + consolFun.get() + ":" + xff.get() + ":" +
			steps.get() + ":" + rows.get() + "\n");
		buffer.append("interval [" + getStartTime() + ", " + getEndTime() + "]" + "\n");
		for(int i = 0; i < robins.length; i++) {
			buffer.append(states[i].dump());
			buffer.append(robins[i].dump());
		}
		return buffer.toString();
	}

	RrdDb getParentDb() {
		return parentDb;
	}

	/**
	 * Returns the underlying RrdFile object.
	 * @return Underlying RrdFile object
	 */
	public RrdFile getRrdFile() {
		return parentDb.getRrdFile();
	}

	void archive(int dsIndex, double value, long numUpdates) throws IOException {
		Robin robin = robins[dsIndex];
		ArcState state = states[dsIndex];
		long step = parentDb.getHeader().getStep();
		long lastUpdateTime = parentDb.getHeader().getLastUpdateTime();
		long updateTime = Util.normalize(lastUpdateTime, step) + step;
		long arcStep = getArcStep();
		// finish current step
		while(numUpdates > 0) {
        	accumulate(state, value);
			numUpdates--;
			if(updateTime % arcStep == 0) {
				finalizeStep(state, robin);
				break;
			}
			else {
				updateTime += step;
			}
		}
		// update robin in bulk
		int bulkUpdateCount = (int) Math.min(numUpdates / steps.get(), (long) rows.get());
		robin.bulkStore(value, bulkUpdateCount);
		// update remaining steps
		long remainingUpdates = numUpdates % steps.get();
		for(long i = 0; i < remainingUpdates; i++) {
			accumulate(state, value);
		}
	}

	private void accumulate(ArcState state, double value) throws IOException {
		if(Double.isNaN(value)) {
			state.setNanSteps(state.getNanSteps() + 1);
		}
		else {
			if(consolFun.get().equals("MIN")) {
				state.setAccumValue(Util.min(state.getAccumValue(), value));
			}
			else if(consolFun.get().equals("MAX")) {
				state.setAccumValue(Util.max(state.getAccumValue(), value));
			}
			else if(consolFun.get().equals("LAST")) {
				state.setAccumValue(value);
			}
			else if(consolFun.get().equals("AVERAGE")) {
				state.setAccumValue(Util.sum(state.getAccumValue(), value));
			}
		}
	}

	private void finalizeStep(ArcState state, Robin robin) throws IOException {
		// should store
		long arcSteps = steps.get();
		long nanSteps = state.getNanSteps();
		double nanPct = (double) nanSteps / (double) arcSteps;
		double accumValue = state.getAccumValue();
		if(nanPct < xff.get() && !Double.isNaN(accumValue)) {
			if(consolFun.get().equals("AVERAGE")) {
				accumValue /= (arcSteps - nanSteps);
			}
			robin.store(accumValue);
		}
		else {
			robin.store(Double.NaN);
		}
		state.setAccumValue(Double.NaN);
		state.setNanSteps(0);
	}

	/**
	 * Returns archive consolidation function (AVERAGE, MIN, MAX or LAST).
	 * @return Archive consolidation function.
	 */
	public String getConsolFun() {
		return consolFun.get();
	}

	/**
	 * Returns archive X-files factor.
	 * @return Archive X-files factor (between 0 and 1).
	 */
	public double getXff() {
		return xff.get();
	}

	/**
	 * Returns the number of archive steps.
	 * @return Number of archive steps.
	 */
	public int getSteps() {
		return steps.get();
	}

	/**
	 * Returns the number of archive rows.
	 * @return Number of archive rows.
	 */
	public int getRows() {
		return rows.get();
	}

	/**
	 * Returns current starting timestamp. This value is not constant.
	 * @return Timestamp corresponding to the first archive row
	 */
	public long getStartTime() {
		long endTime = getEndTime();
		long arcStep = getArcStep();
		long numRows = rows.get();
		return endTime - (numRows - 1) * arcStep;
	}

	/**
	 * Returns current ending timestamp. This value is not constant.
	 * @return Timestamp corresponding to the last archive row
	 */
	public long getEndTime() {
		long arcStep = getArcStep();
		long lastUpdateTime = parentDb.getHeader().getLastUpdateTime();
		return Util.normalize(lastUpdateTime, arcStep);
	}

	/**
	 * Returns the underlying archive state object. Each datasource has its
	 * corresponding ArcState object (archive states are managed independently
	 * for each RRD datasource).
	 * @param dsIndex Datasource index
	 * @return Underlying archive state object
	 */
	public ArcState getArcState(int dsIndex) {
		return states[dsIndex];
	}

	/**
	 * Returns the underlying round robin archive. Robins are used to store actual
	 * archive values on a per-datasource basis.
	 * @param dsIndex Index of the datasource in the RRD file.
	 * @return Underlying round robin archive for the given datasource.
	 */
	public Robin getRobin(int dsIndex) {
		return robins[dsIndex];
	}

	FetchPoint[] fetch(FetchRequest request) throws IOException, RrdException {
		if(request.getFilter() != null) {
			throw new RrdException("fetch() method does not support filtered datasources." +
				" Use fetchData() to get filtered fetch data.");
		}
		long arcStep = getArcStep();
		long fetchStart = Util.normalize(request.getFetchStart(), arcStep);
		long fetchEnd = Util.normalize(request.getFetchEnd(), arcStep);
		if(fetchEnd < request.getFetchEnd()) {
			fetchEnd += arcStep;
		}
		long startTime = getStartTime();
		long endTime = getEndTime();
		int dsCount = robins.length;
		int ptsCount = (int) ((fetchEnd - fetchStart) / arcStep + 1);
		FetchPoint[] points = new FetchPoint[ptsCount];
		for(int i = 0; i < ptsCount; i++) {
			long time = fetchStart + i * arcStep;
			FetchPoint point = new FetchPoint(time, dsCount);
			if(time >= startTime && time <= endTime) {
				int robinIndex = (int)((time - startTime) / arcStep);
				for(int j = 0; j < dsCount; j++) {
					point.setValue(j, robins[j].getValue(robinIndex));
				}
			}
			points[i] = point;
		}
		return points;
	}

	FetchData fetchData(FetchRequest request) throws IOException, RrdException {
		long arcStep = getArcStep();
		long fetchStart = Util.normalize(request.getFetchStart(), arcStep);
		long fetchEnd = Util.normalize(request.getFetchEnd(), arcStep);
		if(fetchEnd < request.getFetchEnd()) {
			fetchEnd += arcStep;
		}
		long startTime = getStartTime();
		long endTime = getEndTime();
		String[] dsToFetch = request.getFilter();
		if(dsToFetch == null) {
			dsToFetch = parentDb.getDsNames();
		}
		int dsCount = dsToFetch.length;
		int ptsCount = (int) ((fetchEnd - fetchStart) / arcStep + 1);
		long[] timestamps = new long[ptsCount];
		double[][] values = new double[dsCount][ptsCount];
		long matchStartTime = Math.max(fetchStart, startTime);
		long matchEndTime = Math.min(fetchEnd, endTime);
		double[][] robinValues = null;
		if(matchStartTime <= matchEndTime) {
			// preload robin values
			int matchCount = (int)((matchEndTime - matchStartTime) / arcStep + 1);
			int matchStartIndex = (int)((matchStartTime - startTime) / arcStep);
			robinValues = new double[dsCount][];
			for(int i = 0; i < dsCount; i++) {
				int dsIndex = parentDb.getDsIndex(dsToFetch[i]);
				robinValues[i] = robins[dsIndex].getValues(matchStartIndex, matchCount);
			}
		}
		for(int ptIndex = 0; ptIndex < ptsCount; ptIndex++) {
			long time = fetchStart + ptIndex * arcStep;
			timestamps[ptIndex] = time;
			for(int i = 0; i < dsCount; i++) {
				double value = Double.NaN;
				if(time >= matchStartTime && time <= matchEndTime) {
					// inbound time
					int robinValueIndex = (int)((time - matchStartTime) / arcStep);
					value = robinValues[i][robinValueIndex];
				}
				values[i][ptIndex] = value;
			}
		}
		FetchData fetchData = new FetchData(this, request);
		fetchData.setTimestamps(timestamps);
		fetchData.setValues(values);
		return fetchData;
	}

    void appendXml(XmlWriter writer) throws IOException {
		writer.startTag("rra");
		writer.writeTag("cf", consolFun.get());
		writer.writeComment(getArcStep() + " seconds");
		writer.writeTag("pdp_per_row", steps.get());
		writer.writeTag("xff", xff.get());
		writer.startTag("cdp_prep");
		for(int i = 0; i < states.length; i++) {
			states[i].appendXml(writer);
		}
		writer.closeTag(); // cdp_prep
		writer.startTag("database");
		long startTime = getStartTime();
		for(int i = 0; i < rows.get(); i++) {
			long time = startTime + i * getArcStep();
			writer.writeComment(Util.getDate(time) + " / " + time);
			writer.startTag("row");
			for(int j = 0; j < robins.length; j++) {
				writer.writeTag("v", robins[j].getValue(i));
			}
			writer.closeTag(); // row
		}
		writer.closeTag(); // database
		writer.closeTag(); // rra
	}

	/**
	 * Copies object's internal state to another Archive object.
	 * @param other New Archive object to copy state to
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown if supplied argument is not an Archive object
	 */
	public void copyStateTo(RrdUpdater other) throws IOException, RrdException {
		if(!(other instanceof Archive)) {
			throw new RrdException(
				"Cannot copy Archive object to " + other.getClass().getName());
		}
		Archive arc = (Archive) other;
		if(!arc.consolFun.get().equals(consolFun.get())) {
			throw new RrdException("Incompatible consolidation functions");
		}
		if(arc.steps.get() != steps.get()) {
			throw new RrdException("Incompatible number of steps");
		}
		int count = parentDb.getHeader().getDsCount();
		for(int i = 0; i < count; i++) {
			int j = Util.getMatchingDatasourceIndex(parentDb, i, arc.parentDb);
			if(j >= 0) {
				states[i].copyStateTo(arc.states[j]);
				robins[i].copyStateTo(arc.robins[j]);
			}
		}
	}

	/**
	 * Sets X-files factor to a new value.
	 * @param xff New X-files factor value. Must be >= 0 and < 1.
	 * @throws RrdException Thrown if invalid value is supplied
	 * @throws IOException Thrown in case of I/O error
	 */
	public void setXff(double xff) throws RrdException, IOException {
		if(xff < 0D || xff >= 1D) {
			throw new RrdException("Invalid xff supplied (" + xff + "), must be >= 0 and < 1");
		}
		this.xff.set(xff);
	}
}
