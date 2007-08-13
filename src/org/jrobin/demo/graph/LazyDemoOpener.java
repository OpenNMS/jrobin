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

import org.jrobin.core.RrdOpener;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdBackendFactory;
import org.jrobin.core.RrdException;

import java.io.IOException;

/**
 * <p>Basic implementation showing the use of RrdOpener.  This demo
 * class simply logs an attempt to retrieve an RrdDb and diverts all
 * actual RrdDb retrieval to the basic RrdOpener.</p>
 * 
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class LazyDemoOpener extends RrdOpener
{
	/**
	 * Custom constructor.
	 */
	LazyDemoOpener()
	{
		//  Our custom RrdOpener does not use the pool, to better illustrate
		super( false, true );

		System.out.println( "LOG: RrdOpener object created." );
	}

	/**
	 * Retrieves an RrdDb instance.
	 */
	public RrdDb getRrd( String name, RrdBackendFactory backendFactory ) throws RrdException, IOException
	{
		RrdDb db = null;

		System.out.println( "LOG: Access request for RRD with name " + name + " (backend: " + backendFactory.getFactoryName() + ")" );
		db 		= super.getRrd( name, backendFactory );

		if ( db == null )
			System.out.println( "LOG: FAILURE locating RRD with name " + name );
		else
			System.out.println( "LOG: SUCCESS locating RRD with name " + name );

		return db;
	}

	/**
	 * Releases RrdDb instance.
	 */
	public void releaseRrd( RrdDb rrdDb ) throws IOException, RrdException
	{
		System.out.println( "LOG: Releasing RRD " + rrdDb.getPath() );

		super.releaseRrd( rrdDb );
	}
}
