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

	private long pointer;
	private int byteCount;

	RrdFile rrdFile;
	boolean cached = false;

	RrdPrimitive(RrdUpdater parent, int byteCount) throws IOException {
		rrdFile = parent.getRrdFile();
		// this will set pointer and byteCount
		rrdFile.allocate(this, byteCount);
	}
	
	long getPointer() {
		return pointer;
	}

	void setPointer(long pointer) {
		this.pointer = pointer;
	}
	
	int getByteCount() {
		return byteCount;
	}
	
	void setByteCount(int byteCount) {
		this.byteCount = byteCount;
	}
	
	byte[] readBytes() throws IOException {
		byte[] b = new byte[byteCount];
		restorePosition();
		int bytesRead = rrdFile.read(b);
		assert bytesRead == byteCount: "Could not read enough bytes (" + byteCount +
				" bytes requested, " + bytesRead + " bytes obtained";
		return b;
	}

	void writeBytes(byte[] b) throws IOException {
		assert b.length == byteCount: "Invalid number of bytes supplied (" + b.length +
				"), exactly " + byteCount + " needed";
		restorePosition();
		rrdFile.write(b);
	}

	final void restorePosition() throws IOException {
		rrdFile.seek(pointer);
	}

	final void restorePosition(int unitIndex, int unitSize) throws IOException {
		rrdFile.seek(pointer + unitIndex * unitSize);
	}
}
