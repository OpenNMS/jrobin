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

import java.awt.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

class Demo {
	static final long SEED = 1909752002L;  
	
	static final Random RANDOM = new Random(SEED);
	static final String FILE = "demo";

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

		// creation
		println("== Creating RRD file " + rrdPath);
		RrdDef rrdDef = new RrdDef(rrdPath, start - 1, 300);
		rrdDef.addDatasource("sun", "GAUGE", 600, 0, Double.NaN);
		rrdDef.addDatasource("shade", "GAUGE", 600, 0, Double.NaN);
		rrdDef.addArchive("AVERAGE", 0.5, 1, 600);
		rrdDef.addArchive("AVERAGE", 0.5, 6, 700);
		rrdDef.addArchive("AVERAGE", 0.5, 24, 775);
		rrdDef.addArchive("AVERAGE", 0.5, 288, 797);
		rrdDef.addArchive("MAX", 0.5, 1, 600);
		rrdDef.addArchive("MAX", 0.5, 6, 700);
		rrdDef.addArchive("MAX", 0.5, 24, 775);
		rrdDef.addArchive("MAX", 0.5, 288, 797);
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
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setTimePeriod(start, end);
        gDef.setTitle("Temperatures in May 2003");
		gDef.setVerticalLabel("temperature");
		gDef.datasource("sun", rrdRestoredPath, "sun", "AVERAGE");
		gDef.datasource("shade", rrdRestoredPath, "shade", "AVERAGE");
		gDef.datasource("median", "sun,shade,+,2,/");
		gDef.datasource("diff", "sun,shade,-,ABS,-1,*");
		gDef.datasource("sine", "TIME," + start + ",-," + (end - start) +
			",/,2,PI,*,*,SIN,1000,*");
		gDef.line("sun", Color.GREEN, "sun temp");
		gDef.line("shade", Color.BLUE, "shade temp");
		gDef.line("median", Color.MAGENTA, "median value@L");
		gDef.area("diff", Color.YELLOW, "difference@r");
		gDef.line("diff", Color.RED, null);
		gDef.line("sine", Color.CYAN, "sine function demo@L");
		gDef.gprint("sun", "MAX", "maxSun = @3@s");
		gDef.gprint("sun", "AVERAGE", "avgSun = @3@S@r");
		gDef.gprint("shade", "MAX", "maxShade = @3@S");
		gDef.gprint("shade", "AVERAGE", "avgShade = @3@S@r");
		// create graph finally
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
			if(RANDOM.nextDouble() > 0.5) {
				increment *= -1;
			}
			value += increment;
			if(value <= 0) {
				value = 0;
			}
			return Math.round(oldValue);
		}
	}
}




