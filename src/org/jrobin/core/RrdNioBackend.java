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
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import sun.nio.ch.DirectBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * JRobin backend which is used to store RRD data to ordinary disk files
 * by using fast java.nio.* package. This is the default backend engine since JRobin 1.4.0.
 */
public class RrdNioBackend extends RrdFileBackend {
	private static final Timer fileSyncTimer = new Timer(true);

	private MappedByteBuffer byteBuffer;
	private int syncMode;
	private TimerTask syncTask;

	/**
	 * Creates RrdFileBackend object for the given file path, backed by java.nio.* classes.
	 * @param path Path to a file
	 * @param readOnly True, if file should be open in a read-only mode. False otherwise
	 * @param lockMode Locking mode, as described in {@link RrdDb#getLockMode()}
	 * @param syncMode See {@link RrdNioBackendFactory#setSyncMode(int)} for explanation
	 * @param syncPeriod See {@link RrdNioBackendFactory#setSyncMode(int)} for explanation
	 * @throws IOException Thrown in case of I/O error
	 */
	protected RrdNioBackend(String path, boolean readOnly, int lockMode, int syncMode, int syncPeriod)
			throws IOException {
		super(path, readOnly, lockMode);
		this.syncMode = syncMode;
		mapFile();
		if(syncMode == RrdNioBackendFactory.SYNC_BACKGROUND && !readOnly) {
			syncTask = new TimerTask() {
				public void run() {
					sync();
				}
			};
			fileSyncTimer.schedule(syncTask, syncPeriod * 1000L, syncPeriod * 1000L);
		}
	}

	private void mapFile() throws IOException {
		long length = getLength();
		if(length > 0) {
			FileChannel.MapMode mapMode =
				readOnly? FileChannel.MapMode.READ_ONLY: FileChannel.MapMode.READ_WRITE;
			byteBuffer = channel.map(mapMode, 0, length);
		}
	}

	private void unmapFile() {
		if(byteBuffer != null) {
			((DirectBuffer) byteBuffer).cleaner().clean();
			byteBuffer = null;
		}
	}

	/**
	 * Sets length of the underlying RRD file. This method is called only once, immediately
	 * after a new RRD file gets created.
	 * @param newLength Length of the RRD file
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected synchronized void setLength(long newLength) throws IOException {
		unmapFile();
		super.setLength(newLength);
		mapFile();
	}

	/**
	 * Writes bytes to the underlying RRD file on the disk
	 * @param offset Starting file offset
	 * @param b Bytes to be written.
	 */
	protected synchronized void write(long offset, byte[] b) throws IOException {
		if(byteBuffer != null) {
			byteBuffer.position((int)offset);
			byteBuffer.put(b);
		}
		else {
			throw new IOException("Write failed, file " + getPath() + " not mapped for I/O");
		}
	}

	/**
	 * Reads a number of bytes from the RRD file on the disk
	 * @param offset Starting file offset
	 * @param b Buffer which receives bytes read from the file.
	 */
	protected synchronized void read(long offset, byte[] b) throws IOException {
		if(byteBuffer != null) {
			byteBuffer.position((int)offset);
			byteBuffer.get(b);
		}
		else {
			throw new IOException("Read failed, file " + getPath() + " not mapped for I/O");
		}
	}

   	/**
	 * Closes the underlying RRD file.
	 * @throws IOException Thrown in case of I/O error
	 */
	public synchronized void close() throws IOException {
		// cancel synchronization
		if(syncTask != null) {
			syncTask.cancel();
		}
		unmapFile();
		super.close();
	}

	/**
	 * This method forces all data cached in memory but not yet stored in the file,
	 * to be stored in it.
	 */
	protected synchronized void sync() {
		if(byteBuffer != null) {
			byteBuffer.force();
		}
	}

	/**
	 * Method called by the framework immediatelly before RRD update operation starts. This method
	 * will synchronize in-memory cache with the disk content if synchronization mode is set to
	 * {@link RrdNioBackendFactory#SYNC_BEFOREUPDATE}. Otherwise it does nothing.
	 */
	protected void beforeUpdate() {
		if(syncMode == RrdNioBackendFactory.SYNC_BEFOREUPDATE) {
			sync();
		}
	}

	/**
	 * Method called by the framework immediatelly after RRD update operation finishes. This method
	 * will synchronize in-memory cache with the disk content if synchronization mode is set to
	 * {@link RrdNioBackendFactory#SYNC_AFTERUPDATE}. Otherwise it does nothing.
	 */
	protected void afterUpdate() {
		if(syncMode == RrdNioBackendFactory.SYNC_AFTERUPDATE) {
			sync();
		}
	}

	/**
	 * Method called by the framework immediatelly before RRD fetch operation starts. This method
	 * will synchronize in-memory cache with the disk content if synchronization mode is set to
	 * {@link RrdNioBackendFactory#SYNC_BEFOREFETCH}. Otherwise it does nothing.
	 */
	protected void beforeFetch() {
		if(syncMode == RrdNioBackendFactory.SYNC_BEFOREFETCH) {
			sync();
		}
	}

	/**
	 * Method called by the framework immediatelly after RRD fetch operation finishes. This method
	 * will synchronize in-memory cache with the disk content if synchronization mode is set to
	 * {@link RrdNioBackendFactory#SYNC_AFTERFETCH}. Otherwise it does nothing.
	 */
	protected void afterFetch() {
		if(syncMode == RrdNioBackendFactory.SYNC_AFTERFETCH) {
			sync();
		}
	}
}
