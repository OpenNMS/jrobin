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

import jrobin.graph.*;

/**
 * @author cbld
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JRobinComplexGraph {

	public static void main(String[] args) 
	{
		GregorianCalendar start = new GregorianCalendar(2003, 7, 23, 0, 0);
		GregorianCalendar end 	= new GregorianCalendar(2003, 7, 24, 0, 0);

		RrdGraphDef gDef 		= new RrdGraphDef();
		
		try 
		{
			gDef.setTimePeriod(start, end);
			gDef.setTitle("Server load baseline projection");
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
			gDef.area("load", new Color(0x66,0x99,0xcc), " 0 - 25%");
			gDef.area("p25t50", new Color(0x00,0x66,0x99), "25 - 50%");
			gDef.comment("             ");
			gDef.gprint("busy", "MIN", "Minimum:@5.1@s%");
			gDef.gprint("busy", "MAX", "Maximum: @5.1@s%");
			gDef.comment("\n");
			gDef.comment("  ");
			gDef.area("p50t75", new Color(0x66,0x66,0x00), "50 - 75%");
			gDef.area("p75t90", new Color(0xff,0x66,0x00), "75 - 90%");
			gDef.area("p90t100", new Color(0xcc,0x33,0x00), "90 - 100%");
			gDef.rule(10.0, Color.ORANGE, null);
			gDef.gprint("busy", "AVERAGE", " Average:@5.1@s%");
			gDef.gprint("busy", "LAST", "Current: @5.1@s%");
			gDef.comment("\n");
			gDef.comment("\n");
			gDef.comment("Server load\n");
			gDef.comment("  ");
			gDef.line("load", new Color(0x00,0x00,0x00), "Load average (5 min)" );
			//gDef.area("load", Color.RED, " hmm \n");
			//gDef.stack("p75t90", Color.GREEN, " hmm \n");
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
			gDef.comment("Generated: " + new Date() + "@r");
			//gDef.setBackColor( Color.ORANGE );
			gDef.setValueAxisLabel("server load");
			//gDef.setGridX( false );
			//gDef.setGridY( false );
			gDef.setImageBorder( Color.DARK_GRAY, 1 );
			gDef.setFrontGrid(false);
			gDef.setShowLegend(true);
			// Create actual graph
			RrdGraph graph = new RrdGraph(gDef);
			graph.saveAsPNG("/zzzzzz.png", 0, 0);
	
			
			// -- New graph
			RrdGraphDef gd = new RrdGraphDef();
			gd.setBackColor( Color.WHITE );
			gd.setTimePeriod( start, end );
			gd.datasource("in2", "c:/test.rrd", "ifInOctets", "AVERAGE");
			gd.datasource("out2", "c:/test.rrd", "ifOutOctets", "AVERAGE");
			gd.datasource("in", "in2,8,*");
			gd.datasource("out", "out2,8,*");
			gd.area("in", Color.GREEN, null);
			gd.line("out", Color.BLUE, null);
			gd.setRigidGrid(true);			
			RrdGraph graph2 = new RrdGraph(gd);
			graph2.saveAsPNG("/traff.png", 0, 0);
			
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
						
			graph2 = new RrdGraph(gd);
			graph2.saveAsPNG("/pkts.png", 0, 0);
			
			gd = new RrdGraphDef();
			gd.setBackColor( Color.WHITE );
			gd.setTimePeriod( start, end );
			gd.datasource("ftp", "c:/test.rrd", "ftpUsers", "AVERAGE");
			gd.area("ftp", Color.BLUE, null);
									
			graph2 = new RrdGraph(gd);
			graph2.saveAsPNG("/ftp.png", 0, 0);
			
						
			//graph.saveAsPNG("c:/demo.png", 495, 200);
		} 
		catch (Exception e) {
			System.err.println( e.getMessage() );
			System.exit(1);
		}
		
		System.out.println("Complex demo graph created.");
	}
}

