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

package jrobin.core;

import java.io.IOException;

// TODO: FIX JAVADOC, CLASS MADE PUBLIC
public class Archive implements RrdUpdater {
	private RrdDb parentDb;
	// definition
	private RrdString consolFun;
	private RrdDouble xff;
	private RrdInt steps, rows;
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
            states[i] = new ArcState(this, true);
			robins[i] = new Robin(this, rows.get(), true);
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
			states[i] = new ArcState(this, false);
            robins[i] = new Robin(this, rows.get(), false);
		}
	}

	Archive(RrdDb parentDb, XmlReader reader, int arcIndex) throws IOException, RrdException {
		this.parentDb = parentDb;
		consolFun = new RrdString(reader.getConsolFun(arcIndex), this);
		xff = new RrdDouble(reader.getXff(arcIndex), this);
		steps = new RrdInt(reader.getSteps(arcIndex), this);
		rows = new RrdInt(reader.getRows(arcIndex), this);
		int dsCount = reader.getDsCount();
		robins = new Robin[dsCount];
		states = new ArcState[dsCount];
		for(int dsIndex = 0; dsIndex < dsCount; dsIndex++) {
			// restore state
            states[dsIndex] = new ArcState(this, true);
			states[dsIndex].setAccumValue(reader.getStateAccumValue(arcIndex, dsIndex));
			states[dsIndex].setNanSteps(reader.getStateNanSteps(arcIndex, dsIndex));
			// restore robins
			robins[dsIndex] = new Robin(this, rows.get(), true);
			double[] values = reader.getValues(arcIndex, dsIndex);
			for(int j = 0; j < values.length; j++) {
				robins[dsIndex].store(values[j]);
			}
		}
	}

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
		long bulkUpdates = Math.min(numUpdates / steps.get(), (long) rows.get());
		for(long i = 0; i < bulkUpdates; i++) {
			robin.store(value);
		}
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
		if(nanPct <= xff.get() && !Double.isNaN(accumValue)) {
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

	public String getConsolFun() throws IOException {
		return consolFun.get();
	}

	public double getXff() throws IOException {
		return xff.get();
	}

	public int getSteps() throws IOException {
		return steps.get();
	}

	public int getRows() throws IOException{
		return rows.get();
	}

	public long getStartTime() throws IOException {
		long endTime = getEndTime();
		long arcStep = getArcStep();
		long numRows = rows.get();
		return endTime - (numRows - 1) * arcStep;
	}

	public long getEndTime() throws IOException {
		long arcStep = getArcStep();
		long lastUpdateTime = parentDb.getHeader().getLastUpdateTime();
		return Util.normalize(lastUpdateTime, arcStep);
	}

	public ArcState getArcState(int dsIndex) {
		return states[dsIndex];
	}

	public Robin getRobin(int dsIndex) {
		return robins[dsIndex];
	}

	FetchPoint[] fetch(FetchRequest request) throws IOException, RrdException {
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

}
