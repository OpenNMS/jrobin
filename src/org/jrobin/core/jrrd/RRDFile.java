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
package org.jrobin.core.jrrd;

import java.io.*;

/**
 * This class is a quick hack to read information from an RRD file. Writing
 * to RRD files is not currently supported. As I said, this is a quick hack.
 * Some thought should be put into the overall design of the file IO.
 * <p/>
 * Currently this can read RRD files that were generated on Solaris (Sparc)
 * and Linux (x86).
 *
 * @author <a href="mailto:ciaran@codeloop.com">Ciaran Treanor</a>
 * @version $Revision$
 */
public class RRDFile implements Constants {

	boolean bigEndian;
	boolean debug;
	int alignment;
	RandomAccessFile ras;
	byte[] buffer;

	RRDFile(String name) throws IOException {
		this(new File(name));
	}

	RRDFile(File file) throws IOException {

		ras = new RandomAccessFile(file, "r");
		buffer = new byte[128];

		this.debug = false;
		initDataLayout(file);
	}

	private void initDataLayout(File file) throws IOException {

		if (file.exists()) {	// Load the data formats from the file
			int bytes = ras.read(buffer, 0, 24);
			if (bytes < 24) {
				throw new IOException("Invalid RRD file");
			}

			int index;

			if ((index = indexOf(FLOAT_COOKIE_BIG_ENDIAN, buffer)) != -1) {
				bigEndian = true;
			}
			else if ((index = indexOf(FLOAT_COOKIE_LITTLE_ENDIAN, buffer))
					!= -1) {
				bigEndian = false;
			}
			else {
				throw new IOException("Invalid RRD file");
			}

			switch (index) {

				case 12:
					alignment = 4;
					break;

				case 16:
					alignment = 8;
					break;

				default :
					throw new RuntimeException("Unsupported architecture");
			}
		}
		else {				// Default to data formats for this hardware architecture
		}

		ras.seek(0);	// Reset file pointer to start of file
	}

	private int indexOf(byte[] pattern, byte[] array) {
		return (new String(array)).indexOf(new String(pattern));
	}

	boolean isBigEndian() {
		return bigEndian;
	}

	int getAlignment() {
		return alignment;
	}

	double readDouble() throws IOException {
		if(debug) {
			System.out.print("Read 8 bytes (Double) from offset "+ras.getFilePointer()+":");
		}

		//double value;
		byte[] tx = new byte[8];

		ras.read(buffer, 0, 8);

		if (bigEndian) {
			tx = buffer;
		}
		else {
			for (int i = 0; i < 8; i++) {
				tx[7 - i] = buffer[i];
			}
		}

		DataInputStream reverseDis =
				new DataInputStream(new ByteArrayInputStream(tx));

		Double result = reverseDis.readDouble();
		if(this.debug) {
			System.out.println(result);
		}
		return result;
	}

	int readInt() throws IOException {
		return readInt(false);
	}

	int readInt(boolean dump) throws IOException {
		//An integer is "alignment" bytes long - 4 bytes on 32-bit, 8 on 64-bit.
		if(this.debug) {
			System.out.print("Read "+alignment+" bytes (int) from offset "+ras.getFilePointer()+":");
		}

		ras.read(buffer, 0, alignment);

		long value;

		if (bigEndian) {
			if(alignment == 8) {
				value = (0xFF & buffer[7]) | ((0xFF & buffer[6]) << 8)
						| ((0xFF & buffer[5]) << 16) | ((0xFF & buffer[4]) << 24)
						| ((0xFF & buffer[3]) << 32) | ((0xFF & buffer[2]) << 40)
						| ((0xFF & buffer[1]) << 48) | ((0xFF & buffer[0]) << 56);
			} else {
				value = (0xFF & buffer[3]) | ((0xFF & buffer[2]) << 8)
						| ((0xFF & buffer[1]) << 16) | ((0xFF & buffer[0]) << 24);
			}
		}
		else {
			if(alignment == 8) {
				value = (0xFF & buffer[0]) | ((0xFF & buffer[1]) << 8)
					| ((0xFF & buffer[2]) << 16) | ((0xFF & buffer[3]) << 24)
					| ((0xFF & buffer[4]) << 32) | ((0xFF & buffer[5]) << 40)
					| ((0xFF & buffer[6]) << 48) | ((0xFF & buffer[7]) << 56);
			} else {
				value = (0xFF & buffer[0]) | ((0xFF & buffer[1]) << 8)
					| ((0xFF & buffer[2]) << 16) | ((0xFF & buffer[3]) << 24);
			}
		}

		if(this.debug) {
			System.out.println(value);
		}
		return (int)value;
	}

	String readString(int maxLength) throws IOException {
		if(this.debug) {
			System.out.print("Read "+maxLength+" bytes (string) from offset "+ras.getFilePointer()+":");
		}
		ras.read(buffer, 0, maxLength);

		String result = new String(buffer, 0, maxLength).trim();
		if(this.debug) {
			System.out.println( result +":");
		}
		return result;
	}

	void skipBytes(final int n) throws IOException {
		int bytesSkipped = ras.skipBytes(n);
		if(this.debug) {
			System.out.println("Skipping "+bytesSkipped+" bytes");
		}
	}

	int align(int boundary) throws IOException {

		int skip = (int) (boundary - (ras.getFilePointer() % boundary)) % boundary;

		if (skip != 0) {
			skip = ras.skipBytes(skip);
		}
		if(this.debug) {
			System.out.println("Aligning to boundary "+ boundary +".  Offset is now "+ras.getFilePointer());
		}
		return skip;
	}

	int align() throws IOException {
		return align(alignment);
	}

	long info() throws IOException {
		return ras.getFilePointer();
	}

	long getFilePointer() throws IOException {
		return ras.getFilePointer();
	}

	void close() throws IOException {
		ras.close();
	}
}
