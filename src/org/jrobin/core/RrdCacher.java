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

final class RrdCacher {
	private boolean cached = false;
	private int i;
	private long l;
	private double d;
	private String s;

	final boolean setInt(int value) {
		if (cached && value == i) {
			return false;
		}
		else {
			i = value;
			return cached = true;
		}
	}

	final boolean setLong(long value) {
		if(cached && value == l) {
			return false;
		}
		else {
			l = value;
			return cached = true;
		}
	}

	final boolean setDouble(double value) {
		if(cached && value == d) {
			return false;
		}
		else {
			d = value;
			return cached = true;
		}
	}

	final boolean setString(String value) {
		if(cached && value.equals(s)) {
			return false;
		}
		else {
			s = value;
			return cached = true;
		}
	}

	final boolean isEmpty() {
		return !cached;
	}

	final int getInt() {
		return i;
	}

	final long getLong() {
		return l;
	}

	final double getDouble() {
		return d;
	}

	final String getString() {
		return s;
	}

	final void clearCache() {
		cached = false;
	}

}
