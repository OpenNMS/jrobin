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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to represent RRD file on the disk. All disk I/O operations are performed
 * through object of this class. <code>RrdFile</code> is a light wrapper around
 * <code>java.io.RandomAccessFile</code>.
 *
 * @author <a href="mailto:saxon@jrobin.org">Sasa Markovic</a>
 */
public class RrdFile extends RandomAccessFile {
	static final int MODE_NORMAL = 0;
	static final int MODE_RESTORE = 1;
	static final int MODE_CREATE = 2;	

	static final long LOCK_DELAY = 500; // 0.5sec
	static final String FLAGS = "rw";   // R/W access
	static int lockMode = RrdDb.NO_LOCKS;

	private String filePath;
	private FileLock fileLock;
	private List primitives = new LinkedList();
	private int mode;

	RrdFile(String filePath, int mode) throws IOException {
		super(filePath, FLAGS);
		this.filePath = filePath;
		this.mode = mode;
		lockFile();
	}

	private void lockFile() throws IOException {
		if(lockMode == RrdDb.WAIT_IF_LOCKED || lockMode == RrdDb.EXCEPTION_IF_LOCKED) {
			FileChannel fileChannel = getChannel();
			do {
				fileLock = fileChannel.tryLock();
				if(fileLock == null) {
					// could not obtain lock
					if(lockMode == RrdDb.WAIT_IF_LOCKED) {
						// wait a little, than try again
						try {
							Thread.sleep(LOCK_DELAY);
						} catch (InterruptedException e) {
							// NOP
						}
					}
					else {
						throw new IOException("Access denied. " +
							"File [" + filePath + "] already locked");
					}
				}
			} while(fileLock == null);
		}
	}

	/**
	 * Closes the underlying RandomAccessFile object.
	 * @throws IOException Thrown in case of I/O error
	 */
	public void close() throws IOException {
		unlockFile();
		super.close();
	}

	private void unlockFile() throws IOException {
		if(fileLock != null) {
			fileLock.release();
			fileLock = null;
		}
	}

	protected void finalize() throws IOException {
		close();
	}
	
	RrdPrimitive getPrimitive(int index) {
		return (RrdPrimitive) primitives.get(index);
	}
	
	void allocate(RrdPrimitive primitive, int byteCount) throws IOException {
		long pointer = getNextPointer();
		primitive.setPointer(pointer);
		primitive.setByteCount(byteCount);
		primitives.add(primitive);
	}
	
	private long getNextPointer() {
		long pointer = 0;
		int count = primitives.size();
		if(count > 0) {
			RrdPrimitive lastPrimitive = getPrimitive(count - 1);
			pointer = lastPrimitive.getPointer() + lastPrimitive.getByteCount();  		 
		}
		return pointer;
	}	

	void truncateFile() throws IOException {
		setLength(getNextPointer());
	}

	boolean isEndReached() throws IOException {
		return getNextPointer() == length();
	}

	/**
	 * Returns path to RRD file on disk.
	 * @return RRD file path.
	 */
	public String getFilePath() {
		return filePath;
	}
	
	/**
	 * Returns canonical path to RRD file on disk.
	 * @return RRD file path.
	 * @throws IOException Thrown in case of I/O error
	 */
	public String getCanonicalFilePath() throws IOException {
		return new File(filePath).getCanonicalPath();
	}

	/**
	 * Returns RRD file length. Once created, RRD file has the same length,
	 * no matter how many times you update it.
	 * @return RRD file size.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public long getFileSize() throws IOException {
		return length();
	}

	static int getLockMode() {
		return lockMode;
	}

	static void setLockMode(int lockMode) {
		RrdFile.lockMode = lockMode;
	}
	
	int getMode() {
		return mode;
	}

	void setMode(int mode) {
		this.mode = mode;
	}

}
