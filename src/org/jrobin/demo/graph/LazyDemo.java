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
package org.jrobin.demo.graph;

import org.jrobin.core.Util;
import org.jrobin.core.RrdException;
import org.jrobin.graph.RrdGraphDef;
import org.jrobin.graph.RrdGraph;
import org.jrobin.graph.FetchSourceList;

import java.io.File;
import java.io.IOException;
import java.awt.*;
import java.util.GregorianCalendar;

/**
 * <p>This demo shows the use of the FetchSourceList class, the RrdOpener class
 * and the RrdGraphDef setLazy() method.</p>
 *
 * @author Arne Vandamme (cobralord@cherrymon.org)
 */
public class LazyDemo
{
	private static String rrd1				= "eth0.rrd";
	private static String rrd2				= "eth1.rrd";
	private static String graph1			= Util.getJRobinDemoPath( "lazy-graph1.png" );
	private static String graph2			= Util.getJRobinDemoPath( "lazy-graph2.png" );
	private static String graph3			= Util.getJRobinDemoPath( "lazy-graph3.png" );
	private static String graph4			= Util.getJRobinDemoPath( "lazy-graph4.png" );
	private static String demoResources 	= "";

	private static int runCount				= 0;
	private static long runStart, runStop;
	private static long[] runTimes			= new long[4];

	private static RrdGraph graph;
	private static RrdGraphDef graphDef;

	private static GregorianCalendar start1, end1, start2, end2, start3, end3, start4, end4;

	static
	{
		// Statically initialize the start and end times
		start1		= new GregorianCalendar( 2004, GregorianCalendar.JULY, 3 );
		end1		= new GregorianCalendar( 2004, GregorianCalendar.JULY, 4 );

		start2		= new GregorianCalendar( 2004, GregorianCalendar.JULY, 1 );
		end2		= new GregorianCalendar( 2004, GregorianCalendar.JULY, 8 );

		start3		= new GregorianCalendar( 2004, GregorianCalendar.JUNE, 1 );
		end3		= new GregorianCalendar( 2004, GregorianCalendar.JULY, 1 );

		start4		= new GregorianCalendar( 2004, GregorianCalendar.JULY, 1 );
		end4		= new GregorianCalendar( 2004, GregorianCalendar.AUGUST, 1 );
	}

	private static void println( String str ) {
		System.out.println( str );
	}

	private static void prepare( String[] args )
	{
		if ( args.length != 1 )
		{
			println( "Usage: LazyDemo <path_to_demo_resources>" );
			println( "  The only argument to this program should be the path to the JRobin" );
			println( "  demo resources.  These are normally included in the JRobin distribution" );
			println( "  in the <jrobin>/res/demo directory." );

			System.exit( 1 );
		}

		demoResources 	= new File(args[0]).getAbsolutePath() + "/";
		rrd1			= demoResources + rrd1;
		rrd2			= demoResources + rrd2;
	}

	private static void createGraphs() throws RrdException, IOException
	{
		runStart				= System.currentTimeMillis();

		// -- First graph is one day in time
		graphDef.setTimePeriod( start1, end1 );
		graph.saveAsPNG( graph1 );

		// -- Second is one week in time
		graphDef.setTimePeriod( start2, end2 );
		graph.saveAsPNG( graph2 );

		// -- Third is one month in time
		graphDef.setTimePeriod( start3, end3 );
		graph.saveAsPNG( graph3 );

		// -- Fourth is again one month in time
		graphDef.setTimePeriod( start4, end4 );
		graph.saveAsPNG( graph4 );

		runStop					= System.currentTimeMillis();
		runTimes[ runCount++ ]	= (runStop - runStart);
	}

	public static void main( String[] args ) throws RrdException, IOException
	{
		prepare( args );

		println( "+-----------------------------------------------------------------------------------+" );
		println( "|                                JRobin LazyDemo                                    |" );
		println( "|                                                                                   |" );
		println( "| This demo illustrates the use of RrdOpener, FetchSourceList and the lazy flag of  |" );
		println( "| the RrdGraphDef.                                                                  |" );
		println( "|                                                                                   |" );
		println( "+-----------------------------------------------------------------------------------+" );

		long execStart		= System.currentTimeMillis();

		// -- Create the basic RrdGraphDef and RrdGraph
		graphDef			= new RrdGraphDef();
		graphDef.area( "bitsOutEth0", Color.RED, "Outgoing eth0" );
		graphDef.stack( "bitsOutEth1", Color.GREEN, "Outgoing eth1" );
		graphDef.stack( "bitsInEth0", Color.BLUE, "Incoming eth0" );
		graphDef.stack( "bitsInEth1", Color.YELLOW, "Incoming eth1" );
		graph				= new RrdGraph( graphDef );

		// -- Put the datasources in a FetchSourceList and add it to the graphDef
		// -- The FetchSourceList also uses our LazyDemoOpener, overriding the default RrdOpener for a graph
		// -- Lock the RrdOpener, this way it is not overridden by graph generation
		FetchSourceList fsl	= new FetchSourceList( 2 , false, true, new LazyDemoOpener() );
		fsl.add( "bitsOutEth0", rrd1, "ifOutOctets", "AVERAGE" );
		fsl.add( "bitsInEth0", rrd1, "ifInOctets", "AVERAGE" );
		fsl.add( "bitsOutEth1", rrd2, "ifOutOctets", "AVERAGE" );
		fsl.add( "bitsInEth1", rrd2, "ifInOctets", "AVERAGE" );
		graphDef.setDatasources( fsl );

		// -- Run the graphs 2 times, second time with lazy, but the FetchSourceList should not be persistent
		println( "\n>>> GRAPH RUN 1" );
		graphDef.setLazy( false );			// Disable lazy flag, graphs will be generated
		createGraphs();

		println( "\n>>> GRAPH RUN 2" );
		graphDef.setLazy( true );			// Enable lazy flag, graphs will be skipped
		createGraphs();

		// -- Now run the graphs 2 times again, second time with lazy, but now the FetchSourceList is persistent

		fsl.setPersistent( true );			// Mark the FSL as persistent, RRDs will only be opened once, not closed again

		println( "\n>>> Manually opening all RRD references" );
		fsl.openAll();						// Manually open all RRDs, this is more obvious than the implicit opening in graph generation

		println( "\n>>> GRAPH RUN 3" );
		graphDef.setLazy( false );			// Disable lazy flag, graphs will be generated
		createGraphs();

		println( "\n>>> GRAPH RUN 4" );
		graphDef.setLazy( true );			// Enable lazy flag, graphs will be skipped
		createGraphs();

		fsl.setPersistent( false );			// Remove persistence so we can release datasources

		println( "\n>>> Manually releasing all RRD references" );
		fsl.releaseAll();					// Release the datasources again

		// -- Print out timing results
		println( "\n>>> Generation time results:" );
		println( "  First run, no lazy, no persistence - normal generation: " + runTimes[0] + " ms" );
		println( "  Second run, lazy, no persistence - generation skipped:  " + runTimes[1] + " ms" );
		println( "  Third run, no lazy, persistence - normal generation:    " + runTimes[2] + " ms" );
		println( "  Fourth run, lazy, persistence - generation skipped:     " + runTimes[3] + " ms" );

	}
}
