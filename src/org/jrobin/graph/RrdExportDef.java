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
package org.jrobin.graph;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;

import org.jrobin.core.Util;
import org.jrobin.core.RrdException;
import org.jrobin.core.XmlWriter;

/**
 * <p>Class used to collect information for a JRobin export.</p>
 *
 * <p>JRobin export works the same way as Rrdtool XPORT does, to learn
 * more about the XPORT functionality, see RRDTool's
 * <a href="../../../../man/rrdxport.html" target="man">rrdxport man page</a>.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class RrdExportDef implements Serializable
{
	// ================================================================
	// -- Members
	// ================================================================
	public final static int STRICT_IMPLICIT_OFF	= 0;
	public final static int STRICT_IMPLICIT_ON	= 1;
	public final static int STRICT_EXPLICIT_OFF	= 2;
	public final static int STRICT_EXPLICIT_ON	= 3;

	private long endTime						= Util.getTime();					// default time span of the last 24 hours
	private long startTime						= Util.getTime() - 86400L;
	private long resolution						= 1;								// resolution to fetch from the RRD databases

	private int strict							= STRICT_IMPLICIT_OFF;

	// -- Non-settable members
	private int numSdefs						= 0;
	private int numDefs							= 0;								// number of Def datasources added

	protected FetchSourceList fetchSources		= new FetchSourceList( 10 );		// holds the list of FetchSources
	protected ArrayList pdefList				= new ArrayList( 10 );				// holds the list of Plottable datasources
	protected ArrayList cdefList				= new ArrayList( 10 );				// holds the list of Cdef datasources
	protected ArrayList exportList				= new ArrayList( 10 );				// holds the list of datasources to export
	protected ArrayList edefList				= new ArrayList( 3 );				// holds the list of export data objects

	// ================================================================
	// -- Constructors
	// ================================================================
	public RrdExportDef() {
	}

	/**
	 * Constructs a new JRobin graph object, with a specified time span to be presented on the graph.
	 * Using timestamps defined as number of seconds since the epoch.
	 *
	 * @param startTime Starting timestamp in seconds.
	 * @param endTime Ending timestamp in seconds.
	 * @throws org.jrobin.core.RrdException Thrown if invalid parameters are supplied.
	 */
	public RrdExportDef( long startTime, long endTime ) throws RrdException
	{
		setTimePeriod( startTime, endTime );
	}

	/**
	 * Constructs a new JRobin graph object, with a specified time span to be presented on the graph.
	 * Time spam defined using <code>java.util.Date</code> objects.
	 *
	 * @param start Starting time.
	 * @param end Ending time.
	 * @throws RrdException Thrown in case of invalid parameters.
	 */
	public RrdExportDef( Date start, Date end) throws RrdException
	{
		setTimePeriod( start, end );
	}

	/**
	 * Constructs a new JRobin graph object, with a specified time span to be presented on the graph.
	 * Time spam defined using <code>java.util.GregorianCalendar</code> objects.
	 *
	 * @param start Starting time.
	 * @param end Ending time.
	 * @throws RrdException Thrown in case of invalid parameters.
	 */
	public RrdExportDef( GregorianCalendar start, GregorianCalendar end ) throws RrdException
	{
		setTimePeriod( start, end );
	}

	// ================================================================
	// -- Public methods
	// ================================================================
	/**
	 * Sets time span to be presented on the graph using timestamps in number of seconds.
	 * An end time of 0 means JRobin will try to use the last available update time.
	 *
	 * @param startTime Starting timestamp in seconds.
	 * @param endTime Ending timestamp in secons.
	 * @throws RrdException Thrown if invalid parameters are supplied.
	 */
	public void setTimePeriod( long startTime, long endTime ) throws RrdException
	{
		if ( startTime < 0 || ( endTime != 0 && endTime <= startTime ) )
			throw new RrdException( "Invalid start/end time: " + startTime + "/" + endTime );

		this.startTime 	= startTime;
		this.endTime 	= endTime;
	}

	/**
	 * Sets time span to be presented on the graph using <code>java.util.Date</code> objects.
	 *
	 * @param start Starting time.
	 * @param end Ending time.
	 * @throws RrdException Thrown in case of invalid parameters.
	 */
	public void setTimePeriod( Date start, Date end ) throws RrdException
	{
		setTimePeriod( start.getTime() / 1000L, end.getTime() / 1000L );
	}

	/**
	 * Sets time span to be presented on the graph using <code>java.util.GregorianCalendar</code> objects.
	 *
	 * @param start Starting time.
	 * @param end Ending time
	 * @throws RrdException Thrown if invalid parameters are supplied.
	 */
	public void setTimePeriod( GregorianCalendar start, GregorianCalendar end ) throws RrdException
	{
		setTimePeriod( start.getTime(), end.getTime() );
	}

	/**
	 * Sets the resolution with which data will be fetched from the RRD sources.
	 * JRobin will try to match the requested resolution as closely as possible.
	 *
	 * @param resolution Resolution (data step) in seconds.
	 */
	public void setResolution( long resolution )
	{
		this.resolution = resolution;
	}

	/**
	 * <p>Adds simple graph source to graph definition. Graph source <code>name</code>
	 * can be used:</p>
	 * <ul>
	 * <li>To specify sources for line, area and stack plots.</li>
	 * <li>To define complex graph sources
	 * (see {@link #datasource(java.lang.String, java.lang.String) complex graph
	 * source definition}).</li>
	 * </ul>
	 *
	 * @param name Graph source name.
	 * @param file Path to RRD file.
	 * @param dsName Data source name defined in the RRD file.
	 * @param consolFunc Consolidation function that will be used to extract data from the RRD
	 * file ("AVERAGE", "MIN", "MAX" or "LAST").
	 */
	public void datasource( String name, String file, String dsName, String consolFunc ) throws RrdException
	{
		fetchSources.add( name, file, dsName, consolFunc );

		numDefs++;
	}

	/**
	 * <p>Adds simple graph source to graph definition. Graph source <code>name</code>
	 * can be used:</p>
	 * <ul>
	 * <li>To specify sources for line, area and stack plots.</li>
	 * <li>To define complex graph sources
	 * (see {@link #datasource(java.lang.String, java.lang.String) complex graph
	 * source definition}).</li>
	 * </ul>
	 *
	 * @param name Graph source name.
	 * @param file Path to RRD file.
	 * @param dsName Data source name defined in the RRD file.
	 * @param consolFunc Consolidation function that will be used to extract data from the RRD
	 * file ("AVERAGE", "MIN", "MAX" or "LAST").
	 * @param backend Name of the RrdBackendFactory that should be used for this RrdDb.
	 */
	public void datasource( String name, String file, String dsName, String consolFunc, String backend ) throws RrdException
	{
		fetchSources.add( name, file, dsName, consolFunc, backend );

		numDefs++;
	}

	/**
	 * <p>Clears the list of RRD datasources for this GraphDef and sets it to the FetchSourceList
	 * passed as aparameter.  This does not alter any Cdef, Sdef or Pdef definitions.  The datasources
	 * should be passed on as a FetchSourceList {@link FetchSourceList}.</p>
	 * @param datasourceList FetchSourceList of the datasources to use.
	 */
	public void setDatasources( FetchSourceList datasourceList )
	{
		fetchSources	= datasourceList;

		numDefs			= fetchSources.defCount();
	}

	/**
	 * <p>Adds complex graph source with the given name to the graph definition.
	 * Complex graph sources are evaluated using the supplied <code>rpn</code> expression.
	 *
	 * <p>Complex graph source <code>name</code> can be used:</p>
	 * <ul>
	 * <li>To specify sources for line, area and stack plots.</li>
	 * <li>To define other complex graph sources.</li>
	 * </ul>
	 *
	 * <p>JRobin supports the following RPN functions, operators and constants: +, -, *, /,
	 * %, SIN, COS, LOG, EXP, FLOOR, CEIL, ROUND, POW, ABS, SQRT, RANDOM, LT, LE, GT, GE, EQ,
	 * IF, MIN, MAX, LIMIT, DUP, EXC, POP, UN, UNKN, NOW, TIME, PI and E. JRobin does not
	 * force you to specify at least one simple graph source name as RRDTool.</p>
	 *
	 * <p>For more details on RPN see RRDTool's
	 * <a href="http://people.ee.ethz.ch/~oetiker/webtools/rrdtool/manual/rrdgraph.html" target="man">rrdgraph man page</a>.</p>
	 *
	 * @param name Graph source name.
	 * @param rpn RPN expression containig comma delmited simple and complex graph
	 * source names, RPN constants, functions and operators.
	 */
	public void datasource( String name, String rpn )
	{
		cdefList.add( new Cdef(name, rpn) );
	}

	/**
	 * <p>Adds static graph source with the given name to the graph definition.
	 * Static graph sources are the result of a consolidation function applied
	 * to *any* other graph source that has been defined previously.</p>
	 *
	 * @param name Graph source name.
	 * @param defName Name of the datasource to calculate the value from.
	 * @param consolFunc Consolidation function to use for value calculation
	 */
	public void datasource( String name, String defName, String consolFunc ) throws RrdException
	{
		cdefList.add( new Sdef(name, defName, consolFunc) );
		numSdefs++;
	}

	/**
	 * <p>Adds a custom graph source with the given name to the graph definition.
	 * The datapoints should be made available by a class extending Plottable.</p>
	 *
	 * @param name Graph source name.
	 * @param plottable Class that extends Plottable class and is suited for graphing.
	 */
	public void datasource( String name, Plottable plottable )
	{
		pdefList.add( new Pdef(name, plottable) );
	}

	/**
	 * <p>Adds a custom graph source with the given name to the graph definition.
	 * The datapoints should be made available by a class extending Plottable.</p>
	 *
	 * @param name Graph source name.
	 * @param plottable Class that extends Plottable class and is suited for graphing.
	 * @param index Integer referring to the datasource in the Plottable class.
	 */
	public void datasource( String name, Plottable plottable, int index )
	{
		pdefList.add( new Pdef(name, plottable, index) );
	}

	/**
	 * <p>Adds a custom graph source with the given name to the graph definition.
	 * The datapoints should be made available by a class extending Plottable.</p>
	 *
	 * @param name Graph source name.
	 * @param plottable Class that extends Plottable class and is suited for graphing.
	 * @param sourceName String name referring to the datasource in the Plottable class.
	 */
	public void datasource( String name, Plottable plottable, String sourceName )
	{
		pdefList.add( new Pdef(name, plottable, sourceName) );
	}

	/**
	 * Adds a set of ExportData to the datasource list.
	 *
	 * @param edata ExportData to add.
	 */
	public void addExportData( ExportData edata )
	{
		edefList.add( edata );
	}

	/**
	 * Sets a specific datasource to be exported (if export is strict).
	 * The expor legend for this datasource will be empty. 
	 *
	 * @param name Name of the datasource
	 */
	public void export( String name )
	{
		export( name, "" );
	}

	/**
	 * Sets a specific datasource to be exported (if export is strict).
	 * And maps an export legend to this datasource.
	 *
	 * @param name Name of the datasource
	 * @param legend Legend text
	 */
	public void export( String name, String legend )
	{
		if ( strict == STRICT_IMPLICIT_OFF )
			strict = STRICT_IMPLICIT_ON;

		exportList.add( new String[] { name, legend } );
	}

	/**
	 * <p>Sets the strict flag for the export functionality.  By default, the
	 * export is in implicit not-strict, this means that by default, all
	 * datasources specified in the RrdExportDef will be exported into
	 * the ExportData.</p>
	 *
	 * <p>If the strict flag is not specified explicitly by calling this method,
	 * the export will convert to implicitly strict as soon as a particular
	 * export() mapping is defined.  Explicit settings will override implicit.</p>
	 *
	 * <p>When explicit is off, the legend for datasources will by default be
	 * the same as the datasource name, the legend can be overridden by setting
	 * mappings using export() method.</p>
	 *
	 * <p>Strict export is the same behaviour as RRDtool's XPORT.</p>
	 *
	 * @param strict True if strict export should on, false if not.
	 */
	public void setStrictExport( boolean strict )
	{
		this.strict = ( strict ? STRICT_EXPLICIT_ON : STRICT_EXPLICIT_OFF );
	}

	/**
	 * Exports RrdExportDef (export definition) object in XML format to output stream.
	 * Generated code can be parsed with {@link RrdExportDefTemplate} class.
	 *
	 * @param stream Output stream to send XML code to.
	 */
	public void exportXmlTemplate( OutputStream stream )
	{
		XmlWriter xml = new XmlWriter( stream );

		xml.startTag("rrd_export_def");

        // SPAN
		xml.startTag("span");
		xml.writeTag("start", getStartTime() );
		xml.writeTag("end", getEndTime() );
		xml.closeTag(); // span

		// OPTIONS
		xml.startTag( "options" );
		if ( resolution > 1 )
			xml.writeTag( "resolution", resolution );
		xml.writeTag( "strict_export", ( strict == STRICT_IMPLICIT_ON || strict == STRICT_EXPLICIT_ON ? "true" : "false" ) );
		xml.closeTag();

		// DATASOURCES
		xml.startTag("datasources");
		// defs
		for ( int i = 0; i < fetchSources.size(); i++ )
			fetchSources.get( i ).exportXml(xml);
		// cdefs and sdefs
		for (int i = 0; i < cdefList.size(); i++ )
		{
			Cdef cdef = (Cdef) cdefList.get(i);
			cdef.exportXml(xml);
		}
		xml.closeTag(); // datasources

		// EXPORTS
		xml.startTag("exports");
		String[][] list = getExportDatasources();
		for ( int i = 0; i < list.length; i++ )
		{
			xml.startTag( "export" );
			xml.writeTag( "datasource", list[i][0] );
			xml.writeTag( "legend", list[i][1] );
			xml.closeTag();
		}
		xml.closeTag(); // exports

		xml.closeTag(); // rrd_export_def
		xml.flush();

		xml.flush();
	}

	/**
	 * Exports RrdExportDef (export definition) object in XML format to string.
	 * Generated code can be parsed with {@link RrdExportDefTemplate} class, see
	 * {@link RrdExportDef#exportXmlTemplate()}.
	 *
	 * @return String representing graph definition in XML format.
	 */
	public String getXmlTemplate()
	{
		return exportXmlTemplate();
	}

	/**
	 * Exports RrdExportDef (export definition) object in XML format to string.
	 * Generated code can be parsed with {@link RrdExportDefTemplate} class.
	 *
	 * @return String representing graph definition in XML format.
	 */
	public String exportXmlTemplate()
	{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		exportXmlTemplate(outputStream);
		return outputStream.toString();
	}

	/**
	 * Exports RrdExportDef (export definition) object in XML format to file.
	 * Generated code can be parsed with {@link RrdExportDefTemplate} class.
	 *
	 * @param filePath destination file
	 */
	public void exportXmlTemplate(String filePath) throws IOException
	{
		FileOutputStream outputStream = new FileOutputStream(filePath, false);
		exportXmlTemplate(outputStream);
		outputStream.close();
	}

	// ================================================================
	// -- Protected (package) methods
	// ================================================================
	protected long getStartTime() {
		return startTime;
	}

	protected long getEndTime() {
		return endTime;
	}

	protected long getResolution() {
		return resolution;
	}

	protected int getNumDefs()
	{
		return numDefs;
	}

	protected Cdef[] getCdefs()
	{
		return (Cdef[]) cdefList.toArray( new Cdef[] {} );
	}

	protected Pdef[] getPdefs()
	{
		return (Pdef[]) pdefList.toArray( new Pdef[] {} );
	}

	protected ExportData[] getExportData()
	{
		return (ExportData[]) edefList.toArray( new ExportData[] {} );
	}

	protected int getNumSdefs()
	{
		return numSdefs;
	}

	protected FetchSourceList getFetchSources()
	{
		return fetchSources;
	}

	protected boolean isStrict() {
		return ( strict == STRICT_IMPLICIT_ON || strict == STRICT_EXPLICIT_ON );
	}

	protected String[][] getExportDatasources() {
		return (String[][]) exportList.toArray( new String[0][2] );
	}
}
