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
package jrobin.graph;

import java.util.Vector;
import java.io.IOException;

import jrobin.core.RrdDb;
import jrobin.core.FetchData;
import jrobin.core.FetchRequest;
import jrobin.core.RrdException;

/**
 * <p>Class used to group datasources per RRD db, for faster fetching.
 * A FetchSource represents one RRD database file, and will take care of all datasource
 * fetching using objects of the <code>core</code> package.  Fetching will be done in such 
 * a way that all datasources per consolidation function are fetched with the minimum possible
 * file reads.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class FetchSource 
{
	// ================================================================
	// -- Members
	// ================================================================
	protected static final int AVG			= 0;
	protected static final int MAX 			= 1;
	protected static final int MIN 			= 2;
	protected static final int LAST			= 3;
	protected static final int MAX_CF 		= 4;
	
	protected static final String[] cfNames	= new String[] { "AVERAGE", "MAX", "MIN", "LAST" };
	
	private String rrdFile;						// Holds the name of the RRD file
	
	private int numSources					= 0;
	private Vector[] datasources			= new Vector[MAX_CF];
	
	
	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a FetchSource object based on a RRD file name.
	 * @param rrdFile Name of the RRD file holding all datasources.
	 */
	protected FetchSource( String rrdFile )
	{
		this.rrdFile = rrdFile;
		
		// Initialization of datasource lists per CF
		for (int i = 0; i < datasources.length; i++)
			datasources[i] = new Vector();	
	}
	
	/**
	 * Constructs a FetchSource object based on a RRD file name, and
	 * adds a given datasource to the datasources list.
	 * @param rrdFile Name of the RRD file holding all datasources.
	 * @param consolFunc Consolidation function of the datasource to fetch.
	 * @param dsName Internal name of the datasource in the RRD file.
	 * @param name Variable name of the datasource in the graph definition.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	FetchSource( String rrdFile, String consolFunc, String dsName, String name ) throws RrdException
	{
		this( rrdFile );
		addSource( consolFunc, dsName, name );	
	}
	
	
	// ================================================================
	// -- Protected methods
	// ================================================================
	/**
	 * Adds a given datasource to the datasources list for this FetchSource.
	 * @param consolFunc Consolidation function of the datasource to fetch.
	 * @param dsName Internal name of the datasource in the RRD file.
	 * @param name Variable name of the datasource in the graph definition.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected void addSource( String consolFunc, String dsName, String name ) throws RrdException
	{
		if ( consolFunc.equalsIgnoreCase("AVERAGE") || consolFunc.equalsIgnoreCase("AVG") )
			datasources[AVG].add( new String[] { dsName, name } );
		else if ( consolFunc.equalsIgnoreCase("MAX") || consolFunc.equalsIgnoreCase("MAXIMUM") )
			datasources[MAX].add( new String[] { dsName, name } );
		else if ( consolFunc.equalsIgnoreCase("MIN") || consolFunc.equalsIgnoreCase("MINIMUM") )
			datasources[MIN].add( new String[] { dsName, name } );
		else if ( consolFunc.equalsIgnoreCase("LAST") )
			datasources[LAST].add( new String[] { dsName, name } );
		else
			throw new RrdException( "Invalid consolidation function specified." );
		
		numSources++;				
	}
	
	/**
	 * Fetches all datavalues for a given timespan out of the provided RRD file.
	 * @param rrd An open <code>RrdDb</code> object holding the necessary datasources.
	 * @param startTime Start time of the given timespan.
	 * @param endTime End time of the given timespan.
	 * @return A <code>ValueExtractor</code> object holding all fetched data.
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	protected ValueExtractor fetch ( RrdDb rrd, long startTime, long endTime ) throws IOException, RrdException
	{
		long rrdStep			= rrd.getRrdDef().getStep();
		FetchData[] result		= new FetchData[datasources.length];
		
		String[] names 			= new String[numSources];
		int tblPos		= 0;
		
		for (int i = 0; i < datasources.length; i++)
		{
			if ( datasources[i].size() > 0 ) {
				// Set the list of ds names
				String[] dsNames 	= new String[ datasources[i].size() ];
				String[] vNames		= new String[ datasources[i].size() ];
				
				for (int j = 0; j < dsNames.length; j++ ) {
					String[] spair	= (String[]) datasources[i].elementAt(j);
					dsNames[j]	 	= spair[0];
					vNames[j]		= spair[1];
				}
				
				// Fetch datasources
				FetchRequest request 		= rrd.createFetchRequest( cfNames[i], startTime, endTime + rrdStep);
				request.setFilter( dsNames );
				
				FetchData data				= request.fetchData();
				
				for (int j = 0; j < vNames.length; j++)
					names[ data.getDsIndex(dsNames[j]) + tblPos ] = vNames[j];
				tblPos				+= dsNames.length; 
				
				result[i]					= data;
			}
		}
		
		return new ValueExtractor( names, result );
	}

	protected String getRrdFile() {
		return rrdFile;
	}	
}
