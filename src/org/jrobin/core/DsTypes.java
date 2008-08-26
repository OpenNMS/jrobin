/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *
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

/**
 * Simple interface to represent available datasource types.
 */
public interface DsTypes {
	/**
	 * Constant to represent GAUGE datasource type
	 */
	public static final String DT_GAUGE = "GAUGE";

	/**
	 * Constant to represent COUNTER datasource type
	 */
	public static final String DT_COUNTER = "COUNTER";

	/**
	 * Constant to represent DERIVE datasource type
	 */
	public static final String DT_DERIVE = "DERIVE";

	/**
	 * Constant to represent ABSOLUTE datasource type
	 */
	public static final String DT_ABSOLUTE = "ABSOLUTE";
}
