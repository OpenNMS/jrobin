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

import java.util.ArrayList;
import java.io.IOException;

import org.jrobin.core.*;

/**
 * <p>Class used to group datasources per RRD db, for faster fetching.
 * A FetchSource represents one RRD database file, and will take care of all datasource
 * fetching using objects of the <code>core</code> package.  Fetching will be done in such 
 * a way that all datasources per consolidation function are fetched with the minimum possible
 * file reads.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class FetchSource implements ConsolFuns
{
	// ================================================================
	// -- Members
	// ================================================================
	protected static final int AVG			= 0;
	protected static final int MAX 			= 1;
	protected static final int MIN 			= 2;
	protected static final int LAST			= 3;
	protected static final int MAX_CF 		= 4;
	
	protected static final String[] cfNames	= new String[] {
		CF_AVERAGE, CF_MAX, CF_MIN, CF_LAST
	};

	private RrdDb rrd;
	private RrdDef rrdDef;

	private String rrdFile;						// Holds the name of the RRD file
	private String backendName;

	private int numSources					= 0;
	private ArrayList[] datasources			= new ArrayList[MAX_CF];

	private FetchSourceList listReference	= null;


	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a FetchSource object based on a RRD file name.
	 *
	 * @param rrdFile Name of the RRD file holding all datasources.
	 * @param listRef Reference to the FetchSourceList this FetchSource belongs to.
	 */
	protected FetchSource( String rrdFile, FetchSourceList listRef )
	{
		this.rrdFile 	= rrdFile;
		listReference	= listRef;

		// Initialization of datasource lists per CF
		for (int i = 0; i < datasources.length; i++)
			datasources[i] = new ArrayList( 10 );
	}
	
	/**
	 * Constructs a FetchSource object based on a RRD file name, and
	 * adds a given datasource to the datasources list.
	 *
	 * @param rrdFile Name of the RRD file holding all datasources.
	 * @param consolFunc Consolidation function of the datasource to fetch.
	 * @param dsName Internal name of the datasource in the RRD file.
	 * @param name Variable name of the datasource in the graph definition.
	 * @param listRef Reference to the FetchSourceList this FetchSource belongs to.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected FetchSource( String rrdFile, String consolFunc, String dsName, String name, FetchSourceList listRef ) throws RrdException
	{
		this( rrdFile, listRef );
		addSource( consolFunc, dsName, name );
	}

	/**
	 * Constructs a FetchSource object based on a RRD file name, and
	 * adds a given datasource to the datasources list.
	 *
	 * @param rrdFile Name of the RRD file holding all datasources.
	 * @param consolFunc Consolidation function of the datasource to fetch.
	 * @param dsName Internal name of the datasource in the RRD file.
	 * @param name Variable name of the datasource in the graph definition.
	 * @param backendName Name of the RrdBackendFactory to use for this RrdDb.
	 * @param listRef Reference to the FetchSourceList this FetchSource belongs to.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected FetchSource( String rrdFile, String consolFunc, String dsName, String name, String backendName, FetchSourceList listRef ) throws RrdException
	{
		this( rrdFile, consolFunc, dsName, name, listRef );
		setBackendFactory( backendName );
	}

	// ================================================================
	// -- Protected methods
	// ================================================================
	/**
	 * Adds a given datasource to the datasources list for this FetchSource.
	 *
	 * @param consolFunc Consolidation function of the datasource to fetch.
	 * @param dsName Internal name of the datasource in the RRD file.
	 * @param name Variable name of the datasource in the graph definition.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected void addSource( String consolFunc, String dsName, String name ) throws RrdException
	{
		if ( consolFunc.equalsIgnoreCase(CF_AVERAGE) || consolFunc.equalsIgnoreCase("AVG") )
			datasources[AVG].add( new String[] { dsName, name } );
		else if ( consolFunc.equalsIgnoreCase(CF_MAX) || consolFunc.equalsIgnoreCase("MAXIMUM") )
			datasources[MAX].add( new String[] { dsName, name } );
		else if ( consolFunc.equalsIgnoreCase(CF_MIN) || consolFunc.equalsIgnoreCase("MINIMUM") )
			datasources[MIN].add( new String[] { dsName, name } );
		else if ( consolFunc.equalsIgnoreCase(CF_LAST) )
			datasources[LAST].add( new String[] { dsName, name } );
		else
			throw new RrdException( "Invalid consolidation function specified." );
		
		numSources++;				
	}

	/**
	 * Sets the name of the RrdBackendFactory that should be used for this FetchSource.
	 * The factory should be registered with RrdBackendFactory static.
	 *
	 * @param backendName Name of the RrdBackendFactory to use for this RrdDb.
	 */
	protected void setBackendFactory( String backendName ) {
		this.backendName = backendName;
	}

	/**
	 * Fetches all datavalues for a given timespan out of the provided RRD file.
	 *
	 * @param startTime Start time of the given timespan.
	 * @param endTime End time of the given timespan.
	 * @param resolution Resolution for the fetch request.
	 * @return A <code>ValueExtractor</code> object holding all fetched data.
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected ValueExtractor fetch ( long startTime, long endTime, long resolution, int reduceFactor ) throws IOException, RrdException
	{
		if ( rrd == null )
			openRrd();

		int dsSize				= 0;
		String[] dsNames, vNames;

		long rrdStep			= rrdDef.getStep();
		FetchData[] result		= new FetchData[datasources.length];
		
		String[] names 			= new String[numSources];
		int tblPos		= 0;
		
		for (int i = 0; i < datasources.length; i++)
		{
			dsSize				= datasources[i].size();

			if ( dsSize > 0 )
			{
				// Set the list of ds names
				dsNames 	= new String[ dsSize ];
				vNames		= new String[ dsSize ];
				
				for (int j = 0; j < dsSize; j++ ) {
					String[] spair	= (String[]) datasources[i].get(j);
					dsNames[j]	 	= spair[0];
					vNames[j]		= spair[1];
				}
				
				// Fetch datasources
				FetchRequest request		= rrd.createFetchRequest( cfNames[i], startTime, endTime, resolution );
				request.setFilter( dsNames );

				FetchData data				= request.fetchData();

				for (int j = 0; j < dsSize; j++)
					names[ data.getDsIndex(dsNames[j]) + tblPos ] = vNames[j];
				tblPos						+= dsSize; 
				
				result[i]					= data;
			}
		}
		
		return new ValueExtractor( names, result, reduceFactor );
	}

	/**
	 * Retrieves the RrdDb connected to this FetchSource.
	 * The RrdDb instance is retrieved through the use of the
	 * RrdOpener that is referred internally in the FetchSourceList.
	 * It is okay to call this method multiple times in a row.
	 *
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected void openRrd() throws RrdException, IOException
	{
		if ( rrd == null )
		{
			org.jrobin.core.RrdOpener opener = listReference.getRrdOpener();

			if ( opener == null )
				throw new RrdException( "No RrdOpener specified for RRD management." );

			// Only open if not open yet
			if ( rrd == null )
				rrd = opener.getRrd( rrdFile, getRrdBackendFactory() );

			rrdDef	= rrd.getRrdDef();
		}
	}

	/**
	 * Gets the RrdDb instance for this FetchSource.  If the
	 * RrdDb has not been retrieved yet, it is before this
	 * method returns. It is okay to call this method multiple
	 * times in a row.
	 *
	 * @return Reference to the RrdDb instance.
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected RrdDb getRrd() throws RrdException, IOException
	{
		if ( rrd == null )
			openRrd();

		return rrd;
	}

	/**
	 * Releases the internal RrdDb reference for this FetchSource.
	 * It is okay to call this method multiple times in a row.
	 *
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected void release() throws RrdException, IOException
	{
		if ( rrd != null )
		{
			org.jrobin.core.RrdOpener opener = listReference.getRrdOpener();

			if ( opener == null )
				throw new RrdException( "No RrdOpener specified for RRD management." );

			opener.releaseRrd( rrd );
			rrd = null;
		}
	}

	/**
	 * Returns the timestamp of the last completed sample before or on the given time.
	 * This method is useful to find out the actual last timestamp for graphing, if the
	 * current time is after the last update time.  This sample can contain bad (Unknown)
	 * values, as long as the interval for it has been completed. This is not the
	 * timestamp of the last non-unknown sample!
	 *
	 * @param startTime Timestamp for which the last sample time should be calculated.
	 * @param endTime Timestamp for which the last sample time should be calculated.
	 * @param resolution Last timestamp for this particular fetch resolution.
	 * @return Last sample timestamp in seconds.
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected long getLastSampleTime( long startTime, long endTime, long resolution ) throws RrdException, IOException
	{
		if ( rrd == null )
			openRrd();

		long minSampleTime = Util.MAX_LONG, sampleTime = 0;

		for ( int i = 0; i < datasources.length; i++ )
		{
			if ( datasources[i].size() > 0 )
			{
				sampleTime = rrd.findStartMatchArchive( cfNames[i], startTime, resolution ).getEndTime();

				if ( sampleTime < minSampleTime )
					minSampleTime = sampleTime;
			}
		}

		return minSampleTime;
	}

	/**
	 * Returns an array of the smallest and the largest step in the set.
	 *
	 * @param startTime
	 * @param endTime
	 * @param resolution
	 * @return
	 * @throws RrdException
	 * @throws IOException
	 */
	protected long[] getFetchStep( long startTime, long endTime, long resolution ) throws RrdException, IOException
	{
		if ( rrd == null )
			openRrd();

		long maxStep = Util.MIN_LONG, minStep = Util.MAX_LONG, step = 0;

		for ( int i = 0; i < datasources.length; i++ )
		{
			if ( datasources[i].size() > 0 )
			{
				FetchRequest request 	= rrd.createFetchRequest( cfNames[i], startTime, endTime, resolution );
				step					= rrd.findMatchingArchive( request ).getArcStep();

				if ( step < minStep )
					minStep = step;
				if ( step > maxStep )
					maxStep = step;
			}
		}

		return new long[] { minStep, maxStep };
	}

	protected String getRrdFile() {
		return rrdFile;
	}

	protected RrdBackendFactory getRrdBackendFactory() throws RrdException
	{
		if ( backendName != null )
			return RrdBackendFactory.getFactory( backendName );

		return RrdBackendFactory.getDefaultFactory();
	}

	public void exportXml(XmlWriter xml) {
		for ( int i = 0; i < datasources.length; i++ ) {
			for ( int j = 0; j < datasources[i].size(); j++ ) {
				String[] pair = (String[]) datasources[i].get(j);
				xml.startTag("def");
				xml.writeTag("name", pair[1]);
				xml.writeTag("rrd", rrdFile);
				xml.writeTag("source", pair[0]);
				xml.writeTag("cf", cfNames[i]);
				xml.closeTag(); // def
			}
		}
	}
}
