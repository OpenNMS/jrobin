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

abstract class DataImporter {

	// header
	abstract String getVersion() throws RrdException, IOException;
	abstract long getLastUpdateTime() throws RrdException, IOException;
	abstract long getStep() throws RrdException, IOException;
	abstract int getDsCount() throws RrdException, IOException;
	abstract int getArcCount() throws RrdException, IOException;

	// datasource
	abstract String getDsName(int dsIndex) throws RrdException, IOException;
	abstract String getDsType(int dsIndex) throws RrdException, IOException;
	abstract long getHeartbeat(int dsIndex) throws RrdException, IOException;
	abstract double getMinValue(int dsIndex) throws RrdException, IOException;
	abstract double getMaxValue(int dsIndex) throws RrdException, IOException;

	// datasource state
	abstract double getLastValue(int dsIndex) throws RrdException, IOException;
	abstract double getAccumValue(int dsIndex) throws RrdException, IOException;
	abstract long getNanSeconds(int dsIndex) throws RrdException, IOException;

    // archive
	abstract String getConsolFun(int arcIndex) throws RrdException, IOException;
	abstract double getXff(int arcIndex) throws RrdException, IOException;
	abstract int getSteps(int arcIndex) throws RrdException, IOException;
	abstract int getRows(int arcIndex) throws RrdException, IOException;

	// archive state
	abstract double getStateAccumValue(int arcIndex, int dsIndex) throws RrdException, IOException;
	abstract int getStateNanSteps(int arcIndex, int dsIndex) throws RrdException, IOException;
	abstract double[] getValues(int arcIndex, int dsIndex) throws RrdException, IOException;

	long getEstimatedSize() throws RrdException, IOException {
		int dsCount = getDsCount();
		int arcCount = getArcCount();
		int rowCount = 0;
		for(int i = 0; i < arcCount; i++) {
			rowCount += getRows(i);
		}
		return RrdDef.calculateSize(dsCount, arcCount, rowCount);
	}

	void release() throws RrdException, IOException {
		// NOP
	}

}