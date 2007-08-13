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

import org.jrobin.core.RrdDataSet;
import org.jrobin.core.RrdException;
import org.jrobin.core.Util;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.HashMap;
import java.io.*;

/**
 * <p>ExportData represents a reduced dataset that is the result of a JRobin rrd export.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class ExportData implements RrdDataSet
{
	// ================================================================
	// -- Members
	// ================================================================
	private int arraySize;
	private long[] timestamps;

	private HashMap sourceByName, legends;
	private Source[] sources;

	private Print printer;

	// ================================================================
	// -- Constructors
	// ================================================================
	ExportData()
	{
		sourceByName	= new HashMap();
		legends			= new HashMap();
	}

	ExportData( long[] timestamps, Source[] sources, HashMap legends )
	{
		this.timestamps	= timestamps;
		this.sources	= sources;
		this.legends	= legends;
		this.arraySize	= timestamps.length;

		sourceByName	= new HashMap( sources.length );
		for ( int i = 0; i < sources.length; i++ )
			sourceByName.put( sources[i].getName(), sources[i] );
	}

	/**
	 * Create an ExportData object based on export XML string..
	 *
	 * @param xportXml File containing export xml.
	 * @throws RrdException Thrown in case of JRobin specific exception.
	 * @throws IOException Thrown in case of I/O related exception.
	 */
	public ExportData( String xportXml ) throws RrdException, IOException
	{
		this();

		importXml( xportXml );
	}

	/**
	 * Create an ExportData object based on export XML string..
	 *
	 * @param xportXml File containing export xml.
	 * @param useLegendNames Map datasources to legend items in the export xml.
	 * @throws RrdException Thrown in case of JRobin specific exception.
	 * @throws IOException Thrown in case of I/O related exception.
	 */
	public ExportData( String xportXml, boolean useLegendNames ) throws RrdException, IOException
	{
		this();

		importXml( xportXml, useLegendNames );
	}

	/**
	 * Create an ExportData object based on export XML string..
	 *
	 * @param xportXml File containing export xml.
	 * @param dsNamePrefix Prefix of the datasource names.
	 * @throws RrdException Thrown in case of JRobin specific exception.
	 * @throws IOException Thrown in case of I/O related exception.
	 */
	public ExportData( String xportXml, String dsNamePrefix ) throws RrdException, IOException
	{
		this();

		importXml( xportXml, dsNamePrefix );
	}

	/**
	 * Create an ExportData object based on export XML file.
	 *
	 * @param xmlFile File containing export xml.
	 * @throws RrdException Thrown in case of JRobin specific exception.
	 * @throws IOException Thrown in case of I/O related exception.
	 */
	public ExportData( File xmlFile ) throws RrdException, IOException
	{
		this();

		importXml( xmlFile );
	}

	/**
	 * Create an ExportData object based on export XML file.
	 *
	 * @param xmlFile File containing export xml.
	 * @param useLegendNames Map datasources to legend items in the export xml.
	 * @throws RrdException Thrown in case of JRobin specific exception.
	 * @throws IOException Thrown in case of I/O related exception.
	 */
	public ExportData( File xmlFile, boolean useLegendNames ) throws RrdException, IOException
	{
		this();

		importXml( xmlFile, useLegendNames );
	}

	/**
	 * Create an ExportData object based on export XML file.
	 *
	 * @param xmlFile File containing export xml.
	 * @param dsNamePrefix Prefix of the datasource names.
	 * @throws RrdException Thrown in case of JRobin specific exception.
	 * @throws IOException Thrown in case of I/O related exception.
	 */
	public ExportData( File xmlFile, String dsNamePrefix ) throws RrdException, IOException
	{
		this();

		importXml( xmlFile, dsNamePrefix );
	}


	// ================================================================
	// -- Public methods
	// ================================================================
	/**
	 * Returns the number of rows in this dataset.
	 *
	 * @return Number of rows (data samples).
	 */
	public int getRowCount() {
		return sources.length;
	}

    /**
	 * Returns the number of columns in this dataset.
	 *
	 * @return Number of columns (datasources).
	 */
	public int getColumnCount() {
		return arraySize;
	}

	/**
	 * Returns an array of timestamps covering the whole range specified in the
	 * dataset object.
	 *
	 * @return Array of equidistant timestamps.
	 */
	public long[] getTimestamps()
	{
		return timestamps;
	}

	/**
	 * Returns the step with which this data was fetched.
	 * 
	 * @return Step as long.
	 */
	public long getStep()
	{
		return timestamps[1] - timestamps[0];
	}

	/**
	 * Returns all values for a single datasource, the returned values
	 * correspond to the timestamps returned with the {@link #getTimestamps() getTimestamps()} method.
	 *
	 * @param dsIndex Datasource index.
	 * @return Array of single datasource values.
	 */
	public double[] getValues( int dsIndex )
	{
		return sources[dsIndex].getValues();
	}

	/**
	 * Returns all values for all datasources, the returned values
	 * correspond to the timestamps returned with the {@link #getTimestamps() getTimestamps()} method.
	 *
	 * @return Two-dimensional aray of all datasource values.
	 */
	public double[][] getValues()
	{
		double[][] values = new double[ sources.length ][ arraySize ];

		for ( int i = 0; i < sources.length; i++ )
			values[i] = sources[i].getValues();

		return values;
	}

	/**
	 * Returns all values for a single datasource. The returned values
	 * correspond to the timestamps returned with the {@link #getTimestamps() getTimestamps()} method.
	 *
	 * @param dsName Datasource name.
	 * @return Array of single datasource values.
	 * @throws RrdException Thrown if no matching datasource name is found.
	 */
	public double[] getValues( String dsName ) throws RrdException
	{
		Source src 		= getSource( dsName );

		return src.getValues();
	}

    /**
	 * Returns the first timestamp in the dataset.
	 *
	 * @return The smallest timestamp.
	 */
	public long getFirstTimestamp() {
		return timestamps[0];
	}

	/**
	 * Returns the last timestamp in the dataset.
	 *
	 * @return The biggest timestamp.
	 */
	public long getLastTimestamp() {
		return timestamps[ arraySize - 1 ];
	}

	/**
	 * Returns array of the names of all datasources in the set.
	 *
	 * @return Array of datasource names.
	 */
	public String[] getDsNames()
	{
		String[] names = new String[ sources.length ];

		for ( int i = 0; i < sources.length; i++ )
			names[i] = sources[i].getName();

		return names;
	}

	/**
	 * Retrieve the table index number of a datasource by name.
	 * Names are case sensitive.
	 *
	 * @param dsName Name of the datasource for which to find the index.
	 * @return Index number of the datasource in the value table.
	 * @throws RrdException Thrown if the given datasource name cannot be found in the dataset.
	 */
	public int getDsIndex( String dsName ) throws RrdException
	{
		for ( int i = 0; i < sources.length; i++ )
			if ( sources[i].getName().equals(dsName) )
				return i;

		throw new RrdException( "No such datasource: " + dsName );
	}

	/**
	 * Returns aggregated value from the dataset for a single datasource.
	 *
	 * @param dsName Datasource name
	 * @param consolFun Consolidation function to be applied to set datasource values datasource.
	 * Valid consolidation functions are MIN, MAX, LAST, FIRST, AVERAGE and TOTAL
	 * @return MIN, MAX, LAST, FIRST, AVERAGE or TOTAL value calculated from the dataset for the given datasource name
	 * @throws RrdException Thrown if the given datasource name cannot be found in the dataset.
	 */
	public double getAggregate( String dsName, String consolFun ) throws RrdException
	{
		Source src = getSource( dsName );

		if( consolFun.equalsIgnoreCase("MAX") )
			return src.getAggregate( Source.AGG_MAXIMUM );
		else if ( consolFun.equalsIgnoreCase("MIN") )
			return src.getAggregate( Source.AGG_MINIMUM );
		else if ( consolFun.equalsIgnoreCase("LAST") )
			return src.getAggregate( Source.AGG_LAST);
		else if ( consolFun.equalsIgnoreCase("FIRST") )
			return src.getAggregate( Source.AGG_FIRST );
		else if ( consolFun.equalsIgnoreCase("TOTAL") )
			return src.getAggregate( Source.AGG_TOTAL );
		else if ( consolFun.equalsIgnoreCase("AVERAGE") )
			return src.getAggregate( Source.AGG_AVERAGE );
		else
			throw new RrdException("Unsupported consolidation function [" + consolFun + "]");
	}

	/**
	 * <p>Calculate the chosen consolidation function <code>consolFun</code> over
	 * the <code>sourceName</code> and returns the result as a string using the
	 * specified <code>format</code>.</p>
	 *
	 * <p>In the format string there should be a
	 * <code>@n.d</code> marker (replace <code>n</code> with the total number of spaces the
	 * value should at minimum take up, and replace <code>d</code> with the desired number of decimals)
	 * in the place where the number should be printed. If an additional <code>@s</code> is
	 * found in the format, the value will be scaled and an appropriate SI magnitude
	 * unit will be printed in place of the <code>@s</code> marker. If you specify
	 * <code>@S</code> instead of <code>@s</code>, the value will be scaled with the scale
	 * factor used in the last gprint directive (uniform value scaling).</p>
	 *
	 * @param sourceName Source name
	 * @param consolFun Consolidation function to be used for calculation ("AVERAGE",
	 * "MIN", "MAX", "LAST" or "TOTAL" (since 1.3.1)
	 * @param format Format string. For example: "speed is @5.2 @sbits/sec@c",
	 * "temperature = @0 degrees"
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public String print( String sourceName, String consolFun, String format ) throws RrdException {
		return print( sourceName, consolFun, format, ValueFormatter.DEFAULT_BASE );
	}

	/**
	 * <p>Calculate the chosen consolidation function <code>consolFun</code> over
	 * the <code>sourceName</code> and returns the result as a string using the
	 * specified <code>format</code>.</p>
	 *
	 * <p>In the format string there should be a
	 * <code>@n.d</code> marker (replace <code>n</code> with the total number of spaces the
	 * value should at minimum take up, and replace <code>d</code> with the desired number of decimals)
	 * in the place where the number should be printed. If an additional <code>@s</code> is
	 * found in the format, the value will be scaled and an appropriate SI magnitude
	 * unit will be printed in place of the <code>@s</code> marker. If you specify
	 * <code>@S</code> instead of <code>@s</code>, the value will be scaled with the scale
	 * factor used in the last gprint directive (uniform value scaling).</p>
	 *
	 * @param sourceName Source name
	 * @param consolFun Consolidation function to be used for calculation ("AVERAGE",
	 * "MIN", "MAX", "LAST" or "TOTAL" (since 1.3.1)
	 * @param format Format string. For example: "speed is @5.2 @sbits/sec@c",
	 * "temperature = @0 degrees"
	 * @param base Base value used to calculate the appriopriate scaling SI magnitude.
	 * @throws RrdException Thrown in case of JRobin specific error
	 */
	public String print( String sourceName, String consolFun, String format, double base ) throws RrdException
	{
		double value = getAggregate( sourceName, consolFun );

		if ( printer == null )
			printer = new Print( base, ValueFormatter.NO_SCALE );

		return printer.getFormattedString( value, format, base );
	}

	/**
	 * Imports a export XML string and maps it back to this ExportData object.
	 * The XML can be from either a JRobin or RRDtool export.
	 *
	 * Datasources found will be named d1, d2, ...
	 *
	 * @param xportXml String containing the XML result of an export.
	 */
	public void importXml( String xportXml ) throws RrdException, IOException {
		importXml( xportXml, true );
	}

	/**
	 * Imports a export XML string and maps it back to this ExportData object.
	 * The XML can be from either a JRobin or RRDtool export.
	 *
	 * Datasources found will be named d1, d2, ...
	 *
	 * @param xmlFile File containing export XML dump.
	 */
	public void importXml( File xmlFile ) throws RrdException, IOException {
		importXml( xmlFile, true );
	}

	/**
	 * Imports a export XML string and maps it back to this ExportData object.
	 * The XML can be from either a JRobin or RRDtool export.
	 *
	 * The name of the datasources found will depend on the 'useLegendNames' flag.
	 *
	 * @param xmlFile File containing export XML dump.
	 * @param useLegendNames True if the names for the datasources should be set to
	 * 						 the legend values, false if they should be d1, d2, ...
	 */
	public void importXml( File xmlFile , boolean useLegendNames ) throws RrdException, IOException
	{
		Element root 		= Util.Xml.getRootElement( xmlFile );
		importXml( root, useLegendNames, "d" );
	}

	/**
	 * Imports a export XML string and maps it back to this ExportData object.
	 * The XML can be from either a JRobin or RRDtool export.
	 *
	 * The name of the datasources found will be the prefix passed as parameter,
	 * followed by a number, making the name unique.
	 *
	 * @param xportXml String containing the XML result of an export.
	 * @param dsNamePrefix Prefix of the datasource names.
	 */
	public void importXml( String xportXml, String dsNamePrefix ) throws RrdException, IOException
	{
		Element root 		= Util.Xml.getRootElement( xportXml );
		importXml( root, false, dsNamePrefix );
	}

	/**
	 * Imports a export XML string and maps it back to this ExportData object.
	 * The XML can be from either a JRobin or RRDtool export.
	 *
	 * The name of the datasources found will be the prefix passed as parameter,
	 * followed by a number, making the name unique.
	 *
	 * @param xmlFile File containing export XML dump.
	 * @param dsNamePrefix Prefix of the datasource names.
	 */
	public void importXml( File xmlFile, String dsNamePrefix ) throws RrdException, IOException
	{
		Element root 		= Util.Xml.getRootElement( xmlFile );
		importXml( root, false, dsNamePrefix );
	}

	/**
	 * Imports a export XML string and maps it back to this ExportData object.
	 * The XML can be from either a JRobin or RRDtool export.
	 *
	 * The name of the datasources found will depend on the 'useLegendNames' flag.
	 *
	 * @param xportXml String containing the XML result of an export.
	 * @param useLegendNames True if the names for the datasources should be set to
	 * 						 the legend values, false if they should be d1, d2, ...
	 */
	public void importXml( String xportXml, boolean useLegendNames ) throws RrdException, IOException
	{
		Element root 		= Util.Xml.getRootElement( xportXml );
		importXml( root, useLegendNames, "d" );
	}

	/**
	 * Dumps fetch data to output stream in XML format.
	 *
	 * @param outputStream Output stream to dump dataset to
	 * @throws RrdException Thrown in case of JRobin specific error.
	 * @throws IOException Thrown in case of I/O error
	 */
	public void exportXml( OutputStream outputStream ) throws RrdException, IOException
	{
		PrintWriter pw = new PrintWriter( outputStream );
		pw.write( exportXml() );
		pw.flush();
	}

	/**
	 * Dumps dataset to file in XML format.
	 *
	 * @param filepath Path to destination file
	 * @throws RrdException Thrown in case of JRobin specific error.
	 * @throws IOException Thrown in case of I/O error
	 */
	public void exportXml( String filepath ) throws RrdException, IOException
	{
		FileWriter fw = new FileWriter( filepath );
		fw.write( exportXml() );
		fw.close();
	}

	/**
	 * Dumps the dataset to XML.
	 *
	 * @return XML string format of the dataset.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 * @throws java.io.IOException Thrown in case of an I/O related error.
	 */
	public String exportXml() throws RrdException, IOException
	{
		StringBuffer xml 	= new StringBuffer( "<xport>\n" );

		// Add metadata section
		xml.append( "\t<meta>\n" );
		xml.append( "\t\t<start>" + timestamps[0] + "</start>\n" );
		xml.append( "\t\t<step>" + (timestamps[1] - timestamps[0]) + "</step>\n" );
		xml.append( "\t\t<end>" + timestamps[arraySize - 1] + "</end>\n" );
		xml.append( "\t\t<rows>" + arraySize + "</rows>\n" );
		xml.append( "\t\t<columns>" + sources.length + "</columns>\n" );
		xml.append( "\t\t<legend>\n" );
		for ( int i = 0; i < sources.length; i++ )
			xml.append( "\t\t\t<entry>" + getExportLegend( sources[i].getName() ) + "</entry>\n" );
		xml.append( "\t\t</legend>\n" );
		xml.append( "\t</meta>\n" );

		// Add data section
		xml.append( "\t<data>\n" );

		for ( int i = 0; i < arraySize; i++ )
		{
			xml.append( "\t\t<row>" );
			xml.append( "<t>" + timestamps[i] + "</t>" );
			for ( int j = 0; j < sources.length; j++ )
				xml.append( "<v>" + sources[ j ].get( i ) + "</v>" );
			xml.append( "</row>\n" );
		}
		xml.append( "\t</data>\n" );

		xml.append( "</xport>\n" );

		return xml.toString();
	}

	// ================================================================
	// -- Protected methods
	// ================================================================
	protected Source[] getSources() {
		return sources;
	}

	// ================================================================
	// -- Private methods
	// ================================================================
	private String getExportLegend( String name )
	{
		if ( !legends.containsKey(name) )
			return "";

		return (String) legends.get(name);
	}

	private Source getSource( String name ) throws RrdException
	{
		if ( !sourceByName.containsKey(name) )
			throw new RrdException( "No such datasource: " + name );

		return (Source) sourceByName.get(name);
	}

	private void importXml( Element root, boolean useLegendNames, String dsNamePrefix ) throws RrdException
	{
		Node meta			= Util.Xml.getFirstChildNode( root, "meta" );
		Node[] dataRows 	= Util.Xml.getChildNodes( Util.Xml.getFirstChildNode( root, "data" ), "row" );

		sourceByName.clear();
		legends.clear();

		// -- Parse the metadata
		int columns			= Util.Xml.getChildValueAsInt( meta, "columns" );
		long step			= Util.Xml.getChildValueAsLong( meta, "step" );
		String[] dsNames	= new String[ columns ];
		Node[] legendNodes	= Util.Xml.getChildNodes( Util.Xml.getFirstChildNode( meta, "legend"), "entry" );
		for ( int i = 0; i < legendNodes.length; i++ )
		{
			String legend = Util.Xml.getValue( legendNodes[i] );
			if ( useLegendNames )
				dsNames[i] = legend;
			else
				dsNames[i] = dsNamePrefix + (i + 1);

			legends.put( dsNames[i], legend );
		}

		// -- Parse the data
		timestamps			= new long[ dataRows.length ];
		sources				= new Source[ columns ];
		arraySize 			= timestamps.length;

		for ( int i = 0; i < sources.length; i++ )
		{
			sources[i] 			= new Def( dsNames[i], arraySize, arraySize );
			sources[i].setFetchedStep( step );
		}

		for ( int i = 0; i < dataRows.length; i++ )
		{
			timestamps[i] 	= Util.Xml.getChildValueAsLong( dataRows[i], "t" );
			Node[] data		= Util.Xml.getChildNodes( dataRows[i], "v" );

			for ( int j = 0; j < data.length; j++ )
				sources[j].set( i, timestamps[i], Util.Xml.getValueAsDouble(data[j]) );
		}

		// -- Set the datasource - name
		for ( int i = 0; i < sources.length; i++ )
			sourceByName.put( sources[i].getName(), sources[i] );
	}
}
