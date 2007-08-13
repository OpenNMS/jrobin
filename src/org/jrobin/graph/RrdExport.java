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

import org.jrobin.core.RrdException;
import org.jrobin.core.RrdOpener;

import java.io.IOException;

/**
 * <p>RrdExport can be used to export graph-like datasources to XML format,
 * by means of the ExportData object.  More information about Export can be
 * found in the RRDtool XPORT man page.</p>
 *
 * <p>RrdExport needs a RrdExportDef that holds the configuration.</p>
 *
 * @author Arne Vandamme (cobralord@jrobin.org)
 */
public class RrdExport extends RrdExporter
{
	// ================================================================
	// -- Members
	// ================================================================
	private int maxRows						= 400;							// Default width of a graph


	// ================================================================
	// -- Constructors
	// ================================================================
	/**
	 * Constructs a new RrdExport object based on a RrdExportDef
	 * containing the configuration.
	 *
	 * @param def Reference to a RrdExportDef object.
	 */
	public RrdExport( RrdExportDef def )
	{
		super( def );
	}

	/**
	 * Constructs a new RrdExport object based on a RrdExportDef
	 * containing the configuration and a RrdOpener for opening the RRD
	 * datasources.
	 *
	 * @param def Reference to a RrdExportDef object.
	 * @param rrdOpener Reference to a RrdOpener object.
	 */
	public RrdExport( RrdExportDef def, RrdOpener rrdOpener )
	{
		super( def, rrdOpener );
	}

	/**
	 * Constructs a new RrdExport object based on a RrdExportDef
	 * containing the configuration and a ballpark figure of
	 * maximum number of rows in the reduced dataset.
	 *
	 * @param def Reference to a RrdExportDef object.
	 * @param maxRows Maximum number of rows in the reduced dataset.
	 */
	public RrdExport( RrdExportDef def, int maxRows )
	{
		super( def );
		setMaxRows( maxRows );
	}

	/**
	 * Constructs a new RrdExport object based on a RrdExportDef
	 * containing the configuration, RrdOpener for opening the RRD
	 * datasources and a ballpark figure of maximum number of rows
	 * in the reduced dataset.
	 *
	 * @param def Reference to a RrdExportDef object.
	 * @param rrdOpener Reference to a RrdOpener object.
	 * @param maxRows Maximum number of rows in the reduced dataset.
	 */
	public RrdExport( RrdExportDef def, RrdOpener rrdOpener, int maxRows )
	{
		super( def, rrdOpener );
		setMaxRows( maxRows );
	}


	// ================================================================
	// -- Public methods
	// ================================================================
	/**
	 * Sets the RrdExportDef that holds the export configuration.
	 *
	 * @param def Reference to a RrdExportDef object.
	 */
	public void setExportDef( RrdExportDef def )
	{
		super.setExportDef( def );
	}

	/**
	 * Sets the rrd opener reference to use for data retrieval.
	 *
	 * @param rrdOpener Reference to a RrdOpener object.
	 */
	public void setRrdOpener( RrdOpener rrdOpener )
	{
		super.setRrdOpener( rrdOpener );
	}

	/**
	 * Sets the maximum number of rows that the reduced dataset should
	 * contain.  Note: this number is not absolute and can be overruled
	 * in some cases.
	 *
	 * @param maxRows Maximum number of rows in the reduced dataset.
	 */
	public void setMaxRows( int maxRows )
	{
		this.maxRows = maxRows;
	}

	/**
	 * Fetches the reduced dataset based on provided RrdExportDef as
	 * a ExportData object.
	 *
	 * @param maxRows Maximum number of rows in the returned set.
	 * @return ExportData object.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 * @throws IOException Thrown in case of I/O related error.
	 */
	public ExportData fetch( int maxRows ) throws RrdException, IOException {
		return super.fetch( maxRows );
	}

	/**
	 * Fetches the reduced dataset based on provided RrdExportDef as
	 * a ExportData object.
	 *
	 * @return ExportData object.
	 * @throws RrdException Thrown in case of JRobin specific error.
	 * @throws IOException Thrown in case of I/O related error.
	 */
	public ExportData fetch() throws RrdException, IOException {
		return super.fetch( maxRows );
	}

	public int getMaxRows() {
		return maxRows;
	}
}
