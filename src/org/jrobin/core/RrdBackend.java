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

/**
 * Base implementation class for all backend classes. Each Round Robin Database object
 * ({@link RrdDb} object) is backed with a single RrdBackend object which performs
 * actual I/O operations on the underlying storage. JRobin supports
 * three different bakcends out of the box:</p>
 * <ul>
 * <li>{@link RrdFileBackend}: objects of this class are created from the
 * {@link RrdFileBackendFactory} class. This was the default backend used in all
 * JRobin releases prior to 1.4.0. It uses java.io.* package and
 * RandomAccessFile class to store RRD data in files on the disk.
 *
 * <li>{@link RrdNioBackend}: objects of this class are created from the
 * {@link RrdNioBackendFactory} class. The backend uses java.io.* and java.nio.*
 * classes (mapped ByteBuffer) to store RRD data in files on the disk. This backend is fast, very fast,
 * but consumes a lot of memory (borrowed not from the JVM but from the underlying operating system
 * directly). <b>This is the default backend used in JRobin since 1.4.0 release.</b>
 *
 * <li>{@link RrdMemoryBackend}: objects of this class are created from the
 * {@link RrdMemoryBackendFactory} class. This backend stores all data in memory. Once
 * JVM exits, all data gets lost. The backend is extremely fast and memory hungry.
 * </ul>
 *
 * To create your own backend in order to provide some custom type of RRD storage,
 * you should do the following:</p>
 *
 * <ul>
 * <li>Create your custom RrdBackend class (RrdCustomBackend, for example)
 * by extending RrdBackend class. You have to implement all abstract methods defined
 * in the base class.
 *
 * <li>Create your custom RrdBackendFactory class (RrdCustomBackendFactory,
 * for example) by extending RrdBackendFactory class. You have to implement all
 * abstract methods defined in the base class. Your custom factory class will actually
 * create custom backend objects when necessary.
 *
 * <li>Create instance of your custom RrdBackendFactory and register it as a regular
 * factory available to JRobin framework. See javadoc for {@link RrdBackendFactory} to
 * find out how to do this
 * </ul>
 *
 */
public abstract class RrdBackend {
	private String path;
	private static long count = 0;

	/**
	 * Creates backend for a RRD storage with the given path.
	 * @param path String identifying RRD storage. For files on the disk, this
	 * argument should represent file path. Other storage types might interpret
	 * this argument differently.
	 */
	protected RrdBackend(String path) {
		this.path = path;
		count++;
	}

	/**
	 * Returns path to the storage.
	 * @return Storage path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Writes an array of bytes to the underlying storage starting from the given
	 * storage offset.
	 * @param offset Storage offset.
	 * @param b Array of bytes that should be copied to the underlying storage
	 * @throws IOException Thrown in case of I/O error
	 */
	protected abstract void write(long offset, byte[] b) throws IOException;

	/**
	 * Reads an array of bytes from the underlying storage starting from the given
	 * storage offset.
	 * @param offset Storage offset.
	 * @param b Array which receives bytes from the underlying storage
	 * @throws IOException Thrown in case of I/O error
	 */
	protected abstract void read(long offset, byte[] b) throws IOException;

	/**
	 * Reads all RRD bytes from the underlying storage
	 * @return RRD bytes
	 * @throws IOException Thrown in case of I/O error
	 */
	public final byte[] readAll() throws IOException {
		byte[] b = new byte[(int) getLength()];
		read(0, b);
		return b;
	}

	/**
	 * Returns the number of RRD bytes in the underlying storage.
	 * @return Number of RRD bytes in the storage.
	 * @throws IOException Thrown in case of I/O error.
	 */
	public abstract long getLength() throws IOException;

	/**
	 * Sets the number of bytes in the underlying RRD storage.
	 * This method is called only once, immediately after a new RRD storage gets created.
	 * @param length Length of the underlying RRD storage in bytes.
	 * @throws IOException Thrown in case of I/O error.
	 */
	protected abstract void setLength(long length) throws IOException;

	/**
	 * Closes the underlying storage. Calls sync() implicitly.
	 * In other words, you don't have to call sync() before close() in order to preserve
	 * data cached in memory.
	 * @throws IOException Thrown in case of I/O error
	 */
	public void close() throws IOException {
		sync();
	}

	/**
	 * Method called by the framework immediatelly before RRD update operation starts. This method
	 * does nothing, but can be overriden in subclasses.
	 */
	protected void beforeUpdate() throws IOException {
	}

	/**
	 * Method called by the framework immediatelly after RRD update operation is completed. This method
	 * does nothing, but can be overriden in subclasses.
	 */
	protected void afterUpdate() throws IOException {
	}

	/**
	 * Method called by the framework immediatelly before RRD fetch operation starts. This method
	 * does nothing, but can be overriden in subclasses.
	 */
	protected void beforeFetch() throws IOException {
	}

	/**
	 * Method called by the framework immediatelly after RRD fetch operation is completed. This method
	 * does nothing, but can be overriden in subclasses.
	 */
	protected void afterFetch() throws IOException {
	}

	/**
	 * Method called by the framework immediatelly after RrdDb obejct is created. This method
	 * does nothing, but can be overriden in subclasses.
	 */
	protected void afterCreate() throws IOException {
	}

	/**
	 * This method forces all data cached in memory but not yet stored in the persistant
	 * storage, to be stored in it. In the base class this method does nothing but
	 * subclasses might provide real functionality.<p>
	 *
	 * @throws IOException Thrown in case of I/O error
	 */
	public void sync() throws IOException {
	}

	final void writeInt(long offset, int value) throws IOException {
		write(offset, getIntBytes(value));
	}

	final void writeLong(long offset, long value) throws IOException {
		write(offset, getLongBytes(value));
	}

	final void writeDouble(long offset, double value) throws IOException {
		write(offset, getDoubleBytes(value));
	}

	final void writeDouble(long offset, double value, int count) throws IOException {
		byte[] b = getDoubleBytes(value);
		byte[] image = new byte[8 * count];
		for(int i = 0, k = 0; i < count; i++) {
			image[k++] = b[0];
			image[k++] = b[1];
			image[k++] = b[2];
			image[k++] = b[3];
			image[k++] = b[4];
			image[k++] = b[5];
			image[k++] = b[6];
			image[k++] = b[7];
		}
		write(offset, image);
		image = null;
	}

	final void writeDouble(long offset, double[] values) throws IOException {
		int count = values.length;
		byte[] image = new byte[8 * count];
		for(int i = 0, k = 0; i < count; i++) {
			byte[] b = getDoubleBytes(values[i]);
			image[k++] = b[0];
			image[k++] = b[1];
			image[k++] = b[2];
			image[k++] = b[3];
			image[k++] = b[4];
			image[k++] = b[5];
			image[k++] = b[6];
			image[k++] = b[7];
		}
		write(offset, image);
		image = null;
	}

	final void writeString(long offset, String value) throws IOException {
		value = value.trim();
		byte[] b = new byte[RrdPrimitive.STRING_LENGTH * 2];
		for(int i = 0, k = 0; i < RrdPrimitive.STRING_LENGTH; i++) {
			char c = (i < value.length())? value.charAt(i): ' ';
			byte[] cb = getCharBytes(c);
			b[k++] = cb[0];
			b[k++] = cb[1];
		}
		write(offset, b);
	}

	final int readInt(long offset) throws IOException {
		byte[] b = new byte[4];
		read(offset, b);
		return getInt(b);
	}

	final long readLong(long offset) throws IOException {
		byte[] b = new byte[8];
		read(offset, b);
		return getLong(b);
	}

	final double readDouble(long offset) throws IOException {
		byte[] b = new byte[8];
		read(offset, b);
		return getDouble(b);
	}

	final double[] readDouble(long offset, int count) throws IOException {
		int byteCount = 8 * count;
		byte[] image = new byte[byteCount];
		read(offset, image);
		double[] values = new double[count];
		for(int i = 0, k = -1; i < count; i++) {
			byte[] b = new byte[] {
				image[++k], image[++k], image[++k], image[++k],
				image[++k], image[++k], image[++k], image[++k]
			};
			values[i] = getDouble(b);
		}
		image = null;
		return values;
	}

	final String readString(long offset) throws IOException {
		byte[] b = new byte[RrdPrimitive.STRING_LENGTH * 2];
		char[] c = new char[RrdPrimitive.STRING_LENGTH];
		read(offset, b);
		for(int i = 0, k = -1; i < RrdPrimitive.STRING_LENGTH; i++) {
			byte[] cb = new byte[] { b[++k], b[++k] };
			c[i] = getChar(cb);
		}
		return new String(c).trim();
	}

	// static helper methods

	private final static byte[] getIntBytes(int value) {
		byte[] b = new byte[4];
		b[0] = (byte)((value >>> 24) & 0xFF);
		b[1] = (byte)((value >>> 16) & 0xFF);
		b[2] = (byte)((value >>>  8) & 0xFF);
		b[3] = (byte)((value >>>  0) & 0xFF);
		return b;
	}

	private final static byte[] getLongBytes(long value) {
		byte[] b = new byte[8];
		b[0] = (byte)((int)(value >>> 56) & 0xFF);
		b[1] = (byte)((int)(value >>> 48) & 0xFF);
		b[2] = (byte)((int)(value >>> 40) & 0xFF);
		b[3] = (byte)((int)(value >>> 32) & 0xFF);
		b[4] = (byte)((int)(value >>> 24) & 0xFF);
		b[5] = (byte)((int)(value >>> 16) & 0xFF);
		b[6] = (byte)((int)(value >>>  8) & 0xFF);
		b[7] = (byte)((int)(value >>>  0) & 0xFF);
		return b;
	}

	private final static byte[] getCharBytes(char value) {
		byte[] b = new byte[2];
		b[0] = (byte)((value >>> 8) & 0xFF);
		b[1] = (byte)((value >>> 0) & 0xFF);
		return b;
	}

	private final static byte[] getDoubleBytes(double value) {
		byte[] bytes = getLongBytes(Double.doubleToLongBits(value));
		return bytes;
	}

	private final static int getInt(byte[] b) {
		assert b.length == 4: "Invalid number of bytes for integer conversion";
		return ((b[0] << 24) & 0xFF000000) + ((b[1] << 16) & 0x00FF0000) +
			((b[2] << 8) & 0x0000FF00) + ((b[3] << 0) & 0x000000FF);
	}

	private final static long getLong(byte[] b) {
		assert b.length == 8: "Invalid number of bytes for long conversion";
		int high = getInt(new byte[] { b[0], b[1], b[2], b[3] });
		int low = getInt(new byte[] { b[4], b[5], b[6], b[7] });
		long value = ((long)(high) << 32) + (low & 0xFFFFFFFFL);
		return value;
	}

	private final static char getChar(byte[] b) {
		assert b.length == 2: "Invalid number of bytes for char conversion";
		return (char)(((b[0] << 8) & 0x0000FF00)
				+ ((b[1] << 0) & 0x000000FF));
	}

	private final static double getDouble(byte[] b) {
		assert b.length == 8: "Invalid number of bytes for double conversion";
		return Double.longBitsToDouble(getLong(b));
	}

	static long getCount() {
		return count;
	}
}
