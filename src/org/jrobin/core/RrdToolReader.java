/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *
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

import org.jrobin.core.jrrd.RRDatabase;

import java.io.IOException;

class RrdToolReader extends DataImporter {
	private RRDatabase rrd;

	RrdToolReader(String rrdPath) throws IOException {
		rrd = new RRDatabase(rrdPath);
	}

	String getVersion() {
		return rrd.getHeader().getVersion();
	}

	long getLastUpdateTime() {
		return Util.getTimestamp(rrd.getLastUpdate());
	}

	long getStep() {
		return rrd.getHeader().getPDPStep();
	}

	int getDsCount() {
		return rrd.getHeader().getDSCount();
	}

	int getArcCount() throws RrdException, IOException {
		return rrd.getNumArchives();
	}

	String getDsName(int dsIndex) {
		return rrd.getDataSource(dsIndex).getName();
	}

	String getDsType(int dsIndex) {
		return rrd.getDataSource(dsIndex).getType().toString();
	}

	long getHeartbeat(int dsIndex) {
		return rrd.getDataSource(dsIndex).getMinimumHeartbeat();
	}

	double getMinValue(int dsIndex) {
		return rrd.getDataSource(dsIndex).getMinimum();
	}

	double getMaxValue(int dsIndex) {
		return rrd.getDataSource(dsIndex).getMaximum();
	}

	double getLastValue(int dsIndex) {
		String valueStr = rrd.getDataSource(dsIndex).getPDPStatusBlock().getLastReading();
		return Util.parseDouble(valueStr);
	}

	double getAccumValue(int dsIndex) {
		return rrd.getDataSource(dsIndex).getPDPStatusBlock().getValue();
	}

	long getNanSeconds(int dsIndex) {
		return rrd.getDataSource(dsIndex).getPDPStatusBlock().getUnknownSeconds();
	}

	String getConsolFun(int arcIndex) {
		return rrd.getArchive(arcIndex).getType().toString();
	}

	double getXff(int arcIndex) {
		return rrd.getArchive(arcIndex).getXff();
	}

	int getSteps(int arcIndex) {
		return rrd.getArchive(arcIndex).getPdpCount();
	}

	int getRows(int arcIndex) throws RrdException, IOException {
		return rrd.getArchive(arcIndex).getRowCount();
	}

	double getStateAccumValue(int arcIndex, int dsIndex) throws RrdException, IOException {
		return rrd.getArchive(arcIndex).getCDPStatusBlock(dsIndex).getValue();
	}

	int getStateNanSteps(int arcIndex, int dsIndex) throws RrdException, IOException {
		return rrd.getArchive(arcIndex).getCDPStatusBlock(dsIndex).getUnknownDatapoints();
	}

	double[] getValues(int arcIndex, int dsIndex) throws RrdException, IOException {
		return rrd.getArchive(arcIndex).getValues()[dsIndex];
	}

	void release() throws IOException {
		if (rrd != null) {
			rrd.close();
			rrd = null;
		}
	}

	protected void finalize() throws Throwable {
		super.finalize();
		release();
	}
}
