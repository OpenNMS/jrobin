/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003, by Sasa Markovic.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                Arne Vandamme (cobralord@jrobin.org)
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

class RrdString extends RrdPrimitive {
	private static final int SIZE = 20;
	
	private String cache;

	RrdString(RrdUpdater updater) throws IOException {
		super(updater, SIZE * 2);
		loadCache();
	}
	
	void loadCache() throws IOException {
		RrdFile rrdFile = getRrdFile();
		if(rrdFile.getRrdMode() == RrdFile.MODE_RESTORE) {
			rrdFile.seek(getPointer());
			char[] c = new char[SIZE];
			for(int i = 0; i < SIZE; i++) {
				c[i] = rrdFile.readChar();			
			}
			cache = new String(c).trim();
			cached = true;
		}
	}

	RrdString(String initValue, RrdUpdater updater) throws IOException {
		super(updater, SIZE * 2);
		set(initValue);
	}

	void set(String value) throws IOException {
		value = value.trim();
		if(!cached || !cache.equals(value)) {
			RrdFile rrdFile = getRrdFile();
			rrdFile.seek(getPointer());
			for(int i = 0; i < SIZE; i++) {
				if(i < value.length()) {
					rrdFile.writeChar(value.charAt(i));
				}
				else {
					rrdFile.writeChar(' ');
				}
			}
			cache = value;
			cached = true;
		}
	}

	String get() {
		assert cached: "Not cached!";
		return cache;
	}
}
