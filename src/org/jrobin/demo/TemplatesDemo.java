package org.jrobin.demo;

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

import org.jrobin.core.*;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraphDefTemplate;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

class TemplatesDemo {
	static final String RRD_TEMPLATE =
		"<rrd_def>                              " +
		"    <path>${path}</path>               " +
		"    <start>${start}</start>            " +
		"	 <step>300</step>                   " +
		"    <datasource>                       " +
		" 	     <name>sun</name>               " +
		"        <type>GAUGE</type>             " +
		"        <heartbeat>600</heartbeat>     " +
		"        <min>0</min>                   " +
		"		 <max>U</max>                   " +
		"    </datasource>                      " +
		"    <datasource>                       " +
		" 	     <name>shade</name>             " +
		"        <type>GAUGE</type>             " +
		"        <heartbeat>600</heartbeat>     " +
		"        <min>0</min>                   " +
		"		 <max>U</max>                   " +
		"    </datasource>                      " +
		"    <archive>                          " +
		"        <cf>AVERAGE</cf>               " +
		"        <xff>0.5</xff>                 " +
		"        <steps>1</steps>               " +
		"	     <rows>600</rows>               " +
		"    </archive>                         " +
		"    <archive>                          " +
		"        <cf>AVERAGE</cf>               " +
		"        <xff>0.5</xff>                 " +
		"        <steps>6</steps>               " +
		"	     <rows>700</rows>               " +
		"    </archive>                         " +
		"    <archive>                          " +
		"        <cf>AVERAGE</cf>               " +
		"        <xff>0.5</xff>                 " +
		"        <steps>24</steps>              " +
		"	     <rows>775</rows>               " +
		"    </archive>                         " +
		"    <archive>                          " +
		"        <cf>AVERAGE</cf>               " +
		"        <xff>0.5</xff>                 " +
		"        <steps>288</steps>             " +
		"	     <rows>797</rows>               " +
		"    </archive>                         " +
		"    <archive>                          " +
		"        <cf>MAX</cf>                   " +
		"        <xff>0.5</xff>                 " +
		"        <steps>1</steps>               " +
		"	     <rows>600</rows>               " +
		"    </archive>                         " +
		"    <archive>                          " +
		"        <cf>MAX</cf>                   " +
		"        <xff>0.5</xff>                 " +
		"        <steps>6</steps>               " +
		"	     <rows>700</rows>               " +
		"    </archive>                         " +
		"    <archive>                          " +
		"        <cf>MAX</cf>                   " +
		"        <xff>0.5</xff>                 " +
		"        <steps>24</steps>              " +
		"	     <rows>775</rows>               " +
		"    </archive>                         " +
		"    <archive>                          " +
		"        <cf>MAX</cf>                   " +
		"        <xff>0.5</xff>                 " +
		"        <steps>288</steps>             " +
		"	     <rows>797</rows>               " +
		"    </archive>                         " +
		"</rrd_def>                             " ;

	static final String GRAPH_TEMPLATE =
		"<rrd_graph_def>                                      " +
		"    <span>                                           " +
		"        <start>${start}</start>                      " +
		"        <end>${end}</end>                            " +
		"    </span>                                          " +
		"    <options>                                        " +
		"        <title>${title}</title>                      " +
		"        <vertical_label>temperature</vertical_label> " +
		"    </options>                                       " +
		"    <datasources>                                    " +
		"        <def>                                        " +
		"            <name>sun</name>                         " +
		"            <rrd>${rrd}</rrd>                        " +
		"            <source>sun</source>                     " +
		"            <cf>AVERAGE</cf>                         " +
		"        </def>                                       " +
		"        <def>                                        " +
		"            <name>shade</name>                       " +
		"            <rrd>${rrd}</rrd>                        " +
		"            <source>shade</source>                   " +
		"            <cf>AVERAGE</cf>                         " +
		"        </def>                                       " +
		"        <def>                                        " +
		"            <name>median</name>                      " +
		"            <rpn>sun,shade,+,2,/</rpn>               " +
		"        </def>                                       " +
		"        <def>                                        " +
		"            <name>diff</name>                        " +
		"            <rpn>sun,shade,-,ABS,-1,*</rpn>          " +
		"        </def>                                       " +
		"        <def>                                        " +
		"            <name>sine</name>                        " +
		"            <rpn>${sine}</rpn>                       " +
		"        </def>                                       " +
		"    </datasources>                                   " +
		"    <graph>                                          " +
		"        <line>                                       " +
		"            <datasource>sun</datasource>             " +
		"            <color>#00FF00</color>                   " +
		"            <legend>sun temp</legend>                " +
		"        </line>                                      " +
		"        <line>                                       " +
		"            <datasource>shade</datasource>           " +
		"            <color>#0000FF</color>                   " +
		"            <legend>shade temp</legend>              " +
		"        </line>                                      " +
		"        <line>                                       " +
		"            <datasource>median</datasource>          " +
		"            <color>#FF00FF</color>                   " +
		"            <legend>median value@L</legend>          " +
		"        </line>                                      " +
		"        <area>                                       " +
		"            <datasource>diff</datasource>            " +
		"            <color>#FFFF00</color>                   " +
		"            <legend>difference@r</legend>            " +
		"        </area>                                      " +
		"        <line>                                       " +
		"            <datasource>diff</datasource>            " +
		"            <color>#FF0000</color>                   " +
		"            <legend/>                                " +
		"        </line>                                      " +
		"        <line>                                       " +
		"            <datasource>sine</datasource>            " +
		"            <color>#00FFFF</color>                   " +
		"            <legend>sine function demo@L</legend>    " +
		"        </line>                                      " +
		"        <gprint>                                     " +
		"            <datasource>sun</datasource>             " +
		"            <cf>MAX</cf>                             " +
		"            <format>maxSun = @3@s</format>           " +
		"        </gprint>                                    " +
		"        <gprint>                                     " +
		"            <datasource>sun</datasource>             " +
		"            <cf>AVERAGE</cf>                         " +
		"            <format>avgSun = @3@S@r</format>         " +
		"        </gprint>                                    " +
		"        <gprint>                                     " +
		"            <datasource>shade</datasource>           " +
		"            <cf>MAX</cf>                             " +
		"            <format>maxShade = @3@S</format>         " +
		"        </gprint>                                    " +
		"        <gprint>                                     " +
		"            <datasource>shade</datasource>           " +
		"            <cf>AVERAGE</cf>                         " +
		"            <format>avgShade = @3@S@r</format>       " +
		"        </gprint>                                    " +
		"    </graph>                                         " +
		"</rrd_graph_def>                                     " ;

	static final long SEED = 1909752002L;

	static final Random RANDOM = new Random(SEED);
	static final String FILE = "templates_demo";

	static final long START = Util.getTimestamp(2003, 4, 1);
	static final long END = Util.getTimestamp(2003, 5, 1);

	static final int MAX_STEP = 300;

	public static void main(String[] args) throws RrdException, IOException {
		// setup
		println("== Starting demo");
		RrdDb.setLockMode(RrdDb.NO_LOCKS);

		long startMillis = System.currentTimeMillis();
		long start = START;
		long end = END;
		String rrdPath = Util.getJRobinDemoPath(FILE + ".rrd");
		String xmlPath = Util.getJRobinDemoPath(FILE + ".xml");
		String rrdRestoredPath = Util.getJRobinDemoPath(FILE + "_restored.rrd");
		String pngPath = Util.getJRobinDemoPath(FILE + ".png");
		String jpegPath = Util.getJRobinDemoPath(FILE + ".jpeg");
		String gifPath = Util.getJRobinDemoPath(FILE + ".gif");
		String logPath = Util.getJRobinDemoPath(FILE + ".log");
		PrintWriter log = new PrintWriter(
			new BufferedOutputStream(new FileOutputStream(logPath, false))
		);

		// creation from the template
		println("== Creating RRD file " + rrdPath);
		RrdDefTemplate defTemplate = new RrdDefTemplate(RRD_TEMPLATE);
		defTemplate.setVariable("path", rrdPath);
		defTemplate.setVariable("start", start - 1);
		RrdDef rrdDef = defTemplate.getRrdDef();

		println(rrdDef.dump());
		log.println(rrdDef.dump());
		RrdDb rrdDb = new RrdDb(rrdDef);
		rrdDb.close();
		println("== RRD file created and closed.");

		// update database
		GaugeSource sunSource = new GaugeSource(1200, 20);
		GaugeSource shadeSource = new GaugeSource(300, 10);
		println("== Simulating one month of RRD file updates with step not larger than " +
			MAX_STEP + " seconds (* denotes 1000 updates)");
		long t = start; int n = 0;
		rrdDb = new RrdDb(rrdPath);
		Sample sample = rrdDb.createSample();
		while(t <= end + 86400L) {
			sample.setTime(t);
			sample.setValue("sun", sunSource.getValue());
			sample.setValue("shade", shadeSource.getValue());
			log.println(sample.dump());
			sample.update();

			t += RANDOM.nextDouble() * MAX_STEP + 1;
			if(((++n) % 1000) == 0) {
				System.out.print("*");
			};
		}
		System.out.println("");
		println("== Finished. RRD file updated " + n + " times");
		println("== Last update time was: " + rrdDb.getLastUpdateTime());

		// fetch data
		println("== Fetching data for the whole month");
		FetchRequest request = rrdDb.createFetchRequest("AVERAGE", start, end);
		println(request.dump());
		log.println(request.dump());
		FetchData fetchData = request.fetchData();
		println("== Data fetched. " + fetchData.getRowCount() + " points obtained");
		for(int i = 0; i < fetchData.getRowCount(); i++) {
			println(fetchData.getRow(i).dump());
		}
		println("== Dumping fetch data to XML format");
		println(fetchData.exportXml());
		println("== Fetch completed");

		// dump to XML file
		println("== Dumping RRD file to XML file " + xmlPath + " (can be restored with RRDTool)");
		rrdDb.exportXml(xmlPath);
		println("== Creating RRD file " + rrdRestoredPath + " from XML file " + xmlPath);
		RrdDb rrdRestoredDb = new RrdDb(rrdRestoredPath, xmlPath);

		// close files
		println("== Closing both RRD files");
		rrdDb.close();
		rrdRestoredDb.close();

		// create graph
		println("== Creating graph from the second file");
		RrdGraphDefTemplate graphTemplate = new RrdGraphDefTemplate(GRAPH_TEMPLATE);
		graphTemplate.setVariable("start", start);
		graphTemplate.setVariable("end", end);
		graphTemplate.setVariable("title", "Temperatures in May 2003");
		graphTemplate.setVariable("rrd", rrdRestoredPath);
		// RPN expressions can be created at runtime and put into a template
		graphTemplate.setVariable("sine", "TIME," + start + ",-," + (end - start) +
			",/,2,PI,*,*,SIN,1000,*");
		// create graph finally
		RrdGraphDef gDef = graphTemplate.getRrdGraphDef();
		RrdGraph graph = new RrdGraph(gDef);
		println("== Graph created");
		println("== Saving graph as PNG file " + pngPath);
		graph.saveAsPNG(pngPath, 400, 250);
		println("== Saving graph as JPEG file " + jpegPath);
		graph.saveAsJPEG(jpegPath, 400, 250, 0.5F);
		println("== Saving graph as GIF file " + gifPath);
		graph.saveAsGIF(gifPath, 400, 250);

		// demo ends
		log.close();
		println("== Demo completed in " +
			((System.currentTimeMillis() - startMillis) / 1000.0) +	" sec");
	}

	static void println(String msg) {
		System.out.println(msg);
	}

	static class GaugeSource {
		private double value;
		private double step;

		GaugeSource(double value, double step) {
			this.value = value;
			this.step = step;
		}

		long getValue() {
			double oldValue = value;
			double increment = RANDOM.nextDouble() * step;
			if (RANDOM.nextDouble() > 0.5) {
				increment *= -1;
			}
			value += increment;
			if (value <= 0) {
				value = 0;
			}
			return Math.round(oldValue);
		}
	}
}




