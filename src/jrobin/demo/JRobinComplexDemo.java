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

import java.awt.Color;
import java.util.Date;
import java.util.GregorianCalendar;
import java.text.SimpleDateFormat;

import jrobin.core.*;
import jrobin.graph2.*;

/**
 * <p>Extended graphing demo for JRobin.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class JRobinComplexDemo 
{
	static String demofile	= "/complexdemo.rrd";
	
	static void println(String msg) {
		System.out.println(msg);
	}
	
	public static void createDatabase()
	{
		try
		{
			println( "- Preparing to import complexdemo.xml" );
			
			// Import database from XML
			RrdDb rrd	= new RrdDb( demofile, "/complexdemo.xml" );
			println( "- Database complexdemo.rrd has been succesfully imported" );
			
			rrd.close();
		}
		catch ( Exception e ) {
			println( "Error occurred while creating database: " + e.getMessage() );
		}
		
	}
	
	public static void createGraphs()
	{
		GregorianCalendar start, stop;
		
		RrdDbPool pool 	= RrdDbPool.getInstance();
		RrdGraph graph	= new RrdGraph( pool );
		
		try
		{
			// Create traffic overview of a week
			println( "- Creating graph 1: complexdemo1.png" );
			start	= new GregorianCalendar( 2003, 9, 20 );
			stop	= new GregorianCalendar( 2003, 9, 27 );
			
			RrdGraphDef def	= new RrdGraphDef( start, stop );
			def.setImageBorder( Color.GRAY, 1 );
			def.setTitle( "JRobinComplexDemo@Ldemo graph 1@r\nNetwork traffic overview" );
			def.setVerticalLabel( "bits per second" );
			def.datasource( "ifInOctets", demofile, "ifInOctets", "AVERAGE" );
			def.datasource( "ifOutOctets", demofile, "ifOutOctets", "AVERAGE" );
			def.datasource( "bitIn", "ifInOctets,8,*" );
			def.datasource( "bitOut", "ifOutOctets,8,*" );
			def.comment(" ");
			def.area( "bitIn", new Color(0x00, 0xFF, 0x00), "Incoming traffic " );
			def.line( "bitOut", new Color(0x00, 0x00, 0x33), "Outgoing traffic\n\n" );
			def.gprint( "bitIn", "MAX", "Max:   @6.1 @sbit/s");
			def.gprint( "bitOut", "MAX", "      @6.1 @sbit/s\n");
			def.gprint( "bitIn", "MIN", "Min:   @6.1 @sbit/s");
			def.gprint( "bitOut", "MIN", "      @6.1 @sbit/s");
			def.comment( "       Connection:  100 Mbit/s\n" );
			def.gprint( "bitIn", "AVG", "Avg:   @6.1 @sbit/s");
			def.gprint( "bitOut", "AVG", "      @6.1 @sbit/s");
			def.comment( "       Duplex mode: FD - fixed\n\n");
			def.gprint( "bitIn", "LAST", "Cur:   @6.1 @sbit/s");
			def.gprint( "bitOut", "LAST", "      @6.1 @sbit/s\n\n");
			def.comment( "[ courtesy of www.cherrymon.org ]@L" );
			def.comment( "Generated: " + timestamp() + "  @r" );
			
			graph.setGraphDef( def );
			graph.saveAsPNG( "/complexdemo1.png" );
			// ---------------------------------------------------------------
			
			
			// Create server load and cpu usage of a day
			println( "- Creating graph 2: complexdemo2.png" );
			start	= new GregorianCalendar( 2003, 8, 19 );
			stop	= new GregorianCalendar( 2003, 8, 20 );
			
			def		= new RrdGraphDef( start, stop );
			def.setImageBorder( Color.GRAY, 1 );
			def.setTitle( "JRobinComplexDemo@Ldemo graph 2@r\nServer load and CPU utilization" );
			def.datasource("load", demofile, "serverLoad", "AVERAGE");
			def.datasource("user", demofile, "serverCPUUser", "AVERAGE");
			def.datasource("nice", demofile, "serverCPUNice", "AVERAGE");
			def.datasource("system", demofile, "serverCPUSystem", "AVERAGE");
			def.datasource("idle", demofile, "serverCPUIdle", "AVERAGE");
			def.datasource("total", "user,nice,+,system,+,idle,+");
			def.datasource("busy", "user,nice,+,system,+,total,/,100,*");
			def.datasource("p25t50", "busy,25,GT,busy,50,LE,load,0,IF,0,IF");
			def.datasource("p50t75", "busy,50,GT,busy,75,LE,load,0,IF,0,IF");
			def.datasource("p75t90", "busy,75,GT,busy,90,LE,load,0,IF,0,IF");
			def.datasource("p90t100", "busy,90,GT,load,0,IF");
			def.comment( "CPU utilization (%)\n " );
			def.area("load", new Color(0x66,0x99,0xcc), " 0 - 25%");
			def.area("p25t50", new Color(0x00,0x66,0x99), "25 - 50%");
			def.comment("             ");
			def.gprint("busy", "MIN", "Minimum:@5.1@s%");
			def.gprint("busy", "MAX", "Maximum:@5.1@s%\n ");
			def.area("p50t75", new Color(0x66,0x66,0x00), "50 - 75%");
			def.area("p75t90", new Color(0xff,0x66,0x00), "75 - 90%");
			def.area("p90t100", new Color(0xcc,0x33,0x00), "90 - 100%");
			def.gprint("busy", "AVG", " Average:@5.1@s%");
			def.gprint("busy", "LAST", "Current:@5.1@s%\n ");
			def.comment( "\nServer load\n " );
			def.line("load", new Color(0x00,0x00,0x00), "Load average (5 min)@L" );
			def.gprint("load", "MIN", "Minimum:@5.2@s%");
			def.gprint("load", "MAX", "Maximum:@5.2@s% @r ");
			def.hrule( 0.7, new Color( 0xFF, 0xCC, 0x00), "Average load@L" );
			def.gprint("load", "AVG", "Average:@5.2@s%");
			def.gprint("load", "LAST", "Current:@5.2@s% @r");
			def.comment( "\n\n[ courtesy of www.cherrymon.org ]@L" );
			def.comment( "Generated: " + timestamp() + "  @r" );
			
			graph.setGraphDef( def );
			graph.saveAsPNG( "/complexdemo2.png" );
			
			println( "- Creating graph 2: complexdemo2.jpg" );
			graph.saveAsJPEG( "/complexdemo2.jpg", 640, 480, 1.0f );
			// ---------------------------------------------------------------
			
			
			// Create ftp graph for a month
			println( "- Creating graph 3: complexdemo3.png");
			start	= new GregorianCalendar( 2003, 8, 19, 12, 00 );
			stop	= new GregorianCalendar( 2003, 8, 20, 12, 00 );
			
			def		= new RrdGraphDef( start, stop );
			def.setImageBorder( Color.GRAY, 1 );
			def.setFrontGrid(false);
			def.setTitle( "JRobinComplexDemo@Ldemo graph 3@r\nFTP Usage" );
			def.datasource( "ftp", demofile, "ftpUsers", "AVERAGE" );
			def.line( "ftp", new Color(0x00, 0x00, 0x33), "FTP connections" );
			def.gprint( "ftp", "AVG", "( average: @0,");
			def.gprint( "ftp", "MIN", "never below: @0 )\n\n");
			def.comment( "  Usage spread:" );
			def.area( 	new GregorianCalendar( 2003, 8, 19, 17, 00 ), Double.MIN_VALUE, 
						new GregorianCalendar( 2003, 8, 19, 23, 00 ), Double.MAX_VALUE, 
						Color.RED, "peak period" );
			def.area( 	new GregorianCalendar( 2003, 8, 20, 5, 00 ), Double.MIN_VALUE, 
						new GregorianCalendar( 2003, 8, 20, 8, 30 ), Double.MAX_VALUE, 
						Color.LIGHT_GRAY, "quiet period\n" );
			def.comment( "  Rise/descend:" );
			def.area( "ftp", new Color(0x00, 0x00, 0x33), null );
			def.line( 	new GregorianCalendar( 2003, 8, 19, 12, 00 ), 8, 
						new GregorianCalendar( 2003, 8, 19, 20, 30 ), 15,
						Color.PINK, "climb slope", 2 );
			def.line( 	new GregorianCalendar( 2003, 8, 19, 20, 30 ), 15, 
						new GregorianCalendar( 2003, 8, 20, 8, 00 ), 4,
						Color.CYAN, "fall-back slope\n", 2 );
			def.vrule( new GregorianCalendar( 2003, 8, 20 ), Color.YELLOW, null );
			def.comment( "\n\n[ courtesy of www.cherrymon.org ]@L" );
			def.comment( "Generated: " + timestamp() + "  @r" );
			
			graph.setGraphDef( def );
			graph.saveAsPNG( "/complexdemo3.png" );
			
			println( "- Creating graph 3: complexdemo3.jpg");
			graph.saveAsJPEG( "/complexdemo3.jpg", 1.0f );
			// ---------------------------------------------------------------
			
		}
		catch ( Exception e ) {
			println( "Error occurred while creating the graph: " + e.getMessage() );
		}
		
	}
	
	public static String timestamp()
	{
		SimpleDateFormat df = new SimpleDateFormat( "dd/MM/yyyy HH:mm" );
		
		return df.format( new Date() );
	}
	
	public static void main(String[] args) 
	{
		createDatabase();
		
		createGraphs();
		
		println( "- Demo finished." );
	}
}
