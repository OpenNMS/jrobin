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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;

/**
 *
 */
class Header implements RrdUpdater {
	static final String SIGNATURE = "JRobin, version 0.1";
	static final String RRDTOOL_VERSION = "0001";

	private RrdDb parentDb;

	private RrdString signature;
	private RrdLong step;
	private RrdInt dsCount, arcCount;
	private RrdLong lastUpdateTime;

	Header(RrdDb parentDb) throws IOException {
		this.parentDb = parentDb;
		signature = new RrdString(this);
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

	String getSignature() throws IOException {
		return signature.get();
	}

	long getLastUpdateTime() throws IOException {
		return lastUpdateTime.get();
	}

	long getStep() throws IOException {
		return step.get();
	}

	int getDsCount() throws IOException {
		return dsCount.get();
	}

	int getArcCount() throws IOException {
		return arcCount.get();
	}

	void setLastUpdateTime(long lastUpdateTime) throws IOException {
        this.lastUpdateTime.set(lastUpdateTime);
	}

	String dump() throws IOException {
		return "== HEADER ==\n" +
			"signature:" + getSignature() +
			" lastUpdateTime:" + getLastUpdateTime() +
			" step:" + getStep() +
			" dsCount:" + getDsCount() +
			" arcCount:" + getArcCount() + "\n";
	}

	public RrdFile getRrdFile() {
		return parentDb.getRrdFile();
	}

    void appendXml(Element parent) throws IOException {
		Document doc = parent.getOwnerDocument();
		Element versionElem = doc.createElement("version");
		versionElem.appendChild(doc.createTextNode(RRDTOOL_VERSION));
		Element stepElem = doc.createElement("step");
		stepElem.appendChild(doc.createTextNode("" + step.get()));
		Node stepComment = doc.createComment("Seconds");
		Element lastElem = doc.createElement("lastupdate");
		lastElem.appendChild(doc.createTextNode("" + lastUpdateTime.get()));
		Node lastComment = doc.createComment("" + Util.getDate(lastUpdateTime.get()));
		parent.appendChild(versionElem);
		parent.appendChild(stepComment);
		parent.appendChild(stepElem);
        parent.appendChild(lastComment);
		parent.appendChild(lastElem);
	}

}
