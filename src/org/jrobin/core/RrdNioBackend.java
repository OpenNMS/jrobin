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
import java.nio.channels.FileChannel;
import java.nio.MappedByteBuffer;

/**
 * JRobin backend which is used to store RRD data to ordinary files on the disk
 * by using java.nio.* package.
 */
public class RrdNioBackend extends RrdFileBackend {
	FileChannel.MapMode mapMode;
	MappedByteBuffer byteBuffer;

	RrdNioBackend(String path, boolean readOnly, int lockMode) throws IOException {
		super(path, readOnly, lockMode);
		mapMode = readOnly? FileChannel.MapMode.READ_ONLY: FileChannel.MapMode.READ_WRITE;
		byteBuffer = file.getChannel().map(mapMode, 0, getLength());
	}

	/**
	 * Sets length of the underlying RRD file. This method is called only once, immediately
	 * after a new RRD file gets created.
	 * @param length Length of the RRD file
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected void setLength(long length) throws IOException {
		super.setLength(length);
		byteBuffer = file.getChannel().map(mapMode, 0, length);
	}

	/**
	 * Writes bytes to the underlying RRD file on the disk
	 * @param offset Starting file offset
	 * @param b Bytes to be written.
	 * @throws IOException Thrown in case of I/O error
	 */
	protected void write(long offset, byte[] b) throws IOException {
		byteBuffer.position((int)offset);
		byteBuffer.put(b);
	}

	/**
	 * Reads a number of bytes from the RRD file on the disk
	 * @param offset Starting file offset
	 * @param b Buffer which receives bytes read from the file.
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected void read(long offset, byte[] b) throws IOException {
		byteBuffer.position((int)offset);
		byteBuffer.get(b);
	}

   	/**
	 * Closes the underlying RRD file.
	 * @throws IOException Thrown in case of I/O error
	 */
	public void close() throws IOException {
		super.close();
	}

	/**
	 * This method forces all data cached in memory but not yet stored in the file,
	 * to be stored in it. RrdNioBackend uses (a lot of) memory to cache I/O data.
	 * This method is automatically invoked when the {@link #close()}
	 * method is called. In other words, you don't have to call sync() before you call close().<p>
	 *
	 * @throws IOException Thrown in case of I/O error
	 */
	protected void sync() throws IOException {
		byteBuffer.force();
	}

}
