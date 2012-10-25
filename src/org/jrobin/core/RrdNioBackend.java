/*******************************************************************************
 * Copyright (c) 2001-2005 Sasa Markovic and Ciaran Treanor.
 * Copyright (c) 2011 The OpenNMS Group, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *******************************************************************************/

package org.jrobin.core;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Timer;
import java.util.TimerTask;

import sun.nio.ch.DirectBuffer;

/**
 * JRobin backend which is used to store RRD data to ordinary disk files
 * by using fast java.nio.* package. This is the default backend engine since JRobin 1.4.0.
 */
@SuppressWarnings("restriction")
public class RrdNioBackend extends RrdFileBackend {
	private static final Timer fileSyncTimer = new Timer(true);

	private MappedByteBuffer m_byteBuffer;
	private final TimerTask m_syncTask = new TimerTask() {
		public void run() {
			sync();
		}
	};

	/**
	 * Creates RrdFileBackend object for the given file path, backed by java.nio.* classes.
	 *
	 * @param path	   Path to a file
	 * @param m_readOnly   True, if file should be open in a read-only mode. False otherwise
	 * @param syncPeriod See {@link RrdNioBackendFactory#setSyncPeriod(int)} for explanation
	 * @throws IOException Thrown in case of I/O error
	 */
	protected RrdNioBackend(final String path, final boolean readOnly, final int syncPeriod) throws IOException {
		super(path, readOnly);
		try {
			mapFile();
			if (!readOnly) {
				fileSyncTimer.schedule(m_syncTask, syncPeriod * 1000L, syncPeriod * 1000L);
			}
		}
		catch (final IOException ioe) {
		    super.close();
			throw ioe;
		}
	}

	private void mapFile() throws IOException {
		final long length = getLength();
		if (length > 0) {
			final FileChannel.MapMode mapMode = isReadOnly() ? FileChannel.MapMode.READ_ONLY : FileChannel.MapMode.READ_WRITE;
			m_byteBuffer = file.getChannel().map(mapMode, 0, length);
		}
	}

    private void unmapFile() {
		if (m_byteBuffer != null) {
			if (m_byteBuffer instanceof DirectBuffer) {
				((DirectBuffer) m_byteBuffer).cleaner().clean();
			}
			m_byteBuffer = null;
		}
	}

	/**
	 * Sets length of the underlying RRD file. This method is called only once, immediately
	 * after a new RRD file gets created.
	 *
	 * @param newLength Length of the RRD file
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected synchronized void setLength(final long newLength) throws IOException {
		unmapFile();
		super.setLength(newLength);
		mapFile();
	}

	/**
	 * Writes bytes to the underlying RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b	  Bytes to be written.
	 */
	protected synchronized void write(final long offset, final byte[] b) throws IOException {
		if (m_byteBuffer != null) {
			m_byteBuffer.position((int) offset);
			m_byteBuffer.put(b);
		}
		else {
			throw new IOException("Write failed, file " + getPath() + " not mapped for I/O");
		}
	}

	/**
	 * Reads a number of bytes from the RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b	  Buffer which receives bytes read from the file.
	 */
	protected synchronized void read(final long offset, final byte[] b) throws IOException {
		if (m_byteBuffer != null) {
			m_byteBuffer.position((int) offset);
			m_byteBuffer.get(b);
		}
		else {
			throw new IOException("Read failed, file " + getPath() + " not mapped for I/O");
		}
	}

	/**
	 * Closes the underlying RRD file.
	 *
	 * @throws IOException Thrown in case of I/O error
	 */
	public synchronized void close() throws IOException {
		// cancel synchronization
		try {
			if (m_syncTask != null) {
				m_syncTask.cancel();
			}
			sync();
			unmapFile();
		}
		finally {
			super.close();
		}
	}

	/**
	 * This method forces all data cached in memory but not yet stored in the file,
	 * to be stored in it.
	 */
	protected synchronized void sync() {
		if (m_byteBuffer != null) {
			m_byteBuffer.force();
		}
	}
}
