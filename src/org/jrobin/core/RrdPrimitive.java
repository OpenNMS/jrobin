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
	
	private RrdUpdater parent;
	private long pointer;
	private int byteCount;
	
	RrdPrimitive(RrdUpdater parent, int byteCount) throws IOException {
		this.parent = parent;
		// this will set pointer and byteCount
		parent.getRrdFile().allocate(this, byteCount);
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
	
	RrdUpdater getParent() {
		return parent;
	}
	
	RrdFile getRrdFile() {
		return parent.getRrdFile();
	}
	
	byte[] getBytes() throws IOException {
		byte[] b = new byte[byteCount];
		RrdFile rrdFile = getRrdFile();
		rrdFile.seek(pointer);
		int bytesRead = rrdFile.read(b);
		if(bytesRead != byteCount) {
			throw new IOException("Could not read enough bytes (" + byteCount + 
				" bytes requested, " + bytesRead + " bytes obtained");
		}
		return b;
	}

	void writeBytes(byte[] b) throws IOException {
		if(b.length != byteCount) {
			throw new IOException("Invalid number of bytes supplied (" + b.length +
				"), exactly " + byteCount + " needed");
		}
		RrdFile rrdFile = getRrdFile();
		rrdFile.seek(pointer);
		rrdFile.write(b);
	}

}
