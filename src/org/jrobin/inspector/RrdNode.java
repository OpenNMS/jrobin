/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
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

package org.jrobin.inspector;

import org.jrobin.core.*;

import java.io.File;
import java.io.IOException;

class RrdNode {
	private int dsIndex = -1, arcIndex = -1;
	private String label;

	RrdNode(RrdDb rrd) {
		// header node
		String path = rrd.getRrdFile().getFilePath();
		label = new File(path).getName();
	}

	RrdNode(RrdDb rrd, int dsIndex) throws IOException, RrdException {
		// datasource node
		this.dsIndex = dsIndex;
		RrdDef def = rrd.getRrdDef();
		DsDef[] dsDefs = def.getDsDefs();
		label = dsDefs[dsIndex].dump();
	}

	RrdNode(RrdDb rrd, int dsIndex, int arcIndex) throws IOException, RrdException {
		// archive node
		this.dsIndex = dsIndex;
		this.arcIndex = arcIndex;
		ArcDef[] arcDefs = rrd.getRrdDef().getArcDefs();
		label = arcDefs[arcIndex].dump();
	}

	int getDsIndex() {
		return dsIndex;
	}

	int getArcIndex() {
		return arcIndex;
	}

	public String toString() {
		return label;
	}
}
