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

import org.jrobin.graph.RrdExportDef;
import org.jrobin.graph.RrdExportDefTemplate;
import org.jrobin.graph.RrdExport;
import org.jrobin.graph.ExportData;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;

import java.io.IOException;
import java.io.File;
import java.util.GregorianCalendar;

/**
 * <p>This is a small demo that illustrates JRobin export functionality.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class ExportExportDemo
{
	public static String exportRrd1 	= Util.getJRobinDemoPath( "export-eth0.xml" );
	public static String exportRrd2 	= Util.getJRobinDemoPath( "export-eth1.xml" );
	private static String demoResources = "";

	private static void println( String str ) {
		System.out.println( str );
	}

	private static void prepare( String[] args )
	{
		demoResources = Util.getJRobinHomeDirectory() + "/res/demo/";
	}

	public static void main( String[] args ) throws RrdException, IOException
	{
		prepare( args );

		println( "+----------------------------------------------------------------------------------+" );
		println( "|                            JRobin ExportExportDemo                               |" );
		println( "|                                                                                  |" );
		println( "| This demo will perform the same export on two different rrd files.  In fact the  |" );
		println( "| two RRD files contain the same data, they are data from a network interface and  |" );
		println( "| contain a full days worth of samples on July 3rd 2004.  For this example the RRD |" );
		println( "| are treated as if from two different interfaces: eth0 and eth1.                  |" );
		println( "|                                                                                  |" );
		println( "+----------------------------------------------------------------------------------+" );

		long execStart						= System.currentTimeMillis();

		// -- Read in the export def xml
		println( ">>> Reading export def template\n" );
		RrdExportDefTemplate xmlTemplate	= new RrdExportDefTemplate( new File(demoResources + "exportdef.xml") );

		// -- The data we're interested in is from the 3rd of july 2004, to the 4th
		GregorianCalendar start 			= new GregorianCalendar( 2004, GregorianCalendar.JULY, 3 );
		GregorianCalendar end 				= new GregorianCalendar( 2004, GregorianCalendar.JULY, 4 );

		xmlTemplate.setVariable( "start", start );
		xmlTemplate.setVariable( "end", end );

		Util.getLapTime();

		// -- Exporting the first rrd
		println( ">>> Exporting data from eth0 for July 3rd, 2004" );
		println( ">>> Number of rows for the export is limited to 400." );
		xmlTemplate.setVariable( "rrd", demoResources + "eth0.rrd" );
		RrdExportDef exportDef				= xmlTemplate.getRrdExportDef();
		RrdExport export					= new RrdExport( exportDef );

		// We don't limit the number of rows returned, this will try to auto limit on 400 max
		ExportData dataFromRrd1				= export.fetch();

		// Save the exported data to export xml
		dataFromRrd1.exportXml( exportRrd1 );
		println( ">>> File saved: " + exportRrd1 + " " + Util.getLapTime() + "\n" );

		// -- Exporting the second rrd
		println( ">>> Exporting data from eth1 for July 3rd, 2004" );
		println( ">>> Number of rows for the export is limited to 20." );
		println( "    This means the same data as in the previous export (since in fact" );
		println( "    both RRD files are the same) will be aggregated to around 20 rows." );
		xmlTemplate.setVariable( "rrd", demoResources + "eth1.rrd" );
		exportDef							= xmlTemplate.getRrdExportDef();
		export.setExportDef( exportDef );

		// Now we limit the number of rows retrieved to 20
		ExportData dataFromRrd2				= export.fetch( 20 );

		// Save the exported data to export xml
		dataFromRrd2.exportXml( exportRrd2 );
		println( ">>> File saved: " + exportRrd2 + " " + Util.getLapTime() + "\n" );

		long execStop						= System.currentTimeMillis();

		println( ">>> Demo finished in " + (execStop - execStart) + " milliseconds" );
	}
}
