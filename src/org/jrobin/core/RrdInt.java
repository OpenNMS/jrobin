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

import java.io.IOException;

class RrdInt {
	private RrdFile file;
	private long pointer;
	private int count;

	private boolean cached = false;
	private int cachedValue;

	RrdInt(RrdUpdater updater, int count) throws IOException {
		this.count = count;
		file = updater.getRrdFile();
		pointer = file.allocate(RrdFile.INT_SIZE, count);
	}

	RrdInt(RrdUpdater updater) throws IOException {
		this(updater, 1);
	}

	RrdInt(int initValue, RrdUpdater updater) throws IOException {
		this.count = 1;
		file = updater.getRrdFile();
		pointer = file.allocate(initValue);
		cached = true;
		cachedValue = initValue;
	}

	void set(int index, int value) throws IOException {
		assert index < count;
		long readPointer = pointer + index * RrdFile.INT_SIZE;
		file.writeInt(readPointer, value);
	}

	void set(int value) throws IOException {
		cached = true;
		cachedValue = value;
		set(0, value);
	}

	int get(int index) throws IOException {
		assert index < count;
		long readPointer = pointer + index * RrdFile.INT_SIZE;
		return file.readInt(readPointer);
	}

	int get() throws IOException {
		if(!cached) {
			cachedValue = get(0);
			cached = true;
		}
		return cachedValue;
	}
}
