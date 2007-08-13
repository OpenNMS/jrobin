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

/**
 * <p>Interface to be used for custom datasources.
 * If you wish to use a custom datasource in a graph, you should create a class implementing this interface
 * that represents that datasource, and then pass this class on to the RrdGraphDef.</p>
 * 
 * @author Arne Vandamme <cobralord@cherrymon.org>
 */
public abstract class Plottable 
{
	/**
	 * Retrieves datapoint value based on a given timestamp.
	 * Use this method if you only have one series of data in this class.
	 * @param timestamp Timestamp in seconds for the datapoint.
	 * @return Double value of the datapoint.
	 */
	public double getValue( long timestamp ) {
		return Double.NaN;
	}
	
	/**
	 * Retrieves datapoint value based on a given timestamp.
	 * @param timestamp Timestamp in seconds for the datapoint.
	 * @param index Integer referring to the series containing the specific datapoint.
	 * @return Double value of the datapoint.
	 */
	public double getValue( long timestamp, int index ) {
		return Double.NaN;
	}
	
	/**
	 * Retrieves datapoint value based on a given timestamp.
	 * @param timestamp Timestamp in seconds for the datapoint.
	 * @param fieldName String that refers to the series containing the datapoint.
	 * @return Double value of the datapoint.
	 */
	public double getValue( long timestamp, String fieldName ) {
		return Double.NaN;
	}
}
