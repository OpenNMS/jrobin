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
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraph;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Date;
import java.awt.*;

class StressTest {
	static final String FACTORY_NAME = "NIO";

	static final String RRD_PATH = Util.getJRobinDemoPath("stress.rrd");
	static final long RRD_START = 946710000L;
	static final long RRD_STEP = 30;

	static final String RRD_DATASOURCE_NAME = "T";
	static final int RRD_DATASOURCE_COUNT = 6;

	static final long TIME_START = 1060142010L;
	static final long TIME_END = 1080013472L;

	static final String PNG_PATH = Util.getJRobinDemoPath("stress.png");
	static final int PNG_WIDTH = 400;
	static final int PNG_HEIGHT = 250;

	static void printLapTime(String message) {
		System.out.println(message + " " + Util.getLapTime());
	}

    public static void main(String[] args) throws RrdException, IOException {
		if(args.length == 0) {
			System.out.println("Usage: StressTest [path to stress-test.txt file]");
			System.out.println("You can download separate stress-test.txt file from:");
			System.out.println("http://www.sourceforge.net/projects/jrobin");
			System.exit(-1);
		}
		System.out.println("********************************************************************");
		System.out.println("* JRobinStressTest                                                 *");
		System.out.println("*                                                                  *");
		System.out.println("* This demo creates single RRD file and tries to update it         *");
		System.out.println("* more than 600.000 times. Real data (> 20Mb) is obtained from the *");
		System.out.println("* stress-test.txt file provided by Vadim Tkachenko                 *");
		System.out.println("* (http://diy-zoning.sourceforge.net).                             *");
		System.out.println("*                                                                  *");
		System.out.println("* Finally, a single PNG graph will be created from the RRD file.   *");
		System.out.println("* The stress test takes about one hour to complete on a 1.6GHz     *");
		System.out.println("* computer with 256MB of RAM.                                      *");
		System.out.println("********************************************************************");
		printLapTime("Starting demo at " + new Date());
		RrdDb.setDefaultFactory(FACTORY_NAME);
		printLapTime("Backend factory set to " + FACTORY_NAME);
		// create RRD database
		printLapTime("Creating RRD definition");
		RrdDef def = new RrdDef(RRD_PATH);
        def.setStartTime(RRD_START);
        def.setStep(RRD_STEP);
		for(int i = 0; i < RRD_DATASOURCE_COUNT; i++) {
			def.addDatasource(RRD_DATASOURCE_NAME + i, "GAUGE", 90, -60, 85);
		}
        def.addArchive("LAST", 0.5, 1, 5760);
        def.addArchive("MIN", 0.5, 1, 5760);
        def.addArchive("MAX", 0.5, 1, 5760);
        def.addArchive("AVERAGE", 0.5, 5, 13824);
        def.addArchive("MIN", 0.5, 5, 13824);
        def.addArchive("MAX", 0.5, 5, 13824);
        def.addArchive("AVERAGE", 0.5, 60, 16704);
        def.addArchive("MIN", 0.5, 60, 16704);
        def.addArchive("MAX", 0.5, 60, 16704);
        def.addArchive("AVERAGE", 0.5, 1440, 50000);
        def.addArchive("MIN", 0.5, 1440, 50000);
        def.addArchive("MAX", 0.5, 1440, 50000);
		printLapTime("Definition created, creating RRD file");
		RrdDb rrd = RrdDbPool.getInstance().requestRrdDb(def);
		printLapTime("RRD file created: " + RRD_PATH);
		BufferedReader r = new BufferedReader(new FileReader(args[0]));
		printLapTime("Buffered reader created, processing data");
		int count = 0;
		Date updateStart = new Date();
		for(String line; (line = r.readLine()) != null;) {
			Sample sample = rrd.createSample();
			try {
				sample.setAndUpdate(line);
				if(++count % 1000 == 0) {
					Date now = new Date();
					long speed = (long)(count * 1000.0 / (now.getTime() - updateStart.getTime()));
					printLapTime(count + " samples stored, " + speed + " updates/sec");
				}
			}
			catch(RrdException e) {
				printLapTime("RRD ERROR: " + line);
			}
		}
		RrdDbPool.getInstance().release(rrd);
		printLapTime("FINISHED: " + count + " samples stored");
        // GRAPH
		printLapTime("Creating composite graph definition");
		RrdGraphDef gdef = new RrdGraphDef(TIME_START, TIME_END);
		gdef.setTitle("Temperatures");
		gdef.setVerticalLabel("Fahrenheits");
		final Color[] colors =  { Color.RED, Color.GREEN, Color.BLUE, Color.MAGENTA,
			 Color.CYAN, Color.ORANGE };
		// datasources
		for(int i = 0; i < RRD_DATASOURCE_COUNT; i++) {
			String name = RRD_DATASOURCE_NAME + i;
			gdef.datasource(name, RRD_PATH, name, "AVERAGE");
		}
		// lines
		for(int i = 0; i < RRD_DATASOURCE_COUNT; i++) {
			String name = RRD_DATASOURCE_NAME + i;
			gdef.line(name, colors[i], name);
		}
		gdef.comment("@c");
		gdef.comment("\nOriginal data provided by diy-zoning.sf.net@c");
		printLapTime("Graph definition created");
		RrdGraph g = new RrdGraph(gdef, true);
		g.saveAsPNG(PNG_PATH, PNG_WIDTH, PNG_HEIGHT);
		printLapTime("Graph saved: " + PNG_PATH);
		printLapTime("Finished at " + new Date());
	}
}
