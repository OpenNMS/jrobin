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

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.GregorianCalendar;

/**
 * <p>Class used to demonstrate almost all of JRobin capabilities. To run this
 * demonstration execute the following command:</p>
 *
 * <pre>
 * java -jar JRobin-{version}.jar
 * </pre>
 *
 * <p>The jar-file can be found in the <code>libs</code> directory of this distribution.
 * <b>On Linux platforms, graphs cannot be generated if your computer has no X-windows
 * server up and running</b> (you'll end up with a nasty looking exception).</p>
 *
 * @author <a href="mailto:saxon@eunet.yu">Sasa Markovic</a>
 */
public class JRobinDemo {
	static final String HOME = System.getProperty("user.home");
	static final String SEPARATOR = System.getProperty("file.separator");
	static final String FILE = "demo";
	static final GregorianCalendar START = new GregorianCalendar(2003, 4, 1);
	static final GregorianCalendar END = new GregorianCalendar(2003, 5, 1);
	static final int MAX_STEP = 240;

/**
 * <p>Runs the demonstration.
 * Demonstration consists of the following steps: (all files will be
 * created in your HOME directory):</p>
 *
 * <ul>
 * <li>Sample RRD database 'demo.rrd' is created (uses two GAUGE data sources).
 * <li>RRD file is updated more than 20.000 times, simulating one month of updates with
 * time steps of max. 4 minutes.
 * <li>Last update time is printed.
 * <li>Sample fetch request is executed, fetching data from RRD file for the whole month.
 * <li>Fetched data is printed on the screen.
 * <li>RRD file is dumped to file 'demo.xml' (XML format).
 * <li>New RRD file 'demo_restored.rrd' is created from XML file.
 * <li>RRD graph for the whole month is created in memory.
 * <li>Graph is saved in PNG and JPEG format (files 'demo.png' and 'demo.jpeg').
 * <li>All files are closed.
 * <li>Log file 'demo.log' is created. Log file consist of RRDTool commands - execute this file
 * as Linux shell script to compare RRDTool's and Jrobin's fetch results (should be exactly the
 * same).
 * </ul>
 *
 * <p>If everything goes well, no exceptions are thrown and your graphs should look like:</p>
 * <p align="center"><img src="../../../images/demo.png" border="1"></p>
 * <p>Red and blue lines are simple (DEF) graph sources. Green line (median value)
 * represents complex (CDEF) graph source which is defined by a RPN expression. Filled
 * area (orange color) represents the difference between data source values. Sine line is here
 * just to represent JRobin's advanced graphing capabilities.</p>
 *
 * @param args Empty. This demo does not accept command line parameters.
 * @throws RrdException Thrown in case of JRobin specific error.
 * @throws IOException Thrown in case of I/O related error.
 */
	public static void main(String[] args) throws RrdException, IOException {
		// setup
		println("==Starting demo");
		RrdDb.setLockMode(RrdDb.WAIT_IF_LOCKED);

		long startMillis = System.currentTimeMillis();
		long start = START.getTime().getTime() / 1000L;
		long end = END.getTime().getTime() / 1000L;
		String rrdFile = getFullPath(FILE + ".rrd");
		String xmlFile = getFullPath(FILE + ".xml");
		String rrdFile2 = getFullPath(FILE + "_restored.rrd");
		String pngFile = getFullPath(FILE + ".png");
		String jpegFile = getFullPath(FILE + ".jpeg");
		String logFile = getFullPath(FILE + ".log");
		PrintWriter pw = new PrintWriter(
			new BufferedOutputStream(
			new FileOutputStream(logFile, false))
		);

		// creation
		println("==Creating RRD file " + rrdFile);
		RrdDef rrdDef = new RrdDef(rrdFile, start - 1, 300);
		rrdDef.addDatasource("sun", "GAUGE", 600, 0, Double.NaN);
		rrdDef.addDatasource("shade", "GAUGE", 600, 0, Double.NaN);
		rrdDef.addArchive("AVERAGE", 0.5, 1, 600);
		rrdDef.addArchive("AVERAGE", 0.5, 6, 700);
		rrdDef.addArchive("AVERAGE", 0.5, 24, 797);
		rrdDef.addArchive("AVERAGE", 0.5, 288, 775);
		rrdDef.addArchive("MAX", 0.5, 1, 600);
		rrdDef.addArchive("MAX", 0.5, 6, 700);
		rrdDef.addArchive("MAX", 0.5, 24, 797);
		rrdDef.addArchive("MAX", 0.5, 288, 775);
		println(rrdDef.dump());
		pw.println(rrdDef.dump());
		RrdDb rrdDb = new RrdDb(rrdDef);
		println("==RRD file created.");

		// update database
		GaugeSource sunSource = new GaugeSource(1200, 20);
		GaugeSource shadeSource = new GaugeSource(300, 10);
		println("==Simulating one month of RRD file updates with step not larger than " +
			MAX_STEP + " seconds (* denotes 1000 updates)");
		long t = start; int n = 0;
		while(t <= end) {
			Sample sample = rrdDb.createSample(t);
			sample.setValue("sun", sunSource.getValue());
			sample.setValue("shade", shadeSource.getValue());
			pw.println(sample.dump());
			sample.update();
			t += Math.random() * MAX_STEP + 1;
			if(((++n) % 1000) == 0) {
				System.out.print("*");
			};
		}
		System.out.println("");
		println("==Finished. RRD file updated " + n + " times");
		println("==Last update time was: " + rrdDb.getLastUpdateTime());

		// fetch data
		println("==Fetching data for the whole month");
		FetchRequest request = rrdDb.createFetchRequest("AVERAGE", start, end);
		println(request.dump());
		pw.println(request.dump());
		FetchPoint[] points = request.fetch();
		println("==Data fetched. " + points.length + " points obtained");
		for(int i = 0; i < points.length; i++) {
			println(points[i].dump());
		}
		println("==Fetch completed");
		println("==Dumping RRD file to XML file " + xmlFile + " (can be restored with RRDTool)");
		rrdDb.dumpXml(xmlFile);
		println("==Creating RRD file " + rrdFile2 + " from " + xmlFile);
		RrdDb rrdDb2 = new RrdDb(rrdFile2, xmlFile);
		// close files
		println("==Closing both RRD files");
		rrdDb.close();
		rrdDb2.close();

		// creating graph
		println("==Creating graph from the second file");
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setTimePeriod(start, end);
		gDef.setTimeAxisLabel("day in month");
        gDef.setTitle("Temperatures in May 2003");
		gDef.setValueAxisLabel("temperature");
		gDef.datasource("sun", rrdFile2, "sun", "AVERAGE");
		gDef.datasource("shade", rrdFile2, "shade", "AVERAGE");
		gDef.datasource("median", "sun,shade,+,2,/");
		gDef.datasource("diff", "sun,shade,-,ABS,-1,*");
		gDef.datasource("sine", "TIME," + start + ",-," + (end - start) +
			",/,2,PI,*,*,SIN,1000,*");
		gDef.line("sun", Color.RED, "sun temp");
		gDef.line("shade", Color.BLUE, "shade temp");
		gDef.line("median", Color.GREEN, "median value");
		gDef.area("diff", Color.ORANGE, "difference");
		gDef.line("sine", Color.CYAN, "sine function demo");
		gDef.gprint("sun", "MAX", "maxSun = @3@s");
		gDef.gprint("sun", "AVERAGE", "avgSun = @3@S@r");
		gDef.gprint("shade", "MAX", "maxShade = @3@s");
		gDef.gprint("shade", "AVERAGE", "avgShade = @3@S@r");
		RrdGraph graph = new RrdGraph(gDef);
		println("==Graph created");
		println("==Saving graph as PNG file " + pngFile);
		graph.saveAsPNG(pngFile, 600, 400);
		println("==Saving graph as JPEG file " + jpegFile);
		graph.saveAsJPEG(jpegFile, 600, 400, 0.8F);

		// demo ends
		pw.close();
		println("Demo completed in " +
			((System.currentTimeMillis() - startMillis) / 1000.0) +
			" sec");
	}

	static void println(String msg) {
		System.out.println(msg);
	}

	static String getFullPath(String path) {
		return HOME + SEPARATOR + path;
	}
}

class GaugeSource {
	private double value;
	private double step;

	GaugeSource(double value, double step) {
		this.value = value;
		this.step = step;
	}

	double getValue() {
		double oldValue = value;
		double increment = Math.random() * step;
		if(Math.random() > 0.5) {
			increment *= -1;
		}
		value += increment;
		if(value <= 0) {
			value = 0;
		}
		return oldValue;
	}


}


