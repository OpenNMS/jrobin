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
import java.util.GregorianCalendar;

class Demo {
	static final String HOME = Util.getJRobinDemoDirectory();
	static final String FILE = "demo";
	static final GregorianCalendar START = new GregorianCalendar(2003, 4, 1);
	static final GregorianCalendar END = new GregorianCalendar(2003, 5, 1);
	static final int MAX_STEP = 240;

	// increase this to get better flaming graph...
	static final int GRADIENT_COLOR_STEPS = 20;

	public static void main(String[] args) throws RrdException, IOException {
		// setup
		println("==Starting demo");
		RrdDb.setLockMode(RrdDb.NO_LOCKS);

		long startMillis = System.currentTimeMillis();
		long start = START.getTime().getTime() / 1000L;
		long end = END.getTime().getTime() / 1000L;
		String rrdPath = getFullPath(FILE + ".rrd");
		String xmlPath = getFullPath(FILE + ".xml");
		String rrdRestoredPath = getFullPath(FILE + "_restored.rrd");
		String pngPath = getFullPath(FILE + ".png");
		String jpegPath = getFullPath(FILE + ".jpeg");
		String gifPath = getFullPath(FILE + ".gif");
		String logPath = getFullPath(FILE + ".log");
		PrintWriter pw = new PrintWriter(
			new BufferedOutputStream(new FileOutputStream(logPath, false))
		);

		// creation
		println("==Creating RRD file " + rrdPath);
		RrdDef rrdDef = new RrdDef(rrdPath, start - 1, 300);
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
		Sample sample = rrdDb.createSample();
		while(t <= end) {
			sample.setTime(t);
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
		FetchData fetchData = request.fetchData();
		println("==Data fetched. " + fetchData.getRowCount() + " points obtained");
		for(int i = 0; i < fetchData.getRowCount(); i++) {
			println(fetchData.getRow(i).dump());
		}
		println("==Fetch completed");
		println("==Dumping RRD file to XML file " + xmlPath + " (can be restored with RRDTool)");
		rrdDb.dumpXml(xmlPath);
		println("==Creating RRD file " + rrdRestoredPath + " from XML file " + xmlPath);
		RrdDb rrdRestoredDb = new RrdDb(rrdRestoredPath, xmlPath);
		// close files
		println("==Closing both RRD files");
		rrdDb.close();
		rrdRestoredDb.close();

		// creating graph
		println("==Creating graph from the second file");
		RrdGraphDef gDef = new RrdGraphDef();
		gDef.setTimePeriod(start, end);
        gDef.setTitle("Temperatures in May 2003");
		gDef.setVerticalLabel("temperature");
		gDef.datasource("sun", rrdRestoredPath, "sun", "AVERAGE");
		gDef.datasource("shade", rrdRestoredPath, "shade", "AVERAGE");
		gDef.datasource("median", "sun,shade,+,2,/");
		gDef.datasource("diff", "sun,shade,-,ABS,-1,*");
		// gradient color datasources
		for(int i = 1; i < GRADIENT_COLOR_STEPS; i++) {
			double factor = i / (double) GRADIENT_COLOR_STEPS;
			gDef.datasource("diff" + i, "diff," + factor + ",*");
		}
		gDef.datasource("sine", "TIME," + start + ",-," + (end - start) +
			",/,2,PI,*,*,SIN,1000,*");
		gDef.line("sun", Color.GREEN, "sun temp");
		gDef.line("shade", Color.BLUE, "shade temp");
		gDef.line("median", Color.MAGENTA, "median value@L");
		gDef.area("diff", Color.RED, "difference@r");
		// gradient color areas
		for(int i = GRADIENT_COLOR_STEPS - 1; i >= 1; i--) {
			gDef.area("diff" + i,
				new Color(255, 255 - 255 * i / GRADIENT_COLOR_STEPS, 0),
				null);
		}
		gDef.line("sine", Color.CYAN, "sine function demo@L");
		gDef.gprint("sun", "MAX", "maxSun = @3@s");
		gDef.gprint("sun", "AVERAGE", "avgSun = @3@S@r");
		gDef.gprint("shade", "MAX", "maxShade = @3@S");
		gDef.gprint("shade", "AVERAGE", "avgShade = @3@S@r");
		// create graph finally
		RrdGraph graph = new RrdGraph(gDef);
		println("==Graph created");
		println("==Saving graph as PNG file " + pngPath);
		graph.saveAsPNG(pngPath, 400, 250);
		println("==Saving graph as JPEG file " + jpegPath);
		graph.saveAsJPEG(jpegPath, 400, 250, 0.5F);
		println("==Saving graph as GIF file " + gifPath);
		graph.saveAsGIF(gifPath, 400, 250);

		// demo ends
		pw.close();
		println("Demo completed in " +
			((System.currentTimeMillis() - startMillis) / 1000.0) +	" sec");
	}

	static void println(String msg) {
		System.out.println(msg);
	}

	static String getFullPath(String path) {
		return HOME + path;
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


