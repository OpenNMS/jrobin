/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.sourceforge.net/projects/jrobin
 * Project Lead:  Sasa Markovic (saxon@eunet.yu);
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

/**
 *
 */
class RrdDouble {
	private RrdFile file;
	private long pointer;
	private int count;

	private boolean cached = false;
	private double cachedValue;

	RrdDouble(RrdUpdater updater, int count) throws IOException {
		this.count = count;
		file = updater.getRrdFile();
		pointer = file.allocate(RrdFile.DOUBLE_SIZE, count);
	}

	RrdDouble(RrdUpdater updater) throws IOException {
		this(updater, 1);
	}

	RrdDouble(double initValue, RrdUpdater updater) throws IOException {
		this.count = 1;
		file = updater.getRrdFile();
		pointer = file.allocate(initValue);
		cached = true;
		cachedValue = initValue;
	}

	void set(int index, double value) throws IOException {
		assert index < count;
		long readPointer = pointer + index * RrdFile.DOUBLE_SIZE;
		file.writeDouble(readPointer, value);
	}

	void set(double value) throws IOException {
		cached = true;
		cachedValue = value;
		set(0, value);
	}

	double get(int index) throws IOException {
		assert index < count;
		long readPointer = pointer + index * RrdFile.DOUBLE_SIZE;
		return file.readDouble(readPointer);
	}

	double get() throws IOException {
		if(!cached) {
			cachedValue = get(0);
			cached = true;
		}
		return cachedValue;
	}
}
