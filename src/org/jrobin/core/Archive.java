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
 * Class to represent single RRD archive in a RRD with its internal state.
 * Normally, you don't need methods to manipulate archive objects directly
 * because JRobin framework does it automatically for you.<p>
 *
 * Each archive object consists of three parts: archive definition, archive state objects
 * (one state object for each datasource) and round robin archives (one round robin for
 * each datasource). API (read-only) is provided to access each of theese parts.<p>
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
 */
public class Archive implements RrdUpdater, ConsolFuns {
	private RrdDb parentDb;
	// definition
	private RrdString consolFun;
	private RrdDouble xff;
	private RrdInt steps, rows; 
	// state
	private Robin[] robins;
	private ArcState[] states;

	Archive(RrdDb parentDb, ArcDef arcDef) throws IOException {
		boolean shouldInitialize = arcDef != null;
		this.parentDb = parentDb;
		consolFun = new RrdString(this);
		xff = new RrdDouble(this);
		steps = new RrdInt(this);
		rows = new RrdInt(this);
		if(shouldInitialize) {
			consolFun.set(arcDef.getConsolFun());
			xff.set(arcDef.getXff());
			steps.set(arcDef.getSteps());
			rows.set(arcDef.getRows());
		}
		int n = parentDb.getHeader().getDsCount();
		states = new ArcState[n];
		robins = new Robin[n];
		for(int i = 0; i < n; i++) {
			states[i] = new ArcState(this, shouldInitialize);
            robins[i] = new Robin(this, rows.get(), shouldInitialize);
		}
	}

	// read from XML
	Archive(RrdDb parentDb, DataImporter reader, int arcIndex) throws IOException, RrdException {
		this(parentDb, new ArcDef(
			reader.getConsolFun(arcIndex), reader.getXff(arcIndex),
			reader.getSteps(arcIndex), reader.getRows(arcIndex)));
		int n = parentDb.getHeader().getDsCount();
		for(int i = 0; i < n; i++) {
			// restore state
			states[i].setAccumValue(reader.getStateAccumValue(arcIndex, i));
			states[i].setNanSteps(reader.getStateNanSteps(arcIndex, i));
			// restore robins
			double[] values = reader.getValues(arcIndex, i);
			robins[i].update(values);
		}
	}

	/**
	 * Returns archive time step in seconds. Archive step is equal to RRD step
	 * multiplied with the number of archive steps.
	 *
	 * @return Archive time step in seconds
	 * @throws IOException Thrown in case of I/O error.
	 */
	public long getArcStep() throws IOException {
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
			if(consolFun.get().equals(CF_MIN)) {
				state.setAccumValue(Util.min(state.getAccumValue(), value));
			}
			else if(consolFun.get().equals(CF_MAX)) {
				state.setAccumValue(Util.max(state.getAccumValue(), value));
			}
			else if(consolFun.get().equals(CF_LAST)) {
				state.setAccumValue(value);
			}
			else if(consolFun.get().equals(CF_AVERAGE)) {
				state.setAccumValue(Util.sum(state.getAccumValue(), value));
			}
		}
	}

	private void finalizeStep(ArcState state, Robin robin) throws IOException {
		// should store
		long arcSteps = steps.get();
		double arcXff = xff.get();
		long nanSteps = state.getNanSteps();
		//double nanPct = (double) nanSteps / (double) arcSteps;
		double accumValue = state.getAccumValue();
		if(nanSteps <= arcXff * arcSteps && !Double.isNaN(accumValue)) {
			if(consolFun.get().equals(CF_AVERAGE)) {
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
	 * Returns archive consolidation function ("AVERAGE", "MIN", "MAX" or "LAST").
	 * @return Archive consolidation function.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public String getConsolFun() throws IOException {
		return consolFun.get();
	}

	/**
	 * Returns archive X-files factor.
	 * @return Archive X-files factor (between 0 and 1).
	 * @throws IOException Thrown in case of I/O error.
	 */
	public double getXff() throws IOException {
		return xff.get();
	}

	/**
	 * Returns the number of archive steps.
	 * @return Number of archive steps.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public int getSteps() throws IOException {
		return steps.get();
	}

	/**
	 * Returns the number of archive rows.
	 * @return Number of archive rows.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public int getRows() throws IOException {
		return rows.get();
	}

	/**
	 * Returns current starting timestamp. This value is not constant.
	 * @return Timestamp corresponding to the first archive row
	 * @throws IOException Thrown in case of I/O error.
	 */
	public long getStartTime() throws IOException {
		long endTime = getEndTime();
		long arcStep = getArcStep();
		long numRows = rows.get();
		return endTime - (numRows - 1) * arcStep;
	}

	/**
	 * Returns current ending timestamp. This value is not constant.
	 * @return Timestamp corresponding to the last archive row
	 * @throws IOException Thrown in case of I/O error.
	 */
	public long getEndTime() throws IOException {
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
	 * @param dsIndex Index of the datasource in the RRD.
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
