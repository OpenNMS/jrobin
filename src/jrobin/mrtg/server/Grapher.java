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
package jrobin.mrtg.server;

import jrobin.core.RrdException;
import jrobin.graph.RrdGraph;
import jrobin.graph.RrdGraphDef;
import jrobin.mrtg.MrtgException;

import java.awt.*;
import java.io.IOException;
import java.util.Date;

class Grapher {
	static final int GRAPH_WIDTH = 502, GRAPH_HEIGHT = 234;

	private String ifDescr, host, alias;

	Grapher(String host, String ifDescr) throws MrtgException {
		this.host = host;
		this.ifDescr = ifDescr;
		this.alias = Server.getInstance().getHardware().
			getRouterByHost(host).getLinkByIfDescr(ifDescr).getIfAlias();
	}

	byte[] getPngGraphBytes(long start, long stop) throws MrtgException {
		RrdGraph graph = getRrdGraph(start, stop);
		try {
			return graph.getPNGBytes(GRAPH_WIDTH, GRAPH_HEIGHT);
		} catch (RrdException e) {
			throw new MrtgException(e);
		} catch (IOException e) {
			throw new MrtgException(e);
		}
	}

	RrdGraph getRrdGraph(long start, long stop) throws MrtgException {
		String filename = Archiver.getRrdFilename(host, ifDescr);
		RrdGraphDef gDef = new RrdGraphDef();
		try {
			gDef.setImageBorder(Color.WHITE, 0);	// Don't show border
			gDef.setTimePeriod(start, stop);
			gDef.setTitle(ifDescr + "@" + host);
			gDef.setVerticalLabel("transfer speed [bits/sec]");
			gDef.datasource("in", filename, "in", "AVERAGE");
			gDef.datasource("out", filename, "out", "AVERAGE");
			gDef.datasource("in8", "in,8,*");
			gDef.datasource("out8", "out,8,*");
			gDef.area("out8", Color.GREEN, "output traffic\n");
			gDef.line("in8", Color.BLUE, "input traffic");
			gDef.comment("\n");
			gDef.gprint("in8", "AVERAGE", "Average input: @7.2 @sbits/s");
			gDef.gprint("in8", "MAX", "Maximum input: @7.2 @Sbits/s\n");
			gDef.gprint("out8", "AVERAGE", "Average output:@7.2 @sbits/s");
			gDef.gprint("out8", "MAX", "Maximum output:@7.2 @Sbits/s\n");
			gDef.comment("\n");
			gDef.comment("Description on device: " + alias);
			gDef.comment("\n");
			gDef.comment("Graph from " + new Date(start * 1000L));
			gDef.comment("to " + new Date(stop * 1000L + 1));
			return new RrdGraph(gDef);
		} catch (RrdException e) {
			throw new MrtgException(e);
		} catch (IOException e) {
			throw new MrtgException(e);
		}
	}
}
