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

import java.awt.Color;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;
import java.io.IOException;

import org.jrobin.core.*;
import org.jrobin.graph.*;

/**
 * <p>Extended graphing demo for JRobin.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class ComplexDemo {
	private static final String filename = "complexdemo";

	private static String getPath(String ext) {
		return Util.getJRobinDemoDirectory() + filename + "." + ext;
	}

	private static String getPath(int version, String ext) {
		return Util.getJRobinDemoDirectory() + filename + version + "." + ext;
	}

	private static void println(String msg) {
		System.out.println(msg);
	}

	private static void createDatabase(String xmlPath) throws IOException, RrdException {
		// Import database from XML
		String rrdPath = getPath("rrd");
		println("-- Importing XML file: " + xmlPath);
		println("-- to RRD file: " + rrdPath);
		RrdDbPool pool = RrdDbPool.getInstance();
		RrdDb rrd = pool.requestRrdDb(rrdPath, xmlPath);
		println("-- RRD file created");
		pool.release(rrd);
	}

	private static void createGraphs() throws RrdException, IOException {
		GregorianCalendar start, stop;
		RrdGraph graph = new RrdGraph(true);
		String rrdPath = getPath("rrd");

		// Create traffic overview of a week
		println("-- Creating graph 1");
		start = new GregorianCalendar(2003, 7, 20);
		stop = new GregorianCalendar(2003, 7, 27);

		RrdGraphDef def = new RrdGraphDef(start, stop);
		def.setImageBorder(Color.GRAY, 1);
		def.setTitle("JRobinComplexDemo@Ldemo graph 1@r\nNetwork traffic overview");
		def.setVerticalLabel("bits per second");
		def.datasource("ifInOctets", rrdPath, "ifInOctets", "AVERAGE");
		def.datasource("ifOutOctets", rrdPath, "ifOutOctets", "AVERAGE");
		def.datasource("bitIn", "ifInOctets,8,*");
		def.datasource("bitOut", "ifOutOctets,8,*");
		def.comment(" ");
		def.area("bitIn", new Color(0x00, 0xFF, 0x00), "Incoming traffic ");
		def.line("bitOut", new Color(0x00, 0x00, 0x33), "Outgoing traffic\n\n");
		def.gprint("bitIn", "MAX", "Max:   @6.1 @sbit/s");
		def.gprint("bitOut", "MAX", "      @6.1 @sbit/s\n");
		def.gprint("bitIn", "MIN", "Min:   @6.1 @sbit/s");
		def.gprint("bitOut", "MIN", "      @6.1 @sbit/s");
		def.comment("       Connection:  100 Mbit/s\n");
		def.gprint("bitIn", "AVG", "Avg:   @6.1 @sbit/s");
		def.gprint("bitOut", "AVG", "      @6.1 @sbit/s");
		def.comment("       Duplex mode: FD - fixed\n\n");
		def.gprint("bitIn", "LAST", "Cur:   @6.1 @sbit/s");
		def.gprint("bitOut", "LAST", "      @6.1 @sbit/s\n\n");
		def.comment("[ courtesy of www.cherrymon.org ]@L");
		def.comment("Generated: " + timestamp() + "  @r");

		graph.setGraphDef(def);
		String pngFile = getPath(1, "png");
		graph.saveAsPNG(pngFile);
		String gifFile = getPath(1, "gif");
		graph.saveAsGIF(gifFile);
		String jpgFile = getPath(1, "jpg");
		graph.saveAsJPEG(jpgFile, 0.6F);

		// Create server load and cpu usage of a day
		println("-- Creating graph 2");
		start = new GregorianCalendar(2003, 7, 19);
		stop = new GregorianCalendar(2003, 7, 20);

		def = new RrdGraphDef(start, stop);
		def.setImageBorder(Color.GRAY, 1);
		def.setTitle("JRobinComplexDemo@Ldemo graph 2@r\nServer load and CPU utilization");
		def.datasource("load", rrdPath, "serverLoad", "AVERAGE");
		def.datasource("user", rrdPath, "serverCPUUser", "AVERAGE");
		def.datasource("nice", rrdPath, "serverCPUNice", "AVERAGE");
		def.datasource("system", rrdPath, "serverCPUSystem", "AVERAGE");
		def.datasource("idle", rrdPath, "serverCPUIdle", "AVERAGE");
		def.datasource("total", "user,nice,+,system,+,idle,+");
		def.datasource("busy", "user,nice,+,system,+,total,/,100,*");
		def.datasource("p25t50", "busy,25,GT,busy,50,LE,load,0,IF,0,IF");
		def.datasource("p50t75", "busy,50,GT,busy,75,LE,load,0,IF,0,IF");
		def.datasource("p75t90", "busy,75,GT,busy,90,LE,load,0,IF,0,IF");
		def.datasource("p90t100", "busy,90,GT,load,0,IF");
		def.comment("CPU utilization (%)\n ");
		def.area("load", new Color(0x66, 0x99, 0xcc), " 0 - 25%");
		def.area("p25t50", new Color(0x00, 0x66, 0x99), "25 - 50%@L");
		def.gprint("busy", "MIN", "Minimum:@5.1@s%");
		def.gprint("busy", "MAX", "Maximum:@5.1@s% @r ");
		def.area("p50t75", new Color(0x66, 0x66, 0x00), "50 - 75%");
		def.area("p75t90", new Color(0xff, 0x66, 0x00), "75 - 90%");
		def.area("p90t100", new Color(0xcc, 0x33, 0x00), "90 - 100%@L");
		def.gprint("busy", "AVG", " Average:@5.1@s%");
		def.gprint("busy", "LAST", "Current:@5.1@s% @r ");
		def.comment("\nServer load\n ");
		def.line("load", new Color(0x00, 0x00, 0x00), "Load average (5 min)@L");
		def.gprint("load", "MIN", "Minimum:@5.2@s%");
		def.gprint("load", "MAX", "Maximum:@5.2@s% @r ");
		def.gprint("load", "AVG", "Average:@5.2@s%");
		def.gprint("load", "LAST", "Current:@5.2@s% @r");
		def.comment("\n\n[ courtesy of www.cherrymon.org ]@L");
		def.comment("Generated: " + timestamp() + "  @r");

		graph.setGraphDef(def);
		pngFile = getPath(2, "png");
		graph.saveAsPNG(pngFile);
		gifFile = getPath(2, "gif");
		graph.saveAsGIF(gifFile);
		jpgFile = getPath(2, "jpg");
		graph.saveAsJPEG(jpgFile, 0.6F);

		// Create ftp graph for a month
		println("-- Creating graph 3");
		start = new GregorianCalendar(2003, 7, 19, 12, 00);
		stop = new GregorianCalendar(2003, 7, 20, 12, 00);

		def = new RrdGraphDef(start, stop);
		def.setImageBorder(Color.GRAY, 1);
		def.setFrontGrid(false);
		def.setTitle("JRobinComplexDemo@Ldemo graph 3@r\nFTP Usage");
		def.datasource("ftp", rrdPath, "ftpUsers", "AVERAGE");
		def.line("ftp", new Color(0x00, 0x00, 0x33), "FTP connections");
		def.gprint("ftp", "AVG", "( average: @0,");
		def.gprint("ftp", "MIN", "never below: @0 )\n\n");
		def.comment("  Usage spread:");
		def.area(new GregorianCalendar(2003, 7, 19, 17, 00), Double.MIN_VALUE,
			new GregorianCalendar(2003, 7, 19, 23, 00), Double.MAX_VALUE,
			Color.RED, "peak period");
		def.area(new GregorianCalendar(2003, 7, 20, 5, 00), Double.MIN_VALUE,
			new GregorianCalendar(2003, 7, 20, 8, 30), Double.MAX_VALUE,
			Color.LIGHT_GRAY, "quiet period\n");
		def.comment("  Rise/descend:");
		def.area("ftp", new Color(0x00, 0x00, 0x33), null);
		def.line(new GregorianCalendar(2003, 7, 19, 12, 00), 110,
			new GregorianCalendar(2003, 7, 19, 20, 30), 160,
			Color.PINK, "climb slope", 2);
		def.line(new GregorianCalendar(2003, 7, 19, 20, 30), 160,
			new GregorianCalendar(2003, 7, 20, 8, 00), 45,
			Color.CYAN, "fall-back slope\n", 2);
		def.vrule(new GregorianCalendar(2003, 7, 20), Color.YELLOW, null);
		def.comment("\n\n[ courtesy of www.cherrymon.org ]@L");
		def.comment("Generated: " + timestamp() + "  @r");

		graph.setGraphDef(def);
		pngFile = getPath(3, "png");
		graph.saveAsPNG(pngFile, 500, 300);
		gifFile = getPath(3, "gif");
		graph.saveAsGIF(gifFile, 500, 300);
		jpgFile = getPath(3, "jpg");
		graph.saveAsJPEG(jpgFile, 500, 300, 0.6F);
		println("-- Finished");
		println("**************************************");
		println("Check your " + Util.getJRobinDemoDirectory() + " directory.");
		println("You should see nine nice looking graphs starting with [" + filename + "],");
		println("three different graphs, each in three different image formats");
		println("**************************************");
	}

	private static String timestamp() {
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return df.format(new Date());
	}

	public static void main(String[] args) throws IOException, RrdException {
		if(args.length == 0) {
			println("Usage: ComplexDemo [path to rrdtool_dump.xml file]");
			println("You can download separate rrdtool_dump.xml file from:");
			println("http://www.sourceforge.net/projects/jrobin");
			System.exit(-1);
		}
		long start = System.currentTimeMillis();

		println("********************************************************************");
		println("* JRobinComplexDemo                                                *");
		println("*                                                                  *");
		println("* This demo creates 3 separate graphs and stores them under        *");
		println("* several formats in 9 files.  Values are selected from a large    *");
		println("* RRD file that will be created by importing an XML dump           *");
		println("* of approx. 7 MB.                                                 *");
		println("*                                                                  *");
		println("* Graphs are created using real-life values, original RRD file     *");
		println("* provided by www.cherrymon.org. See the ComplexDemo               *");
		println("* sourcecode on how to create the graphs generated by this demo.   *");
		println("********************************************************************");

		createDatabase(args[0]);
		createGraphs();

		long stop = System.currentTimeMillis();
		println("-- Demo finished in " + ((stop - start) / 1000.0) + " seconds.");
	}
}
