/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package org.jrobin.cmd;

import org.jrobin.core.RrdException;
import org.jrobin.core.RrdDb;
import org.jrobin.core.Datasource;
import org.jrobin.core.Archive;

import java.io.IOException;

class RrdInfoCmd extends RrdToolCmd {
	String getCmdType() {
		return "info";
	}

	Object execute() throws RrdException, IOException {
		String[] words = getRemainingWords();
		if(words.length != 2) {
			throw new RrdException("Invalid rrdinfo syntax");
		}
		String path = words[1], info;
		RrdDb rrd = getRrdDbReference(path);
		try {
			info = getInfo(rrd);
			println(info);
		}
		finally {
			releaseRrdDbReference(rrd);
		}
		return info;
	}

	private String getInfo(RrdDb rrd) throws IOException {
		StringBuffer b = new StringBuffer();
		b.append("filename = \"" + rrd.getPath() + "\"\n");
		b.append("rrd_version = \"0001\"\n");
		b.append("step = " + rrd.getHeader().getStep() + "\n");
		b.append("last_update = " + rrd.getHeader().getLastUpdateTime() + "\n");
		for(int i = 0; i < rrd.getDsCount(); i++) {
			Datasource ds = rrd.getDatasource(i);
			b.append("ds[" + ds.getDsName() + "].type = \"" + ds.getDsType() + "\"\n");
			b.append("ds[" + ds.getDsName() + "].minimal_heartbeat = " + ds.getHeartbeat() + "\n");
			b.append("ds[" + ds.getDsName() + "].min = " + ds.getMinValue() + "\n");
			b.append("ds[" + ds.getDsName() + "].max = " + ds.getMaxValue() + "\n");
			b.append("ds[" + ds.getDsName() + "].last_ds = " + ds.getLastValue() + "\n");
			b.append("ds[" + ds.getDsName() + "].value = " + ds.getAccumValue() + "\n");
			b.append("ds[" + ds.getDsName() + "].unknown_sec = " + ds.getNanSeconds() + "\n");
		}
		for(int i = 0; i < rrd.getArcCount(); i++) {
			Archive arc = rrd.getArchive(i);
			b.append("rra[" + i + "].cf = \"" + arc.getConsolFun() + "\"\n");
			b.append("rra[" + i + "].rows = " + arc.getRows() + "\n");
			b.append("rra[" + i + "].pdp_per_row = " + arc.getSteps() + "\n");
			b.append("rra[" + i + "].xff = " + arc.getXff() + "\n");
			for(int j = 0; j < rrd.getDsCount(); j++) {
				b.append("rra[" + i + "].cdp_prep[" + j + "].value = " +
						arc.getArcState(j).getAccumValue() + "\n");
				b.append("rra[" + i + "].cdp_prep[" + j + "].unknown_datapoints = " +
						arc.getArcState(j).getNanSteps() + "\n");
			}
		}
		return b.toString();
	}
}
