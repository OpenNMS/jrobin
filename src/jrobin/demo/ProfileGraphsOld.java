/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org)
 * 
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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

import java.io.IOException;
import java.io.PrintWriter;
import java.awt.Color;
import java.util.LinkedList;
import java.util.GregorianCalendar;
import java.util.Date;

import jrobin.core.RrdException;
import jrobin.core.Util;
import jrobin.graph.*;

/**
 * <p>description</p>
 * 
 * @author Arne Vandamme (arne.vandamme@jrobin.org)
 * @author Sasa Markovic (saxon@jrobin.org)
 */
class ProfileGraphsOld 
{
	private final int DNUM = 0;		// profiler num, 0 or 1 (1 = new, 0 = old)
	private int demoNum;
	
	private PrintWriter out;
	private RrdGraph graph;
	private JRobinTimeProfiler profiler;
	
	// Demo sake
	private GregorianCalendar startDate = new GregorianCalendar(2003, 7, 24, 00, 00);
	private GregorianCalendar endDate 	= new GregorianCalendar(2003, 7, 25, 00, 00);
	
	private void addTimingInfo( int profileIndex, LinkedList v )
	{
		int i = profileIndex - 1;
		
		while ( ! v.isEmpty() )
		{
			String timetag 	= (String) v.removeFirst();
			String[] s 		= timetag.split("_");
			int num 		= Integer.parseInt(s[0]);
			long time		= Long.parseLong(s[1]); 
			
			profiler.profiles[i].creation[DNUM][num] = time; 
		}
	}
	
	ProfileGraphsOld( PrintWriter out, JRobinTimeProfiler profiler )
	{
		this.profiler = profiler;
		this.out = out;
		long fullStart, fullStop;

		fullStart	= System.currentTimeMillis();
		
		try
		{
			createDemo1();
			
			createDemo2();
			
			createDemo3();
			
			createDemoEx(4);
			
			createDemoEx(5);
			
			createDemoEx(6);
		}
		catch ( Exception e ) 
		{
			out.println( "\nException occurred: " + e.getMessage() + "\n" );	
		}
		
		fullStop	= System.currentTimeMillis();
		profiler.oldGraphs = (fullStop - fullStart);
		//out.println( " Total execution time for graph batch: " + setw( 10, (fullStop - fullStart)) );
	}
	
	private void createDemo1() throws IOException, RrdException
	{
		demoNum = 1;
		
		profiler.profiles[demoNum - 1].name = "Simple traffic graph.";
		
		long start, stop;
		
		// Create Def
		start = System.currentTimeMillis();
		
		RrdGraphDef def = new RrdGraphDef();
		def.setTimePeriod( startDate, endDate );
		def.setTitle("ahha");
		def.datasource("in2", "/test.rrd", "ifInOctets", "AVERAGE");
		def.datasource("out2", "/test.rrd", "ifOutOctets", "AVERAGE");
		def.datasource("in", "in2,8,*");
		def.datasource("out", "out2,8,*");
		def.area("in", Color.RED, "Incoming traffic");
		def.gprint("in", "AVERAGE", "(average: @5.2 @sbit/s)\n");
		def.line("out", Color.BLUE, "Outgoing traffic");
		def.gprint("out", "AVERAGE", "(average: @6.2 @sbit/s)");
		
		stop = System.currentTimeMillis();
		profiler.profiles[demoNum - 1].defCreation[DNUM] = (stop - start);
		
		// Create graph
		graph		= new RrdGraph( def );
		graph.saveAsPNG( "/time_demo" + demoNum + "-" + DNUM + ".png", 0, 0 );		// Default size	
		
		stop = System.currentTimeMillis();
		profiler.profiles[demoNum - 1].totalCreation[DNUM] = (stop - start);
	
		addTimingInfo( demoNum, Util.timeList );
	}
	
	private void createDemo2() throws IOException, RrdException
	{
		demoNum = 2;
	
		profiler.profiles[demoNum - 1].name = "Rainbow graph.";
	
		long start, stop;
	
		// Create Def
		start = System.currentTimeMillis();
	
		// ==============================================================
		RrdGraphDef def = new RrdGraphDef();
		GregorianCalendar sd = new GregorianCalendar(2003, 4, 1);
		GregorianCalendar ed = new GregorianCalendar(2003, 5, 1);
		def.setTimePeriod(sd, ed);
		long t0 = sd.getTime().getTime() / 1000L;
		long t1 = ed.getTime().getTime() / 1000L;
		def.datasource("sine", "TIME," + t0 + ",-," + (t1 - t0) +
			",/,7,PI,*,*,SIN");
		def.datasource("v2", "/gallery.rrd", "shade", "AVERAGE");
		def.datasource("cosine", "TIME," + t0 + ",-," + (t1 - t0) +
			",/,3,PI,*,*,COS");
		def.datasource("line", "TIME," + t0 + ",-," + (t1 - t0) + ",/,1000,*");
		def.datasource("v1", "sine,line,*,ABS");
		int n = 40;
		for(int i = 0; i < n; i++) {
			long t = t0 + (t1 - t0) * i / n;
			def.datasource("c" + i, "TIME," + t + ",GT,v2,UNKN,IF");
		}
		for(int i = 0; i < n; i++) {
			if(i==0) {
				def.area("c"+i, new Color(255-255*Math.abs(i-n/2)/(n/2),0,0), "Output by night");
			}
			else if(i==n/2) {
				def.area("c"+i, new Color(255-255*Math.abs(i-n/2)/(n/2),0,0), "Output by day");
			}
			else {
				def.area("c"+i, new Color(255-255*Math.abs(i-n/2)/(n/2),0,0), null);
			}
		}
		def.line("v2", Color.YELLOW, null);
		def.line("v1", Color.BLUE, "Input voltage@L", 3);
		def.line("v1", Color.YELLOW, null, 1);
		def.setTitle("Voltage measurement");
		//def.setVerticalLabel("[Volts]");
		def.setValueAxisLabel("[Volts]");
		def.comment("fancy looking graphs@r");
		//def.setValueStep(100);
		// ==============================================================
		
		stop = System.currentTimeMillis();
		profiler.profiles[demoNum - 1].defCreation[DNUM] = (stop - start);
	
		// Create graph
		graph		= new RrdGraph( def );
		graph.saveAsPNG( "/time_demo" + demoNum + "-" + DNUM + ".png", 400, 250 );		// Default size	
	
		stop = System.currentTimeMillis();
		profiler.profiles[demoNum - 1].totalCreation[DNUM] = (stop - start);

		addTimingInfo( demoNum, Util.timeList );
	}
	
	private void createDemo3() throws IOException, RrdException
	{
		demoNum = 3;

		profiler.profiles[demoNum - 1].name = "Customized complex graph.";

		long start, stop;

		// Create Def
		start = System.currentTimeMillis();

		// ==============================================================
		RrdGraphDef def 	= new RrdGraphDef();
		def.setTimePeriod( startDate, endDate );
		//gl.setTitleFont( new Font("Lucida Sans Typewriter", Font.PLAIN, 10) );
		def.setTitle( "+------------------------------------------------------------------+@c"
						+ "| Server load...@Land@C...CPU usage |@r" 
						+ "+------------------------------------------------------------------+@c"
					);
		//gl.setVerticalLabel("server load");
		def.setValueAxisLabel("server load");
		def.setBackColor( Color.DARK_GRAY );
		def.setCanvasColor( Color.LIGHT_GRAY );
		def.setImageBorder( Color.BLACK, 1 );
		//gl.setDefaultFontColor( Color.WHITE );
		//gl.setTitleFontColor( Color.GREEN );
		def.setMajorGridColor(Color.YELLOW);
		def.setMinorGridColor( new Color( 130, 30, 30) );
		def.setFrameColor( Color.BLACK );
		def.setAxisColor( Color.RED );
		def.setArrowColor( Color.GREEN );
		//gl.setGridX( false );
		//gl.setGridY( false );
		//gl.setFrontGrid(false);
		def.setShowLegend(true);
		//gl.setUnitsExponent(3);
		//gl.setAntiAliasing(false);
		//gl.setGridRange( 0, 1, false );
		def.setValueRange( 0, 1 );
		def.setRigidGrid( false );
		def.setFontColor( Color.WHITE );

		def.datasource("load", "c:/test.rrd", "serverLoad", "AVERAGE");
		def.datasource("user", "c:/test.rrd", "serverCPUUser", "AVERAGE");
		def.datasource("nice", "c:/test.rrd", "serverCPUNice", "AVERAGE");
		def.datasource("system", "c:/test.rrd", "serverCPUSystem", "AVERAGE");
		def.datasource("idle", "c:/test.rrd", "serverCPUIdle", "AVERAGE");
		def.datasource("total", "user,nice,+,system,+,idle,+");
		def.datasource("busy", "user,nice,+,system,+,total,/,100,*");
		def.datasource("p25t50", "busy,25,GT,busy,50,LE,load,0,IF,0,IF");
		def.datasource("p50t75", "busy,50,GT,busy,75,LE,load,0,IF,0,IF");
		def.datasource("p75t90", "busy,75,GT,busy,90,LE,load,0,IF,0,IF");
		def.datasource("p90t100", "busy,90,GT,load,0,IF");

		def.comment("CPU utilization (%)\n");
		def.comment("  ");
		//gl.hrule( 7.0, Color.YELLOW, null, 10f);
		def.area("load", new Color(0x66,0x99,0xcc), " 0 - 25%");
		def.area("p25t50", new Color(0x00,0x66,0x99), "25 - 50%");
		def.comment("             ");
		def.gprint("busy", "MIN", "Minimum:@5.1@s%");
		def.gprint("busy", "MAX", "Maximum: @5.1@S%");
		def.comment("\n");
		def.comment("  ");
		def.area("p50t75", new Color(0x66,0x66,0x00), "50 - 75%");
		def.area("p75t90", new Color(0xff,0x66,0x00), "75 - 90%");
		def.area("p90t100", new Color(0xcc,0x33,0x00), "90 - 100%");
		//gDef.rule(10.0, Color.ORANGE, null);
		def.gprint("busy", "AVERAGE", " Average:@5.1@s%");
		def.gprint("busy", "LAST", "Current: @5.1@s%");
		def.comment("\n");
		def.comment("\n");
		def.comment("Server load\n");
		def.comment("  ");
		def.line("load", new Color(0x00,0x00,0x00), "Load average (5 min)" );
		//gDef.area("load", Color.RED, " hmm \n");
		//gl.stack("p75t90", Color.GREEN, " hmm");
		def.comment("             ");
		def.gprint("load", "MIN", " Minimum: @5.2@s");
		def.gprint("load", "MAX", "Maximum: @6.2@s");
		def.comment("\n");
		def.comment("                                ");
		def.comment("         ");
		def.gprint("load", "AVERAGE", "Average: @5.2@s");
		def.gprint("load", "LAST", "Current: @6.2@s\n");
		def.hrule( 3.0, Color.YELLOW, "legende", 5);
		def.vrule( new GregorianCalendar(2003, 7, 24, 9, 00), Color.BLUE, "9am", 2 );
		def.comment("\n");
		def.comment("-------------------------------------------------------------------------------@c");
		//gl.vrule( new GregorianCalendar(2003, 7, 24, 9, 00), Color.BLUE, "9am", 2f );
		//gl.vrule( new GregorianCalendar(2003, 7, 24, 17, 00), Color.BLUE, "5pm", 3f );
		def.comment("Generated: " + new Date() + "@R");
		// ==============================================================
	
		stop = System.currentTimeMillis();
		profiler.profiles[demoNum - 1].defCreation[DNUM] = (stop - start);

		// Create graph
		graph		= new RrdGraph( def );
		graph.saveAsPNG( "/time_demo" + demoNum + "-" + DNUM + ".png", 0, 0 );		// Default size	

		stop = System.currentTimeMillis();
		profiler.profiles[demoNum - 1].totalCreation[DNUM] = (stop - start);

		addTimingInfo( demoNum, Util.timeList );
	}
	
	private void createDemoEx( int demoNum ) throws IOException, RrdException
	{
		profiler.profiles[demoNum - 1].name = "Complex graph (3 RRD files).";

		long start, stop;

		// Create Def
		start = System.currentTimeMillis();

		// ==============================================================
		RrdGraphDef def 	= new RrdGraphDef();
		def.setTimePeriod( startDate, endDate );
		def.setTitle( "+------------------------------------------------------------------+@c"
						+ "| Server load...@Land@C...CPU usage |@r" 
						+ "+------------------------------------------------------------------+@c"
					);
		def.setValueAxisLabel("server load");
		def.setAntiAliasing(false);
		
		def.datasource("load", "c:/test.rrd", "serverLoad", "AVERAGE");
		def.datasource("user", "c:/test.rrd", "serverCPUUser", "AVERAGE");
		def.datasource("nice", "c:/test1.rrd", "serverCPUNice", "AVERAGE");
		def.datasource("system", "c:/test1.rrd", "serverCPUSystem", "AVERAGE");
		def.datasource("idle", "c:/test2.rrd", "serverCPUIdle", "AVERAGE");
		def.datasource("total", "user,nice,+,system,+,idle,+");
		def.datasource("busy", "user,nice,+,system,+,total,/,100,*");
		def.datasource("p25t50", "busy,25,GT,busy,50,LE,load,0,IF,0,IF");
		def.datasource("p50t75", "busy,50,GT,busy,75,LE,load,0,IF,0,IF");
		def.datasource("p75t90", "busy,75,GT,busy,90,LE,load,0,IF,0,IF");
		def.datasource("p90t100", "busy,90,GT,load,0,IF");

		def.comment("CPU utilization (%)\n");
		def.comment("  ");
		//gl.hrule( 7.0, Color.YELLOW, null, 10f);
		def.area("load", new Color(0x66,0x99,0xcc), " 0 - 25%");
		def.area("p25t50", new Color(0x00,0x66,0x99), "25 - 50%");
		def.comment("             ");
		def.gprint("busy", "MIN", "Minimum:@5.1@s%");
		def.gprint("busy", "MAX", "Maximum: @5.1@S%");
		def.comment("\n");
		def.comment("  ");
		def.area("p50t75", new Color(0x66,0x66,0x00), "50 - 75%");
		def.area("p75t90", new Color(0xff,0x66,0x00), "75 - 90%");
		def.area("p90t100", new Color(0xcc,0x33,0x00), "90 - 100%");
		//gDef.rule(10.0, Color.ORANGE, null);
		def.gprint("busy", "AVERAGE", " Average:@5.1@s%");
		def.gprint("busy", "LAST", "Current: @5.1@s%");
		def.comment("\n");
		def.comment("\n");
		def.comment("Server load\n");
		def.comment("  ");
		def.line("load", new Color(0x00,0x00,0x00), "Load average (5 min)" );
		//gDef.area("load", Color.RED, " hmm \n");
		//gl.stack("p75t90", Color.GREEN, " hmm");
		def.comment("             ");
		def.gprint("load", "MIN", " Minimum: @5.2@s");
		def.gprint("load", "MAX", "Maximum: @6.2@s");
		def.comment("\n");
		def.comment("                                ");
		def.comment("         ");
		def.gprint("load", "AVERAGE", "Average: @5.2@s");
		def.gprint("load", "LAST", "Current: @6.2@s\n");
		def.comment("\n");
		def.comment("-------------------------------------------------------------------------------@c");
		//gl.vrule( new GregorianCalendar(2003, 7, 24, 9, 00), Color.BLUE, "9am", 2f );
		//gl.vrule( new GregorianCalendar(2003, 7, 24, 17, 00), Color.BLUE, "5pm", 3f );
		def.comment("Generated: " + new Date() + "@R");
		// ==============================================================

		stop = System.currentTimeMillis();
		profiler.profiles[demoNum - 1].defCreation[DNUM] = (stop - start);

		// Create graph
		graph		= new RrdGraph( def );
		graph.saveAsPNG( "/time_demo" + demoNum + "-" + DNUM + ".png", 0, 0 );		// Default size	

		stop = System.currentTimeMillis();
		profiler.profiles[demoNum - 1].totalCreation[DNUM] = (stop - start);

		addTimingInfo( demoNum, Util.timeList );
	}
}
