/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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
package org.jrobin.demo.graph;

import org.jrobin.core.Util;
import org.jrobin.core.RrdException;
import org.jrobin.graph.*;

import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;

/**
 * <p>This is a small demo that creates a graph based on the export XML from the ExportExportDemo.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class ExportImportDemo
{
	private static String exportRrd1 		= ExportExportDemo.exportRrd1;
	private static String exportRrd2 		= ExportExportDemo.exportRrd2;
	private static String exportCombined 	= Util.getJRobinDemoPath( "export-combined.xml" );
	private static String graphFile 		= Util.getJRobinDemoPath( "export-graph.png" );
	private static String demoResources 	= "";

	private static void println( String str ) {
		System.out.println( str );
	}

	private static void prepare( String[] args )
	{
		/*
		if ( args.length != 1 )
		{
			println( "Usage: ExportImportDemo <path_to_demo_resources>" );
			println( "  The only argument to this program should be the path to the JRobin" );
			println( "  demo resources.  These are normally included in the JRobin distribution" );
			println( "  in the <jrobin>/res/demo directory." );

			System.exit( 1 );
		}

		demoResources = new File(args[0]).getAbsolutePath() + "/";
		*/
		demoResources = Util.getJRobinHomeDirectory() + "/res/demo/";
	}

	public static void main( String[] args ) throws RrdException, IOException
	{
		prepare( args );

		println( "+-----------------------------------------------------------------------------------+" );
		println( "|                             JRobin ExportImportDemo                               |" );
		println( "|                                                                                   |" );
		println( "| This demo supposes the ExportExportDemo has been run and the necessary data has   |" );
		println( "| been exported to the jrobin-demo directory.  The ExportImportDemo creates a graph |" );
		println( "| containing the data from the separate export XML files, the configuration of the  |" );
		println( "| graph is in the RrdGraphDef xml file in the directory passed as argument on the   |" );
		println( "| command line.                                                                     |" );
		println( "|                                                                                   |" );
		println( "+-----------------------------------------------------------------------------------+" );

		long execStart						= System.currentTimeMillis();

		// -- Read in the graph def xml
		RrdGraphDefTemplate xmlTemplate		= new RrdGraphDefTemplate( new File(demoResources + "export-graphdef.xml") );

		// -- The data we're interested in is from the 3rd of july 2004, to the 4th
		GregorianCalendar start 			= new GregorianCalendar( 2004, GregorianCalendar.JULY, 3 );
		GregorianCalendar end 				= new GregorianCalendar( 2004, GregorianCalendar.JULY, 4 );

		xmlTemplate.setVariable( "start", start );
		xmlTemplate.setVariable( "end", end );
		xmlTemplate.setVariable( "export1", exportRrd1 );
		xmlTemplate.setVariable( "export2", exportRrd2 );

		Util.getLapTime();

		// -- Create the graph
		println( ">>> Creating graph image from XML graph def" );
		RrdGraphDef graphDef				= xmlTemplate.getRrdGraphDef();

		RrdGraph graph						= new RrdGraph( graphDef );
		graph.saveAsPNG( graphFile );
		println( ">>> File saved: " + graphFile + " " + Util.getLapTime() + "\n" );

		// -- Dump the combined export
		println( ">>> Dumping combined export xml" );
		ExportData combinedData				= graph.getExportData();
		combinedData.exportXml( exportCombined );
		println( ">>> File saved: " + exportCombined + " " + Util.getLapTime() + "\n" );

		// -- Print out information
		println( ">>> Retrieving AVERAGE outoing traffic from combined export" );
		println( combinedData.print( "eth0-2", "AVERAGE", "  From eth0 data: @5.2 @sbit/s" ) );
		println( combinedData.print( "eth1-2", "AVERAGE", "  From eth1 data: @5.2 @sbit/s" ) );

		long execStop						= System.currentTimeMillis();

		println( "\n>>> Demo finished in " + (execStop - execStart) + " milliseconds" );
	}
}
