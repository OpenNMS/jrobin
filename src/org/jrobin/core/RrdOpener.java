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
package org.jrobin.core;

import java.io.IOException;

/**
 * <p>Class that represents an object that can be used to perform the actual
 * opening and closing of RRD files, using different methods.  Other objects
 * like the FetchSourceList representing the Graph datasources
 * ({@link org.jrobin.graph.FetchSourceList}) use a RrdOpener to retrieve the RrdDb instances of
 * RRD datasources.</p>
 * <p>Overriding the RrdOpener class allows finetuned access on the level
 * of RrdDb retrieval and release.  An child class could for example add
 * log or debug statements, gather statistics on RRD access, or provide
 * a transparent way to to access RRD datasources in an alternative way
 * like through a DBMS. </p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class RrdOpener
{
	// ================================================================
	// -- Members
	// ================================================================
	protected RrdDbPool pool;

	protected boolean readOnly	= false;
	protected boolean usePool	= false;


	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Creates a new RrdOpener that will open RrdDb objects with read/write
	 * access.  If the usePool flag is set, the RrdOpener will use the RrdDbPool
	 * to retrieve RrdDb instances.
	 * @param usePool True if the RrdOpener should use the RrdDbPool.
	 */
	public RrdOpener( boolean usePool )
	{
		this.usePool = usePool;

		if ( usePool )
			pool = RrdDbPool.getInstance();
	}

	/**
	 * Creates a new RrdOpener that will open RrdDb objects with read/write
	 * or read-only access, depending on the readOnly flag..  If the usePool
	 * flag is set, the RrdOpener will use the RrdDbPool to retrieve RrdDb
	 * instances.
	 * @param usePool True if the RrdOpener should use the RrdDbPool.
	 * @param readOnly True if the RrdOpener should open RrdDb objects as read-only.
	 */
	public RrdOpener( boolean usePool, boolean readOnly )
	{
		this( usePool );
		this.readOnly = readOnly;
	}


	// ================================================================
	// -- Public methods
	// ================================================================
	/**
	 * Retrieves the RrdDb instance matching a specific RRD datasource name
	 * (usually a file name) and using a specified RrdBackendFactory.
	 *
	 * @param rrdFile Name of the RRD datasource.
	 * @param backendFactory BackendFactory to use for retrieval.
	 * @return RrdDb instance of the datasource.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	public RrdDb getRrd( String rrdFile, RrdBackendFactory backendFactory  ) throws IOException, RrdException
	{
		if ( pool != null )
			return pool.requestRrdDb( rrdFile );
		else
			return new RrdDb( rrdFile, readOnly, backendFactory );
	}

	/**
	 * Releases an RrdDb instance.  Depending on the settings of the RrdOpener,
	 * this either closes the RrdDb, or releases it back into the RrdDbPool.
	 * @param rrdDb RrdDb object that should be released.
	 * @throws IOException Thrown in case of I/O error.
	 * @throws RrdException Thrown in case of a JRobin specific error.
	 */
	public void releaseRrd(RrdDb rrdDb) throws IOException, RrdException
	{
		if ( pool != null )
			pool.release(rrdDb);
		else
			rrdDb.close();
	}
}
