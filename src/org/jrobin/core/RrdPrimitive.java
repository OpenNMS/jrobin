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

abstract class RrdPrimitive {
	static final int STRING_LENGTH = 20;
	static final int RRD_INT = 0, RRD_LONG = 1, RRD_DOUBLE = 2, RRD_STRING = 3;
	static final int[] RRD_PRIM_SIZES = { 4, 8, 8, 2 * STRING_LENGTH };

	private RrdBackend backend;
	private int byteCount;
	private final long pointer;

	RrdPrimitive(RrdUpdater updater, int type) throws IOException {
		this(updater, type, 1);
	}

	RrdPrimitive(RrdUpdater updater, int type, int count) throws IOException {
		this.backend = updater.getRrdBackend();
		this.byteCount = RRD_PRIM_SIZES[type] * count;
		this.pointer = updater.getRrdAllocator().allocate(byteCount);
	}
	
	byte[] readBytes() throws IOException {
		byte[] b = new byte[byteCount];
		backend.read(pointer, b);
		return b;
	}

	void writeBytes(byte[] b) throws IOException {
		assert b.length == byteCount: "Invalid number of bytes supplied to RrdPrimitive.write method";
		backend.write(pointer, b);
	}

	int readInt() throws IOException {
		return backend.readInt(pointer);
	}

	void writeInt(int value) throws IOException {
		backend.writeInt(pointer, value);
	}

	long readLong() throws IOException {
		return backend.readLong(pointer);
	}

	void writeLong(long value) throws IOException {
		backend.writeLong(pointer, value);
	}

	double readDouble() throws IOException {
		return backend.readDouble(pointer);
	}

	double readDouble(int index) throws IOException {
		long offset = pointer + index * RRD_PRIM_SIZES[RRD_DOUBLE];
		return backend.readDouble(offset);
	}

	double[] readDouble(int index, int count) throws IOException {
		long offset = pointer + index * RRD_PRIM_SIZES[RRD_DOUBLE];
		return backend.readDouble(offset, count);
	}

	void writeDouble(double value) throws IOException {
		backend.writeDouble(pointer, value);
	}

	void writeDouble(int index, double value, int count) throws IOException {
		long offset = pointer + index * RRD_PRIM_SIZES[RRD_DOUBLE];
		backend.writeDouble(offset, value, count);
	}

	void writeDouble(int index, double[] values) throws IOException {
		long offset = pointer + index * RRD_PRIM_SIZES[RRD_DOUBLE];
		backend.writeDouble(offset, values);
	}

	String readString() throws IOException {
		return backend.readString(pointer);
	}

	void writeString(String value) throws IOException {
		backend.writeString(pointer, value);
	}
}
