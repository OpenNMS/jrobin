/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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
 * Class to represent RRD file header. Header information is mainly static (once set, it
 * cannot be changed), with the exception of last update time (this value is changed whenever
 * RRD file gets updated).<p>
 *
 * Normally, you don't need to manipulate the Header object directly - JRobin framework
 * does it for you.<p>
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>*
 */
public class Header implements RrdUpdater {
	static final String SIGNATURE = "JRobin, version 0.1";
	static final String RRDTOOL_VERSION = "0001";

	private RrdDb parentDb;

	private RrdString signature;
	private RrdLong step;
	private RrdInt dsCount, arcCount;
	private RrdLong lastUpdateTime;

	Header(RrdDb parentDb) throws IOException, RrdException {
		this.parentDb = parentDb;
		signature = new RrdString(this);
		if(!signature.get().equals(SIGNATURE)) {
			throw new RrdException("Not a JRobin RRD file");
		}
		step = new RrdLong(this);
		dsCount = new RrdInt(this);
		arcCount = new RrdInt(this);
		lastUpdateTime = new RrdLong(this);
	}

	Header(RrdDb parentDb, RrdDef rrdDef) throws IOException, RrdException {
		this.parentDb = parentDb;
		signature = new RrdString(SIGNATURE, this);
		step = new RrdLong(rrdDef.getStep(), this);
		dsCount = new RrdInt(rrdDef.getDsCount(), this);
		arcCount = new RrdInt(rrdDef.getArcCount(), this);
		lastUpdateTime = new RrdLong(rrdDef.getStartTime(), this);
	}

	Header(RrdDb parentDb, XmlReader reader) throws IOException, RrdException {
		this.parentDb = parentDb;
		String version = reader.getVersion();
		if(!version.equals(RRDTOOL_VERSION)) {
			throw new RrdException("Could not unserilalize xml version " + version);
		}
		signature = new RrdString(SIGNATURE, this);
		step = new RrdLong(reader.getStep(), this);
		dsCount = new RrdInt(reader.getDsCount(), this);
		arcCount = new RrdInt(reader.getArcCount(), this);
		lastUpdateTime = new RrdLong(reader.getLastUpdateTime(), this);
	}

	/**
	 * Returns RRD file signature. The returned string will be always
	 * of the form <b><i>JRobin, version x.x</i></b>. Note: RRD file format did not
	 * change since Jrobin 1.0.0 release (and probably never will).
	 *
	 * @return RRD file signature
	 */
	public String getSignature() {
		return signature.get();
	}

	/**
	 * Returns the last update time of the RRD file.
	 *
	 * @return Timestamp (Unix epoch, no milliseconds) corresponding to the last update time.
	 */
	public long getLastUpdateTime() {
		return lastUpdateTime.get();
	}

	/**
	 * Returns primary RRD file time step.
	 *
	 * @return Primary time step in seconds
	 */
	public long getStep() {
		return step.get();
	}

	/**
	 * Returns the number of datasources defined in the RRD file.
	 *
	 * @return Number of datasources defined
	 */
	public int getDsCount() {
		return dsCount.get();
	}

	/**
	 * Returns the number of archives defined in the RRD file.
	 *
	 * @return Number of archives defined
	 */
	public int getArcCount() {
		return arcCount.get();
	}

	void setLastUpdateTime(long lastUpdateTime) throws IOException {
        this.lastUpdateTime.set(lastUpdateTime);
	}

	String dump() {
		return "== HEADER ==\n" +
			"signature:" + getSignature() +
			" lastUpdateTime:" + getLastUpdateTime() +
			" step:" + getStep() +
			" dsCount:" + getDsCount() +
			" arcCount:" + getArcCount() + "\n";
	}

	/**
	 * Returns the underlying RrdFile object.
	 *
	 * @return Underlying RrdFile object.
	 */
	public RrdFile getRrdFile() {
		return parentDb.getRrdFile();
	}

    void appendXml(XmlWriter writer) throws IOException {
		writer.writeTag("version", RRDTOOL_VERSION);
		writer.writeComment("Seconds");
		writer.writeTag("step", step.get());
		writer.writeComment(Util.getDate(lastUpdateTime.get()));
		writer.writeTag("lastupdate", lastUpdateTime.get());
	}

	/**
	 * Copies object's internal state to another Header object.
	 * @param other New Header object to copy state to
	 * @throws IOException Thrown in case of I/O error
	 * @throws RrdException Thrown if supplied argument is not a Header object
	 */
	public void copyStateTo(RrdUpdater other) throws IOException, RrdException {
		if(!(other instanceof Header)) {
			throw new RrdException(
				"Cannot copy Header object to " + other.getClass().getName());
		}
		Header header = (Header) other; 
		header.lastUpdateTime.set(lastUpdateTime.get());
	}
}
