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

class RrdDoubleArray extends RrdPrimitive {
	
	private int length;

	RrdDoubleArray(RrdUpdater updater, int length) throws IOException {
		super(updater, length * RrdDouble.SIZE);
		this.length = length;
	}
	
	RrdDoubleArray(RrdUpdater updater, int length, double initVal) throws IOException {
		super(updater, length * RrdDouble.SIZE);
		this.length = length;
		for(int i = 0; i < length; i++) {
			set(i, initVal);
		}
	}

	void set(int index, double value) throws IOException {
		if(index >= length) {
			throw new IOException("Invalid index supplied: " + index + ", max = " + length);
		}
		RrdFile rrdFile = getRrdFile();
		rrdFile.seek(getPointer() + index * RrdDouble.SIZE);
		rrdFile.writeDouble(value);
	}

	double get(int index) throws IOException {
		if(index >= length) {
			throw new IOException("Invalid index supplied: " + index + ", max = " + length);
		}
		RrdFile rrdFile = getRrdFile();
		rrdFile.seek(getPointer() + index * RrdDouble.SIZE);
		return rrdFile.readDouble();
	}

}
