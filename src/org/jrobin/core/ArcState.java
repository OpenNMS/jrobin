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
 * Class to represent internal RRD archive state for a single datasource. Objects of this
 * class are never manipulated directly, it's up to JRobin framework to manage
 * internal arcihve states.<p>
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
 */
public class ArcState implements RrdUpdater {
	private Archive parentArc;

	private RrdDouble accumValue;
	private RrdLong nanSteps;

	ArcState(Archive parentArc) throws IOException {
		this.parentArc = parentArc;
		if(getRrdFile().getMode() == RrdFile.MODE_CREATE) {
			// should initialize
			Header header = parentArc.getParentDb().getHeader();
			long step = header.getStep();
			long lastUpdateTime = header.getLastUpdateTime();
			long arcStep = parentArc.getArcStep();
			long nan = (Util.normalize(lastUpdateTime, step) -
				Util.normalize(lastUpdateTime, arcStep)) / step;
			accumValue = new RrdDouble(Double.NaN, this);
			nanSteps = new RrdLong(nan, this);
		}
		else {
			accumValue = new RrdDouble(this);
			nanSteps = new RrdLong(this);
		}
	}

	/**
	 * Returns the underlying RrdFile object.
	 * @return Underlying RrdFile object.
	 */
	public RrdFile getRrdFile() {
		return parentArc.getParentDb().getRrdFile();
	}

	String dump() {
		return "accumValue:" + accumValue.get() + " nanSteps:" + nanSteps.get() + "\n";
	}

	void setNanSteps(long value) throws IOException {
		nanSteps.set(value);
	}

	/**
	 * Returns the number of currently accumulated NaN steps.
	 *
	 * @return Number of currently accumulated NaN steps.
	 */
	public long getNanSteps() {
		return nanSteps.get();
	}

	void setAccumValue(double value) throws IOException {
		accumValue.set(value);
	}

	/**
	 * Returns the value accumulated so far.
	 *
	 * @return Accumulated value
	 */
	public double getAccumValue() {
		return accumValue.get();
	}

	/**
	 * Returns the Archive object to which this ArcState object belongs.
	 *
	 * @return Parent Archive object.
	 */
	public Archive getParent() {
		return parentArc;
	}

	void appendXml(XmlWriter writer) throws IOException {
		writer.startTag("ds");
		writer.writeTag("value", accumValue.get());
		writer.writeTag("unknown_datapoints", nanSteps.get());
		writer.closeTag(); // ds
	}

	/**
	 * Copies object's internal state to another ArcState object.
	 * @param other New ArcState object to copy state to
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown if supplied argument is not an ArcState object
	 */
	public void copyStateTo(RrdUpdater other) throws IOException, RrdException {
		if(!(other instanceof ArcState)) {
			throw new RrdException(
				"Cannot copy ArcState object to " + other.getClass().getName());
		}
		ArcState arcState = (ArcState) other;
		arcState.accumValue.set(accumValue.get());
		arcState.nanSteps.set(nanSteps.get());
	}
} 
