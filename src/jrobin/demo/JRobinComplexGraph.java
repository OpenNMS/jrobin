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
/*
 * Created on 26-aug-2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package jrobin.demo;

import java.util.*;
import java.awt.*;
import javax.swing.*;

import jrobin.graph.*;
import jrobin.graph2.RrdGraph;

/**
 * @author cbld
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JRobinComplexGraph {

	public static void main(String[] args) 
	{
		GregorianCalendar start = new GregorianCalendar(2003, 7, 24, 00, 00);
		GregorianCalendar end 	= new GregorianCalendar(2003, 7, 25, 00, 00);
		
		try 
		{
			// --------------------------------------------------------
	
			// Create the graphdef to be used
			jrobin.graph2.RrdGraphDef gl 	= new jrobin.graph2.RrdGraphDef();
			gl.setTimePeriod( start, end );
			//gl.setTitleFont( new Font("Lucida Sans Typewriter", Font.PLAIN, 10) );
			gl.setTitle( "+------------------------------------------------------------------+@c"
							+ "| Server load...@Land@C...CPU usage |@r" 
							+ "+------------------------------------------------------------------+@c"
						);
			gl.setVerticalLabel("server load");
			gl.setBackColor( Color.DARK_GRAY );
			gl.setCanvasColor( Color.LIGHT_GRAY );
			gl.setImageBorder( Color.BLACK, 1 );
			gl.setDefaultFontColor( Color.WHITE );
			gl.setTitleFontColor( Color.GREEN );
			gl.setMajorGridColor(Color.YELLOW);
			gl.setMinorGridColor( new Color( 130, 30, 30) );
			gl.setFrameColor( Color.BLACK );
			gl.setAxisColor( Color.RED );
			gl.setArrowColor( Color.GREEN );
			//gl.setGridX( false );
			//gl.setGridY( false );
			//gl.setFrontGrid(false);
			gl.setShowLegend(true);
			//gl.setUnitsExponent(3);
			//gl.setAntiAliasing(false);
			gl.setGridRange( 0, 1, false );
			//gl.setBackground( "/demo6.png" );
			//gl.setOverlay( "/overview.gif" );
			gl.datasource("load", "c:/test.rrd", "serverLoad", "AVERAGE");
			gl.datasource("user", "c:/test.rrd", "serverCPUUser", "AVERAGE");
			gl.datasource("nice", "c:/test.rrd", "serverCPUNice", "AVERAGE");
			gl.datasource("system", "c:/test.rrd", "serverCPUSystem", "AVERAGE");
			gl.datasource("idle", "c:/test.rrd", "serverCPUIdle", "AVERAGE");
			gl.datasource("total", "user,nice,+,system,+,idle,+");
			gl.datasource("busy", "user,nice,+,system,+,total,/,100,*");
			gl.datasource("p25t50", "busy,25,GT,busy,50,LE,load,0,IF,0,IF");
			gl.datasource("p50t75", "busy,50,GT,busy,75,LE,load,0,IF,0,IF");
			gl.datasource("p75t90", "busy,75,GT,busy,90,LE,load,0,IF,0,IF");
			gl.datasource("p90t100", "busy,90,GT,load,0,IF");
			
			gl.comment("CPU utilization (%)\n");
			gl.comment("  ");
			//gl.stack("load", Color.RED, null);
			gl.area("load", new Color(0x66,0x99,0xcc), " 0 - 25%");
			gl.area("p25t50", new Color(0x00,0x66,0x99), "25 - 50%");
			gl.comment("             ");
			gl.gprint("busy", "MIN", "Minimum:@5.1@s%");
			gl.gprint("busy", "MAX", "Maximum: @5.1@S%");
			gl.comment("\n");
			gl.comment("  ");
			gl.area("p50t75", new Color(0x66,0x66,0x00), "50 - 75%");
			gl.area("p75t90", new Color(0xff,0x66,0x00), "75 - 90%");
			gl.area("p90t100", new Color(0xcc,0x33,0x00), "90 - 100%");
			//gDef.rule(10.0, Color.ORANGE, null);
			gl.gprint("busy", "AVERAGE", " Average:@5.1@s%");
			gl.gprint("busy", "LAST", "Current: @5.1@s%");
			gl.comment("\n");
			gl.comment("\n");
			gl.comment("Server load\n");
			gl.comment("  ");
			gl.line("load", new Color(0x00,0x00,0x00), "Load average (5 min)" );
			//gDef.area("load", Color.RED, " hmm \n");
			//gl.stack("p75t90", Color.GREEN, " hmm");
			gl.comment("             ");
			gl.gprint("load", "MIN", " Minimum: @5.2@s");
			gl.gprint("load", "MAX", "Maximum: @6.2@s");
			gl.comment("\n");
			gl.comment("                                ");
			gl.comment("         ");
			gl.gprint("load", "AVERAGE", "Average: @5.2@s");
			gl.gprint("load", "LAST", "Current: @6.2@s\n");
			//gl.hrule( 3.0, null, "legende", 1);
			//gl.vrule( new GregorianCalendar(2003, 7, 24, 9, 00), Color.BLUE, "9am", 2 );
			gl.area( new GregorianCalendar(2003, 7, 24, 9, 00), Double.MIN_VALUE, new GregorianCalendar(2003, 7, 24, 17, 00), Double.MAX_VALUE, Color.ORANGE, "deviation@r" );
			gl.area( new GregorianCalendar(2003, 7, 24, 8, 00), Double.MIN_VALUE, new GregorianCalendar(2003, 7, 24, 10, 00), Double.MAX_VALUE, Color.PINK, null );
			gl.area( new GregorianCalendar(2003, 7, 24, 16, 00), Double.MIN_VALUE, new GregorianCalendar(2003, 7, 24, 18, 00), 12, Color.PINK, null );
			gl.stack("load", Color.BLUE, "hmm");
			gl.comment("\n-------------------------------------------------------------------------------@c");
			
			//gl.vrule( new GregorianCalendar(2003, 7, 24, 17, 00), Color.BLUE, "5pm", 3f );
			gl.comment("Generated: " + new Date() + "@L");
			
			// Create the actual graph
			long s1 = Calendar.getInstance().getTimeInMillis();
			jrobin.graph2.RrdGraph gf 		= new jrobin.graph2.RrdGraph();
			
			// Create image as PNG and as JPEG
			gf.setGraphDef( gl );
			gf.saveAsPNG( "/demo_graph.png" );
			//gf.saveAsJPEG( "/demo_graph.jpg", 1.0f );
			
			
			gl = new jrobin.graph2.RrdGraphDef();
			gl.setTimePeriod( start, end );
			gl.setTitle("ahha");
			gl.datasource("in2", "c:/test.rrd", "ifInOctets", "AVERAGE");
			gl.datasource("out2", "c:/test.rrd", "ifOutOctets", "AVERAGE");
			gl.datasource("in", "in2,8,*");
			gl.datasource("out", "out2,8,*");
			gl.area("in", Color.RED, "Incoming traffic");
			gl.gprint("in", "AVERAGE", "(average: @5.2 @sbit/s)\n");
			gl.line("out", Color.BLUE, "Outgoing traffic");
			gl.gprint("out", "AVERAGE", "(average: @6.2 @sbit/s)");
			gf.setGraphDef( gl );			
			
			gf.saveAsPNG("/demo_graph2.png", 0, 200);
			
			// Wrap up
			gf.closeFiles();
			
			// Print out timing information for the new package API
			long s2 = Calendar.getInstance().getTimeInMillis();
			System.err.println( "New package: " + (s2 - s1) + " ms" );
			// --------------------------------------------------------

			
			// --------------------------------------------------------
			RrdGraphDef gDef 		= new RrdGraphDef();
			gDef.setTimePeriod(start, end);
			
			gDef.setTitle("Server load\nCPU usage");
			gDef.setValueAxisLabel("server load");
			gDef.setBackColor( Color.DARK_GRAY );
			gDef.setCanvasColor( Color.LIGHT_GRAY );
			gDef.setImageBorder( Color.BLACK, 1 );
			gDef.setFontColor( Color.WHITE );
			gDef.setMajorGridColor(Color.YELLOW);
			gDef.setMinorGridColor( new Color( 130, 30, 30) );
			gDef.setFrameColor( Color.BLACK );
			gDef.setAxisColor( Color.RED );
			gDef.setArrowColor( Color.GREEN );
			// gDef.setGridX( false );
			// gDef.setGridY( false );
			// gDef.setFrontGrid(false);
			gDef.setShowLegend(true);
			// gDef.setAntiAliasing(false);
			
			gDef.datasource("load", "c:/test.rrd", "serverLoad", "AVERAGE");
			gDef.datasource("user", "c:/test.rrd", "serverCPUUser", "AVERAGE");
			gDef.datasource("nice", "c:/test.rrd", "serverCPUNice", "AVERAGE");
			gDef.datasource("system", "c:/test.rrd", "serverCPUSystem", "AVERAGE");
			gDef.datasource("idle", "c:/test.rrd", "serverCPUIdle", "AVERAGE");
			gDef.datasource("total", "user,nice,+,system,+,idle,+");
			gDef.datasource("busy", "user,nice,+,system,+,total,/,100,*");
			gDef.datasource("p25t50", "busy,25,GT,busy,50,LE,load,0,IF,0,IF");
			gDef.datasource("p50t75", "busy,50,GT,busy,75,LE,load,0,IF,0,IF");
			gDef.datasource("p75t90", "busy,75,GT,busy,90,LE,load,0,IF,0,IF");
			gDef.datasource("p90t100", "busy,90,GT,load,0,IF");
			
			gDef.comment("CPU utilization (%)\n");
			gDef.comment("  ");
			//gDef.hrule( 7.0, Color.YELLOW, null, 10f);
			gDef.area("load", new Color(0x66,0x99,0xcc), " 0 - 25%");
			gDef.area("p25t50", new Color(0x00,0x66,0x99), "25 - 50%");
			gDef.comment("             ");
			gDef.gprint("busy", "MIN", "Minimum:@5.1@s%");
			gDef.gprint("busy", "MAX", "Maximum: @5.1@S%");
			gDef.comment("\n");
			gDef.comment("  ");
			gDef.area("p50t75", new Color(0x66,0x66,0x00), "50 - 75%");
			gDef.area("p75t90", new Color(0xff,0x66,0x00), "75 - 90%");
			gDef.area("p90t100", new Color(0xcc,0x33,0x00), "90 - 100%");
			//gDef.rule(10.0, Color.ORANGE, null);
			gDef.gprint("busy", "AVERAGE", " Average:@5.1@s%");
			gDef.gprint("busy", "LAST", "Current: @5.1@s%");
			gDef.comment("\n");
			gDef.comment("\n");
			gDef.comment("Server load\n");
			gDef.comment("  ");
			gDef.line("load", new Color(0x00,0x00,0x00), "Load average (5 min)" );
			//gDef.area("load", Color.RED, " hmm \n");
			//gDef.stack("p75t90", Color.GREEN, " hmm");
			gDef.comment("             ");
			gDef.gprint("load", "MIN", " Minimum: @5.2@s");
			gDef.gprint("load", "MAX", "Maximum: @6.2@s");
			gDef.comment("\n");
			gDef.comment("                                ");
			gDef.comment("         ");
			gDef.gprint("load", "AVERAGE", "Average: @5.2@s");
			gDef.gprint("load", "LAST", "Current: @6.2@s");
			gDef.comment("\n");
			gDef.comment("\n");
			gDef.comment("-------------------------------------------------------------------------------@c");
			//gDef.vrule( new GregorianCalendar(2003, 7, 24, 9, 00), Color.BLUE, "9am", 2f );
			//gDef.vrule( new GregorianCalendar(2003, 7, 24, 17, 00), Color.BLUE, "5pm", 3f );
			gDef.comment("Generated: " + new Date() + "@r");
			
			// Create actual graph
			s1 = Calendar.getInstance().getTimeInMillis();
			
			jrobin.graph.RrdGraph graph = new jrobin.graph.RrdGraph(gDef);
			graph.saveAsPNG("/zzzzzz.png", 0, 0);
			//graph.saveAsJPEG("/zzzzzz.jpg", 0, 0, 1f);
								
			//gDef.setOverlay("/pkts.png");
								
			
			//gDef.setChartLeftPadding( 40 );
			//gDef.setTimeAxis( TimeAxisUnit.HOUR, 6, TimeAxisUnit.DAY, 1, "EEEEE dd MMM", true );
			//gDef.setValueAxis( 2.5, 5 );
			
			
			// --------------------------------------------------------
			
			
					
			// -- New graph
			RrdGraphDef gd = new RrdGraphDef();
			//gd.setBackColor( Color.WHITE );
			gd.setTimePeriod( start, end );
			//gd.setBackground("/ftp.png");
			gd.datasource("in2", "c:/test.rrd", "ifInOctets", "AVERAGE");
			gd.datasource("out2", "c:/test.rrd", "ifOutOctets", "AVERAGE");
			gd.datasource("in", "in2,8,*");
			gd.datasource("out", "out2,8,*");
			gd.area("in", Color.GREEN, null);
			gd.line("out", Color.BLUE, null);
			gd.gprint("out", "AVERAGE", " Minimum: @5.2@s");
			gd.gprint("in", "AVERAGE", "Maximum: @6.2@s");
			gd.setRigidGrid(true);			
			jrobin.graph.RrdGraph graph2 = new jrobin.graph.RrdGraph(gd);
			graph2.saveAsPNG("/traff.png", 0, 0);
			
			

			// Print out timings for the old graph API
			s2 = Calendar.getInstance().getTimeInMillis();
			System.err.println( "Old package: " + (s2 - s1) + " ms" );
			
			System.exit(0);
			
			
			//////////////////////////////
			gd = new RrdGraphDef();
			gd.setBackColor( Color.WHITE );
			gd.setTimePeriod( start, end );
			gd.datasource("in2", "c:/test.rrd", "ifInUcastPkts", "AVERAGE");
			gd.datasource("out2", "c:/test.rrd", "ifOutUcastPkts", "AVERAGE");
			gd.datasource("in", "in2,8,*");
			gd.datasource("out", "out2,8,*");
			gd.area("in", Color.GREEN, null);
			gd.line("out", Color.BLUE, null);
			//gd.setUnitsExponent(6);			
			graph2 = new jrobin.graph.RrdGraph(gd);
			graph2.saveAsPNG("/pkts.png", 0, 0);
			
			gd = new RrdGraphDef();
			gd.setBackColor( Color.WHITE );
			gd.setTimePeriod( start, end );
			gd.datasource("ftp", "c:/test.rrd", "ftpUsers", "AVERAGE");
			gd.area("ftp", Color.BLUE, null);
					
			graph2 = new jrobin.graph.RrdGraph(gd);
			graph2.saveAsPNG("/ftp.png", 0, 0);
			
			/*
			try {
				JFrame frame = new JFrame("Simple chartpanel test");
	
				frame.getContentPane().add(graph2.getChartPanel());
				frame.pack();
				frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
			*/
						
			//graph.saveAsPNG("c:/demo.png", 495, 200);
		} 
		catch (Exception e) {
			System.err.println( e.getMessage() );
			System.exit(1);
		}
		
		System.out.println("Complex demo graph created.");
	}
}

