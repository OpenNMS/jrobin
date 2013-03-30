/*******************************************************************************
 * Copyright (c) 2001-2005 Sasa Markovic and Ciaran Treanor.
 * Copyright (c) 2011 The OpenNMS Group, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/
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
		if (words.length != 2) {
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
		b.append("filename = \"").append(rrd.getPath()).append("\"\n");
		b.append("rrd_version = \"0001\"\n");
		b.append("step = ").append(rrd.getHeader().getStep()).append("\n");
		b.append("last_update = ").append(rrd.getHeader().getLastUpdateTime()).append("\n");
		for (int i = 0; i < rrd.getDsCount(); i++) {
			Datasource ds = rrd.getDatasource(i);
			b.append("ds[").append(ds.getDsName()).append("].type = \"").append(ds.getDsType()).append("\"\n");
			b.append("ds[").append(ds.getDsName()).append("].minimal_heartbeat = ").
					append(ds.getHeartbeat()).append("\n");
			b.append("ds[").append(ds.getDsName()).append("].min = ").append(ds.getMinValue()).append("\n");
			b.append("ds[").append(ds.getDsName()).append("].max = ").append(ds.getMaxValue()).append("\n");
			b.append("ds[").append(ds.getDsName()).append("].last_ds = ").append(ds.getLastValue()).append("\n");
			b.append("ds[").append(ds.getDsName()).append("].value = ").append(ds.getAccumValue()).append("\n");
			b.append("ds[").append(ds.getDsName()).append("].unknown_sec = ").append(ds.getNanSeconds()).append("\n");
		}
		for (int i = 0; i < rrd.getArcCount(); i++) {
			Archive arc = rrd.getArchive(i);
			b.append("rra[").append(i).append("].cf = \"").append(arc.getConsolFun()).append("\"\n");
			b.append("rra[").append(i).append("].rows = ").append(arc.getRows()).append("\n");
			b.append("rra[").append(i).append("].pdp_per_row = ").append(arc.getSteps()).append("\n");
			b.append("rra[").append(i).append("].xff = ").append(arc.getXff()).append("\n");
			for (int j = 0; j < rrd.getDsCount(); j++) {
				b.append("rra[").append(i).append("].cdp_prep[").append(j).append("].value = ").
						append(arc.getArcState(j).getAccumValue()).append("\n");
				b.append("rra[").append(i).append("].cdp_prep[").append(j).append("].unknown_datapoints = ").
						append(arc.getArcState(j).getNanSteps()).append("\n");
			}
		}
		return b.toString();
	}
}
