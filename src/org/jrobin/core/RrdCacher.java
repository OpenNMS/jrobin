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

package org.jrobin.core;

class RrdCacher {
	private Object cache = null;

	boolean setInt(int value) {
		if(isCached(value)) {
			return false;
		}
		else {
			cache = new Integer(value);
			return true;
		}
	}

	boolean setLong(long value) {
		if(isCached(value)) {
			return false;
		}
		else {
			cache = new Long(value);
			return true;
		}
	}

	boolean setDouble(double value) {
		if(isCached(value)) {
			return false;
		}
		else {
			cache = new Double(value);
			return true;
		}
	}

	boolean setString(String value) {
		if(isCached(value)) {
			return false;
		}
		else {
			cache = value;
			return true;
		}
	}

	boolean isEmpty() {
		return cache == null;
	}

	int getInt() {
		return ((Integer) cache).intValue();
	}

	long getLong() {
		return ((Long) cache).longValue();
	}

	double getDouble() {
		return ((Double) cache).doubleValue();
	}

	String getString() {
		return (String) cache;
	}

	private boolean isCached(int value) {
		return cache != null && getInt() == value;
	}

	private boolean isCached(long value) {
		return cache != null && getLong() == value;
	}

	private boolean isCached(double value) {
		return cache != null && getDouble() == value;
	}

	private boolean isCached(String value) {
		return cache != null && getString().equals(value);
	}
}
