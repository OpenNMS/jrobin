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
import java.util.TimerTask;
import java.util.Timer;

/**
 * JRobin backend which is used to store RRD data to ordinary disk files
 * by using fast java.nio.* package. This is the default backend engine since JRobin 1.4.0.
 */
public class RrdNioBackend extends RrdFileBackend {
	/**
	 * Defines <code>System.gc()</code> usage policy for this backend.<p>
	 *
	 * NIO backend uses potentially large in-memory buffer to cache file data.
	 * The buffer remains 'active' (by prohibiting file re-creation with the smaller file size)
	 * as long as it is not garbage-collected. By forcing <code>System.gc()</code> call where
	 * appropriate, this backend will free in-memory buffers sooner and file re-creation won't fail.<p>
	 *
	 * The constant is set to <b><code>true</code></b> initially and currently there is no
	 * API to change it during runtime.
	 *
	 * Garbage collection will be forced only in some special circumstances.
	 * It should not affect the speed of your application significantly.<p>
	 */
	public static final boolean SHOULD_GC = true;

	private static final Timer syncTimer = new Timer(true);

	private int syncMode;
	MappedByteBuffer byteBuffer;
	private TimerTask syncTask;

	protected RrdNioBackend(String path, boolean readOnly, int lockMode, int syncMode, int syncPeriod)
			throws IOException {
		super(path, readOnly, lockMode);
		map(readOnly);
		this.syncMode = syncMode;
		if(syncMode == RrdNioBackendFactory.SYNC_BACKGROUND && !readOnly) {
			createSyncTask(syncPeriod);
		}
	}

	private void map(boolean readOnly) throws IOException {
		long length = getLength();
		if(length > 0) {
			FileChannel.MapMode mapMode =
				readOnly? FileChannel.MapMode.READ_ONLY: FileChannel.MapMode.READ_WRITE;
			byteBuffer = channel.map(mapMode, 0, length);
		}
		else {
			byteBuffer = null;
		}
	}

	private void createSyncTask(int syncPeriod) {
		syncTask = new TimerTask() {
			public void run() {
				sync();
			}
		};
		syncTimer.schedule(syncTask, syncPeriod * 1000L, syncPeriod * 1000L);
	}

	/**
	 * Sets length of the underlying RRD file. This method is called only once, immediately
	 * after a new RRD file gets created.
	 * @param newLength Length of the RRD file
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected void setLength(long newLength) throws IOException {
		if(newLength < getLength()) {
			// the file will be truncated
			if(SHOULD_GC) {
				byteBuffer = null;
				System.gc();
			}
		}
		super.setLength(newLength);
		map(false);
	}

	/**
	 * Writes bytes to the underlying RRD file on the disk
	 * @param offset Starting file offset
	 * @param b Bytes to be written.
	 */
	protected void write(long offset, byte[] b) {
		synchronized(byteBuffer) {
			byteBuffer.position((int)offset);
			byteBuffer.put(b);
		}
	}

	/**
	 * Reads a number of bytes from the RRD file on the disk
	 * @param offset Starting file offset
	 * @param b Buffer which receives bytes read from the file.
	 */
	protected void read(long offset, byte[] b) {
		synchronized(byteBuffer) {
			byteBuffer.position((int)offset);
			byteBuffer.get(b);
		}
	}

   	/**
	 * Closes the underlying RRD file.
	 * @throws IOException Thrown in case of I/O error
	 */
	public void close() throws IOException {
		// cancel synchronization
		if(syncTask != null) {
			syncTask.cancel();
		}
		// synchronize with the disk for the last time
		sync();
		// release the buffer, make it eligible for GC as soon as possible
		byteBuffer = null;
		// close the underlying file		
		super.close();
	}

	/**
	 * This method forces all data cached in memory but not yet stored in the file,
	 * to be stored in it. RrdNioBackend uses (a lot of) memory to cache I/O data.
	 * This method is automatically invoked when the {@link #close()}
	 * method is called. In other words, you don't have to call sync() before you call close().<p>
	 */
	public void sync() {
		if(byteBuffer != null) {
			synchronized(byteBuffer) {
				// System.out.println("** SYNC **");
				byteBuffer.force();
			}
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
