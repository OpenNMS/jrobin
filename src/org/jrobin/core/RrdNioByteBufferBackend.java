/* ============================================================
 * JRobin : Pure java implementation of RRDTool's functionality
 * ============================================================
 *
 * Project Info:  http://www.jrobin.org
 * Project Lead:  Sasa Markovic (saxon@jrobin.org);
 *
 * (C) Copyright 2003-2005, by Sasa Markovic.
 *
 * Developers:    Sasa Markovic (saxon@jrobin.org)
 *                <a href=mailto:david@opennms.org>David Hustace</a>
 *
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
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * JRobin backend which is used to store RRD data to ordinary disk files
 * by using fast java.nio.* package. This is the default backend engine since JRobin 1.4.0.
 */
public class RrdNioByteBufferBackend extends RrdFileBackend {
	
	boolean m_readOnly;
	
	private boolean m_printStatements = false;

	private ByteBuffer m_byteBuffer;

	private FileChannel m_ch;

	private Object m_lock = new Object();

	/**
	 * Creates RrdFileBackend object for the given file path, backed by java.nio.* classes.
	 *
	 * @param path	   Path to a file
	 * @param readOnly   True, if file should be open in a read-only mode. False otherwise
	 * @param syncPeriod See {@link RrdNioBackendFactory#setSyncPeriod(int)} for explanation
	 * @throws IOException Thrown in case of I/O error
	 */
	protected RrdNioByteBufferBackend(String path, boolean readOnly) throws IOException, IllegalStateException {
		super(path, readOnly);
		m_readOnly = readOnly;
		m_printStatements = true;
		if (m_printStatements) System.out.println("Using class: "+ getClass());
		
		if (file != null) {
			m_ch = file.getChannel();
			m_byteBuffer = ByteBuffer.allocate((int) m_ch.size());
			m_ch.read(m_byteBuffer, 0);
		} else {
			throw new IllegalStateException("File in base class is null.");
		}
	}

	/**
	 * Sets length of the underlying RRD file. This method is called only once, immediately
	 * after a new RRD file gets created.
	 *
	 * @param newLength Length of the RRD file
	 * @throws IOException 
	 * @throws IOException Thrown in case of I/O error.
	 */
	@Override
	protected void setLength(long newLength) throws IOException {
		synchronized (m_lock) {
			super.setLength(newLength);
			m_ch = file.getChannel();
			m_byteBuffer = ByteBuffer.allocate((int) newLength);
			m_ch.read(m_byteBuffer, 0);
			m_byteBuffer.position(0);
		}
	}

	/**
	 * Writes bytes to the underlying RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b	  Bytes to be written.
	 */
	@Override
	protected void write(long offset, byte[] b) {
		synchronized (m_lock) {
			m_byteBuffer.position((int) offset);
			m_byteBuffer.put(b);
		}
	}

	/**
	 * Reads a number of bytes from the RRD file on the disk
	 *
	 * @param offset Starting file offset
	 * @param b	  Buffer which receives bytes read from the file.
	 */
	@Override
	protected void read(long offset, byte[] b) {
		synchronized (m_lock) {
			m_byteBuffer.position((int) offset);
			m_byteBuffer.get(b);
		}
	}
	
	/**
	 * Closes the underlying RRD file.
	 *
	 * @throws IOException Thrown in case of I/O error
	 */
	public void close() throws IOException {
		synchronized (m_lock) {
			m_byteBuffer.position(0);
			
			if (!readOnly) m_ch.write(m_byteBuffer, 0);
			//just calling close here because the super calls close 
			//on the File object and Java calls close on the channel
			super.close();
		}
	}
		
}
