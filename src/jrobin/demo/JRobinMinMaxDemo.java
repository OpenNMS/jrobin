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

package jrobin.demo;

import jrobin.core.*;
import jrobin.graph.RrdGraph;
import jrobin.graph.RrdGraphDef;

import java.io.*;
import java.awt.*;
import java.io.IOException;

/**
 * Class used to compare JRobin and RRDTool. This class is a java version of minmax.pl script
 * found in 'examples' directory of RRDTool. It should only prove that JRobin and RRDTool
 * produce identical graphs after the same sequence of create/update operations.
 */
public class JRobinMinMaxDemo {
	static final String HOME = System.getProperty("user.home");
	static final String SEPARATOR = System.getProperty("file.separator");
	static final String FILE = "demo2";

	private JRobinMinMaxDemo() {

	}

	/**
	 * <d>Runs this demo. This method is just a java translation of minmax.pl example script
	 * found in RRDTool's 'examples' directory. When run, this code generates two
	 * files in your HOME directory: 'demo2.rrd' (RRD file) and 'demo2.png' (graph which
	 * should be compared with RRDTool's 'minmax.png').</p>
	 *
	 * <p>To spare you some time, I'll show you the results here:</p>
	 * <p align="center"><img src="../../../images/minmax_rrdtool.png" border="1"><br>
	 * Original RRDTool graph</p>
	 * <p align="center"><img src="../../../images/minmax.png" border="1"><br>
	 * JRobin graph</p>
	 * <p>Same graphs? If still in doubt, check for yourself.</p>
	 * @param args Not used.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 * @throws IOException Thrown in case of I/O specific error.
	 */
	public static void main(String[] args) throws RrdException, IOException {
		// create the database
		String rrdFile = getFullPath(FILE + ".rrd");
		//String pngFile = getFullPath(FILE + ".png");
		String pngFile = "/zzzzzz.png";
        long start = Util.getTime();
		long end = start + 300 * 300;
        RrdDef rrdDef = new RrdDef(rrdFile, start - 1, 300);
		rrdDef.addDatasource("a", "GAUGE", 600, Double.NaN, Double.NaN);
		rrdDef.addArchive("AVERAGE", 0.5, 1, 300);
		rrdDef.addArchive("MIN", 0.5, 12, 300);
		rrdDef.addArchive("MAX", 0.5, 12, 300);
        RrdDb rrdDb = new RrdDb(rrdDef);
		// update the database
        for(long t = start; t <  end; t += 300) {
            Sample sample = rrdDb.createSample(t);
            sample.setValue("a", Math.sin(t / 3000.0) * 50 + 50);
			sample.update();
		}
		// fetch data
		FetchRequest request = rrdDb.createFetchRequest("MIN", start, end);
		FetchPoint[] points = request.fetch();
		for(int i = 0; i < points.length; i++) {
			System.out.println(points[i].dump());
		}
		// create graph
        RrdGraphDef gDef = new RrdGraphDef();
		gDef.setTimePeriod(start, start + 86400);
        gDef.setTitle("RRDTool's MINMAX.pl demo");
		gDef.setTimeAxisLabel("time");
		gDef.datasource("a", rrdFile, "a", "AVERAGE");
		gDef.datasource("b", rrdFile, "a", "MIN");
		gDef.datasource("c", rrdFile, "a", "MAX");
        gDef.area("a", Color.decode("0xb6e4"), "real");
		gDef.line("b", Color.decode("0x22e9"), "min");
		gDef.line("c", Color.decode("0xee22"), "max");
		//gDef.setBackColor(Color.WHITE);
		RrdGraph graph = new RrdGraph(gDef);
		//graph.saveAsPNG(pngFile, 550, 250);
		//graph.saveAsPNG(pngFile, 0, 0);
		
		byte[] l = graph.getPNGBytes(0, 0);
		System.out.println(l.length);
		FileOutputStream fl = new FileOutputStream("/byte.png");
		fl.write(l);
		fl.close();
		
	}

	static void p(String msg) {
		System.out.println(msg);
	}

	static String getFullPath(String path) {
		return HOME + SEPARATOR + path;
	}
}
