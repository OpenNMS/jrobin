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

import java.util.HashMap;
import java.util.ArrayList;
import java.io.IOException;

import org.jrobin.core.RrdException;
import org.jrobin.core.RrdDb;

/**
 * <p>A FetchSourceList represents a number of RRD datasources,
 * to be used with RrdGraphDef for Graph generation.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class FetchSourceList
{
	// ================================================================
	// -- Members
	// ================================================================
	private HashMap map;
	private ArrayList list;

	private int defCount;

	private boolean persistent;
	private boolean opened;

	private RrdOpener rrdOpener;


	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Creates a new FetchSourceList with the specified default size.
	 * The size of the actual list is not limited to this number, and
	 * the list will expand automatically if necessary.  The default size
	 * should be a ballpark figure for the number of different RrdDb
	 * that will be used (usually a RrdDb corresponds with a single RRD file).
	 *
	 * @param defaultSize Default size of the FetchSourceList.
	 */
	public FetchSourceList( int defaultSize )
	{
		this( defaultSize, false );
	}

	/**
	 * Creates a new FetchSourceList with the specified default size.
	 * The size of the actual list is not limited to this number, and
	 * the list will expand automatically if necessary.  The default size
	 * should be a ballpark figure for the number of different RrdDb
	 * that will be used (usually a RrdDb corresponds with a single RRD file).
	 *
	 * @param defaultSize Default size of the FetchSourceList.
	 * @param persistent True if the list is persistent, false if not.
	 */
	public FetchSourceList( int defaultSize, boolean persistent )
	{
		map				= new HashMap( defaultSize );
		list			= new ArrayList( defaultSize );

		opened			= false;
		this.persistent	= persistent;
	}

	/**
	 * Creates a new FetchSourceList with the specified default size.
	 * The size of the actual list is not limited to this number, and
	 * the list will expand automatically if necessary.  The default size
	 * should be a ballpark figure for the number of different RrdDb
	 * that will be used (usually a RrdDb corresponds with a single RRD file).
	 *
	 * @param defaultSize Default size of the FetchSourceList.
	 * @param persistent True if the list is persistent, false if not.
	 * @param rrdOpener Reference to the RrdOpener object that will be used
	 * 					for RrdDb retrieval.
	 */
	public FetchSourceList( int defaultSize, boolean persistent, RrdOpener rrdOpener )
	{
		this( defaultSize, persistent );

		this.rrdOpener	= rrdOpener;
	}


	// ================================================================
	// -- Public methods
	// ================================================================
	/**
	 * Sets the internal RrdOpener object to use for RrdDb retrieval.
	 *
	 * @param rrdOpener Reference to the corresponding RrdOpener instance.
	 */
	public void setRrdOpener( RrdOpener rrdOpener )
	{
		// Only allow RrdOpener change if not persistent
		if ( !persistent )
			this.rrdOpener	= rrdOpener;
	}

	public RrdOpener getRrdOpener() {
		return rrdOpener;
	}

	/**
	 * Sets the persistency state of the FetchSourceList.
	 * If the list is set as persistent, RrdDb's can be opened
	 * and retrieved, but not released, and the RrdOpener reference
	 * can not be changed.  This is useful to avoid premature closing
	 * and reopening of datasources for performance reasons.
	 *
	 * Setting a FetchSourceList as persistent requires you to manually
	 * control releasing all datasources (all calls to openAll() will
	 * still succeed).
	 *
	 * @param persistent True if the list should behave as persistent.
	 */
	public void setPersistent( boolean persistent ) {
		this.persistent = persistent;
	}

	/**
	 * Returns the number of FetchSources hold in the list.
	 * @return Number of different FetchSources.
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Returns the number of Defs represented by the
	 * different FetchSources.
	 * @return Number of Def definitions.
	 */
	public  int defCount() {
		return defCount;
	}

	/**
	 * Retrieves (opens) all RrdDb instances related to the
	 * different FetchSources.
	 * It is safe to call this method multiple times in a row.
	 *
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	public void openAll() throws RrdException, IOException
	{
		if ( opened ) return;

		for ( int i = 0; i < size(); i++ )
			get(i).openRrd();

		opened = true;
	}

	/**
	 * Releases all RrdDb instances for the FetchSources.
	 * It is safe to call this method multiple times in a row.
	 * In case of a persistent list, this method does nothing
	 * until persistency is removed.
	 *
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	public void releaseAll() throws RrdException, IOException
	{
		if ( persistent ) return;		// Do not allow release if this FSList is persistent

		for ( int i = 0; i < size(); i++ )
			get(i).release();

		opened = false;
	}

	/**
	 * Clears up the FetchSourceList for new use.
	 * This removes persistency, releases all RrdDb instances, and
	 * clears the internal list of FetchSources.  After clear()
	 * the list is empty.
	 *
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	public void clear() throws RrdException, IOException
	{
		persistent	 = false;

		releaseAll();

		map.clear();
		list.clear();
	}

	/**
	 * Returns the highest last update time in seconds of the datasources
	 * represented by the list.  If the update time differs for different
	 * datasources, the highest overall timestamp will be returned.
	 * 
	 * @return Last update time in seconds.
	 * @throws IOException Thrown in case of fetching I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	public long getLastUpdateTime() throws RrdException, IOException
	{
		RrdDb rrd;

		long maxUpdateTime	= 0;
		long lastUpdateTime = 0;

		for ( int i = 0; i < size(); i++ )
		{
			rrd	= get(i).getRrd();

			lastUpdateTime	= rrd.getLastUpdateTime();
			if ( lastUpdateTime > maxUpdateTime )
				maxUpdateTime	= lastUpdateTime;
		}

		return maxUpdateTime;
	}

	/**
	 * Adds a datasource for graphing purposes to the list,
	 * {@see RrdGraphDef#datasource( java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String, java.lang.String )}.

	 * @param name Internal datasource name, to be used in GraphDefs.
	 * @param file Path to RRD file.
	 * @param dsName Data source name defined in the RRD file.
	 * @param consolFunc Consolidation function that will be used to extract data from the RRD
	 * file ("AVERAGE", "MIN", "MAX" or "LAST").
	 * @param backend Name of the RrdBackendFactory that should be used for this RrdDb.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	public void add( String name, String file, String dsName, String consolFunc, String backend ) throws RrdException
	{
		if ( map.containsKey(file) )
		{
			FetchSource rf = (FetchSource) map.get(file);
			rf.addSource( consolFunc, dsName, name );
		}
		else
		{
			FetchSource fs = new FetchSource( file, consolFunc, dsName, name, backend, this );
			map.put( file, fs );
			list.add( fs );
		}

		defCount++;
	}

	/**
	 * Adds a datasource for graphing purposes to the list,
	 * {@see RrdGraphDef#datasource( java.lang.String, java.lang.String,
	 * java.lang.String, java.lang.String )}.
	 *
	 * @param name Internal datasource name, to be used in GraphDefs.
	 * @param file Path to RRD file.
	 * @param dsName Data source name defined in the RRD file.
	 * @param consolFunc Consolidation function that will be used to extract data from the RRD
	 * file ("AVERAGE", "MIN", "MAX" or "LAST").
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	public void add( String name, String file, String dsName, String consolFunc ) throws RrdException
	{
		if ( map.containsKey(file) )
		{
			FetchSource rf = (FetchSource) map.get(file);
			rf.addSource( consolFunc, dsName, name );
		}
		else
		{
			FetchSource fs = new FetchSource( file, consolFunc, dsName, name, this );
			map.put( file, fs );
			list.add( fs );
		}

		defCount++;
	}

	// ================================================================
	// -- Protected (package) methods
	// ================================================================
	/**
	 * Returns the FetchSource for the given index.
	 * 
	 * @param index Index of the FetchSource in the list.
	 * @return FetchSource instance.
	 */
	protected FetchSource get( int index )
	{
		return (FetchSource) list.get(index);
	}
}
