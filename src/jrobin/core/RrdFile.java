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

package jrobin.core;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

/**
 * Class to represent RRD file on the disk. All disk I/O operations are performed
 * through object of this class. <code>RrdFile</code> is a light wrapper around
 * <code>java.io.RandomAccessFile</code>.
 *
 * @author <a href="mailto:saxon@eunet.yu">Sasa Markovic</a> 
 */
public class RrdFile {
	/** Maximum acceptable string length (20).*/
	public static int STRING_LENGTH = 20;
	static final int STRING_SIZE = STRING_LENGTH * 2;
	static final int INT_SIZE = 4;
	static final int LONG_SIZE = 8;
	static final int DOUBLE_SIZE = 8;

	static final long LOCK_DELAY = 500; // 0.5sec
	static int lockMode = RrdDb.NO_LOCKS;

	private RandomAccessFile file;
	private boolean safeMode = true;
	private long bookmark;
	private String filePath;
	private FileLock fileLock;

	RrdFile(String filePath) throws IOException {
		this.filePath = filePath;
		file = new RandomAccessFile(filePath, "rw");
		lockFile();
	}

	private void lockFile() throws IOException {
		if(lockMode == RrdDb.WAIT_IF_LOCKED || lockMode == RrdDb.EXCEPTION_IF_LOCKED) {
			FileChannel fileChannel = file.getChannel();
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

	void close() throws IOException {
		unlockFile();
		if(file != null) {
			file.close();
			file = null;
		}
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

	void truncate() throws IOException {
		file.setLength(file.getFilePointer());
	}

	boolean isEndReached() throws IOException {
		return file.getFilePointer() == file.length();
	}

	long allocate(int typeSize, int count) throws IOException {
		assert typeSize == STRING_SIZE || typeSize == INT_SIZE ||
			typeSize == LONG_SIZE || typeSize == DOUBLE_SIZE;
		long pointer = file.getFilePointer();
		file.seek(pointer + typeSize * count);
		return pointer;
	}

	long allocate(int intVal) throws IOException {
		long pointer = file.getFilePointer();
		file.writeInt(intVal);
		return pointer;
	}

	long allocate(long longVal) throws IOException {
		long pointer = file.getFilePointer();
		file.writeLong(longVal);
		return pointer;
	}

	long allocate(double doubleVal) throws IOException {
		long pointer = file.getFilePointer();
		file.writeDouble(doubleVal);
		return pointer;
	}

	long allocate(String stringVal) throws IOException {
		long pointer = file.getFilePointer();
		writeStringInternal(stringVal);
		return pointer;
	}

	int readInt(long pointer) throws IOException {
		prepareIO(pointer);
		int result = file.readInt();
		finalizeIO();
		return result;
	}

	long readLong(long pointer) throws IOException {
		prepareIO(pointer);
		long result = file.readLong();
		finalizeIO();
		return result;
	}

	double readDouble(long pointer) throws IOException {
		prepareIO(pointer);
		double result = file.readDouble();
		finalizeIO();
		return result;
	}

	String readString(long pointer) throws IOException {
		prepareIO(pointer);
		char[] chars = new char[STRING_LENGTH];
		for(int i = 0; i < STRING_LENGTH; i++) {
			chars[i] = file.readChar();
		}
		String result = new String(chars).trim();
		finalizeIO();
		return result;
	}

	void writeInt(long pointer, int value) throws IOException {
		prepareIO(pointer);
		file.writeInt(value);
		finalizeIO();
	}

	void writeLong(long pointer, long value) throws IOException {
		prepareIO(pointer);
		file.writeLong(value);
		finalizeIO();
	}

	void writeDouble(long pointer, double value) throws IOException {
		prepareIO(pointer);
		file.writeDouble(value);
		finalizeIO();
	}

	void writeString(long pointer, String value) throws IOException {
		prepareIO(pointer);
		writeStringInternal(value);
		finalizeIO();
	}

	private void writeStringInternal(String value) throws IOException {
		for(int i = 0; i < STRING_LENGTH; i++) {
			if(i < value.length()) {
				file.writeChar(value.charAt(i));
			}
			else {
				file.writeChar(' ');
			}
		}
	}

	private void prepareIO(long pointer) throws IOException {
		if(safeMode) {
			bookmark = file.getFilePointer();
		}
		file.seek(pointer);
	}

	private void finalizeIO() throws IOException {
        if(safeMode) {
			file.seek(bookmark);
		}
	}

	boolean isSafeMode() {
		return safeMode;
	}

	void setSafeMode(boolean safeMode) {
		this.safeMode = safeMode;
	}

	/**
	 * Returns path to RRD file on disk.
	 * @return RRD file path.
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Returns RRD file length. Once created, RRD file has the same length,
	 * no matter how many times you update it.
	 * @return RRD file size.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public long getFileSize() throws IOException {
		return file.length();
	}

	static int getLockMode() {
		return lockMode;
	}

	static void setLockMode(int lockMode) {
		RrdFile.lockMode = lockMode;
	}
}
