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

import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;

import org.jrobin.core.*;

/**
 * <p>RrdExporter takes care of calculating a reduced dataset based on a RrdExportDef.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
class RrdExporter
{
	private RrdExportDef def;
	private RrdOpener rrdOpener;

	protected int numRows, reducedNumRows;								// Actual number of rows in the data set
	protected long startTime, endTime;									// Exact (requested) start and end time
	protected long reducedStartTime, reducedEndTime, reducedStep;		// Reduced start and end time, with step for the reduced set

	protected long[] timestamps;
	protected Source[] sources;
	protected HashMap sourceIndex;
	protected FetchSourceList fetchSources;

	RrdExporter( RrdExportDef def )
	{
		rrdOpener = new RrdOpener( false, true );
		setExportDef( def );
	}

	RrdExporter( RrdExportDef def, RrdOpener rrdOpener )
	{
		this.rrdOpener	= rrdOpener;
		setExportDef( def );
	}

	void setExportDef( RrdExportDef def )
	{
		this.def 			= def;
		this.fetchSources	= def.getFetchSources();
		fetchSources.setRrdOpener( rrdOpener );
	}

	void setRrdOpener( RrdOpener rrdOpener )
	{
		if ( fetchSources != null )
			fetchSources.setRrdOpener( rrdOpener );
	}

	/**
	 * Fetches and calculates all datasources requested.
	 *
	 * This method is NOT synchronized, but it is important that access to it is controlled
	 * manually.  This should only be an issue if you are using it for package access, since
	 * the public interface of RrdExport only uses the synchronized fetch method.
	 *
	 * @throws org.jrobin.core.RrdException Thrown in case of a JRobin specific error.
	 * @throws java.io.IOException Thrown in case of a I/O related error.
	 */
	protected void calculateSeries( int maxRows ) throws RrdException, IOException
	{
		ValueExtractor ve;
		FetchSource src;
		String[] varList;

		long finalEndTime 		= Long.MAX_VALUE;
		boolean changingEndTime = false;

		long startTime 			= def.getStartTime();
		long endTime			= def.getEndTime();
		changingEndTime			= (endTime == 0);
		numRows 				= maxRows;
		reducedNumRows			= maxRows;

		int numDefs				= def.getNumDefs();
		int numSdefs			= def.getNumSdefs();

		Cdef[] cdefList			= def.getCdefs();
		int numCdefs			= cdefList.length;

		Pdef[] pdefList			= def.getPdefs();
		int numPdefs			= pdefList.length;

		ExportData[] edata		= def.getExportData();
		int[] edefTs;
		Source[] edefList;
		if ( edata.length > 0 )
		{
			ArrayList tsList	= new ArrayList( 30 );
			ArrayList list		= new ArrayList( 30 );
			for ( int i = 0; i < edata.length; i++ )
			{
				Source[] esrc = edata[i].getSources();

				for ( int j = 0; j < esrc.length; j++ )
				{
					list.add( esrc[j] );
					tsList.add( new Integer(i) );
				}
			}
			edefTs				= new int[ tsList.size() ];
			for ( int i = 0; i < edefTs.length; i++ )
				edefTs[i]		= ((Integer) tsList.get(i)).intValue();
			edefList			= (Source[]) list.toArray( new Source[] {} );
		}
		else
		{
			edefTs				= new int[0];
			edefList			= new Source[0];
		}
		int numEdefs			= edefList.length;

		// Set up the array with all datasources (both Def, Cdef and Pdef)
		sources 				= new Source[ numDefs + numEdefs + numCdefs + numPdefs ];
		sourceIndex 			= new HashMap( numDefs + numEdefs + numCdefs + numPdefs );
		int tblPos				= 0;
		int vePos				= 0;

		ValueExtractor[] veList	= new ValueExtractor[ fetchSources.size() ];

		long requestedStep		= (long) (endTime - startTime) / maxRows;

		// Shift start and endTime
		int minReduceFactor 	= 1;
		long minStep 			= Integer.MAX_VALUE, maxStep = Integer.MIN_VALUE, vStartTime, vEndTime, fetchEndTime;

		// -- Open all fetch datasources
		if ( fetchSources.size() > 0 || numEdefs > 0 )
		{
			try
			{
				fetchSources.openAll();

				// Calculate the reduce data factor
				for ( int i = 0; i < fetchSources.size(); i++ )
				{
					src					= fetchSources.get( i );

					if ( changingEndTime )
					{
						endTime		= src.getLastSampleTime( startTime, endTime, def.getResolution() );

						if ( endTime < finalEndTime )
							finalEndTime = endTime;

						requestedStep = (long) (endTime - startTime) / maxRows;
					}

					// Calculate the step for data retrieval
					long[] steps		= src.getFetchStep( startTime, endTime, def.getResolution() );

					int reduceFactor	= (int) Math.ceil( (double) requestedStep / (double) steps[0] );
					steps[0]			= steps[0] * reduceFactor;

					if ( steps[0] < minStep )
					{
						minStep 		= steps[0];
						minReduceFactor = reduceFactor;
					}
					if ( steps[1] > maxStep )
						maxStep			= steps[1];
				}

				for ( int i = 0; i < edata.length; i++ )
				{
					long step = edata[i].getStep();

					int reduceFactor	= (int) Math.ceil( (double) requestedStep / (double) step );
					step				= step * reduceFactor;

					if ( step < minStep )
					{
						minStep 		= step;
						minReduceFactor = reduceFactor;
					}
					if ( step > maxStep )
						maxStep			= step;
				}

				vStartTime			= Util.normalize( startTime, minStep );
				vStartTime			= ( vStartTime > startTime ? vStartTime - minStep : vStartTime );

				if ( !changingEndTime )
				{
					vEndTime			= Util.normalize( endTime, minStep );
					vEndTime			= ( vEndTime < endTime ? vEndTime + minStep : vEndTime );
				}
				else
				{
					vEndTime			= Util.normalize( finalEndTime, minStep );
					vEndTime			= ( vEndTime < finalEndTime ? vEndTime + minStep : vEndTime );
				}

				// This is the actual end time for the reduced data set
				reducedEndTime			= vEndTime;
				reducedStartTime		= vStartTime;
				reducedStep				= minStep;
				reducedNumRows			= (int) ((reducedEndTime - reducedStartTime) / reducedStep) + 1;

				fetchEndTime			= Util.normalize( vEndTime, maxStep );
				fetchEndTime			= ( fetchEndTime < vEndTime ? vEndTime + maxStep : fetchEndTime );

				// Now move back to the first time greater than or equal to fetchEndTime, normalized on minStep
				vEndTime				= Util.normalize( fetchEndTime, minStep );
				vEndTime				= ( vEndTime < fetchEndTime ? vEndTime + minStep : vEndTime );

				// Calculate numRows in the end table
				numRows					= (int) ((vEndTime - vStartTime) / minStep) + 1;

				// Fetch the actual data
				for ( int i = 0; i < fetchSources.size(); i++ )
				{
					src					= fetchSources.get( i );

					// Fetch all required datasources
					ve 		= src.fetch( vStartTime, vEndTime, def.getResolution(), minReduceFactor );
					varList = ve.getNames();

					for (int j= 0; j < varList.length; j++) {
						sources[tblPos]	= new Def( varList[j], numRows, reducedNumRows );
						sourceIndex.put( varList[j], new Integer(tblPos++) );
					}

					veList[ vePos++ ] = ve;
				}
			}
			finally
			{
				// Release all datasources again
				fetchSources.releaseAll();
			}
		}
		else
		{
			// The range should be used exactly as specified
			minStep					= requestedStep;
			vStartTime				= Util.normalize( startTime, minStep );
			vStartTime				= ( vStartTime > startTime ? vStartTime - minStep : vStartTime );

			if ( !changingEndTime )
			{
				vEndTime			= Util.normalize( endTime, minStep );
				vEndTime			= ( vEndTime < endTime ? vEndTime + minStep : vEndTime );
			}
			else
			{
				vEndTime			= Util.normalize( Util.getTime(), minStep );
				vEndTime			= ( vEndTime < endTime ? vEndTime + minStep : vEndTime );
			}

			reducedEndTime			= vEndTime;
			reducedStartTime		= vStartTime;
			reducedStep				= minStep;
			reducedNumRows			= (int) ((reducedEndTime - reducedStartTime) / reducedStep) + 1;
			finalEndTime			= endTime;

			vEndTime				+= minStep;
			numRows					= (int) ((vEndTime - vStartTime) / minStep) + 1;
		}

		// -- Add all Export datasources to the source table
		for ( int i = 0; i < edefList.length; i++ )
		{
			sources[tblPos] = new Def( edefList[i].getName(), numRows, reducedNumRows );
			sources[tblPos].setFetchedStep( edefList[i].getStep() );
			sourceIndex.put( edefList[i].getName(), new Integer(tblPos++) );
		}

		// -- Add all Pdefs to the source table
		for ( int i = 0; i < pdefList.length; i++ )
		{
			pdefList[i].prepare( numRows, reducedNumRows );
			pdefList[i].setFetchedStep( minStep );

			sources[tblPos] = pdefList[i];
			sourceIndex.put( pdefList[i].getName(), new Integer(tblPos++) );
		}

		int cdefStart = tblPos;		// First Cdef element, necessary for tree descend calculation

		// -- Add all Cdefs to the source table
		// Reparse all RPN datasources to use indices of the correct variables
		for ( int i = 0; i < cdefList.length; i++ )
		{
			cdefList[i].prepare( sourceIndex, numRows, reducedNumRows );
			cdefList[i].setFetchedStep( minStep );

			sources[tblPos]	= cdefList[i];
			sourceIndex.put( cdefList[i].getName(), new Integer(tblPos++) );
		}

		// Fill the array for all datasources
		timestamps 				= new long[numRows];

		// RPN calculator for the Cdefs
		RpnCalculator rpnCalc 	= new RpnCalculator( sources, minStep );

		int pos = 0;
		for (int j = 0; j < veList.length; j++)
			pos = veList[j].prepareSources( sources, pos );

		// **************************************************************************************** //
		// If there are Sdefs, we should determine a tree-descend order for calculation.			//
		// An Sdef is completely dependant on another datasource and can only be calculated			//
		// after the datasource it depends on has been calculated itself entirely.					//
		//  e.g. The Sdef giving the AVG of a Def should be one lower in the calculation tree		//
		//		 than the corresponding Def.  Lower = higher level.									//
		// Since Sdefs can be nested and combined into new Cdefs and possibly resulting in new		//
		// Sdefs, the worst case calculation could result in every datasource being calculated		//
		// separately, resulting in more overhead.  However the impact of this should remain fairly	//
		// small in CPU time.																		//
		// **************************************************************************************** //
		if ( numSdefs > 0 )
		{
			// Initalize level for each def on 0
			int treeDepth	= 0;
			int[] treeLevel = new int[ sources.length ];

			// First level contains all fetched datasources, custom datasources and combined datasources that use other first levels
			for ( int i = cdefStart; i < sources.length; i++ )
			{
				// Get the level of all defs needed, take the maximum level
				int level 		= ((Cdef) sources[i]).calculateLevel( treeLevel );
				treeDepth		= (level > treeDepth ? level : treeDepth);

				treeLevel[i]	= level;
			}

			// Run through each level of the tree
			long t;

			for ( int l = 0; l <= treeDepth; l++ )
			{
				t 	= vStartTime - minStep;
				for ( int i = 0; i < numRows; i++ )
				{
					pos = cdefStart;

					// First level of the tree includes fetched datasources and pdefs,
					// since these values can never depend on others in the list.
					if ( l == 0 )
					{
						// Calculate new timestamp
						pos	= 0;
						t 	+= minStep;

						// Get all fetched datasources
						for (int j = 0; j < veList.length; j++)
							pos = veList[j].extract( t, sources, i, pos );

						// Get all export datasources
						for (int j = pos; j < pos + numEdefs; j++ )
							sources[j].set( i, t, edefList[j - pos].get( t, edata[ edefTs[j - pos] ].getTimestamps() ) );
						pos += numEdefs;

						// Get all custom datasources
						for (int j = pos; j < pos + numPdefs; j++)
							((Pdef) sources[j]).set( i, t );
						pos += numPdefs;

						timestamps[i] = t;
					}
					else
						t = timestamps[i];

					// Calculate the cdefs of this level
					for ( int j = pos; j < sources.length; j++ )
					{
						if ( treeLevel[j] == l )
						{
							// This Cdef/Sdef can be calculated
							if ( sources[j] instanceof Sdef )
								((Sdef) sources[j]).set( sources );
							else
								sources[j].set( i, t, rpnCalc.evaluate( (Cdef) sources[j], i, t ) );
						}
					}
				}
			}
		}
		else
		{
			// Traditional way of calculating all datasources, slightly faster
			long t = vStartTime - minStep;
			for ( int i = 0; i < numRows; i++ )
			{
				t		+= minStep;
				pos 	= 0;

				// Get all fetched datasources
				for (int j = 0; j < veList.length; j++)
					pos = veList[j].extract( t, sources, i, pos );

				// Get all export datasources
				for (int j = pos; j < pos + numEdefs; j++ )
					sources[j].set( i, t, edefList[j - pos].get( t, edata[ edefTs[j - pos] ].getTimestamps() ) );
				pos += numEdefs;

				// Get all custom datasources
				for (int j = pos; j < pos + numPdefs; j++)
					((Pdef) sources[j]).set( i, t );
				pos += numPdefs;

				// Get all combined datasources
				for (int j = pos; j < sources.length; j++)
					sources[j].set(i, t, rpnCalc.evaluate( (Cdef) sources[j], i, t ) );

				timestamps[i] = t;
			}
		}

		// Clean up the fetched datasources forcibly
		veList = null;

		this.startTime 	= startTime;
		this.endTime	= ( changingEndTime ? finalEndTime : endTime );
	}

	private Source getSource( String name ) throws RrdException
	{
		if ( !sourceIndex.containsKey(name) )
			throw new RrdException( "No such datasource: " + name );

		return sources[ ( (Integer) sourceIndex.get(name) ).intValue() ];
	}

	/**
	 * Creates an ExportData object corresponding to the reduced dataset
	 * contained in the RrdExporter.  This assumes that the reduced dataset
	 * has been calculated already!
	 *
	 * @return ExportData object created.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 */
	protected ExportData createExportData() throws RrdException
	{
		// Now create a RrdDataSet object containing the results
		Source[] sourceSet;
		String[][] export 	= def.getExportDatasources();
		HashMap legends		= new HashMap( export.length );

		if ( def.isStrict() )
		{
			sourceSet			= new Def[ export.length ];
			for ( int i = 0; i < export.length; i++ )
				sourceSet[i] = createReducedDef( getSource( export[i][0] ) );
		}
		else
		{
			sourceSet			= new Def[ sources.length ];
			for ( int i = 0; i < sources.length; i++ )
			{
				sourceSet[i] = createReducedDef( sources[i] );
				legends.put( sourceSet[i].getName(), sourceSet[i].getName() );
			}
		}

		for ( int i = 0; i < export.length; i++ )
			legends.put( export[i][0], export[i][1] );

		long[] reducedTs = new long[ reducedNumRows ];
		System.arraycopy( timestamps, 0, reducedTs, 0, reducedNumRows );

		return new ExportData( reducedTs, sourceSet, legends );
	}

	private Def createReducedDef( Source origSrc )
	{
		Def src = new Def( origSrc.getName(), reducedNumRows, reducedNumRows );
		src.setFetchedStep( reducedStep );

		for ( int i = 0; i < reducedNumRows; i++ )
			src.set( i, timestamps[i], origSrc.get(i) );

		return src;
	}

	/**
	 * Provides a convenient synchronized wrapper around calculateSeries and createExportData.
	 */
	protected synchronized ExportData fetch( int maxRows ) throws RrdException, IOException
	{
		// Calculate the requested reduced data set
		calculateSeries( maxRows );

		return createExportData();
	}
}
